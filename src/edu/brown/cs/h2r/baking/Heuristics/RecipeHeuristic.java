package edu.brown.cs.h2r.baking.Heuristics;

import java.util.Set;

import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import burlap.behavior.singleagent.planning.deterministic.informed.Heuristic;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public class RecipeHeuristic implements Heuristic {
	@Override
	public double h(State state) {
		return 0;
		//List<ObjectInstance> objects = state.getObjectsOfTrueClass(Recipe.ComplexIngredient.className);
		//double max = 0;
		//for (ObjectInstance object : objects)
		//{
		//	max = Math.max(max, this.getSubIngredients(state, object));
		//}
		//return numSteps - max;
	}
	
	public int getSubIngredients(State state, ObjectInstance object)
	{
		int count = 0;
		count += IngredientFactory.isBakedIngredient(object) ? 1 : 0;
		count += IngredientFactory.isMixedIngredient(object) ? 1 : 0;
		count += IngredientFactory.isMeltedIngredient(object) ? 1 : 0; 
		
		if (IngredientFactory.isSimple(object))
		{
			return count;
		}
		Set<String> contents = IngredientFactory.getContentsForIngredient(object);
		for (String str: contents)
		{
			count += this.getSubIngredients(state, state.getObject(str));
		}
		return count;
	}

}
