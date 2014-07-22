package edu.brown.cs.h2r.baking.Knowledgebase;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.Recipes.Recipe;


public class IngredientKnowledgebase {
	
	public static final String NONMELTABLE = "unsaturated";
	public static final String LUBRICANT = "lubricant";
	
	private AbstractMap<String, IngredientRecipe> allIngredients;
	private AbstractMap<String, Set<String>> traitMap, toolTraitMap;
	private BakingParser parser;
	public IngredientKnowledgebase() {
		this.parser = new BakingParser();
		this.traitMap = new HashMap<String, Set<String>>();
		this.toolTraitMap = new HashMap<String, Set<String>>();
		this.allIngredients = new HashMap<String, IngredientRecipe>();
		this.generateAllIngredients();
	}
	
	private void generateAllIngredients() {
		for (Entry<String, BakingInformation> entry : this.parser.getIngredientMap().entrySet()) {
			String name = entry.getKey();
			IngredientRecipe ing = new IngredientRecipe(name, Recipe.NO_ATTRIBUTES);
			BakingInformation info = entry.getValue();
			List<String> traits = null;
			List<String> toolTraits = null;
			try {
				traits = info.getListOfString(BakingInformation.ingredientTraits);
				toolTraits = info.getListOfString(BakingInformation.ingredientToolTraits);
			} catch (BakingCastException e) {
				e.printStackTrace();
			}
			if (traits != null) {
				ing.addTraits(traits);
			}
			if (toolTraits != null) {
				ing.addToolTraits(toolTraits);
			}
			this.allIngredients.put(name, ing);
			this.traitMap.put(name, new HashSet<String>(traits));
			this.toolTraitMap.put(name, new HashSet<String>(toolTraits));
		}
	}
	
	public List<IngredientRecipe> getIngredientList() {
		List<IngredientRecipe> ingredients = new ArrayList<IngredientRecipe>();
		ingredients.addAll(allIngredients.values());
		return ingredients;
	}
	
	public IngredientRecipe getIngredient(String name) {
		return this.allIngredients.get(name);
	}
	
	public List<ObjectInstance> getAllIngredientObjectInstanceList(Domain domain) {
		List<ObjectInstance> ingredientObjects = new ArrayList<ObjectInstance>();
		List<IngredientRecipe> ingredients = this.getIngredientList();
		for (IngredientRecipe ing : ingredients) {
			ObjectClass oc = ing.isSimple() ? domain.getObjectClass(IngredientFactory.ClassNameSimple) : domain.getObjectClass(IngredientFactory.ClassNameComplex);
			ObjectInstance obj = IngredientFactory.getNewIngredientInstance(ing, ing.getName(), oc);
			ingredientObjects.add(obj);
		}
		return ingredientObjects;
	}
	
	public List<ObjectInstance> getPotentialIngredientObjectInstanceList(State state, Domain domain, IngredientRecipe tlIngredient) {
		List<ObjectInstance> ingredientObjects = new ArrayList<ObjectInstance>();
		List<IngredientRecipe> ingredients = this.getPotentialIngredientList(state, domain, tlIngredient);
		for (IngredientRecipe ing : ingredients) {
			ObjectClass oc = ing.isSimple() ? domain.getObjectClass(IngredientFactory.ClassNameSimple) : domain.getObjectClass(IngredientFactory.ClassNameComplex);
			ObjectInstance obj = IngredientFactory.getNewIngredientInstance(ing, ing.getName(), oc);
			IngredientFactory.clearBooleanAttributes(obj);
			IngredientFactory.clearToolAttributes(obj);
			ingredientObjects.add(obj);
		}
		return ingredientObjects;
	}
	
	
	public List<IngredientRecipe> getPotentialIngredientList(State state, Domain domain, IngredientRecipe tlIngredient) {
		List<IngredientRecipe> ingredients = new ArrayList<IngredientRecipe>();
		Collection<IngredientRecipe> allIngredients = this.allIngredients.values();
		Set<String> necessaryTraits = tlIngredient.getNecessaryTraits().keySet();
		for (String trait : necessaryTraits) {
			for (IngredientRecipe ing : allIngredients) {
				if (ing.getTraits().contains(trait)) {
					if (ingredients.contains(ing)) {
						IngredientRecipe i = ingredients.get(ingredients.indexOf(ing));
						i.setUseCount(i.getUseCount()+1);
					} else {
						ingredients.add(ing);
					}
				}
			}
		}
		List<IngredientRecipe> contents = tlIngredient.getContents();
		for (IngredientRecipe ingredient : contents) {
			if (ingredient.isSimple()) {
				if (ingredients.contains(ingredient)) {
					IngredientRecipe ing = ingredients.get(ingredients.indexOf(ingredient));
					ing.setUseCount(ing.getUseCount()+1);
				} else {
					ingredients.add(ingredient);
				}
			} else {
				List<IngredientRecipe> toAdd = getPotentialIngredientList(state, domain, ingredient);
				for (IngredientRecipe i : toAdd) {
					if (ingredients.contains(i)) {
						IngredientRecipe ing = ingredients.get(ingredients.indexOf(i));
						ing.setUseCount(ing.getUseCount()+i.getUseCount());
					} else {
						ingredients.add(i);
					}
				}
			}
		}
		return ingredients;
	}
	
	public Set<String> getTraits(String ingredient) {
		if (this.traitMap.containsKey(ingredient)) {
			return this.traitMap.get(ingredient);
		}
		return new HashSet<String>();
	}
}
