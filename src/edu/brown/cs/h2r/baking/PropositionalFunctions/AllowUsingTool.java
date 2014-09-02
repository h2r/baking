package edu.brown.cs.h2r.baking.PropositionalFunctions;

import java.util.Set;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.AffordanceCreator;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ToolFactory;
import edu.brown.cs.h2r.baking.Recipes.Recipe;

public class AllowUsingTool extends BakingPropositionalFunction {

	public AllowUsingTool(String name, Domain domain, IngredientRecipe ingredient) {
		super(name, domain, new String[] {AgentFactory.ClassName, ToolFactory.ClassName, ContainerFactory.ClassName} ,ingredient);
	}
	
	public boolean isTrue(State state, String[] params) {
		return false; /*
		ObjectInstance tool = state.getObject(params[1]);
		ObjectInstance container = state.getObject(params[2]);
		String attribute = ToolFactory.getToolAttribute(tool);
		Set<String> tlAttributes = this.topLevelIngredient.getRecipeToolAttributes();
		if (tlAttributes == null) {
			return false;
		}
		if (!ToolFactory.toolCanCarry(tool)) {
			return tlAttributes.contains(attribute);
		}
		if (ToolFactory.isEmpty(tool)) {
			if (tlAttributes.isEmpty()) {
				return false;
			}
			if (attribute.equals("strained")) {
				for (String name : ContainerFactory.getContentNames(container)) {
					ObjectInstance obj = state.getObject(name);
					if (IngredientFactory.getHeatedState(obj) != "boiled") {
						return false;
					}
				}
				return true;
			}
			return tlAttributes.contains(attribute);
		} else {
			ObjectInstance fakeContainer = ContainerFactory.getNewFakeToolContainerObjectInstance(domain, attribute, 
					ToolFactory.getContents(tool), ContainerFactory.getSpaceName(container));
			state.addObject(fakeContainer);
			AllowPouring allowPouring = ((AllowPouring)domain.getPropFunction(AffordanceCreator.POUR_PF));
			boolean allow = allowPouring.isTrue(state, new String[] {params[0], fakeContainer.getName(), params[2]});
			state.removeObject(fakeContainer.getName());
			return allow;	
		} */
	}
}
