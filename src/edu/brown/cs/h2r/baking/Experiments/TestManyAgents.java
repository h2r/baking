package edu.brown.cs.h2r.baking.Experiments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.SADomain;
import edu.brown.cs.h2r.baking.Agents.AdaptiveByFlow;
import edu.brown.cs.h2r.baking.Agents.Agent;
import edu.brown.cs.h2r.baking.Agents.AgentHelper;
import edu.brown.cs.h2r.baking.Agents.Human;
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
import edu.brown.cs.h2r.baking.actions.SwitchAction;
import edu.brown.cs.h2r.baking.actions.UseAction;

public class TestManyAgents {
	private static Random rando = new Random();
	
	private static Domain generateGeneralDomain(List<Recipe> recipes) {
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
		
		
		// To the failed propFunction, add in all subgoals for a recipe that are based on an ingredient.
		RecipeBotched failed = ((RecipeBotched)domain.getPropFunction(AffordanceCreator.BOTCHED_PF));
		if (failed != null) {
			failed.clearSubgoals();
		}
		
		return domain;
	}
	
	private static Domain generateSpecificDomain(Domain generalDomain, Recipe recipe) {
		Domain domain = new SADomain();
		for (ObjectClass objectClass : generalDomain.getObjectClasses()) {
			domain.addObjectClass(objectClass);
		}
		
		Action mix = new MixAction(domain, recipe.topLevelIngredient);
		Action pour = new PourAction(domain, recipe.topLevelIngredient);
		Action move = new MoveAction(domain, recipe.topLevelIngredient);
		Action grease = new GreaseAction(domain);
		Action aSwitch = new SwitchAction(domain);
		Action use = new UseAction(domain, recipe.topLevelIngredient);
		//Action hand = new HandAction(domain, recipe.topLevelIngredient);
		//Action waitAction = new WaitAction(domain);

		recipe.init(domain);
		return domain;
	}
	
	private static State generateInitialState(Domain generalDomain, List<Domain> recipeDomains, List<Recipe> recipes, Agent agent1, Agent agent2) {
		List<ObjectInstance> objects = new ArrayList<ObjectInstance>();
		objects.add(agent1.getAgentObject());
		objects.add(agent2.getAgentObject());
		ObjectInstance makeSpan = MakeSpanFactory.getNewObjectInstance(generalDomain, "makespan", 2);
		
		List<String> containers = Arrays.asList("mixing_bowl_1", "mixing_bowl_2", "baking_dish", "melting_pot");
		ObjectInstance counterSpace = SpaceFactory.getNewWorkingSpaceObjectInstance(generalDomain, SpaceFactory.SPACE_COUNTER, containers, "human");
		objects.add(counterSpace);
		
		objects.add(ContainerFactory.getNewBakingContainerObjectInstance(generalDomain, "baking_dish", null, SpaceFactory.SPACE_COUNTER));
		objects.add(ContainerFactory.getNewHeatingContainerObjectInstance(generalDomain, "melting_pot", null, SpaceFactory.SPACE_COUNTER));
		objects.add(SpaceFactory.getNewBakingSpaceObjectInstance(generalDomain, SpaceFactory.SPACE_OVEN, null, ""));
		objects.add(SpaceFactory.getNewHeatingSpaceObjectInstance(generalDomain, SpaceFactory.SPACE_STOVE, null, ""));
		
		for (String container : containers) { 
			objects.add(ContainerFactory.getNewMixingContainerObjectInstance(generalDomain, container, null, SpaceFactory.SPACE_COUNTER));
		}
		
		// Out of all the ingredients in our kitchen, plan over only those that might be useful!
		Knowledgebase knowledgebase = new Knowledgebase();
		
		List<ObjectInstance> ingredients = new ArrayList<ObjectInstance>();
		List<ObjectInstance> tools = new ArrayList<ObjectInstance>();
		for (int i = 0;i < recipes.size(); i++) {
			Recipe recipe = recipes.get(i);
			Domain domain = recipeDomains.get(i);
			ingredients.addAll(knowledgebase.getRecipeObjectInstanceList(domain, recipe));
			tools.addAll(knowledgebase.getTools(domain, SpaceFactory.SPACE_COUNTER));
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
			System.out.println("Executing action " + firstAction.toString());
			State nextState = firstAction.executeIn(state);
			if (nextState.equals(state)) {
				System.out.println("Action had no effect");
			}
			state = nextState;
			statePair.add(state);
			actionPair.add(firstAction);
		}

		if (secondAction != null) {
			System.out.println("Executing action " + secondAction.toString());
			State nextState = secondAction.executeIn(state);
			if (nextState.equals(state)) {
				System.out.println("Action had no effect");
			}
			state = nextState;
			statePair.add(state);
			actionPair.add(secondAction);
		}
		
		return state;
	}
	
	private static double evaluateAgent(Human human, Agent partner, State startingState) {
		
		List<AbstractGroundedAction> actionSequence = new ArrayList<AbstractGroundedAction>();
		List<State> stateSequence = new ArrayList<State>();
		boolean finished = false;
		State currentState = startingState;
		stateSequence.add(currentState);
		List<State> statePair = new ArrayList<State>();
		List<AbstractGroundedAction> actionPair = new ArrayList<AbstractGroundedAction>();
		human.chooseNewRecipe();
		
		while (!finished) {
			AbstractGroundedAction humanAction = human.getAction(currentState);
			if (humanAction == null) {
				System.err.println("Human chose to do nothing");
				break;
			}
			
			AbstractGroundedAction partnerAction = TestManyAgents.getActionAndWait(partner, currentState);
			
			currentState = TestManyAgents.performActions(currentState, humanAction, partnerAction, statePair, actionPair, 0.5);
			stateSequence.addAll(statePair);
			actionSequence.addAll(actionPair);
			finished = human.isFinished(currentState);
		}
		
		return human.getCostActions(actionSequence, stateSequence);
	}
	
	private static AbstractGroundedAction getActionAndWait(final Agent agent, final State state) {
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
			System.err.println("Waiting for agent's action timed out");
		}
		executor.shutdownNow();
		return action;
	}
	
	public static void main(String[] args) {
		
		Domain generalDomain = TestManyAgents.generateGeneralDomain(AgentHelper.recipes()); 
		
		List<Agent> agents = Arrays.asList(
				(Agent)new AdaptiveByFlow(generalDomain)
				);
		
		int numTrials = 10;
		
		Map<Agent, Double> scores = new HashMap<Agent, Double>();
		for (Agent agent : agents) {
			System.out.println("Agent: " + agent.getAgentName());
			double score = 0.0;
			for (int i = 0; i < numTrials; i++) {
				Human human = new Human(generalDomain);
				
				List<Domain> recipeDomains = new ArrayList<Domain>();
				List<Recipe> recipes = AgentHelper.recipes();
				for (Recipe recipe : recipes) {
					recipeDomains.add(TestManyAgents.generateSpecificDomain(generalDomain, recipe));
				}
				
				State startingState = TestManyAgents.generateInitialState(generalDomain, recipeDomains,recipes, human, agent);
				human.setInitialState(startingState);
				agent.setInitialState(startingState);
				
				System.out.println("Trial: " + i);
				score += TestManyAgents.evaluateAgent(human, agent, startingState);
			}
			score /= numTrials;
			scores.put(agent, score);
		}
		
		for (Map.Entry<Agent, Double> entry : scores.entrySet()) {
			Agent agent = entry.getKey();
			Double score = entry.getValue();
			System.out.println(agent.getAgentName() + ": " + score);
		}
	}

}
