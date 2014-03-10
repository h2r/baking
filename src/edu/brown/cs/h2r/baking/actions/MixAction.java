package edu.brown.cs.h2r.baking.actions;
import java.util.Random;
import java.util.Set;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.SpaceFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;


public class MixAction extends BakingAction {	
	public static final String className = "mix";
	public MixAction(Domain domain) {
		super(MixAction.className, domain, new String[] {AgentFactory.ClassName, ContainerFactory.ClassName});
	}
	
	@Override
	public boolean applicableInState(State state, String[] params) {
		if (!super.applicableInState(state, params)) {
			return false;
		}
		
		ObjectInstance agent =  state.getObject(params[0]);
		
		if (AgentFactory.isRobot(agent)) {
			return false;
		}
		ObjectInstance containerInstance = state.getObject(params[1]);
		if (!ContainerFactory.isMixingContainer(containerInstance)) {
			return false;
		}
		if (ContainerFactory.getContentNames(containerInstance).size() == 0) {
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

	@Override
	protected State performActionHelper(State state, String[] params) {
		super.performActionHelper(state, params);
		ObjectInstance containerInstance = state.getObject(params[1]);
		this.mix(state, containerInstance);
		return state;
	}
	
	protected void mix(State state, ObjectInstance container)
	{	
		ObjectClass complexIngredientClass = this.domain.getObjectClass(IngredientFactory.ClassNameComplex);
		Random rando = new Random();
		Set<String> contents = ContainerFactory.getContentNames(container);
		ObjectInstance newIngredient = 
				IngredientFactory.getNewComplexIngredientObjectInstance(complexIngredientClass, 
						Integer.toString(rando.nextInt()), false, false, false, container.getName(), contents);
		state.addObject(newIngredient);
		ContainerFactory.removeContents(container);
		ContainerFactory.addIngredient(container, newIngredient.getName());
		IngredientFactory.changeIngredientContainer(newIngredient, container.getName());
		
	}
}
