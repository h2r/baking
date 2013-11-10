import java.util.List;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;


public class RecipeFinished extends PropositionalFunction {
	
	protected Recipe.Ingredient topLevelIngredient;
	public RecipeFinished(String name, Domain domain, Recipe.Ingredient ingredient) {
		super(name, domain, new String[] {Recipe.ComplexIngredient.className});
		this.topLevelIngredient = ingredient;
	}

	@Override
	public boolean isTrue(State state, String[] params) {
		ObjectInstance ingredient = state.getObject(params[0]);
		return Recipe.isSuccess(state, this.topLevelIngredient, ingredient);
	}

}
