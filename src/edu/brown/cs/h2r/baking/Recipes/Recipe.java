package edu.brown.cs.h2r.baking.Recipes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import edu.brown.cs.h2r.baking.Knowledgebase.IngredientKnowledgebase;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

import java.util.AbstractMap;


public abstract class Recipe {
	
	public IngredientRecipe topLevelIngredient;
	protected IngredientKnowledgebase knowledgebase;
	
	public static final Boolean NOT_MIXED = false;
	public static final Boolean NOT_MELTED = false;
	public static final Boolean NOT_BAKED = false;
	public static final Boolean NOT_SWAPPED = false;
	public static final Boolean NOT_PEELED = false;
	public static final Boolean MIXED = true;
	public static final Boolean MELTED = true;
	public static final Boolean BAKED = true;
	public static final Boolean SWAPPED = true;
	public static final Boolean PEELED = true;
	
	public Recipe()
	{
		this.knowledgebase = new IngredientKnowledgebase();
	}
	
	public List<String> getRecipeProcedures() {
		return Arrays.asList("Recipe not implemented");
	}
	
	public List<Boolean> getRecipeProceduresStatus() {
		return new ArrayList<Boolean>(this.getRecipeProcedures().size());
	}
	
	public int getNumberSteps()
	{
		return Recipe.getNumberSteps(this.topLevelIngredient);
	}
	public static int getNumberSteps(IngredientRecipe ingredient)
	{
		int count = 0;
		count += ingredient.getBaked() ? 1 : 0;
		count += ingredient.getMelted() ? 1 : 0;
		count += ingredient.getMixed() ? 1 : 0;
		count += ingredient.getConstituentIngredientsCount();
		return count;
	}
	public List<ObjectInstance> getRecipeList(ObjectClass simpleIngredientClass)
	{
		return IngredientFactory.getIngredientInstancesList(simpleIngredientClass, this.topLevelIngredient);
	}
	
	public static List<ObjectInstance> getContainers(ObjectClass containerClass, List<ObjectInstance> ingredients, String containerSpace)
	{
		List<ObjectInstance> containers = new ArrayList<ObjectInstance>();
		for (ObjectInstance ingredient : ingredients)
		{
			ObjectInstance container = 
					ContainerFactory.getNewIngredientContainerObjectInstance(
							containerClass, ingredient.getName() + "_bowl", ingredient.getName(), containerSpace);
			containers.add(container);
			IngredientFactory.changeIngredientContainer(ingredient, container.getName());
		}
		return containers;
	}
	
	public Boolean isSuccess(State state, ObjectInstance topLevelObject)
	{
		return Recipe.isSuccess(state, this.topLevelIngredient, topLevelObject);
	}
	
