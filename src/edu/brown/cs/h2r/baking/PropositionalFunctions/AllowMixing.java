package edu.brown.cs.h2r.baking.PropositionalFunctions;

import java.util.Map.Entry;

import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ToolFactory;
import edu.brown.cs.h2r.baking.actions.BakingActionResult;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public class AllowMixing extends BakingPropositionalFunction {

	public AllowMixing(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[]{AgentFactory.ClassName, ContainerFactory.ClassName, ToolFactory.ClassName}, ingredient) ;
	}
	
	public BakingPropositionalFunction updatePF(Domain newDomain, IngredientRecipe ingredient, BakingSubgoal subgoal) {
		return new AllowMixing(this.name, newDomain, ingredient);
	}
	@Override
	public boolean isTrue(State state, String[] params) {
		
		ObjectInstance containerInstance = state.getObject(params[1]);
		int contentAmount = ContainerFactory.getContentNames(containerInstance).size();
		if (ContainerFactory.getContentNames(containerInstance).isEmpty()) {
			return false;
		}
		
		int neededAmount =  this.topLevelIngredient.getContents().size() 
				+ this.topLevelIngredient.getNecessaryTraits().size();
		
		if (contentAmount < 2) {
			return false;
		}
		
		if (contentAmount != neededAmount) {
			return false;
		}
		
		for (String name : ContainerFactory.getContentNames(containerInstance)) {
			ObjectInstance ingObj = state.getObject(name);
			IngredientRecipe ingredient = null;
			for (IngredientRecipe i : this.topLevelIngredient.getContents()) {
				if (i.getFullName().equals(name) || i.isMatching(ingObj, state)) {
					ingredient = i;
					break;
				}
			}
			
			if (ingredient == null) {
				for (Entry<String, IngredientRecipe> entry : this.topLevelIngredient.getNecessaryTraits().entrySet()) {
					if (IngredientFactory.getTraits(ingObj).contains(entry.getKey())) {
						ingredient = entry.getValue();
						break;
					}
				}
				if (ingredient == null) {
					return false;
				}
			}
		}
		
		String toolName = params[2];
		ObjectInstance tool = state.getObject(toolName);
		if (!ToolFactory.getSpaceName(tool).equals(SpaceFactory.SPACE_COUNTER)) {
			return false;
		}
		
		
		return true;
	}

}
