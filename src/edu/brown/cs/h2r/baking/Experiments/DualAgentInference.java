package edu.brown.cs.h2r.baking.Experiments;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.deterministic.DDPlannerPolicy;
import burlap.behavior.singleagent.planning.deterministic.informed.Heuristic;
import burlap.behavior.singleagent.planning.deterministic.informed.astar.AStar;
import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Domain;
//import edu.brown.cs.h2r.baking.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import edu.brown.cs.h2r.baking.InferenceRewardFunction;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.RecipeAgentSpecificMakeSpanRewardFunction;
import edu.brown.cs.h2r.baking.RecipeAgentSpecificRewardFunction;
import edu.brown.cs.h2r.baking.RecipeTerminalFunction;
import edu.brown.cs.h2r.baking.GoalCondition.RecipeGoalCondition;
import edu.brown.cs.h2r.baking.Heuristics.RecipeHeuristic;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.MakeSpanFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeBotched;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeFinished;
import edu.brown.cs.h2r.baking.Recipes.Brownies;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.actions.MixAction;
import edu.brown.cs.h2r.baking.actions.MoveAction;
import edu.brown.cs.h2r.baking.actions.PourAction;
/*
The issue here, appears to be that our makespan calculation can't account for subgoals. Because we plan subgoals
sequentially, a plan that optimally uses the two agents won't be computed. Instead, we need to change the task to
allow for parallel plans. This will increase the possible states to visit, and also probable won't speed up 
planning time. 

Therefore, this is interesting because if two independent planners attempt to speed up planning and use subgoals,
they will both collide if they both choose the same subgoal. Instead, you only want as much cooperation as will
speed up the total time t1o solve the problem.
*/
public class DualAgentInference  implements DomainGenerator {

	public DualAgentInference() {
	}
	@Override
	public Domain generateDomain() {
		Domain domain = new SADomain();
		domain.addObjectClass(ContainerFactory.createObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createSimpleIngredientObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createComplexIngredientObjectClass(domain));
		domain.addObjectClass(SpaceFactory.createObjectClass(domain));		
		domain.addObjectClass(AgentFactory.getObjectClass(domain));
		domain.addObjectClass(MakeSpanFactory.getObjectClass(domain));
		
		return domain;
	}
	
