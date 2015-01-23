package edu.brown.cs.h2r.baking.Experiments;

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
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import edu.brown.cs.h2r.baking.Agents.Agent;
import edu.brown.cs.h2r.baking.Agents.AgentHelper;
import edu.brown.cs.h2r.baking.Agents.Human;
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
import edu.brown.cs.h2r.baking.Scheduling.Assignment;
import edu.brown.cs.h2r.baking.Scheduling.Assignment.ActionTime;
import edu.brown.cs.h2r.baking.Scheduling.ExhaustiveScheduler;
import edu.brown.cs.h2r.baking.Scheduling.GreedyScheduler;
import edu.brown.cs.h2r.baking.Scheduling.RandomScheduler;
import edu.brown.cs.h2r.baking.Scheduling.Scheduler;
import edu.brown.cs.h2r.baking.Scheduling.WeightByDifference;
import edu.brown.cs.h2r.baking.Scheduling.WeightByShortest;
import edu.brown.cs.h2r.baking.Scheduling.Workflow;
import edu.brown.cs.h2r.baking.Scheduling.Workflow.Node;
import edu.brown.cs.h2r.baking.actions.GreaseAction;
import edu.brown.cs.h2r.baking.actions.MixAction;
import edu.brown.cs.h2r.baking.actions.MoveAction;
import edu.brown.cs.h2r.baking.actions.PourAction;
import edu.brown.cs.h2r.baking.actions.ResetAction;
import edu.brown.cs.h2r.baking.actions.SwitchAction;
import edu.brown.cs.h2r.baking.actions.UseAction;

public class RecipeScheduling {
	private final static StateHashFactory hashingFactory = new NameDependentStateHashFactory();
	private final static RewardFunction rewardFunction = new RewardFunction() {

		@Override
		public double reward(State s, GroundedAction a, State sprime) {
			return (a.action instanceof ResetAction) ? -2 : -1;
		}
		
	};
	
	public static Domain generateGeneralDomain() {
		Domain domain = new SADomain();
		domain.addObjectClass(ContainerFactory.createObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createSimpleIngredientObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createComplexIngredientObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createSimpleHiddenIngredientObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createComplexHiddenIngredientObjectClass(domain));
		domain.addObjectClass(SpaceFactory.createObjectClass(domain));		
		domain.addObjectClass(AgentFactory.getObjectClass(domain));
		domain.addObjectClass(ToolFactory.createObjectClass(domain));
		domain.addObjectClass(MakeSpanFactory.getObjectClass(domain));
		domain.setObjectIdentiferDependence(true);
		
		Action mix = new MixAction(domain);
		Action pour = new PourAction(domain);
		Action move = new MoveAction(domain);
		Action grease = new GreaseAction(domain);
		Action aSwitch = new SwitchAction(domain);
		Action use = new UseAction(domain);
		Action resetAction = new ResetAction(domain);
		
		
		
		// To the failed propFunction, add in all subgoals for a recipe that are based on an ingredient.
		RecipeBotched failed = ((RecipeBotched)domain.getPropFunction(AffordanceCreator.BOTCHED_PF));
		if (failed != null) {
			failed.clearSubgoals();
		}
		
		return domain;
	}
	
