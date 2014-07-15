package edu.brown.cs.h2r.baking.Heuristics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import burlap.behavior.singleagent.planning.deterministic.informed.Heuristic;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;

public class AgentSpecificHeuristic implements Heuristic {
	private String agent;
	private IngredientRecipe recipe;
	private List<String> recipeIngredients;
	private double costMe;
	private double costYou;
	public AgentSpecificHeuristic(Domain domain, IngredientRecipe recipe, String agent)
	{
		this.agent = agent;
		this.recipe = recipe;
		List<IngredientRecipe> ingredients = recipe.getConstituentIngredients();
		this.recipeIngredients = new ArrayList<String>();
		for (IngredientRecipe ingredient : ingredients)
		{
			this.recipeIngredients.add(ingredient.getName());
		}
		this.costMe = -1;
		this.costYou = -2;
	}
	
	public AgentSpecificHeuristic(Domain domain, IngredientRecipe recipe, String agent, double costMe, double costYou)
	{
		this.agent = agent;
		this.recipe = recipe;
		List<IngredientRecipe> ingredients = recipe.getConstituentIngredients();
		this.recipeIngredients = new ArrayList<String>();
		for (IngredientRecipe ingredient : ingredients)
		{
			this.recipeIngredients.add(ingredient.getName());
		}
		this.costMe = costMe;
		this.costYou = costYou;
	}
	
	@Override
	public double h(State state) {
		int maxIngredients = 0;
		List<ObjectInstance> containers = state.getObjectsOfTrueClass(ContainerFactory.ClassName);
		for (ObjectInstance container : containers)
		{
			Set<String> contents = ContainerFactory.getContentNames(container);
			Set<String> contentsCopy = new HashSet<String>(contents);
			contents.removeAll(this.recipeIngredients);
			contentsCopy.removeAll(contents);
			if (contentsCopy.size() > maxIngredients)
			{
				maxIngredients = contentsCopy.size();
			}
		}
		int stepsRemaining = this.recipe.getConstituentIngredientsCount() - maxIngredients;
		double costYou = Math.floor(stepsRemaining/2) * this.costYou;
		double costMe = Math.ceil(stepsRemaining/2) * this.costMe;
		return (costYou + costMe);
		
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

}
