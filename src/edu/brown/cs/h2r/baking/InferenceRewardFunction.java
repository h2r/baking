package edu.brown.cs.h2r.baking;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

public class InferenceRewardFunction implements RewardFunction {
	List<RewardFunction> rewardFunctions;
	List<Double> beliefs;
	
	public InferenceRewardFunction(List<RewardFunction> rewardFunctions) {
		this.rewardFunctions = new ArrayList<RewardFunction>(rewardFunctions);
		this.beliefs = new ArrayList<Double>();
		for (RewardFunction rf : this.rewardFunctions) {
			this.beliefs.add(1.0 / this.rewardFunctions.size());
		}
	}

	public void updateBeliefs(List<Double> values) {
		for (int i = 0; i < this.beliefs.size(); i++)
		{
			this.beliefs.set(i, this.beliefs.get(i) * values.get(i));
		}
	}
	
	@Override
	public double reward(State s, GroundedAction a, State sprime) {
		double reward = 0;
		double sumBeliefs = 0;
		for (int i =0; i < this.rewardFunctions.size(); i++)
		{
			sumBeliefs += this.beliefs.get(i);
			reward += this.rewardFunctions.get(i).reward(s, a, sprime) * this.beliefs.get(i);
		}
		return reward / sumBeliefs;
	}

}
