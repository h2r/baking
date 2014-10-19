package edu.brown.cs.h2r.baking;
import java.util.Arrays;
import java.util.List;

import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;

public class RecipeTerminalFunction implements TerminalFunction{

	List<PropositionalFunction> terminalConditions;
	
	
	public RecipeTerminalFunction(PropositionalFunction ... terminalConditions) {
		this.terminalConditions = Arrays.asList(terminalConditions);
	}

	@Override
	public boolean isTerminal(State state) {
		for (PropositionalFunction pf : this.terminalConditions) {
			for (GroundedProp prop : pf.getAllGroundedPropsForState(state)) {
				if (prop.isTrue(state))  {
					return true;
				}
			}
		}
		return false;
	}

}
