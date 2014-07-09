package edu.brown.cs.h2r.baking;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

import burlap.oomdp.core.ObjectInstance;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.Recipes.Recipe;


public class IngredientRecipe {
	
	private Boolean mixed;
	private Boolean melted;
	private Boolean baked;
	private Set<String> traits;
	private Set<String> toolTraits;
	private Set<String> toolAttributes;
	private String name;
	private Boolean swapped;
	private List<IngredientRecipe> contents;
	private int useCount;
	private AbstractMap<String, IngredientRecipe> necessaryTraits;
	
	public IngredientRecipe(String name, int attributes) {
		this.name = name;
		this.setAttributes(attributes);
		this.swapped = false;
		this.traits = new TreeSet<String>();
		this.useCount = 1;
		this.contents = null;
		this.traits = new TreeSet<String>();
		this.useCount = 1;
		this.necessaryTraits = null;
		this.toolTraits = new TreeSet<String>();
		this.toolAttributes = new TreeSet<String>();
	}
	
	public IngredientRecipe(String name, int attributes, Boolean swapped, List<IngredientRecipe> contents) {
		this.name = name;
		this.setAttributes(attributes);
		this.contents = contents;
		this.traits = new TreeSet<String>();
		this.swapped = swapped;
		this.necessaryTraits = new HashMap<String, IngredientRecipe>();
		this.useCount = 1;
		this.toolTraits = new TreeSet<String>();
		this.toolAttributes = new TreeSet<String>();
	}
	
	public Boolean isSimple() {
		if (this.necessaryTraits == null || this.necessaryTraits.size() == 0) {
			return this.contents == null || this.contents.size() == 0;
		}
		return false;
	}
	
	public Boolean getMixed () {
		return this.mixed;
	}
	
	public Boolean getMelted () {
		return this.melted;
	}
	
	public void setMelted() {
		this.melted = true;
	}
	
	public Boolean getBaked () {
		return this.baked;
	}
	
	public void setBaked() {
		this.baked = true;
	}
		
	public String getName() {
		return this.name;
	}
	
	public Set<String> getToolTraits() {
		return this.toolTraits;
	}
	
	public Set<String> getToolAttributes() {
		return this.toolAttributes;
	}
	
	public Boolean getSwapped() {
		if (this.isSimple()) {
			return false;
		}
		return this.swapped;
	}
	
	public void setSwapped() {
		this.swapped = true;
	}
	
	public int getUseCount() {
		return this.useCount;
	}
	
	public void setUseCount(int count) {
		this.useCount = count;
	}
	
	public void addToolTraits(Set<String> traits) {
		this.toolTraits.addAll(traits);
	}
	
	public void addToolAttribute(String attribute) {
		this.toolAttributes.add(attribute);
	}
	
	public boolean hasToolTrait(String trait) {
		return this.toolTraits.contains(trait);
	}
	
	public boolean hasToolAttribute(String attribute) {
		return this.toolAttributes.contains(attribute);
	}

	public boolean hasTraits() {
		return !this.traits.isEmpty();
	}
	
	public Set<String> getTraits() {
		return this.traits;
	}
	
	public void addTraits(String trait) {
		this.traits.add(trait);
	}
	
	public void addTraits(Set<String> traits) {
		for (String trait : traits) {
			this.addTraits(trait);
		}
	}
	
	public Boolean hasThisTrait(String trait) {
		if (this.traits.isEmpty()) {
			return false;
		}
		for (String t : this.traits) {
			if (t.equals(trait)) {
				return true;
			}
		}
		return false;
	}
	
	public List<IngredientRecipe> getContents() {
		return new ArrayList<IngredientRecipe>(this.contents);
	}
	
	public List<IngredientRecipe> getConstituentIngredients()
	{
		return this.getConstituentIngredient(this.getContents());
	}
	
	public List<IngredientRecipe> getConstituentIngredient(List<IngredientRecipe> ingredients)
	{
		List<IngredientRecipe> subIngredients = new ArrayList<IngredientRecipe>();
		for (IngredientRecipe ingredient : ingredients)
		{
			if (ingredient.isSimple())
			{
				subIngredients.add(ingredient);
			}
			else
			{
				subIngredients.addAll(ingredient.getConstituentIngredients());
			}
			
		}
		return subIngredients;
	}
	
	public int getConstituentIngredientsCount() {
		return this.getConstituentIngredients().size() + this.getConstituentNecessaryTraits().size();
	}
	
