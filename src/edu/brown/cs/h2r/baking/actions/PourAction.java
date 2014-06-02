package edu.brown.cs.h2r.baking.actions;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.HashMap;

import burlap.oomdp.core.Domain;
//import edu.brown.cs.h2r.baking.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;

public class PourAction extends BakingAction {
	public static final String className = "pour";
	public PourAction(Domain domain, IngredientRecipe ingredient) {
		super(PourAction.className, domain, ingredient, new String[] {AgentFactory.ClassName, ContainerFactory.ClassName, ContainerFactory.ClassName});
		this.domain = domain;
	}
	
	@Override
	public boolean applicableInState(State state, String[] params) {
		if (!super.applicableInState(state, params)) {
			return false;
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
		
		
		/* Traits */
		
		PropositionalFunction affordance_test = domain.getPropFunction("affordances");
		for (String ing : pouringContainer.getAllRelationalTargets("contains")) {
			ObjectInstance ob = state.getObject(ing);
			Set<String> trait_set = ob.getAllRelationalTargets("traits");
			String[] traits = new String[trait_set.size()];
			trait_set.toArray(traits);
			if (!affordance_test.isTrue(state, traits)) {
				return false;
			}
			// No traits were matched, ingredient not necessary!
			//return false;
		}
		/* Traits end */
	
		
		
		/* Proposed affordance, using edu.brown.cs.h2r.baking.Domain.java as Domain
		if (((Domain) domain).hasAffordances()) {
			HashMap<String,Boolean> affordances = ((Domain) domain).getAffordances();
			
			Boolean pour_match = false;
			Boolean receive_match = false;
			for (String key : affordances.keySet()) {
				int val = (affordances.get(key)) ? 1 : 0;
				if (val == pouringContainer.getRealValForAttribute(key)) {
					pour_match = true;
				}
				if (val == receivingContainer.getRealValForAttribute(key)) {
					receive_match = true;
				}
			}
			if (!(pour_match && receive_match)) {
				return false;
			}
		}
		*/
		
		if (pouringContainerSpace == null || receivingContainerSpace == null)
		{
			throw new RuntimeException("One of the pouring containers is not in any space");
		}
		
		if (pouringContainerSpace != receivingContainerSpace)
		{
			return false;
		}
		ObjectInstance pouringContainerSpaceObject = state.getObject(pouringContainerSpace);
		
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
		ObjectInstance pouringContainer = state.getObject(params[1]);
		ObjectInstance receivingContainer = state.getObject(params[2]);
		Set<String> ingredients = new HashSet<String>(ContainerFactory.getContentNames(pouringContainer));
		ContainerFactory.addIngredients(receivingContainer, ingredients);
		ContainerFactory.removeContents(pouringContainer);
		for (String ingredient : ingredients) {
			ObjectInstance ingredientInstance = state.getObject(ingredient); 
			IngredientFactory.changeIngredientContainer(ingredientInstance, receivingContainer.getName());
		}
		return state;
	}
	
	protected void pour(ObjectInstance pouringContainer, ObjectInstance receivingContainer)
	{
		
		
	}
}