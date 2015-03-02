package edu.brown.cs.h2r.baking.Experiments;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.yaml.snakeyaml.Yaml;

import burlap.behavior.statehashing.ObjectHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.auxiliary.common.StateYAMLParser;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.SADomain;
import edu.brown.cs.h2r.baking.Agents.Agent;
import edu.brown.cs.h2r.baking.Agents.AgentHelper;
import edu.brown.cs.h2r.baking.Agents.Expert;
import edu.brown.cs.h2r.baking.Agents.Human;
import edu.brown.cs.h2r.baking.Agents.RandomRecipeAgent;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.Knowledgebase.Knowledgebase;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.MakeSpanFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ToolFactory;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeBotched;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;
import edu.brown.cs.h2r.baking.actions.BakingAction;
import edu.brown.cs.h2r.baking.actions.BakingActionResult;
import edu.brown.cs.h2r.baking.actions.MixAction;
import edu.brown.cs.h2r.baking.actions.MoveAction;
import edu.brown.cs.h2r.baking.actions.PourAction;
import edu.brown.cs.h2r.baking.actions.ResetAction;
import edu.brown.cs.h2r.baking.actions.SwitchAction;

public class SimulationHelper {
	public static Domain generateGeneralDomain() {
		Domain domain = new SADomain();
		domain.addObjectClass(ContainerFactory.createObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createSimpleIngredientObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createComplexIngredientObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createSimpleHiddenIngredientObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createComplexHiddenIngredientObjectClass(domain));
		domain.addObjectClass(SpaceFactory.createObjectClass(domain));		
		domain.addObjectClass(AgentFactory.getObjectClass(domain));
		domain.addObjectClass(ToolFactory.createObjectClass(domain));
		domain.addObjectClass(MakeSpanFactory.getObjectClass(domain));
		domain.setObjectIdentiferDependence(true);
		
		Action mix = new MixAction(domain);
		Action pour = new PourAction(domain);
		Action move = new MoveAction(domain);
		//Action grease = new GreaseAction(domain);
		Action aSwitch = new SwitchAction(domain);
		//Action use = new UseAction(domain);
		Action resetAction = new ResetAction(domain);
		//Action prepareAction = new PreparationAction(domain);
		
		
		// To the failed propFunction, add in all subgoals for a recipe that are based on an ingredient.
		RecipeBotched failed = ((RecipeBotched)domain.getPropFunction(AffordanceCreator.BOTCHED_PF));
		if (failed != null) {
			failed.clearSubgoals();
		}
		
		return domain;
	}
	
	public static State generateInitialState(Domain generalDomain, StateHashFactory hashingFactory, 
			List<Recipe> recipes, Agent agent1, Agent agent2) {
		ObjectHashFactory objectHashingFactory = hashingFactory.getObjectHashFactory();
		List<ObjectInstance> objects = new ArrayList<ObjectInstance>();
		objects.add(agent1.getAgentObject());
		
		if (agent2 != null) {
			objects.add(agent2.getAgentObject());
		}
		ObjectInstance makeSpan = MakeSpanFactory.getNewObjectInstance(generalDomain, "makespan", 2, objectHashingFactory);
		objects.add(makeSpan);
		
		List<String> containers = Arrays.asList("mixing_bowl_1", "mixing_bowl_2", "mixing_bowl_3");
		ObjectInstance counterSpace = SpaceFactory.getNewWorkingSpaceObjectInstance(generalDomain, SpaceFactory.SPACE_COUNTER, containers, "human", objectHashingFactory);
		objects.add(counterSpace);
		
		objects.add(ToolFactory.getNewSimpleToolObjectInstance(generalDomain, "whisk", "", "", SpaceFactory.SPACE_COUNTER, objectHashingFactory));
		//objects.add(ToolFactory.getNewSimpleToolObjectInstance(generalDomain, "spoon","", "", SpaceFactory.SPACE_COUNTER, objectHashingFactory));
		
		objects.add(ContainerFactory.getNewBakingContainerObjectInstance(generalDomain, "baking_dish", null, SpaceFactory.SPACE_COUNTER, objectHashingFactory));
		ObjectInstance pan = ContainerFactory.getNewHeatingContainerObjectInstance(generalDomain, "frying_pan", null, SpaceFactory.SPACE_COUNTER, objectHashingFactory);
		
		objects.add(pan);
		objects.add(SpaceFactory.getNewBakingSpaceObjectInstance(generalDomain, SpaceFactory.SPACE_OVEN, null, "", objectHashingFactory));
		objects.add(SpaceFactory.getNewHeatingSpaceObjectInstance(generalDomain, SpaceFactory.SPACE_STOVE, null, "", objectHashingFactory));
		//objects.add(ContainerFactory.getNewTrashContainerObjectInstance(generalDomain, "trash", SpaceFactory.SPACE_COUNTER, objectHashingFactory));
		
		for (String container : containers) { 
			objects.add(ContainerFactory.getNewMixingContainerObjectInstance(generalDomain, container, null, SpaceFactory.SPACE_COUNTER, objectHashingFactory));
		}
		
		// Out of all the ingredients in our kitchen, plan over only those that might be useful!
		Knowledgebase knowledgebase = Knowledgebase.getKnowledgebase(generalDomain);
		
		List<ObjectInstance> ingredients = new ArrayList<ObjectInstance>();
		List<ObjectInstance> tools = new ArrayList<ObjectInstance>();
		for (int i = 0;i < recipes.size(); i++) {
			Recipe recipe = recipes.get(i);
			ingredients.addAll(knowledgebase.getRecipeObjectInstanceList(generalDomain, objectHashingFactory, recipe));
			//tools.addAll(knowledgebase.getTools(generalDomain, SpaceFactory.SPACE_COUNTER, objectHashingFactory));
		}
		
		/*
		ObjectClass oc = ingredients.get(0).getObjectClass();
		IngredientRecipe wholeEggs = knowledgebase.getIngredient("whole_eggs");
		ObjectInstance wholeEggsObject = IngredientFactory.getNewIngredientInstance(wholeEggs, wholeEggs.getFullName(), oc, hashingFactory.getObjectHashFactory());
		int index = -1;
		for (int i = 0; i < ingredients.size(); i++) {
			ObjectInstance ingredient = ingredients.get(i);
			if (ingredient.getName().equals("eggs")) {
				index = i;
				break;
			}
		}
		if (index >= 0) {
			ingredients.set(index, wholeEggsObject);
		}*/
	
		ObjectClass containerClass = generalDomain.getObjectClass(ContainerFactory.ClassName);		
		
		List<ObjectInstance> containersAndIngredients = Recipe.getContainersAndIngredients(containerClass, ingredients, counterSpace.getName());
		objects.addAll(containersAndIngredients);
		objects.addAll(tools);
		return new State(objects);
	}
	
