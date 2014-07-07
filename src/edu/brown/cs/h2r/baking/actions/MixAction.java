package edu.brown.cs.h2r.baking.actions;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import static java.util.Arrays.asList;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.Experiments.ExperimentHelper;
import edu.brown.cs.h2r.baking.Knowledgebase.IngredientKnowledgebase;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.actions.BakingAction;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;


public class MixAction extends BakingAction {	
	public static final String className = "mix";
	private IngredientKnowledgebase knowledgebase;
	public MixAction(Domain domain, IngredientRecipe ingredient) {
		super(MixAction.className, domain, ingredient, new String[] {AgentFactory.ClassName, ContainerFactory.ClassName});
		this.knowledgebase = new IngredientKnowledgebase();
	}
	
	@Override
	public ApplicableInStateResult checkActionIsApplicableInState(State state, String[] params) {
		ApplicableInStateResult superResult = super.checkActionIsApplicableInState(state, params);
		
		if (!superResult.getIsApplicable()) {
			return superResult;
		}

		String agentName = params[0];
		ObjectInstance agent =  state.getObject(params[0]);
		
		if (AgentFactory.isRobot(agent)) {
			return ApplicableInStateResult.False("Robot cannot perform this action");
		}
		
		String containerName = params[1];
		ObjectInstance containerInstance = state.getObject(containerName);
		
		if (ContainerFactory.getContentNames(containerInstance).isEmpty()) {
			return ApplicableInStateResult.False(containerName + " is empty");
		}
		
		if (!ContainerFactory.isMixingContainer(containerInstance)) {
			return ApplicableInStateResult.False(containerName + " is not a mixing container");
		}
		// move to should mix probably!
		if (ContainerFactory.getContentNames(containerInstance).size() < 2) {
			return ApplicableInStateResult.False(containerName + " containers only one ingredient");
		}
		
		String containerSpaceName = ContainerFactory.getSpaceName(containerInstance);
		if (containerSpaceName == null) {
			return ApplicableInStateResult.False(containerName + " is not in any space");
		}

		ObjectInstance mixingContainerSpaceName = state.getObject(containerSpaceName);
		if (mixingContainerSpaceName == null) {
			return ApplicableInStateResult.False(containerSpaceName + " does not exist");
		}
		
		String agentOfSpace = SpaceFactory.getAgent(mixingContainerSpaceName).iterator().next();
		if (!agentOfSpace.equalsIgnoreCase(agent.getName()))
		{		
			return ApplicableInStateResult.False(agentName + " cannot perform actions in " + containerSpaceName);
		}
				
		if (!SpaceFactory.isWorking(mixingContainerSpaceName)) {
			return ApplicableInStateResult.False(containerSpaceName + " is not suitable for mixing");
		}
		return ApplicableInStateResult.True();
	}
	
	@Override
	public boolean applicableInState(State state, String[] params) {
		return this.checkActionIsApplicableInState(state, params).getIsApplicable();
	}

	@Override
	protected State performActionHelper(State state, String[] params) {
		super.performActionHelper(state, params);
		ObjectInstance containerInstance = state.getObject(params[1]);
		this.mix(state, containerInstance);
		return state;
	}
	public void mix(State state, String container) {
		mix(state, state.getObject(container));
	}
	public void mix(State state, ObjectInstance container)
	{	
		ObjectClass complexIngredientClass = this.domain.getObjectClass(IngredientFactory.ClassNameComplex);
		Random rando = new Random();
		Set<String> contents = ContainerFactory.getContentNames(container);
		String res;
		if (!(res  = knowledgebase.canCombine(state, container)).equals("")) {
			knowledgebase.combineIngredients(state, domain, ingredient, container, res);
		} else {
			Set<ObjectInstance> hidden_copies = new HashSet<ObjectInstance>();
			Set<String> traits = new TreeSet<String>();
			Set<ObjectInstance> objects = new HashSet<ObjectInstance>();
			for (String obj : contents) {
				objects.add(state.getObject(obj));
			}
			ObjectInstance[] objectArray = new ObjectInstance[objects.size()];
			objects.toArray(objectArray);
			//find mutual traits
			for (String trait: IngredientFactory.getTraits(objectArray[0])) {
				if (IngredientFactory.getTraits(objectArray[1]).contains(trait)) {
					traits.add(trait);
				}
			}
			// hide objects
			for (String name: contents) {
				ObjectInstance ing = state.getObject(name);
				if (!IngredientFactory.isSimple(ing)) {
					hidden_copies.add(IngredientFactory.makeHiddenObjectCopy(state, this.domain, ing));
				}
			}
			
			// TODO: Reevaluate the swapped false here? (5th one)
			ObjectInstance newIngredient = 
					IngredientFactory.getNewComplexIngredientObjectInstance(complexIngredientClass, 
							Integer.toString(rando.nextInt()), IngredientRecipe.generateAttributeNumber(false, false, false, false), false, container.getName(), traits, contents);
			state.addObject(newIngredient);
			ContainerFactory.removeContents(container);
			for (ObjectInstance ob : hidden_copies) {
				state.removeObject(state.getObject(ob.getName()));
				state.addObject(ob);
			}
			
			ContainerFactory.addIngredient(container, newIngredient.getName());
			IngredientFactory.changeIngredientContainer(newIngredient, container.getName());
			
			
			ExperimentHelper.checkIngredientCompleted(ingredient.makeFakeAttributeCopy(newIngredient), state, asList(newIngredient), state.getObjectsOfTrueClass(ContainerFactory.ClassName));
		}
	}
	
	public void changeKnowledgebase(IngredientKnowledgebase kb) {
		this.knowledgebase = kb;
	}
}