import java.util.List;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;


public class RecipeBotched extends PropositionalFunction {

	protected Recipe recipe;
	public RecipeBotched(String name, Domain domain, Recipe recipe) {
		super(name, domain, new String[] {IngredientClass.className});
		this.recipe = recipe;
	}

	@Override
	public boolean isTrue(State state, String[] params) {
		ObjectInstance ingredient = state.getObject(params[0]);
		return this.recipe.isFailure(state, ingredient);
	}

}
