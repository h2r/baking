import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;


public class Brownies extends Recipe {
	
	public Brownies() {
		super();
		List<IngredientRecipe> ingredientList = new ArrayList<IngredientRecipe>();
		ingredientList.add(new IngredientRecipe("cocoa", false, false, false));
		ingredientList.add(new IngredientRecipe("baking_soda", false, false, false));
		ingredientList.add(new IngredientRecipe("baking_powder", false, false, false));
		//ingredientList.add(new SimpleIngredient("eggs", false, false, false));
		//ingredientList.add(new SimpleIngredient("butter", false, false, false));
		//ingredientList.add(new SimpleIngredient("flour", false, false, false));
		//ingredientList.add(new SimpleIngredient("sugar", false, false, false));
		//ingredientList.add(new SimpleIngredient("salt", false, false, false));
		this.topLevelIngredient = new IngredientRecipe("Brownies", false, false, false, ingredientList);
	}
}
