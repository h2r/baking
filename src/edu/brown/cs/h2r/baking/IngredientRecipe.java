package edu.brown.cs.h2r.baking;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.HashMap;
import java.util.Set;

import burlap.oomdp.core.ObjectInstance;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.Recipes.Recipe;


public class IngredientRecipe {
	
	private Boolean mixed;
	private Boolean heated;
	private Boolean baked;
	private Boolean recipeBaked;
	private Boolean recipeHeated;
	private Set<String> traits;
	private Set<String> toolTraits;
	private Set<String> toolAttributes;
	private String name;
	private Boolean swapped;
	private List<IngredientRecipe> contents;
	private int useCount;
	private AbstractMap<String, IngredientRecipe> necessaryTraits;
	private Set<String> recipeToolAttributes;
	
	public IngredientRecipe(String name, int attributes) {
		this.name = name;
		this.setAttributes(attributes);
		this.recipeBaked = false;
		this.recipeHeated = false;
		this.swapped = false;
		this.useCount = 1;
		this.traits = new HashSet<String>();
		this.toolTraits = new HashSet<String>();
		this.toolAttributes = new HashSet<String>();
		this.recipeToolAttributes = new HashSet<String>();
	}
	
	public IngredientRecipe(String name, int attributes, Boolean swapped, List<IngredientRecipe> contents) {
		this.name = name;
		this.setAttributes(attributes);
		this.recipeBaked = false;
		this.recipeHeated = false;
		this.contents = contents;
		this.swapped = swapped;
		this.useCount = 1;
		this.traits = new HashSet<String>();
		this.necessaryTraits = new HashMap<String, IngredientRecipe>();
		this.toolTraits = new HashSet<String>();
		this.toolAttributes = new HashSet<String>();
		this.recipeToolAttributes = new HashSet<String>();
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
	
	public Boolean getHeated () {
		return this.heated;
	}
	
	public void setHeated() {
		this.heated = true;
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
	
	public void addToolTraits(Collection<String> traits) {
		this.toolTraits.addAll(traits);
	}
	
	public void addToolAttribute(String attribute) {
		this.toolAttributes.add(attribute);
	}
	public void addToolAttributes(Set<String> attributes) {
		this.toolAttributes.addAll(attributes);
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
	
	public void addTraits(Collection<String> traits) {
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
		if (this.isSimple()) {
			return new ArrayList<IngredientRecipe>();
		}
		return new ArrayList<IngredientRecipe>(this.contents);
	}
	
	public void addContents(List<IngredientRecipe> contents) {
		this.contents.addAll(contents);
	}
	
	public List<String> getContentNames() {
		List<String> contents = new ArrayList<String>();
		for (IngredientRecipe ing : this.contents) {
			contents.add(ing.getName());
		}
		return contents;
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
	
	public boolean AttributesMatch(ObjectInstance object) {
		if (IngredientFactory.isBakedIngredient(object) != this.getBaked()) {
			return false;
		}
		if (this.getHeated()) {
			if (!(IngredientFactory.isHeatedIngredient(object) || IngredientFactory.isMeltedAtRoomTemperature(object))) {
				return false;
			}
		} else {
			if (IngredientFactory.isHeatedIngredient(object)) {
				return false;
			}
		}
		if (IngredientFactory.isMixedIngredient(object) != this.getMixed()) {
			return false;
		}
		return this.toolAttributesMatch(object);
	}
	
	public boolean toolAttributesMatch(ObjectInstance object) {
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
				IngredientFactory.isHeatedIngredient(obj), IngredientFactory.isBakedIngredient(obj));
		List<IngredientRecipe> contents = new ArrayList<IngredientRecipe>();
		if (!this.isSimple()) {
			contents.addAll(this.getContents());
		}
		IngredientRecipe newIng = new IngredientRecipe(this.getName(), attributes, this.getSwapped(),
				contents);
		newIng.addNecessaryTraits(this.getNecessaryTraits());
		return newIng;
	}
	
	public void setAttributes(int attributes) {
		this.baked = ((attributes & Recipe.BAKED) == Recipe.BAKED) ? true : false;
		this.heated = ((attributes & Recipe.HEATED) == Recipe.HEATED) ? true : false;
		this.mixed = ((attributes & Recipe.MIXED) == Recipe.MIXED) ? true : false;
	}
	
	public static int generateAttributeNumber(Boolean mixed, Boolean heated, Boolean baked) {
		int mixedInt = mixed ? Recipe.MIXED : 0;
		int heatedInt = heated ? Recipe.HEATED : 0;
		int bakedInt = baked ? Recipe.BAKED : 0;
		return mixedInt|heatedInt|bakedInt;
	}
	
	public int generateAttributeNumber() {
		int mixedInt = this.getMixed() ? Recipe.MIXED : 0;
		int heatedInt = this.getHeated() ? Recipe.HEATED : 0;
		int bakedInt = this.getBaked() ? Recipe.BAKED : 0;
		return mixedInt|heatedInt|bakedInt;	
	}
	
	public static AbstractMap<String, IngredientRecipe> getRecursiveSwappedIngredients(IngredientRecipe ingredient) {
		AbstractMap<String, IngredientRecipe> swapped = new HashMap<String, IngredientRecipe>();
		if (ingredient.getSwapped()) {
			swapped.put(ingredient.getName(), ingredient);
		}
		if (ingredient.isSimple()) {
			return swapped;
		}
		for (IngredientRecipe ing : ingredient.getContents()) {
			swapped.putAll(getRecursiveSwappedIngredients(ing));
		}
		return swapped;
	}
	
	public boolean hasBakedContent() {
		for (IngredientRecipe ing : this.getContents()) {
			if (ing.getBaked()) {
				return true;
			}
		}
		for (IngredientRecipe ing : this.getNecessaryTraits().values()) {
			if (ing.getBaked()) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasHeatedContent() {
		for (IngredientRecipe ing : this.getContents()) {
			if (ing.getHeated()) {
				return true;
			}
		}
		for (IngredientRecipe ing : this.getNecessaryTraits().values()) {
			if (ing.getHeated()) {
				return true;
			}
		}
		return false;
	}
	
	public void setRecipeBaked() {
		this.recipeBaked = true;
	}
	
	public void setRecipeHeated() {
		this.recipeHeated = true;
	}
	
	public boolean getRecipeBaked() {
		return this.recipeBaked;
	}
	public boolean getRecipeHeated() {
		return this.recipeHeated;
	}
	
	public void changeName(String newName) {
		this.name = newName;
	}
	
	public IngredientRecipe makeCopy() {
		String name = this.getName();
		int attributes = this.generateAttributeNumber();
		IngredientRecipe newIng = new IngredientRecipe(name, attributes);
		if( this.getRecipeBaked()) {
			newIng.setRecipeBaked();
		}
		if (this.getRecipeHeated()) {
			newIng.setRecipeHeated();
		}
		if (!this.isSimple()) {
			newIng.addContents(this.getContents());
		}
		if(this.getSwapped()) {
			newIng.setSwapped();
		}
		newIng.setUseCount(this.getUseCount());
		newIng.addTraits(this.getTraits());
		if (!this.getNecessaryTraits().isEmpty()) {
			newIng.addNecessaryTraits(this.getNecessaryTraits());
		}
		newIng.addToolTraits(this.getToolTraits());
		newIng.addToolAttributes(this.getToolAttributes());
		return newIng;
	}
	public IngredientRecipe getCopyWithNewName(String newName) {
		IngredientRecipe newIng = this.makeCopy();
		newIng.changeName(newName);
		return newIng;
	}
}
