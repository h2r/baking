import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.ObjectClass;


public abstract class Recipe extends ComplexIngredientInstance{

	public Recipe(ComplexIngredientInstance complexIngredientInstance) {
		super(complexIngredientInstance);
	}

	public Recipe(IngredientClass ingredientClass, String name) {
		super(ingredientClass, name);
	}
	
	public List<IngredientInstance> getRecipeList()
	{
		List<IngredientInstance> ingredients = new ArrayList<IngredientInstance>();
		this.addIngredientsToList(this, ingredients);
		return ingredients;
	}
	
	public void addIngredientsToList(ComplexIngredientInstance ingredient, 
			List<IngredientInstance> ingredients)
	{
		ingredients.addAll(ingredient.simpleContents);
		for (ComplexIngredientInstance complexIngredientInstance : ingredient.complexContents)
		{
			this.addIngredientsToList(complexIngredientInstance, ingredients);
		}
	}
	
	public List<ContainerInstance> getContainers(ContainerClass containerClass)
	{
		List<ContainerInstance> containers = new ArrayList<ContainerInstance>();
		List<IngredientInstance> ingredientList = this.getRecipeList();
		
		for (IngredientInstance ingredientInstance : ingredientList)
		{
			ContainerInstance container = 
					new IngredientContainerInstance(containerClass, ingredientInstance.getName() + "_bowl");
			container.add(ingredientInstance);
			containers.add(container);
		}
		return containers;
	}
}
