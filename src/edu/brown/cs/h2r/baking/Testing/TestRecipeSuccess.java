package edu.brown.cs.h2r.baking.Testing;

import java.util.Arrays;
import java.util.List;
import java.util.HashSet;

import org.junit.*;
import org.junit.Test;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.SADomain;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.Knowledgebase;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.Recipes.*;

public class TestRecipeSuccess {

	Knowledgebase knowledgebase;
	State state;
	Domain domain;
	List<ObjectInstance> allIngredients;
	IngredientRecipe topLevelIngredient;
	
	@Before
	public void setUp() {
		domain = new SADomain();
		setUpDomain();
		state = new State();
		state.addObject(AgentFactory.getNewHumanAgentObjectInstance(domain, "human"));
		List<String> containers = Arrays.asList("mixing_bowl_1", "mixing_bowl_2");
		state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, "counter", containers, "human"));

		for (String container : containers) { 
			state.addObject(ContainerFactory.getNewMixingContainerObjectInstance(domain, container, null, "counter"));
		}
	}
	
	public void setUpState() {
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
		allIngredients = null;
		topLevelIngredient = null;
	}
	// Test a successful recipe comprised of two swapped objects
	@Test
	public void testSwappedIngredientsSuccess() {
		topLevelIngredient = new PecanPie().topLevelIngredient;
		knowledgebase = new Knowledgebase();
		allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, topLevelIngredient);
		setUpState();
		ObjectClass complexClass = domain.getObjectClass("complex_ingredient");
		
		// Make the two swapped objects
		List<String> fillingContents = Arrays.asList("bourbon", "vanilla", "pecans", "brown_sugar", 
				"butter", "light_corn_syrup", "salt", "eggs");
		ObjectInstance finishedFilling = IngredientFactory.getNewComplexIngredientObjectInstance(
				complexClass, "finished_filling", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, "mixing_bowl_2", 
				new HashSet<String>(), new HashSet<String>(), new HashSet<String>(), fillingContents);
		
		List<String> crustContents = Arrays.asList("eggs", "brown_sugar", 
				"butter", "salt", "flour");
		ObjectInstance crust = IngredientFactory.getNewComplexIngredientObjectInstance(
				complexClass, "pie_crust", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, "mixing_bowl_1",
				new HashSet<String>(), new HashSet<String>(), new HashSet<String>(), crustContents);
		
		state.addObject(finishedFilling);
		state.addObject(crust);
		// Make the object we're testing!
		ObjectInstance pie = IngredientFactory.getNewComplexIngredientObjectInstance(
				complexClass, "PecanPie", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, "mixing_bowl_1", 
				new HashSet<String>(), new HashSet<String>(), new HashSet<String>(), Arrays.asList("finished_filling", "pie_crust"));
		
		IngredientFactory.bakeIngredient(pie);
		BakingAsserts.assertSuccess(state, topLevelIngredient, pie);
	}
	
	// test a successful recipe with a swapped and some ingredients
	@Test
	public void testSwappedCombinationSuccess() {
		topLevelIngredient = new DeviledEggs().topLevelIngredient;
		knowledgebase = new Knowledgebase();
		allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, topLevelIngredient);
		setUpState();
		
		ObjectInstance finishedMix = IngredientFactory.getNewComplexIngredientObjectInstance(
				domain.getObjectClass("complex_ingredient"), "finished_mix", Recipe.NO_ATTRIBUTES,
				Recipe.SWAPPED, "mixing_bowl_2", new HashSet<String>(), new HashSet<String>(), new HashSet<String>(), Arrays.asList("egg_yolks", 
						"salt", "pepper", "dijon_mustard", "sweet_gherkins", "chopped_tarragon", "shallots"));
		
		state.addObject(finishedMix);
		
		ObjectInstance deviledEggs = IngredientFactory.getNewComplexIngredientObjectInstance(
				domain.getObjectClass("complex_ingredient"), "DeviledEggs", Recipe.NO_ATTRIBUTES, 
				Recipe.SWAPPED, "mixing_bowl_1", new HashSet<String>(), new HashSet<String>(), new HashSet<String>(),
				Arrays.asList("egg_whites", "finished_mix"));

		BakingAsserts.assertSuccess(state, topLevelIngredient, deviledEggs);
	}

}