	public static Boolean isSuccess(State state, IngredientRecipe ingredientRecipe, ObjectInstance object)
	{
		if (!ingredientRecipe.AttributesMatch(object)) {
			return false;
		}
		
		if (ingredientRecipe.isSimple())
		{
			//TODO: Fix this weird bug. Inelegant solution for now.
			if (ingredientRecipe.getName() != object.getName()) {
				String obj_name = object.getName();
				String ing_name = ingredientRecipe.getName();
				for (int i = 0; i < ing_name.length(); i++) {
					if (obj_name.charAt(i) != ing_name.charAt(i)) {
						return false;
					}
				}
				return true;
			}
			return true;
		}
		
		// List of ingredients that fulfill a "trait" rather than a ingredient in recipe
		List<ObjectInstance> traitIngredients = new ArrayList<ObjectInstance>();
		
		List<IngredientRecipe> recipeContents = ingredientRecipe.getContents();
		Set<String> compulsoryTraits = ingredientRecipe.getNecessaryTraits().keySet();
		AbstractMap<String, IngredientRecipe> compulsoryTraitMap = ingredientRecipe.getNecessaryTraits();
		Set<String> objContents = IngredientFactory.getRecursiveContentsAndSwapped(state, object);
		Set<String> contents = IngredientFactory.getRecursiveContentsForIngredient(state, object);
		
		// First, take care of the "swapped" ingredients and evaluate those.
		// Get all the swapped ingredients the recipe calls for
		List<IngredientRecipe> swappedRecipeIngredients = new ArrayList<IngredientRecipe>();
		for (IngredientRecipe ing : recipeContents) {
			if (ing.getSwapped()) {
				swappedRecipeIngredients.add(ing);
			}
		}
		// If the recipe actually has swapped ingredients, then...
		if (!swappedRecipeIngredients.isEmpty()) {
			// Find any swapped ingredients in our object
			List<ObjectInstance> swappedObjectIngredients = new ArrayList<ObjectInstance>();
			for (String name : objContents) {
				ObjectInstance obj = state.getObject(name);
				if (IngredientFactory.isSwapped(obj)) {
					swappedObjectIngredients.add(obj);
				}
			}
			// If we don't have equal amounts of swapped ingredients, then we've done something wrong
			if (swappedRecipeIngredients.size() != swappedObjectIngredients.size()) {
				return false;
			}
			IngredientRecipe match;
			for (ObjectInstance swappedObj : swappedObjectIngredients) {
				match = null;
				for (IngredientRecipe swappedIng : swappedRecipeIngredients) {
					if (swappedIng.getName().equals(swappedObj.getName())) {
						match = swappedIng;
						break;
					}
				}
				// couldn't find a matched ingredient, we failed!
				if (match == null) {
					return false;
				// we've matched swapped ingredients
				} else {
					swappedRecipeIngredients.remove(match);
					recipeContents.remove(match);
					// Remove the contents of the swapped ingredients from objectContents, such that
					// we're not "double counting" those ingredients!
					for (String name : IngredientFactory.getRecursiveContentsForIngredient(state, swappedObj)) {
						contents.remove(name);
						for (String trait : compulsoryTraits) {
							if (IngredientFactory.getTraits(state.getObject(name)).contains(trait)) {
								compulsoryTraits.remove(trait);
								break;
							}
						}
					}
				}
			}
			
		}

		// We don't have enough/have too many ingredients.
		if ((recipeContents.size() + compulsoryTraits.size()) != contents.size())
		{
			return false;
		}
		
		// Gets all of the contents for the current object we've created
		for (String name : contents) {
			ObjectInstance obj = state.getObject(name);
			traitIngredients.add(obj);
		}
		
		Map<String, IngredientRecipe> complexIngredientRecipes = new HashMap<String, IngredientRecipe>();
		Map<String, IngredientRecipe> simpleIngredientRecipes = new HashMap<String, IngredientRecipe>();
		
		for (IngredientRecipe subIngredientRecipe : recipeContents)
		{		
			if (!subIngredientRecipe.isSimple())
			{
				complexIngredientRecipes.put(subIngredientRecipe.getName(), subIngredientRecipe);
			}
			else
			{
				simpleIngredientRecipes.put(subIngredientRecipe.getName(), subIngredientRecipe);
			}

		}
		/*
		 * Now, we want to remove each ingredient that is a compulsory ingredient from
		 * trait ingredients. To do so, we check if our object has all of the compulsory
		 * ingredients, and recursively checks if these recipes are successes. If it doesn't find
		 * a compulsory ingredient, it will return false. When it does find the right
		 * ingredient, it will remove it form traitIngredients, since it was "used up".
		 */
		for (IngredientRecipe ingredient : recipeContents) {
			String ingredientName = ingredient.getName();
			ObjectInstance ingredientObj = null;
			Boolean match = false;
			for (ObjectInstance obj : traitIngredients) {
				if (obj.getName().equals(ingredientName)) {
					ingredientObj = obj;
					match = true;
					break;
				}
			}
			if (!match) {
				return false;
			}
			if (ingredient.isSimple()) {
				if (!Recipe.isSuccess(state, ingredient, ingredientObj)) {
					return false;
				}
				traitIngredients.remove(ingredientObj);
			} else {
				Boolean exists = false;
				Collection<IngredientRecipe> complexIngredientRecipeValues = complexIngredientRecipes.values();
				for (IngredientRecipe complexSubIngredient : complexIngredientRecipeValues)
				{
					if (Recipe.isSuccess(state, complexSubIngredient, ingredientObj))
					{
						exists = true;
						break;
					}
				}
				if (!exists)
				{
					return false;
				}
			}
		}
		
		/* At this point, traitIngredients only has the ingredients that could be used
		 * for Traits, not as compulsory ingredients.
		 */
		for (String trait : compulsoryTraits) {
			Boolean match = false;
			for (ObjectInstance obj : traitIngredients) {
				if (IngredientFactory.getTraits(obj).contains(trait)) {
					// Ensure traitIngredient has the correct Attributes (melted, baked...)
					// I.E. The fat we are using is in fact melted.
					match = compulsoryTraitMap.get(trait).AttributesMatch(obj);
					break;
				}
			}
			if (!match) {
				return false;
			}
		}
		// All of our ingredients in our recipe are accounted for in our object, and vice versa.
		// Every ingredient has the right attribute, so we must be done!
		return true;
	}
	
