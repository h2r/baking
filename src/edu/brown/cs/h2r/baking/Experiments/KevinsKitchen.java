package edu.brown.cs.h2r.baking.Experiments;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.deterministic.DDPlannerPolicy;
import burlap.behavior.singleagent.planning.deterministic.informed.Heuristic;
import burlap.behavior.singleagent.planning.deterministic.informed.astar.AStar;
import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.auxiliary.DomainGenerator;
//import burlap.oomdp.core.Domain;
import edu.brown.cs.h2r.baking.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import edu.brown.cs.h2r.baking.AffordancesApply;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.RecipeAgentSpecificMakeSpanRewardFunction;
import edu.brown.cs.h2r.baking.RecipeBotched;
import edu.brown.cs.h2r.baking.RecipeFinished;
import edu.brown.cs.h2r.baking.RecipeRewardFunction;
import edu.brown.cs.h2r.baking.RecipeTerminalFunction;
import edu.brown.cs.h2r.baking.SpaceFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.Recipes.Brownies;
import edu.brown.cs.h2r.baking.Recipes.BrowniesSubGoals;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.Recipes.TestSubGoals;
import edu.brown.cs.h2r.baking.actions.MixAction;
import edu.brown.cs.h2r.baking.actions.MoveAction;
import edu.brown.cs.h2r.baking.actions.PourAction;


public class KevinsKitchen implements DomainGenerator {
		
	List<ObjectInstance> allIngredients;
	public KevinsKitchen() {

	}
	
	@Override
	public Domain generateDomain() {
		Domain domain = new Domain();
		domain.addObjectClass(ContainerFactory.createObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createSimpleIngredientObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createComplexIngredientObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createSimpleHiddenIngredientObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createComplexHiddenIngredientObjectClass(domain));
		domain.addObjectClass(SpaceFactory.createObjectClass(domain));		
		domain.addObjectClass(AgentFactory.getObjectClass(domain));
		
		Action mix = new MixAction(domain);
		//Action bake = new BakeAction(domain);
		Action pour = new PourAction(domain);
		Action move = new MoveAction(domain);
		return domain;
	}
	
