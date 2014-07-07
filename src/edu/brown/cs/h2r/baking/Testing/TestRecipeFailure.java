package edu.brown.cs.h2r.baking.Testing;

import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;

import org.junit.After;
import org.junit.Before;
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

public class TestRecipeFailure {
		IngredientKnowledgebase knowledgebase;
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

		// Tests to see if failure is found on bad attributes on a subgoal.
		@Test
		public void badAttributesSubgoal() {
			topLevelIngredient = new MashedPotatoes().topLevelIngredient;
			knowledgebase = new IngredientKnowledgebase();
			allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, topLevelIngredient);
			setUpState();
			
			List<String> contents = Arrays.asList("butter", "potatoes", "salt", "eggs");
			
			ObjectInstance mash = IngredientFactory.getNewComplexIngredientObjectInstance(
					domain.getObjectClass("complex_ingredient"), "Mashed_potatoes", Recipe.NO_ATTRIBUTES, 
					Recipe.SWAPPED, "mixing_bowl_1", new TreeSet<String>(),new TreeSet<String>(), new TreeSet<String>(), contents);
			
			// Potatoes haven't been peeled
			BakingAsserts.assertFailure(state, topLevelIngredient, mash);
		}
		
		// Test double up on a trait ingredient failure
		@Test
		public void extraTraitIngredient() {
			topLevelIngredient = new MashedPotatoes().topLevelIngredient;
			knowledgebase = new IngredientKnowledgebase();
			allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, topLevelIngredient);
			setUpState();
			
			List<String> contents = Arrays.asList("butter", "potatoes", "salt", "eggs", "sea_salt");
			
			ObjectInstance mash = IngredientFactory.getNewComplexIngredientObjectInstance(
					domain.getObjectClass("complex_ingredient"), "Mashed_potatoes", Recipe.NO_ATTRIBUTES,
					Recipe.SWAPPED, "mixing_bowl_1", new TreeSet<String>(),new TreeSet<String>(), new TreeSet<String>(), contents);
			
			BakingAsserts.assertFailure(state, topLevelIngredient, mash);
			
		}
		
		// Test to see if failure is found in a subgoal of the top level ingredient
		@Test
		public void extraIngredientInSubgoal() {
			topLevelIngredient = new CucumberSalad().topLevelIngredient;
			knowledgebase = new IngredientKnowledgebase();
			allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, topLevelIngredient);
			setUpState();
			ObjectClass ob = domain.getObjectClass("complex_ingredient");
			
			List<String> salad_contents = Arrays.asList("tomatoes", "cucumbers", "red_onions");
			ObjectInstance salad = IngredientFactory.getNewComplexIngredientObjectInstance(
					ob, "Salad", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, "mixing_bowl_1", 
					new TreeSet<String>(),new TreeSet<String>(), new TreeSet<String>(), salad_contents);
			state.addObject(salad);
			
			List<String> dressing_contents = Arrays.asList("salt", "pepper", "olive_oil", "lemon_juice", "sea_salt");
			ObjectInstance dressing = IngredientFactory.getNewComplexIngredientObjectInstance(
					ob, "dressing", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, "mixing_bowl_2",
					new TreeSet<String>(),new TreeSet<String>(), new TreeSet<String>(), dressing_contents);
			state.addObject(dressing);
			
			ObjectInstance cucumber_salad = IngredientFactory.getNewComplexIngredientObjectInstance(
					ob, "CucumberSalad", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, "mixing_bowl_1", 
					new TreeSet<String>(), new TreeSet<String>(), new TreeSet<String>(), Arrays.asList("Salad", "dressing"));
			
			BakingAsserts.assertFailure(state, topLevelIngredient, cucumber_salad);
		}

		// Tests to see if failure is found when not all ingredients are available
		@Test
		public void cantCompleteRecipe() {
			topLevelIngredient = new MashedPotatoes().topLevelIngredient;
			knowledgebase = new IngredientKnowledgebase();
			allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, topLevelIngredient);
			
			int index = 0;
			for (ObjectInstance ing : allIngredients) {
				if (ing.getName().equals("eggs")) {
					break;
				}
				index++;
			}
			
			allIngredients.remove(index);
			setUpState();
			
			List<String> contents = Arrays.asList("butter", "potatoes", "salt");
			
			ObjectInstance mash = IngredientFactory.getNewComplexIngredientObjectInstance(
					domain.getObjectClass("complex_ingredient"), "Mashed_potatoes", Recipe.NO_ATTRIBUTES, 
					Recipe.SWAPPED, "mixing_bowl_1", new TreeSet<String>(),new TreeSet<String>(), new TreeSet<String>(), contents);
			
			BakingAsserts.assertFailure(state, topLevelIngredient, mash);
		}
		
		// Tests to see if failure is found if not enough of an ingredient is available
		@Test
		public void notEnoughIngredient() {
			topLevelIngredient = new MashedPotatoes().topLevelIngredient;
			knowledgebase = new IngredientKnowledgebase();
			allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, topLevelIngredient);
			setUpState();
			IngredientFactory.setUseCount(state.getObject("eggs"),0);
			
			List<String> contents = Arrays.asList("butter", "potatoes", "salt");
			
			ObjectInstance mash = IngredientFactory.getNewComplexIngredientObjectInstance(
					domain.getObjectClass("complex_ingredient"), "Mashed_potatoes", Recipe.NO_ATTRIBUTES,
					Recipe.SWAPPED, "mixing_bowl_1", new TreeSet<String>(),new TreeSet<String>(), new TreeSet<String>(), contents);
			
			BakingAsserts.assertFailure(state, topLevelIngredient, mash);
			org.junit.Assert.assertTrue(true);
		}
}