	public static State generateInitialState(Domain generalDomain, List<Recipe> recipes, Agent agent1, Agent agent2) {
		ObjectHashFactory objectHashingFactory = hashingFactory.getObjectHashFactory();
		List<ObjectInstance> objects = new ArrayList<ObjectInstance>();
		objects.add(agent1.getAgentObject());
		
		if (agent2 != null) {
			objects.add(agent2.getAgentObject());
		}
		ObjectInstance makeSpan = MakeSpanFactory.getNewObjectInstance(generalDomain, "makespan", 2, objectHashingFactory);
		objects.add(makeSpan);
		
		List<String> containers = Arrays.asList("mixing_bowl_1", "mixing_bowl_2", "mixing_bowl_3", "baking_dish", "melting_pot");
		ObjectInstance counterSpace = SpaceFactory.getNewWorkingSpaceObjectInstance(generalDomain, SpaceFactory.SPACE_COUNTER, containers, "human", objectHashingFactory);
		objects.add(counterSpace);
		
		objects.add(ToolFactory.getNewSimpleToolObjectInstance(generalDomain, "whisk", "", "", SpaceFactory.SPACE_COUNTER, objectHashingFactory));
		objects.add(ToolFactory.getNewSimpleToolObjectInstance(generalDomain, "spoon","", "", SpaceFactory.SPACE_COUNTER, objectHashingFactory));
		
		objects.add(ContainerFactory.getNewBakingContainerObjectInstance(generalDomain, "baking_dish", null, SpaceFactory.SPACE_COUNTER, objectHashingFactory));
		objects.add(ContainerFactory.getNewHeatingContainerObjectInstance(generalDomain, "melting_pot", null, SpaceFactory.SPACE_COUNTER, objectHashingFactory));
		objects.add(SpaceFactory.getNewBakingSpaceObjectInstance(generalDomain, SpaceFactory.SPACE_OVEN, null, "", objectHashingFactory));
		objects.add(SpaceFactory.getNewHeatingSpaceObjectInstance(generalDomain, SpaceFactory.SPACE_STOVE, null, "", objectHashingFactory));
		//objects.add(ContainerFactory.getNewTrashContainerObjectInstance(generalDomain, "trash", SpaceFactory.SPACE_COUNTER, objectHashingFactory));
		
		for (String container : containers) { 
			objects.add(ContainerFactory.getNewMixingContainerObjectInstance(generalDomain, container, null, SpaceFactory.SPACE_COUNTER, objectHashingFactory));
		}
		
		// Out of all the ingredients in our kitchen, plan over only those that might be useful!
		Knowledgebase knowledgebase = Knowledgebase.getKnowledgebase(generalDomain);
		
		List<ObjectInstance> ingredients = new ArrayList<ObjectInstance>();
		List<ObjectInstance> tools = new ArrayList<ObjectInstance>();
		for (int i = 0;i < recipes.size(); i++) {
			Recipe recipe = recipes.get(i);
			ingredients.addAll(knowledgebase.getRecipeObjectInstanceList(generalDomain, objectHashingFactory, recipe));
			tools.addAll(knowledgebase.getTools(generalDomain, SpaceFactory.SPACE_COUNTER, objectHashingFactory));
		}
		
	
		ObjectClass containerClass = generalDomain.getObjectClass(ContainerFactory.ClassName);		
		
		List<ObjectInstance> containersAndIngredients = Recipe.getContainersAndIngredients(containerClass, ingredients, counterSpace.getName());
		objects.addAll(containersAndIngredients);
		objects.addAll(tools);
		return new State(objects);
	}
	
	public static Map<String, Map<Workflow.Node, Double>> buildActionTimeLookup(Workflow workflow, int numAgents, List<Double> factors) {
		Map<String, Map<Workflow.Node, Double>> actionTimeLookup = new HashMap<String, Map<Workflow.Node, Double>>();
		
		Random random = new Random();
		
		for (int i = 0; i < numAgents; i++) {
			String id = Integer.toString(i);
			Double factor = factors.get(i);
			Map<Workflow.Node, Double> times = new HashMap<Workflow.Node, Double>();
			for (Workflow.Node node : workflow) {
				times.put(node, factor * random.nextDouble());
			}
			actionTimeLookup.put(id, times);
		}
		
		return actionTimeLookup;
	}
	
	
	
	public static double getAgentsSoloTime(Workflow workflow, Map<Workflow.Node, Double> timeLookup) {
		double sum = 0.0;
		for (Node node : workflow) {
			sum += timeLookup.get(node);
		}
		return sum;
	}
	
