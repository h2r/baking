import java.util.List;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;


public class RecipeFinished extends PropositionalFunction {
	
	protected Recipe recipe;
	public RecipeFinished(String name, Domain domain, Recipe recipe) {
		super(name, domain, new String[] {Recipe.ComplexIngredient.className});
		this.recipe = recipe;
	}

	@Override
	public boolean isTrue(State state, String[] params) {
		ObjectInstance ingredient = state.getObject(params[0]);
		return this.recipe.isSuccess(state, ingredient);
	}

}
