package edu.brown.cs.h2r.baking.Experiments;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.Agents.AdaptiveByFlow;
import edu.brown.cs.h2r.baking.Agents.Agent;
import edu.brown.cs.h2r.baking.Agents.AgentHelper;
import edu.brown.cs.h2r.baking.Agents.Expert;
import edu.brown.cs.h2r.baking.Agents.Human;
import edu.brown.cs.h2r.baking.Agents.RandomActionAgent;
import edu.brown.cs.h2r.baking.Agents.RandomRecipeAgent;
import edu.brown.cs.h2r.baking.Knowledgebase.Knowledgebase;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;
import edu.brown.cs.h2r.baking.actions.ResetAction;

public class ManyAgentsSchedulingSubgoals {
	private static StateHashFactory hashingFactory = new NameDependentStateHashFactory();
	
	
	public static void main(String[] args) {
		
		int numTrials = 100;
		int trialId = new Random().nextInt();
		trialId = Math.abs(trialId);
		if (args.length == 1) {
			trialId = Integer.parseInt(args[0]);
		} /*else {
			System.err.println("Args provided: "  + Arrays.toString(args));
			System.err.println("Usage TestManyAgents numTrials trialId");
			System.exit(0);
		}	*/
		
		Domain generalDomain = SimulationHelper.generateGeneralDomain(); 
		
		List<Recipe> recipes = AgentHelper.allRecipes(generalDomain);
		Knowledgebase knowledgebase = Knowledgebase.getKnowledgebase(generalDomain);
		knowledgebase.initKnowledgebase(recipes);
		Map<String, Double> factors = new HashMap<String, Double>();
		factors.put("human", 1.0);
		
		ActionTimeGenerator timeGenerator = new ActionTimeGenerator(factors);
		Expert human = new Expert(generalDomain, "human", timeGenerator, recipes);
		
		State state = SimulationHelper.generateInitialState(generalDomain, hashingFactory, recipes, human, null);
		/*for (Recipe recipe : recipes) {
			//System.out.println("Testing recipe " + recipe.toString());
			ExperimentHelper.testRecipeExecution(generalDomain, state, recipe);
			//System.out.println("\n\n");
		}*/
		
		//System.exit(0);
		
		
		
		List<Agent> agents = Arrays.asList(
				(Agent)new RandomActionAgent(generalDomain),
				(Agent)new RandomRecipeAgent(generalDomain,"partner", timeGenerator, recipes),
				(Agent)new Human(generalDomain, "partner", timeGenerator, recipes),
				(Agent)new Expert(generalDomain, "partner", timeGenerator, recipes),
				(Agent)new AdaptiveByFlow(generalDomain, false, timeGenerator, recipes, false),
				(Agent)new AdaptiveByFlow(generalDomain, false, timeGenerator, recipes, true)
				);
		System.out.println("Agent, Successes, Trials, Average reward, average successful reward");
		ResetAction reset = (ResetAction)generalDomain.getAction(ResetAction.className);
		reset.setState(state);
		
		
			//System.out.println("Agent: " + agent.getAgentName());
		//Agent agent = agents.get(3);
		int choice = trialId % (agents.size() + 1);
		SimulationHelper.run(numTrials, generalDomain, hashingFactory, recipes, timeGenerator, human, agents,
				reset, choice, true, null);	
	}
}
