package edu.brown.cs.h2r.baking;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import static java.util.Arrays.asList;


public class IngredientRecipe {
	
	private Boolean mixed;
	private Boolean melted;
	private Boolean baked;
	private Boolean dry;
	private String name;
	private List<IngredientRecipe> contents;
	//private List<IngredientRecipe> optionList;
	//private Boolean options;
	private HashMap<String, Boolean> affordances;
	private List<String> affordance_list;
	private Boolean has_affordance;
	
	/*public IngredientRecipe (String name, Boolean mixed, Boolean melted, Boolean baked, Boolean dry) {
		this.name = name;
		this.mixed = mixed;
		this.melted = melted;
		this.baked = baked;
		this.dry = dry;
		//this.options = false;
		
		this.has_affordance = false;
		//affordance_list = new ArrayList<String>(asList("is_dry", "is_wet", "is_fruit", "is_fat", "is_vegetable", "is_grain"));
		affordance_list = new ArrayList<String>(asList("dry"));
		this.affordances = new HashMap<String,Boolean>();
		for (String s : affordance_list) {
			this.affordances.put(s, false);
		}
		
	}
	
	public IngredientRecipe (String name, Boolean mixed, Boolean melted, Boolean baked, Boolean dry, List<IngredientRecipe> contents) {
		this.name = name;
		this.mixed = mixed;
		this.melted = melted;
		this.baked = baked;
		this.dry = dry;
		this.contents = contents;
		//this.optionList = new ArrayList<IngredientRecipe>();
		
		this.has_affordance = false;
		//affordance_list = new ArrayList<String>(asList("is_dry", "is_wet", "is_fruit", "is_fat", "is_vegetable", "is_grain"));
		affordance_list = new ArrayList<String>(asList("dry"));
		this.affordances = new HashMap<String,Boolean>();
		for (String s : affordance_list) {
			this.affordances.put(s, false);
		}
	}*/
	
	public IngredientRecipe (String name, Boolean mixed, Boolean melted, Boolean baked) {
		this.name = name;
		this.mixed = mixed;
		this.melted = melted;
		this.baked = baked;
		/*this.options = false;
		
		this.has_affordance = false;
		affordance_list = new ArrayList<String>(asList("is_dry", "is_wet", "is_fruit", "is_fat", "is_vegetable", "is_grain"));
		affordance_list = new ArrayList<String>(asList("dry"));
		this.affordances = new HashMap<String,Boolean>();
		for (String s : affordance_list) {
			this.affordances.put(s, false);
		}*/
		
	}
	
	public IngredientRecipe (String name, Boolean mixed, Boolean melted, Boolean baked, List<IngredientRecipe> contents) {
		this.name = name;
		this.mixed = mixed;
		this.melted = melted;
		this.baked = baked;
		this.contents = contents;
		/*this.optionList = new ArrayList<IngredientRecipe>();
		
		this.has_affordance = false;
		//affordance_list = new ArrayList<String>(asList("is_dry", "is_wet", "is_fruit", "is_fat", "is_vegetable", "is_grain"));
		affordance_list = new ArrayList<String>(asList("dry"));
		this.affordances = new HashMap<String,Boolean>();
		for (String s : affordance_list) {
			this.affordances.put(s, false);
		}*/
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
	
	/* Affordances
	public void set_affordance(String attribute) {
		this.affordances.put(attribute, true);
		this.has_affordance = true;
	}
	
	public HashMap<String, Boolean> get_affordances() {
		return this.affordances;
	}
	
	public Boolean has_affordance() {
		return this.has_affordance;
	}
	 End Affordances */

	/* "Options"
	public Boolean getOptions() {
		return this.options;
	}
	
	public void setOptions(Boolean bool) {
		this.options = bool;
	}
	
	public void addToOptionsList(IngredientRecipe ingredient) {
		this.optionList.add(ingredient);
	}
	
	public List<IngredientRecipe> getOptionList() {
		return this.optionList;
	}

 	End "options"*/
	
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
		{/*
			if (ingredient.getOptions()) {
				for (IngredientRecipe ing : ingredient.getContents()) {
					if (ing.isSimple())
					{
						subIngredients.add(ing);
					}
					else
					{
						subIngredients.addAll(ing.getContents());
					}
				}
			}
			else {*/
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
