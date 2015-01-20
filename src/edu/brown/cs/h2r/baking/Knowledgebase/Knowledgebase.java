package edu.brown.cs.h2r.baking.Knowledgebase;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import burlap.behavior.statehashing.ObjectHashFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ToolFactory;
import edu.brown.cs.h2r.baking.Recipes.Recipe;


public class Knowledgebase {
	
	private static Knowledgebase singleton = null;
	private final Domain domain;
	public static final String NONMELTABLE = "unsaturated";
	public static final String LUBRICANT = "lubricant";
	
	private AbstractMap<String, ArrayList<Set<String>>> combinationMap;
	private AbstractMap<String, IngredientRecipe> allIngredients;
	private AbstractMap<String, Set<String>> traitMap, toolTraitMap, combinationTraitMap, prepTraitMap;
	private AbstractMap<String, BakingInformation> toolMap;
	private List<Recipe> recipes;
	
	private Map<Set<IngredientRecipe>, List<IngredientRecipe>> recipeCombinationLookup;
	private BakingParser parser;
	private Knowledgebase(Domain domain) {
		this.parser = new BakingParser();
		this.traitMap = new HashMap<String, Set<String>>();
		this.toolTraitMap = new HashMap<String, Set<String>>();
		this.prepTraitMap = new HashMap<String, Set<String>>();
		this.allIngredients = new HashMap<String, IngredientRecipe>();
		this.generateAllIngredients();
		this.combinationTraitMap = new HashMap<String, Set<String>>();
		this.combinationMap = new HashMap<String, ArrayList<Set<String>>>();
		this.recipeCombinationLookup = new HashMap<Set<IngredientRecipe>, List<IngredientRecipe>>();
		this.generateCombinations();
		this.toolMap = parser.getToolMap();
		this.domain = domain;
	}
	
	public void initKnowledgebase(List<Recipe> recipes) {
		this.recipes = Collections.unmodifiableList(recipes);
		
		for (Recipe recipe : recipes) {
			this.addEntriesToMap(recipe.topLevelIngredient);
		}	
	}
	
	private void addEntriesToMap(IngredientRecipe ingredient) {
		
		Set<List<IngredientRecipe>> subIngredients = this.getPotentialFirstLevelIngredients(domain, ingredient);
		List<Set<IngredientRecipe>> combinations = this.generateAllCombinations(subIngredients);
		
		for (Set<IngredientRecipe> combination : combinations) {
			List<IngredientRecipe> list = this.recipeCombinationLookup.get(combination);
			if (list == null) {
				list = new ArrayList<IngredientRecipe>();
				this.recipeCombinationLookup.put(combination, list);
			}
			list.add(ingredient);
		}
		
		for (List<IngredientRecipe> list : subIngredients) {
			for (IngredientRecipe subIngredient : list) {
				if (!subIngredient.isSimple()) {
					this.addEntriesToMap(subIngredient);
				}
			}
		}
	}
	
	public static Knowledgebase getKnowledgebase() {
		return Knowledgebase.singleton;
	}
	
	public static Knowledgebase getKnowledgebase(Domain domain) {
		if (Knowledgebase.singleton == null) {
			Knowledgebase.singleton = new Knowledgebase(domain);
		}
		return Knowledgebase.singleton;
	}
	
