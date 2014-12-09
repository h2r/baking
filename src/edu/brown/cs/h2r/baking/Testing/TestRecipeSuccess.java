package edu.brown.cs.h2r.baking.Testing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.ObjectHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
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
import edu.brown.cs.h2r.baking.Recipes.DeviledEggs;
import edu.brown.cs.h2r.baking.Recipes.PecanPie;
import edu.brown.cs.h2r.baking.Recipes.Recipe;

public class TestRecipeSuccess {

	Knowledgebase knowledgebase;
	State state;
	Domain domain;
	List<ObjectInstance> allIngredients;
	IngredientRecipe topLevelIngredient;
	StateHashFactory hashingFactory = new NameDependentStateHashFactory();
	ObjectHashFactory objectHashingFactory = hashingFactory.getObjectHashFactory();
	
	@Before
	public void setUp() {
		domain = new SADomain();
		setUpDomain();
		knowledgebase = Knowledgebase.getKnowledgebase(domain);
		
		List<ObjectInstance> objectsToAdd = new ArrayList<ObjectInstance>();
		objectsToAdd.add(AgentFactory.getNewHumanAgentObjectInstance(domain, "human", objectHashingFactory));
		List<String> containers = Arrays.asList("mixing_bowl_1", "mixing_bowl_2");
		objectsToAdd.add(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, "counter", containers, "human"));

		for (String container : containers) { 
			objectsToAdd.add(ContainerFactory.getNewMixingContainerObjectInstance(domain, container, null, "counter"));
		}
		this.state = new State(objectsToAdd);
	}
	
	public void setUpState() {
		ObjectClass containerClass = domain.getObjectClass(ContainerFactory.ClassName);		
		ObjectInstance counterSpace = state.getObject("counter");

		List<ObjectInstance> objectsToAdd = new ArrayList<ObjectInstance>();
		List<ObjectInstance> ingredientInstances = allIngredients;
		List<ObjectInstance> ingredientsAndContainers = Recipe.getContainersAndIngredients(containerClass, ingredientInstances, counterSpace.getName());
		
		objectsToAdd.addAll(ingredientsAndContainers);
		this.state = this.state.appendAllObjects(objectsToAdd);
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
		topLevelIngredient = new PecanPie(domain).topLevelIngredient;
		allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(domain, topLevelIngredient);
		setUpState();
		ObjectClass complexClass = domain.getObjectClass("complex_ingredient");
		
		// Make the two swapped objects
		List<String> fillingContents = Arrays.asList("bourbon", "vanilla", "pecans", "brown_sugar", 
				"butter", "light_corn_syrup", "salt", "eggs");
		ObjectInstance finishedFilling = IngredientFactory.getNewComplexIngredientObjectInstance(
				complexClass, "finished_filling", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, "mixing_bowl_2", 
				null, null, new HashSet<String>(), new HashSet<String>(), new HashSet<String>(), fillingContents);
		
		List<String> crustContents = Arrays.asList("eggs", "brown_sugar", 
				"butter", "salt", "flour");
		ObjectInstance crust = IngredientFactory.getNewComplexIngredientObjectInstance(
				complexClass, "pie_crust", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, "mixing_bowl_1",
				null, null, new HashSet<String>(), new HashSet<String>(), new HashSet<String>(), crustContents);
		
		this.state = this.state.appendAllObjects(Arrays.asList(finishedFilling, crust));
		
		// Make the object we're testing!
		ObjectInstance pie = IngredientFactory.getNewComplexIngredientObjectInstance(
				complexClass, "PecanPie", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, "mixing_bowl_1", 
				null, null, new HashSet<String>(), new HashSet<String>(), new HashSet<String>(), Arrays.asList("finished_filling", "pie_crust"));
		
		pie = IngredientFactory.bakeIngredient(pie);
		BakingAsserts.assertSuccess(state, topLevelIngredient, pie);
	}
	
	// test a successful recipe with a swapped and some ingredients
	@Test
	public void testSwappedCombinationSuccess() {
		topLevelIngredient = new DeviledEggs(domain).topLevelIngredient;
		allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(domain, topLevelIngredient);
		setUpState();
		
		ObjectInstance finishedMix = IngredientFactory.getNewComplexIngredientObjectInstance(
				domain.getObjectClass("complex_ingredient"), "finished_mix", Recipe.NO_ATTRIBUTES,
				Recipe.SWAPPED, "mixing_bowl_2", null, null, new HashSet<String>(), new HashSet<String>(), new HashSet<String>(), Arrays.asList("egg_yolks", 
						"salt", "pepper", "dijon_mustard", "sweet_gherkins", "chopped_tarragon", "shallots"));
		this.state = this.state.appendObject(finishedMix);
		
		ObjectInstance deviledEggs = IngredientFactory.getNewComplexIngredientObjectInstance(
				domain.getObjectClass("complex_ingredient"), "DeviledEggs", Recipe.NO_ATTRIBUTES, 
				Recipe.SWAPPED, "mixing_bowl_1", null, null, new HashSet<String>(), new HashSet<String>(), new HashSet<String>(),
				Arrays.asList("egg_whites", "finished_mix"));

		BakingAsserts.assertSuccess(state, topLevelIngredient, deviledEggs);
	}

}
