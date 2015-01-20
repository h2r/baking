package edu.brown.cs.h2r.baking;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.Knowledgebase.Knowledgebase;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.Recipes.Recipe;


public class IngredientRecipe {
	
	private final Recipe recipe;
	private Boolean mixed;
	private Boolean heated;
	private Boolean baked;
	private Boolean recipeBaked;
	private Boolean recipeHeated;
	private Set<String> recipeToolAttributes;
	private Set<String> traits;
	private Set<String> toolTraits;
	private Set<String> toolAttributes;
	private Set<String> prepTraits;
	
	private String heatingInformation;
	private String heatedState;
	private String name;
	private Boolean swapped;
	private List<IngredientRecipe> contents;
	private int useCount;
	private AbstractMap<String, IngredientRecipe> necessaryTraits;
	
	public IngredientRecipe(String name, int attributes, Recipe recipe) {
		this.name = name;
		this.setAttributes(attributes);
		this.recipeBaked = false;
		this.recipeHeated = false;
		this.swapped = false;
		this.useCount = 1;
		this.traits = new HashSet<String>();
		this.toolTraits = new HashSet<String>();
		this.toolAttributes = new HashSet<String>();
		this.prepTraits = new HashSet<String>();
		this.necessaryTraits = new HashMap<String, IngredientRecipe>();
		this.recipe = recipe;
	}
	
	public IngredientRecipe(String name, int attributes, Recipe recipe, Boolean swapped, List<IngredientRecipe> contents) {
		this.name = name;
		this.setAttributes(attributes);
		this.recipeBaked = false;
		this.recipeHeated = false;
		this.contents = contents;
		
		for (IngredientRecipe ingredient : contents) {
			if (ingredient == null) {
				System.err.println("Ingredient is null");
			}
		}
		
		this.swapped = swapped;
		this.useCount = 1;
		this.traits = new HashSet<String>();
		this.necessaryTraits = new HashMap<String, IngredientRecipe>();
		this.toolTraits = new HashSet<String>();
		this.toolAttributes = new HashSet<String>();
		this.prepTraits = new HashSet<String>();
		this.recipe = recipe;
	}
	
	public IngredientRecipe(IngredientRecipe other) {
		this.name = other.name;
		this.setAttributes(other.getAttributeNumber());
		this.recipeBaked = other.recipeBaked;
		this.recipeHeated = other.recipeHeated;
		if (other.contents != null)
		this.contents = (other.contents == null) ? null : new ArrayList<IngredientRecipe>(other.contents);
		this.swapped = other.swapped;
		this.useCount = other.useCount;
		this.heatingInformation = other.heatingInformation;
		this.heatedState = other.heatedState;
		this.traits = new HashSet<String>(other.traits);
		this.necessaryTraits = new HashMap<String, IngredientRecipe>(other.necessaryTraits);
		this.toolTraits = new HashSet<String>(other.toolTraits);
		this.toolAttributes = new HashSet<String>(other.toolAttributes);
		this.prepTraits = new HashSet<String>(other.prepTraits);
		this.recipe = other.recipe;
	}
	
	//Copy Constructor
	public IngredientRecipe(String name, int attributes, Recipe recipe, boolean recipeBaked, boolean recipeHeated, List<IngredientRecipe> contents,
			boolean swapped, int useCount, String heatingInfo, String heatedState, Set<String> traits, AbstractMap<String, 
			IngredientRecipe> necessaryTraits, Set<String> toolTraits, Set<String> toolAttributes, Set<String> prepTraits) {
		this.name = name;
		this.setAttributes(attributes);
		this.recipeBaked = recipeBaked;
		this.recipeHeated = recipeHeated;
		this.contents = new ArrayList<IngredientRecipe>(contents);
		
		for (IngredientRecipe ingredient : contents) {
			if (ingredient == null) {
				System.err.println("Ingredient is null");
			}
		}
		
		
		this.swapped = swapped;
		this.useCount = useCount;
		this.heatingInformation = heatingInfo;
		this.heatedState = heatedState;
		this.traits = new HashSet<String>(traits);
		this.necessaryTraits = new HashMap<String, IngredientRecipe>(necessaryTraits);
		this.toolTraits = new HashSet<String>(toolTraits);
		this.toolAttributes = new HashSet<String>(toolAttributes);
		this.prepTraits = new HashSet<String>(prepTraits);
		this.recipe = recipe;
	}
	
