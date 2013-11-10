import java.util.List;
import java.util.Set;

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
		String[] classes = a.action.getParameterClasses();
		for (int i = 0; i < classes.length; ++i)
		{
			if (classes[i] == SingleAgentKitchen.CLASSAGENT)
			{
				ObjectInstance agent = state.getObject(a.params[i]);
				if (agent.getDiscValForAttribute(SingleAgentKitchen.ATTROBOT) == 0)
				{
					return -2;
				}
			}
		}
		return -1;
	}
}
