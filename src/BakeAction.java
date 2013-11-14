import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;


public class BakeAction extends Action {
	public static final String className = "bake";
	public BakeAction(Domain domain) {
		super(BakeAction.className, domain, "");
	}
	
	@Override
	public boolean applicableInState(State s, String[] params) {
		
		return false;
	}

	@Override
	protected State performActionHelper(State state, String[] params) {
		this.bake(state, this.domain);
		//System.out.println("Bake!");
		return state;
	}
	
	public void bake(State state, Domain domain)
	{
	}
}