	@Override 
	public String toString() {
		String recipeName = (this.recipe == null || this.isSimple()) ? "" : this.recipe.toString()  + " - ";
		return recipeName + this.name;
	}
	public Boolean isSimple() {
		if (this.necessaryTraits == null || this.necessaryTraits.size() == 0) {
			return this.contents == null || this.contents.size() == 0;
		}
		return false;
	}
	
	public void setMixed() {
		this.mixed = true;
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
	
	public String getSimpleName(){
		return this.name;
	}
	
	public String getFullName() {
		return this.toString();
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
	
	public void incrementUseCount() {
		this.useCount++;
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
	
	public void addPrepTraits(Collection<String> traits) {
		this.prepTraits.addAll(traits);
	}
	public boolean hasToolTrait(String trait) {
		return this.toolTraits.contains(trait);
	}
	
	public boolean hasToolAttribute(String attribute) {
		return this.toolAttributes.contains(attribute);
	}
	
	public boolean hasPrepTraits(String trait) {
		return this.prepTraits.contains(trait);
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
			contents.add(ing.toString());
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
		IngredientRecipe ing = new IngredientRecipe(trait, attributes, this.recipe);
		this.necessaryTraits.put(trait, ing);
	}
	
	public void addNecessaryTrait(String trait, int attributes, String heatedState) {
		IngredientRecipe ing = new IngredientRecipe(trait, attributes, this.recipe);
		ing.setHeatedState(heatedState);
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
	
	public boolean attributesMatch(ObjectInstance object) {
		int thisAttributes = this.getAttributeNumber();
		int objectAttributes = IngredientFactory.getAttributeNumber(object);
		return (thisAttributes == objectAttributes);
		
	}
	
	/*
	public boolean AttributesMatch(ObjectInstance object) {
		if (IngredientFactory.isBakedIngredient(object) != this.getBaked()) {
			return false;
		}
		if (this.getHeated()) {
			if (!(IngredientFactory.isHeatedIngredient(object) || IngredientFactory.isMeltedAtRoomTemperature(object))) {
				return false;
			}
			String ingHeatedState = this.getHeatedState();
			String objHeatedState = IngredientFactory.getHeatedState(object);
			if (ingHeatedState == null) {
				if (!objHeatedState.equals("")) {
					return false;
				}
			}
			else if (!ingHeatedState.equals(objHeatedState)) {
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
	}*/
	
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
		IngredientRecipe newIng = new IngredientRecipe(this.toString(), attributes, this.recipe, this.getSwapped(),
				contents);
		newIng.addNecessaryTraits(this.getNecessaryTraits());
		return newIng;
	}
	
	// Makes an ingredient copy of the object. Any attributes the object doesn't have are
	// ignored (and theingredient copy won't have them either), but any attributes the object
	// does have will only transfer only if the ingredient has them too. This way, we prevent false positives
	// for the failure method, but also properly catch a failure.
	public IngredientRecipe makeFakeFailureCopy(ObjectInstance obj) {
		boolean mixed = this.getMixed() && IngredientFactory.isMixedIngredient(obj);
		boolean heated = this.getHeated() && IngredientFactory.isHeatedIngredient(obj);
		boolean baked = this.getBaked() && IngredientFactory.isBakedIngredient(obj);
		int attributes = generateAttributeNumber(mixed, heated, baked);
		List<IngredientRecipe> contents = new ArrayList<IngredientRecipe>();
		if (!this.isSimple()) {
			contents.addAll(this.getContents());
		}
		IngredientRecipe newIng = new IngredientRecipe(this.toString(), attributes, this.recipe, this.getSwapped(),
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
	
	public int getAttributeNumber() {
		int mixedInt = this.getMixed() ? Recipe.MIXED : 0;
		int heatedInt = this.getHeated() ? Recipe.HEATED : 0;
		int bakedInt = this.getBaked() ? Recipe.BAKED : 0;
		return mixedInt|heatedInt|bakedInt;	
	}
	
	public static AbstractMap<String, IngredientRecipe> getRecursiveSwappedIngredients(IngredientRecipe ingredient) {
		AbstractMap<String, IngredientRecipe> swapped = new HashMap<String, IngredientRecipe>();
		if (ingredient.getSwapped()) {
			swapped.put(ingredient.toString(), ingredient);
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
	
	public IngredientRecipe getCopyWithNewName(String newName) {
		int attributes = this.getAttributeNumber();
		boolean recipeBaked = this.getRecipeBaked();
		boolean recipeHeated = this.getRecipeHeated();
		List<IngredientRecipe> contents = this.getContents();
		boolean swapped = this.getSwapped();
		int useCount = this.getUseCount();
		String heatingInfo = this.getHeatingInfo();
		String heatedState = this.getHeatedState();
		Set<String> traits = this.getTraits();
		AbstractMap<String, IngredientRecipe> necessaryTraits = this.getNecessaryTraits();
		Set<String> toolTraits = this.getToolTraits();
		Set<String> toolAttributes = this.getToolAttributes();
		Set<String> prepTraits = this.getPrepTraits();
		
		return new IngredientRecipe(newName, attributes, this.recipe, recipeBaked, recipeHeated, contents,
				swapped, useCount, heatingInfo, heatedState, traits, necessaryTraits, toolTraits, toolAttributes, prepTraits);
	}
	
	public IngredientRecipe getCopyWithNewAttributes(int attributes) {
		String name = this.getSimpleName();
		boolean recipeBaked = this.getRecipeBaked();
		boolean recipeHeated = this.getRecipeHeated();
		List<IngredientRecipe> contents = this.getContents();
		boolean swapped = this.getSwapped();
		int useCount = this.getUseCount();
		String heatingInfo = this.getHeatingInfo();
		String heatedState = this.getHeatedState();
		Set<String> traits = this.getTraits();
		AbstractMap<String, IngredientRecipe> necessaryTraits = this.getNecessaryTraits();
		Set<String> toolTraits = this.getToolTraits();
		Set<String> toolAttributes = this.getToolAttributes();
		Set<String> prepTraits = this.getPrepTraits();
		
		IngredientRecipe other = new IngredientRecipe(name, attributes, this.recipe, recipeBaked, recipeHeated, contents,
				swapped, useCount, heatingInfo, heatedState, traits, necessaryTraits, toolTraits, toolAttributes, prepTraits);
		
		return other;
	}
	
	public boolean objectHasExtraAttribute(ObjectInstance object) {
		if (IngredientFactory.isMixedIngredient(object)) {
			return !this.getMixed();
		}
		if (IngredientFactory.isBakedIngredient(object)) {
			return !this.getBaked();
		}
		if (IngredientFactory.isHeatedIngredient(object)) {
			return !this.getHeated();
		}
		return false;
	}
	
	public void addRecipeToolAttributes(Set<String> attributes) {
		this.recipeToolAttributes = new HashSet<String>(attributes);
	}
	
	public Set<String> getRecipeToolAttributes() {
		return this.recipeToolAttributes;
	}
	
	public void addHeatingInformation(String info) {
		this.heatingInformation = info;
	}
	
	public String getHeatingInfo() {
		return this.heatingInformation;
	}
	
	public boolean hasHeatingInfo() {
		return this.heatingInformation != null;
	}
	
	public String getHeatedState() {
		return this.heatedState;
	}
	
	public void setHeatedState(String heatedState) {
		this.heatedState = heatedState;
	}
	
	// Check if this object in state matches this ingredient recipe
	public boolean isMatching(ObjectInstance object, State state) {
		if (object.getName().equals(this.toString())) {
			System.out.print("");
		}
		// If the attributes don't match, then not there yet
		if (!this.attributesMatch(object)) {
			return false;
		}
		
		// If this is a simple ingredient, then the names must match. Yes this is called twice if going through
		// isMatching, astute observer
		if (IngredientFactory.isSimple(object)) {
			return this.isSimple() && (object.getName().equals(this.toString()));
		}
		
		// Now we check the subIngredients
		Set<String> contentNames = IngredientFactory.getContentsForIngredient(object);
		List<IngredientRecipe> subIngredients = new ArrayList<IngredientRecipe>(this.getContents());
		List<IngredientRecipe> matchedSubIngredients = new ArrayList<IngredientRecipe>();
		for (IngredientRecipe subIngredient : subIngredients) {
			String name = subIngredient.toString();
			if (contentNames.remove(name)) {
				ObjectInstance ingredient = state.getObject(name);
				if(!subIngredient.isMatching(ingredient, state)) {
					return false;
				} else {
					matchedSubIngredients.add(subIngredient);
				}
			}
		}
		subIngredients.removeAll(matchedSubIngredients);
		for (IngredientRecipe subIngredient : subIngredients) {
			boolean matched = false;
			String matchedName = null;
			for (String name : contentNames) {
				ObjectInstance ingredient = state.getObject(name);
				if(subIngredient.isMatching(ingredient, state)) {
					matchedSubIngredients.add(subIngredient);
					matched = true;
					matchedName = name;
				}
			}
			if (!matched) {
				return false;
			}
			contentNames.remove(matchedName);
		}
		Knowledgebase knowledgebase = Knowledgebase.getKnowledgebase();
		Map<String, IngredientRecipe> necessaryTraits = new HashMap<String, IngredientRecipe>(this.necessaryTraits);
		if (contentNames.size() > necessaryTraits.size()) {
			return false;
		}
		for (String name : contentNames) {
			IngredientRecipe ingredient = knowledgebase.getIngredient(name);
			if (ingredient == null) {
				continue;
			}
			for (String trait : ingredient.getTraits()) {
				IngredientRecipe matching = necessaryTraits.remove(trait);
				if (matching == null) {
					continue;
				}
				ObjectInstance ingredientObject = state.getObject(name);
				if (!matching.isTraitMatching(ingredientObject, state)) {
					return false;
				}
			}
			
		}
		
		return necessaryTraits.isEmpty();/*
		// For each item contained within the object
		for (String subIngredientName : contentNames) {
			
			// Retrieve object
			ObjectInstance subIngredient = state.getObject(subIngredientName);
			int indexToRemove = -1;
			
			// Find this object in the recipe
			for (int i = 0; i < subIngredients.size(); i++) {
				
				// Check if it matches
				if (subIngredients.get(i).isMatching(subIngredient, state)) {
					indexToRemove = i;
					break;
				}
			}
			
			// If it wasn't found, then we can stop
			if (indexToRemove == -1) {
				return false;
			} else {
				// Remove it from the list, and continue on
				subIngredients.remove(indexToRemove);
			}
		}
		
		// If we didn't find a one-to-one match, then they are not the same
		if (subIngredients.size() != 0) {
			return false;
		}
		
		
		return true;*/
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.getFullName(), this.getAttributeNumber(), this.traits, this.necessaryTraits, this.toolTraits, this.toolAttributes, this.prepTraits );
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		
		if (!(other instanceof IngredientRecipe)) {
			return false;
		}
		IngredientRecipe otherIngredient = (IngredientRecipe)other;
		
		if (!this.getFullName().equals(otherIngredient.getFullName())) {
			return false;
		}
		
		if (this.getAttributeNumber() != otherIngredient.getAttributeNumber()) {
			return false;
		}
		
		if ((this.traits == null) != (otherIngredient.traits == null)) {
			return false;
		}
		
		if (!this.traits.equals(otherIngredient.traits)){ 
			return false;
		}
		
		if ((this.necessaryTraits == null) != (otherIngredient.necessaryTraits == null)) {
			return false;
		}
		if (!this.necessaryTraits.equals(otherIngredient.necessaryTraits)){ 
			return false;
		}
		
		if ((this.toolTraits == null) != (otherIngredient.toolTraits == null)) {
			return false;
		}
		
		if (!this.toolTraits.equals(otherIngredient.toolTraits)){ 
			return false;
		}
		
		if ((this.toolAttributes == null) != (otherIngredient.toolAttributes == null)) {
			return false;
		}
		
		if (!this.toolAttributes.equals(otherIngredient.toolAttributes)){ 
			return false;
		}
		
		if ((this.prepTraits == null) != (otherIngredient.prepTraits == null)) {
			return false;
		}
		
		if (!this.prepTraits.equals(otherIngredient.prepTraits)){ 
			return false;
		}
		
		return true;
	}
	
	public boolean isTraitMatching(ObjectInstance object, State state) {
		
		// If the attributes don't match, then not there yet
		if (!this.attributesMatch(object)) {
			return false;
		}
		
		Set<String> objectTraits = IngredientFactory.getTraits(object);
		return (objectTraits.contains(this.getSimpleName()));

	}

	public Set<String> getPrepTraits() {
		return this.prepTraits;
	}
}
