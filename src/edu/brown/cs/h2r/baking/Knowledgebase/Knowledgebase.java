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
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ToolFactory;
import edu.brown.cs.h2r.baking.Recipes.Recipe;


public class Knowledgebase {
	
	public static final String NONMELTABLE = "unsaturated";
	public static final String LUBRICANT = "lubricant";
	
	private AbstractMap<String, ArrayList<Set<String>>> combinationMap;
	private AbstractMap<String, IngredientRecipe> allIngredients;
	private AbstractMap<String, Set<String>> traitMap, toolTraitMap, combinationTraitMap;
	private AbstractMap<String, BakingInformation> toolMap;
	private BakingParser parser;
	public Knowledgebase() {
		this.parser = new BakingParser();
		this.traitMap = new HashMap<String, Set<String>>();
		this.toolTraitMap = new HashMap<String, Set<String>>();
		this.allIngredients = new HashMap<String, IngredientRecipe>();
		this.generateAllIngredients();
		this.combinationTraitMap = new HashMap<String, Set<String>>();
		this.combinationMap = new HashMap<String, ArrayList<Set<String>>>();
		this.generateCombinations();
		this.toolMap = parser.getToolMap();
	}
	
	private void generateAllIngredients() {
		for (Entry<String, BakingInformation> entry : this.parser.getIngredientMap().entrySet()) {
			String name = entry.getKey();
			IngredientRecipe ing = new IngredientRecipe(name, Recipe.NO_ATTRIBUTES);
			BakingInformation info = entry.getValue();
			List<String> traits = null;
			List<String> toolTraits = null;
			String heatingInfo = null;
			try {
				traits = info.getListOfString(BakingInformation.ingredientTraits);
				toolTraits = info.getListOfString(BakingInformation.ingredientToolTraits);
				heatingInfo = info.getString(BakingInformation.ingredientHeatingInformation);
			} catch (BakingCastException e) {
				e.printStackTrace();
			}
			if (traits != null) {
				ing.addTraits(traits);
			}
			if (toolTraits != null) {
				ing.addToolTraits(toolTraits);
			}
			if (heatingInfo != null) {
				ing.addHeatingInformation(heatingInfo);
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
	
	public void addTools(Domain domain, State state, String space) {
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
			
			if (transportable) {
				state.addObject(ToolFactory.getNewCarryingToolObjectInstance(domain, name, toolTrait, toolAttribute,
						space,includes, excludes));
			} else {
				state.addObject(ToolFactory.getNewSimpleToolObjectInstance(domain, name, toolTrait, toolAttribute, space));
			}
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
	
	public List<ObjectInstance> getRecipeObjectInstanceList(State state, Domain domain, Recipe recipe) {
		List<ObjectInstance> objs = new ArrayList<ObjectInstance>();
		for (BakingSubgoal sg : recipe.getSubgoals()) {
			IngredientRecipe ing = sg.getIngredient();
			objs.addAll(this.getPotentialIngredientObjectInstanceList(state, domain, ing));
		}
		return objs;
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
	
	// Determine whether the ingredient in the container can be swapped out (flour + liquid -> batter).
	// If a match is found, return the name of the combination found.
	public String canCombine(State state, ObjectInstance container) {
		Set<ObjectInstance> contains = new HashSet<>();
		// get contents
		Set<String> contents = ContainerFactory.getContentNames(container);
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
	public static void heatIngredient(State state, ObjectInstance container, ObjectInstance ing) {
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
		if (containsLiquid) {
			IngredientFactory.setHeatedState(ing, "boiled");
		} else {
			if (IngredientFactory.getTraits(ing).contains("liquid")) {
				IngredientFactory.setHeatedState(ing, "boiled");
				for (ObjectInstance obj : contents) {
					IngredientFactory.setHeatedState(obj, "boiled");
				}
			} else {
				String heatingInfo = IngredientFactory.getHeatingInfo(ing);
				if (heatingInfo != null) {
					IngredientFactory.setHeatedState(ing, heatingInfo);
				}
			}
		}
		IngredientFactory.heatIngredient(ing);
	}
	
	public static void heatContainer(State state, ObjectInstance container) {
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
				IngredientFactory.setHeatedState(ing, "boiled");
				IngredientFactory.heatIngredient(ing);
			}
		} else {
			for (ObjectInstance ing : contents) {
				String heatingInfo = IngredientFactory.getHeatingInfo(ing);
				if (heatingInfo != null) {
					IngredientFactory.setHeatedState(ing, heatingInfo);
				}
				IngredientFactory.heatIngredient(ing);
			}
		}
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
}
