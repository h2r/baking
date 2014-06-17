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
import edu.brown.cs.h2r.baking.Recipes.BrowniesSubGoals;
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
public class DualAgentRandomRobot  implements DomainGenerator {

	public DualAgentRandomRobot() {
		// TODO Auto-generated constructor stub
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
		state.addObject(SpaceFactory.getNewObjectInstance(domain, "shelf", false, false, false, null, "" ));
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
		
		boolean finished = false;
		State endState = startingState;
		List<GroundedAction> fullActions = new ArrayList<GroundedAction>();
		List<Double> fullReward = new ArrayList<Double>();
		boolean currentAgent = false;
		while (!finished) {
			
			GroundedAction robotAction = ExperimentHelper.getRandomGroundedAction(domain, currentState, "robot");
			
			State humanCurrentState = ExperimentHelper.setPrimaryAgent(currentState, "human");
			AStar humanAgent = new AStar(domain, humanRewardFunction, goalCondition, hashFactory, heuristic);
			humanAgent.planFromState(humanCurrentState);
			
			Policy policy = new DDPlannerPolicy(humanAgent);
			EpisodeAnalysis humanEpisodes = 
					policy.evaluateBehavior(humanCurrentState, humanRewardFunction, recipeTerminalFunction);

			if (humanEpisodes.actionSequence.size() <= 1) {
				finished = true;
				continue;
			}

			GroundedAction humanAction = ExperimentHelper.getFirstRelavantAction(humanEpisodes.actionSequence, "human");

			System.out.println("Human tries action " + humanAction.toString());
			System.out.println("Robot tries action " + robotAction.toString());
			
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
			
			ExperimentHelper.printEpisodeSequence(fullActions, fullReward);

			currentState = nextState;
		}
		
		ExperimentHelper.printResults(fullActions, fullReward);
		
		return endState;
	}
	

	public static void main(String[] args) throws IOException {
		DualAgentRandomRobot kitchen = new DualAgentRandomRobot();
		System.out.println("Generating Domain");
		Domain domain = kitchen.generateDomain();
		kitchen.PlanRecipeTwoAgents(domain, new Brownies());
	}
}