	public static double getActionTime(GroundedAction action,  Map<String, Double> lastActionTimes, ActionTimeGenerator timeGenerator ) {
		if (action == null) {
			return -1.0;
		}
		double time = timeGenerator.get(action, true);
		return time + lastActionTimes.get(action.params[0]);
	}
	
	public static State performActions(State state, AbstractGroundedAction action1, AbstractGroundedAction action2, Map<String, Double> lastActionTimes, 
			List<State> statePair, List<AbstractGroundedAction> actionPair, List<Double> timesPair, ActionTimeGenerator timeGenerator) {
		
		statePair.clear();
		actionPair.clear();
		timesPair.clear();
		double action1Time = SimulationHelper.getActionTime((GroundedAction)action1, lastActionTimes, timeGenerator);
		double action2Time = SimulationHelper.getActionTime((GroundedAction)action2, lastActionTimes, timeGenerator);
		
		if (action1Time < 0.0 && action2Time < 0.0) {
			System.err.println("No valid actions");
			return state;
		}
		
		boolean action1First = (action1Time > 0.0 && (action2Time < 0.0 || action1Time < action2Time));
		
		AbstractGroundedAction firstAction = (action1First) ? action1 : action2;
		double firstTime = (action1First) ? action1Time : action2Time;		
		AbstractGroundedAction secondAction = (action1First) ? action2 : action1;
		double secondTime = (action1First) ? action2Time : action1Time;
		
		if (firstTime > 0.0) {
			State nextState = firstAction.executeIn(state);
			System.out.println("First executing action " + firstAction.toString() + ", " + firstTime);
			if (nextState.equals(state)) {
				//System.out.println("Action had no effect");
			}
			state = nextState;
			statePair.add(state);
			actionPair.add(firstAction);
			timesPair.add(firstTime);
			GroundedAction groundedAction = (GroundedAction)firstAction;
			lastActionTimes.put(groundedAction.params[0], firstTime);
		}
		
		if (secondTime > 0.0 && secondTime == firstTime) {
			State nextState = secondAction.executeIn(state);
			System.out.println("Second executing action " + secondAction.toString() + ", " + secondTime);
			
			if (nextState.equals(state)) {
				//System.out.println("Action had no effect");
			}
			state = nextState;
			statePair.add(state);
			actionPair.add(firstAction);
			timesPair.add(secondTime);
			GroundedAction groundedAction = (GroundedAction)secondAction;
			lastActionTimes.put(groundedAction.params[0], secondTime);
		}
		
		return state;
	}
	/*
	private static EvaluationResult evaluateAgent(Domain domain, StateHashFactory hashingFactory, Human human, Agent partner, State startingState, 
			ActionTimeGenerator timeGenerator, boolean onlySubgoals, String saveFile) {
		human.reset();
		partner.reset();
		
		List<AbstractGroundedAction> actionSequence = new ArrayList<AbstractGroundedAction>();
		List<State> stateSequence = new ArrayList<State>();
		boolean finished = false;
		State currentState = startingState;
		stateSequence.add(currentState);
		List<State> statePair = new ArrayList<State>(2);
		List<AbstractGroundedAction> actionPair = new ArrayList<AbstractGroundedAction>(2);
		List<Double> timesPair = new ArrayList<Double>(2);
		List<Double> actionTimes = new ArrayList<Double>();
		human.chooseNewRecipe();
		if (human.getCurrentRecipe() == null) {
			throw new RuntimeException("Human's chosen recipe is null");
		}
		System.out.println("Chosen recipe: " + human.getCurrentRecipe().toString());
		Human otherHuman = null;
		Expert humanExpert = null, otherExpert = null;
		Map<String, Double> actionMap = new HashMap<String, Double>();
		actionMap.put(human.getAgentName(), 0.0);
		actionMap.put(partner.getAgentName(), 0.0);
		double currentTime = 0.0;
		List<String> agents = Arrays.asList(human.getAgentName(), partner.getAgentName());
		
		if (human instanceof Expert) {
			humanExpert = (Expert)human;
			humanExpert.setCooperative(false);
		}
		if ((partner instanceof Human) && !(partner instanceof RandomRecipeAgent)) {
			otherHuman = (Human)partner;
			human.initialSubgoal(startingState);
			otherHuman.setRecipe(human.getCurrentRecipe());
			otherHuman.setSubgoal(human.getCurrentSubgoal());
			if (otherHuman instanceof Expert) {
				otherExpert = (Expert)otherHuman;
			}
			if (humanExpert != null) {
				humanExpert.setCooperative(true);
			}
		}
		return evaluateAgent(domain, saveFile, onlySubgoals, hashingFactory,
				startingState, currentState, currentTime, human, partner,
				timeGenerator, actionMap, actionSequence, stateSequence,
				finished, statePair, actionPair, timesPair, actionTimes,
				otherHuman, humanExpert, otherExpert, agents);
	}*/
	
