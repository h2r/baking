package edu.brown.cs.h2r.baking.Agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.Policy.ActionProb;
import burlap.behavior.singleagent.planning.stochastic.rtdp.RTDP;
import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import edu.brown.cs.h2r.baking.RecipeTerminalFunction;
import edu.brown.cs.h2r.baking.Experiments.KitchenSubdomain;
import edu.brown.cs.h2r.baking.Experiments.ManyAgentsScheduling;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;
import edu.brown.cs.h2r.baking.Scheduling.AssignedWorkflow;
import edu.brown.cs.h2r.baking.Scheduling.AssignedWorkflow.ActionTime;
import edu.brown.cs.h2r.baking.Scheduling.ExhaustiveScheduler;
import edu.brown.cs.h2r.baking.Scheduling.Scheduler;
import edu.brown.cs.h2r.baking.Scheduling.Workflow;
import edu.brown.cs.h2r.baking.actions.ResetAction;

public class Human implements Agent {
	private final static RewardFunction rewardFunction = new RewardFunction() {

		@Override
		public double reward(State s, GroundedAction a, State sprime) {
			// TODO Auto-generated method stub
			return (a.action instanceof ResetAction) ? -2 : -1;
		}
		
	};
	
	private final static StateHashFactory hashingFactory = new NameDependentStateHashFactory();
	private State startingState;
	private final String name;
	
	private Recipe currentRecipe;
	private KitchenSubdomain currentSubgoal;
	private List<KitchenSubdomain> kitchenSubdomains;
	private List<KitchenSubdomain> allKitchenSubdomains;
	private TerminalFunction isFailure;
	private Domain generalDomain;
	private final Scheduler scheduler = new ExhaustiveScheduler(5);
	private final ActionTimeGenerator timeGenerator;
	public Human(Domain generalDomain, ActionTimeGenerator timeGenerator) {
		this.generalDomain = generalDomain;
		this.name = "human";
		this.timeGenerator = timeGenerator;
	}
	
	public Human(Domain generalDomain, String name, ActionTimeGenerator timeGenerator) {
		this.generalDomain = generalDomain;
		this.name = name;
		this.timeGenerator = timeGenerator;
	}
	
	@Override
	public ObjectInstance getAgentObject() {
		return AgentFactory.getNewHumanAgentObjectInstance(this.generalDomain, this.getAgentName(), hashingFactory.getObjectHashFactory());
	}
	
	@Override
	public void setInitialState(State state) {
		this.startingState = state;
	}
	
	public void chooseNewRecipe() {
		List<Recipe> recipes = new ArrayList<Recipe>(AgentHelper.recipes(generalDomain));
		Collections.shuffle(recipes);
		
		this.setRecipe(recipes.get(0));
		//System.out.println(this.getAgentName() + " chose " + this.currentRecipe.toString());
	}
	
	public void setRecipe(Recipe recipe) {
		this.currentRecipe = recipe;
		this.currentSubgoal = null;
		this.kitchenSubdomains = AgentHelper.generateRTDPPolicies(this.currentRecipe, this.generalDomain, this.startingState, Human.rewardFunction, Human.hashingFactory);
	}
	
	public void setSubgoal(String subgoalName) {
		for (KitchenSubdomain subdomain : this.allKitchenSubdomains) {
			if (subdomain.toString().equals(subgoalName)) {
				this.currentRecipe = subdomain.getRecipe();
				this.currentSubgoal = subdomain;
				return;
			}
		}
		this.currentRecipe = null;
		this.currentSubgoal = null;
		
	}
	
	
	
	public Recipe getCurrentRecipe() {
		return this.currentRecipe;
	}
	
	public String getCurrentSubgoal() {
		return (this.currentSubgoal == null) ? "" : this.currentSubgoal.toString();
	}
	
	public State chooseNewSubdomain() {
		Random random = new Random();
		int choice = random.nextInt(this.allKitchenSubdomains.size());
		this.currentSubgoal = this.allKitchenSubdomains.get(choice);
		this.currentRecipe = this.currentSubgoal.getRecipe();
		return this.currentSubgoal.getStartState();
	}
	
