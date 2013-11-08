import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.Value;


public abstract class IngredientInstance extends ObjectInstance {

	public IngredientInstance(IngredientInstance ingredientInstance) {
		super(ingredientInstance);
		this.setAttributes();
	}

	public IngredientInstance(IngredientClass ingredientClass, String name) {
		super(ingredientClass, name);
		this.setAttributes();
	}
	
	protected void setAttributes()
	{
		this.setValue(IngredientClass.attBaked, 0);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result =  prime * this.getValueForAttribute(IngredientClass.attBaked).hashCode();
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		SimpleIngredientInstance other = (SimpleIngredientInstance)obj;
		Value bakedCurrent = this.getValueForAttribute(IngredientClass.attBaked);
		Value bakedOther = other.getValueForAttribute(IngredientClass.attBaked);
		if (bakedCurrent.getDiscVal() != bakedOther.getDiscVal())
		{
			return false;
		}
		return true;
	}
	
	public abstract IngredientInstance copy();
	public abstract Boolean contains(IngredientInstance ingredientInstance);
	
	public abstract Boolean terminate(IngredientInstance goal);
}
