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
	private Boolean peeled;
	private Set<String> traits;
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
		this.peeled = false;
		this.traits = new TreeSet<String>();
		this.useCount = 1;
	}
	
	
	public IngredientRecipe (String name, int attributes, List<IngredientRecipe> contents) {
		this.name = name;
		this.setAttributes(attributes);
		this.swapped = false;
		this.contents = contents;
		this.traits = new TreeSet<String>();
		this.necessaryTraits = new HashMap<String, IngredientRecipe>();
		this.useCount = 1;
	}
	
	public IngredientRecipe(String name, int attributes, Boolean swapped, List<IngredientRecipe> contents) {
		this.name = name;
		this.setAttributes(attributes);
		this.contents = contents;
		this.traits = new TreeSet<String>();
		this.swapped = swapped;
		this.necessaryTraits = new HashMap<String, IngredientRecipe>();
		this.useCount = 1;
	}
	
	public IngredientRecipe(String name, int attributes, Boolean swapped, List<IngredientRecipe> contents, AbstractMap<String, IngredientRecipe> necessaryTraits) {
		this.name = name;
		this.setAttributes(attributes);
		this.contents = contents;
		this.traits = new TreeSet<String>();
		this.swapped = swapped;
		this.necessaryTraits = necessaryTraits;
		this.useCount = 1;
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
	
	public void setPeeled() {
		this.peeled = true;
	}
	
	public Boolean getPeeled() {
		return this.peeled;
	}
		
	public String getName() {
		return this.name;
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

	/* traits */
	public Boolean hasTraits() {
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
		if (IngredientFactory.isPeeledIngredient(object) != this.getPeeled()) {
			return false;
		}
		return true;
	}
	
	
	public IngredientRecipe makeFakeAttributeCopy(ObjectInstance obj) {
		return new IngredientRecipe(this.getName(), generateAttributeNumber(IngredientFactory.isMixedIngredient(obj), 
				IngredientFactory.isMeltedIngredient(obj), IngredientFactory.isBakedIngredient(obj), 
				IngredientFactory.isPeeledIngredient(obj)), this.getSwapped(), this.getContents(), this.getNecessaryTraits());
	}
	
	public void setAttributes(int attributes) {
		this.baked = ((attributes & Recipe.BAKED) == Recipe.BAKED) ? true : false;
		this.melted = ((attributes & Recipe.MELTED) == Recipe.MELTED) ? true : false;
		this.mixed = ((attributes & Recipe.MIXED) == Recipe.MIXED) ? true : false;
		this.peeled = ((attributes & Recipe.PEELED) == Recipe.PEELED) ? true : false;
	}
	
	public static int generateAttributeNumber(Boolean mixed, Boolean melted, Boolean baked, Boolean peeled) {
		int mixed_int = mixed ? Recipe.MIXED : 0;
		int melted_int = melted ? Recipe.MELTED : 0;
		int baked_int = baked ? Recipe.BAKED : 0;
		int peeled_int = peeled ? Recipe.PEELED : 0;
		return mixed_int|melted_int|baked_int|peeled_int;
	}
}
