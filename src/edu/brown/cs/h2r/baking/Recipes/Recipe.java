package edu.brown.cs.h2r.baking.Recipes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
	
	protected final Boolean NOTMIXED= false;
	protected final Boolean NOTMELTED= false;
	protected final Boolean NOTBAKED= false;
	protected final Boolean MIXED= true;
	protected final Boolean MELTED= true;
	protected final Boolean BAKED= true;;
	
	public Recipe()
	{
		this.knowledgebase = new IngredientKnowledgebase();
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
		if (!AttributesMatch(ingredientRecipe, object)) {
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
		
		if (!ingredientRecipe.isSimple()) {
			Set<String> contents = IngredientFactory.getContentsForIngredient(object);
			
			if ((recipeContents.size() + compulsoryTraits.size()) != contents.size())
			{
				return false;
			}
			// Gets all of the contents for the current object we've created
			for (String name : contents) {
				ObjectInstance obj = state.getObject(name);
				traitIngredients.add(obj);
			}
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
				if (obj.getAllRelationalTargets("traits").contains(trait)) {
					// Ensure traitIngredient has the correct Attributes (melted, baked...)
					// I.E. The fat we are using is in fact melted.
					match = AttributesMatch(compulsoryTraitMap.get(trait), obj);
					break;
				}
			}
			if (!match) {
				return false;
			}
		}
		
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
			if ((!ingredientRecipe.getBaked() && IngredientFactory.isBakedIngredient(object)) ||
				(!ingredientRecipe.getMelted() && IngredientFactory.isMeltedIngredient(object)) ||
				(!ingredientRecipe.getMixed() && IngredientFactory.isMixedIngredient(object)))
			{
				// One of baked/melted/mixed is true that shouldn't be. FAIL!
				return true;
			}
		}
		
		List<IngredientRecipe> recipeContents = ingredientRecipe.getContents();
		Set<String> contents = IngredientFactory.getContentsForIngredient(object);
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
		

		List<ObjectInstance> producedIngredientInstances =
				state.getObjectsOfTrueClass(IngredientFactory.ClassNameComplex);
		List<ObjectInstance> complexIngredientContents = new ArrayList<ObjectInstance>();
		for (ObjectInstance obj : producedIngredientInstances)
		{
			if (contents.contains(obj.getName()))
			{
				complexIngredientContents.add(obj);
			}
		}
		
		List<ObjectInstance> simpleIngredientInstances = 
				state.getObjectsOfTrueClass(IngredientFactory.ClassNameSimple);
		List<ObjectInstance> simpleIngredientContents = new ArrayList<ObjectInstance>();
		for (ObjectInstance obj : simpleIngredientInstances)
		{
			if (contents.contains(obj.getName()))
			{
				simpleIngredientContents.add(obj);
			}
		}
		
		if (simpleIngredientContents.size() > simpleIngredientRecipes.size() ||
			complexIngredientContents.size() > complexIngredientRecipes.size())
		{
			// The number of simple/complex ingredients is greater than what we've called for. DESCEND!
			return Recipe.isFailure(state, simpleIngredientRecipes, complexIngredientRecipes, object);
		}
	
		for (ObjectInstance obj : simpleIngredientContents)
		{
			if (simpleIngredientRecipes.containsKey(obj.getName()) &&
				Recipe.isFailure(state, simpleIngredientRecipes.get(obj.getName()), obj))
			{
				// If the list of simple ingredients does contain this simple ingredient, but
				// the two simple ingredients don't match, we must descend.
				return Recipe.isFailure(state, simpleIngredientRecipes, complexIngredientRecipes, object);
			}		
		}
		
		for (ObjectInstance obj : complexIngredientContents)
		{
			Boolean found = false;
			for (IngredientRecipe subSubIngredient : complexIngredientRecipes.values())
			{
				if (!Recipe.isFailure(state, subSubIngredient, obj))
				{
					found = true;
				}
			}
			if (!found)
			{
				// If we can't find this objectInstance in the complex ingredient list, we must descend.
				return Recipe.isFailure(state, simpleIngredientRecipes, complexIngredientRecipes, object);
			}
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
	
	public static Boolean AttributesMatch(IngredientRecipe ingredientRecipe, ObjectInstance object) {
		if (IngredientFactory.isBakedIngredient(object) != ingredientRecipe.getBaked()) {
			return false;
		}
		if (IngredientFactory.isMeltedIngredient(object) != ingredientRecipe.getMelted()) {
			return false;
		}
		if (IngredientFactory.isMixedIngredient(object) != ingredientRecipe.getMixed()) {
			return false;
		}
		return true;
	}
}
