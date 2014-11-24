package edu.brown.cs.h2r.baking.Testing;

import java.util.AbstractMap;
import java.util.List;
import java.util.Set;

import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;

import org.junit.Assert;

import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ToolFactory;
import edu.brown.cs.h2r.baking.Recipes.Recipe;

public class BakingAsserts {
	
	public static void assertIsBaked(IngredientRecipe ing) {
		Assert.assertTrue(ing.getBaked());
	}
	
	public static void assertIsBaked(ObjectInstance ing) {
		Assert.assertTrue(IngredientFactory.isBakedIngredient(ing));
	}
	
	public static void assertIsNotBaked(IngredientRecipe ing) {
		Assert.assertFalse(ing.getBaked());
	}
	
	public static void assertIsNotBaked(ObjectInstance ing) {
		Assert.assertFalse(IngredientFactory.isBakedIngredient(ing));
	}
	
	public static void assertIsHeated(IngredientRecipe ing) {
		Assert.assertTrue(ing.getHeated());
	}
	
	public static void assertIsHeated(ObjectInstance ing) {
		Assert.assertTrue(IngredientFactory.isHeatedIngredient(ing));
	}
	
	public static void assertIsNotHeated(IngredientRecipe ing) {
		Assert.assertFalse(ing.getHeated());
	}
	
	public static void assertIsNotHeated(ObjectInstance ing) {
		Assert.assertFalse(IngredientFactory.isHeatedIngredient(ing));
	}
	
	public static void assertIsMixed(IngredientRecipe ing) {
		Assert.assertTrue(ing.getMixed());
	}
	
	public static void assertIsMixed(ObjectInstance ing) {
		Assert.assertTrue(IngredientFactory.isMixedIngredient(ing));
	}
	
	
	public static void assertIsSwapped(IngredientRecipe ing) {
		Assert.assertTrue(ing.getSwapped());
	}
	
	public static void assertIsSwapped(ObjectInstance ing) {
		Assert.assertTrue(IngredientFactory.isSwapped(ing));
	}
	
	public static void assertCorrectUseCount(IngredientRecipe ing, int uc) {
		Assert.assertEquals(ing.getUseCount(), uc);
	}
	
	public static void assertCorrectUseCount(ObjectInstance ing, int uc) {
		Assert.assertEquals(IngredientFactory.getUseCount(ing), uc);
	}
	
	public static void assertUseCounts(List<ObjectInstance> ingredients, AbstractMap<String, Integer> useCounts) {
		for (ObjectInstance ingredient : ingredients) {
			BakingAsserts.assertCorrectUseCount(ingredient, useCounts.get(ingredient.getName()));
		}
	}
	
	
	public static void assertHasTrait(IngredientRecipe ing, String trait) {
		Assert.assertTrue(ing.hasThisTrait(trait));
	}
	
	public static void assertHasTrait(ObjectInstance ing, String trait) {
		Assert.assertTrue(IngredientFactory.getTraits(ing).contains(trait));
	}
	
	public static void assertIngredientContains(IngredientRecipe ing, IngredientRecipe content) {
		Assert.assertTrue(ing.getContents().contains(content));
	}
	
	public static void assertIngredientContains(IngredientRecipe ing, List<IngredientRecipe> ings) {
		List<IngredientRecipe> ingredients = ing.getContents();
		for (IngredientRecipe i : ings) {
			Assert.assertTrue(ingredients.contains(i));
		}
	}
	
	public static void assertIngredientContains(ObjectInstance ing, String content) {
		Assert.assertTrue(IngredientFactory.getContentsForIngredient(ing).contains(content));
	}
	
	public static void assertContainerContains(ObjectInstance container, String content) {
		Assert.assertTrue(ContainerFactory.getContentNames(container).contains(content));
	}
	
	public static void assertIngredientContains(ObjectInstance ing, List<String> ings) {
		Set<String> ingredients = IngredientFactory.getContentsForIngredient(ing);
		for (String i : ings) {
			Assert.assertTrue(ingredients.contains(i));
		}
	}
	
	public static void assertConstituentNecessaryTraitsMatch(IngredientRecipe ing, List<String> traits) {
		Set<String> constituentTraits = ing.getConstituentNecessaryTraits().keySet();
		Boolean match = true;
		for (String trait : traits) {
			if (!constituentTraits.contains(trait)) {
				match = false;
				break;
			}
		}
		Assert.assertTrue(match);
	}
	
	public static void assertNecessaryTraitsMatch(IngredientRecipe ing, List<String> traits) {
		Set<String> necessaryTraits = ing.getNecessaryTraits().keySet();
		Boolean match = true;
		for (String trait : traits) {
			if (!necessaryTraits.contains(trait)) {
				match = false;
				break;
			}
		}
		Assert.assertTrue(match);
	}
	
