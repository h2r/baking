package edu.brown.cs.h2r.baking;
import java.util.List;
import java.util.Set;

import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.MakeSpanFactory;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

public class RecipeAgentSpecificRewardFunction implements RewardFunction {

	protected String agent;
	public RecipeAgentSpecificRewardFunction(String agent) {
		this.agent = agent;
	}

	@Override
	public double reward(State state, GroundedAction a, State sprime) {
		// Things look ok...for now. Continue!
		//return -1;
		if (a.params[0] == this.agent){ 
			return -1;
		}
		else {
			return -2;
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
