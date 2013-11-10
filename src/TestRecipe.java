

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;



public class TestRecipe extends Recipe {

	public TestRecipe(String ingredient1) {
		super();
		List<Ingredient> ingredientList = new ArrayList<Ingredient>();
		ingredientList.add(new SimpleIngredient(ingredient1, false, false, false));
		this.topLevelIngredient = new ComplexIngredient("p1", false, false, false, ingredientList);
	}

	public TestRecipe(String ingredient1, String ingredient2) {
		super();
		List<Ingredient> ingredientList = new ArrayList<Ingredient>();
		ingredientList.add(new SimpleIngredient(ingredient1, false, false, false));
		ingredientList.add(new SimpleIngredient(ingredient2, false, false, false));
		this.topLevelIngredient = new ComplexIngredient("p1", false, false, false, ingredientList);
	}

	public TestRecipe(String ingredient1, String ingredient2, String ingredient3) {
		super();
		List<Ingredient> ingredientList = new ArrayList<Ingredient>();
		ingredientList.add(new SimpleIngredient(ingredient1, false, false, false));
		ingredientList.add(new SimpleIngredient(ingredient2, false, false, false));
		ingredientList.add(new SimpleIngredient(ingredient3, false, false, false));
		this.topLevelIngredient = new ComplexIngredient("p1", false, false, false, ingredientList);
	}

	public TestRecipe(String ingredient1, String ingredient2, String ingredient3, String ingredient4) {
		super();
		List<Ingredient> ingredientList = new ArrayList<Ingredient>();
		ingredientList.add(new SimpleIngredient(ingredient1, false, false, false));
		ingredientList.add(new SimpleIngredient(ingredient2, false, false, false));
		ingredientList.add(new SimpleIngredient(ingredient3, false, false, false));
		ingredientList.add(new SimpleIngredient(ingredient4, false, false, false));
		this.topLevelIngredient = new ComplexIngredient("p1", false, false, false, ingredientList);

	}

	protected void init()
	{
		List<Ingredient> ingredientList = new ArrayList<Ingredient>();
		ingredientList.add(new SimpleIngredient("s1", false, false, false));
		ingredientList.add(new SimpleIngredient("s2", false, false, false));
		ingredientList.add(new SimpleIngredient("s4", false, false, false));
		ingredientList.add(new SimpleIngredient("s5", false, false, false));
		this.topLevelIngredient = new ComplexIngredient("p1", false, false, false, ingredientList);
	}


}
