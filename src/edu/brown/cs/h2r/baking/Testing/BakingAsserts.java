package edu.brown.cs.h2r.baking.Testing;

import java.util.AbstractMap;
import java.util.List;
import java.util.Set;

import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import static org.junit.Assert.*;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.Recipes.Recipe;

public class BakingAsserts {
	
	public static void assertIsBaked(IngredientRecipe ing) {
		assertTrue(ing.getBaked());
	}
	
	public static void assertIsBaked(ObjectInstance ing) {
		assertTrue(IngredientFactory.isBakedIngredient(ing));
	}
	
	public static void assertIsNotBaked(IngredientRecipe ing) {
		assertFalse(ing.getBaked());
	}
	
	public static void assertIsNotBaked(ObjectInstance ing) {
		assertFalse(IngredientFactory.isBakedIngredient(ing));
	}
	
	public static void assertIsMelted(IngredientRecipe ing) {
		assertTrue(ing.getMelted());
	}
	
	public static void assertIsMelted(ObjectInstance ing) {
		assertTrue(IngredientFactory.isMeltedIngredient(ing));
	}
	
	public static void assertIsNotMelted(IngredientRecipe ing) {
		assertFalse(ing.getMelted());
	}
	
	public static void assertIsNotMelted(ObjectInstance ing) {
		assertFalse(IngredientFactory.isMeltedIngredient(ing));
	}
	
	public static void assertIsMixed(IngredientRecipe ing) {
		assertTrue(ing.getMixed());
	}
	
	public static void assertIsMixed(ObjectInstance ing) {
		assertTrue(IngredientFactory.isMixedIngredient(ing));
	}
	
	public static void assertIsPeeled(IngredientRecipe ing) {
		assertTrue(ing.getPeeled());
	}
	
	public static void assertIsPeeled(ObjectInstance ing) {
		assertTrue(IngredientFactory.isPeeledIngredient(ing));
	}
	
	public static void assertIsNotPeeled(IngredientRecipe ing) {
		assertFalse(ing.getPeeled());
	}
	
	public static void assertIsNotPeeled(ObjectInstance ing) {
		assertFalse(IngredientFactory.isPeeledIngredient(ing));
	}
	
	public static void assertIsSwapped(IngredientRecipe ing) {
		assertTrue(ing.getSwapped());
	}
	
	public static void assertIsSwapped(ObjectInstance ing) {
		assertTrue(IngredientFactory.isSwapped(ing));
	}
	
	public static void assertCorrectUseCount(IngredientRecipe ing, int uc) {
		assertEquals(ing.getUseCount(), uc);
	}
	
	public static void assertCorrectUseCount(ObjectInstance ing, int uc) {
		assertEquals(IngredientFactory.getUseCount(ing), uc);
	}
	
	public static void assertUseCounts(List<ObjectInstance> ingredients, AbstractMap<String, Integer> useCounts) {
		for (ObjectInstance ingredient : ingredients) {
			assertCorrectUseCount(ingredient, useCounts.get(ingredient.getName()));
		}
	}
	
	
	public static void assertHasTrait(IngredientRecipe ing, String trait) {
		assertTrue(ing.hasThisTrait(trait));
	}
	
	public static void assertHasTrait(ObjectInstance ing, String trait) {
		assertTrue(IngredientFactory.getTraits(ing).contains(trait));
	}
	
	public static void assertIngredientContains(IngredientRecipe ing, IngredientRecipe content) {
		assertTrue(ing.getContents().contains(content));
	}
	
	public static void assertIngredientContains(IngredientRecipe ing, List<IngredientRecipe> ings) {
		List<IngredientRecipe> ingredients = ing.getContents();
		for (IngredientRecipe i : ings) {
			assertTrue(ingredients.contains(i));
		}
	}
	
	public static void assertIngredientContains(ObjectInstance ing, String content) {
		assertTrue(IngredientFactory.getContentsForIngredient(ing).contains(content));
	}
	
	public static void assertContainerContains(ObjectInstance container, String content) {
		assertTrue(ContainerFactory.getContentNames(container).contains(content));
	}
	
	public static void assertIngredientContains(ObjectInstance ing, List<String> ings) {
		Set<String> ingredients = IngredientFactory.getContentsForIngredient(ing);
		for (String i : ings) {
			assertTrue(ingredients.contains(i));
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
		assertTrue(match);
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
		assertTrue(match);
	}
	
	public static void assertConstituentIngredientsMatch(IngredientRecipe ing, List<IngredientRecipe> ings) {
		List<IngredientRecipe> constituentIngredients = ing.getConstituentIngredients();
		for (IngredientRecipe i : ings) {
			Boolean match = false;
			for (IngredientRecipe ingredient : constituentIngredients) {
				if (ingredient.getName().equals(i.getName())) {
					match = true;
					break;
				}
			}
			assertTrue(match);
		}
	}
	
	public static void assertConstituentIngredientsMatch(ObjectInstance ing, State s, List<String> ings) {
		Set<String> constituentIngredients = IngredientFactory.getRecursiveContentsForIngredient(s, ing);
		Boolean match = true;
		for (String i : ings) {
			if (!constituentIngredients.contains(i)) {
				match = false;
				break;
			}
		}
		assertTrue(match);
	}
	
	public static void assertSwappedIngredientsMatch(ObjectInstance ing, State s, List<String> ings) {
		Set<String> swappedIngredients = IngredientFactory.getRecursiveContentsAndSwapped(s, ing);
		Boolean match = true;
		assertEquals(swappedIngredients.size(), ings.size());
		for (String i : ings) {
			if (!swappedIngredients.contains(i)) {
				match = false;
				break;
			}
		}
		assertTrue(match);
	}
	
	public static void assertSwappedIngredientsMatch(IngredientRecipe ing, List<String> ings) {
		Set<String> names = IngredientRecipe.getRecursiveSwappedIngredients(ing).keySet();
		Boolean match = true;
		assertEquals(names.size(), ings.size());
		for (String name : names) {
			if (!ings.contains(name)) {
				match = false;
				break;
			}
		}
		assertTrue(match);
	}
	
	public static void assertSuccess(State s, IngredientRecipe ingredient, ObjectInstance success) {
		assertTrue(Recipe.isSuccess(s, ingredient, success));
		assertFalse(Recipe.isFailure(s, ingredient, success));
	}
	
	public static void assertFailure(State s, IngredientRecipe ingredient, ObjectInstance failure) {
		assertFalse(Recipe.isSuccess(s, ingredient, failure));
		assertTrue(Recipe.isFailure(s, ingredient, failure));
	}
	
	public static void assertActionApplicable(Action a, State s, String[] params) {
		assertTrue(a.applicableInState(s, params));
	}
	
	public static void assertActionNotApplicable(Action a, State s, String[] params) {
		assertFalse(a.applicableInState(s, params));
	}
	
	public static void assertSpaceOn(ObjectInstance space) {
		assertTrue(SpaceFactory.getOnOff(space));
	}
	
	public static void assertSpaceOff(ObjectInstance space) {
		assertFalse(SpaceFactory.getOnOff(space));
	}
}
