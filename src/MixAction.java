import java.util.Random;
import java.util.Set;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;


public class MixAction extends Action {	
	public static final String className = "mix";
	public MixAction(Domain domain) {
		super(MixAction.className, domain, new String[] {AgentFactory.ClassName, ContainerFactory.ClassName});
	}
	
	@Override
	public boolean applicableInState(State state, String[] params) {
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
				
		if (!SpaceFactory.isWorking(pouringContainerSpaceObject)) {
			return false;
		}
		
		return true;
	}

	@Override
	protected State performActionHelper(State state, String[] params) {
		ObjectInstance agent = state.getObject(params[0]);
		ObjectInstance containerInstance = state.getObject(params[1]);
		//System.out.println("Mixing ingredients in container " + containerInstance.getName());
		this.mix(state, containerInstance);
		return state;
	}
	
	protected void mix(State state, ObjectInstance container)
	{	
		ObjectClass complexIngredientClass = this.domain.getObjectClass(IngredientFactory.ClassNameComplex);
		Random rando = new Random();
		Set<String> contents = ContainerFactory.getContentNames(container);
		if (contents.size() > 2) {
			int c = 1;
		}
		ObjectInstance newIngredient = 
				IngredientFactory.getNewComplexIngredientObjectInstance(complexIngredientClass, 
						Integer.toString(rando.nextInt()), false, false, false, container.getName(), contents);
		state.addObject(newIngredient);
		ContainerFactory.removeContents(container);
		IngredientFactory.changeIngredientContainer(newIngredient, container.getName());
	}
}
