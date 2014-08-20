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
		super(domain, rf, tf, gamma, hashingFactory, vInit, numRollouts, maxDelta, maxDepth, affController);
		this.affController = affController;
	}
	
	@Override
	// Runs a BellmanUpdate, but also uses affordances to trim the action space, which will allow
	// the planner to plan optimally!
	protected double performBellmanUpdateOn(StateHashTuple sh) {
		if(this.tf.isTerminal(sh.s)){
			//terminal states always have a state value of 0
			valueFunction.put(sh, 0.);
			return 0;
		}
		
		
		double maxQ = Double.NEGATIVE_INFINITY;
		
		Map<String,String> matching = null;
		StateHashTuple indexSH = mapToStateIndex.get(sh);
		if(this.containsParameterizedActions && !this.domain.isObjectIdentifierDependent()){
			matching = sh.s.getObjectMatchingTo(indexSH.s, false);
		}
		
		// get all actions and Q values
		//List <QValue> allQValues = new ArrayList<QValue>();
		//for(Action a : actions){
		//	List<GroundedAction> applications = a.getAllApplicableGroundedActions(sh.s);
		//	for(GroundedAction ga : applications){
		//		allQValues.add(this.getQ(sh, ga, matching));
		//	}
		//}
		
		
		// Have affordances prune away all unnecessary actions
		List<QValue> affFilteredQValues = new ArrayList<QValue>();
		List<AbstractGroundedAction> qActions = this.affController.getPrunedActionSetForState(sh.s);
		if (qActions.isEmpty()) {
			throw new RuntimeException("No valid actions have been found for this state. Your affordances may be too strict");
		}

		for(AbstractGroundedAction ga : qActions){
			affFilteredQValues.add(this.getQ(sh, (GroundedAction)ga, matching));
		}
		
		//for(QValue q : allQValues){
		//	qActions.add(q.a);
		//}
		
		//qActions = 
		
		//for(QValue q : allQValues){
		//	if(qActions.contains(q.a)){
		//		affFilteredQValues.add(q);
		//	}
		//}
		
		// If Affordances prune away all actions, back off to full action set 
		//if (affFilteredQValues.isEmpty()) {
		//	affFilteredQValues = allQValues;
		//}

		// Find max Q values
		//List <QValue> maxActions = new ArrayList<QValue>();
		//maxActions.add(affFilteredQValues.get(0));
		for (QValue q : affFilteredQValues) {
			//if(q.q == maxQ){
			//	maxActions.add(q);
			//}
			//else if(q.q > maxQ){
			//	maxActions.clear();
			//	maxActions.add(q);
			if (q.q > maxQ) {
				maxQ = q.q;
			}
		}
	
		// perform bellman update
		valueFunction.put(sh, maxQ);
	
		return maxQ;
	}
}

