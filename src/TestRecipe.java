

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;



public class TestRecipe extends Recipe {

	public TestRecipe(String ingredient1) {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(new IngredientRecipe(ingredient1, false, false, false));
		this.topLevelIngredient = new IngredientRecipe("p1", false, false, false, ingredientList);
	}

	public TestRecipe(String ingredient1, String ingredient2) {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(new IngredientRecipe(ingredient1, false, false, false));
		ingredientList.add(new IngredientRecipe(ingredient2, false, false, false));
		this.topLevelIngredient = new IngredientRecipe("p1", false, false, false, ingredientList);
	}

	public TestRecipe(String ingredient1, String ingredient2, String ingredient3) {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(new IngredientRecipe(ingredient1, false, false, false));
		ingredientList.add(new IngredientRecipe(ingredient2, false, false, false));
		ingredientList.add(new IngredientRecipe(ingredient3, false, false, false));
		this.topLevelIngredient = new IngredientRecipe("p1", false, false, false, ingredientList);
	}

	public TestRecipe(String ingredient1, String ingredient2, String ingredient3, String ingredient4) {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(new IngredientRecipe(ingredient1, false, false, false));
		ingredientList.add(new IngredientRecipe(ingredient2, false, false, false));
		ingredientList.add(new IngredientRecipe(ingredient3, false, false, false));
		ingredientList.add(new IngredientRecipe(ingredient4, false, false, false));
		this.topLevelIngredient = new IngredientRecipe("p1", false, false, false, ingredientList);

	}

	protected void init()
	{
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(new IngredientRecipe("s1", false, false, false));
		ingredientList.add(new IngredientRecipe("s2", false, false, false));
		ingredientList.add(new IngredientRecipe("s4", false, false, false));
		ingredientList.add(new IngredientRecipe("s5", false, false, false));
		this.topLevelIngredient = new IngredientRecipe("p1", false, false, false, ingredientList);
	}


}
