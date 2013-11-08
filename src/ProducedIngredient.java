import java.util.List;


public class ProducedIngredient extends ComplexIngredientInstance {

	public ProducedIngredient(
			ComplexIngredientInstance complexIngredientInstance) {
		super(complexIngredientInstance);
		// TODO Auto-generated constructor stub
	}

	public ProducedIngredient(IngredientClass ingredientClass, String name,
			List<IngredientInstance> contents) {
		super(ingredientClass, name);
		for (IngredientInstance ingredientInstance : contents)
		{
			if (ingredientInstance instanceof SimpleIngredientInstance)
			{
				this.simpleContents.add((SimpleIngredientInstance)ingredientInstance);
			}
			else if (ingredientInstance instanceof ComplexIngredientInstance)
			{
				this.complexContents.add((ComplexIngredientInstance)ingredientInstance);
			}
		}
	}

	@Override
	public IngredientInstance copy() {
		return new ProducedIngredient(this);
	}

}
