package edu.brown.cs.h2r.baking;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import static java.util.Arrays.asList;
import java.util.Set;
import java.util.TreeSet;


public class IngredientRecipe {
	
	private Boolean mixed;
	private Boolean melted;
	private Boolean baked;
	private Set<String> traits;
	private String name;
	private List<IngredientRecipe> contents;
	private Set<String> necessaryTraits;
	private List<IngredientRecipe> possibleIngredients;
	
	public IngredientRecipe (String name, Boolean mixed, Boolean melted, Boolean baked) {
		this.name = name;
		this.mixed = mixed;
		this.melted = melted;
		this.baked = baked;
		this.traits = new TreeSet<String>();
	}
	
	public IngredientRecipe (String name, Boolean mixed, Boolean melted, Boolean baked, List<IngredientRecipe> contents) {
		this.name = name;
		this.mixed = mixed;
		this.melted = melted;
		this.baked = baked;
		this.contents = contents;
		this.traits = new TreeSet<String>();
		this.possibleIngredients = new ArrayList<IngredientRecipe>();
	}
	
	public IngredientRecipe (String name, Boolean mixed, Boolean melted, Boolean baked, List<IngredientRecipe> contents, Set<String> compulsoryTraits) {
		this.name = name;
		this.mixed = mixed;
		this.melted = melted;
		this.baked = baked;
		this.contents = contents;
		this.traits = new TreeSet<String>();
		this.necessaryTraits = compulsoryTraits;
		this.possibleIngredients = new ArrayList<IngredientRecipe>();
	}
	
	public Boolean isSimple() {
		return this.contents == null || this.contents.size() == 0;
	}
	
	public Boolean getMixed () {
		return this.mixed;
	}
	
	public Boolean getMelted () {
		return this.melted;
	}
	
	public Boolean getBaked () {
		return this.baked;
	}
	
	public String getName() {
		return this.name;
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
				subIngredients.addAll(ingredient.getContents());
			}
			
		}
		return subIngredients;
	}
	
	public int getConstituentIngredientsCount() {
		int count = 0;
		if (this.contents != null ) {
			for (IngredientRecipe subIngredient : this.contents) {
				count += subIngredient.getConstituentIngredientsCount();
			}
		}
		else {
			count++;
		}
		return count;
	}
	
	public Set<String> getNecessaryTraits() {
		if (this.necessaryTraits != null) {
			return this.necessaryTraits;
		}
		return new TreeSet<String>();
	}
	
	public List<IngredientRecipe> getPossibleIngredients() {
		return this.possibleIngredients;
	}
	
	public void addPossibleIngredient(IngredientRecipe toadd) {
		this.possibleIngredients.add(toadd);
	}
	
	public void addPossibleIngredient(List<IngredientRecipe> toadd) {
		for (IngredientRecipe ing : toadd) {
			this.possibleIngredients.add(ing);
		}
	}
}
