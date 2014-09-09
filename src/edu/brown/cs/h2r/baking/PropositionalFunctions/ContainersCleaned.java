
package edu.brown.cs.h2r.baking.PropositionalFunctions;

import java.util.Set;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ToolFactory;

public class ContainersCleaned extends BakingPropositionalFunction {

	public ContainersCleaned(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[]{}, ingredient) ;
	}
	@Override
	public boolean isTrue(State state, String[] params) {
		for (ObjectInstance container : state.getObjectsOfTrueClass(ContainerFactory.ClassName)) {
			boolean isUsed = ContainerFactory.getUsed(container);
			boolean isEmpty = ContainerFactory.isEmptyContainer(container);
			
			String spaceName = ContainerFactory.getSpaceName(container);
			ObjectInstance space = state.getObject(spaceName);
			boolean inSink = SpaceFactory.isCleaning(space);
			if (isUsed && isEmpty) {
				if (!inSink) {
					return false;
				}
			}
		}
		/*s
		for (ObjectInstance tool : state.getObjectsOfTrueClass(ToolFactory.ClassName)) {
			boolean isUsed = ToolFactory.isUsed(tool);
			
			String spaceName = ToolFactory.getSpaceName(tool);
			ObjectInstance space = state.getObject(spaceName);
			boolean inSink = SpaceFactory.isCleaning(space);
			boolean inRobotCounter = spaceName.equals(SpaceFactory.SPACE_ROBOT);
			if (isUsed && inRobotCounter) {
				return false;
			}
			if (isUsed) {
				return true;
			}
		}*/
		return true;
	}
}