import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;


public class ComplexIngredient extends Ingredient {
	
	public static final String className = "produced";
	public static final String attContains = "contains";
	public List<Ingredient> Contents;
	public ComplexIngredient(String name, Boolean melted, Boolean baked, Boolean mixed, List<Ingredient> contents) {
		super(name, melted, baked, mixed);
		this.Contents = new ArrayList<Ingredient>(contents);
	}
	
	@Override
	public ObjectInstance getObjectInstance(ObjectClass complexIngredientClass)
	{
		ObjectInstance objectInstance = new ObjectInstance(complexIngredientClass, this.Name);
		objectInstance.setValue(Ingredient.attributeBaked, this.Baked ? 1 : 0);
		objectInstance.setValue(Ingredient.attributeMelted, this.Melted ? 1 : 0);
		objectInstance.setValue(Ingredient.attributeMixed, this.Mixed ? 1 : 0);
		for (Ingredient ingredient : this.Contents)
		{
			objectInstance.addRelationalTarget(ComplexIngredient.attContains, ingredient.Name);
		}
		return objectInstance;
		
	}

	@Override
	public List<ObjectInstance> getSimpleObjectInstances(ObjectClass simpleIngredientClass) {
		List<ObjectInstance> ingredientsInstances = new ArrayList<ObjectInstance>();
		for (Ingredient ingredient : this.Contents)
		{
			ingredientsInstances.addAll(ingredient.getSimpleObjectInstances(simpleIngredientClass));
		}
		return ingredientsInstances;
	}
}
