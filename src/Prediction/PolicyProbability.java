package Prediction;

import burlap.behavior.singleagent.Policy;
import burlap.oomdp.core.TerminalFunction;
import edu.brown.cs.h2r.baking.BakingSubgoal;
import edu.brown.cs.h2r.baking.Experiments.KitchenSubdomain;

public final class PolicyProbability {

	private final double probability;
	private final KitchenSubdomain policyDomain;
	
	private PolicyProbability(KitchenSubdomain policyDomain, double probability) {
		this.probability = probability;
		this.policyDomain = policyDomain;
	}

	public PolicyProbability() {
		this.probability = 0.0;
		this.policyDomain = null;
	}

	public double getProbability() {
		return this.probability;
	}
	
	public KitchenSubdomain getPolicyDomain() {
		return this.policyDomain;
	}
	
	public static PolicyProbability newPolicyProbability(KitchenSubdomain policyDomain, double probability) {
		if (policyDomain == null) {
			return null;
		}

		return new PolicyProbability(policyDomain, probability);	
	}
	
	public static PolicyProbability updatePolicyProbability(PolicyProbability prior, double newProbability) {
		return (prior == null) ? null :
			new PolicyProbability(prior.policyDomain, newProbability);
	}
	
	@Override
	public String toString() {
		return this.policyDomain.toString() + ": " + this.probability;
	}
}
