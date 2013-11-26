import java.util.List;
import java.util.Set;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;

public class PourAction extends Action {
	public static final String className = "pour";
	public PourAction(Domain domain) {
		super(PourAction.className, domain, new String[] {AgentFactory.ClassName, ContainerFactory.ClassName, ContainerFactory.ClassName});
	}
	
	@Override
	public boolean applicableInState(State state, String[] params) {
		if (params[2].contains("mixing")) {
			String name = params[2];
		}
		ObjectInstance agent = state.getObject(params[0]);
		if (AgentFactory.isRobot(agent)) {
			return false;
		}
		
		ObjectInstance pouringContainer = state.getObject(params[1]);
		if (ContainerFactory.getContentNames(pouringContainer).size() == 0) {
			return false;
		}
		
		ObjectInstance receivingContainer = state.getObject(params[2]);
		if (!ContainerFactory.isReceivingContainer(receivingContainer)) {
			return false;
		}

		String pouringContainerSpace = ContainerFactory.getSpaceName(pouringContainer);
		String receivingContainerSpace = ContainerFactory.getSpaceName(receivingContainer);
		
		
		if (pouringContainerSpace == null || receivingContainerSpace == null)
		{
			throw new RuntimeException("One of the pouring containers is not in any space");
		}
		
		if (pouringContainerSpace != receivingContainerSpace)
		{
			return false;
		}
		ObjectInstance pouringContainerSpaceObject = state.getObject(pouringContainerSpace);
		
		if (SpaceFactory.isWorking(pouringContainerSpaceObject)) {
			return false;
		}

		return true;
	}

	@Override
	protected State performActionHelper(State state, String[] params) {
		ObjectInstance agent = state.getObject(params[0]);
		ObjectInstance pouringContainer = state.getObject(params[1]);
		ObjectInstance receivingContainer = state.getObject(params[2]);
		Set<String> ingredients = ContainerFactory.getContentNames(pouringContainer);
		ContainerFactory.addIngredients(receivingContainer, ingredients);
		ContainerFactory.removeContents(pouringContainer);
		for (String ingredient : ingredients) {
			ObjectInstance ingredientInstance = state.getObject(ingredient); 
			IngredientFactory.changeIngredientContainer(ingredientInstance, receivingContainer.getName());
		}
		//System.out.println("Pour contents of container " + params[1] + " container " + params[2]);
		return state;
	}
	
	protected void pour(ObjectInstance pouringContainer, ObjectInstance receivingContainer)
	{
		
		
	}
}