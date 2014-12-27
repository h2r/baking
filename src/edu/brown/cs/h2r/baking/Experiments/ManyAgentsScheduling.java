package edu.brown.cs.h2r.baking.Experiments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.ObjectHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.SADomain;
import edu.brown.cs.h2r.baking.Agents.AdaptiveByFlow;
import edu.brown.cs.h2r.baking.Agents.Agent;
import edu.brown.cs.h2r.baking.Agents.AgentHelper;
import edu.brown.cs.h2r.baking.Agents.Human;
import edu.brown.cs.h2r.baking.Agents.RandomActionAgent;
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
import edu.brown.cs.h2r.baking.Scheduling.Workflow;
import edu.brown.cs.h2r.baking.actions.GreaseAction;
import edu.brown.cs.h2r.baking.actions.MixAction;
import edu.brown.cs.h2r.baking.actions.MoveAction;
import edu.brown.cs.h2r.baking.actions.PourAction;
import edu.brown.cs.h2r.baking.actions.ResetAction;
import edu.brown.cs.h2r.baking.actions.SwitchAction;
import edu.brown.cs.h2r.baking.actions.UseAction;

public class ManyAgentsScheduling {
	private static Random rando = new Random();
	private static StateHashFactory hashingFactory = new NameDependentStateHashFactory();
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
		Action grease = new GreaseAction(domain);
		Action aSwitch = new SwitchAction(domain);
		Action use = new UseAction(domain);
		Action resetAction = new ResetAction(domain);
		
		
		
		// To the failed propFunction, add in all subgoals for a recipe that are based on an ingredient.
		RecipeBotched failed = ((RecipeBotched)domain.getPropFunction(AffordanceCreator.BOTCHED_PF));
		if (failed != null) {
			failed.clearSubgoals();
		}
		
