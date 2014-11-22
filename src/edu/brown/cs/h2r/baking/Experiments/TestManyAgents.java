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
import edu.brown.cs.h2r.baking.actions.GreaseAction;
import edu.brown.cs.h2r.baking.actions.MixAction;
import edu.brown.cs.h2r.baking.actions.MoveAction;
import edu.brown.cs.h2r.baking.actions.PourAction;
import edu.brown.cs.h2r.baking.actions.ResetAction;
import edu.brown.cs.h2r.baking.actions.SwitchAction;
import edu.brown.cs.h2r.baking.actions.UseAction;

public class TestManyAgents {
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
		
		List<String> containers = Arrays.asList("mixing_bowl_1", "mixing_bowl_2", "baking_dish", "melting_pot");
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
	
	private static State performActions(State state, AbstractGroundedAction action1, AbstractGroundedAction action2, 
			List<State> statePair, List<AbstractGroundedAction> actionPair, double bias) {
		
		statePair.clear();
		actionPair.clear();
		double roll = TestManyAgents.rando.nextDouble();
		boolean action1First = (roll < bias);
		
		AbstractGroundedAction firstAction = (action1First) ? action1 : action2;
		AbstractGroundedAction secondAction = (action1First) ? action2 : action1;
		
		if (firstAction != null) {
			//System.out.println("Executing action " + firstAction.toString());
			State nextState = firstAction.executeIn(state);
			if (nextState.equals(state)) {
				//System.out.println("Action had no effect");
			}
			state = nextState;
			statePair.add(state);
			actionPair.add(firstAction);
		}

		if (secondAction != null) {
			//System.out.println("Executing action " + secondAction.toString());
			State nextState = secondAction.executeIn(state);
			if (nextState.equals(state)) {
				//System.out.println("Action had no effect");
			}
			state = nextState;
			statePair.add(state);
			actionPair.add(secondAction);
		}
		
		return state;
	}
	
