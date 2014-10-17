package edu.brown.cs.h2r.baking.Testing;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.After;
import org.junit.Before;
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
import edu.brown.cs.h2r.baking.Recipes.CucumberSalad;
import edu.brown.cs.h2r.baking.Recipes.MashedPotatoes;
import edu.brown.cs.h2r.baking.Recipes.Recipe;

public class TestRecipeFailure {
		Knowledgebase knowledgebase;
		State state;
		Domain domain;
		List<ObjectInstance> allIngredients;
		IngredientRecipe topLevelIngredient;
		
		@Before
		public void setUp() {
			domain = new SADomain();
			setUpDomain();
			List<ObjectInstance> objectsToAdd = new ArrayList<ObjectInstance>();
			objectsToAdd.add(AgentFactory.getNewHumanAgentObjectInstance(domain, "human"));
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
			List<ObjectInstance> ingredientsAndContainers = 
					Recipe.getContainersAndIngredients(containerClass, ingredientInstances, counterSpace.getName());
			objectsToAdd.addAll(ingredientsAndContainers);
			this.state = state.appendAllObjects(objectsToAdd);
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
			knowledgebase = new Knowledgebase();
			allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(domain, topLevelIngredient);
			setUpState();
			
			List<String> contents = Arrays.asList("butter", "potatoes", "salt", "eggs");
			
			ObjectInstance mash = IngredientFactory.getNewComplexIngredientObjectInstance(
					domain.getObjectClass("complex_ingredient"), "Mashed_potatoes", Recipe.NO_ATTRIBUTES, 
					Recipe.SWAPPED, "mixing_bowl_1", null, null, new HashSet<String>(),new HashSet<String>(), new HashSet<String>(), contents);
			
			// Potatoes haven't been peeled
			BakingAsserts.assertFailure(state, topLevelIngredient, mash);
		}
		
		// Test double up on a trait ingredient failure
		@Test
		public void extraTraitIngredient() {
			topLevelIngredient = new MashedPotatoes().topLevelIngredient;
			knowledgebase = new Knowledgebase();
			allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(domain, topLevelIngredient);
			setUpState();
			
			List<String> contents = Arrays.asList("butter", "potatoes", "salt", "eggs", "sea_salt");
			
			ObjectInstance mash = IngredientFactory.getNewComplexIngredientObjectInstance(
					domain.getObjectClass("complex_ingredient"), "Mashed_potatoes", Recipe.NO_ATTRIBUTES,
					Recipe.SWAPPED, "mixing_bowl_1", null, null, new HashSet<String>(),new HashSet<String>(), new HashSet<String>(), contents);
			
			BakingAsserts.assertFailure(state, topLevelIngredient, mash);
			
		}
		
		// Test to see if failure is found in a subgoal of the top level ingredient
		@Test
		public void extraIngredientInSubgoal() {
			topLevelIngredient = new CucumberSalad().topLevelIngredient;
			knowledgebase = new Knowledgebase();
			allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(domain, topLevelIngredient);
			setUpState();
			ObjectClass ob = domain.getObjectClass("complex_ingredient");
			
			List<String> saladContents = Arrays.asList("tomatoes", "cucumbers", "red_onions");
			ObjectInstance salad = IngredientFactory.getNewComplexIngredientObjectInstance(
					ob, "Salad", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, "mixing_bowl_1", null,  
					null, new HashSet<String>(),new HashSet<String>(), new HashSet<String>(), saladContents);
			this.state = this.state.appendObject(salad);
			
			List<String> dressingContents = Arrays.asList("salt", "pepper", "olive_oil", "lemon_juice", "sea_salt");
			ObjectInstance dressing = IngredientFactory.getNewComplexIngredientObjectInstance(
					ob, "dressing", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, "mixing_bowl_2", null, 
					null, new HashSet<String>(),new HashSet<String>(), new HashSet<String>(), dressingContents);
			this.state = this.state.appendObject(dressing);
			
			ObjectInstance cucumberSalad = IngredientFactory.getNewComplexIngredientObjectInstance(
					ob, "CucumberSalad", Recipe.NO_ATTRIBUTES, Recipe.SWAPPED, "mixing_bowl_1", null, 
					null, new HashSet<String>(), new HashSet<String>(), new HashSet<String>(), Arrays.asList("Salad", "dressing"));
			
			BakingAsserts.assertFailure(state, topLevelIngredient, cucumberSalad);
		}

		// Tests to see if failure is found when not all ingredients are available
		@Test
		public void cantCompleteRecipe() {
			topLevelIngredient = new MashedPotatoes().topLevelIngredient;
			knowledgebase = new Knowledgebase();
			allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(domain, topLevelIngredient);
			
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
					Recipe.SWAPPED, "mixing_bowl_1", null, null, new HashSet<String>(),new HashSet<String>(), new HashSet<String>(), contents);
			
			BakingAsserts.assertFailure(state, topLevelIngredient, mash);
		}
		
		// Tests to see if failure is found if not enough of an ingredient is available
		@Test
		public void notEnoughIngredient() {
			topLevelIngredient = new MashedPotatoes().topLevelIngredient;
			knowledgebase = new Knowledgebase();
			allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(domain, topLevelIngredient);
			setUpState();
			ObjectInstance eggs = state.getObject("eggs");
			ObjectInstance newEggs = IngredientFactory.changeUseCount(eggs, 0);
			this.state = this.state.replaceObject(eggs, newEggs);
			List<String> contents = Arrays.asList("butter", "potatoes", "salt");
			
			ObjectInstance mash = IngredientFactory.getNewComplexIngredientObjectInstance(
					domain.getObjectClass("complex_ingredient"), "Mashed_potatoes", Recipe.NO_ATTRIBUTES,
					Recipe.SWAPPED, "mixing_bowl_1", null, null, new HashSet<String>(),new HashSet<String>(), new HashSet<String>(), contents);
			
			BakingAsserts.assertFailure(state, topLevelIngredient, mash);
			org.junit.Assert.assertTrue(true);
		}
}
