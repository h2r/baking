package edu.brown.cs.h2r.baking;

import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.Domain;

import java.util.Set;

import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;


public class AffordancesApply extends PropositionalFunction {
	protected Set<String> traits;
	
	public AffordancesApply(String name,Domain domain, Set<String> traits) {
		super(name, domain, new String[] {IngredientFactory.ClassNameComplex});
		this.traits = traits;
		
	}
	public boolean isTrue(State s, String[] obj_traits) {
		// TODO Auto-generated method stub
		if (this.traits.size() == 0) {
			return true;
		}
		for (String curr_trait : obj_traits) {
			if (this.traits.contains(curr_trait)) {
				return true;
			}
		}
		/*if (obj_traits.length != 0) {
			String have = "";
			for (String t: obj_traits) {
				have = have + t + ",";
			}
			System.out.println("Want: "+this.traits+", but have: "+have);
		}*/
		return false;
	}
	
	public void changeTraits(Set<String> new_traits) {
		this.traits = new_traits;
	}

}
