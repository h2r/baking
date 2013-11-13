import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;


public abstract class Recipe {
	
	protected Ingredient topLevelIngredient;
	
	public Recipe()
	{
		
	}
	
	public int getNumberSteps()
	{
		return Recipe.getNumberSteps(this.topLevelIngredient);
	}
	public static int getNumberSteps(Ingredient ingredient)
	{
		int count = 0;
		count += ingredient.Baked ? 1 : 0;
		count += ingredient.Melted ? 1 : 0;
		count += ingredient.Mixed ? 1 : 0;
		if (ingredient instanceof SimpleIngredient)
		{
			return count;
		}
		ComplexIngredient complexIngredient = (ComplexIngredient)ingredient;
		{
			for (Ingredient subIngredient : complexIngredient.Contents)
			{
				count += Recipe.getNumberSteps(subIngredient);
			}
		}
		return count;
	}
	public List<ObjectInstance> getRecipeList(ObjectClass simpleIngredientClass)
	{
		List<ObjectInstance> ingredients = new ArrayList<ObjectInstance>();
		ingredients.addAll(this.topLevelIngredient.getSimpleObjectInstances(simpleIngredientClass));
		return ingredients;
	}
	
	public static List<ObjectInstance> getContainers(ObjectClass containerClass, List<ObjectInstance> ingredients)
	{
		List<ObjectInstance> containers = new ArrayList<ObjectInstance>();
		for (ObjectInstance ingredient : ingredients)
		{
			ObjectInstance container = 
					new ObjectInstance(containerClass, ingredient.getName() + "_bowl");
			container.setValue(ContainerClass.ATTRECEIVING, 0);
			container.setValue(ContainerClass.ATTHEATING, 0);
			container.setValue(ContainerClass.ATTMIXING, 0);
			container.addRelationalTarget(ContainerClass.ATTCONTAINS, ingredient.getName());
			containers.add(container);
		}
		return containers;
	}
	
	public Boolean isSuccess(State state, ObjectInstance topLevelObject)
	{
		return Recipe.isSuccess(state, this.topLevelIngredient, topLevelObject);
	}
	
