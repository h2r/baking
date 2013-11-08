import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.Value;


public class SimpleIngredientInstance extends IngredientInstance implements Comparable{

	public SimpleIngredientInstance(SimpleIngredientInstance simpleIngredientInstance) {
		super(simpleIngredientInstance);
		// TODO Auto-generated constructor stub
	}

	public SimpleIngredientInstance(IngredientClass ingredientClass, String name) {
		super(ingredientClass, name);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + this.name.hashCode();
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
		
		return true;
	}

	@Override
	public Boolean contains(IngredientInstance ingredientInstance) {
		return this.equals(ingredientInstance);
	}
	
	@Override
	public Boolean terminate(IngredientInstance goal) {
		return false;
	}

	@Override
	public IngredientInstance copy() {
		return new SimpleIngredientInstance(this);
	}

	@Override
	public int compareTo(Object arg0) {
		int thisHash = this.hashCode();
		int otherHash = arg0.hashCode();
		return Integer.compare(thisHash, otherHash);
	}

	

}
