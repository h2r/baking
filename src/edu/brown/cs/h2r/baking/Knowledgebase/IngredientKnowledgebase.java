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
	
	private AbstractMap<String, ArrayList<Set<String>>> combinationMap;
	private AbstractMap<String, IngredientRecipe> allIngredients;
	private AbstractMap<String, Set<String>> traitMap, toolTraitMap, combinationTraitMap;
	private BakingParser parser;
	public IngredientKnowledgebase() {
		this.parser = new BakingParser();
		this.traitMap = new HashMap<String, Set<String>>();
		this.toolTraitMap = new HashMap<String, Set<String>>();
		this.allIngredients = new HashMap<String, IngredientRecipe>();
		this.generateAllIngredients();
		this.combinationTraitMap = new HashMap<String, Set<String>>();
		this.combinationMap = new HashMap<String, ArrayList<Set<String>>>();
		this.generateCombinations();
	}
	
	private void generateAllIngredients() {
		for (Entry<String, BakingInformation> entry : this.parser.getIngredientMap().entrySet()) {
			String name = entry.getKey();
			IngredientRecipe ing = new IngredientRecipe(name, Recipe.NO_ATTRIBUTES);
			BakingInformation info = entry.getValue();
			List<String> traits = null;
			List<String> toolTraits = null;
			try {
				traits = info.getList(BakingInformation.ingredientTraits);
				toolTraits = info.getList(BakingInformation.ingredientToolTraits);
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
	
	private void generateCombinations() {
		for (Entry<String, BakingInformation> entry : this.parser.getCombinationMap().entrySet()) {
			String name = entry.getKey();
			//IngredientRecipe ing = new IngredientRecipe(name, Recipe.NO_ATTRIBUTES);
			BakingInformation info = entry.getValue();
			List<String> traits = null;
			List<List<String>> combinations = null;
			try {
				traits = info.getList(BakingInformation.combinationTraits);
				combinations = info.getList(BakingInformation.combinationPossibleCombinations);
			} catch (BakingCastException e) {
				e.printStackTrace();
			}
			this.combinationTraitMap.put(name, new HashSet<String>(traits));
			ArrayList<Set<String>> realCombinations = new ArrayList<Set<String>>();
			for (List<String> combination : combinations) {
				realCombinations.add(new HashSet<String>(combination));
			}
			this.combinationMap.put(name, realCombinations);
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
		if (this.combinationTraitMap.containsKey(ingredient)) {
			return this.combinationTraitMap.get(ingredient);
		}
		return new HashSet<String>();
	}
	
	// Determine whether the ingredient in the container can be swapped out (flour + liquid -> flour).
	// If a match is found, return the name of the combination found.
	public String canCombine(State state, ObjectInstance container) {
		Set<ObjectInstance> contains = new HashSet<>();
		// get contents
		Set<String> contents = ContainerFactory.getContentNames(container);
		for (String content : contents) {
			contains.add(state.getObject(content));
		}
		int contentSize = contents.size();
		for (Entry<String, ArrayList<Set<String>>> entry : this.combinationMap.entrySet()) {
			String key = entry.getKey();
			ArrayList<Set<String>> possibleCombinations = entry.getValue();
			for (Set<String> necessaryTraits : possibleCombinations) {
				// If there's only one necessary trait, then this combination can be treated like
				// a "collection" (that is, a collection of dry ingredients, or a collection of wet
				// ingredients).
				if (necessaryTraits.size() == 1) {
					if (contentSize != 1) {
						continue;
					}
					String[] traitArray = new String[1];
					String trait = necessaryTraits.toArray(traitArray)[0];
					Boolean match = true;
					for (ObjectInstance obj : contains) {
						if (!IngredientFactory.getTraits(obj).contains(trait)) {
							match = false;
						}
					}
					if (match) {
						return key;
					}
					
				} else {
					if (contentSize != 2) {
						continue;
					}
					String[] traitArray = new String[necessaryTraits.size()];
					necessaryTraits.toArray(traitArray);
					ObjectInstance[] contentArray = new ObjectInstance[contains.size()];
					contains.toArray(contentArray);
					// If the combination has two traits (flour + liquid), then check that either ingredient
					// 1 has trait 1 and ingredient 2 has trait 2 or vice versa!
					if ((IngredientFactory.getTraits(contentArray[0]).contains(traitArray[0])) 
							&& (IngredientFactory.getTraits(contentArray[1]).contains(traitArray[1]))) {
						return key;
					}
					if ((IngredientFactory.getTraits(contentArray[0]).contains(traitArray[1])) 
							&& (IngredientFactory.getTraits(contentArray[1]).contains(traitArray[0]))) {
						return key;
					}
				}
			}
		}
		// no combination found, return an empty string.
		return "";
	}
	
	public void newCombinationMap(String filename) {
		this.combinationMap = new CombinationParser(filename).getMap();
	}
}
