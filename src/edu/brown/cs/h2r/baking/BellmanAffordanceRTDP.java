package edu.brown.cs.h2r.baking;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import burlap.behavior.affordances.AffordancesController;
import burlap.behavior.singleagent.QValue;
import burlap.behavior.singleagent.planning.stochastic.rtdp.AffordanceRTDP;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;


/**
 * A subclass of the AffordanceRTDP planner that has an updated BellmanUpdate. This update also uses
 * affordances to trim the action space and ignore any state that were thrown out by said affordances,
 * which allows the planner to behve optimally. Everything else is the same as in AffordanceRTDP.
 * @author Kevin O'Farrell
 */
public class BellmanAffordanceRTDP extends AffordanceRTDP {

	private AffordancesController affController;
	public BellmanAffordanceRTDP(Domain domain, RewardFunction rf, TerminalFunction tf, 
			double gamma, StateHashFactory hashingFactory, double vInit, int numRollouts, 
			double maxDelta, int maxDepth, AffordancesController affController) {
		super(domain, rf, tf, gamma, hashingFactory, vInit, numRollouts, maxDelta, maxDepth, affController, 4);
		this.affController = affController;
	}
	
	@Override
	// Runs a BellmanUpdate, but also uses affordances to trim the action space, which will allow
	// the planner to plan optimally!
	public double performAffordanceBellmanUpdateOn(StateHashTuple sh, AffordancesController affController) {
		if(this.tf.isTerminal(sh.getState())){
			//terminal states always have a state value of 0
			valueFunction.put(sh, 0.);
			return 0;
		}
		
		Map<String,String> matching = null;
		StateHashTuple indexSH = mapToStateIndex.get(sh);
		if(this.containsParameterizedActions && !this.domain.isObjectIdentifierDependent()){
			matching = sh.getState().getObjectMatchingTo(indexSH.getState(), false);
		}
		
		// Have affordances prune away all unnecessary actions
		List<QValue> affFilteredQValues = new ArrayList<QValue>();
		List<AbstractGroundedAction> qActions =  
				this.affController.getPrunedActionsForState(this.actions, sh.getState());
		
		for (AbstractGroundedAction action : qActions) {
			affFilteredQValues.add(this.getQ(sh,(GroundedAction)action, matching));
		}
		
		// If Affordances prune away all actions, back off to full action set 
		if (affFilteredQValues.isEmpty()) {
			for(Action a : actions){
				List<GroundedAction> applications = a.getAllApplicableGroundedActions(sh.getState());
				for(GroundedAction ga : applications){
					affFilteredQValues.add(this.getQ(sh, ga, matching));
				}
			}
		}

		// Find max Q values
		double maxQ = Double.NEGATIVE_INFINITY;
		for(int i = 0; i < affFilteredQValues.size(); i++){
			maxQ = Math.max(maxQ, affFilteredQValues.get(i).q);
		}
		valueFunction.put(sh, maxQ);
	
		return maxQ;
	}
}

