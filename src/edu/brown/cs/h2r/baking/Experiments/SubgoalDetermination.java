package edu.brown.cs.h2r.baking.Experiments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import burlap.behavior.singleagent.Policy;
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
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.Agents.AgentHelper;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.Knowledgebase.Knowledgebase;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ToolFactory;
import edu.brown.cs.h2r.baking.Prediction.PolicyPrediction;
import edu.brown.cs.h2r.baking.Prediction.PolicyProbability;
import edu.brown.cs.h2r.baking.PropositionalFunctions.RecipeBotched;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.actions.GreaseAction;
import edu.brown.cs.h2r.baking.actions.MixAction;
import edu.brown.cs.h2r.baking.actions.MoveAction;
import edu.brown.cs.h2r.baking.actions.PourAction;
import edu.brown.cs.h2r.baking.actions.SwitchAction;
import edu.brown.cs.h2r.baking.actions.UseAction;

public class SubgoalDetermination {
	private final static StateHashFactory hashingFactory = new NameDependentStateHashFactory();
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
		domain.setObjectIdentiferDependence(true);
		
		Action mix = new MixAction(domain);
		Action pour = new PourAction(domain);
		Action move = new MoveAction(domain);
		Action grease = new GreaseAction(domain);
		Action aSwitch = new SwitchAction(domain);
		Action use = new UseAction(domain);
		
		
		// To the failed propFunction, add in all subgoals for a recipe that are based on an ingredient.
		RecipeBotched failed = ((RecipeBotched)domain.getPropFunction(AffordanceCreator.BOTCHED_PF));
		if (failed != null) {
			failed.clearSubgoals();
		}
		
