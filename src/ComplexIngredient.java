import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;


public class ComplexIngredient extends IngredientFactory {
	
	public static final String className = "produced";
	public static final String attContains = "contains";
	public List<IngredientFactory> Contents;
	public ComplexIngredient(String name, Boolean melted, Boolean baked, Boolean mixed, List<IngredientFactory> contents) {
		super(name, melted, baked, mixed);
		this.Contents = new ArrayList<IngredientFactory>(contents);
	}
	
	@Override
	public ObjectInstance getObjectInstance(ObjectClass complexIngredientClass)
	{
		ObjectInstance objectInstance = new ObjectInstance(complexIngredientClass, this.Name);
		objectInstance.setValue(IngredientFactory.attributeBaked, this.Baked ? 1 : 0);
		objectInstance.setValue(IngredientFactory.attributeMelted, this.Melted ? 1 : 0);
		objectInstance.setValue(IngredientFactory.attributeMixed, this.Mixed ? 1 : 0);
		for (IngredientFactory ingredient : this.Contents)
		{
			objectInstance.addRelationalTarget(ComplexIngredient.attContains, ingredient.Name);
		}
		return objectInstance;
		
	}

	@Override
	public List<ObjectInstance> getSimpleObjectInstances(ObjectClass simpleIngredientClass) {
		List<ObjectInstance> ingredientsInstances = new ArrayList<ObjectInstance>();
		for (IngredientFactory ingredient : this.Contents)
		{
			ingredientsInstances.addAll(ingredient.getSimpleObjectInstances(simpleIngredientClass));
		}
		return ingredientsInstances;
	}
}