	private static EvaluationResult evaluateAgent(Human human, Agent partner, State startingState) {
		double actionBias = 0.5;
		List<AbstractGroundedAction> actionSequence = new ArrayList<AbstractGroundedAction>();
		List<State> stateSequence = new ArrayList<State>();
		boolean finished = false;
		State currentState = startingState;
		stateSequence.add(currentState);
		List<State> statePair = new ArrayList<State>();
		List<AbstractGroundedAction> actionPair = new ArrayList<AbstractGroundedAction>();
		human.chooseNewRecipe();
		Human otherHuman = null;
		
		if ((partner instanceof Human) && !(partner instanceof RandomRecipeAgent)) {
			otherHuman = (Human)partner;
			otherHuman.setRecipe(human.getCurrentRecipe());
			actionBias = 1.0;
		}
		boolean isSuccess = false;
		while (!finished) {
			partner.addObservation(currentState);
			
			AbstractGroundedAction humanAction = human.getAction(currentState);
			if (humanAction == null) {
				if (human.isSuccess(currentState)) {
					//System.out.println("\n\nHuman finished successfully!!!\n\n");
				}
				else {
					humanAction = human.getAction(currentState);
					//System.err.println("\n\nHuman failed recipe!!!\n\n");
				}
				break;
			}
			
			State newState = 
					TestManyAgents.performActions(currentState, humanAction, null, statePair, actionPair, actionBias);
			
			
			AbstractGroundedAction partnerAction = null;
			if (otherHuman != null) {
				
				////System.out.println("\nEvaluating how partner would complete recipe");
				//TestManyAgents.evaluateHumanAlone(otherHuman, newState);
				////System.out.println("");
				partnerAction = TestManyAgents.getActionAndWait(otherHuman, newState);
			} else {
				partnerAction = TestManyAgents.getActionAndWait(partner, currentState);
				if (partnerAction == null) {
					partnerAction = TestManyAgents.getActionAndWait(partner, currentState);
				}
			}
			
			
			
			currentState = TestManyAgents.performActions(currentState, humanAction, partnerAction, statePair, actionPair, actionBias);
			
			stateSequence.addAll(statePair);
			actionSequence.addAll(actionPair);
			boolean isRepeating = checkIfRepeating(stateSequence);
			isSuccess = human.isSuccess(currentState);
			double reward = human.getCostActions(actionSequence, stateSequence);
			finished = isSuccess || reward < -200.0;
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
		double reward = human.getCostActions(actionSequence, stateSequence);
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
	
	private static EvaluationResult evaluateHumanAlone(Human human, State startingState) {
		
		List<AbstractGroundedAction> actionSequence = new ArrayList<AbstractGroundedAction>();
		List<State> stateSequence = new ArrayList<State>();
		boolean finished = false;
		State currentState = startingState;
		stateSequence.add(currentState);
		List<State> statePair = new ArrayList<State>();
		List<AbstractGroundedAction> actionPair = new ArrayList<AbstractGroundedAction>();
		
		while (!finished) {
			AbstractGroundedAction humanAction = human.getAction(currentState);
			if (humanAction == null) {
				//System.err.println("Human chose to do nothing");
				break;
			}
			
			GroundedAction groundedAction = (GroundedAction)humanAction;
			if (groundedAction.action instanceof ResetAction) {
				//System.err.println("Human resetting state");
			}
			
			currentState = TestManyAgents.performActions(currentState, humanAction, null, statePair, actionPair, 0.5);
			stateSequence.addAll(statePair);
			actionSequence.addAll(actionPair);
			
			finished = human.isFinished(currentState);
		}
		
		if (human.isSuccess(currentState)) {
			//System.out.println("Recipe was a success");
		} else {
			//System.err.println("Recipe was a failure");
		}
		double score = human.getCostActions(actionSequence, stateSequence);
		return new EvaluationResult(score, human.isSuccess(currentState));
	}
	
	private static EvaluationResult evaluateHuman(Domain generalDomain, Human human, int numTrials) {
		double score = 0.0;
		EvaluationResult result = new EvaluationResult();
		for (int i = 0; i < numTrials; i++) {
			List<Recipe> recipes = AgentHelper.recipes(generalDomain);
			
			State startingState = TestManyAgents.generateInitialState(generalDomain, recipes, human, null);
			human.setInitialState(startingState);
			
			
			//System.out.println("Trial: " + i);
			human.chooseNewRecipe();
			result.incrementResult(TestManyAgents.evaluateHumanAlone(human, startingState));
		}
		return result;
		//System.out.println("Human alone: " + result.toString());
		
	}
	
	private static AbstractGroundedAction getActionAndWait(final Agent agent, final State state) {
		return agent.getAction(state);
		/*
		ExecutorService executor = Executors.newFixedThreadPool(2); 
		AbstractGroundedAction action = null;
		final Future<AbstractGroundedAction> handler = executor.submit(new Callable<AbstractGroundedAction>(){
			@Override
			public AbstractGroundedAction call() throws Exception {
				return agent.getAction(state);
			}});
		try {
			action =  handler.get(10000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException | ExecutionException | TimeoutException e) {
			//System.err.println("Waiting for agent's action timed out");
		}
		executor.shutdownNow();
		return action;*/
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
	
	public static void main(String[] args) {
		
		int numTrials = 8;
		int trialId = 0;
		if (args.length == 2) {
			numTrials = Integer.parseInt(args[0]);
			trialId = Integer.parseInt(args[1]);
		} else {
			System.err.println("Args provided: "  + Arrays.toString(args));
			System.err.println("Usage TestManyAgents numTrials trialId");
			System.exit(0);
		}	
		
		Domain generalDomain = TestManyAgents.generateGeneralDomain(); 
		
		List<Recipe> recipes = AgentHelper.recipes(generalDomain);
		Knowledgebase knowledgebase = Knowledgebase.getKnowledgebase(generalDomain);
		knowledgebase.initKnowledgebase(recipes);
		
		Human human = new Human(generalDomain);
		
		State state = TestManyAgents.generateInitialState(generalDomain, recipes, human, null);
		/*for (Recipe recipe : recipes) {
			//System.out.println("Testing recipe " + recipe.toString());
			ExperimentHelper.testRecipeExecution(generalDomain, state, recipe);
			//System.out.println("\n\n");
		}*/
		
		//System.exit(0);
		
		
		
		List<Agent> agents = Arrays.asList(
				(Agent)new RandomActionAgent(generalDomain),
				(Agent)new RandomRecipeAgent(generalDomain),
				(Agent)new Human(generalDomain, "friend"),
				(Agent)new AdaptiveByFlow(generalDomain)
				);
		System.out.println("Agent, Successes, Trials, Average reward, average successful reward");
		ResetAction reset = (ResetAction)generalDomain.getAction(ResetAction.className);
		reset.setState(state);
		//System.out.println("solo" + ", " +  TestManyAgents.evaluateHuman(generalDomain, human, numTrials).toString());
		
		for (Agent agent : agents) {
			//System.out.println("Agent: " + agent.getAgentName());
		//Agent agent = agents.get(1);
			EvaluationResult result;
			for (int i = 0; i < numTrials; i++) {
				human = new Human(generalDomain);
				
				
				State startingState = TestManyAgents.generateInitialState(generalDomain, recipes, human, agent);
				reset.setState(startingState);
				
				human.setInitialState(startingState);
				agent.setInitialState(startingState);
				
				
				//System.out.println("Trial: " + i);
				result = TestManyAgents.evaluateAgent(human, agent, startingState);
				System.out.println(agent.getAgentName() + ", " +  result.toString());
			}
		}	
	}
}
