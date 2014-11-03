package edu.brown.cs.h2r.baking.actions;

import java.util.List;
import java.util.Set;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TransitionProbability;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.ActionObserver;
import burlap.oomdp.singleagent.GroundedAction;
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
	
	public String[] getUsedObjects(State state, String[] params) {
		return params.clone();
	}
	
	@Override
	public List<TransitionProbability> getTransitions(State state, String[] params){
		return this.deterministicTransition(state, params);
	}

	@Override
	public State performAction(State s, String [] params){
		
		State resultState = s;
		//if (!resultState.equals(s)) {
		//	throw new RuntimeException("Semi-deep copying failed to properly duplicate the state");
		//}
		if(!this.applicableInState(s, params)){
			return resultState; //can't do anything if it's not applicable in the state so return the current state
		}
		
		resultState = performActionHelper(resultState, params);
		
		for(ActionObserver observer : this.actionObservers){
			observer.actionEvent(resultState, new GroundedAction(this, params), resultState);
		}
		
		/*int objectCount = 0;
		for (List<ObjectInstance> objects : resultState.getAllObjectsByTrueClass()) {
			objectCount += objects.size();
		}
		if (objectCount != resultState.numTotalObjects()) {
			throw new RuntimeException("Perform action failed to properly copy objects");
		}
		
		for (ObjectInstance object : resultState.getAllObjects()) {
			switch (object.getTrueClassName())
			{
			case IngredientFactory.ClassNameSimple:
			case IngredientFactory.ClassNameSimpleHidden:
				// Nothing to be done
				break;
			case IngredientFactory.ClassNameComplex:
			case IngredientFactory.ClassNameComplexHidden:
				for (String contentName : IngredientFactory.getContentsForIngredient(object)) {
					ObjectInstance contentIngredient = resultState.getObject(contentName);
					if (contentIngredient == null) {
						throw new RuntimeException("Perform action failed to properly copy objects");
					}
				}
				break;
				
			case ContainerFactory.ClassName:
				for (String contentName : ContainerFactory.getContentNames(object)) {
					ObjectInstance contentIngredient = resultState.getObject(contentName);
					if (contentIngredient == null) {
						throw new RuntimeException("Perform action failed to properly copy objects");
					}
				}
				break;
			}
		}*/
		
		
		return resultState;
		
	}
	@Override
	protected State performActionHelper(State state, String[] params) {
		return this.addAgentToOccupiedList(state, params[0]);
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
	
	protected State addAgentToOccupiedList(State state, String agentName) {
		List<ObjectInstance> makeSpanObjects = state.getObjectsOfTrueClass(MakeSpanFactory.ClassName);
		if (!makeSpanObjects.isEmpty()) {
			ObjectInstance newMakeSpan = MakeSpanFactory.occupyAgent(makeSpanObjects.get(0), agentName);
			state = state.replaceObject(makeSpanObjects.get(0), newMakeSpan);
		}		
		return state;
	}
	
	public void changePlanningIngredient(IngredientRecipe newIngredient) {
		this.ingredient = newIngredient;
	}
}
