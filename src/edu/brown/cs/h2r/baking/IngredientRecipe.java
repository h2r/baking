package edu.brown.cs.h2r.baking;
import java.util.ArrayList;
import java.util.List;


public class IngredientRecipe {
	
	private Boolean mixed;
	private Boolean melted;
	private Boolean baked;
	private Boolean peeled;
	private String name;
	private List<IngredientRecipe> contents;
	
	@Deprecated
	public IngredientRecipe (String name, Boolean mixed, Boolean melted, Boolean baked) {
		this.name = name;
		this.mixed = mixed;
		this.melted = melted;
		this.baked = baked;
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
	}
	
	public IngredientRecipe(String name, Boolean mixed, Boolean melted, Boolean baked, Boolean peeled) {
		this.name = name;
		this.mixed = mixed;
		this.melted = melted;
		this.baked = baked;
		this.peeled = peeled;
	}
	
	public IngredientRecipe(String name, Boolean mixed, Boolean melted, Boolean baked, Boolean peeled, List<IngredientRecipe> contents) {
		this.name = name;
		this.mixed = mixed;
		this.melted = melted;
		this.baked = baked;
		this.peeled = peeled;
		this.contents = contents;
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
	
	public Boolean getPeeled() {
		return this.peeled;
	}
	
	public String getName() {
		return this.name;
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
}
