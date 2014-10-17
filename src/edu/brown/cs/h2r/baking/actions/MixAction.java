package edu.brown.cs.h2r.baking.actions;
import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Experiments.ExperimentHelper;
import edu.brown.cs.h2r.baking.Knowledgebase.Knowledgebase;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.Recipes.Recipe;


public class MixAction extends BakingAction {	
	public static final String className = "mix";
	private Knowledgebase knowledgebase;
	public MixAction(Domain domain, IngredientRecipe ingredient) {
		super(MixAction.className, domain, ingredient, new String[] {AgentFactory.ClassName, ContainerFactory.ClassName});
		this.knowledgebase = new Knowledgebase();
	}
	
	@Override
	public BakingActionResult checkActionIsApplicableInState(State state, String[] params) {
		BakingActionResult superResult = super.checkActionIsApplicableInState(state, params);
		
		if (!superResult.getIsSuccess()) {
			return superResult;
		}

		String agentName = params[0];
		ObjectInstance agent =  state.getObject(agentName);
		
		if (AgentFactory.isRobot(agent)) {
			return BakingActionResult.failure("Robot cannot perform this action");
		}
		
		String containerName = params[1];
		ObjectInstance containerInstance = state.getObject(containerName);
		
		if (ContainerFactory.getContentNames(containerInstance).isEmpty()) {
			return BakingActionResult.failure(containerName + " is empty");
		}
		
		// move to should mix probably!
		if (ContainerFactory.getContentNames(containerInstance).size() < 2) {
			return BakingActionResult.failure(containerName + " containers only one ingredient");
		}
		
		String containerSpaceName = ContainerFactory.getSpaceName(containerInstance);
		if (containerSpaceName == null) {
			return BakingActionResult.failure(containerName + " is not in any space");
		}

		ObjectInstance mixingContainerSpaceName = state.getObject(containerSpaceName);
		if (mixingContainerSpaceName == null) {
			return BakingActionResult.failure(containerSpaceName + " does not exist");
		}

		if (SpaceFactory.isBaking(mixingContainerSpaceName)) {
			return BakingActionResult.failure(mixingContainerSpaceName + " is not suitable for mixing!");
		}
		return BakingActionResult.success();
	}
	
	@Override
	public String[] getUsedObjects(State state, String[] params) {
		List<String> usedObjects =  new ArrayList<String>(Arrays.asList(params));
		ObjectInstance container = state.getObject(params[1]);
		Set<String> contents = ContainerFactory.getContentNames(container);
		
		for (String ingredient : contents) {
			ObjectInstance obj = state.getObject(ingredient);
			usedObjects.addAll(IngredientFactory.getRecursiveContentsAndSwapped(state, obj));
		}
		
		usedObjects.addAll(contents);
		String[] usedObjectsArray = new String[usedObjects.size()];
		return usedObjects.toArray(usedObjectsArray);
	}
	
	@Override
	public boolean applicableInState(State state, String[] params) {
		return this.checkActionIsApplicableInState(state, params).getIsSuccess();
	}

	@Override
	protected State performActionHelper(State state, String[] params) {
		super.performActionHelper(state, params);
		ObjectInstance containerInstance = state.getObject(params[1]);
		return this.mix(state, containerInstance);
	}
	
	private State mix(State state, ObjectInstance container)
	{	
		ObjectClass complexIngredientClass = this.domain.getObjectClass(IngredientFactory.ClassNameComplex);
		Random rando = new Random();
		Set<String> contents = ContainerFactory.getContentNames(container);
		String res = knowledgebase.canCombine(state, container);
		if (!res.equals("")) {
			this.combineIngredients(state, domain, ingredient, container, res);
		} else {
			Set<ObjectInstance> hidden_copies = new HashSet<ObjectInstance>();
			Set<String> traits;
			Set<ObjectInstance> objects = new HashSet<ObjectInstance>();
			for (String obj : contents) {
				objects.add(state.getObject(obj));
			}
			ObjectInstance[] objectArray = new ObjectInstance[objects.size()];
			objects.toArray(objectArray);
			//find mutual traits
			Set<String> allTraits = IngredientFactory.getTraits(objectArray[0]);
			traits = new HashSet<String>(IngredientFactory.getTraits(objectArray[1]));
			traits.retainAll(allTraits);
		
			// hide objects
			for (String name: contents) {
				ObjectInstance ing = state.getObject(name);
				if (!IngredientFactory.isSimple(ing)) {
					hidden_copies.add(IngredientFactory.makeHiddenObjectCopy(state, this.domain, ing));
				}
			}
			
			ObjectInstance newIngredient = 
					IngredientFactory.getNewComplexIngredientObjectInstance(complexIngredientClass, 
							Integer.toString(rando.nextInt()), Recipe.NO_ATTRIBUTES, false, container.getName(),
							null, null, traits, new TreeSet<String>(), new TreeSet<String>(), contents);
			ObjectInstance newContainer = ContainerFactory.removeContents(container);
			state = state.replaceObject(container, newContainer);
			/*for (ObjectInstance ob : hidden_copies) {
				state.removeObject(state.getObject(ob.getName()));
				state.addObject(ob);
			}*/
			
			ContainerFactory.addIngredient(container, newIngredient.getName());
			newIngredient = IngredientFactory.changeIngredientContainer(newIngredient, container.getName());
			
			ObjectInstance receivingSpace = state.getObject(ContainerFactory.getSpaceName(container));
			if (SpaceFactory.isBaking(receivingSpace) && SpaceFactory.getOnOff(receivingSpace)) {
				newIngredient = IngredientFactory.bakeIngredient(newIngredient);
			} else if (SpaceFactory.isHeating(receivingSpace) && SpaceFactory.getOnOff(receivingSpace)) {
				newIngredient = IngredientFactory.heatIngredient(newIngredient);
			}
			
			state = state.appendObject(newIngredient);
			
			
			state = this.makeSwappedIngredient(state, newIngredient);
		}
		return state;
	}
	
