package edu.brown.cs.h2r.baking.Experiments;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.Domain;
import edu.brown.cs.h2r.baking.Agents.AdaptiveByFlow;
import edu.brown.cs.h2r.baking.Agents.Agent;
import edu.brown.cs.h2r.baking.Agents.Expert;
import edu.brown.cs.h2r.baking.Agents.Human;
import edu.brown.cs.h2r.baking.Knowledgebase.Knowledgebase;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;

public class ManyAgentsSchedulingRealDataHeldOutManyRecipes {
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
		boolean useRobots = true;
		if (args.length > 2 && args[2].equals("robots")) {
			useRobots = true;
		}
		boolean useShelf = true;
		if (args.length > 3 && args[3].equals("shelf")) {
			useShelf = true;
		}
		
		int numberRecipes = 10 + trialId % 17;
		
		Domain generalDomain = SimulationHelper.generateGeneralDomain(); 
		Knowledgebase knowledgebase = Knowledgebase.getKnowledgebase(generalDomain);
		List<Recipe> allRecipes = Recipe.generateRecipes(generalDomain, numberRecipes, knowledgebase.getIngredientList(), 1, 4);
		//List<Recipe> recipes = Recipe.generateRecipes(generalDomain, 5 * numberOfRecipes, knowledgebase.getIngredientList(), 1, 4);
		
		Random random = new Random();
		//for (int i = 0; i < 10; i++) {
		//for (Recipe recipe : allRecipes) {
		List<Recipe> recipes = new ArrayList<Recipe>();
		recipes.add(allRecipes.get(random.nextInt(allRecipes.size())));
		// TODO, when agent does something unhelpful, human ignores it, and continues on. It should decide to be uncooperative when the robot didn't help
		//recipes.add(allRecipes.get(2));
		//recipes.add(recipe);
		
		knowledgebase.initKnowledgebase(allRecipes);
		Map<String, Double> factors = new HashMap<String, Double>();
		factors.put("human", 1.0);
		
		ActionTimeGenerator timeGenerator = new ActionTimeGenerator(true, true);
		Human human = new Expert(generalDomain, "human", timeGenerator, recipes);
		
		
		
		List<Agent> agents = Arrays.asList(
				//(Agent)new RandomActionAgent(generalDomain),
				//(Agent)new RandomRecipeAgent(generalDomain, "partner", timeGenerator, allRecipes),
				(Agent)new Expert(generalDomain, "partner", useRobots, timeGenerator, recipes),
				(Agent)new AdaptiveByFlow(generalDomain, useRobots, timeGenerator, allRecipes, true)
				);
		
		System.out.println("Agent, Successes, Trials, Average reward, average successful reward");
		
		Path path = Paths.get(saveFile);
		if (true){
			int choice = 2;//trialId % (agents.size() + 1);
			
			SimulationHelper.run(numTrials, generalDomain, hashingFactory, allRecipes, timeGenerator, human, agents,
				 choice, false, saveFile, useShelf, false);	
		} else {
			SimulationHelper.runFromSaved(saveFile, generalDomain, hashingFactory, allRecipes, agents, false);	
		}
		//}
		//}
	}
}