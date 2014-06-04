package edu.brown.cs.h2r.baking;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;


public class KitchenIngredients {
	AbstractMap<String, IngredientRecipe> allIngredients;
	List<String> names;
	TraitKnowledgebase traits;
	public KitchenIngredients() {
		this.names = new ArrayList<String>();
		names.add("flour");
		names.add("salt");
		names.add("baking_powder");
		names.add("baking_soda");
		names.add("white_sugar");
		names.add("brown_sugar");
		names.add("eggs");
		names.add("butter");
		names.add("flour");
		names.add("cocoa");
		this.traits = new TraitKnowledgebase();
		this.allIngredients = generateIngredients();
	}
	
	private AbstractMap<String, IngredientRecipe> generateIngredients() {
		AbstractMap<String, IngredientRecipe> allIngredients = new HashMap<String, IngredientRecipe>();
		for (String name : this.names) {
			IngredientRecipe ing = new IngredientRecipe(name, false, false, false);
			ing.addTraits(traits.getTraits(name));
			allIngredients.put(name, ing);
		}
		return allIngredients;
	}
	// do better
	public List<IngredientRecipe> getAllIngredients() {
		List<IngredientRecipe> ingredients = new ArrayList<IngredientRecipe>();
		for (IngredientRecipe ing : allIngredients.values()) {
			ingredients.add(ing);
		}
		return ingredients;
	}
	
	public IngredientRecipe getIngredient(String name) {
		return allIngredients.get(name);
	}
	
	public List<ObjectInstance> getAllIngredientObjectInstances(Domain domain) {
		List<ObjectInstance> ingredients = new ArrayList<ObjectInstance>();
		for (IngredientRecipe ing : getAllIngredients()) {
			ObjectClass oc = ing.isSimple() ? domain.getObjectClass(IngredientFactory.ClassNameSimple) : domain.getObjectClass(IngredientFactory.ClassNameComplex);
			ObjectInstance obj = IngredientFactory.getNewIngredientInstance(ing, ing.getName(), oc);
			ingredients.add(obj);
		}
		return ingredients;
	}
}
