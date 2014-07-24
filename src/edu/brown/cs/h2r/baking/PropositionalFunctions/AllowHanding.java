package edu.brown.cs.h2r.baking.PropositionalFunctions;

import java.util.List;
import java.util.Set;

import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ToolFactory;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;

public class AllowHanding extends BakingPropositionalFunction {

	public AllowHanding(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[]{AgentFactory.ClassName, ToolFactory.ClassName, SpaceFactory.ClassName}, ingredient) ;
	}
	@Override
	public boolean isTrue(State state, String[] params) {
		if (params[0].equals(AgentFactory.agentHuman)) {
			return false;
		}
		ObjectInstance spaceInstance = state.getObject(params[2]);
		ObjectInstance toolInstance = state.getObject(params[1]);
		
		String spaceName = spaceInstance.getName();
		String toolSpaceName = ToolFactory.getSpaceName(toolInstance);
		String toolType = ToolFactory.getType(toolInstance);
		
		boolean dirty = ToolFactory.getUsed(toolInstance);
		
		// If it is in robot space
		if (toolSpaceName.equals(SpaceFactory.SPACE_ROBOT)) {
			if (dirty) {
				if (spaceName.equals(SpaceFactory.SPACE_DIRTY)) {
					return true;
				}
				return false;
			} else {
				if (spaceName.equals(SpaceFactory.SPACE_HUMAN)) {
					if (toolType.equals(ToolFactory.whiskType)) {
						return ContainerFactory.dryContainersFinished(state);
					} else {
						return ContainerFactory.wetContainersFinished(state);
					}
					
				} else {
					return false;
				}
			}
			
		}
		return false;
	}
}
