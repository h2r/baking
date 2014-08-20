package edu.brown.cs.h2r.baking;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;

public class StatePolicyAnalysis {
	Policy policy;
	
	public StatePolicyAnalysis(Policy policy) {
		this.policy = policy;
	}
	
	public boolean isStateInPolicySpace(State state) {
		return this.policy.isDefinedFor(state);
	}
	
	public double getMinimumCostFromStateToState(final State from, final State to, OOMDPPlanner planner) {
		final StateHashFactory hashFactory = planner.getHashingFactory();
		final StateHashTuple toTuple = hashFactory.hashState(to);
		TerminalFunction tf = new TerminalFunction () {
			@Override
			public boolean isTerminal(State s) {
				return toTuple.equals(hashFactory.hashState(s));
			}
		};
		
		EpisodeAnalysis episodeAnalysis = this.policy.evaluateBehavior(from, planner.getRF(), tf);
		return episodeAnalysis.getDiscountedReturn(1.0);
	}
}
