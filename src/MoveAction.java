import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;

public class MoveAction extends Action {
	public static final String className = "move";
	public MoveAction(Domain domain) {
		super("move", domain, new String[] {AgentFactory.ClassName, ContainerFactory.ClassName, SpaceFactory.ClassName});
	}
	
	@Override
	protected State performActionHelper(State state, String[] params) {
		//System.out.println("Moving container " + params[1] + " to " + params[2]);
		ObjectInstance containerInstance = state.getObject(params[1]);
		ContainerFactory.addIngredient(containerInstance, params[2]);
		return state;
	}
}