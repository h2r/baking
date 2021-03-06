package edu.brown.cs.h2r.baking.actions;

import java.util.List;
import java.util.Set;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.ObjectFactories.MakeSpanFactory;


public abstract class BakingAction extends Action {
	IngredientRecipe ingredient;
	public BakingAction() {
	}

	public BakingAction(String name, Domain domain, String parameterClasses) {
		super(name, domain, parameterClasses);
	}

	public BakingAction(String name, Domain domain, String[] parameterClasses) {
		super(name, domain, parameterClasses);
	}

	public BakingAction(String name, Domain domain, String[] parameterClasses,
			String[] parameterOrderGroups) {
		super(name, domain, parameterClasses, parameterOrderGroups);
	}
	
	public BakingAction(String name, Domain domain, IngredientRecipe ingredient, String parameterClasses) {
		super(name, domain, parameterClasses);
		this.ingredient = ingredient;
	}

	public BakingAction(String name, Domain domain, IngredientRecipe ingredient, String[] parameterClasses) {
		super(name, domain, parameterClasses);
		this.ingredient = ingredient;
	}

	public BakingAction(String name, Domain domain, IngredientRecipe ingredient, String[] parameterClasses,
			String[] parameterOrderGroups) {
		super(name, domain, parameterClasses, parameterOrderGroups);
		this.ingredient = ingredient;
	}
	
	public BakingActionResult checkActionIsApplicableInState(State state, String[] params) {
		for (int i = 0, len = params.length; i < len; i++) {
			String objectName = params[i];
			ObjectInstance object = state.getObject(objectName);
			if (object == null) {
				return BakingActionResult.failure(objectName + " does not exist");
			}
			String className = object.getObjectClass().name;
			if (!className.equalsIgnoreCase(this.parameterClasses[i])) {
				return BakingActionResult.failure(objectName + " is not valid for this action");
			}
		}
		if (!this.canAgentGo(state, params)) {
			return BakingActionResult.failure(params[0] + " is not a valid agent to take this action");
		}
		
		return BakingActionResult.success();
	}
	
	@Override
	public boolean applicableInState(State state, String[] params) {
		return this.checkActionIsApplicableInState(state, params).getIsSuccess();
	}

	@Override
	protected State performActionHelper(State state, String[] params) {
		this.addAgentToOccupiedList(state, params[0]);
		return state;	
	}
	
	protected boolean canAgentGo(State state, String[] params) {
		List<ObjectInstance> makeSpanObjects = state.getObjectsOfTrueClass(MakeSpanFactory.ClassName);
		if (!makeSpanObjects.isEmpty()) {
			ObjectInstance makeSpanObject = makeSpanObjects.get(0);
			String primaryAgent = MakeSpanFactory.getPrimaryAgent(makeSpanObject);
			if (primaryAgent.isEmpty())
			{
				return true;
			}
			if (params[0].equalsIgnoreCase(primaryAgent)) {
				return true;
			}
			Set<String> agents = MakeSpanFactory.getOccupiedAgentNames(makeSpanObject);
			if (agents.size() == MakeSpanFactory.getAgentCount(makeSpanObject)) {
				return false;
			}
			if (agents.contains(primaryAgent)) {
				return true;
			}
			return false;
		}
		// If make span is not used here, it's always good.
		return true;
	}
	
	protected boolean checkValidAgent(State state, String agentToCheck) {
		List<ObjectInstance> makeSpanObjects = state.getObjectsOfTrueClass(MakeSpanFactory.ClassName);
		if (!makeSpanObjects.isEmpty()) {
			return MakeSpanFactory.isAgentIsFree(makeSpanObjects.get(0), agentToCheck);
		}
		// If make span is not used here, it's always good.
		return true;
	}
	
	protected void addAgentToOccupiedList(State state, String agentName) {
		List<ObjectInstance> makeSpanObjects = state.getObjectsOfTrueClass(MakeSpanFactory.ClassName);
		if (!makeSpanObjects.isEmpty()) {
			MakeSpanFactory.occupyAgent(makeSpanObjects.get(0), agentName);
		}		
	}
	
	public void changePlanningIngredient(IngredientRecipe newIngredient) {
		this.ingredient = newIngredient;
	}
}
