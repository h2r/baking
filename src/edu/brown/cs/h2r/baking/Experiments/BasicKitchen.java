package edu.brown.cs.h2r.baking.Experiments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.auxiliary.common.StateJSONParser;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.SADomain;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeBotched;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeFinished;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.Knowledgebase.IngredientKnowledgebase;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.MakeSpanFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.actions.BakingActionResult;
import edu.brown.cs.h2r.baking.actions.BakingAction;
import edu.brown.cs.h2r.baking.actions.MixAction;
import edu.brown.cs.h2r.baking.actions.MoveAction;
import edu.brown.cs.h2r.baking.actions.PeelAction;
import edu.brown.cs.h2r.baking.actions.PourAction;
import edu.brown.cs.h2r.baking.actions.SwitchAction;
import edu.brown.cs.h2r.baking.actions.UseAction;

public class BasicKitchen implements DomainGenerator {
	Domain domain;
	StateJSONParser parser;
	Recipe recipe;
	State currentState;
	PropositionalFunction isSuccess;
	PropositionalFunction isFailure;
	StateHashFactory stateHashFactory;
	List<ObjectInstance> ingredientContainers;
	List<BakingSubgoal> recipeSubgoals;
	boolean[] completedSubgoals;
	
	IngredientKnowledgebase knowledgebase;
	
	public BasicKitchen(Recipe recipe) {
		this.recipe = recipe;
		knowledgebase = new IngredientKnowledgebase();
		this.stateHashFactory = new NameDependentStateHashFactory();
	}
	
	public State getCurrentState() {
		return new State(this.currentState);
	}
	
	public String getCurrentStateString() {
		return this.parser.stateToString(this.currentState);
	}
	
	public Recipe getRecipe() {
		return this.recipe;
	}
	
	@Override
	public Domain generateDomain() {
		Domain domain = new SADomain();
		domain.addObjectClass(ContainerFactory.createObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createSimpleIngredientObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createComplexIngredientObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createComplexHiddenIngredientObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createSimpleHiddenIngredientObjectClass(domain));
		domain.addObjectClass(SpaceFactory.createObjectClass(domain));		
		domain.addObjectClass(AgentFactory.getObjectClass(domain));
		domain.addObjectClass(MakeSpanFactory.getObjectClass(domain));
		
		Action mix = new MixAction(domain, recipe.topLevelIngredient);
		Action pour = new PourAction(domain, recipe.topLevelIngredient);
		Action move = new MoveAction(domain, recipe.topLevelIngredient);
		Action peel = new PeelAction(domain, recipe.topLevelIngredient);
		Action turnOnOff = new SwitchAction(domain);
		Action use = new UseAction(domain, recipe.topLevelIngredient);
		return domain;
	}
	