	public AbstractMap<String, IngredientRecipe> getNecessaryTraits() {
		if (this.necessaryTraits != null) {
			return this.necessaryTraits;
		}
		return new HashMap<String, IngredientRecipe>();
	}
	
	public void addNecessaryTrait(String trait, int attributes) {
		IngredientRecipe ing = new IngredientRecipe(trait, attributes);
		this.necessaryTraits.put(trait, ing);
	}
	
	public void addNecessaryTraits(AbstractMap<String, IngredientRecipe> necessaryTraits) {
		this.necessaryTraits.putAll(necessaryTraits);
	}
	
	public AbstractMap<String, IngredientRecipe> getConstituentNecessaryTraits() {
		AbstractMap<String, IngredientRecipe> toRet = this.getNecessaryTraits();
		AbstractMap<String, IngredientRecipe> toAdd = getConstituentNecessaryTraits(this.contents);
		if (!toRet.isEmpty()) {
			if (!toAdd.isEmpty()) {
				toRet.putAll(toAdd);
			}
			return toRet;
		} else {
			if (!toAdd.isEmpty()) {
				return toAdd;
			}
		}
		return new HashMap<String, IngredientRecipe>();
	}
	
	public AbstractMap<String, IngredientRecipe> getConstituentNecessaryTraits(List<IngredientRecipe> ingredients) {
		AbstractMap<String, IngredientRecipe> traits = new HashMap<String, IngredientRecipe>();
		for (IngredientRecipe ingredient : ingredients) {
			if (!ingredient.isSimple()) {
				traits.putAll(ingredient.getNecessaryTraits());
				traits.putAll(getConstituentNecessaryTraits(ingredient.getContents()));
			}
		}
		return traits;
	}
	
	public Boolean AttributesMatch(ObjectInstance object) {
		if (IngredientFactory.isBakedIngredient(object) != this.getBaked()) {
			return false;
		}
		if (this.getMelted()) {
			if (!(IngredientFactory.isMeltedIngredient(object) || IngredientFactory.isMeltedAtRoomTemperature(object))) {
				return false;
			}
		} else {
			if (IngredientFactory.isMeltedIngredient(object)) {
				return false;
			}
		}
		if (IngredientFactory.isMixedIngredient(object) != this.getMixed()) {
			return false;
		}
		Set<String> ingToolAttributes = this.getToolAttributes();
		Set<String> objToolAttributes = IngredientFactory.getToolAttributes(object);
		if (ingToolAttributes.size() != objToolAttributes.size()) {
			return false;
		}
		for (String attribute : ingToolAttributes) {
			if (!objToolAttributes.contains(attribute)) {
				return false;
			}
		}
		return true;
	}
	
	
	public IngredientRecipe makeFakeAttributeCopy(ObjectInstance obj) {
		int attributes = generateAttributeNumber(IngredientFactory.isMixedIngredient(obj), 
				IngredientFactory.isMeltedIngredient(obj), IngredientFactory.isBakedIngredient(obj));
		IngredientRecipe newIng = new IngredientRecipe(this.getName(), attributes, this.getSwapped(),
				this.getContents());
		newIng.addNecessaryTraits(this.getNecessaryTraits());
		return newIng;
	}
	
	public void setAttributes(int attributes) {
		this.baked = ((attributes & Recipe.BAKED) == Recipe.BAKED) ? true : false;
		this.melted = ((attributes & Recipe.MELTED) == Recipe.MELTED) ? true : false;
		this.mixed = ((attributes & Recipe.MIXED) == Recipe.MIXED) ? true : false;
	}
	
	public static int generateAttributeNumber(Boolean mixed, Boolean melted, Boolean baked) {
		int mixed_int = mixed ? Recipe.MIXED : 0;
		int melted_int = melted ? Recipe.MELTED : 0;
		int baked_int = baked ? Recipe.BAKED : 0;
		return mixed_int|melted_int|baked_int;
	}
	
	public static AbstractMap<String, IngredientRecipe> getRecursiveSwappedIngredients(IngredientRecipe ingredient) {
		AbstractMap<String, IngredientRecipe> swapped = new HashMap<String, IngredientRecipe>();
		if (ingredient.isSimple()) {
			return swapped;
		}
		if (ingredient.getSwapped()) {
			swapped.put(ingredient.getName(), ingredient);
		}
		for (IngredientRecipe ing : ingredient.getContents()) {
			swapped.putAll(getRecursiveSwappedIngredients(ing));
		}
		return swapped;
	}
}
