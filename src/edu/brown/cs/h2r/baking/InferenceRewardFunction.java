package edu.brown.cs.h2r.baking;

import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

public class InferenceRewardFunction implements RewardFunction {
	List<RewardFunction> rewardFunctions;
	private List<Double> beliefs;
	
	public InferenceRewardFunction(List<RewardFunction> rewardFunctions) {
		this.rewardFunctions = new ArrayList<RewardFunction>(rewardFunctions);
		this.setBeliefs(new ArrayList<Double>());
		
		for (int i = 0; i < this.rewardFunctions.size(); ++i) {
			this.getBeliefs().add(1.0 / this.rewardFunctions.size());
		}
	}

	public void updateBeliefs(List<Double> values) {
		for (int i = 0; i < this.getBeliefs().size(); i++)
		{
			this.getBeliefs().set(i, this.getBeliefs().get(i) * values.get(i));
		}
	}
	
	@Override
	public double reward(State state, GroundedAction a, State sprime) {
		double reward = 0;
		//double sumBeliefs = 0;
		double maxBelief = 0;

		for (int i =0; i < this.rewardFunctions.size(); i++)
		{
			if (this.getBeliefs().get(i) > maxBelief) {
				maxBelief = this.getBeliefs().get(i);
				reward = this.rewardFunctions.get(i).reward(state,  a,  sprime);
			}
			//sumBeliefs += this.getBeliefs().get(i);
			//reward += this.rewardFunctions.get(i).reward(s, a, sprime) * this.getBeliefs().get(i);
		}
		return reward;
	}

	public List<Double> getBeliefs() {
		return beliefs;
	}

	public void setBeliefs(List<Double> beliefs) {
		this.beliefs = beliefs;
	}

}
