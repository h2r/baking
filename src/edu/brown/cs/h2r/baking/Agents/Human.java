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
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.PropositionalFunctions.AllowReset;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;
import edu.brown.cs.h2r.baking.actions.ResetAction;

public class Human extends Agent {
	protected final static RewardFunction rewardFunction = new RewardFunction() {

		@Override
		public double reward(State s, GroundedAction a, State sprime) {
			return (a.action instanceof ResetAction) ? -2 : -1;
		}
		
	};
	
	private final static StateHashFactory hashingFactory = new NameDependentStateHashFactory();
	
	protected Recipe currentRecipe;
	protected final List<Recipe> recipes;
	protected KitchenSubdomain currentSubgoal;
	protected List<KitchenSubdomain> kitchenSubdomains;
	private List<KitchenSubdomain> allKitchenSubdomains;
	private Map<Recipe, List<KitchenSubdomain>> recipeLookup;
	private TerminalFunction isFailure;
	protected Domain generalDomain;
	protected final ActionTimeGenerator timeGenerator;
	public Human(Domain generalDomain, ActionTimeGenerator timeGenerator, List<Recipe> recipes) {
		super("human");
		this.generalDomain = generalDomain;
		this.timeGenerator = timeGenerator;
		this.recipeLookup = new HashMap<Recipe, List<KitchenSubdomain>>();
		this.kitchenSubdomains = new ArrayList<KitchenSubdomain>();
		this.recipes = recipes;
	}
	
	public Human(Domain generalDomain, String name, ActionTimeGenerator timeGenerator, List<Recipe> recipes) {
		super(name);
		this.generalDomain = generalDomain;
		this.timeGenerator = timeGenerator;
		this.recipeLookup = new HashMap<Recipe, List<KitchenSubdomain>>();
		this.kitchenSubdomains = new ArrayList<KitchenSubdomain>();
		this.recipes = recipes;
	}
	
	public Human(Domain generalDomain, String name, boolean isRobot, ActionTimeGenerator timeGenerator, List<Recipe> recipes) {
		super(name, isRobot);
		this.generalDomain = generalDomain;
		this.timeGenerator = timeGenerator;
		this.recipeLookup = new HashMap<Recipe, List<KitchenSubdomain>>();
		this.kitchenSubdomains = new ArrayList<KitchenSubdomain>();
		this.recipes = recipes;
	}
	
	protected Human(Domain domain, Map<String, Object> map, ActionTimeGenerator timeGenerator, State startState, List<Recipe> recipes) {
		super(map);
		this.generalDomain = domain;
		this.timeGenerator = timeGenerator;
		this.recipeLookup = new HashMap<Recipe, List<KitchenSubdomain>>();
		this.kitchenSubdomains = new ArrayList<KitchenSubdomain>();
		this.recipes = recipes;
		this.setInitialState(startState);
		String currentRecipeStr = (String)map.get("current_recipe");
		if (currentRecipeStr != null) {
			for (Recipe recipe : this.recipeLookup.keySet()) {
				if (currentRecipeStr.equals(recipe.toString())) {
					this.setRecipe(recipe);
					break;
				}
			}
		}
		Object currentSubgoal = map.get("current_subgoal");
		if (currentSubgoal != null) {
			this.setSubgoal((String)currentSubgoal);
		} 
		List<String> currentSubgoalsAvailable = (List<String>)map.get("current_subgoals");
		
		List<KitchenSubdomain> toRemove = new ArrayList<KitchenSubdomain>();
		for (KitchenSubdomain subdomain : this.kitchenSubdomains) {
			if (!currentSubgoalsAvailable.contains(subdomain.toString())) {
				toRemove.add(subdomain);
			}
		}
		this.kitchenSubdomains.removeAll(toRemove);
	}
	
	@Override
	protected Map<String, Object> toMap() {
		Map<String, Object> map = super.toMap();
		
		if (this.currentSubgoal != null) {
			map.put("current_subgoal", this.currentSubgoal.toString());
		}
		if (this.currentRecipe != null) {
			map.put("current_recipe", this.currentRecipe.toString());
		}
		List<String> currentSubgoalsAvailable = new ArrayList<String>();
		for (KitchenSubdomain subdomain : this.kitchenSubdomains) {
			currentSubgoalsAvailable.add(subdomain.toString());
		}
		map.put("current_subgoals", currentSubgoalsAvailable);
		return map;
	}
	
	@Override
	public String toString() {
		return this.getClass().getSimpleName();
	}
	
	@Override
	public void setInitialState(State state) {
		super.setInitialState(state);
		ResetAction reset = (ResetAction)generalDomain.getAction(ResetAction.className);
		reset.setState(this.getStartState());
		
	}
	
	@Override
	public void reset() {
		this.currentRecipe = null;
		this.currentSubgoal = null;
		this.kitchenSubdomains.clear();
	}
	
	public void performResetAction() {
		this.setRecipe(this.currentRecipe);
	}
	
	public void chooseNewRecipe() {
		List<Recipe> recipes = new ArrayList<Recipe>(this.recipes);
		Collections.shuffle(recipes);
		
		this.setRecipe(recipes.get(0));
		//System.out.println(this.getAgentName() + " chose " + this.currentRecipe.toString());
	}
	