		return domain;
	}
	
	private static State generateInitialState(Domain generalDomain, List<Recipe> recipes, Agent agent1, Agent agent2) {
		ObjectHashFactory objectHashingFactory = hashingFactory.getObjectHashFactory();
		List<ObjectInstance> objects = new ArrayList<ObjectInstance>();
		objects.add(agent1.getAgentObject());
		
		if (agent2 != null) {
			objects.add(agent2.getAgentObject());
		}
		ObjectInstance makeSpan = MakeSpanFactory.getNewObjectInstance(generalDomain, "makespan", 2, objectHashingFactory);
		objects.add(makeSpan);
		
		List<String> containers = Arrays.asList("mixing_bowl_1", "mixing_bowl_2", "mixing_bowl_3", "baking_dish", "melting_pot");
		ObjectInstance counterSpace = SpaceFactory.getNewWorkingSpaceObjectInstance(generalDomain, SpaceFactory.SPACE_COUNTER, containers, "human", objectHashingFactory);
		objects.add(counterSpace);
		
		objects.add(ToolFactory.getNewSimpleToolObjectInstance(generalDomain, "whisk", "", "", SpaceFactory.SPACE_COUNTER, objectHashingFactory));
		objects.add(ToolFactory.getNewSimpleToolObjectInstance(generalDomain, "spoon","", "", SpaceFactory.SPACE_COUNTER, objectHashingFactory));
		
		objects.add(ContainerFactory.getNewBakingContainerObjectInstance(generalDomain, "baking_dish", null, SpaceFactory.SPACE_COUNTER, objectHashingFactory));
		objects.add(ContainerFactory.getNewHeatingContainerObjectInstance(generalDomain, "melting_pot", null, SpaceFactory.SPACE_COUNTER, objectHashingFactory));
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
			tools.addAll(knowledgebase.getTools(generalDomain, SpaceFactory.SPACE_COUNTER, objectHashingFactory));
		}
		
	
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
		double time = timeGenerator.get(action);
		return time + lastActionTimes.get(action.params[0]);
	}
	
	private static State performActions(State state, AbstractGroundedAction action1, AbstractGroundedAction action2, Map<String, Double> lastActionTimes, 
			List<State> statePair, List<AbstractGroundedAction> actionPair, ActionTimeGenerator timeGenerator) {
		
		statePair.clear();
		actionPair.clear();
		double action1Time = ManyAgentsScheduling.getActionTime((GroundedAction)action1, lastActionTimes, timeGenerator);
		double action2Time = ManyAgentsScheduling.getActionTime((GroundedAction)action2, lastActionTimes, timeGenerator);
		
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
			//System.out.println("Executing action " + firstAction.toString());
			if (nextState.equals(state)) {
				//System.out.println("Action had no effect");
			}
			state = nextState;
			statePair.add(state);
			actionPair.add(firstAction);
			GroundedAction groundedAction = (GroundedAction)firstAction;
			lastActionTimes.put(groundedAction.params[0], firstTime);
		}
		
		if (secondTime > 0.0 && secondTime == firstTime) {
			State nextState = secondAction.executeIn(state);
			//System.out.println("Executing action " + secondAction.toString());
			
			if (nextState.equals(state)) {
				//System.out.println("Action had no effect");
			}
			state = nextState;
			statePair.add(state);
			actionPair.add(firstAction);
			GroundedAction groundedAction = (GroundedAction)secondAction;
			lastActionTimes.put(groundedAction.params[0], secondTime);
		}
		
		return state;
	}
	
	private static double sequenceTime(List<AbstractGroundedAction> actions, ActionTimeGenerator timeGenerator) {
		double time = 0.0;
		for (AbstractGroundedAction action : actions) {
			time += timeGenerator.get((GroundedAction)action);
		}
		return time;
	}
	
	private static double longestTime(Map<String, Double> lastActionTimes) {
		double maxTime = 0.0;
		for (Map.Entry<String, Double> entry : lastActionTimes.entrySet()) {
			maxTime = Math.max(maxTime, entry.getValue());
		}
		return maxTime;
	}
	
	private static EvaluationResult evaluateAgent(Human human, Agent partner, State startingState, ActionTimeGenerator timeGenerator) {
		List<AbstractGroundedAction> actionSequence = new ArrayList<AbstractGroundedAction>();
		List<State> stateSequence = new ArrayList<State>();
		boolean finished = false;
		State currentState = startingState;
		stateSequence.add(currentState);
		List<State> statePair = new ArrayList<State>();
		List<AbstractGroundedAction> actionPair = new ArrayList<AbstractGroundedAction>();
		human.chooseNewRecipe();
		Human otherHuman = null;
		
		Map<String, Double> actionMap = new HashMap<String, Double>();
		actionMap.put(human.getAgentName(), 0.0);
		actionMap.put(partner.getAgentName(), 0.0);
		
		List<String> agents = Arrays.asList(human.getAgentName(), partner.getAgentName());
		
		if ((partner instanceof Human) && !(partner instanceof RandomRecipeAgent)) {
			otherHuman = (Human)partner;
			otherHuman.setRecipe(human.getCurrentRecipe());
		}
		boolean isSuccess = false;
		partner.addObservation(currentState);
		AbstractGroundedAction humanAction = null, partnerAction = null;
		while (!finished) {
			
			if (humanAction == null) {
				humanAction = human.getAction(currentState);
				if (humanAction == null) {
					if (human.isSuccess(currentState)) {
					}
					else {
						humanAction = human.getActionWithScheduler(currentState, agents);
					}
					break;
				}
			}
			
			if (partnerAction == null) {
				if (otherHuman != null) {
					State newState = humanAction.executeIn(currentState);
					
					////System.out.println("\nEvaluating how partner would complete recipe");
					//TestManyAgents.evaluateHumanAlone(otherHuman, newState);
					////System.out.println("");
					partnerAction = otherHuman.getActionWithScheduler(newState, agents);
				} else {
					partnerAction = partner.getActionWithScheduler(currentState, agents);
					if (partnerAction == null) {
						//partnerAction = TestManyAgents.getActionAndWait(partner, currentState);
					}
				}
			}
			
			
			
			currentState = ManyAgentsScheduling.performActions(currentState, humanAction, partnerAction, actionMap, statePair, actionPair, timeGenerator);
			if (actionPair.contains(humanAction)) {
				humanAction = null;
			}
			if (actionPair.contains(partnerAction)) {
				partnerAction = null;
			}
			
			//partner.addObservation(currentState);
			
			stateSequence.addAll(statePair);
			actionSequence.addAll(actionPair);
			boolean isRepeating = checkIfRepeating(stateSequence);
			isSuccess = human.isSuccess(currentState);
			double reward = ManyAgentsScheduling.longestTime(actionMap);
			finished = isSuccess || reward > 1000.0;
			
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
		double reward = ManyAgentsScheduling.longestTime(actionMap);
		return new EvaluationResult(reward, isSuccess);
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
	
	private static EvaluationResult evaluateHumanAlone(Human human, State startingState, ActionTimeGenerator timeGenerator) {
		
		List<AbstractGroundedAction> actionSequence = new ArrayList<AbstractGroundedAction>();
		List<State> stateSequence = new ArrayList<State>();
		boolean finished = false;
		State currentState = startingState;
		stateSequence.add(currentState);
		List<State> statePair = new ArrayList<State>();
		List<AbstractGroundedAction> actionPair = new ArrayList<AbstractGroundedAction>();
		Map<String, Double> lastActionTime = new HashMap<String, Double>();
		lastActionTime.put(human.getAgentName(), 0.0);
		while (!finished) {
			AbstractGroundedAction humanAction = human.getAction(currentState);
			if (humanAction == null) {
				break;
			}
			
			currentState = ManyAgentsScheduling.performActions(currentState, humanAction, null, lastActionTime, statePair, actionPair, timeGenerator);
			stateSequence.addAll(statePair);
			actionSequence.addAll(actionPair);
			
			finished = human.isFinished(currentState);
		}
		
		double score = ManyAgentsScheduling.sequenceTime(actionSequence, timeGenerator);
		return new EvaluationResult(score, human.isSuccess(currentState));
	}
	
	private static EvaluationResult evaluateHuman(Domain generalDomain, Human human, ActionTimeGenerator timeGenerator, int numTrials) {
		EvaluationResult result = new EvaluationResult();
		for (int i = 0; i < numTrials; i++) {
			List<Recipe> recipes = AgentHelper.recipes(generalDomain);
			
			State startingState = ManyAgentsScheduling.generateInitialState(generalDomain, recipes, human, null);
			human.setInitialState(startingState);
			
			
			//System.out.println("Trial: " + i);
			human.chooseNewRecipe();
			result.incrementResult(ManyAgentsScheduling.evaluateHumanAlone(human, startingState, timeGenerator));
		}
		return result;
		//System.out.println("Human alone: " + result.toString());
		
	}
	
	public static class EvaluationResult {
		private double score;
		private double successScore;
		private int numberSuccesses;
		private int numberTrials;
		
		public EvaluationResult() {
			this.score = 0.0;
			this.successScore = 0.0;
			this.numberSuccesses = 0;
			this.numberTrials = 0;
		}
		
		public EvaluationResult(double score, boolean wasSuccess) {
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
		
		// Successes, Trials, Average reward, average success reward
		@Override
		public String toString() {
			double averageSuccessScore = (this.numberSuccesses == 0) ? 0.0 : this.successScore / this.numberSuccesses;
			return "" + this.numberSuccesses + ", " + this.numberTrials + ", " + this.score / this.numberTrials + ", " + averageSuccessScore;
		}
	}
	
	public static Map<Workflow.Node, Double> buildActionTimeLookup(Workflow workflow, double factor) {
		Random random = new Random();
		Map<Workflow.Node, Double> times = new HashMap<Workflow.Node, Double>();
		
		for (Workflow.Node node : workflow) {
			times.put(node, factor * random.nextDouble());
		}
		
		return times;
	}
	
	public static void main(String[] args) {
		
		int numTrials = 20;
		int trialId = 0;/*
		if (args.length == 2) {
			numTrials = Integer.parseInt(args[0]);
			trialId = Integer.parseInt(args[1]);
		} else {
			System.err.println("Args provided: "  + Arrays.toString(args));
			System.err.println("Usage TestManyAgents numTrials trialId");
			System.exit(0);
		}	*/
		
		Domain generalDomain = ManyAgentsScheduling.generateGeneralDomain(); 
		
		List<Recipe> recipes = AgentHelper.recipes(generalDomain);
		Knowledgebase knowledgebase = Knowledgebase.getKnowledgebase(generalDomain);
		knowledgebase.initKnowledgebase(recipes);
		Map<String, Double> factors = new HashMap<String, Double>();
		factors.put("human", 1.0);
		
		ActionTimeGenerator timeGenerator = new ActionTimeGenerator(factors);
		Human human = new Human(generalDomain, timeGenerator);
		
		State state = ManyAgentsScheduling.generateInitialState(generalDomain, recipes, human, null);
		/*for (Recipe recipe : recipes) {
			//System.out.println("Testing recipe " + recipe.toString());
			ExperimentHelper.testRecipeExecution(generalDomain, state, recipe);
			//System.out.println("\n\n");
		}*/
		
		//System.exit(0);
		
		
		
		List<Agent> agents = Arrays.asList(/*
				(Agent)new RandomActionAgent(generalDomain),
				(Agent)new RandomRecipeAgent(generalDomain, timeGenerator),*/
				//(Agent)new Human(generalDomain, "friend", timeGenerator)//,
				(Agent)new AdaptiveByFlow(generalDomain, timeGenerator)
				);
		System.out.println("Agent, Successes, Trials, Average reward, average successful reward");
		ResetAction reset = (ResetAction)generalDomain.getAction(ResetAction.className);
		reset.setState(state);
		
		
			//System.out.println("Agent: " + agent.getAgentName());
		//Agent agent = agents.get(3);
		
			EvaluationResult result;
		for (int i = 0; i < numTrials; i++) {
			//System.out.println("solo" + ", " +  ManyAgentsScheduling.evaluateHuman(generalDomain, human, timeGenerator, 1).toString());
			
			Collections.shuffle(agents);
			
			for (Agent agent : agents) {
			
				human = new Human(generalDomain, timeGenerator);
				
				State startingState = ManyAgentsScheduling.generateInitialState(generalDomain, recipes, human, agent);
				reset.setState(startingState);
				
				human.setInitialState(startingState);
				agent.setInitialState(startingState);
				
				result = ManyAgentsScheduling.evaluateAgent(human, agent, startingState, timeGenerator);
				System.out.println(agent.getAgentName() + ", " +  result.toString());
			}
			timeGenerator.clear();
		}	
	}
}
