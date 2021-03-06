package edu.brown.cs.h2r.baking.Recipes;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.Knowledgebase.Knowledgebase;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.Domain;

import java.util.AbstractMap;


public abstract class Recipe {
	
	public IngredientRecipe topLevelIngredient;
	protected Knowledgebase knowledgebase;
	protected List<BakingSubgoal> subgoals, ingredientSubgoals;
	protected Set<String> recipeToolAttributes;
	protected AbstractMap<String, IngredientRecipe> subgoalIngredients;
	
	public static final int NO_ATTRIBUTES = 0;
	public static final int MIXED = 1;
	public static final int HEATED = 2;
	public static final int BAKED = 4;
	public static final Boolean SWAPPED = true;
	public static final Boolean NOT_SWAPPED = false;
	
	public Recipe()
	{
		this.knowledgebase = new Knowledgebase();
		this.subgoals = new ArrayList<BakingSubgoal>();
		this.ingredientSubgoals = new ArrayList<BakingSubgoal>();
		this.recipeToolAttributes = new HashSet<String>();
		this.subgoalIngredients = new HashMap<String, IngredientRecipe>();
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
		count += ingredient.getHeated() ? 1 : 0;
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
		boolean subgoalSimple = ingredientRecipe.isSimple();
		boolean objectSimple = IngredientFactory.isSimple(object);
		
		// ensures that both the ingredient and the object are either simple of complex. Obviously,
		// if ones simple and the other one isn't, we haven't succeeded!
		if (subgoalSimple != objectSimple) {
			return false;
		}
		
		if (subgoalSimple)
		{
			return ingredientRecipe.getName().equals(object.getName());
		}
		// List of ingredients that fulfill a "trait" rather than a ingredient in recipe
		List<ObjectInstance> traitIngredients = new ArrayList<ObjectInstance>();
		List<IngredientRecipe> recipeContents = ingredientRecipe.getContents();
		
		// Real trait map
		AbstractMap<String, IngredientRecipe> TraitMap = ingredientRecipe.getNecessaryTraits();
		
		// Copy of trait map / trait map Key set
		AbstractMap<String, IngredientRecipe> compulsoryTraitMap = 
				new HashMap<String, IngredientRecipe>(TraitMap);
		Set<String> compulsoryTraits = new HashSet<String>(TraitMap.keySet());
		
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
			/*if (swappedRecipeIngredients.size() != swappedObjectIngredients.size()) {
				return false;
			}*/
			IngredientRecipe match;
			for (ObjectInstance swappedObj : swappedObjectIngredients) {
				match = null;
				for (IngredientRecipe swappedIng : swappedRecipeIngredients) {
					if (swappedIng.getName().equals(swappedObj.getName())) {
						int ingSize = swappedIng.getConstituentIngredients().size() + swappedIng.getConstituentNecessaryTraits().size();
						int objSize = IngredientFactory.getRecursiveContentsForIngredient(state,swappedObj).size();
						if (ingSize == objSize) {
							match = swappedIng;
							break;
						}
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
			// any unmatched swapped recipe ingredients, add their contents to recipe content
			for (IngredientRecipe swappedIng : swappedRecipeIngredients) {
				if (swappedIng.isSimple()) {
					recipeContents.add(swappedIng);
				} else {
					recipeContents.addAll(swappedIng.getContents());
				}
				AbstractMap<String, IngredientRecipe> swappedTraits = swappedIng.getNecessaryTraits();
				for (Entry<String, IngredientRecipe> entry : swappedTraits.entrySet()) {
					compulsoryTraits.add(entry.getKey());
					compulsoryTraitMap.put(entry.getKey(), entry.getValue());
				}
				recipeContents.remove(swappedIng);
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
					// Ensure traitIngredient has the correct Attributes (heated, baked...)
					// I.E. The fat we are using is in fact heated.
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
		// Object has been heated/baked, when it wasn't supposed to!
		if (ingredientRecipe.objectHasExtraAttribute(object)) {
			return true;
		}
		if (IngredientFactory.isSwapped(object)) {
			if (ingredientRecipe.getName().equals(object.getName())) {
				if (ingredientRecipe.AttributesMatch(object)) {
					return false;
				}
			}
		}
		
		if (ingredientRecipe.isSimple())
		{
			String ocName = object.getObjectClass().name;
			if (!(ocName.equals(IngredientFactory.ClassNameSimple) || ocName.equals(IngredientFactory.ClassNameSimpleHidden)))
			{
				// Object is not a simple ingredient, but the ingredient we are checking against is. FAIL!
				return true;
			}
			if (!ingredientRecipe.getName().equals(object.getName()))
			{
				// They aren't even the same name. FAIL!
				return true;
			}
			// they don't have the right attributes (heated, peeled, etc).
			if (!ingredientRecipe.AttributesMatch(object)) {
				return true;
			}
			return false;
		}
		
		// Call isFailure recursively
		for (String name : IngredientFactory.getContentsForIngredient(object)) {
			IngredientRecipe matchTo = null;
			ObjectInstance content = state.getObject(name);
			if (!IngredientFactory.isSimple(content)) {
				for (IngredientRecipe ing : ingredientRecipe.getContents()) {
					if (ing.getName().equals(name)) {
						matchTo = ing;
						break;
					}
				}
				for (IngredientRecipe ing : ingredientRecipe.getConstituentIngredients()) {
					if (ing.getName().equals(name)) {
						matchTo = ing;
						break;
					}
				}
					
				if (matchTo != null) {
					if (isFailure(state, matchTo, content)) {
						return true;
					}
				} else {
					// we've clearly made a mistake if we can't find the matching ingredient
					return true;
				}
			}
		}
		
		// Make copies so we can edit them as we go!
		List<IngredientRecipe> recipeContents = new ArrayList<IngredientRecipe>(ingredientRecipe.getContents());
		Set<String> recipeTraits = new HashSet<String>(ingredientRecipe.getNecessaryTraits().keySet());
		AbstractMap<String, IngredientRecipe> compulsoryTraitMap = ingredientRecipe.getNecessaryTraits();
		Set<String> contents = new HashSet<String>(IngredientFactory.getRecursiveContentsAndSwapped(state, object));
		
		// If we've found a match in our ingredients
		Boolean foundAGoodIngredient = false;
		
		// For all of our contents...
		for (String contentName : contents) {
			
			Boolean goodIngredient = false;
			// Check that the object is a required ingredient
			for (IngredientRecipe ing : recipeContents) {
				if (ing.getName().equals(contentName)) {
					goodIngredient = true;
					foundAGoodIngredient = true;
				}
			}
			// or check that it fulfills a required trait
			for (String trait : IngredientFactory.getTraits(state.getObject(contentName))) {
				if (recipeTraits.contains(trait)) {
					goodIngredient = true;
					foundAGoodIngredient = true;
				}
			}
			// Current Ingredient isn't a necessary ingredient NOR does it fulfill any
			// of the required traits
			if (!goodIngredient) {
				// In our ingredients, we have added at least one "good" ingredient...
				if (foundAGoodIngredient) {
					// but have also ruined our recipe by adding a bad ingredient -- we failed! 
					return true;
				}
			}
		}
		
		
		// See which necessary/trait ingredients we've fulfilled thus far
		for (String name : contents) {
			IngredientRecipe match = null;
			for (IngredientRecipe recipeContent : recipeContents) {
				if (recipeContent.getName().equals(name)) {
					if (!isFailure(state, recipeContent, state.getObject(name))) {
						match = recipeContent;
						break;
					}
				}
			}
			if (match != null) {
				recipeContents.remove(match);
			} else {
				ObjectInstance ing = state.getObject(name);
				String traitMatch = null;
				for (String trait : IngredientFactory.getTraits(ing)) {
					if (recipeTraits.contains(trait)) {
						IngredientRecipe ingredient = compulsoryTraitMap.get(trait);
						if (!isFailure(state, ingredient.getCopyWithNewName(ing.getName()), ing)) {
							traitMatch = trait;
						}
						break;
					}
				}
				if (traitMatch != null) {
					recipeTraits.remove(traitMatch);
				} else {
					// this ingredient had no match!
					return true;
				}
			}
		}
		
		// ensure that our there are still enough ingredients in our state to fulfill
		// all the necessary ingredients and traits!
		List<ObjectInstance> simpleObjs = state.getObjectsOfTrueClass("simple_ingredient");
		for (ObjectInstance ing : simpleObjs) {
			IngredientRecipe match = null;
			String name = ing.getName();
			for (IngredientRecipe recipeContent : recipeContents) {
				if (recipeContent.getName().equals(name) && IngredientFactory.getUseCount(ing) > 0) {
					match = recipeContent;
					break;
				}
			}
			if (match != null) {
				recipeContents.remove(match);
			} else {
				String traitMatch = null;
				for (String trait : IngredientFactory.getTraits(ing)) {
					if (recipeTraits.contains(trait) && IngredientFactory.getUseCount(ing) > 0) {
						traitMatch = trait;
						break;
					}
				}
				if (traitMatch != null) {
					recipeTraits.remove(traitMatch);
				}
			}
		}
		List<ObjectInstance> complexObjs = state.getObjectsOfTrueClass("complex_ingredient");
		for (ObjectInstance complexObj : complexObjs) {
			Set<String> contentNames = IngredientFactory.getIngredientContents(complexObj);
			for (String name : contentNames) {
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
					String traitMatch = null;
					for (String trait : IngredientFactory.getTraits(ing)) {
						if (recipeTraits.contains(trait)) {
							traitMatch = trait;
							break;
						}
					}
					if (traitMatch != null) {
						recipeTraits.remove(traitMatch);
					}
				}
			}
		}
		int simpleRecipeContents = 0;
		for (IngredientRecipe ing : recipeContents) {
			if (ing.isSimple()) {
				simpleRecipeContents++;
			}
		}
		
		
		// Check if there are any ingredients or traits we haven't fulfilled yet.
		if (simpleRecipeContents != 0 || recipeTraits.size() != 0) {
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
	
	public abstract void setUpSubgoals(Domain domain);
	
	public List<BakingSubgoal> getSubgoals() {
		return this.subgoals;
	}
	
	public void addIngredientSubgoals() {
		for (BakingSubgoal sg : subgoals) {
			if (sg.getGoal().getClassName().equals(AffordanceCreator.FINISH_PF)) {
				this.ingredientSubgoals.add(sg);
			}
		}
	}
	
	public List<BakingSubgoal> getIngredientSubgoals() {
		return this.ingredientSubgoals;
	}
	
	public void setUpRecipeToolAttributes() {
		for (IngredientRecipe ing : this.topLevelIngredient.getConstituentIngredients()) {
			this.recipeToolAttributes.addAll(ing.getToolAttributes());
		}
		this.recipeToolAttributes.addAll(this.topLevelIngredient.getToolAttributes());
	}
	
	public Set<String> getRecipeToolAttributes() {
		return this.recipeToolAttributes;
	}
	
	public void resetSubgoals() {
		this.subgoals = new ArrayList<BakingSubgoal>();
	}
	
	public static Set<String> getSubgoalToolAttributes(IngredientRecipe ingredient) {
		Set<String> toolAttributes = new HashSet<String>();
		toolAttributes.addAll(ingredient.getToolAttributes());
		Set<IngredientRecipe> contents = new HashSet<IngredientRecipe>(ingredient.getContents());
		contents.addAll(ingredient.getNecessaryTraits().values());
		for (IngredientRecipe ing : contents) {
			toolAttributes.addAll(ing.getToolAttributes());
		}
		return toolAttributes;
	}
	
	private static boolean subgoalRequiresBaking(IngredientRecipe ingredient) {
		if (ingredient.getBaked()) {
			return true;
		}
		if (!ingredient.isSimple()) {
			Set<IngredientRecipe> contents = new HashSet<IngredientRecipe>(ingredient.getContents());
			contents.addAll(ingredient.getNecessaryTraits().values());
			for (IngredientRecipe ing : contents) {
				if (ing.getBaked()) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static boolean subgoalRequiresHeating(IngredientRecipe ingredient) {
		if (ingredient.getHeated()) {
			return true;
		}
		if (!ingredient.isSimple()) {
			Set<IngredientRecipe> contents = new HashSet<IngredientRecipe>(ingredient.getContents());
			contents.addAll(ingredient.getNecessaryTraits().values());
			for (IngredientRecipe ing : contents) {
				if (ing.getHeated()) {
					return true;
				}
			}
		}
		return false;
	}
	
	public void addRequiredRecipeAttributes() {
		for (BakingSubgoal sg : this.ingredientSubgoals) {
			IngredientRecipe ing = sg.getIngredient();
			if (Recipe.subgoalRequiresBaking(ing)) {
				ing.setRecipeBaked();
			}
			
			if (Recipe.subgoalRequiresHeating(ing)) {
				ing.setRecipeHeated();
			}
			Set<String> subgoalToolAttributes = Recipe.getSubgoalToolAttributes(ing);
			ing.addRecipeToolAttributes(subgoalToolAttributes);
		}
	}
}
