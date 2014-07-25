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
		
		if (toolInstance.getName().equals(ToolFactory.SPATULA) && spaceName.equals("dirtyCounter")) {
			return false;
		}
		
		// If it is in robot space
		if (toolSpaceName.equals(SpaceFactory.SPACE_ROBOT)) {
			if (dirty) {
				if (spaceName.equals(SpaceFactory.SPACE_DIRTY)) {
					return true;
				}
				if (ContainerFactory.wetContainersFinished(state) || ContainerFactory.dryContainersFinished(state)) {
					String wetSpace = ContainerFactory.getSpaceName(state.getObject(ContainerFactory.WET_BOWL));
					String drySpace = ContainerFactory.getSpaceName(state.getObject(ContainerFactory.DRY_BOWL));
					if (!wetSpace.equals(SpaceFactory.SPACE_HUMAN) || !drySpace.equals(SpaceFactory.SPACE_HUMAN)) {
						return toolInstance.getName().equals(ToolFactory.SPATULA);
					}
				}
			} else {
				if (spaceName.equals(SpaceFactory.SPACE_HUMAN)) {
					if (ContainerFactory.dryContainersFinished(state)) {
						if (toolType.equals(ToolFactory.whiskType)) {
								return !ToolFactory.getUsed(state.getObject(ToolFactory.WHISK));
						} else {
							return ToolFactory.getUsed(state.getObject(ToolFactory.WHISK));
						}
					} else {
						if (toolType.equals(ToolFactory.whiskType)) {
							return false;
						} else {
							return ContainerFactory.wetContainersFinished(state);
						}
					}
				} else {
					return false;
				}
			}
			
		}
		return false;
	}
}
