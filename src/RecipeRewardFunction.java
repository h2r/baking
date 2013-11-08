import java.util.List;

import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

public class RecipeRewardFunction implements RewardFunction {

	protected ComplexIngredientInstance goal;
	public RecipeRewardFunction(Recipe recipeGoal) {
		this.goal = recipeGoal;
	}

	@Override
	public double reward(State s, GroundedAction a, State sprime) {
		
		List<ObjectInstance> objects = s.getObjectsOfTrueClass(ContainerClass.className);
		for (ObjectInstance obj : objects)
		{
			ContainerInstance container = (ContainerInstance)obj;
			if (container.contents.size() == 1)
			{
				if (container.contents.get(0) instanceof ComplexIngredientInstance)
				{
					if (this.goal.equals(container.contents.get(0))) 
					{
						return 0;
					}
				}
			}
			else
			{
				for (IngredientInstance ingredientInstance : container.contents)
				{
					// This ingredient is not part of the recipe, abort!
					if (!this.goal.contains(ingredientInstance))
					{
						return -1;
					}
				}
			}
		}
		// Things look ok...for now. Continue!
		return -1;
	}
}
