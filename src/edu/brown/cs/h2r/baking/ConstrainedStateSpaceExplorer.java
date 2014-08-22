package edu.brown.cs.h2r.baking;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.brown.cs.h2r.baking.Experiments.CleaningPassingObjectsKitchen;
import edu.brown.cs.h2r.baking.Experiments.KevinsKitchen;
import edu.brown.cs.h2r.baking.Recipes.Brownies;
import edu.brown.cs.h2r.baking.Recipes.CucumberSalad;
import edu.brown.cs.h2r.baking.Recipes.DeviledEggs;
import edu.brown.cs.h2r.baking.Recipes.MashedPotatoes;
import edu.brown.cs.h2r.baking.Recipes.MoltenLavaCake;
import edu.brown.cs.h2r.baking.Recipes.PeanutButterCookies;
import edu.brown.cs.h2r.baking.Recipes.PecanPie;
import edu.brown.cs.h2r.baking.Recipes.Recipe;

import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.deterministic.DeterministicPlanner.PlanningFailedException;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;

public class ConstrainedStateSpaceExplorer {
	OOMDPPlanner planner;
	List<Action> availableActions;
	StateHashFactory stateHash;
	public ConstrainedStateSpaceExplorer(OOMDPPlanner planner, List<Action> actions) {
		this.planner = planner;
		this.stateHash = this.planner.getHashingFactory();
		this.availableActions = actions;
	}
	
	public List<StateHashTuple> getConstrainedStatesAccessibleFromState(State startState) {
		Set<StateHashTuple> accessibleStates = new HashSet<StateHashTuple>();
		Set<StateHashTuple> frontier = new HashSet<StateHashTuple>();
		
		Set<StateHashTuple> statesToAdd;
		StateHashTuple currentState;
		Iterator<StateHashTuple> frontierIt;
		
		
		if (this.accesibleFromState(startState)) {
			currentState = this.stateHash.hashState(startState);
			frontier.add(currentState);
			accessibleStates.add(currentState);
		}
		while(!frontier.isEmpty()) {
			frontierIt = frontier.iterator();
			currentState = frontierIt.next();
			frontierIt.remove();
			statesToAdd = this.addStatesToConstrainedSet(currentState.s, accessibleStates);
			frontier.addAll(statesToAdd);
		}
		
		return new ArrayList<StateHashTuple>(accessibleStates);
	}
	
	public Set<StateHashTuple> addStatesToConstrainedSet(State currentState, Set<StateHashTuple> accessibleStates) {
		List<GroundedAction> groundedActions = 
				Action.getAllApplicableGroundedActionsFromActionList(this.availableActions, currentState);
		Set<StateHashTuple> newStates = new HashSet<StateHashTuple>();
		
		for (GroundedAction groundedAction : groundedActions) {
			State newState = groundedAction.executeIn(currentState);
			if (this.accesibleFromState(newState)) {
				StateHashTuple newHash = this.stateHash.hashState(newState);
				if (accessibleStates.add(newHash)) {
					newStates.add(newHash);
				}
			}
		}
		
		return newStates;
	}
	
	public boolean accesibleFromState(State from) {
		this.planner.resetPlannerResults();
		try {
			this.planner.planFromState(from);
			return true;
		}
		catch (PlanningFailedException e) {
			return false;
		}
	}
	
	public static void main(String[] args) {
		Recipe recipe = new MashedPotatoes();
		//recipe.getIngredientSubgoals()
		CleaningPassingObjectsKitchen kitchen = new CleaningPassingObjectsKitchen(new MashedPotatoes());
		Domain domain = kitchen.generateDomain();
		State state = kitchen.generateInitialState(domain);
		//ConstrainedStateSpaceExplorer explorer = new ConstrainedStateSpaceExplorer(planner, domain.getActions());
		//List<State> states = explorer.getConstrainedStatesAccessibleFromState(state);
		
		//System.out.println("States: " + Integer.toString(states.size()));
	}
}