	public static boolean verifyAssignments(Workflow workflow, List<Assignment> assignedWorkflows) {
		int size = 0;
		for (Assignment assignedWorkflow : assignedWorkflows) {
			size += assignedWorkflow.size();
			for (ActionTime time : assignedWorkflow) {
				int duration = (int)(time.getTime() * 10);
				String label = (time.getNode() == null ) ? "." : time.getNode().toString();
				int length = duration * 3;
				length = Math.max(1, length);
				if (length > 0) {
					label = String.format("%" + length + "s", label);
					System.out.print(label.replace(' ', '.'));
				}
				
			}
			System.out.print("\n");
		}
		System.out.println("\n\n");
		
		
		
		if (size != workflow.size()) {
			System.err.println(Integer.toString(size) + " actions were assigned. Should be " + workflow.size());
			return false;
		}
		return true;
	}
	public static void main(String argv[]) {
		List<Scheduler> schedulers = Arrays.asList(
				new RandomScheduler(),
				new GreedyScheduler(false),
				new WeightByShortest(false),
				new WeightByDifference(false),
				new ExhaustiveScheduler(5)/*,
				new ExhaustiveScheduler()*/
				);
		
		int numTries = 100;
		/*for (Map.Entry<String, Map<Workflow.Node, Double>> entry : actionTimeLookup.entrySet()) {
			System.out.println("Workflow time for " + entry.getKey() + ": " + SchedulingComparison.getAgentsSoloTime(workflow, entry.getValue()));
		}*/
		
		Domain domain = RecipeScheduling.generateGeneralDomain();
		List<Recipe> recipes = AgentHelper.recipes(domain);
		
		Knowledgebase knowledgebase = Knowledgebase.getKnowledgebase(domain);
		knowledgebase.initKnowledgebase(recipes);
		Map<String, Double> factors = new HashMap<String, Double>();
		factors.put("human", 1.0);
		
		ActionTimeGenerator timeGenerator = new ActionTimeGenerator(factors);
		Human human = new Human(domain, timeGenerator);
		State state = RecipeScheduling.generateInitialState(domain, recipes, human, null);
		ResetAction reset = (ResetAction)domain.getAction(ResetAction.className);
		
		human.setInitialState(state);
		reset.setState(state);
		
		Map<String, List<GroundedAction>> actionLists = new HashMap<String, List<GroundedAction>>();
		for (Recipe recipe : recipes) {
			
			
			for (int i = 0; i < 5; i++) {
				long start = System.nanoTime();
				
				List<KitchenSubdomain> policyDomains = AgentHelper.generateRTDPPolicies(recipe, domain, state, rewardFunction, hashingFactory);
				List<GroundedAction> actionList = AgentHelper.generateRecipeActionSequence(state, rewardFunction, policyDomains);
				long end = System.nanoTime();
				System.out.println("recipe scheduling, " + (end - start) / 1000000000.0);
			
			/*for (GroundedAction action : actionList) {
				System.out.println(action.toString());
			}*/
			actionLists.put(recipe.toString(), actionList);
			}
			
			
		}
		
		Map<String, List<Double>> factorLookup = new HashMap<String, List<Double>>();
		factorLookup.put("same", Arrays.asList(1.0, 1.0));
		factorLookup.put("slow", Arrays.asList(0.5, 2.0));
		factorLookup.put("fast", Arrays.asList(2.0, 0.5));
		
		
		for (int i = 0; i < numTries; i++) {
			double sum = 0.0;
			for (Map.Entry<String, List<Double>> factorEntry : factorLookup.entrySet()) {
				
			
			Collections.shuffle(schedulers);
			
				for (Map.Entry<String, List<GroundedAction>> entry : actionLists.entrySet()) {
					List<GroundedAction> actionList = entry.getValue();
					List<AbstractGroundedAction> abstractActionList = new ArrayList<AbstractGroundedAction>(actionList.size());
					for (GroundedAction action : actionList) { abstractActionList.add(action); }
					for (int j = 0; j < 1; j++) {
						Workflow workflow = Workflow.buildWorkflow(state, abstractActionList);
						Map<String, Map<Workflow.Node, Double>> actionTimeLookup = RecipeScheduling.buildActionTimeLookup(workflow, 2, factorEntry.getValue());
						/*
						for (Scheduler scheduler : schedulers) {
							
							List<AssignedWorkflow> assignments = scheduler.schedule(workflow, actionTimeLookup);
							double time = SchedulingHelper.computeSequenceTime(assignments);
							System.out.println(scheduler.getClass().getSimpleName() + ", " + factorEntry.getKey() + ", " + entry.getKey() + ", " + time);
						}*/
					}
					
				}
			}
		}
	}
}