	public void PlanRecipeTwoAgents(Domain domain, Recipe recipe)
	{
		System.out.println("Creating two-agent initial start state");
		State state = new State();
		Action mix = new MixAction(domain, recipe.topLevelIngredient);
		//Action bake = new BakeAction(domain);
		Action pour = new PourAction(domain, recipe.topLevelIngredient);
		Action move = new MoveAction(domain, recipe.topLevelIngredient);
		state.addObject(AgentFactory.getNewHumanAgentObjectInstance(domain, "human"));
		state.addObject(AgentFactory.getNewHumanAgentObjectInstance(domain, "robot"));
		state.addObject(MakeSpanFactory.getNewObjectInstance(domain, "makeSpan", 2));
		List<String> containers = Arrays.asList("mixing_bowl_1");
		state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, "shelf", null, null));
		state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, "counter_human", containers, "human"));
		state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, "counter_robot", containers, "robot"));
	
		for (String container : containers) { 
			state.addObject(ContainerFactory.getNewMixingContainerObjectInstance(domain, container, null, "shelf"));
		}
		
		this.PlanIngredient(domain, state, recipe.topLevelIngredient);
	}
	
	public State PlanIngredient(Domain domain, State startingState, IngredientRecipe ingredient)
	{
		State currentState = new State(startingState);
		
		List<IngredientRecipe> contents = ingredient.getContents();
		for (IngredientRecipe subIngredient : contents) {
			if (!subIngredient.isSimple()) {
				System.out.println("Planning ingredient " + subIngredient.getName());
				currentState = this.PlanIngredient(domain, currentState, subIngredient);
			}
		}
		
		ObjectClass simpleIngredientClass = domain.getObjectClass(IngredientFactory.ClassNameSimple);
		ObjectClass containerClass = domain.getObjectClass(ContainerFactory.ClassName);		
		ObjectInstance shelfSpace = currentState.getObject("shelf");
		
		List<ObjectInstance> ingredientInstances = 
				IngredientFactory.getSimpleIngredients(simpleIngredientClass, ingredient);
		List<ObjectInstance> containerInstances = 
				Recipe.getContainers(containerClass, ingredientInstances, shelfSpace.getName());
		
		
		for (ObjectInstance ingredientInstance : ingredientInstances) {
			if (currentState.getObject(ingredientInstance.getName()) == null) {
				currentState.addObject(ingredientInstance);
			}
		}
		
		for (ObjectInstance containerInstance : containerInstances) {
			if (currentState.getObject(containerInstance.getName()) == null) {
				ContainerFactory.changeContainerSpace(containerInstance, shelfSpace.getName());
				currentState.addObject(containerInstance);
			}
		}
		
		
		final PropositionalFunction isSuccess = new RecipeFinished("success", domain, ingredient);
		PropositionalFunction isFailure = new RecipeBotched("botched", domain, ingredient);
		//RewardFunction recipeRewardFunction = new RecipeRewardFunction(brownies);
		//RewardFunction recipeRewardFunction = new RecipeRewardFunction();
		List<RewardFunction> rewardFunctions = new ArrayList<RewardFunction>();
		rewardFunctions.add(new RecipeAgentSpecificMakeSpanRewardFunction("human"));
		rewardFunctions.add(new RecipeAgentSpecificMakeSpanRewardFunction("robot"));
		rewardFunctions.add(new RecipeAgentSpecificRewardFunction("human"));
		rewardFunctions.add(new RecipeAgentSpecificRewardFunction("robot"));
		
		TerminalFunction recipeTerminalFunction = new RecipeTerminalFunction(isSuccess, isFailure);
		
		StateHashFactory hashFactory = new NameDependentStateHashFactory();
		StateConditionTest goalCondition = new RecipeGoalCondition(isSuccess);
		//final int numSteps = Recipe.getNumberSteps(ingredient);
		Heuristic heuristic = new RecipeHeuristic();
		List<EpisodeAnalysis> episodes = new ArrayList<EpisodeAnalysis>();
				
		return planIngredient(domain, startingState, ingredient,
				currentState, rewardFunctions, recipeTerminalFunction,
				hashFactory, goalCondition, heuristic, episodes);
		}
	private State planIngredient(Domain domain, State startingState,
			IngredientRecipe ingredient, State currentState,
			List<RewardFunction> rewardFunctions,
			TerminalFunction recipeTerminalFunction,
			StateHashFactory hashFactory, StateConditionTest goalCondition,
			Heuristic heuristic, List<EpisodeAnalysis> episodes) {
		RewardFunction humanRewardFunction = rewardFunctions.get(0);
		RewardFunction robotRewardFunction = new InferenceRewardFunction(rewardFunctions);
		
		boolean finished = false;
		State endState = startingState;
		List<GroundedAction> fullActions = new ArrayList<GroundedAction>();
		List<Double> fullReward = new ArrayList<Double>();
		while (!finished) {
			//currentAgent = !currentAgent;
			//RewardFunction recipeRewardFunction = (currentAgent) ? humanRewardFunction : robotRewardFunction;
			
			State humanCurrentState = ExperimentHelper.setPrimaryAgent(currentState, "human");
			State robotCurrentState = ExperimentHelper.setPrimaryAgent(currentState, "robot");
			AStar robotAgent = new AStar(domain, robotRewardFunction, goalCondition, hashFactory, heuristic);
			AStar humanAgent = new AStar(domain, humanRewardFunction, goalCondition, hashFactory, heuristic);
			robotAgent.planFromState(robotCurrentState);
			humanAgent.planFromState(humanCurrentState);
			
			Policy robotPolicy = new DDPlannerPolicy(robotAgent);
			Policy humanPolicy = new DDPlannerPolicy(humanAgent);
			
			EpisodeAnalysis robotEpisodes = 
					robotPolicy.evaluateBehavior(robotCurrentState, robotRewardFunction, recipeTerminalFunction);	
			EpisodeAnalysis humanEpisodes = 
					humanPolicy.evaluateBehavior(humanCurrentState, humanRewardFunction, recipeTerminalFunction);	
			
			if (robotEpisodes.actionSequence.size() <= 1 ||
					humanEpisodes.actionSequence.size() <= 1) {
				finished = true;
				continue;
			}
			GroundedAction robotAction = ExperimentHelper.getFirstRelavantAction(robotEpisodes.actionSequence, "robot");
			GroundedAction humanAction = ExperimentHelper.getFirstRelavantAction(humanEpisodes.actionSequence, "human");

			System.out.println("Robot tries action " + robotAction.toString());
			System.out.println("Human tries action " + humanAction.toString());
			
			// Robot's action is always #2
			
			Random random = new Random();
			Boolean reverse = random.nextBoolean();
			int numApplicable = ExperimentHelper.numberActionsApplicableInState(currentState, humanAction, robotAction, reverse);
			if (numApplicable <= 1)
			{
				String agentFailed = reverse ? "human" : "robot";
				System.out.println(agentFailed + " failed to execute action");
			}
			
			State nextState = ExperimentHelper.applyGroundedActions(currentState, humanAction, robotAction, reverse);
			
			if (reverse)
			{
				fullActions.add(robotAction);
				fullReward.add(-1.0);
				if (numApplicable > 1){
					fullActions.add(humanAction);
					fullReward.add(0.0);
				}
			}
			else
			{
				fullActions.add(humanAction);
				fullReward.add(-1.0);
				if (numApplicable > 1) {
					fullActions.add(robotAction);
					fullReward.add(0.0);
				}
			}
			
			for (RewardFunction rf : rewardFunctions)
			{
				AStar search = new AStar(domain, rf, goalCondition, hashFactory, heuristic);
				search.planFromState(humanCurrentState);
				Policy p = new DDPlannerPolicy(search);
				EpisodeAnalysis episode = 
						p.evaluateBehavior(humanCurrentState, rf, recipeTerminalFunction);	
				episodes.add(episode);					
			}
			
			List<Double> newBeliefs = new ArrayList<Double>();
			Double totalMatches = 0.0;
			for (int i =0; i < rewardFunctions.size(); i++)
			{
				GroundedAction action = ExperimentHelper.getFirstRelavantAction(episodes.get(i).actionSequence, "human");
				
				if (action != null && action.action.getName() == humanAction.action.getName() &&
						action.params.length == humanAction.params.length)
				{
					boolean match = true;
					for (int j = 0; j < humanAction.params.length; j++)
					{
						match &= (humanAction.params[j] == action.params[j]);
					}
					newBeliefs.add(match ? 2.0 : 1.0);
					totalMatches++;
				}
				else
				{
					newBeliefs.add(1.0);
				}
			}
			
			double sum = 0;
			for (double belief : newBeliefs)
			{
				sum += belief;
			}
			for (int i = 0; i < newBeliefs.size(); ++i)
			{
				if (sum > 0)
				{
					newBeliefs.set(i, newBeliefs.get(i) / sum);
				}
			}

			((InferenceRewardFunction)robotRewardFunction).updateBeliefs(newBeliefs);
			for (int i =0; i < rewardFunctions.size(); i++) {
				double updatedBelief = ((InferenceRewardFunction)robotRewardFunction).getBeliefs().get(i);
				System.out.println("Belief "  + i + ": " + newBeliefs.get(i) + ", " + updatedBelief);
			}
			
		ExperimentHelper.printEpisodeSequence(fullActions, fullReward);

			currentState = nextState;
		}
		ExperimentHelper.printResults(fullActions, fullReward);
		return endState;
	}
	
	public static void main(String[] args) throws IOException {
		DualAgentInference kitchen = new DualAgentInference();
		System.out.println("Generating Domain");
		Domain domain = kitchen.generateDomain();
		kitchen.PlanRecipeTwoAgents(domain, new Brownies());
	}
}
