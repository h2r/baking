package edu.brown.cs.h2r.baking.Experiments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.Agents.AgentHelper;
import edu.brown.cs.h2r.baking.Agents.Expert;
import edu.brown.cs.h2r.baking.Agents.Human;
import edu.brown.cs.h2r.baking.Knowledgebase.Knowledgebase;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;
import edu.brown.cs.h2r.baking.Scheduling.Workflow;

public class WorkflowSortingTesting {

	public WorkflowSortingTesting() {
		// TODO Auto-generated constructor stub
	}
	
	public boolean test(List<AbstractGroundedAction> list, State state) {
		Workflow workflow = Workflow.buildWorkflow(state, list);
		for (int i = 0; i < 10; i++) {
			workflow.sort();
		}
		return true;
	}
	
	public static List<List<String>> testCases() {
		List<List<String>> lists= new ArrayList<List<String>>();
		List<String> list = Arrays.asList("move [human, cocoa_bowl, counter], " +
				"pour [human, mixing_bowl_1, trash], " + 
				"move [partner, milk_bowl, counter]," +
				"pour [human, milk_bowl, mixing_bowl_1]," +
				"pour [human, cocoa_bowl, mixing_bowl_1]",   
				"move [human, cocoa_bowl, counter]," + 
				"pour [human, cocoa_bowl, mixing_bowl_1]," + 
				"pour [human, mixing_bowl_1, trash]," + 
				"move [partner, milk_bowl, counter]," + 
				"pour [human, milk_bowl, mixing_bowl_1],");
		lists.add(list);
		return lists;
	}
	
	public List<List<AbstractGroundedAction>> loadTestCase(List<String> testCase, Domain domain) {
		List<List<AbstractGroundedAction>> actions = new ArrayList<List<AbstractGroundedAction>>();
		
		for (String s : testCase) {
			actions.add(this.loadString(s, domain));
		}
		
		return actions;
	}
	
	public List<AbstractGroundedAction> loadString(String str, Domain domain) {
		List<String> split = Arrays.asList(str.split("], "));
		List<AbstractGroundedAction> actions = new ArrayList<AbstractGroundedAction>();
		for (String s : split) {
			s = s.replace("[", "").replace(",", "");
			String[] arry =  s.split(" ");
			String actionName = arry[0];
			String[] params = Arrays.copyOfRange(arry, 1, arry.length);
			Action action = domain.getAction(actionName);
			actions.add(new GroundedAction(action, params));
		}
				
		return actions;
	}

	public static void main(String[] args) {
		Domain domain = SimulationHelper.generateGeneralDomain();
		Knowledgebase knowledgebase = Knowledgebase.getKnowledgebase(domain);
		List<Recipe> allRecipes = AgentHelper.breakfastRecipes(domain);
		ActionTimeGenerator timeGenerator = new ActionTimeGenerator(true, true);
		Human human = new Expert(domain, "human", timeGenerator, allRecipes);
		StateHashFactory hashingFactory = new NameDependentStateHashFactory();
		
		State state = SimulationHelper.generateInitialState(domain, hashingFactory, allRecipes, human, null);
		WorkflowSortingTesting testing = new WorkflowSortingTesting();
		List<List<String>> testCases = testCases();
		for (List<String> test : testCases) {
			List<List<AbstractGroundedAction>> actions = testing.loadTestCase(test, domain);
			if (!testing.test(actions.get(0), state));
		}
		
		
	}
}