	private void chooseNewSubgoal(State state) {
		if (this.startingState.equals(state)) {
			this.setRecipe(this.currentRecipe);
		}
		List<KitchenSubdomain> activeSubgoals = new ArrayList<KitchenSubdomain>();
		
		for (KitchenSubdomain subdomain : this.kitchenSubdomains) {
			if (subdomain.getSubgoal().allPreconditionsCompleted(state) && 
					!subdomain.getSubgoal().goalCompleted(state)) {
				activeSubgoals.add(subdomain);
				
			}
		}
		
		Collections.shuffle(activeSubgoals);
		this.currentSubgoal = (activeSubgoals.size() == 0) ? null : activeSubgoals.get(0);
		if (this.currentSubgoal == null) {
			return;
		}
		
		final PropositionalFunction isSuccess = this.currentSubgoal.getSubgoal().getGoal();
		final PropositionalFunction isFailure = this.currentSubgoal.getDomain().getPropFunction(AffordanceCreator.BOTCHED_PF);
		this.isFailure = new RecipeTerminalFunction(isFailure);
		
		this.generalDomain = AgentHelper.setSubgoal(this.generalDomain, this.currentSubgoal.getSubgoal());
		//System.out.println(this.getAgentName() + " switches to task: " + this.currentSubgoal.toString());
		
	}
	
	
	
	@Override
	public void addObservation(State state) {
		// TODO Auto-generated method stub

	}

	@Override
	public String getAgentName() {
		return this.name;
	}

	@Override
	public AbstractGroundedAction getAction(State state) {
		if (this.isSuccess(state)) {
			return null;
		}
		if (this.currentSubgoal == null) {
			this.chooseNewSubgoal(state);
		} else if (this.currentSubgoal.getSubgoal().goalCompleted(state)) {
			this.kitchenSubdomains.remove(this.currentSubgoal);
			this.chooseNewSubgoal(state);
		}
		if (this.currentSubgoal == null) {
			return new GroundedAction(this.generalDomain.getAction("reset"), new String[] {"human"});
		}
		
		List<ActionProb> allowableActions = this.getAllowableActions(state);
		if (allowableActions.size() == 0) {
			this.chooseNewSubgoal(state);
			if (this.currentSubgoal == null) {
				return null;
			}
			allowableActions = this.getAllowableActions(state);
		}
		this.normalizeActionDistribution(allowableActions);
		AbstractGroundedAction action = this.getActionFromPolicyDistribution(allowableActions);
		if (action != null) {
			GroundedAction groundedAction = (GroundedAction)action;
			groundedAction.params[0] = this.getAgentName();
		}
		
		return action;
	}
	
	public AbstractGroundedAction getActionWithScheduler(State state, List<String> agents) {
		if (this.isSuccess(state)) {
			return null;
		}
		
		List<AbstractGroundedAction> actions = this.generateActionList(state);
		Workflow workflow = Workflow.buildWorkflow(state, actions);
		List<AssignedWorkflow> assignments = this.scheduler.schedule(workflow, agents, this.timeGenerator);
		for (AssignedWorkflow assignment : assignments) {
			if (assignment.getId().equals(this.getAgentName())) {
				for (ActionTime actionTime : assignment) {
					return actionTime.getNode().getAction();
				}
			}
		}
		
		return null;
	}
	
	private List<AbstractGroundedAction> generateActionList(State state) {
		List<AbstractGroundedAction> actions = new ArrayList<AbstractGroundedAction>();
		boolean isFinished = false;
		while(!isFinished) {
			if (this.currentSubgoal == null) {
				this.chooseNewSubgoal(state);
			} else if (this.currentSubgoal.getSubgoal().goalCompleted(state)) {
				this.kitchenSubdomains.remove(this.currentSubgoal);
				this.chooseNewSubgoal(state);
			}
			if (this.currentSubgoal == null) {
				break;
			}
			
			List<ActionProb> allowableActions = this.getAllowableActions(state);
			if (allowableActions.size() == 0) {
				this.chooseNewSubgoal(state);
				if (this.currentSubgoal == null) {
					return null;
				}
				allowableActions = this.getAllowableActions(state);
			}
			this.normalizeActionDistribution(allowableActions);
			AbstractGroundedAction action = this.getActionFromPolicyDistribution(allowableActions);
			if (action == null) {
				this.kitchenSubdomains.remove(this.currentSubgoal);
				this.chooseNewSubgoal(state);
			} else {
				state = action.executeIn(state);
				actions.add(action);
			}
			
		}
		
		return actions;
	}