	private State getInitialState() {
		State state = new State();
		state.addObject(SpaceFactory.getNewBakingSpaceObjectInstance(this.domain, SpaceFactory.SPACE_OVEN, null, ""));
		state.addObject(SpaceFactory.getNewHeatingSpaceObjectInstance(this.domain, SpaceFactory.SPACE_STOVE, null, ""));
		
		List<String> mixingContainers = Arrays.asList("Large_Bowl");
		for (String container : mixingContainers) { 
			state.addObject(ContainerFactory.getNewMixingContainerObjectInstance(domain, container, null, SpaceFactory.SPACE_COUNTER));
		}
		
		List<String> heatingContainers = Arrays.asList("Large_Pot", "Large_Saucepan");
		for (String container : heatingContainers) { 
			state.addObject(ContainerFactory.getNewHeatingContainerObjectInstance(domain, container, null, SpaceFactory.SPACE_COUNTER));
		}
		
		List<String> bakingContainers = Arrays.asList("Baking_Dish");
		for (String container : bakingContainers) { 
			state.addObject(ContainerFactory.getNewBakingContainerObjectInstance(domain, container, null, SpaceFactory.SPACE_COUNTER));
		}
		
		List<String> containers = new ArrayList<String>();
		containers.addAll(mixingContainers);
		containers.addAll(heatingContainers);
		containers.addAll(bakingContainers);
		state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, SpaceFactory.SPACE_COUNTER, containers, "human"));
		state.addObject(SpaceFactory.getNewObjectInstance(domain, "shelf", SpaceFactory.NO_ATTRIBUTES, null, ""));
		
		ObjectClass containerClass = domain.getObjectClass(ContainerFactory.ClassName);		
		ObjectInstance shelfSpace = state.getObject(SpaceFactory.SPACE_COUNTER);
		
		List<ObjectInstance> ingredientInstances = 
				this.knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, recipe.topLevelIngredient);
		
		this.ingredientContainers = 
				Recipe.getContainers(containerClass, ingredientInstances, shelfSpace.getName());
		
		for (ObjectInstance ingredientInstance : ingredientInstances) {
			if (state.getObject(ingredientInstance.getName()) == null) {
				state.addObject(ingredientInstance);
			}
		}
		
		for (ObjectInstance containerInstance : this.ingredientContainers) {
			if (state.getObject(containerInstance.getName()) == null) {
				ContainerFactory.changeContainerSpace(containerInstance, shelfSpace.getName());
				state.addObject(containerInstance);
			}
		}
		
		state.addObject(AgentFactory.getNewHumanAgentObjectInstance(domain, "human"));
		state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, "shelf", null, null));
		
		return state;
	}
	
	public void init() {
		if (this.domain == null) {
			this.domain = this.generateDomain();
		}
		
		if (this.parser == null) {
			this.parser = new StateJSONParser(this.domain);
		}
		if (this.isSuccess == null) {
			this.isSuccess = new RecipeFinished("success", domain, this.recipe.topLevelIngredient);
		}
		
		if (this.isFailure == null) {
			this.isFailure = new RecipeBotched("botched", domain, this.recipe.topLevelIngredient);
		}

		this.recipe.resetSubgoals();
		this.recipe.setUpSubgoals(this.domain);
		if (this.recipeSubgoals == null) {
			this.recipeSubgoals = new ArrayList<BakingSubgoal>(this.recipe.getSubgoals());
			this.completedSubgoals = new boolean[this.recipeSubgoals.size()];
		}
		
		if (((RecipeBotched)this.isFailure).hasNoSubgoals()) {
			this.addSubgoalsToBotched(recipe.getSubgoals(), ((RecipeBotched)this.isFailure));
		}
	}
	
	public String resetCurrentState() {
		this.init();
		this.currentState = this.getInitialState();
		this.resetCompletedSubgoals(this.completedSubgoals.length);
		return this.parser.stateToString(this.currentState);
	}
	
	public BakingActionResult takeAction(String actionName, String[] params) {
		this.init();
		
		StateHashTuple previousTuple = this.stateHashFactory.hashState(this.currentState);
		BakingAction action = (BakingAction)this.domain.getAction(actionName);
		if (action == null) {
			return BakingActionResult.failure(actionName + " is not a valid action");
		}
		
		BakingActionResult result = action.checkActionIsApplicableInState(this.currentState, params);
		if (!result.getIsSuccess()) {
			return result;
		}

		this.currentState = action.performAction(this.currentState, params);
		this.removeEmptyIngredientContainers(this.currentState);
		StateHashTuple newTuple = this.stateHashFactory.hashState(this.currentState);
		
		if (previousTuple.hashCode() == newTuple.hashCode()) {
			int len = params.length;
			String message = actionName + " had no effect with params [";
			for (int i = 0; i <  len; i++) {
				message += (i < len - 1) ? params[i] + ", " : params[i]; 
			}
			message += "]";
			return BakingActionResult.failure(message);
		}
		return BakingActionResult.success();
	}
	
	protected void removeEmptyIngredientContainers(State state) {
		List<ObjectInstance> containers = new ArrayList<ObjectInstance>(this.ingredientContainers);
		this.ingredientContainers = new ArrayList<ObjectInstance>();
		for (ObjectInstance container : containers) {
			ObjectInstance currentContainer = state.getObject(container.getName());
			if (ContainerFactory.getContentNames(currentContainer).size() == 0) {
				state.removeObject(currentContainer);
			}
			else {
				this.ingredientContainers.add(currentContainer);
			}
		}
	}
	
	public boolean getIsSuccess() {
		return this.isSuccess.isTrue(this.currentState, "");
	}
	
	public boolean getIsBotched() {
		return this.isFailure.isTrue(this.currentState, "");
	}
	
	// Add ingredient-only subgoals to the RecipeBotched propositional function
	private void addSubgoalsToBotched(List<BakingSubgoal> subgoals, RecipeBotched botched) {
		for (BakingSubgoal sg : subgoals) {
			if (sg.getGoal().getClassName().equals(AffordanceCreator.FINISH_PF)) {
				botched.addSubgoal(sg);
			}
		}
	}
	
	public boolean[] getCompletedSubgoals() {
		this.checkCompletedSubgoals(this.currentState);
		return this.completedSubgoals;
	}
	
	
	private void checkCompletedSubgoals(State state) {
		int len = this.recipeSubgoals.size();
		for (int i = 0; i < len; i++) {
			this.completedSubgoals[i] |= this.recipeSubgoals.get(i).goalCompleted(state);
		}
	}
	
	private void resetCompletedSubgoals(int len) {
		this.completedSubgoals = new boolean[len];
	}

}
