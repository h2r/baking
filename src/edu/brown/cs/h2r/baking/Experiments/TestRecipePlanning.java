package edu.brown.cs.h2r.baking.Experiments;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import edu.brown.cs.h2r.baking.Agents.AdaptiveAgent;
import edu.brown.cs.h2r.baking.Agents.AgentHelper;
import edu.brown.cs.h2r.baking.Agents.Human;
import edu.brown.cs.h2r.baking.Knowledgebase.Knowledgebase;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;
import edu.brown.cs.h2r.baking.actions.ResetAction;

public class TestRecipePlanning {
	private static StateHashFactory hashingFactory = new NameDependentStateHashFactory();
	protected final static RewardFunction rewardFunction = new RewardFunction() {
		
		@Override
		public double reward(State s, GroundedAction a, State sprime) {
			// TODO Auto-generated method stub
			return (a.action instanceof ResetAction) ? -2 : -1;
		}
		
	};
	public static void main(String[] args) {
		Domain generalDomain = SimulationHelper.generateGeneralDomain(); 
		
		List<Recipe> recipes = AgentHelper.recipes(generalDomain);
		Knowledgebase knowledgebase = Knowledgebase.getKnowledgebase(generalDomain);
		knowledgebase.initKnowledgebase(recipes);
		Map<String, Double> factors = new HashMap<String, Double>();
		factors.put("human", 1.0);
		
		ActionTimeGenerator timeGenerator = new ActionTimeGenerator(factors);
		Human human = new Human(generalDomain, timeGenerator);
		
		State state = SimulationHelper.generateInitialState(generalDomain, hashingFactory, recipes, human, null);
		for (Recipe recipe : recipes) {
			System.out.println("Testing recipe " + recipe.toString());
			ExperimentHelper.testRecipeExecution(generalDomain, state, recipe, true, true, false);
			System.out.println("\n\n");
		}
		
		ResetAction reset = (ResetAction)generalDomain.getAction(ResetAction.className);
		reset.setState(state);
		
		List<KitchenSubdomain> plannedRecipes = 
				AgentHelper.generateAllRTDPPoliciesParallel(generalDomain, state, AgentHelper.recipes(generalDomain),
				TestRecipePlanning.rewardFunction ,TestRecipePlanning.hashingFactory);

		for (KitchenSubdomain subdomain : plannedRecipes) {
			ExperimentHelper.printPlan(subdomain, TestRecipePlanning.rewardFunction);
		}
	}

}
