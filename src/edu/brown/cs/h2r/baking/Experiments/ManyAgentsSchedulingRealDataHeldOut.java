package edu.brown.cs.h2r.baking.Experiments;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.ObjectHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.SADomain;
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
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.Knowledgebase.Knowledgebase;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.MakeSpanFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ToolFactory;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeBotched;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;
import edu.brown.cs.h2r.baking.Scheduling.SchedulingHelper;
import edu.brown.cs.h2r.baking.actions.GreaseAction;
import edu.brown.cs.h2r.baking.actions.MixAction;
import edu.brown.cs.h2r.baking.actions.MoveAction;
import edu.brown.cs.h2r.baking.actions.PourAction;
import edu.brown.cs.h2r.baking.actions.ResetAction;
import edu.brown.cs.h2r.baking.actions.SwitchAction;
import edu.brown.cs.h2r.baking.actions.UseAction;

public class ManyAgentsSchedulingRealDataHeldOut {
	private static StateHashFactory hashingFactory = new NameDependentStateHashFactory();
	
	
	public static void main(String[] args) {
		
		int numTrials = 1	;
		Integer trialId = Math.abs(new Random().nextInt());
		String saveFile = null;
		if (args.length > 0) {
			saveFile = args[0];
		}
		if (args.length > 1) {
			trialId = Integer.parseInt(args[1]);
		} 
		Domain generalDomain = SimulationHelper.generateGeneralDomain(); 
		Knowledgebase knowledgebase = Knowledgebase.getKnowledgebase(generalDomain);
		int numberOfRecipes = 1 + trialId / (7 * 50);
		List<Recipe> recipes = AgentHelper.breakfastRecipes(generalDomain);
		//List<Recipe> recipes = Recipe.generateRecipes(generalDomain, 5 * numberOfRecipes, knowledgebase.getIngredientList(), 1, 4);
		
		knowledgebase.initKnowledgebase(recipes);
		Map<String, Double> factors = new HashMap<String, Double>();
		factors.put("human", 1.0);
		
		ActionTimeGenerator timeGenerator = new ActionTimeGenerator(true, true);
		Human human = new Expert(generalDomain, "human", timeGenerator, recipes);
		
		State state = SimulationHelper.generateInitialState(generalDomain, hashingFactory, recipes, human, null);
		
		
		List<Agent> agents = Arrays.asList(
				(Agent)new RandomActionAgent(generalDomain),
				//(Agent)new RandomSubgoalAgent(generalDomain, "partner", timeGenerator),
				//(Agent)new RandomActionCorrectRecipeAgent(generalDomain, "partner", timeGenerator),
				//(Agent)new RandomActionCorrectSubgoal(generalDomain, "partner", timeGenerator),
				(Agent)new RandomRecipeAgent(generalDomain,"partner", timeGenerator, recipes),
				(Agent)new Human(generalDomain, "partner", timeGenerator, recipes),
				(Agent)new Expert(generalDomain, "partner", timeGenerator, recipes),
				(Agent)new AdaptiveByFlow(generalDomain, timeGenerator, recipes, false),
				(Agent)new AdaptiveByFlow(generalDomain, timeGenerator, recipes, true)
				);
		
		System.out.println("Agent, Successes, Trials, Average reward, average successful reward");
		ResetAction reset = (ResetAction)generalDomain.getAction(ResetAction.className);
		reset.setState(state);
		
		Path path = Paths.get(saveFile);
		if (true){
			int choice =  agents.size();//trialId % (agents.size() + 1);
			SimulationHelper.run(numTrials, generalDomain, hashingFactory, recipes, timeGenerator, human, agents,
					reset, choice, false, saveFile);	
		} else {
			SimulationHelper.runFromSaved(saveFile, generalDomain, hashingFactory, recipes, reset, false);	
		}
	}
}
