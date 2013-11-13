import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;

public class SimpleIngredient extends Ingredient {
	
	public static final String className = "simple";
	public SimpleIngredient(String name, Boolean melted, Boolean baked, Boolean mixed) {
		super (name, melted, baked, mixed);
	}
	
	@Override
	public ObjectInstance getObjectInstance(ObjectClass simpleIngredientClass)
	{
		ObjectInstance objectInstance = new ObjectInstance(simpleIngredientClass, this.Name);
		objectInstance.setValue(Ingredient.attributeBaked, this.Baked ? 1 : 0);
		objectInstance.setValue(Ingredient.attributeMelted, this.Melted ? 1 : 0);
		objectInstance.setValue(Ingredient.attributeMixed, this.Mixed ? 1 : 0);
		return objectInstance;
	}

	@Override
	public List<ObjectInstance> getSimpleObjectInstances(ObjectClass simpleIngredientClass)
	{
		List<ObjectInstance> objectInstances = new ArrayList<ObjectInstance>();
		objectInstances.add(this.getObjectInstance(simpleIngredientClass));
		return objectInstances;
	}
}