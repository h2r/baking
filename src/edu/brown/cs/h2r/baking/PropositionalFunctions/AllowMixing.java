package edu.brown.cs.h2r.baking.PropositionalFunctions;

import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class AllowMixing extends BakingPropositionalFunction {

	public AllowMixing(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[]{AgentFactory.ClassName, ContainerFactory.ClassName}, ingredient) ;
	}
	@Override
	// Default true for now until I find better logic.
	public boolean isTrue(State state, String[] params) {
		// TODO Auto-generated method stub
		ObjectInstance agent =  state.getObject(params[0]);
		ObjectInstance containerInstance = state.getObject(params[1]);
		if (ContainerFactory.getContentNames(containerInstance).isEmpty()) {
			return false;
		}
		
		
		if (AgentFactory.isRobot(agent)) {
			return false;
		}
		if (!ContainerFactory.isMixingContainer(containerInstance)) {
			return false;
		}
		// move to should mix probably!
		if (ContainerFactory.getContentNames(containerInstance).size() < 2) {
			return false;
		}
		
		String containerSpaceName = ContainerFactory.getSpaceName(containerInstance);
		if (containerSpaceName == null) {
			return false;
		}

		ObjectInstance pouringContainerSpaceObject = state.getObject(containerSpaceName);
		if (pouringContainerSpaceObject == null) {
			return false;
		}
		
		String agentOfSpace = SpaceFactory.getAgent(pouringContainerSpaceObject).iterator().next();
		if (agentOfSpace != agent.getName())
		{		
			return false;
		}
				
		if (!SpaceFactory.isWorking(pouringContainerSpaceObject)) {
			return false;
		}
		
		return true;
	}

}