	private static void removeFile(String saveFile) {
		try {
			Files.delete(Paths.get(saveFile));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	/*
	private static EvaluationResult evaluateAgent(Domain domain, String saveFile, boolean onlySubgoals, StateHashFactory hashingFactory) {
		SimulationState simState = SimulationHelper.getStateFromSaved(domain, hashingFactory, saveFile);
		State startingState = simState.startingState;
		State currentState = simState.state;
		double currentTime = simState.currentTime;
		Human human = (Human)simState.human;
		Agent partner = simState.partner;
		ActionTimeGenerator timeGenerator = simState.timeGenerator;
		Map<String, Double> actionMap = simState.actionMap;
		
		List<AbstractGroundedAction> actionSequence = new ArrayList<AbstractGroundedAction>();
		List<State> stateSequence = new ArrayList<State>();
		boolean finished = false;
		stateSequence.add(currentState);
		List<State> statePair = new ArrayList<State>(2);
		List<AbstractGroundedAction> actionPair = new ArrayList<AbstractGroundedAction>(2);
		List<Double> timesPair = new ArrayList<Double>(2);
		List<Double> actionTimes = new ArrayList<Double>();
		if (human.getCurrentRecipe() == null) {
			human.chooseNewRecipe();
		}
		if (human.getCurrentRecipe() == null) {
			throw new RuntimeException("Human's chosen recipe is null");
		}
		System.out.println("Chosen recipe: " + human.getCurrentRecipe().toString());
		Human otherHuman = null;
		Expert humanExpert = null, otherExpert = null;
		List<String> agents = Arrays.asList(human.getAgentName(), partner.getAgentName());
		
		if (human instanceof Expert) {
			humanExpert = (Expert)human;
			humanExpert.setCooperative(false);
		}
		if ((partner instanceof Human) && !(partner instanceof RandomRecipeAgent)) {
			otherHuman = (Human)partner;
			if (otherHuman instanceof Expert) {
				otherExpert = (Expert)otherHuman;
			}
			if (humanExpert != null) {
				humanExpert.setCooperative(true);
			}
		}
		return evaluateAgent(domain, saveFile, onlySubgoals, hashingFactory,
				startingState, currentState, currentTime, human, partner,
				timeGenerator, actionMap, actionSequence, stateSequence,
				finished, statePair, actionPair, timesPair, actionTimes,
				otherHuman, humanExpert, otherExpert, agents);
	}*/

	private static EvaluationResult evaluateTwoAgents(SimulationState state, Domain domain, String saveFile, boolean onlySubgoals, StateHashFactory hashingFactory) {
		Agent partner = state.partner;
		Human partnerHuman = null;
		Expert partnerExpert = null;
		if (partner instanceof Human && !(partner instanceof RandomRecipeAgent)) {
			partnerHuman = (Human)partner;
			if (partner instanceof Expert) {
				partnerExpert = (Expert)partner;
			}
		}
		Expert human = (Expert)state.human;
		State currentState = state.state;
		State startingState = state.startingState;
		ActionTimeGenerator timeGenerator = state.timeGenerator;
		Map<String, Double> actionMap = state.actionMap;
		double currentTime = state.currentTime;
		List<String> agents = (Arrays.asList(human.getAgentName(), partner.getAgentName()));
		
		List<AbstractGroundedAction> actionSequence = new ArrayList<AbstractGroundedAction>();
		List<State> stateSequence = new ArrayList<State>();
		boolean finished = false;
		stateSequence.add(currentState);
		List<State> statePair = new ArrayList<State>(2);
		List<AbstractGroundedAction> actionPair = new ArrayList<AbstractGroundedAction>(2);
		List<Double> timesPair = new ArrayList<Double>(2);
		List<Double> actionTimes = new ArrayList<Double>();
		
		
		
		boolean isSuccess = false;
		partner.addObservation(currentState);
		GroundedAction humanAction = null, partnerAction = null;
		int numNullActions = 0;
		boolean partnerChoseToWait = false;
		while (!finished) {
			SimulationHelper.writeCurrentState(saveFile, domain, hashingFactory, startingState, currentState, human, partner, actionMap, timeGenerator, currentTime);
			
			if (humanAction == null) {
				if (human != null) {
					human.setCooperative((!currentState.equals(startingState) && numNullActions < 2) || partnerChoseToWait);
					humanAction = (GroundedAction)human.getActionWithScheduler(currentState, agents, !onlySubgoals, (GroundedAction)partnerAction);
				} else {
					humanAction = (GroundedAction)human.getActionWithScheduler(currentState, agents, !onlySubgoals);
				}
				if (humanAction != null) {
					System.out.println("Human chose " + humanAction.toString());
				} 
			}
			if (humanAction != null) {
				BakingAction bakingAction = (BakingAction)humanAction.action;
				BakingActionResult result = bakingAction.checkActionIsApplicableInState(currentState, humanAction.params);
				if (!result.getIsSuccess()) {
					System.err.println("Action " + humanAction.toString() + " cannot be performed");
					System.err.println(result.getWhyFailed());
					humanAction = null;
				}
			}
			
			if (partnerAction == null) {
				partnerChoseToWait = false;
				if (partnerHuman != null) {
					if (partnerExpert != null) {
						partnerAction = (GroundedAction)partnerExpert.getActionWithScheduler(currentState, agents, !onlySubgoals, (GroundedAction)humanAction);
						if (partnerAction != null && !partnerAction.params[0].equals(partner.getAgentName())) {
							throw new RuntimeException("Partner can't choose this action");
						}
					} else if (!currentState.equals(startingState)) {
						State newState = (humanAction == null) ? currentState : humanAction.executeIn(currentState);
						partnerAction = (GroundedAction)partnerHuman.getActionWithScheduler(newState, agents, !onlySubgoals);
					}
					
				} else {
					if (humanAction != null && humanAction.action instanceof ResetAction) {
						System.out.println("Human resets");
					}
					partnerAction = (GroundedAction)partner.getActionWithScheduler(currentState, agents, !onlySubgoals);
					if (partnerAction == null) {
						//partnerAction = TestManyAgents.getActionAndWait(partner, currentState);
					}
				}
				if (partnerAction != null && partnerAction.action == null) {
					partnerChoseToWait = true;
					partnerAction = null;
					System.out.println("Partner chose to wait");
				} 
			}
			
			if (partnerAction != null) {
				BakingAction bakingAction = (BakingAction)partnerAction.action;
				BakingActionResult result = bakingAction.checkActionIsApplicableInState(currentState, partnerAction.params);
				if (!result.getIsSuccess()) {
					System.err.println("Action " + partnerAction.toString() + " cannot be performed");
					System.err.println(result.getWhyFailed());
					partnerAction = null;
				}
			}
			
			if (humanAction != null && !humanAction.params[0].equals(human.getAgentName())) {
				throw new RuntimeException("Human can't choose this action");
			}
			
			if (partnerAction != null && !partnerAction.params[0].equals(partner.getAgentName())) {
				throw new RuntimeException("Partner can't choose this action");
			}
			
			if (humanAction == null && partnerAction == null) {
				numNullActions++;
				if (numNullActions > 3) {
					break;
				}
			} else {
				numNullActions = 0;
			}
			currentState = SimulationHelper.performActions(currentState, humanAction, partnerAction, actionMap, statePair, actionPair, timesPair, timeGenerator);
			
			if (!timesPair.isEmpty()){
				double now = timesPair.get(0);
				if (humanAction == null) {
					SimulationHelper.agentWaitUntilNext(human, actionMap, now);
					System.out.println("Human waits until " + actionMap.get(human.getAgentName()));
				}
				if (partnerAction == null) {
					SimulationHelper.agentWaitUntilNext(partner, actionMap, now);
					System.out.println("Partner waits until " + actionMap.get(partner.getAgentName()));
				}
				currentTime += timesPair.get(0);
			}
			
			if (actionPair.contains(humanAction)) {
				if (((GroundedAction)humanAction).action instanceof ResetAction) {
					partnerAction = null;
				}
				humanAction = null;
				
			}
			if (actionPair.contains(partnerAction)) {
				if (((GroundedAction)partnerAction).action instanceof ResetAction) {
					humanAction = null;
				}
				partnerAction = null;
				
			}
			
			//partner.addObservation(currentState);
			
			stateSequence.addAll(statePair);
			actionSequence.addAll(actionPair);
			actionTimes.addAll(timesPair);
			boolean isRepeating = checkIfRepeating(stateSequence);
			isSuccess = (onlySubgoals) ? human.isSubgoalFinished(currentState) : human.isSuccess(currentState);
			double reward = Collections.max(actionTimes);
			//double reward = SchedulingHelper.computeSequenceTime(startingState, actionSequence, timeGenerator);
			finished = isSuccess || reward > 1000.0;
			if (reward > 1000.0) {
				System.out.println("Reward became to large");
			}
			
			/*if (human.isSubgoalFinished(currentState)) {
				//System.out.println("Subgoal is finished");
			} else {
				//System.out.println("Subogal is not finished");
			}*/
			
			if (finished) {
				if (isSuccess) {
					//System.out.println("\n\nHuman finished successfully!!!\n\n");
				}
				else {
					if (reward < -200.0) {
						//System.err.println("Error became to large");
					}
					if (isRepeating) {
						//System.err.println("\n\nState sequence repetition detected!");
					}
					//System.err.println("\n\nHuman failed recipe!!!\n\n");
				}
				break;
			}
		}
		
		double reward = (actionTimes.isEmpty()) ? 0 : Collections.max(actionTimes);
		//double reward = SchedulingHelper.computeSequenceTime(startingState, actionSequence, timeGenerator);
		SimulationHelper.removeFile(saveFile);
		return new EvaluationResult(partner.toString(), reward, isSuccess);
	}
	
	/*
	private static EvaluationResult evaluateAgent(Domain domain,
			String saveFile, boolean onlySubgoals,
			StateHashFactory hashingFactory, State startingState,
			State currentState, double currentTime, Human human, Agent partner,
			ActionTimeGenerator timeGenerator, Map<String, Double> actionMap,
			List<AbstractGroundedAction> actionSequence,
			List<State> stateSequence, boolean finished, List<State> statePair,
			List<AbstractGroundedAction> actionPair, List<Double> timesPair,
			List<Double> actionTimes, Human otherHuman, Expert humanExpert,
			Expert otherExpert, List<String> agents) {
		boolean isSuccess = false;
		partner.addObservation(currentState);
		GroundedAction humanAction = null, partnerAction = null;
		int numNullActions = 0;
		boolean partnerChoseToWait = false;
		while (!finished) {
			SimulationHelper.writeCurrentState(saveFile, domain, hashingFactory, startingState, currentState, humanExpert, partner, actionMap, timeGenerator, currentTime);
			
			if (humanAction == null) {
				if (humanExpert != null) {
					humanExpert.setCooperative((!currentState.equals(startingState) && numNullActions < 2) || partnerChoseToWait);
					humanAction = (GroundedAction)humanExpert.getActionWithScheduler(currentState, agents, !onlySubgoals, (GroundedAction)partnerAction);
				} else {
					humanAction = (GroundedAction)human.getActionWithScheduler(currentState, agents, !onlySubgoals);
				}
				if (humanAction != null) {
					System.out.println("Human chose " + humanAction.toString());
				} 
			}
			if (humanAction != null) {
				BakingAction bakingAction = (BakingAction)humanAction.action;
				BakingActionResult result = bakingAction.checkActionIsApplicableInState(currentState, humanAction.params);
				if (!result.getIsSuccess()) {
					System.err.println("Action " + humanAction.toString() + " cannot be performed");
					System.err.println(result.getWhyFailed());
					humanAction = null;
				}
			}
			
			if (partnerAction == null) {
				partnerChoseToWait = false;
				if (otherHuman != null) {
					//if (haveGoneOnce) {
						if (otherExpert != null) {
							partnerAction = (GroundedAction)otherExpert.getActionWithScheduler(currentState, agents, !onlySubgoals, (GroundedAction)humanAction);
							if (partnerAction != null && !partnerAction.params[0].equals(partner.getAgentName())) {
								throw new RuntimeException("Partner can't choose this action");
							}
						} else {
							if (!currentState.equals(startingState)) {
								State newState = (humanAction == null) ? currentState : humanAction.executeIn(currentState);
								partnerAction = (GroundedAction)otherHuman.getActionWithScheduler(newState, agents, !onlySubgoals);
							}
						}
					//}
				} else {
					if (humanAction != null && humanAction.action instanceof ResetAction) {
						System.out.println("Human resets");
					}
					partnerAction = (GroundedAction)partner.getActionWithScheduler(currentState, agents, !onlySubgoals);
					if (partnerAction == null) {
						//partnerAction = TestManyAgents.getActionAndWait(partner, currentState);
					}
				}
				if (partnerAction != null && partnerAction.action == null) {
					partnerChoseToWait = true;
					partnerAction = null;
					System.out.println("Partner chose to wait");
				} 
			}
			
			if (partnerAction != null) {
				BakingAction bakingAction = (BakingAction)partnerAction.action;
				BakingActionResult result = bakingAction.checkActionIsApplicableInState(currentState, partnerAction.params);
				if (!result.getIsSuccess()) {
					System.err.println("Action " + partnerAction.toString() + " cannot be performed");
					System.err.println(result.getWhyFailed());
					partnerAction = null;
				}
			}
			
			if (humanAction != null && !humanAction.params[0].equals(human.getAgentName())) {
				throw new RuntimeException("Human can't choose this action");
			}
			
			if (partnerAction != null && !partnerAction.params[0].equals(partner.getAgentName())) {
				throw new RuntimeException("Partner can't choose this action");
			}
			
			if (humanAction == null && partnerAction == null) {
				numNullActions++;
				if (numNullActions > 3) {
					break;
				}
			} else {
				numNullActions = 0;
			}
			currentState = SimulationHelper.performActions(currentState, humanAction, partnerAction, actionMap, statePair, actionPair, timesPair, timeGenerator);
			
			if (!timesPair.isEmpty()){
				double now = timesPair.get(0);
				if (humanAction == null) {
					SimulationHelper.agentWaitUntilNext(human, actionMap, now);
					System.out.println("Human waits until " + actionMap.get(human.getAgentName()));
				}
				if (partnerAction == null) {
					SimulationHelper.agentWaitUntilNext(partner, actionMap, now);
					System.out.println("Partner waits until " + actionMap.get(partner.getAgentName()));
				}
				currentTime += timesPair.get(0);
			}
			
			if (actionPair.contains(humanAction)) {
				if (((GroundedAction)humanAction).action instanceof ResetAction) {
					partnerAction = null;
				}
				humanAction = null;
				
			}
			if (actionPair.contains(partnerAction)) {
				if (((GroundedAction)partnerAction).action instanceof ResetAction) {
					humanAction = null;
				}
				partnerAction = null;
				
			}
			
			//partner.addObservation(currentState);
			
			stateSequence.addAll(statePair);
			actionSequence.addAll(actionPair);
			actionTimes.addAll(timesPair);
			boolean isRepeating = checkIfRepeating(stateSequence);
			isSuccess = (onlySubgoals) ? human.isSubgoalFinished(currentState) : human.isSuccess(currentState);
			double reward = Collections.max(actionTimes);
			//double reward = SchedulingHelper.computeSequenceTime(startingState, actionSequence, timeGenerator);
			finished = isSuccess || reward > 1000.0;
			if (reward > 1000.0) {
				System.out.println("Reward became to large");
			}
			
			/*if (human.isSubgoalFinished(currentState)) {
				//System.out.println("Subgoal is finished");
			} else {
				//System.out.println("Subogal is not finished");
			}*/
			/*
			if (finished) {
				if (isSuccess) {
					//System.out.println("\n\nHuman finished successfully!!!\n\n");
				}
				else {
					if (reward < -200.0) {
						//System.err.println("Error became to large");
					}
					if (isRepeating) {
						//System.err.println("\n\nState sequence repetition detected!");
					}
					//System.err.println("\n\nHuman failed recipe!!!\n\n");
				}
				break;
			}
		}
		
		double reward = (actionTimes.isEmpty()) ? 0 : Collections.max(actionTimes);
		//double reward = SchedulingHelper.computeSequenceTime(startingState, actionSequence, timeGenerator);
		SimulationHelper.removeFile(saveFile);
		return new EvaluationResult(partner.toString(), reward, isSuccess);
	}*/
	
	private static void agentWaitUntilNext(Agent agent,
			Map<String, Double> actionMap, double next) {
		actionMap.put(agent.getAgentName(), next);
	}

	private static boolean checkIfRepeating(List<State> stateSequence) {
		List<State> reverse = new ArrayList<State>(stateSequence);
		Collections.reverse(stateSequence);
		
		List<State> predicate = new ArrayList<State>();
		int maximumPredicateSize = reverse.size() / 2;
		for (int i = 1; i < maximumPredicateSize; i++) {
			predicate.clear();
			for (int j = 0; j < i; j++) {
				predicate.add(reverse.get(j));
				
			}
			boolean predicateMatch = true;
			int offset = predicate.size();
			for (int j = 0; j < predicate.size(); j++) {
				if (!predicate.get(j).equals(reverse.get(j + offset))) {
					predicateMatch = false;
					break;
				}
			}
			
			if (predicateMatch) {
				return true;
			}
		}
		return false;
	}
	
	private static boolean writeCurrentState(String saveFile, Domain domain, StateHashFactory hashingFactory, State startState,  State state, Agent human, Agent partner, Map<String, Double> actionMap, ActionTimeGenerator timeGenerator, double currentTime) {
		if (saveFile == null) {
			return false;
		}
		String saved = SimulationHelper.saveState(domain, hashingFactory, startState, state, human, partner, actionMap, timeGenerator, currentTime);
		boolean success = true;
		
		PrintWriter out = null;
		try {
			out = new PrintWriter(saveFile);
			out.println(saved);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			success = false;
		} finally {
			if (out != null) {
				out.close();
			}
		}
		
		return success;
	}
	
	private static String saveState(Domain domain, StateHashFactory hashingFactory, State startState,  State state, Agent human, Agent partner, Map<String, Double> actionMap, ActionTimeGenerator timeGenerator, double currentTime) {
		SimulationState simState = new SimulationState(startState, state, human, partner, actionMap, timeGenerator, currentTime);
		Yaml yaml = new Yaml();
		return yaml.dump(simState.toMap(domain, hashingFactory));
	}
	
	private static SimulationState getStateFromSaved(Domain domain, StateHashFactory hashingFactory, String filename) {
		StringBuilder  stringBuilder = new StringBuilder();
		BufferedReader reader = null;
	    try {
			reader = new BufferedReader( new FileReader(filename));
			String line = null;
			String ls = System.getProperty("line.separator");
			while( ( line = reader.readLine() ) != null ) {
		        stringBuilder.append( line );
		        stringBuilder.append( ls );
		    }
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	    
		String saved = stringBuilder.toString();
		
		Yaml yaml = new Yaml();
		Map<String, Object> map = (Map<String, Object>)yaml.load(saved);
		return SimulationState.fromMap(map, domain, hashingFactory);
	}
	/*
	private static EvaluationResult evaluateHumanAlone(String saveFile, Human human, State startingState, ActionTimeGenerator timeGenerator, boolean onlySubgoals, Domain domain, StateHashFactory hashingFactory) {
		
		List<AbstractGroundedAction> actionSequence = new ArrayList<AbstractGroundedAction>();
		List<State> stateSequence = new ArrayList<State>();
		boolean finished = false;
		State currentState = startingState;
		stateSequence.add(currentState);
		List<State> statePair = new ArrayList<State>(1);
		List<AbstractGroundedAction> actionPair = new ArrayList<AbstractGroundedAction>(1);
		List<Double> timesPair = new ArrayList<Double>(1);
		Map<String, Double> lastActionTime = new HashMap<String, Double>();
		lastActionTime.put(human.getAgentName(), 0.0);
		while (!finished) {
			SimulationHelper.writeCurrentState(saveFile, domain, hashingFactory, startingState, currentState, human, null, null, timeGenerator, 0.0);
			
			AbstractGroundedAction humanAction = human.getAction(currentState);
			if (humanAction == null) {
				break;
			}
			
			currentState = SimulationHelper.performActions(currentState, humanAction, null, lastActionTime, statePair, actionPair, timesPair, timeGenerator);
			stateSequence.addAll(statePair);
			actionSequence.addAll(actionPair);
			
			finished = (onlySubgoals) ? human.isSubgoalFinished(currentState) : human.isFinished(currentState);
		}
		
		double score = timesPair.get(0);
		//double score = SchedulingHelper.computeSequenceTime(startingState, actionSequence, timeGenerator);
		boolean success = (onlySubgoals) ? human.isSubgoalFinished(currentState) : human.isSuccess(currentState);
		return new EvaluationResult("solo", score, success);
	}*/
	
	private static EvaluationResult evaluateOneAgent(SimulationState simState, String saveFile, Domain domain, StateHashFactory hashingFactory, boolean onlySubgoals) {
		Human human = (Human)simState.human;
		State startingState = simState.startingState;
		ActionTimeGenerator timeGenerator = simState.timeGenerator;
		State currentState = simState.state;
		Map<String, Double> lastActionTime = simState.actionMap;
		List<AbstractGroundedAction> actionSequence = new ArrayList<AbstractGroundedAction>();
		List<State> stateSequence = new ArrayList<State>();
		boolean finished = false;
		stateSequence.add(currentState);
		
		List<State> statePair = new ArrayList<State>(1);
		List<AbstractGroundedAction> actionPair = new ArrayList<AbstractGroundedAction>(1);
		List<Double> timesPair = new ArrayList<Double>(1);
		
		while (!finished) {
			SimulationHelper.writeCurrentState(saveFile, domain, hashingFactory, startingState, currentState, human, null, null, timeGenerator, 0.0);
			
			AbstractGroundedAction humanAction = human.getAction(currentState);
			if (humanAction == null) {
				break;
			}
			
			currentState = SimulationHelper.performActions(currentState, humanAction, null, lastActionTime, statePair, actionPair, timesPair, timeGenerator);
			stateSequence.addAll(statePair);
			actionSequence.addAll(actionPair);
			
			finished = (onlySubgoals) ? human.isSubgoalFinished(currentState) : human.isFinished(currentState);
		}
		
		double score = timesPair.get(0);
		//double score = SchedulingHelper.computeSequenceTime(startingState, actionSequence, timeGenerator);
		boolean success = (onlySubgoals) ? human.isSubgoalFinished(currentState) : human.isSuccess(currentState);
		return new EvaluationResult("solo", score, success);
	}
	
	public static void evaluateHuman(String saveFile, Domain generalDomain, Human human, ActionTimeGenerator timeGenerator, 
			StateHashFactory hashingFactory, boolean onlySubgoals, int numTrials) {
		List<Recipe> recipes = AgentHelper.recipes(generalDomain);
		
		State startingState = SimulationHelper.generateInitialState(generalDomain, hashingFactory, recipes, human, null);
		human.setInitialState(startingState);
		
		for (int i = 0; i < numTrials; i++) {
			human.chooseNewRecipe();
			Map<String, Double> actionMap = new HashMap<String, Double>();
			actionMap.put(human.getAgentName(), 0.0);
			SimulationState simState = new SimulationState(startingState, startingState, human, null, actionMap, timeGenerator, 0.0);
			EvaluationResult result = SimulationHelper.evaluateOneAgent(simState, saveFile, generalDomain, hashingFactory, onlySubgoals);
			System.out.println(result.toString());
		}
	}
	
	public static class EvaluationResult {
		private double score;
		private double successScore;
		private int numberSuccesses;
		private int numberTrials;
		private String agent;
		
		public EvaluationResult() {
			this.score = 0.0;
			this.successScore = 0.0;
			this.numberSuccesses = 0;
			this.numberTrials = 0;
		}
		
		public EvaluationResult(String agent, double score, boolean wasSuccess) {
			this.agent = agent;
			this.score = score;
			this.successScore = (wasSuccess) ? score : 0.0;
			this.numberSuccesses = (wasSuccess) ? 1 : 0;
			this.numberTrials = 1;
		}
		public void incrementResults(double addedScore, boolean wasSuccess) {
			this.score += addedScore;
			this.successScore += (wasSuccess) ? score : 0.0;
			this.numberSuccesses += (wasSuccess) ? 1 : 0;
			this.numberTrials++;
		}
		
		public void incrementResult(EvaluationResult other) {
			this.score += other.score;
			this.successScore += other.successScore;
			this.numberSuccesses += other.numberSuccesses;
			this.numberTrials += other.numberTrials;
		}
		
		public double getScore() {return this.score;}
		
		public int getSuccesses() {return this.numberSuccesses;}
		
		public int getTrials() {return this.numberTrials;}
		
		public String getAgent() {return this.agent;}
		
		// Successes, Trials, Average reward, average success reward
		@Override
		public String toString() {
			double averageSuccessScore = (this.numberSuccesses == 0) ? 0.0 : this.successScore / this.numberSuccesses;
			return this.agent + ", " + this.numberSuccesses + ", " + this.numberTrials + ", " + this.score / this.numberTrials + ", " + averageSuccessScore;
		}
	}
	
	public static void run(int numTrials, Domain generalDomain, StateHashFactory hashingFactory,
			List<Recipe> recipes, ActionTimeGenerator timeGenerator,
			Human human, List<Agent> agents, ResetAction reset, int choice, boolean subgoalsOnly, String filename) {
		
		long start = System.nanoTime();
		SimulationHelper.EvaluationResult result;
		Agent agent = null;
		if (choice < agents.size() ) {
			agent = agents.get(choice);
			System.out.println("Evaluating " + agent.toString());
			
		} else {
			System.out.println("Evaluating solo");
			SimulationHelper.evaluateHuman(filename, generalDomain, human, timeGenerator, hashingFactory, subgoalsOnly, numTrials);
			return;
		}
		human = new Expert(generalDomain, "human", timeGenerator);
		
		State startingState = SimulationHelper.generateInitialState(generalDomain, hashingFactory, recipes, human, agent);
		reset.setState(startingState);
		
		human.setInitialState(startingState);
		if (agent != null) {
			agent.setInitialState(startingState);
		}
		long end = System.nanoTime();
		//System.out.println(parser.stateToString(startingState));
		System.out.println("Required " + (end - start) / 1000000000.0);
		
		for (int i = 0; i < numTrials; i++) {
			human.chooseNewRecipe();
			Map<String, Double> actionTimes =  new HashMap<String, Double>();
			actionTimes.put(human.getAgentName(), 0.0);
			actionTimes.put(agent.getAgentName(), 0.0);
			if (agent instanceof Human && !(agent instanceof RandomRecipeAgent)) {
				Human otherHuman = (Human)agent;
				otherHuman.setRecipe(human.getCurrentRecipe());
			}
			SimulationState simState = new SimulationState(startingState, startingState, human, agent, actionTimes, timeGenerator, 0.0);
			result = SimulationHelper.evaluateTwoAgents(simState, generalDomain, filename, subgoalsOnly, hashingFactory);	
			System.out.println(result.toString());

			System.out.println("");
			timeGenerator.clear();
		}
	}
	
	public static void runFromSaved(String filename, Domain generalDomain, StateHashFactory hashingFactory,
			List<Recipe> recipes, ResetAction reset, boolean subgoalsOnly) {
		SimulationState simState = SimulationHelper.getStateFromSaved(generalDomain, hashingFactory, filename);
		if (simState.partner == null) {
			EvaluationResult result = SimulationHelper.evaluateOneAgent(simState, filename, generalDomain, hashingFactory, subgoalsOnly);
			System.out.println(result.toString());
		} else {
			EvaluationResult result = SimulationHelper.evaluateTwoAgents(simState, generalDomain, filename, subgoalsOnly, hashingFactory);
			System.out.println(result.toString());
		}
		
		
	}
	
	public static class SimulationState {
		private State startingState;
		private State state;
		private	Agent human;
		private Agent partner;
		private Map<String, Double> actionMap;
		private double currentTime;
		private ActionTimeGenerator timeGenerator;
		
		public SimulationState(State startState, State state, Agent human, Agent partner, Map<String, Double> actionMap, ActionTimeGenerator timeGenerator, double currentTime) {
			this.startingState = startState;
			this.state = state;
			this.human = human;
			this.partner = partner;
			this.actionMap = actionMap;
			this.currentTime = currentTime;
			this.timeGenerator = timeGenerator;
		}
		
		public static SimulationState fromMap(Map<String, Object> map, Domain domain, StateHashFactory hashingFactory) {
			Map<String, Double> actionMap = (Map<String, Double>)map.get("action_map");
			double currentTime = (Double)map.get("current_time");
			StateYAMLParser parser = new StateYAMLParser(domain, hashingFactory);
			String stateStr = (String)map.get("state");
			State state = parser.stringToState(stateStr);
			
			String startStateStr = (String)map.get("start_state");
			State startState = parser.stringToState(startStateStr);
			
			ActionTimeGenerator timeGenerator = ActionTimeGenerator.fromMap(domain, (Map<String, Object>)map.get("time_generator"));
			
			Map<String, Object> humanMap = (Map<String, Object>)map.get("human");
			Agent human = Agent.fromMap(domain, humanMap, timeGenerator, startState);
			Object partnerObj = map.get("partner");
			Agent partner = null;
			if (partnerObj != null) {
				Map<String, Object> partnerMap = (Map<String, Object>)partnerObj;
				partner = Agent.fromMap(domain, partnerMap, timeGenerator, startState);
			}
			
			return new SimulationState(startState, state, human, partner, actionMap, timeGenerator, currentTime);
			
		}
		
		public Map<String, Object> toMap(Domain domain, StateHashFactory hashingFactory) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("action_map", this.actionMap);
			map.put("current_time", this.currentTime);
			StateYAMLParser parser = new StateYAMLParser(domain, hashingFactory);
			String statePrepared = parser.stateToString(this.state);
			map.put("state", statePrepared);
			String startStatePrepared = parser.stateToString(this.startingState);
			map.put("start_state", startStatePrepared);
			
			map.put("time_generator", this.timeGenerator.toMap());
			
			Map<String, Object> humanMap = Agent.toMap(this.human);
			map.put("human", humanMap);
			if (this.partner != null) {
				Map<String, Object> partnerMap = Agent.toMap(this.partner);
				map.put("partner", partnerMap);
			}
			return map;
		}
		public State getState() {
			return state;
		}
		public void setState(State state) {
			this.state = state;
		}
		public Agent getHuman() {
			return human;
		}
		public void setHuman(Agent human) {
			this.human = human;
		}
		public Agent getPartner() {
			return partner;
		}
		public void setPartner(Agent partner) {
			this.partner = partner;
		}
		public Map<String, Double> getActionMap() {
			return actionMap;
		}
		public void setActionMap(Map<String, Double> actionMap) {
			this.actionMap = actionMap;
		}
		public double getCurrentTime() {
			return currentTime;
		}
		public void setCurrentTime(double currentTime) {
			this.currentTime = currentTime;
		}
	}
}
