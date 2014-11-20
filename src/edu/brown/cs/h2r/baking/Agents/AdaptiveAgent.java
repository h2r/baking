package edu.brown.cs.h2r.baking.Agents;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import burlap.behavior.singleagent.Policy;
import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.RewardFunction;
import edu.brown.cs.h2r.baking.RecipeAgentSpecificMakeSpanRewardFunction;
import edu.brown.cs.h2r.baking.Experiments.KitchenSubdomain;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.Prediction.PolicyProbability;

public abstract class AdaptiveAgent implements Agent {
	private final Domain domain;
	protected final static StateHashFactory hashingFactory = new NameDependentStateHashFactory();
	protected final static RewardFunction rewardFunction = new RecipeAgentSpecificMakeSpanRewardFunction(Human.HUMAN_NAME);;
	protected final List<State> stateHistory;
	private final List<PolicyProbability> policyBeliefDistribution;
	protected final List<KitchenSubdomain> subdomains;
	
	public AdaptiveAgent(Domain domain) {
		this.domain = domain;
		this.stateHistory = new ArrayList<State>();
		this.subdomains = new ArrayList<KitchenSubdomain>();
		this.policyBeliefDistribution = new ArrayList<PolicyProbability>();
	}
	
	@Override
	public ObjectInstance getAgentObject() {
		return AgentFactory.getNewHumanAgentObjectInstance(this.domain, this.getAgentName(), this.hashingFactory.getObjectHashFactory());
	}
	
	@Override
	public void setInitialState(State state) {
		this.stateHistory.clear();
		this.subdomains.clear();
		this.policyBeliefDistribution.clear();
		this.stateHistory.add(state);
		List<KitchenSubdomain> subdomains = AgentHelper.generateAllRTDPPolicies(domain, state, AgentHelper.recipes(domain),
				AdaptiveAgent.rewardFunction ,AdaptiveAgent.hashingFactory);
		this.subdomains.addAll(subdomains);
		this.policyBeliefDistribution.addAll(this.getInitialPolicyDistribution(subdomains));
		this.init();
	}
	
	private final List<PolicyProbability> getInitialPolicyDistribution(List<KitchenSubdomain> subdomains) {
		List<PolicyProbability> distribution = new ArrayList<PolicyProbability>(subdomains.size());
		double uniformProbability = 1.0 / subdomains.size();
		
		for (KitchenSubdomain subdomain : subdomains) {
			PolicyProbability policyProbability = 
					PolicyProbability.newPolicyProbability(subdomain, uniformProbability);
			distribution.add(policyProbability);
		}
		
		return distribution;
	}

	@Override
	public AbstractGroundedAction getAction(State state) {
		List<PolicyProbability> policyDistribution = this.getPolicyDistribution(state);
		this.updateBeliefDistribution(policyDistribution);
		
		List<PolicyProbability> policyBeliefDistribution = Collections.unmodifiableList(this.policyBeliefDistribution);
		return this.getActionFromPolicyDistribution(policyBeliefDistribution, state);
		
	}
	
	protected abstract List<PolicyProbability> getPolicyDistribution(State currentState);
	protected abstract AbstractGroundedAction getActionFromPolicyDistribution(List<PolicyProbability> policyDistribution, State state);
	protected abstract void init();
	@Override
	public void addObservation(State state) {
		this.stateHistory.add(state);
		
	}
	
	protected void updateBeliefDistribution(List<PolicyProbability> updatePolicyDistribution) {
		
		double sumProbability = 0.0;
		double previousSumProbability = 0.0;
		double updateSumProbability = 0.0;
		for (int i = 0; i < this.policyBeliefDistribution.size(); i++) {
			PolicyProbability priorBelief = this.policyBeliefDistribution.get(i);
			double beliefProbability = priorBelief.getProbability();
			previousSumProbability += beliefProbability;
			
			PolicyProbability update = updatePolicyDistribution.get(i);
			double updateProbability = update.getProbability();
			updateSumProbability += updateProbability;
			
			double newProbability = beliefProbability * updateProbability;
			sumProbability += newProbability;
			PolicyProbability updatedBelief = PolicyProbability.updatePolicyProbability(priorBelief, newProbability);
			this.policyBeliefDistribution.set(i, updatedBelief);
			
		}
		
		if (previousSumProbability == 0.0) {
			//System.err.println("All previous probabilities are 0.0");
		}
		
		if (updateSumProbability == 0.0) {
			//System.err.println("All update probabilities are 0.0");
		}
		
		
		for (int i = 0; i < this.policyBeliefDistribution.size(); i++) {
			PolicyProbability belief = this.policyBeliefDistribution.get(i);
			double beliefProbability = belief.getProbability();
			
			double normalizedProbability = (sumProbability == 0.0) ? 0.0 : beliefProbability / sumProbability;
			PolicyProbability normalizedBelief = PolicyProbability.updatePolicyProbability(belief, normalizedProbability);
			this.policyBeliefDistribution.set(i, normalizedBelief);
			
		}
	}
	
	protected abstract double getTransitionProbability(Policy from, Policy to);
	
}
