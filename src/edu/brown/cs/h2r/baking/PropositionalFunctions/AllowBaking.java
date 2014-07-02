package edu.brown.cs.h2r.baking.PropositionalFunctions;

import java.util.AbstractMap;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public class AllowBaking extends BakingPropositionalFunction {

	public AllowBaking(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[] {AgentFactory.ClassName, IngredientFactory.ClassNameComplex}, ingredient);
	}
	@Override
	public boolean isTrue(State s, String[] params) {
		// TODO Auto-generated method stub
		if (!params[0].equalsIgnoreCase("")) {
			ObjectInstance toBake = s.getObject(params[1]);
			if (this.topLevelIngredient.getName().equals(toBake.getName())) {
				return (topLevelIngredient.getBaked() && !IngredientFactory.isBakedIngredient(toBake));
			}
			for (IngredientRecipe content : this.topLevelIngredient.getConstituentIngredients()) {
				if (content.getName().equals(toBake.getName())) {
					// If it is, then make sure it needs to be peeled in the first place
					return content.getBaked();
				}
			}
			return false;
		} else {
			List<IngredientRecipe> contents = this.topLevelIngredient.getContents();
			for (IngredientRecipe content : contents) {
				if (content.getBaked()) {
					return true;
				}
			}
			AbstractMap<String, IngredientRecipe> necessaryTraits = this.topLevelIngredient.getNecessaryTraits();
			for (String trait : necessaryTraits.keySet()) {
				if (necessaryTraits.get(trait).getBaked()) {
					return true;
				}
			}
		}
		return false;
	}
}