	public void PlanRecipeOneAgent(Domain domain, Recipe recipe)
	{
		State state = new State();
		
		state.addObject(AgentFactory.getNewHumanAgentObjectInstance(domain, "human"));
		List<String> containers = Arrays.asList("mixing_bowl_1");
		state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, "counter", containers, "human"));

		for (String container : containers) { 
			state.addObject(ContainerFactory.getNewMixingContainerObjectInstance(domain, container, null, "counter"));
		}
		
		recipe.addAllIngredients(state, domain);
		this.allIngredients = recipe.getAllIngredients();

		this.PlanIngredient(domain, state, recipe.topLevelIngredient);
	}
	
	public State PlanIngredient(Domain domain, State startingState, IngredientRecipe ingredient)
	{
		State currentState = new State(startingState);
		
		List<IngredientRecipe> contents = ingredient.getContents();
		for (IngredientRecipe subIngredient : contents) {
			if (!subIngredient.isSimple()) {
				currentState = this.PlanIngredient(domain, currentState, subIngredient);
			}
		}
		
		ObjectClass simpleIngredientClass = domain.getObjectClass(IngredientFactory.ClassNameSimple);
		ObjectClass containerClass = domain.getObjectClass(ContainerFactory.ClassName);		
		ObjectInstance counterSpace = currentState.getObject("counter");

		List<ObjectInstance> ingredientInstances = allIngredients;
		List<ObjectInstance> containerInstances = Recipe.getContainers(containerClass, ingredientInstances, counterSpace.getName());
		
		
		for (ObjectInstance ingredientInstance : ingredientInstances) {
			if (currentState.getObject(ingredientInstance.getName()) == null) {
				currentState.addObject(ingredientInstance);
			}
		}
		
		for (ObjectInstance containerInstance : containerInstances) {
			if (currentState.getObject(containerInstance.getName()) == null) {
				ContainerFactory.changeContainerSpace(containerInstance, counterSpace.getName());
				currentState.addObject(containerInstance);
			}
		}
		
		if (domain.getPropFunction("affordances") == null) {
			final PropositionalFunction newProp = new AffordancesApply("affordances", domain, ingredient.getTraits());
		} else {
			System.out.println(ingredient.getTraits());
			/* */
			ObjectInstance bowl = currentState.getObject("mixing_bowl_1");
			Set<String> contains;
			if ((contains = ContainerFactory.getContentNames(bowl)).size()==1) {
				for (String object : contains) {
					currentState.getObject(object).clearRelationalTargets("container");
				}
				ContainerFactory.removeContents((currentState.getObject("mixing_bowl_1")));
			}
			/*			ObjectInstance bowl = currentState.getObject("mixing_bowl_1");
			Set<String> contains;
			if ((contains = ContainerFactory.getContentNames(bowl)).size()==1) {
				ContainerFactory.removeContents((currentState.getObject("mixing_bowl_1")));
				for (String name : contains) {
					ObjectInstance object = currentState.getObject(name);
					for (String trait : ingredient.getTraits()) {
						if (object.getAllRelationalTargets("traits").contains(trait)) {
							ContainerFactory.addIngredient(bowl, object.getName());
							object.addRelationalTarget("container", bowl.getName());
						}
					}
				}
			} */
			((AffordancesApply)(domain.getPropFunction("affordances"))).changeTraits(ingredient.getTraits());

		}
		
		final PropositionalFunction isSuccess = new RecipeFinished("success", domain, ingredient);
		PropositionalFunction isFailure = new RecipeBotched("botched", domain, ingredient);
		TerminalFunction recipeTerminalFunction = new RecipeTerminalFunction(isSuccess, isFailure);
		
		StateHashFactory hashFactory = new NameDependentStateHashFactory();
		StateConditionTest goalCondition = new StateConditionTest() {
			@Override
			public boolean satisfies(State s) {
				return s.somePFGroundingIsTrue(isSuccess);
			}
		};
		RewardFunction rf = new RewardFunction() {
			@Override
			public double reward(State s, GroundedAction a, State sprime) {
				return -1;
			}
		};
				
		Heuristic heuristic = new Heuristic() {
			@Override
			public double h(State state) {
				return 0;
			}
		};
		AStar aStar = new AStar(domain, rf, goalCondition, hashFactory, heuristic);
		aStar.planFromState(currentState);
		Policy policy = new DDPlannerPolicy(aStar);
		EpisodeAnalysis episodeAnalysis = 
				policy.evaluateBehavior(currentState, rf, recipeTerminalFunction);	
		ExperimentHelper.printEpisodeSequence(episodeAnalysis.actionSequence, episodeAnalysis.rewardSequence);

		State endState = episodeAnalysis.getState(episodeAnalysis.stateSequence.size() - 1);

		List<ObjectInstance> finalObjects = 
				new ArrayList<ObjectInstance>(endState.getObjectsOfTrueClass(IngredientFactory.ClassNameComplex));
		List<ObjectInstance> containerObjects =
				new ArrayList<ObjectInstance>(endState.getObjectsOfTrueClass(ContainerFactory.ClassName));
		
		ExperimentHelper.checkIngredientCompleted(ingredient, endState, finalObjects, containerObjects);
		
		ExperimentHelper.printResults(episodeAnalysis.actionSequence, episodeAnalysis.rewardSequence);
		
		/* Adding complex_ingredient_bowl */
		endState.addObject(IngredientFactory.getNewIngredientInstance(ingredient, ingredient.getName(), domain.getObjectClass(IngredientFactory.ClassNameComplex)));
		ObjectInstance container = ContainerFactory.getNewIngredientContainerObjectInstance(domain, ingredient.getName()+"_bowl", ingredient.getName(), "counter");
		endState.getObject(ingredient.getName()).addRelationalTarget("container", container.getName());
		endState.addObject(container);
		/* */
		return endState;
	}
	
	public static void main(String[] args) throws IOException {
		
		KevinsKitchen kitchen = new KevinsKitchen();
		Domain domain = kitchen.generateDomain();
		kitchen.PlanRecipeOneAgent(domain, new Brownies());
	}
}
