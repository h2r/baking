import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.TreeSet;


public class Brownies extends Recipe {
	
	public Brownies(ComplexIngredientInstance complexIngredientInstance) {
		super(complexIngredientInstance);
		this.init();
	}

	public Brownies(IngredientClass ingredientClass, String name) {
		super(ingredientClass, name);
		this.init();
	}
	
	protected void init()
	{
		IngredientClass ingredientClass = (IngredientClass)this.obClass;
		List<SimpleIngredientInstance> ingredients = new ArrayList<SimpleIngredientInstance>();
		ingredients.add(new SimpleIngredientInstance(ingredientClass, "cocoa"));
		ingredients.add(new SimpleIngredientInstance(ingredientClass, "butter"));
		ingredients.add(new SimpleIngredientInstance(ingredientClass, "flour"));
		ingredients.add(new SimpleIngredientInstance(ingredientClass, "sugar"));
		ingredients.add(new SimpleIngredientInstance(ingredientClass, "salt"));
		ingredients.add(new SimpleIngredientInstance(ingredientClass, "baking_soda"));
		ingredients.add(new SimpleIngredientInstance(ingredientClass, "baking_powder"));
		ingredients.add(new SimpleIngredientInstance(ingredientClass, "eggs"));
		this.simpleContents = new TreeSet<SimpleIngredientInstance>(ingredients);
	}

	@Override
	public IngredientInstance copy() {
		return new Brownies(this);
	}
}
