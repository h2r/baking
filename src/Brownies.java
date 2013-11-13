import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;


public class Brownies extends Recipe {
	
	public Brownies() {
		super();
		List<IngredientFactory> ingredientList = new ArrayList<IngredientFactory>();
		ingredientList.add(new SimpleIngredient("cocoa", false, false, false));
		ingredientList.add(new SimpleIngredient("baking_soda", false, false, false));
		ingredientList.add(new SimpleIngredient("baking_powder", false, false, false));
		//ingredientList.add(new SimpleIngredient("eggs", false, false, false));
		//ingredientList.add(new SimpleIngredient("butter", false, false, false));
		//ingredientList.add(new SimpleIngredient("flour", false, false, false));
		//ingredientList.add(new SimpleIngredient("sugar", false, false, false));
		//ingredientList.add(new SimpleIngredient("salt", false, false, false));
		this.topLevelIngredient = new ComplexIngredient("Brownies", false, false, false, ingredientList);
	}
}