	private void generateAllIngredients() {
		for (Entry<String, BakingInformation> entry : this.parser.getIngredientMap().entrySet()) {
			String name = entry.getKey();
			IngredientRecipe ing = new IngredientRecipe(name, Recipe.NO_ATTRIBUTES, null);
			BakingInformation info = entry.getValue();
			List<String> traits = null;
			List<String> toolTraits = null;
			List<String> prepTraits = null;
			String heatingInfo = null;
			try {
				traits = info.getListOfString(BakingInformation.ingredientTraits);
				toolTraits = info.getListOfString(BakingInformation.ingredientToolTraits);
				heatingInfo = info.getString(BakingInformation.ingredientHeatingInformation);
				prepTraits = info.getListOfString(BakingInformation.prepTraits);
			} catch (BakingCastException e) {
				e.printStackTrace();
			}
			if (traits != null) {
				ing.addTraits(traits);
				this.traitMap.put(name, new HashSet<String>(traits));
				
			}
			if (toolTraits != null) {
				ing.addToolTraits(toolTraits);
				this.toolTraitMap.put(name, new HashSet<String>(toolTraits));
				
			}
			if (heatingInfo != null) {
				ing.addHeatingInformation(heatingInfo);
			}
			if (prepTraits != null) {
				ing.addPrepTraits(prepTraits);
				this.prepTraitMap.put(name, new HashSet<String>(prepTraits));
			}
			
			this.allIngredients.put(name, ing);
			
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
				traits = info.getListOfString(BakingInformation.combinationTraits);
				combinations = info.getListOfList(BakingInformation.combinationPossibleCombinations);
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
	
	public List<ObjectInstance> getTools(Domain domain, String space, ObjectHashFactory hashingFactory) {
		
		List<ObjectInstance> toolsToAdd = new ArrayList<ObjectInstance>();
		for (Entry<String, BakingInformation> entry : this.toolMap.entrySet()) {
			String name = entry.getKey();
			BakingInformation info = entry.getValue();
			String toolTrait = null;
			String toolAttribute = "";
			Set<String> includes = new HashSet<String>();
			Set<String> excludes = new HashSet<String>();
			boolean transportable = false;
			try {
				toolTrait = info.getString(BakingInformation.toolTrait);
				toolAttribute = info.getString(BakingInformation.toolAttribute);
				transportable = info.getBoolean(BakingInformation.toolCanCarry);
				if (info.containsKey(BakingInformation.toolExclude)) {
					excludes = new HashSet<String>(info.getListOfString(BakingInformation.toolExclude));
				}
				if (info.containsKey(BakingInformation.toolInclude)) {
					includes = new HashSet<String>(info.getListOfString(BakingInformation.toolInclude));
				}
			} catch (BakingCastException e) {
				e.printStackTrace();
			}
			
			ObjectInstance tool;
			if (transportable) {
				
				tool = ToolFactory.getNewCarryingToolObjectInstance(domain, name, toolTrait, toolAttribute,
						space,includes, excludes, hashingFactory);
			} else {
				tool = ToolFactory.getNewSimpleToolObjectInstance(domain, name, toolTrait, toolAttribute, space, hashingFactory);
			}
			toolsToAdd.add(tool);
		}
		
		return toolsToAdd;
	}
	
	public List<IngredientRecipe> getIngredientList() {
		List<IngredientRecipe> ingredients = new ArrayList<IngredientRecipe>();
		ingredients.addAll(allIngredients.values());
		return ingredients;
	}
	
	public IngredientRecipe getIngredient(String name) {
		IngredientRecipe ingredient = this.allIngredients.get(name);
		if (ingredient == null) {
			return null;
		}
		return new IngredientRecipe(ingredient);
	}
	
	public List<ObjectInstance> getAllIngredientObjectInstanceList(Domain domain, ObjectHashFactory hashingFactory) {
		List<ObjectInstance> ingredientObjects = new ArrayList<ObjectInstance>();
		List<IngredientRecipe> ingredients = this.getIngredientList();
		for (IngredientRecipe ing : ingredients) {
			ObjectClass oc = ing.isSimple() ? 
					domain.getObjectClass(IngredientFactory.ClassNameSimple) : 
						domain.getObjectClass(IngredientFactory.ClassNameComplex);
			ObjectInstance obj = IngredientFactory.getNewIngredientInstance(ing, ing.getFullName(), oc, hashingFactory);
			ingredientObjects.add(obj);
		}
		return ingredientObjects;
	}
	
	public List<ObjectInstance> getRecipeObjectInstanceList(Domain domain, ObjectHashFactory hashingFactory, Recipe recipe) {
		List<ObjectInstance> objs = new ArrayList<ObjectInstance>();
		for (BakingSubgoal sg : recipe.getSubgoals()) {
			IngredientRecipe ing = sg.getIngredient();
			objs.addAll(this.getPotentialIngredientObjectInstanceList(domain, ing, hashingFactory));
		}
		return objs;
	}
	public List<ObjectInstance> getPotentialIngredientObjectInstanceList(Domain domain, IngredientRecipe tlIngredient, ObjectHashFactory hashingFactory) {
		List<ObjectInstance> ingredientObjects = new ArrayList<ObjectInstance>();
		List<IngredientRecipe> ingredients = this.getPotentialIngredientList(domain, tlIngredient);
		for (IngredientRecipe ing : ingredients) {
			ObjectClass oc = ing.isSimple() ? domain.getObjectClass(IngredientFactory.ClassNameSimple) : domain.getObjectClass(IngredientFactory.ClassNameComplex);
			ObjectInstance obj = IngredientFactory.getNewIngredientInstance(ing, ing.getFullName(), oc, hashingFactory);
			obj = IngredientFactory.clearBooleanAttributes(obj);
			obj = IngredientFactory.clearToolAttributes(obj);
			ingredientObjects.add(obj);
		}
		return ingredientObjects;
	}
	
	public List<IngredientRecipe> getPotentialIngredientList(Domain domain, IngredientRecipe tlIngredient) {
		Map<String, IngredientRecipe> ingredients = new HashMap<String, IngredientRecipe>();
		
		this.addPotentialIngredientList(domain, tlIngredient, ingredients);
		
		return new ArrayList<IngredientRecipe>(ingredients.values());
	}
	
	public Set<List<IngredientRecipe>> getPotentialFirstLevelIngredients(Domain domain, IngredientRecipe ingredient) {
		Set<List<IngredientRecipe>> matchedIngredients = this.findIngredientsMatchingIngredientTraits(ingredient);
		List<IngredientRecipe> contents = ingredient.getContents();
		for (IngredientRecipe subIngredient : contents) {
			matchedIngredients.add(Arrays.asList(subIngredient));
		}
		return matchedIngredients;
	}
	
	public void addPotentialIngredientList(Domain domain, IngredientRecipe tlIngredient,
			Map<String, IngredientRecipe> ingredients) {
		Set<List<IngredientRecipe>> matchedIngredients = this.findIngredientsMatchingIngredientTraits(tlIngredient);
		for (List<IngredientRecipe> list : matchedIngredients) {
			for (IngredientRecipe ingredient : list) {
				ingredients.put(ingredient.getFullName(), ingredient);
			}
		}
		
		List<IngredientRecipe> contents = tlIngredient.getContents();
		for (IngredientRecipe ingredient : contents) {
			if (ingredient.isSimple()) {
				IngredientRecipe foundIngredient = ingredients.get(ingredient.getFullName());
				if (foundIngredient != null) {
					foundIngredient.incrementUseCount();
				} else {
					ingredients.put(ingredient.getFullName(), ingredient);
				}
			} else {
				this.addPotentialIngredientList(domain, ingredient, ingredients);
			}
		}
		
	}
	
	public Set<List<IngredientRecipe>> findIngredientsMatchingIngredientTraits(IngredientRecipe ingredient) {
		Set<List<IngredientRecipe>> ingredientMatches = new HashSet<List<IngredientRecipe>>();
		Set<String> necessaryTraits = ingredient.getNecessaryTraits().keySet();
		for (String trait : necessaryTraits) {
			List<IngredientRecipe> traitMatches = this.findIngredientWithTraits(trait);
			List<IngredientRecipe> correctedMatches = new ArrayList<IngredientRecipe>(traitMatches.size());
			for (IngredientRecipe traitMatch : traitMatches) {
				IngredientRecipe corrected = traitMatch.getCopyWithNewAttributes(traitMatch.getAttributeNumber());
				correctedMatches.add(corrected);
			}
			ingredientMatches.add(correctedMatches);
		}
		
		return ingredientMatches;
	}

	private List<IngredientRecipe> findIngredientWithTraits(String trait) {
		
		List<IngredientRecipe> ingredients = new ArrayList<IngredientRecipe>();
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
	
	// Determine whether the ingredient in the container can be swapped out (flour + liquid -> batter).
	// If a match is found, return the name of the combination found.
	public String canCombine(State state, ObjectInstance container) {
		Set<ObjectInstance> contains = new HashSet<>();
		// get contents
		Set<String> contents = ContainerFactory.getContentNames(container);
		if (contents.size() == 0) {
			return "";
		}
		for (String content : contents) {
			contains.add(state.getObject(content));
		}
		int contentSize = contents.size();
		
		// Check to see if our contents exist in this map and could result in a combination.
		// Said map links possible combinations of types of ingredients that could be combined
		// to create a complex ingredient. These are very general possible combinations.
		for (Entry<String, ArrayList<Set<String>>> entry : this.combinationMap.entrySet()) {
			String key = entry.getKey();
			ArrayList<Set<String>> possibleCombinations = entry.getValue();
			for (Set<String> necessaryTraits : possibleCombinations) {
				String name = this.combinationPossible(contains, key, necessaryTraits);
				if (name != null) {
					return name;
				}
			}
		}
		// no combination found, return an empty string.
		return "";
	}
	
	// Any ingredient added that is heated will get the heated attribute. Also, depending on the container
	// it will get a tool attribute added that shows what effect heating the object had. For example,
	// heating liquids will (or heating with a loquid) will give the boiled attribute, or a meltable 
	// will be melted (if not heated with other liquids).
	public static State heatIngredient(State state, ObjectInstance container, ObjectInstance ing) {
		boolean containsLiquid = false;
		Set<ObjectInstance> contents = new HashSet<ObjectInstance>();
		for (String name : ContainerFactory.getContentNames(container)) {
			ObjectInstance obj = state.getObject(name);
			contents.add(obj);
			if (IngredientFactory.getTraits(obj).contains("liquid")) {
				containsLiquid = true;
				break;
			}
		}
		
		ObjectInstance newIngredient = ing.copy();
		if (containsLiquid) {
			newIngredient = IngredientFactory.changeHeatedState(newIngredient, "boiled");
		} else {
			if (IngredientFactory.getTraits(newIngredient).contains("liquid")) {
				newIngredient = IngredientFactory.changeHeatedState(newIngredient, "boiled");
				
				ObjectInstance newContentsIng;
				for (ObjectInstance obj : contents) {
					newContentsIng = IngredientFactory.changeHeatedState(obj, "boiled");
					state = state.replaceObject(obj, newContentsIng);
				}
			} else {
				String heatingInfo = IngredientFactory.getHeatingInfo(newIngredient);
				if (heatingInfo != null) {
					newIngredient = IngredientFactory.changeHeatedState(newIngredient, heatingInfo);
				}
			}
		}
		newIngredient = IngredientFactory.heatIngredient(newIngredient);
		state = state.replaceObject(ing, newIngredient);
		return state;
	}
	
	public static State heatContainer(State state, ObjectInstance container) {
		boolean containsLiquid = false;
		Set<ObjectInstance> contents = new HashSet<ObjectInstance>();
		
		for (String name : ContainerFactory.getContentNames(container)) {
			ObjectInstance obj = state.getObject(name);
			contents.add(obj);
			if (IngredientFactory.getTraits(obj).contains("liquid")) {
				containsLiquid = true;
			}
		}
		if (containsLiquid) {
			for (ObjectInstance ing : contents) {
				ObjectInstance newIng = IngredientFactory.changeHeatedState(ing, "boiled");
				newIng = IngredientFactory.heatIngredient(newIng);
				state = state.replaceObject(ing, newIng);
			}
		} else {
			for (ObjectInstance ing : contents) {
				String heatingInfo = IngredientFactory.getHeatingInfo(ing);
				ObjectInstance newIng = ing;
				if (heatingInfo != null) {
					newIng = IngredientFactory.changeHeatedState(ing, heatingInfo);
				}
				newIng = IngredientFactory.heatIngredient(newIng);
				state = state.replaceObject(ing, newIng);
			}
		}
		return state;
	}
	
	// For a given combination (whose name is key), then check given the contents of our bowl, those can be mixed
	// to create a combination. The parameters of said combination are given by necessaryTraits, which states
	// what kind of ingredients (flours, oils sugars, nuts) our combination needs.
	public String combinationPossible(Set<ObjectInstance> contains, String key, Set<String> necessaryTraits) {
		// If there's only one necessary trait, then this combination can be treated like
		// a "collection" (that is, a collection of dry ingredients, or a collection of wet
		// ingredients).
		if (necessaryTraits.size() == 1) {
			String[] traitArray = new String[1];
			String trait = necessaryTraits.toArray(traitArray)[0];
			Boolean match = true;
			
			// make sure all objects have the necessary trait
			for (ObjectInstance obj : contains) {
				if (!IngredientFactory.getTraits(obj).contains(trait)) {
					match = false;
				}
			}
			if (match) {
				return key;
			}
			
		} else {
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
		return null;
	}
	
	public List<IngredientRecipe> checkCombination(Collection<ObjectInstance> objects, State state) {
		
		// Set of possible ingredient matches
		Set<List<IngredientRecipe>> ingredientPossibilities = new HashSet<List<IngredientRecipe>>();
		
		// Iterate through all objects and find the list of possible matches each object can be
		for (ObjectInstance object : objects) {
			List<IngredientRecipe> possibleIngredients = this.getMatchingIngredientRecipes(object, state);
			ingredientPossibilities.add(possibleIngredients);
		}
		
		// List of all combinations of ingredient matches
		List<Set<IngredientRecipe>> allCombinations = this.generateAllCombinations(ingredientPossibilities);
		
		// List of possible generated combinations
		List<IngredientRecipe> generatedCombinations = new ArrayList<IngredientRecipe>();
		
		// Iterate through all combinations, and check if those combinations generate anything
		for (Set<IngredientRecipe> combination : allCombinations) {
			List<IngredientRecipe> generatedCombos = this.recipeCombinationLookup.get(combination);
			if (generatedCombos != null) {
				generatedCombinations.addAll(generatedCombos);
			}
		}
		
		if (generatedCombinations.isEmpty()) {
			Set<IngredientRecipe> allItems = new HashSet<IngredientRecipe>();
			for (Set<IngredientRecipe> combination : allCombinations) {
				allItems.addAll(combination);
			}
			
			Set<IngredientRecipe> allEntries = new HashSet<IngredientRecipe>();
			for (Set<IngredientRecipe> combination : this.recipeCombinationLookup.keySet()) {
				allEntries.addAll(combination);
			}
			
			for (IngredientRecipe lhs : allItems) {
				for (IngredientRecipe rhs : allEntries) {
					if (lhs.equals(rhs) && lhs.hashCode() != rhs.hashCode()) {
						System.err.println("Inconsistent hashing");
						System.err.println("lhs: " + lhs.getFullName());
						System.err.println("rhs: " + rhs.getFullName());
						
					} else if (!lhs.equals(rhs) && lhs.getFullName().equals(rhs.getFullName())) {
						//System.err.println("Suspicious comparison");
						//boolean result = lhs.equals(rhs);
					}
				}
			}
		}
		
		// Return the list of generated items. Ideally this should be only one item, but if it makes
		// more than one, then the logic calling this method should handle that.
		return generatedCombinations;
	}
	
	// Finds all possible matches for this ObjectInstance, whether it is a named object or a arbitrary object
	private List<IngredientRecipe> getMatchingIngredientRecipes(ObjectInstance object, State state) {
		Set<IngredientRecipe> ingredientMatches = new HashSet<IngredientRecipe>();
		
		//If there is one that matches by name
		IngredientRecipe matchedByName = this.allIngredients.get(object.getName());
		if (matchedByName != null) {
			ingredientMatches.add(matchedByName);
		}

		//Otherwise need to get potential matches
		for (IngredientRecipe ingredient : this.allIngredients.values()) {
			if (ingredient.isMatching(object, state)) {
				ingredientMatches.add(ingredient);
			}
		}
		
		
		// Check recipes for complex ingredients
		for (Recipe recipe : this.recipes) { 
			this.getMatchingIngredientRecipes(object, state, recipe.topLevelIngredient, ingredientMatches);
		}
		
		return new ArrayList<IngredientRecipe>(ingredientMatches);
	}
	
	// Finds possible matches, recursive edition
	private void getMatchingIngredientRecipes(ObjectInstance object, State state, IngredientRecipe ingredient, Set<IngredientRecipe> matching) {
		if (ingredient.isMatching(object, state) || ingredient.isTraitMatching(object, state)) {
			matching.add(ingredient);
		} else if (ingredient.isSimple()){
			return;
		} else {
			for (IngredientRecipe subIngredient : ingredient.getContents()) {
				this.getMatchingIngredientRecipes(object, state, subIngredient, matching);
			}
		}
	}
	
	// This generates all possible combinations given the different possibilities of each ingredient recipe
	private List<Set<IngredientRecipe>> generateAllCombinations(Set<List<IngredientRecipe>> possibleCombinations) {
		// The list of generatedCombinations
		List<Set<IngredientRecipe>> generatedCombinations = new ArrayList<Set<IngredientRecipe>>();
		Set<IngredientRecipe> initialSet = new HashSet<IngredientRecipe>();
		generatedCombinations.add(initialSet);
		
		// A list of lists, that are copied for each new ingredient list with each successive ingredient
		List<List<Set<IngredientRecipe>>> copiedCombinations = new ArrayList<List<Set<IngredientRecipe>>>();
		
		// For each list in the possible combinations, we need to tack on all their possible combos
		for (List<IngredientRecipe> list : possibleCombinations) {
			copiedCombinations.clear();
			
			// Clear previous list, and fill this one with n copies of the current generatedCombinations
			for (IngredientRecipe ingredient : list) {
				List<Set<IngredientRecipe>> copiedCombination = new ArrayList<Set<IngredientRecipe>>(generatedCombinations.size());
				for (Set<IngredientRecipe> set : generatedCombinations) {
					Set<IngredientRecipe> copied = new HashSet<IngredientRecipe>(set);
					copied.add(ingredient);
					copiedCombination.add(copied);
				}
				copiedCombinations.add(copiedCombination);
			}
			
			// Clear the current generated combinations
			generatedCombinations.clear();
			
			// Flatten the copiedCombinations into the generatedCombinations
			for (List<Set<IngredientRecipe>> combination : copiedCombinations) {
				generatedCombinations.addAll(combination);
			}
		}
		
		return generatedCombinations;
	}
	
}
