import java.util.List;

import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;

public class RecipeTerminalFunction implements TerminalFunction{

	protected ComplexIngredientInstance goal;
	
	public RecipeTerminalFunction(Recipe recipeGoal) {
		this.goal = recipeGoal;
	}

	@Override
	public boolean isTerminal(State s) {
		List<ObjectInstance> objects = s.getObjectsOfTrueClass(ContainerClass.className);
		for (ObjectInstance obj : objects)
		{
			ContainerInstance container = (ContainerInstance)obj;
			if (container.contents.size() == 1)
			{
				if (container.contents.get(0).equals(this.goal))
				{
					// We're done!
					return true;
				}
			}
			else
			{
				for (IngredientInstance ingredientInstance : container.contents)
				{
					// This doesn't look like our goal, abort!
					if (!this.goal.contains(ingredientInstance))
					{
						return true;
					}
				}
			}
		}
		// Things still look fine, continue
		return false;
	}

}