	public void setRecipe(Recipe recipe) {
		this.currentRecipe = recipe;
		this.currentSubgoal = null;
		List<KitchenSubdomain> domains = this.recipeLookup.get(recipe);
		if (domains == null) {
			domains = AgentHelper.generateRTDPPolicies(recipe, this.generalDomain, this.getStartState(), Human.rewardFunction, Human.hashingFactory);
			this.recipeLookup.put(recipe, domains);
		}
		this.allKitchenSubdomains = domains;
		this.setKitchenSubdomains(new ArrayList<KitchenSubdomain>(this.allKitchenSubdomains));
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
	
	public void initialSubgoal(State state) {
		List<KitchenSubdomain> activeSubgoals = new ArrayList<KitchenSubdomain>();
		
		for (KitchenSubdomain subdomain : this.getKitchenSubdomains()) {
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
		
		final PropositionalFunction isFailure = this.currentSubgoal.getDomain().getPropFunction(AffordanceCreator.BOTCHED_PF);
		this.isFailure = new RecipeTerminalFunction(isFailure);
		
		this.generalDomain = AgentHelper.setSubgoal(this.generalDomain, this.currentSubgoal.getSubgoal());
	}
	
	protected void chooseNewSubgoal(State state) {
		if (state.equals(this.getStartState())) {
			this.kitchenSubdomains = new ArrayList<KitchenSubdomain>(this.allKitchenSubdomains);
		}
		List<KitchenSubdomain> activeSubgoals = new ArrayList<KitchenSubdomain>();
		
		for (KitchenSubdomain subdomain : this.getKitchenSubdomains()) {
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
		
		final PropositionalFunction isFailure = this.currentSubgoal.getDomain().getPropFunction(AffordanceCreator.BOTCHED_PF);
		this.isFailure = new RecipeTerminalFunction(isFailure);
		AllowReset resetPF = (AllowReset)this.currentSubgoal.getDomain().getPropFunction(AffordanceCreator.RESET_PF);
		resetPF.setStartState(this.getStartState());
		this.generalDomain = AgentHelper.setSubgoal(this.currentSubgoal.getDomain(), this.currentSubgoal.getSubgoal());
	}
	
	@Override
	public void addObservation(State state) {
	}

	@Override
	public AbstractGroundedAction getActionInState(State state) {
		if (this.isSuccess(state)) {
			return null;
		}
		if (this.currentSubgoal == null) {
			this.chooseNewSubgoal(state);
		} else if (this.currentSubgoal.getSubgoal().goalCompleted(state)) {
			this.getKitchenSubdomains().remove(this.currentSubgoal);
			this.chooseNewSubgoal(state);
		}
		if (this.currentSubgoal == null) {
			this.setRecipe(this.currentRecipe);
			return new GroundedAction(this.generalDomain.getAction("reset"), new String[] {"human"});
		}
		
		List<ActionProb> allowableActions = this.getAllowableActions(state);
		System.out.println(allowableActions.toString());
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
	
	public AbstractGroundedAction getActionInStateWithScheduler(State state, List<String> agents, boolean finishRecipe, GroundedAction partnersAction) {
		return this.getActionInState(state);
		/*if (this.isSuccess(state)) {
			return null;
		}
		
		List<AbstractGroundedAction> actions = this.generateActionList(state);
		Workflow workflow = Workflow.buildWorkflow(state, actions);
		List<Assignment> assignments = this.scheduler.schedule(workflow, agents, this.timeGenerator);
		for (Assignment assignment : assignments) {
			if (assignment.getId().equals(this.getAgentName())) {
				for (ActionTime actionTime : assignment) {
					return actionTime.getNode().getAction();
				}
			}
		}
		
		return null;*/
	}
	

	protected List<ActionProb> getAllowableActions(State state) {
		ResetAction reset = (ResetAction)generalDomain.getAction(ResetAction.className);
		reset.setState(this.getStartState());
		
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
	
	protected void normalizeActionDistribution(List<ActionProb> actionDistribution) {
		double sumProbability = 0.0;
		for (ActionProb actionProb : actionDistribution) {
			sumProbability += actionProb.pSelection;
		}
		for (ActionProb actionProb : actionDistribution) {
			actionProb.pSelection = (sumProbability == 0.0) ? 1.0 / actionDistribution.size() : actionProb.pSelection / sumProbability;
		}
	}
	
	protected AbstractGroundedAction getActionFromPolicyDistribution(List<ActionProb> actionDistribution) {
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
		boolean isGoalComplete = this.currentRecipe.isSuccess(state);
		return (isFailure || isGoalComplete); 
	}
	
	public boolean isSuccess(State state) {
		if (this.currentRecipe == null) {
			return false;
		}
		return this.currentRecipe.isSuccess(state);
	}
	
	public boolean isSubgoalFinished(State state) {
		return (this.currentSubgoal == null) ? true : this.currentSubgoal.getSubgoal().goalCompleted(state);
	}
	
	public double getCostActions(List<AbstractGroundedAction> actionSequence, List<State> stateSequence) {
		double cost = 0.0;
		for (int i = 0; i < actionSequence.size(); i++) {
			GroundedAction groundedAction = (GroundedAction)actionSequence.get(i);
			cost += (groundedAction.params[0].equals("human")) ? 1 : 0;
			//State nextState = stateSequence.get(i+1);
			//cost += Human.rewardFunction.reward(previousState, groundedAction, nextState);
		
		}
		return cost;
	}
	
	public void buildAllSubdomains() {
		this.allKitchenSubdomains = AgentHelper.generateAllRTDPPolicies(this.generalDomain, this.getStartState(), 
				this.recipes, Human.rewardFunction, Human.hashingFactory);
		this.setKitchenSubdomains(new ArrayList<KitchenSubdomain>(this.allKitchenSubdomains));
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

	protected List<KitchenSubdomain> getKitchenSubdomains() {
		return kitchenSubdomains;
	}

	private void setKitchenSubdomains(List<KitchenSubdomain> kitchenSubdomains) {
		this.kitchenSubdomains = kitchenSubdomains;
	}
}