	public static Boolean isSuccess(State state, Ingredient ingredient, ObjectInstance object)
	{
		int baked = object.getDiscValForAttribute(Ingredient.attBaked);
		if ((baked == 1) != ingredient.Baked)
		{
			return false;
		}
		int mixed = object.getDiscValForAttribute(Ingredient.attMixed);
		if ((mixed == 1) != ingredient.Mixed)
		{
			return false;
		}
		int melted = object.getDiscValForAttribute(Ingredient.attMelted);
		if ((melted == 1) != ingredient.Melted)
		{
			return false;
		}
		
		if (ingredient instanceof SimpleIngredient)
		{
			return ingredient.Name == object.getName();
		}
		
		ComplexIngredient complexIngredient = (ComplexIngredient)ingredient;
		
		Set<String> contents = object.getAllRelationalTargets(ComplexIngredient.attContains);
		if (complexIngredient.Contents.size() != contents.size())
		{
			return false;
		}
		
		Map<String, ComplexIngredient> complexIngredientsRecipe = new HashMap<String, ComplexIngredient>();
		Map<String, SimpleIngredient> simpleIngredientsRecipe = new HashMap<String, SimpleIngredient>();
		
		for (Ingredient subIngredient : complexIngredient.Contents)
		{
			if (subIngredient instanceof ComplexIngredient)
			{
				complexIngredientsRecipe.put(subIngredient.Name, (ComplexIngredient)subIngredient);
			}
			else
			{
				simpleIngredientsRecipe.put(subIngredient.Name, (SimpleIngredient)subIngredient);
			}
		}
		
		List<ObjectInstance> producedIngredientInstances =
				state.getObjectsOfTrueClass(ComplexIngredient.className);
		List<ObjectInstance> complexIngredientContents = new ArrayList<ObjectInstance>();
		for (ObjectInstance obj : producedIngredientInstances)
		{
			if (contents.contains(obj.getName()))
			{
				complexIngredientContents.add(obj);
			}
		}
		
		List<ObjectInstance> simpleIngredientInstances = 
				state.getObjectsOfTrueClass(SimpleIngredient.className);
		List<ObjectInstance> simpleIngredientContents = new ArrayList<ObjectInstance>();
		for (ObjectInstance obj : simpleIngredientInstances)
		{
			if (contents.contains(obj.getName()))
			{
				simpleIngredientContents.add(obj);
			}
		}
		
		
		if (complexIngredientsRecipe.size() != complexIngredientContents.size() ||
				simpleIngredientsRecipe.size() != simpleIngredientContents.size())
		{
			return false;
		}
		
		for (ObjectInstance simpleIngredient : simpleIngredientContents)
		{
			String ingredientName = simpleIngredient.getName();
			if (!simpleIngredientsRecipe.containsKey(ingredientName) ||
					!Recipe.isSuccess(state, simpleIngredientsRecipe.get(ingredientName), simpleIngredient))
			{
				return false;
			}
		}
		
		for (ObjectInstance producedInstance : complexIngredientContents)
		{
			Boolean exists = false;
			for (ComplexIngredient complexSubIngredient : complexIngredientsRecipe.values())
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
	
	public static Boolean isFailure(State state, Ingredient ingredient, ObjectInstance object)
	{
		if (Recipe.isSuccess(state, ingredient, object))
		{
			// If the recipe is complete, then we didn't fail. Go logic
			return false;
		}
		
		if (ingredient instanceof SimpleIngredient)
		{
			if (object.getObjectClass().name != SimpleIngredient.className)
			{
				// Object is not a simple ingredient, but the ingredient we are checking against is. FAIL!
				return true;
			}
			if (ingredient.Name != object.getName())
			{
				// They aren't even the same name. FAIL!
				return true;
			}
			if ((!ingredient.Baked && object.getDiscValForAttribute(Ingredient.attBaked) == 1) ||
				(!ingredient.Melted && object.getDiscValForAttribute(Ingredient.attMelted) == 1) ||
				(!ingredient.Mixed && object.getDiscValForAttribute(Ingredient.attMixed) == 1))
			{
				// One of baked/melted/mixed is true that shouldn't be. FAIL!
				return true;
			}
		}
		
		
		ComplexIngredient complexIngredient = (ComplexIngredient)ingredient;
		
		Map<String, ComplexIngredient> complexSubIngredients = new HashMap<String, ComplexIngredient>();
		Map<String, SimpleIngredient> simpleSubIngredients = new HashMap<String, SimpleIngredient>();
		
		for (Ingredient subIngredient : complexIngredient.Contents)
		{
			if (subIngredient instanceof SimpleIngredient)
			{
				simpleSubIngredients.put(subIngredient.Name, (SimpleIngredient)subIngredient);
			}
			else
			{
				complexSubIngredients.put(subIngredient.Name, (ComplexIngredient)subIngredient);
			}
		}
		
		Set<String> contents = object.getAllRelationalTargets(ComplexIngredient.attContains);
		if (contents.size() > complexIngredient.Contents.size())
		{
			// The number of constituent ingredient in the produced object are larger than the 
			// ingredient we are checking. Descend!
			return Recipe.isFailure(state, simpleSubIngredients, complexSubIngredients, object);
		}
		
		List<ObjectInstance> simpleIngredientInstances = state.getObjectsOfTrueClass(SimpleIngredient.className);
		List<ObjectInstance> simpleIngredientContents = new ArrayList<ObjectInstance>();
		for (ObjectInstance obj : simpleIngredientInstances)
		{
			if (contents.contains(obj.getName()))
			{
				simpleIngredientContents.add(obj);
			}
		}
		
		List<ObjectInstance> complexIngredientInstances = state.getObjectsOfTrueClass(ComplexIngredient.className);
		List<ObjectInstance> complexIngredientContents = new ArrayList<ObjectInstance>();
		for (ObjectInstance obj : complexIngredientInstances)
		{
			if (contents.contains(obj.getName()))
			{
				complexIngredientContents.add(obj);
			}
		}
		
		if (simpleIngredientContents.size() > simpleSubIngredients.size() ||
			complexIngredientContents.size() > complexSubIngredients.size())
		{
			// The number of simple/complex ingredients is greater than what we've called for. DESCEND!
			return Recipe.isFailure(state, simpleSubIngredients, complexSubIngredients, object);
		}
	
		for (ObjectInstance obj : simpleIngredientContents)
		{
			if (simpleSubIngredients.containsKey(obj.getName()) &&
				Recipe.isFailure(state, simpleSubIngredients.get(obj.getName()), obj))
			{
				// If the list of simple ingredients does contain this simple ingredient, but
				// the two simple ingredients don't match, we must descend.
				return Recipe.isFailure(state, simpleSubIngredients, complexSubIngredients, object);
			}		
		}
		
		for (ObjectInstance obj : complexIngredientContents)
		{
			Boolean found = false;
			for (Ingredient subSubIngredient : complexSubIngredients.values())
			{
				if (!Recipe.isFailure(state, subSubIngredient, obj))
				{
					found = true;
				}
			}
			if (!found)
			{
				// If we can't find this objectInstance in the complex ingredient list, we must descend.
				return Recipe.isFailure(state, simpleSubIngredients, complexSubIngredients, object);
			}
		}
		
		// No failures were explicitly found, therefore we haven't yet failed.
		return false;
	}
	
	public static Boolean isFailure(State state, Map<String, SimpleIngredient> simpleIngredients, Map<String, ComplexIngredient> complexIngredients, ObjectInstance object)
	{
		if (object.getObjectClass().name == SimpleIngredient.className)
		{
			if (simpleIngredients.containsKey(object.getName()))
			{
				SimpleIngredient simpleIngredient = simpleIngredients.get(object.getName());
				if (!Recipe.isFailure(state, simpleIngredient, object))
				{
					// The object we are checking is a simple ingredient, and it checks out. Carry on.
					return false;
				}
			}
		}
		for (ComplexIngredient complexIngredient : complexIngredients.values())
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
	
	public abstract class Ingredient {
		public static final String attBaked = "baked";
		public static final String attMelted = "melted";
		public static final String attMixed = "mixed";
		public Boolean Melted;
		public Boolean Baked;
		public Boolean Mixed;
		public String Name;
		public Ingredient(String name, Boolean melted, Boolean baked, Boolean mixed){
			this.Name = name;
			this.Melted = melted;
			this.Baked = baked;
			this.Mixed = mixed;
		}
		
		public abstract ObjectInstance getObjectInstance(ObjectClass ingredientClass);
		public abstract List<ObjectInstance> getSimpleObjectInstances(ObjectClass ingredientClass);
		
	}
	
	public class ComplexIngredient extends Ingredient {
		
		public static final String className = "produced";
		public static final String attContains = "contains";
		public List<Ingredient> Contents;
		public ComplexIngredient(String name, Boolean melted, Boolean baked, Boolean mixed, List<Ingredient> contents) {
			super(name, melted, baked, mixed);
			this.Contents = new ArrayList<Ingredient>(contents);
		}
		
		@Override
		public ObjectInstance getObjectInstance(ObjectClass complexIngredientClass)
		{
			ObjectInstance objectInstance = new ObjectInstance(complexIngredientClass, this.Name);
			objectInstance.setValue(Ingredient.attBaked, this.Baked ? 1 : 0);
			objectInstance.setValue(Ingredient.attMelted, this.Melted ? 1 : 0);
			objectInstance.setValue(Ingredient.attMixed, this.Mixed ? 1 : 0);
			for (Ingredient ingredient : this.Contents)
			{
				objectInstance.addRelationalTarget(ComplexIngredient.attContains, ingredient.Name);
			}
			return objectInstance;
			
		}

		@Override
		public List<ObjectInstance> getSimpleObjectInstances(ObjectClass simpleIngredientClass) {
			List<ObjectInstance> ingredientsInstances = new ArrayList<ObjectInstance>();
			for (Ingredient ingredient : this.Contents)
			{
				ingredientsInstances.addAll(ingredient.getSimpleObjectInstances(simpleIngredientClass));
			}
			return ingredientsInstances;
		}
	}
	
	public class SimpleIngredient extends Ingredient {
		
		public static final String className = "simple";
		public SimpleIngredient(String name, Boolean melted, Boolean baked, Boolean mixed) {
			super (name, melted, baked, mixed);
		}
		
		@Override
		public ObjectInstance getObjectInstance(ObjectClass simpleIngredientClass)
		{
			ObjectInstance objectInstance = new ObjectInstance(simpleIngredientClass, this.Name);
			objectInstance.setValue(Ingredient.attBaked, this.Baked ? 1 : 0);
			objectInstance.setValue(Ingredient.attMelted, this.Melted ? 1 : 0);
			objectInstance.setValue(Ingredient.attMixed, this.Mixed ? 1 : 0);
			return objectInstance;
		}

		@Override
		public List<ObjectInstance> getSimpleObjectInstances(ObjectClass simpleIngredientClass)
		{
			List<ObjectInstance> objectInstances = new ArrayList<ObjectInstance>();
			objectInstances.add(this.getObjectInstance(simpleIngredientClass));
			return objectInstances;
		}
	}
}
