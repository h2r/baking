package edu.brown.cs.h2r.baking.Heuristics;

import java.util.Set;
import java.util.HashMap;

import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import burlap.behavior.singleagent.planning.deterministic.informed.Heuristic;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public class RecipeHeuristic implements Heuristic {
	private HashMap<String, Boolean> affordances;
	private Boolean has_affordances;
	public RecipeHeuristic() {
		this.affordances = null;
		this.has_affordances = false;
	}
	//@Override
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
		count += IngredientFactory.isHeatedIngredient(object) ? 1 : 0; 
		
		if (IngredientFactory.isSimple(object))
		{
			return count;
		}
		Set<String> contents = IngredientFactory.getIngredientContents(object);
		for (String str: contents)
		{
			count += this.getSubIngredients(state, state.getObject(str));
		}
		return count;
	}
	
	public void setAffordances(HashMap<String, Boolean> aff) {
		this.affordances = aff;
		this.has_affordances = true;
	}
	
	public Boolean has_affordances() {
		return this.has_affordances;
	}
	
	public HashMap<String,Boolean> get_affordances() {
		return this.affordances;
	}

}