	public static void assertConstituentIngredientsMatch(IngredientRecipe ing, List<IngredientRecipe> ings) {
		List<IngredientRecipe> constituentIngredients = ing.getConstituentIngredients();
		for (IngredientRecipe i : ings) {
			Boolean match = false;
			for (IngredientRecipe ingredient : constituentIngredients) {
				if (ingredient.getFullName().equals(i.getFullName())) {
					match = true;
					break;
				}
			}
			Assert.assertTrue(match);
		}
	}
	
	public static void assertConstituentIngredientsMatch(ObjectInstance ing, State state, List<String> ings) {
		Set<String> constituentIngredients = IngredientFactory.getRecursiveContentsForIngredient(state, ing);
		Boolean match = true;
		for (String i : ings) {
			if (!constituentIngredients.contains(i)) {
				match = false;
				break;
			}
		}
		Assert.assertTrue(match);
	}
	
	public static void assertSwappedIngredientsMatch(ObjectInstance ing, State state, List<String> ings) {
		Set<String> swappedIngredients = IngredientFactory.getRecursiveContentsAndSwapped(state, ing);
		Boolean match = true;
		Assert.assertEquals(swappedIngredients.size(), ings.size());
		for (String i : ings) {
			if (!swappedIngredients.contains(i)) {
				match = false;
				break;
			}
		}
		Assert.assertTrue(match);
	}
	
	public static void assertSwappedIngredientsMatch(IngredientRecipe ing, List<String> ings) {
		Set<String> names = IngredientRecipe.getRecursiveSwappedIngredients(ing).keySet();
		Boolean match = true;
		Assert.assertEquals(names.size(), ings.size());
		for (String name : names) {
			if (!ings.contains(name)) {
				match = false;
				break;
			}
		}
		Assert.assertTrue(match);
	}
	
	public static void assertSuccess(State state, IngredientRecipe ingredient, ObjectInstance success) {
		Assert.assertTrue(ingredient.isMatching(success, state));
		Assert.assertFalse(Recipe.isFailure(state, ingredient, success));
	}
	
	public static void assertFailure(State state, IngredientRecipe ingredient, ObjectInstance failure) {
		Assert.assertFalse(ingredient.isMatching(failure, state));
		Assert.assertTrue(Recipe.isFailure(state, ingredient, failure));
	}
	
	public static void assertActionApplicable(Action a, State state, String[] params) {
		Assert.assertTrue(a.applicableInState(state, params));
	}
	
	public static void assertActionNotApplicable(Action a, State state, String[] params) {
		Assert.assertFalse(a.applicableInState(state, params));
	}
	
	public static void assertSpaceOn(ObjectInstance space) {
		Assert.assertTrue(SpaceFactory.getOnOff(space));
	}
	
	public static void assertSpaceOff(ObjectInstance space) {
		Assert.assertFalse(SpaceFactory.getOnOff(space));
	}
	
	public static void assertHasToolTrait(ObjectInstance ingredient, String trait) {
		Assert.assertTrue(IngredientFactory.getToolTraits(ingredient).contains(trait));
	}
	
	public static void assertHasToolTrait(IngredientRecipe ingredient, String trait) {
		Assert.assertTrue(ingredient.getToolTraits().contains(trait));
	}
	
	public static void assertHasToolAttribute(ObjectInstance ingredient, String attribute) {
		Assert.assertTrue(IngredientFactory.getToolAttributes(ingredient).contains(attribute));
	}
	
	public static void assertHasToolAttribute(IngredientRecipe ingredient, String attribute) {
		Assert.assertTrue(ingredient.getToolAttributes().contains(attribute));
	}
	
	public static void assertDoesntHaveToolAttribute(ObjectInstance ingredient, String attribute) {
		Assert.assertFalse(IngredientFactory.getToolAttributes(ingredient).contains(attribute));
	}
	
	public static void assertDoesntHasToolAttribute(IngredientRecipe ingredient, String attribute) {
		Assert.assertFalse(ingredient.getToolAttributes().contains(attribute));
	}
	
	public static void assertContainerIsEmpty(ObjectInstance container) {
		Assert.assertTrue(ContainerFactory.isEmptyContainer(container));
	}
	
	public static void assertToolIsEmpty(ObjectInstance tool) {
		Assert.assertTrue(ToolFactory.getContents(tool).isEmpty());
	}
	
	public static void assertToolHasIngredient(ObjectInstance tool, String ingredient) {
		Assert.assertTrue(ToolFactory.getContents(tool).contains(ingredient));
	}
	
}
