package edu.brown.cs.h2r.baking.Testing;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.*;
import org.junit.Test;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.SADomain;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.IngredientKnowledgebase;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.Recipes.*;


public class TestIngredientRecipe {
	IngredientKnowledgebase knowledgebase;
	State state;
	Domain domain;
	List<String> constituentNecessaryTraits, constituentIngredientList, swappedIngredientList;
	List<IngredientRecipe> constituentIngredients;
	List<ObjectInstance> allIngredients;
	AbstractMap<String, Integer> useCount;
	IngredientRecipe topLevelIngredient;
	
	public void setUp() {
		domain = new SADomain();
		setUpDomain();
		state = new State();
		setUpState();
	}
	
	private void setUpState() {
		state.addObject(AgentFactory.getNewHumanAgentObjectInstance(domain, "human"));
		List<String> containers = Arrays.asList("mixing_bowl_1", "mixing_bowl_2");
		state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, "counter", containers, "human"));

		for (String container : containers) { 
			state.addObject(ContainerFactory.getNewMixingContainerObjectInstance(domain, container, null, "counter"));
		}
		
		knowledgebase = new IngredientKnowledgebase();
		allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, topLevelIngredient);
		ObjectClass containerClass = domain.getObjectClass(ContainerFactory.ClassName);		
		ObjectInstance counterSpace = state.getObject("counter");

		List<ObjectInstance> ingredientInstances = allIngredients;
		List<ObjectInstance> containerInstances = Recipe.getContainers(containerClass, ingredientInstances, counterSpace.getName());
		
		
		for (ObjectInstance ingredientInstance : ingredientInstances) {
			if (state.getObject(ingredientInstance.getName()) == null) {
				state.addObject(ingredientInstance);
			}
		}
		
		for (ObjectInstance containerInstance : containerInstances) {
			if (state.getObject(containerInstance.getName()) == null) {
				ContainerFactory.changeContainerSpace(containerInstance, counterSpace.getName());
				state.addObject(containerInstance);
			}
		}
		
		for (ObjectInstance ingredientInstance : ingredientInstances) {
			if (IngredientFactory.getUseCount(ingredientInstance) >= 1) {
				ObjectInstance ing = state.getObject(ingredientInstance.getName());
				IngredientFactory.changeIngredientContainer(ing, ing.getName()+"_bowl");
				ContainerFactory.addIngredient(state.getObject(ing.getName()+"_bowl"), ing.getName());
			}
		}
	}
	
	private void setUpDomain() {
		domain.addObjectClass(ContainerFactory.createObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createSimpleIngredientObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createComplexIngredientObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createSimpleHiddenIngredientObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createComplexHiddenIngredientObjectClass(domain));
		domain.addObjectClass(SpaceFactory.createObjectClass(domain));		
		domain.addObjectClass(AgentFactory.getObjectClass(domain));
	}
	
	@After
	public void tearDown() {
		knowledgebase = null;
		state = null;
		domain = null;
		constituentNecessaryTraits = null;
		constituentIngredientList = null;
		constituentIngredients = null;
		swappedIngredientList = null;
		allIngredients = null;
		useCount = null;
		topLevelIngredient = null;
	}
	
	public void testRecipe() {
		BakingAsserts.assertConstituentIngredientsMatch(topLevelIngredient, constituentIngredients);
		BakingAsserts.assertConstituentNecessaryTraitsMatch(topLevelIngredient, constituentNecessaryTraits);
		BakingAsserts.assertUseCounts(allIngredients, useCount);
		BakingAsserts.assertSwappedIngredientsMatch(topLevelIngredient, swappedIngredientList);
	}
	
	@Test
	public void testBrownies() {
		topLevelIngredient = new Brownies().topLevelIngredient;
		this.setUp();
		swappedIngredientList = Arrays.asList("butter", "dry_ingredients", "wet_ingredients", "brownie_batter");
		constituentNecessaryTraits = Arrays.asList("salt", "sugar", "flour");
		constituentIngredients = new ArrayList<IngredientRecipe>();
		constituentIngredientList = Arrays.asList("baking_powder", "cocoa", "vanilla", "eggs", "butter");
		for (String ingredient : constituentIngredientList) {
			IngredientRecipe i = knowledgebase.getIngredient(ingredient);
			constituentIngredients.add(i);
		}
		
		useCount = new HashMap<String, Integer>();
		useCount.put("butter", 1);
		useCount.put("eggs", 1);
		useCount.put("flour", 1);
		useCount.put("vanilla", 1);
		useCount.put("baking_powder", 1);
		useCount.put("white_sugar", 1);
		useCount.put("brown_sugar", 1);
		useCount.put("confectioners_sugar", 1);
		useCount.put("salt", 1);
		useCount.put("sea_salt", 1);
		useCount.put("olive_oil", 1);
		useCount.put("cocoa", 1);
		
		this.testRecipe();
	}
}
