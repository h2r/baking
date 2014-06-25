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
import edu.brown.cs.h2r.baking.Knowledgebase.IngredientKnowledgebase;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.MakeSpanFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeBotched;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeFinished;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.actions.MixAction;
import edu.brown.cs.h2r.baking.actions.MoveAction;
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
	
	@Override
	public Domain generateDomain() {
		Domain domain = new SADomain();
		domain.addObjectClass(ContainerFactory.createObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createSimpleIngredientObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createComplexIngredientObjectClass(domain));
		domain.addObjectClass(SpaceFactory.createObjectClass(domain));		
		domain.addObjectClass(AgentFactory.getObjectClass(domain));
		domain.addObjectClass(MakeSpanFactory.getObjectClass(domain));
		
		Action mix = new MixAction(domain, recipe.topLevelIngredient);
		Action pour = new PourAction(domain, recipe.topLevelIngredient);
		Action move = new MoveAction(domain, recipe.topLevelIngredient);
		Action turnOnOff = new SwitchAction(domain);
		Action use = new UseAction(domain, recipe.topLevelIngredient);
		return domain;
	}
	
	private State getInitialState() {
		State state = new State();
		state.addObject(SpaceFactory.getNewBakingSpaceObjectInstance(this.domain, "Oven", null, ""));
		state.addObject(SpaceFactory.getNewHeatingSpaceObjectInstance(this.domain, "Stove", null, ""));
		
		List<String> mixingContainers = Arrays.asList("Large_Bowl");
		for (String container : mixingContainers) { 
			state.addObject(ContainerFactory.getNewMixingContainerObjectInstance(domain, container, null, "counter"));
		}
		
		List<String> heatingContainers = Arrays.asList("Large_Pot", "Large_Saucepan");
		for (String container : heatingContainers) { 
			state.addObject(ContainerFactory.getNewHeatingContainerObjectInstance(domain, container, null, "counter"));
		}
		
		List<String> bakingContainers = Arrays.asList("Baking_Dish");
		for (String container : bakingContainers) { 
			state.addObject(ContainerFactory.getNewHeatingContainerObjectInstance(domain, container, null, "counter"));
		}
		
		List<String> containers = new ArrayList<String>();
		containers.addAll(mixingContainers);
		containers.addAll(heatingContainers);
		containers.addAll(bakingContainers);
		state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, "counter", containers, "human"));
		state.addObject(SpaceFactory.getNewObjectInstance(domain, "shelf", false, false, false, false, null, ""));
		
		
		ObjectClass simpleIngredientClass = domain.getObjectClass(IngredientFactory.ClassNameSimple);
		ObjectClass containerClass = domain.getObjectClass(ContainerFactory.ClassName);		
		ObjectInstance shelfSpace = state.getObject("counter");
		
		List<ObjectInstance> ingredientInstances = 
				this.knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, recipe.topLevelIngredient);
		List<ObjectInstance> containerInstances = 
				Recipe.getContainers(containerClass, ingredientInstances, shelfSpace.getName());
		
		for (ObjectInstance ingredientInstance : ingredientInstances) {
			if (state.getObject(ingredientInstance.getName()) == null) {
				state.addObject(ingredientInstance);
			}
		}
		
		for (ObjectInstance containerInstance : containerInstances) {
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
	}
	
	public String resetCurrentState() {
		this.init();
		this.currentState = this.getInitialState();
		return this.parser.stateToString(this.currentState);
	}
	
	public boolean takeAction(String actionName, String[] params) {
		this.init();
		
		StateHashTuple previousTuple = this.stateHashFactory.hashState(this.currentState);
		Action action = this.domain.getAction(actionName);
			if (action != null && action.applicableInState(this.currentState, params)) {
			this.currentState = action.performAction(this.currentState, params);
		}
		StateHashTuple newTuple = this.stateHashFactory.hashState(this.currentState);
		
		return previousTuple.hashCode() != newTuple.hashCode();
	}
	
	public boolean getIsSuccess() {
		return this.isSuccess.isTrue(this.currentState, "");
	}
	
	public boolean getIsBotched() {
		return this.isFailure.isTrue(this.currentState, "");
	}

}
