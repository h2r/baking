package edu.brown.cs.h2r.baking.Knowledgebase;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.PropositionalFunctions.IngredientNecessaryForRecipe;
import edu.brown.cs.h2r.baking.Recipes.Recipe;


public class IngredientKnowledgebase {
	
	public static final String NONMELTABLE = "unsaturated";
	
	private final String TRAITFILE = "IngredientTraits.txt";
	private final String COMBINATIONFILE = "FakeCombinations.txt";
	private final String COMBINATIONTRAITFILE = "CombinationTraits.txt";
	
	private AbstractMap<String, Set<String>> traitMap;
	private AbstractMap<String, Set<String>> combinationTraitMap;
	private AbstractMap<String, Set<String>> allTraits;
	private AbstractMap<String, ArrayList<Set<String>>> combinationMap;
	private AbstractMap<String, IngredientRecipe> allIngredients;
	public IngredientKnowledgebase() {
		this.traitMap = new TraitParser(TRAITFILE).getMap();
		this.combinationTraitMap = new TraitParser(COMBINATIONTRAITFILE).getMap();
		this.allTraits = generateAllTraitMap();
		this.combinationMap = new CombinationParser(COMBINATIONFILE).getMap();
		this.allIngredients = generateAllIngredients();
	}
	
	private AbstractMap<String, IngredientRecipe> generateAllIngredients() {
		AbstractMap<String, IngredientRecipe> allIngredients = new HashMap<String, IngredientRecipe>();
		for (String name : this.traitMap.keySet()) {
			IngredientRecipe ing = new IngredientRecipe(name, Recipe.NOT_MIXED, Recipe.NOT_MELTED, Recipe.NOT_BAKED, Recipe.NOT_PEELED);
			ing.addTraits(traitMap.get(name));
			allIngredients.put(name, ing);
		}
		return allIngredients;
	}
	
	public List<IngredientRecipe> getIngredientList() {
		List<IngredientRecipe> ingredients = new ArrayList<IngredientRecipe>();
		for (IngredientRecipe ing : allIngredients.values()) {
			ingredients.add(ing);
		}
		return ingredients;
	}
	
	public IngredientRecipe getIngredient(String name) {
		return this.allIngredients.get(name);
	}
	
	public List<ObjectInstance> getAllIngredientObjectInstanceList(Domain domain) {
		List<ObjectInstance> ingredients = new ArrayList<ObjectInstance>();
		for (IngredientRecipe ing : getIngredientList()) {
			ObjectClass oc = ing.isSimple() ? domain.getObjectClass(IngredientFactory.ClassNameSimple) : domain.getObjectClass(IngredientFactory.ClassNameComplex);
			ObjectInstance obj = IngredientFactory.getNewIngredientInstance(ing, ing.getName(), oc);
			ingredients.add(obj);
		}
		return ingredients;
	}
	
