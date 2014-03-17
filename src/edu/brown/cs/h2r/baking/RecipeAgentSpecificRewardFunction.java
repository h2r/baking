package edu.brown.cs.h2r.baking;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

public class RecipeAgentSpecificRewardFunction implements RewardFunction {

	protected String agent;
	private double youCost;
	private double meCost;
	public RecipeAgentSpecificRewardFunction(String agent) {
		this.agent = agent;
		this.youCost = -2;
		this.meCost = -1;
	}
	
	public RecipeAgentSpecificRewardFunction(String agent, double meCost, double youCost)
	{
		this.agent = agent;
		this.youCost = youCost;
		this.meCost = meCost;
	}
	@Override
	public double reward(State state, GroundedAction a, State sprime) {
		return (a.params[0] == this.agent) ? this.meCost : this.youCost;
	}
}
