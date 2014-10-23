package Prediction;

import burlap.behavior.singleagent.Policy;

public class PolicyProbability {

	private double probability;
	private Policy policy;
	
	private PolicyProbability(Policy policy, double probability) {
		this.probability = probability;
		this.policy = policy;
	}

	public PolicyProbability() {
		this.policy = null;
		this.probability = 0.0;
	}

	public double getProbability() {
		return this.probability;
	}
	
	public Policy getPolicy() {
		return this.policy;
	}
	
	public static PolicyProbability newPolicyProbability(Policy policy, double probability) {
		if (policy == null) {
			return null;
		}

		return new PolicyProbability(policy, probability);
		
	}
}