	private List<ActionProb> getAllowableActions(State state) {
		RTDP planner = this.currentSubgoal.getPlanner();
		planner.planFromState(state);
		Policy policy = this.currentSubgoal.getPolicy();
		List<ActionProb> actionDistribution = policy.getActionDistributionForState(state);
		List<ActionProb> allowableActions = new ArrayList<ActionProb>();
		for (ActionProb actionProb : actionDistribution) {
			GroundedAction groundedAction = (GroundedAction)actionProb.ga;
			if (groundedAction.params[0].equals(this.getAgentName())) {
				allowableActions.add(actionProb);
			}
		}
		return allowableActions;
	}
	
	private void normalizeActionDistribution(List<ActionProb> actionDistribution) {
		double sumProbability = 0.0;
		for (ActionProb actionProb : actionDistribution) {
			sumProbability += actionProb.pSelection;
		}
		for (ActionProb actionProb : actionDistribution) {
			actionProb.pSelection = (sumProbability == 0.0) ? 1.0 / actionDistribution.size() : actionProb.pSelection / sumProbability;
		}
	}
	
	private AbstractGroundedAction getActionFromPolicyDistribution(List<ActionProb> actionDistribution) {
		Random random = new Random();
		double roll = random.nextDouble();
		double sumProbability = 0.0;
		for (ActionProb actionProb : actionDistribution) {
			sumProbability += actionProb.pSelection;
			if (roll < sumProbability) {
				return actionProb.ga;
			}
		}
		return null;
	}

	public boolean isFinished(State state) {
		
		boolean isFailure = (this.isFailure == null) ? false : this.isFailure.isTerminal(state);
		boolean isGoalComplete = (this.currentSubgoal == null) ? false : this.currentSubgoal.getSubgoal().goalCompleted(state);
		boolean areGoalsCompleted = this.kitchenSubdomains.size() == 1;
		return (isFailure || (isGoalComplete && areGoalsCompleted)); 
	}
	
	public boolean isSuccess(State state) {
		return this.currentRecipe.isSuccess(state);
	}
	
	public boolean isSubgoalFinished(State state) {
		return (this.currentSubgoal == null) ? true : this.currentSubgoal.getSubgoal().goalCompleted(state);
	}
	
	public double getCostActions(List<AbstractGroundedAction> actionSequence, List<State> stateSequence) {
		double cost = 0.0;
		State previousState = this.startingState;
		for (int i = 0; i < actionSequence.size(); i++) {
			GroundedAction groundedAction = (GroundedAction)actionSequence.get(i);
			cost += (groundedAction.params[0].equals("human")) ? 1 : 0;
			//State nextState = stateSequence.get(i+1);
			//cost += Human.rewardFunction.reward(previousState, groundedAction, nextState);
		
		}
		return cost;
	}
	
	public void buildAllSubdomains() {
		this.allKitchenSubdomains = AgentHelper.generateAllRTDPPolicies(this.generalDomain, this.startingState, 
				AgentHelper.recipes(generalDomain), Human.rewardFunction, Human.hashingFactory);
		this.kitchenSubdomains = new ArrayList<KitchenSubdomain>(this.allKitchenSubdomains);
	}
	
	public State getNewStartingState() {
		if (this.allKitchenSubdomains == null) {
			return null;
		}
		List<KitchenSubdomain> subdomains = new ArrayList<KitchenSubdomain>(this.allKitchenSubdomains);
		Collections.shuffle(subdomains);
		KitchenSubdomain currentSubdomain = subdomains.get(0);
		this.currentSubgoal = currentSubdomain;
		this.currentRecipe = currentSubdomain.getRecipe();
		return this.currentSubgoal.getStartState();
	}
}
