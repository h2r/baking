import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;


public class BrowniesSubGoals extends Recipe {
	
	public BrowniesSubGoals() {
		super();
		List<Ingredient> ingredientList = new ArrayList<Ingredient>();
		ingredientList.add(new SimpleIngredient("cocoa", false, false, false));
		ingredientList.add(new SimpleIngredient("eggs", false, false, false));
		ingredientList.add(new SimpleIngredient("sugar", false, false, false));
		Ingredient ingredient1 = new ComplexIngredient("goal1", false, false, false, ingredientList);
		
		List<Ingredient> ingredientList2 = new ArrayList<Ingredient>();
		ingredientList2.add(new SimpleIngredient("salt", false, false, false));
		ingredientList2.add(new SimpleIngredient("butter", false, false, false));
		ingredientList2.add(new SimpleIngredient("flour", false, false, false));
		Ingredient ingredient2 = new ComplexIngredient("goal2", false, false, false, ingredientList2);
		
		List<Ingredient> ingredientList3 = new ArrayList<Ingredient>();
		ingredientList3.add(ingredient1);
		//ingredientList3.add(ingredient2);
		ingredientList3.add(new SimpleIngredient("baking_soda", false, false, false));
		ingredientList3.add(new SimpleIngredient("baking_powder", false, false, false));
		this.topLevelIngredient = new ComplexIngredient("Brownies", false, false, false, ingredientList3);
	}
}