	public List<ObjectInstance>getPotentialIngredientObjectInstanceList(State s, Domain domain, IngredientRecipe tlIngredient) {
		List<ObjectInstance> ingredients = new ArrayList<ObjectInstance>();
		for (IngredientRecipe ing : getPotentialIngredientList(s, domain, tlIngredient)) {
			ObjectClass oc = ing.isSimple() ? domain.getObjectClass(IngredientFactory.ClassNameSimple) : domain.getObjectClass(IngredientFactory.ClassNameComplex);
			ObjectInstance obj = IngredientFactory.getNewIngredientInstance(ing, ing.getName(), oc);
			IngredientFactory.clearBooleanAttributes(obj);
			ingredients.add(obj);
		}
		return ingredients;
	}
	
	
	public List<IngredientRecipe> getPotentialIngredientList(State s, Domain domain, IngredientRecipe tlIngredient) {
		List<IngredientRecipe> ingredients = new ArrayList<IngredientRecipe>();
		for (String trait : tlIngredient.getNecessaryTraits().keySet()) {
			for (IngredientRecipe ing : this.allIngredients.values()) {
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
		for (IngredientRecipe ingredient : tlIngredient.getContents()) {
			if (ingredient.isSimple()) {
				if (ingredients.contains(ingredient)) {
					IngredientRecipe ing = ingredients.get(ingredients.indexOf(ingredient));
					ing.setUseCount(ing.getUseCount()+1);
				} else {
					ingredients.add(ingredient);
				}
			} else {
				List<IngredientRecipe> toAdd = getPotentialIngredientList(s, domain, ingredient);
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
	private AbstractMap<String,Set<String>> generateAllTraitMap() {
		AbstractMap<String, Set<String>> allTraits = new HashMap<String, Set<String>>();
		allTraits.putAll(this.traitMap);
		allTraits.putAll(combinationTraitMap);
		return allTraits;
	}
	public AbstractMap<String, Set<String>> getTraitMap() {
		return this.allTraits;
	}
	
	public Set<String> getTraits(String ingredient) {
		if (this.traitMap.containsKey(ingredient)) {
			return this.traitMap.get(ingredient);
		}
		if (this.combinationTraitMap.containsKey(ingredient)) {
			return this.combinationTraitMap.get(ingredient);
		}
		return new TreeSet<String>();
	}
	
	// TODO: Update/fix logic when trying to mix 3+ ingredients? Finding all permutations or something!
	// Determine whether the ingredient in the container can be swapped out (flour + liquid -> flour).
	// If a match is found, return the name of the combination found.
	public String canCombine(State state, ObjectInstance container) {
		Set<ObjectInstance> contains = new HashSet<>();
		// get contents
		for (String content : ContainerFactory.getContentNames(container)) {
			contains.add(state.getObject(content));
		}
		for (String key : this.combinationMap.keySet()) {
			ArrayList<Set<String>> possible_combinations = this.combinationMap.get(key);
			for (Set<String> necessary_traits : possible_combinations) {
				// If there's only one necessary trait, then this combination can be treated like
				// a "collection" (that is, a collection of dry ingredients, or a collection of wet
				// ingredients).
				if (necessary_traits.size() == 1) {
					String[] traitArray = new String[1];
					String trait = necessary_traits.toArray(traitArray)[0];
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
					String[] traitArray = new String[necessary_traits.size()];
					necessary_traits.toArray(traitArray);
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
	//TODO: Find a better place for this method -- totes. Mix method should actually work I reckon?
	public void combineIngredients(State state, Domain domain, IngredientRecipe recipe, ObjectInstance container, String toswap) {
		Set<String> traits = new TreeSet<String>();
		//get the actual traits from the trait thing
		for (String trait : recipe.getTraits()) {
			traits.add(trait);
		}
		Set<String> ings = ContainerFactory.getContentNames(container);
		ObjectInstance new_ing = IngredientFactory.getNewComplexIngredientObjectInstance(domain.getObjectClass(IngredientFactory.ClassNameComplex), toswap, Recipe.NOT_MIXED, Recipe.NOT_MELTED, Recipe.NOT_BAKED, Recipe.NOT_PEELED, true, "", traits, ings);
		// Make the hidden Copies
		Set<ObjectInstance> hidden_copies = new HashSet<ObjectInstance>();
		for (String name : ings) {
			ObjectInstance ob = state.getObject(name);
			if (!IngredientFactory.isSimple(ob)) {
				hidden_copies.add(IngredientFactory.makeHiddenObjectCopy(state, domain, ob));
			}
		}
		ContainerFactory.removeContents(container);
		for (String name : ings) {
			state.removeObject(state.getObject(name));
		}
		for (ObjectInstance ob : hidden_copies) {
			state.addObject(ob);
		}
		ContainerFactory.addIngredient(container, toswap);
		IngredientFactory.changeIngredientContainer(new_ing, container.getName());
		state.addObject(new_ing);
	}
	
	public void newCombinationMap(String filename) {
	this.combinationMap = new CombinationParser(filename).getMap();
	}
}
