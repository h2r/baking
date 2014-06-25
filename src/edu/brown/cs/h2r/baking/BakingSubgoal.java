package edu.brown.cs.h2r.baking;

import java.util.HashSet;
import java.util.Set;

import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.logicalexpressions.PFAtom;

public class BakingSubgoal {

	PFAtom goal;
	Set<PFAtom> preconditions;
	
	public BakingSubgoal(PropositionalFunction pf, String[] params) {
		GroundedProp gp = new GroundedProp(pf, params);
		this.goal = new PFAtom(gp);
		this.preconditions = new HashSet<PFAtom>(); 	
	}
	
	public PFAtom getGoal() {
		return this.goal;
	}
	
	public Set<PFAtom> getPreconditions() {
		return this.preconditions;
	}
	
	public void addPrecondition(PropositionalFunction pf, String[] params) {
		GroundedProp gp = new GroundedProp(pf, params);
		PFAtom atom = new PFAtom(gp);
		preconditions.add(atom);
		//preconditions.add(new PFAtom(new GroundedProp(pf, params)));
	}
	
	public Boolean goalCompleted(State state) {
		return this.goal.evaluateIn(state);
	}
	
	public Boolean preconditionsSatisfied(State state) {
		for (PFAtom pc : this.preconditions) {
			if (!pc.evaluateIn(state)) {
				return false;
			}
		}
		return true;
	}
}
