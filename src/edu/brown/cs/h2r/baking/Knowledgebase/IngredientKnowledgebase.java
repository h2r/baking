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
import burlap.oomdp.core.Value;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Experiments.KevinsKitchen;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.PropositionalFunctions.IngredientNecessaryForRecipe;


public class IngredientKnowledgebase {
	
	private final String TRAITFILE = "IngredientTraitsFULL.txt";
	private final String COMBINATIONFILE = "IngredientCombinations.txt";
	private final String COMBINATIONTRAITFILE = "CombinationTraits.txt";
	
	private final Boolean NOTMIXED= false;
	private final Boolean NOTMELTED= false;
	private final Boolean NOTBAKED= false;
	
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
			IngredientRecipe ing = new IngredientRecipe(name, NOTMIXED, NOTMELTED, NOTBAKED);
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
	
	//TODO: Will move this out soon, here to see if it works!
	public List<ObjectInstance>getPotentialIngredientObjectInstanceList(State s, Domain domain, IngredientRecipe tlIngredient) {
		List<ObjectInstance> ingredients = new ArrayList<ObjectInstance>();
		IngredientNecessaryForRecipe necessary = new IngredientNecessaryForRecipe(AffordanceCreator.INGREDIENTPF, domain, tlIngredient);
		for (IngredientRecipe ing : getIngredientList()) {
			if (necessary.isTrue(s, new String[] {ing.getName()})) {
				ObjectClass oc = ing.isSimple() ? domain.getObjectClass(IngredientFactory.ClassNameSimple) : domain.getObjectClass(IngredientFactory.ClassNameComplex);
				ObjectInstance obj = IngredientFactory.getNewIngredientInstance(ing, ing.getName(), oc);
				ingredients.add(obj);
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
	
	// TODO: maybe make this a PF? but returns a string so IDK. If we can't
	// convey what combination is to be made then it might not be as useful
	// to have this separate so we will see if we can keep this is the mix method?
	public String canCombine(State state, ObjectInstance container) {
		Set<ObjectInstance> contains = new HashSet<>();
		for (String content : ContainerFactory.getContentNames(container)) {
			contains.add(state.getObject(content));
		}
		for (String key : this.combinationMap.keySet()) {
			ArrayList<Set<String>> possible_combinations = this.combinationMap.get(key);
			for (Set<String> necessary_traits : possible_combinations) {
				if (necessary_traits.size() == 1) {
					String[] traitArray = new String[1];
					String trait = necessary_traits.toArray(traitArray)[0];
					Boolean match = true;
					for (ObjectInstance obj : contains) {
						if (!obj.getAllRelationalTargets("traits").contains(trait)) {
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
					if ((contentArray[0].getAllRelationalTargets("traits").contains(traitArray[0])) 
							&& (contentArray[1].getAllRelationalTargets("traits").contains(traitArray[1]))) {
						return key;
					}
					if ((contentArray[0].getAllRelationalTargets("traits").contains(traitArray[1])) 
							&& (contentArray[1].getAllRelationalTargets("traits").contains(traitArray[0]))) {
						return key;
					}
				}
			}
		}
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
		ObjectInstance new_ing = IngredientFactory.getNewComplexIngredientObjectInstance(domain.getObjectClass(IngredientFactory.ClassNameComplex), toswap, false, false, false, true, "", traits, ings);
		// Make the hidden Copies
		Set<ObjectInstance> hidden_copies = new HashSet<ObjectInstance>();
		for (String name : ings) {
			ObjectInstance ob = state.getObject(name);
			hidden_copies.add(IngredientFactory.makeHiddenObjectCopy(state, domain, ob));
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
}
