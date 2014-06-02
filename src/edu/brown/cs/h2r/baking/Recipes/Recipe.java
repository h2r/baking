package edu.brown.cs.h2r.baking.Recipes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.TreeSet;


public abstract class Recipe {
	
	public IngredientRecipe topLevelIngredient;
	private AbstractMap<String, Set<String>> traitMap;
	private List<ObjectInstance> allIngredients;
	
	public Recipe()
	{
		//get actual trait map here
		this.traitMap = generateTraitMap();
		this.allIngredients = new ArrayList<ObjectInstance>();
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
		if (IngredientFactory.isBakedIngredient(object) != ingredientRecipe.getBaked()) {
			return false;
		}
		if (IngredientFactory.isMeltedIngredient(object) != ingredientRecipe.getMelted()) {
			return false;
		}
		if (IngredientFactory.isMixedIngredient(object) != ingredientRecipe.getMixed()) {
			return false;
		}
		
		if (ingredientRecipe.isSimple())
		{
			return ingredientRecipe.getName() == object.getName();
		}
		
		List<IngredientRecipe> recipeContents = ingredientRecipe.getContents();
		Set<String> contents = IngredientFactory.getContentsForIngredient(object);
		
		if (contents.contains("dry_stuff")) {
			//System.out.println("biddies");
		}
		if (recipeContents.size() != contents.size())
		{
			return false;
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
		// Gets all of the contents for the curren object we've created
		List<ObjectInstance> complexIngredientContents = new ArrayList<ObjectInstance>();
		List<ObjectInstance> simpleIngredientContents = new ArrayList<ObjectInstance>();
		for (String name : contents) {
			ObjectInstance obj = state.getObject(name);
			if (IngredientFactory.isSimple(obj)) {
				simpleIngredientContents.add(obj);
			} else {
				complexIngredientContents.add(obj);
			}
		}
		
		// compares sizes of recipe and object for both simple and complex ingredient
		if (complexIngredientRecipes.size() != complexIngredientContents.size() ||
				simpleIngredientRecipes.size() != simpleIngredientContents.size())
		{
			return false;
		}
		
		// For each simple, calls success recursively and ensures that that ingredient is in the recipe
		for (ObjectInstance simpleIngredient : simpleIngredientContents)
		{
			String ingredientName = simpleIngredient.getName();
			if (!simpleIngredientRecipes.containsKey(ingredientName) ||
					!Recipe.isSuccess(state, simpleIngredientRecipes.get(ingredientName), simpleIngredient))
			{
				return false;
			}
		}
		
		// For each complex Calls success recursively, checks if it's a valid ingredient
		for (ObjectInstance producedInstance : complexIngredientContents)
		{
			Boolean exists = false;
			Collection<IngredientRecipe> complexIngredientRecipeValues = complexIngredientRecipes.values();
			for (IngredientRecipe complexSubIngredient : complexIngredientRecipeValues)
			{
				if (Recipe.isSuccess(state, complexSubIngredient, producedInstance))
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
	
	public AbstractMap<String, Set<String>> getTraitMap() {
		return this.traitMap;
	}
	
	public AbstractMap<String, Set<String>> generateTraitMap() {
		AbstractMap<String, Set<String>> traitMap = new HashMap<String, Set<String>>();
		Set<String> traits;
		
		traits = new TreeSet<String>();
		traits.add("dry");
		traitMap.put("dry_stuff", traits);
		
		traits = new TreeSet<String>();
		traits.add("dry");
		traitMap.put("baking_soda", traits);
		
		traits = new TreeSet<String>();
		traits.add("dry");
		traits.add("flour");
		traitMap.put("flour", traits);
		
		traits = new TreeSet<String>();
		traits.add("dry");
		traitMap.put("baking_powder", traits);
		
		traits = new TreeSet<String>();
		traits.add("dry");
		traits.add("seasoning");
		traitMap.put("salt", traits);
		
		traits = new TreeSet<String>();
		traits.add("wet");
		traits.add("sugar");
		traitMap.put("sugar", traits);
		
		traits = new TreeSet<String>();
		traits.add("wet");
		traitMap.put("eggs", traits);
		
		traits = new TreeSet<String>();
		traits.add("wet");
		traits.add("fat");
		traitMap.put("butter", traits);
		
		traits = new TreeSet<String>();
		traits.add("wet");
		traitMap.put("cocoa", traits);
		
		traits = new TreeSet<String>();
		traits.add("wet");
		traitMap.put("wet_stuff", traits);
		
		traits = new TreeSet<String>();
		traitMap.put("brownies", traits);
		
		System.out.println(traitMap);
		return traitMap;
	}
	
	public Set<String> getTraits(String ingredient) {
		if (this.traitMap.containsKey(ingredient)) {
			return this.traitMap.get(ingredient);
		}
		return new TreeSet<String>();
	}

	public void addAllIngredients(State state, Domain domain) {
		for (IngredientRecipe ing : topLevelIngredient.getConstituentIngredients()) {
			ObjectClass oc = ing.isSimple() ? domain.getObjectClass(IngredientFactory.ClassNameSimple) : domain.getObjectClass(IngredientFactory.ClassNameComplex);
			ObjectInstance obj = IngredientFactory.getNewIngredientInstance(ing, ing.getName(), oc);
			allIngredients.add(obj);
		}
	}
	
	public List<ObjectInstance> getAllIngredients() {
		return allIngredients;
	}
	
}
