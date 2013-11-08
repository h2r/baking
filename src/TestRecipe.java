import java.util.Arrays;
import java.util.TreeSet;


public class TestRecipe extends Recipe {

	public TestRecipe(ComplexIngredientInstance complexIngredientInstance) {
		super(complexIngredientInstance);
		this.init();
	}

	public TestRecipe(IngredientClass ingredientClass, String name) {
		super(ingredientClass, name);
		this.init();
	}
	
	protected void init()
	{
		IngredientClass ingredientClass = (IngredientClass)this.obClass;
		this.simpleContents = new TreeSet<SimpleIngredientInstance>(Arrays.asList(
				new SimpleIngredientInstance(ingredientClass, "s1"),
				new SimpleIngredientInstance(ingredientClass, "s2"),
				new SimpleIngredientInstance(ingredientClass, "s4"),
				new SimpleIngredientInstance(ingredientClass, "s5")));
	}

	@Override
	public IngredientInstance copy() {
		// TODO Auto-generated method stub
		return null;
	}

}
