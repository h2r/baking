package edu.brown.cs.h2r.baking;
import java.util.List;
import java.util.Set;

import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import edu.brown.cs.h2r.baking.ObjectFactories.MakeSpanFactory;
import edu.brown.cs.h2r.baking.actions.ResetAction;
import edu.brown.cs.h2r.baking.actions.WaitAction;

public class RecipeAgentSpecificMakeSpanRewardFunction implements RewardFunction {

	protected String agent;
	private double costMe;
	private double costYou;
	public RecipeAgentSpecificMakeSpanRewardFunction(String agent) {
		this.agent = agent;
		this.costMe = -1;
		this.costYou = -2;
	}
	
	public RecipeAgentSpecificMakeSpanRewardFunction(String agent, double costMe, double costYou) {
		this.agent = agent;
		this.costMe = costMe;
		this.costYou = costYou;
	}

	@Override
	public double reward(State state, GroundedAction a, State sprime) {
		double actionFactor = (a.action instanceof ResetAction) ? 2.0 : 1.0;
		List<ObjectInstance> objectInstances = state.getObjectsOfTrueClass(MakeSpanFactory.ClassName);
		if (!objectInstances.isEmpty()) {
			Set<String> occupiedAgents = MakeSpanFactory.getOccupiedAgentNames(objectInstances.get(0));
			if (occupiedAgents.contains(a.params[0]) || occupiedAgents.isEmpty()) {
				return (a.params[0].equals(this.agent)) ? actionFactor * this.costMe : actionFactor * this.costYou;
			}
			else if (!(a.action instanceof WaitAction)){
				return -0.5;
			} else {
				return -0.5;
			}
		}
		else {
			return (a.params[0] == this.agent) ? actionFactor * this.costMe : actionFactor * this.costYou;
		}

		//String[] classes = a.action.getParameterClasses();
		//for (int i = 0; i < classes.length; ++i)
		//{
		//	if (classes[i] == AgentFactory.ClassName)
		//	{
		//		ObjectInstance agent = state.getObject(a.params[i]);
		//		if (AgentFactory.isRobot(agent)) {
		//			return -1;
		//		}
		//	}
		//}
		//return -2;
	}
}
