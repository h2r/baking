package edu.brown.cs.h2r.baking.Experiments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.auxiliary.common.StateJSONParser;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.SADomain;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeBotched;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeFinished;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.MakeSpanFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.actions.MixAction;
import edu.brown.cs.h2r.baking.actions.MoveAction;
import edu.brown.cs.h2r.baking.actions.PourAction;

public class TwoPlayerKitchen implements DomainGenerator {
	Domain domain;
	StateJSONParser parser;
	Recipe recipe;
	State currentState;
	PropositionalFunction isSuccess;
	PropositionalFunction isFailure;
	public TwoPlayerKitchen(Recipe recipe) {
		this.recipe = recipe;
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
	
	private State getInitialState() {
		State state = new State();
		Action mix = new MixAction(domain);
		//Action bake = new BakeAction(domain);
		Action pour = new PourAction(domain);
		Action move = new MoveAction(domain);
		//state.addObject(SpaceFactory.getNewBakingSpaceObjectInstance(this.domain, "Oven", null, ""));
		//state.addObject(SpaceFactory.getNewHeatingSpaceObjectInstance(this.domain, "Stove Top", null, ""));
		
		
		List<String> mixingContainers = Arrays.asList("Large Bowl");
		for (String container : mixingContainers) { 
			//state.addObject(ContainerFactory.getNewMixingContainerObjectInstance(domain, container, null, "counter"));
		}
		
		List<String> heatingContainers = Arrays.asList("Large Pot", "Large Saucepan");
		for (String container : heatingContainers) { 
			//state.addObject(ContainerFactory.getNewHeatingContainerObjectInstance(domain, container, null, "counter"));
		}
		
		List<String> bakingContainers = Arrays.asList("Baking Dish");
		for (String container : bakingContainers) { 
			//state.addObject(ContainerFactory.getNewHeatingContainerObjectInstance(domain, container, null, "counter"));
		}
		
		List<String> containers = new ArrayList<String>();
		containers.addAll(mixingContainers);
		containers.addAll(heatingContainers);
		containers.addAll(bakingContainers);
		//state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, "counter", containers, "human"));
		
		
		
		//state.addObject(AgentFactory.getNewHumanAgentObjectInstance(domain, "human"));
		//state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, "shelf", null, null));
		
		
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
	
	public String takeAction(String actionName, String[] params) {
		this.init();
		
		Action action = this.domain.getAction(actionName);
		if (action != null) {
			action.performAction(this.currentState, params);
		}
		
		return this.parser.stateToString(this.currentState);	
	}
	
	public boolean getIsSuccess() {
		return this.isSuccess.isTrue(this.currentState, "");
	}
	
	public boolean getIsBotched() {
		return this.isFailure.isTrue(this.currentState, "");
	}

}
