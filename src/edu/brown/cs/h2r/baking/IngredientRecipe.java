package edu.brown.cs.h2r.baking;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;

import java.util.Set;
import java.util.TreeSet;

import burlap.oomdp.core.ObjectInstance;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;


public class IngredientRecipe {
	
	private Boolean mixed;
	private Boolean melted;
	private Boolean baked;
	private Set<String> traits;
	private Boolean peeled;
	private String name;
	private Boolean swapped;
	private List<IngredientRecipe> contents;
	private int useCount;
	private AbstractMap<String, IngredientRecipe> necessaryTraits;
	
	/*@Deprecated
	public IngredientRecipe (String name, Boolean mixed, Boolean melted, Boolean baked) {
		this.name = name;
		this.mixed = mixed;
		this.melted = melted;
		this.baked = baked;
		this.traits = new TreeSet<String>();
		this.useCount = 1;
		this.peeled = false;
	}
	
	@Deprecated
	public IngredientRecipe (String name, Boolean mixed, Boolean melted, Boolean baked, List<IngredientRecipe> contents) {
		this.name = name;
		this.mixed = mixed;
		this.melted = melted;
		this.baked = baked;
		this.peeled = false;
		this.contents = contents;
	}*/
	
	public IngredientRecipe(String name, Boolean mixed, Boolean melted, Boolean baked, Boolean peeled) {
		this.name = name;
		this.mixed = mixed;
		this.melted = melted;
		this.baked = baked;
		this.peeled = peeled;
		this.swapped = false;
		this.contents = null;
	}
	
	public IngredientRecipe(String name, Boolean mixed, Boolean melted, Boolean baked, Boolean peeled, Boolean swapped, List<IngredientRecipe> contents) {
		this.name = name;
		this.mixed = mixed;
		this.melted = melted;
		this.baked = baked;
		this.peeled = peeled;
		this.contents = contents;
		this.traits = new TreeSet<String>();
		this.swapped = false;
		this.necessaryTraits = new HashMap<String, IngredientRecipe>();
		this.useCount = 1;
	}
	
	/*public IngredientRecipe (String name, Boolean mixed, Boolean melted, Boolean baked, Boolean swapped, List<IngredientRecipe> contents) {
		this.name = name;
		this.mixed = mixed;
		this.melted = melted;
		this.baked = baked;
		this.swapped = swapped;
		this.contents = contents;
		this.traits = new TreeSet<String>();
		this.necessaryTraits = new HashMap<String, IngredientRecipe>();
		this.useCount = 1;
	}*/
	
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
	
	public void addNecessaryTrait(String trait, boolean mixed, boolean melted, boolean baked, boolean peeled) {
		IngredientRecipe ing = new IngredientRecipe(trait, mixed, melted, baked, peeled);
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
		if (IngredientFactory.isMeltedIngredient(object) != this.getMelted()) {
			return false;
		}
		if (IngredientFactory.isMixedIngredient(object) != this.getMixed()) {
			return false;
		}
		return true;
	}
}