	public Boolean isFailure(State state, ObjectInstance object)
	{
		return Recipe.isFailure(state, this.topLevelIngredient, object);
	}
	
	public static Boolean isFailure(State state, IngredientRecipe ingredientRecipe, ObjectInstance object)
	{
		if (Recipe.isSuccess(state, ingredientRecipe, object))
		{
			// If the recipe is complete, then we didn't fail. Go logic
			return false;
		}
		
		if (ingredientRecipe.isSimple())
		{
			if ((object.getObjectClass().name != IngredientFactory.ClassNameSimple) || 
					(object.getObjectClass().name != IngredientFactory.ClassNameSimpleHidden))
			{
				// Object is not a simple ingredient, but the ingredient we are checking against is. FAIL!
				return true;
			}
			if (ingredientRecipe.getName() != object.getName())
			{
				// They aren't even the same name. FAIL!
				return true;
			}
			// they don't have the right attributes (melted, peeled, etc).
			if (!ingredientRecipe.AttributesMatch(object)) {
				return true;
			}
			return false;
		}
		
		// Make copies so we can edit them as we go!
		List<IngredientRecipe> recipeContents = new ArrayList<IngredientRecipe>();
		for (IngredientRecipe rc : ingredientRecipe.getContents()) {
			recipeContents.add(rc);
		}
		Set<String> recipeTraits = new TreeSet<String>();
		for (String trait : ingredientRecipe.getNecessaryTraits().keySet()) {
			recipeTraits.add(trait);
		}
		Set<String> contents = new TreeSet<String>();
		for (String content :IngredientFactory.getRecursiveContentsForIngredient(state, object)) {
			contents.add(content);
		}
		
		// For all of our contents...
		// If we've found a match in our ingredients
		Boolean foundAGoodIngredient = false;
		for (String content_name : contents) {
			// If current ingredient is a match
			Boolean goodIngredient = false;
			// Check that the object is a required ingredient
			for (IngredientRecipe ing : recipeContents) {
				if (ing.getName().equals(content_name)) {
					goodIngredient = true;
					foundAGoodIngredient = true;
				}
			}
			// or check that it fulfills a required trait
			for (String trait : IngredientFactory.getTraits(state.getObject(content_name))) {
				if (recipeTraits.contains(trait)) {
					goodIngredient = true;
					foundAGoodIngredient = true;
				}
			}
			// Current Ingredient isn't a necessary ingredient NOR does it fulfill any
			// of the required traits
			if (!goodIngredient) {
				// In our ingredients, we have added at least one "good" ingredient, but have also
				// ruined our recipe by adding a bad ingredient -- we failed! 
				if (foundAGoodIngredient) {
					return true;
				}
			}
		}
		
		
		// ensure that our there are still enough ingredients in our state to fulfill
		// all the necessary ingredients and traits!
		for (String name : contents) {
			IngredientRecipe match = null;
			for (IngredientRecipe recipeContent : recipeContents) {
				if (recipeContent.getName().equals(name)) {
					match = recipeContent;
					break;
				}
			}
			if (match != null) {
				recipeContents.remove(match);
			} else {
				ObjectInstance ing = state.getObject(name);
				String trait_match = null;
				for (String trait : IngredientFactory.getTraits(ing)) {
					if (recipeTraits.contains(trait)) {
						trait_match = trait;
						break;
					}
				}
				if (trait_match != null) {
					recipeTraits.remove(trait_match);
				}
			}
		}
		List<ObjectInstance> simple_objs = state.getObjectsOfTrueClass("simple_ingredient");
		for (ObjectInstance ing : simple_objs) {
			IngredientRecipe match = null;
			String name = ing.getName();
			for (IngredientRecipe recipeContent : recipeContents) {
				if (recipeContent.getName().equals(name)) {
					match = recipeContent;
					break;
				}
			}
			if (match != null) {
				recipeContents.remove(match);
			} else {
				String trait_match = null;
				for (String trait : IngredientFactory.getTraits(ing)) {
					if (recipeTraits.contains(trait)) {
						trait_match = trait;
						break;
					}
				}
				if (trait_match != null) {
					recipeTraits.remove(trait_match);
				}
			}
		}
		List<ObjectInstance> complex_objs = state.getObjectsOfTrueClass("complex_ingredient");
		for (ObjectInstance complex_obj : complex_objs) {
			Set<String> content_names = IngredientFactory.getIngredientContents(complex_obj);
			for (String name : content_names) {
				IngredientRecipe match = null;
				ObjectInstance ing = state.getObject(name);
				for (IngredientRecipe recipeContent : recipeContents) {
					if (recipeContent.getName().equals(name)) {
						match = recipeContent;
						break;
					}
				}
				if (match != null) {
					recipeContents.remove(match);
				} else {
					String trait_match = null;
					for (String trait : IngredientFactory.getTraits(ing)) {
						if (recipeTraits.contains(trait)) {
							trait_match = trait;
							break;
						}
					}
					if (trait_match != null) {
						recipeTraits.remove(trait_match);
					}
				}
			}
		}
		// Check if there are any ingredients or traits we haven't fulfilled yet.
		if (recipeContents.size() != 0 || recipeTraits.size() != 0) {
			return true;
		}
		
		// No failures were explicitly found, therefore we haven't yet failed.
		return false;
	}
	
	public static Boolean isFailure(State state, Map<String, IngredientRecipe> simpleIngredients, Map<String, IngredientRecipe> complexIngredients, ObjectInstance object)
	{
		if ((object.getObjectClass().name == IngredientFactory.ClassNameSimple) ||
				(object.getObjectClass().name == IngredientFactory.ClassNameSimpleHidden))
		{
			if (simpleIngredients.containsKey(object.getName()))
			{
				IngredientRecipe simpleIngredient = simpleIngredients.get(object.getName());
				if (!Recipe.isFailure(state, simpleIngredient, object))
				{
					// The object we are checking is a simple ingredient, and it checks out. Carry on.
					return false;
				}
			}
		}
		Collection<IngredientRecipe> complexIngredientValues = complexIngredients.values();
		for (IngredientRecipe complexIngredient : complexIngredientValues)
		{
			if (!Recipe.isFailure(state, complexIngredient, object))
			{
				// We found a complex ingredient that matches the object we are checking. Success.
				return false;
			}
		}
		
		// This object doesn't match anything in the two lists of sub ingredients. We've failed.
		return true;
	}
}
