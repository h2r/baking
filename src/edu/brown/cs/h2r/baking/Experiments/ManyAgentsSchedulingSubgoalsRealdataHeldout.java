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
import edu.brown.cs.h2r.baking.Agents.RandomActionCorrectRecipeAgent;
import edu.brown.cs.h2r.baking.Agents.RandomActionCorrectSubgoal;
import edu.brown.cs.h2r.baking.Agents.RandomRecipeAgent;
import edu.brown.cs.h2r.baking.Agents.RandomSubgoalAgent;
import edu.brown.cs.h2r.baking.Knowledgebase.Knowledgebase;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;
import edu.brown.cs.h2r.baking.actions.ResetAction;

public class ManyAgentsSchedulingSubgoalsRealdataHeldout {
	private static StateHashFactory hashingFactory = new NameDependentStateHashFactory();
	
	
	public static void main(String[] args) {
		
		int numTrials = 1;
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
		
		List<Recipe> recipes = AgentHelper.recipes(generalDomain);
		Knowledgebase knowledgebase = Knowledgebase.getKnowledgebase(generalDomain);
		knowledgebase.initKnowledgebase(recipes);
		Map<String, Double> factors = new HashMap<String, Double>();
		factors.put("human", 1.0);
		
		ActionTimeGenerator timeGenerator = new ActionTimeGenerator(true, true);
		Human human = new Expert(generalDomain, "human", timeGenerator);
		
		State state = SimulationHelper.generateInitialState(generalDomain, hashingFactory, recipes, human, null);
		/*for (Recipe recipe : recipes) {
			//System.out.println("Testing recipe " + recipe.toString());
			ExperimentHelper.testRecipeExecution(generalDomain, state, recipe);
			//System.out.println("\n\n");
		}*/
		
		//System.exit(0);
		
		
		
		List<Agent> agents = Arrays.asList(
				(Agent)new RandomActionAgent(generalDomain), //0
				(Agent)new RandomSubgoalAgent(generalDomain, "partner", timeGenerator),
				(Agent)new RandomActionCorrectRecipeAgent(generalDomain, "partner", timeGenerator),
				(Agent)new RandomActionCorrectSubgoal(generalDomain, "partner", timeGenerator),
				(Agent)new RandomRecipeAgent(generalDomain, timeGenerator),
				(Agent)new Human(generalDomain, "partner", timeGenerator),//5
				(Agent)new Expert(generalDomain, "partner", timeGenerator),
				(Agent)new AdaptiveByFlow(generalDomain, timeGenerator, false),
				(Agent)new AdaptiveByFlow(generalDomain, timeGenerator, true)
				);
		
		System.out.println("Agent, Successes, Trials, Average reward, average successful reward");
		ResetAction reset = (ResetAction)generalDomain.getAction(ResetAction.className);
		reset.setState(state);
		
		
			//System.out.println("Agent: " + agent.getAgentName());
		for (int i = 0; i < 100; i++) {
			trialId = Math.abs(new Random().nextInt());
			int choice = 7;//trialId % (agents.size() + 1);
			
			SimulationHelper.run(numTrials, generalDomain, hashingFactory, recipes, timeGenerator, human, agents,
					reset, choice, true);
		}
			
	}


	
}