		return domain;
	}
	
	public static State generateInitialState(Domain generalDomain, List<Recipe> recipes) {
		List<ObjectInstance> objects = new ArrayList<ObjectInstance>();
		ObjectHashFactory objectHashingFactory = hashingFactory.getObjectHashFactory();
		objects.add(AgentFactory.getNewHumanAgentObjectInstance(generalDomain, "human", objectHashingFactory));
		
		
		List<String> containers = Arrays.asList("mixing_bowl_1", "mixing_bowl_2", "baking_dish", "melting_pot");
		ObjectInstance counterSpace = SpaceFactory.getNewWorkingSpaceObjectInstance(generalDomain, SpaceFactory.SPACE_COUNTER, containers, "human", objectHashingFactory);
		objects.add(counterSpace);
		
		objects.add(ToolFactory.getNewSimpleToolObjectInstance(generalDomain, "whisk", "", "", SpaceFactory.SPACE_COUNTER, objectHashingFactory));
		objects.add(ToolFactory.getNewSimpleToolObjectInstance(generalDomain, "spoon","", "", SpaceFactory.SPACE_COUNTER, objectHashingFactory));
		
		objects.add(ContainerFactory.getNewBakingContainerObjectInstance(generalDomain, "baking_dish", null, SpaceFactory.SPACE_COUNTER, objectHashingFactory));
		objects.add(ContainerFactory.getNewHeatingContainerObjectInstance(generalDomain, "melting_pot", null, SpaceFactory.SPACE_COUNTER, objectHashingFactory));
		objects.add(SpaceFactory.getNewBakingSpaceObjectInstance(generalDomain, SpaceFactory.SPACE_OVEN, null, "", objectHashingFactory));
		objects.add(SpaceFactory.getNewHeatingSpaceObjectInstance(generalDomain, SpaceFactory.SPACE_STOVE, null, "", objectHashingFactory));
		
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
	
	public static List<AbstractGroundedAction> generateActionSequenceFromPolicy(KitchenSubdomain subdomain, int maxDepth) {
		List<AbstractGroundedAction> actions = new ArrayList<AbstractGroundedAction>();
		Policy policy = subdomain.getPolicy();
		State state = subdomain.getStartState();
		BakingSubgoal subgoal = subdomain.getSubgoal();
		
		//System.out.println("Taking actions");
		for (int i = 0; i < maxDepth; i++) {
			if (subgoal.goalCompleted(state)) {
				return null;
			}
			AbstractGroundedAction action = policy.getAction(state);
			//System.out.println("\t" + action.actionName() + " " + Arrays.toString(action.params) );
			state = action.executeIn(state);
			actions.add(action);
		}
		return actions;
	}
	
	public static State getStateFromActionSequence(State startingState, List<AbstractGroundedAction> actions) {
		State currentState = startingState;
		for (AbstractGroundedAction action : actions) {
			currentState = action.executeIn(currentState);
		}
		return currentState;
	}
	
	public static void insertPolicy(Map<String, Double> bestPolicies, PolicyProbability probability, KitchenSubdomain subdomain, int numberToReturn) {
		String key = subdomain.toString();
		Double value = probability.getProbability();
		if (bestPolicies.size() < numberToReturn) {
			bestPolicies.put(key, value);
		} else {
			boolean insert = false;
			Map.Entry<String, Double> minEntry = 
					bestPolicies.entrySet().iterator().next();
			
			for (Map.Entry<String, Double> entry : bestPolicies.entrySet()) {
				if (entry.getValue() < minEntry.getValue()) {
					minEntry = entry;
				}
				
			}
			if (minEntry.getValue() < value) {
				bestPolicies.remove(minEntry.getKey());
				bestPolicies.put(key, value);
			}
		}
	}
	
	public static Map<String, Double> findTopPolicies(List<PolicyProbability> distribution, List<KitchenSubdomain> subdomains, int numberToReturn) {
		Map<String, Double> bestPolicies = new HashMap<String, Double>();
		
		for (int i = 0; i < distribution.size(); i++) {
			PolicyProbability probability = distribution.get(i);
			KitchenSubdomain subdomain = subdomains.get(i);
			SubgoalDetermination.insertPolicy(bestPolicies, probability, subdomain, numberToReturn);
		}
		
		return bestPolicies;
		
	}
		
	public static void main(String[] argv) {
		int maxAlpha = 3;
		int numTries = 10;
		//int depth = 1;
		
		if (argv.length > 0) {
			numTries = Integer.parseInt(argv[0]);
		}
		Domain domain = SubgoalDetermination.generateGeneralDomain();
		List<Recipe> recipes = AgentHelper.recipes(domain);
		Knowledgebase knowledgebase = Knowledgebase.getKnowledgebase(domain);
		knowledgebase.initKnowledgebase(recipes);
		State state = SubgoalDetermination.generateInitialState(domain, recipes);
		
		//for (Recipe recipe : recipes) {
		//System.out.println("Testing recipe " + recipe.toString());
		//	ExperimentHelper.testRecipeExecution(domain, state, recipe);
		//System.out.println("\n\n");
		//}
		
		RewardFunction rf = new RewardFunction() {

			@Override
			public double reward(State s, GroundedAction a, State sprime) {
				// TODO Auto-generated method stub
				return -1;
			}
			
		};
		List<KitchenSubdomain> policyDomains = AgentHelper.generateAllRTDPPolicies(domain, state, recipes, rf, hashingFactory);
		List<KitchenSubdomain> testDomains = new ArrayList<KitchenSubdomain>(policyDomains);
		Random rando = new Random();
		
		
		//int k = testDomains.size() -1 ;
		System.out.println("Chosen policy, Depth, Depth Type, Successes, Estimate Successes, Informed Guesses, Total Trials");
		
		for (int i = 0; i < numTries; i++) {
			for (int k = 0; k < testDomains.size(); k++) {
				for (int depthType = 0; depthType < 3; depthType++) {
					List<Double> successRate = new ArrayList<Double>();
					PolicyPrediction prediction = new PolicyPrediction(policyDomains, depthType);			
					for (int depth = 0; depth < 4; depth++) {	
						int numSuccess = 0;
						int numEstimateSuccesses = 0;
						int numRandomGuesses = 0;
						
						KitchenSubdomain policyDomain = testDomains.get(k);
						
						List<AbstractGroundedAction> actions = SubgoalDetermination.generateActionSequenceFromPolicy(policyDomain, depth);
						state = SubgoalDetermination.getStateFromActionSequence(policyDomain.getStartState(), actions);
						if (state == null) {
							break;
						}
						
						List<PolicyProbability> policyDistribution = 
								prediction.getPolicyDistributionFromStatePair(policyDomain.getStartState(), state, maxAlpha+1, 
										policyDomain, actions, SubgoalDetermination.hashingFactory, depthType);
						
						if (policyDistribution == null) {
							continue;
						}
						double maxProb = 0.0;
						List<KitchenSubdomain> bestPolicies = new ArrayList<KitchenSubdomain>();
						for (int j = 0; j < policyDistribution.size(); j++) {
							PolicyProbability policyProbability = policyDistribution.get(j);
							String name = policyProbability.getPolicyDomain().toString();
									
							double prob = (policyProbability == null) ? 0.0 : policyProbability.getProbability();
							if (prob > maxProb) {
								bestPolicies.clear();
								bestPolicies.add(policyProbability.getPolicyDomain());
								maxProb = prob;	
							} else if (prob == maxProb) {
								bestPolicies.add(policyProbability.getPolicyDomain());
							}
						}
						Collections.shuffle(bestPolicies, rando);
						if (bestPolicies.size() > 1) {
							numRandomGuesses++;
						}
						KitchenSubdomain choice = bestPolicies.get(0);
						
						if (choice.equals(policyDomain)) {
							numSuccess++;
							if (bestPolicies.size() == 1) {
								numEstimateSuccesses++;
							}
						}
						System.out.println(policyDomain.toString() + ", " + depth + ", " + depthType + ", " + numSuccess + ", " + numEstimateSuccesses + ", " + numRandomGuesses + ", " +  1);
					}
				}
			}
		}
		
		/*successRate.add((double)numSuccess / numTries);
		System.out.println("Success: " + numSuccess + "/" + numTries);
		System.out.println("Correct non guesses: " + numEstimateSuccesses + "/" + (numTries - numRandomGuesses));
		System.out.println("Educated Guesses: " + numRandomGuesses + "/" + numTries);
		
		
		for (int i = 0; i < successRate.size(); i++) {
			int depthType = i % 3;
			if (depthType == 0) {
				System.out.println("Depth type: " +  i / 3);
			}
			System.out.println(Integer.toString(i+1) + ": " + successRate.get(i));
		}*/
			
	}
}