	public void changeKnowledgebase(Knowledgebase kb) {
		this.knowledgebase = kb;
	}
	
	public State combineIngredients(State state, Domain domain, IngredientRecipe recipe, ObjectInstance container, String toswap) {
		Set<String> traits = new HashSet<String>(knowledgebase.getTraits(recipe.getName()));
		//get the actual traits from the trait thing
		Set<String> ings = ContainerFactory.getContentNames(container);
		ObjectInstance newIng = IngredientFactory.getNewComplexIngredientObjectInstance(
				domain.getObjectClass(IngredientFactory.ClassNameComplex), toswap, Recipe.NO_ATTRIBUTES, true, "", null, null, traits, new HashSet<String>(), new HashSet<String>(), ings);
		// Make the hidden Copies
		List<ObjectInstance> objectsToRemove = new ArrayList<ObjectInstance>(ings.size());
		Set<ObjectInstance> hiddenCopies = new HashSet<ObjectInstance>();
		for (String name : ings) {
			ObjectInstance ob = state.getObject(name);
			objectsToRemove.add(ob);
			hiddenCopies.add(IngredientFactory.makeHiddenObjectCopy(state, domain, ob));
		}
		ObjectInstance newContainer = ContainerFactory.removeContents(container);
		state = state.replaceObject(container, newContainer);
		state = state.removeAll(objectsToRemove);
		state = state.appendAllObjects(hiddenCopies);
		
		ContainerFactory.addIngredient(container, toswap);
		newIng = IngredientFactory.changeIngredientContainer(newIng, container.getName());
		
		ObjectInstance receivingSpace = state.getObject(ContainerFactory.getSpaceName(container));
		
		if (SpaceFactory.isBaking(receivingSpace) && SpaceFactory.getOnOff(receivingSpace)) {
			newIng = IngredientFactory.bakeIngredient(newIng);
		} else if (SpaceFactory.isHeating(receivingSpace) && SpaceFactory.getOnOff(receivingSpace)) {
			//IngredientFactory.heatIngredient(newIng);
			Knowledgebase.heatContainer(state, container);
		}
		
		state = state.appendObject(newIng);
		
		
		return state;
	}
	
	@Deprecated
	public State makeSwappedIngredient(State state, ObjectInstance newIngredient) {
		// Call to makeFakeAttributeCopy here is to ensure we can make a swapped ingredient even
		// if in reality, said swapped ingredient has to eventually be baked/heated/peeled...
		if (Recipe.isSuccess(state, ingredient.makeFakeAttributeCopy(newIngredient), newIngredient)) {
			return ExperimentHelper.makeSwappedIngredientObject(ingredient.makeFakeAttributeCopy(newIngredient),
					state, asList(newIngredient), state.getObjectsOfTrueClass(ContainerFactory.ClassName));
		} else {
			//For the online game, ingredient is always the topLevelIngredient, so check all possible
			// swapped ingredients
			Collection<IngredientRecipe> swappedIngs= IngredientRecipe.getRecursiveSwappedIngredients(ingredient).values();
			for (IngredientRecipe swapped : swappedIngs) {
				IngredientRecipe swappedCopy = swapped.makeFakeAttributeCopy(newIngredient);
				if (Recipe.isSuccess(state, swapped.makeFakeAttributeCopy(newIngredient), newIngredient)) {
					return ExperimentHelper.makeSwappedIngredientObject(swappedCopy, state, 
							asList(newIngredient), state.getObjectsOfTrueClass(ContainerFactory.ClassName));
					
				}
			}
		}
		return state;
	}
}