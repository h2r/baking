import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import java.util.SortedSet;

public abstract class ComplexIngredientInstance extends IngredientInstance implements Comparable {

	protected Set<SimpleIngredientInstance> simpleContents;
	protected Set<ComplexIngredientInstance> complexContents;
	public ComplexIngredientInstance(ComplexIngredientInstance complexIngredientInstance) {
		super(complexIngredientInstance);
		this.simpleContents = new TreeSet<SimpleIngredientInstance>(complexIngredientInstance.simpleContents);
		this.complexContents = new TreeSet<ComplexIngredientInstance>(complexIngredientInstance.complexContents);
	}

	public ComplexIngredientInstance(IngredientClass ingredientClass, String name) {
		super(ingredientClass, name);
		this.simpleContents = new TreeSet<SimpleIngredientInstance>();
		this.complexContents = new TreeSet<ComplexIngredientInstance>();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();

		result = prime * result
				+ ((this.simpleContents == null) ? 0 : this.simpleContents.hashCode());
		result = prime * result
				+ ((this.complexContents == null) ? 0 : this.complexContents.hashCode());
		return result;
	}	
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		//if (!super.equals(obj))
		//	return false;
		if (!(obj instanceof ComplexIngredientInstance))
			return false;
		ComplexIngredientInstance other = (ComplexIngredientInstance) obj;
		if (simpleContents == null) {
			if (other.simpleContents != null)
				return false;
		} 
		
		List<SimpleIngredientInstance> thisSimple = new ArrayList<SimpleIngredientInstance>(this.simpleContents);
		List<SimpleIngredientInstance> thatSimple = new ArrayList<SimpleIngredientInstance>(other.simpleContents);
		List<ComplexIngredientInstance> thisComplex = new ArrayList<ComplexIngredientInstance>(this.complexContents);
		List<ComplexIngredientInstance> thatComplex = new ArrayList<ComplexIngredientInstance>(other.complexContents);
		
		if (other.simpleContents.size() != this.simpleContents.size())
		{
			return false;
		}
		
		for (IngredientInstance simpleIngredient : thisSimple)
		{
			if (!thatSimple.contains(simpleIngredient))
			{
				return false;
			}
		}
		for (IngredientInstance simpleIngredient : thatSimple)
		{
			if (!thisSimple.contains(simpleIngredient)){
				return false;
			}
		}
		for (IngredientInstance complexIngredient : thisComplex)
		{
			if (!thatComplex.contains(complexIngredient))
			{
				return false;
			}
		}
		for (IngredientInstance complexIngredient : thatComplex)
		{
			if (!thisComplex.contains(complexIngredient))
			{
				return false;
			}
		}

		return true;
	}


	@Override
	public Boolean contains(IngredientInstance subIngredient) {
		// If they are equal, this is easy
		if (this.equals(subIngredient))
		{
			return true;
		}
		
		// If we're dealing with a complex ingredient, we have to check the contents
		if (subIngredient instanceof ComplexIngredientInstance)
		{
			ComplexIngredientInstance other = (ComplexIngredientInstance) subIngredient;
			List<SimpleIngredientInstance> thisSimple = new ArrayList<SimpleIngredientInstance>(this.simpleContents);
			List<SimpleIngredientInstance> thatSimple = new ArrayList<SimpleIngredientInstance>(other.simpleContents);
			List<ComplexIngredientInstance> thisComplex = new ArrayList<ComplexIngredientInstance>(this.complexContents);
			List<ComplexIngredientInstance> thatComplex = new ArrayList<ComplexIngredientInstance>(other.complexContents);
			Boolean containsAll = true;

			for (IngredientInstance simpleIngredient : thatSimple)
			{
				if (!thisSimple.contains(simpleIngredient)){
					containsAll = false;
				}
			}
			
			for (IngredientInstance complexIngredient : thatComplex)
			{
				if (!thisComplex.contains(complexIngredient))
				{
					containsAll = false;
				}
			}
			if (containsAll == true)
			{
				return true;
			}

		}
		
		for (IngredientInstance ingredientInstance : this.simpleContents)
		{
			if (ingredientInstance.contains(subIngredient))
			{
				return true;
			}
		}
		for (IngredientInstance ingredientInstance : this.complexContents)
		{
			if (ingredientInstance.contains(subIngredient))
			{
				return true;
			}
		}
		
		return false;
	}

	@Override
	public Boolean terminate(IngredientInstance goal) {
		// TODO Auto-generated method stub
		return null;
	}
	
	@Override
	public int compareTo(Object arg0)
	{
		int thisHash = this.hashCode();
		int otherHash = arg0.hashCode();
		return Integer.compare(thisHash, otherHash);
	}
}
