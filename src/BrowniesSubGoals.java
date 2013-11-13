import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;


public class BrowniesSubGoals extends Recipe {
	
	public BrowniesSubGoals() {
		super();
		List<IngredientFactory> ingredientList = new ArrayList<IngredientFactory>();
		ingredientList.add(new SimpleIngredient("cocoa", false, false, false));
		ingredientList.add(new SimpleIngredient("eggs", false, false, false));
		ingredientList.add(new SimpleIngredient("sugar", false, false, false));
		IngredientFactory ingredient1 = new ComplexIngredient("goal1", false, false, false, ingredientList);
		
		List<IngredientFactory> ingredientList2 = new ArrayList<IngredientFactory>();
		ingredientList2.add(new SimpleIngredient("salt", false, false, false));
		ingredientList2.add(new SimpleIngredient("butter", false, false, false));
		ingredientList2.add(new SimpleIngredient("flour", false, false, false));
		IngredientFactory ingredient2 = new ComplexIngredient("goal2", false, false, false, ingredientList2);
		
		List<IngredientFactory> ingredientList3 = new ArrayList<IngredientFactory>();
		ingredientList3.add(ingredient1);
		//ingredientList3.add(ingredient2);
		ingredientList3.add(new SimpleIngredient("baking_soda", false, false, false));
		ingredientList3.add(new SimpleIngredient("baking_powder", false, false, false));
		this.topLevelIngredient = new ComplexIngredient("Brownies", false, false, false, ingredientList3);
	}
}
