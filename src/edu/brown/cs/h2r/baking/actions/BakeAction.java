package edu.brown.cs.h2r.baking.actions;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;


public class BakeAction extends BakingAction {
	public static final String className = "bake";
	public BakeAction(Domain domain, IngredientRecipe ingredient) {
		super(BakeAction.className, domain, ingredient, new String[] {AgentFactory.ClassName, IngredientFactory.ClassNameComplex});
	}
	
	@Override
	public boolean applicableInState(State s, String[] params) {
		if (!super.applicableInState(s, params)) {
			return false;
		}
		return true;
	}

	@Override
	protected State performActionHelper(State state, String[] params) {
		super.performActionHelper(state, params);
		this.bake(state, this.domain);
		//System.out.println("Bake!");
		return state;
	}
	
	public void bake(State state, Domain domain)
	{
	}
}
