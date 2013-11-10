import java.util.List;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;


public class RecipeBotched extends PropositionalFunction {

	protected Recipe.Ingredient topLevelIngredient;
	public RecipeBotched(String name, Domain domain, Recipe.Ingredient ingredient) {
		super(name, domain, new String[] {IngredientClass.className});
		this.topLevelIngredient = ingredient;
	}

	@Override
	public boolean isTrue(State state, String[] params) {
		ObjectInstance ingredient = state.getObject(params[0]);
		return Recipe.isFailure(state, this.topLevelIngredient, ingredient);
	}

}
