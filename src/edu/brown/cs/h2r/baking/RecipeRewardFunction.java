package edu.brown.cs.h2r.baking;
import java.util.List;
import java.util.Set;

import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.MakeSpanFactory;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;

public class RecipeRewardFunction implements RewardFunction {

	public RecipeRewardFunction() {
	}

	@Override
	public double reward(State state, GroundedAction a, State sprime) {
		// Things look ok...for now. Continue!
		//return -1;
		
		
		List<ObjectInstance> objectInstances = state.getObjectsOfTrueClass(MakeSpanFactory.ClassName);
		if (!objectInstances.isEmpty()) {
			Set<String> occupiedAgents = MakeSpanFactory.getOccupiedAgentNames(objectInstances.get(0));
			if (occupiedAgents.contains(a.params[0]) || occupiedAgents.isEmpty()) {
				return -1;
			}
			else {
				return 0;
			}
		}
		else {
			return -1;
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
