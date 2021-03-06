package edu.brown.cs.h2r.baking;
import java.util.List;
import java.util.Set;

import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import edu.brown.cs.h2r.baking.ObjectFactories.MakeSpanFactory;

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
		
		List<ObjectInstance> objectInstances = state.getObjectsOfTrueClass(MakeSpanFactory.ClassName);
		if (!objectInstances.isEmpty()) {
			Set<String> occupiedAgents = MakeSpanFactory.getOccupiedAgentNames(objectInstances.get(0));
			if (occupiedAgents.contains(a.params[0]) || occupiedAgents.isEmpty()) {
				return (a.params[0] == this.agent) ? this.costMe : this.costYou;
			}
			else {
				return 0;
			}
		}
		else {
			return (a.params[0] == this.agent) ? this.costMe : this.costYou;
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
