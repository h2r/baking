package edu.brown.cs.h2r.baking.actions;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;


public class BakeAction extends BakingAction {
	public static final String className = "bake";
	public BakeAction(Domain domain) {
		super(BakeAction.className, domain, new String[] {AgentFactory.ClassName});
	}
	
	@Override
	public boolean applicableInState(State s, String[] params) {
		if (!super.applicableInState(s, params)) {
			return false;
		}
		return false;
	}

	@Override
	protected State performActionHelper(State state, String[] params) {
		super.performActionHelper(state, params);
		this.bake(state, this.domain);
		//System.out.println("Bake!");
		return state;
	}
	
	public void bake(State state, Domain domain)
	{
	}
}
