package edu.brown.cs.h2r.baking.actions;
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
import burlap.oomdp.core.StateBuilder;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.Knowledgebase;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ToolFactory;
import edu.brown.cs.h2r.baking.Recipes.Recipe;


public class MixAction extends BakingAction {
	public static final String[] PARAMETER_CLASSES = 
			new String[] {AgentFactory.ClassName, ContainerFactory.ClassName, ToolFactory.ClassName}; 
	
	public static final List<String> dries = Arrays.asList("flour", "cocoa", "salt", "baking_powder");
	public static final List<String> wets = Arrays.asList("eggs", "vanilla", "butter", "white_sugar");
	public static final List<String> simples = Arrays.asList("flour", "cocoa", "salt", "baking_powder", "eggs", "vanilla", "butter", "white_sugar");
	
	public static final String className = "mix";
	public MixAction(Domain domain) {
		super(MixAction.className, domain, PARAMETER_CLASSES);
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
		
		Set<String> contents = ContainerFactory.getContentNames(containerInstance);
		List<String> toolSpecificIngredients = null;
		
		String toolName = params[2];
		
		/*if (toolName.equals("whisk")) {
			toolSpecificIngredients = wets;
		} else if (toolName.equals("spoon")) {
			toolSpecificIngredients = dries;
		} else {
			return BakingActionResult.failure(toolName + " is not suitable for mixing");
		}
		
		for (String ingredient : contents) {
			if (!toolSpecificIngredients.contains(ingredient) && simples.contains(ingredient)) {
				return BakingActionResult.failure(toolName + " cannot touch the ingredient " + ingredient);
			}
		}*/
		
		ObjectInstance tool = state.getObject(toolName);
		if (!ToolFactory.getSpaceName(tool).equals(SpaceFactory.SPACE_COUNTER)) {
			return BakingActionResult.failure(toolName + " must be on the counter");
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
		StateBuilder builder = new StateBuilder(state);
		this.addAgentToOccupiedList(state, builder, params[0]);
		ObjectInstance containerInstance = state.getObject(params[1]);
		ObjectInstance toolInstance = state.getObject(params[2]);
		ObjectInstance newTool = ToolFactory.changeUsed(toolInstance);
		state = state.replaceObject(toolInstance, newTool);
		return this.mix(state, builder, containerInstance, toolInstance);
	}
	
	private State mix(State state, StateBuilder builder, ObjectInstance container, ObjectInstance tool)
	{	
		ObjectClass complexIngredientClass = this.domain.getObjectClass(IngredientFactory.ClassNameComplex);
		Set<String> contents = ContainerFactory.getContentNames(container);
		
		if (contents.isEmpty()) {
			return state;
		}
		
		if (contents.size() < 2) {
			ObjectInstance ingredient = state.getObject(contents.iterator().next()); 
			ObjectInstance mixed = IngredientFactory.mixIngredient(ingredient);
			return state.replaceObject(ingredient, mixed);
		}
		
		// get all of the objects for contents of container
		Set<ObjectInstance> objects = new HashSet<ObjectInstance>();
		for (String obj : contents) {
			objects.add(state.getObject(obj));
		}
		
		List<IngredientRecipe> newCombinations = this.knowledgebase.checkCombination(objects, state);
		if (!newCombinations.isEmpty()) {
			return this.makeSwappedIngredient(state, builder, domain, container, newCombinations, objects);
		}
		
		return makeArbitraryIngredient(state, builder, container, complexIngredientClass, contents, objects);
	}

	private State makeArbitraryIngredient(State state, StateBuilder builder,
			ObjectInstance container, ObjectClass complexIngredientClass,
			Set<String> contents, Set<ObjectInstance> objects) {
		
		
		//find traits shared amongst ingredients
		if (objects.size()  == 0) {
			return state;
		}
		Set<String> traits = new HashSet<String>(IngredientFactory.getTraits(objects.iterator().next()));
		for (ObjectInstance object : objects) {
			traits.retainAll(IngredientFactory.getTraits(object));
		}
	
		// hide objects
		// Hidden copies exist in the domain but aren't accounted for when planning.
		Set<ObjectInstance> hidden_copies = new HashSet<ObjectInstance>();
		for (ObjectInstance object : objects) {
			if (!IngredientFactory.isSimple(object)) {
				hidden_copies.add(IngredientFactory.makeHiddenObjectCopy(state, this.domain, object));
			}
		}
		
		Random rando = new Random();
		
		// create the new ingredients
		ObjectInstance newIngredient = 
				IngredientFactory.getNewComplexIngredientObjectInstance(complexIngredientClass, 
						Integer.toString(rando.nextInt()), Recipe.NO_ATTRIBUTES, false, container.getName(),
						null, null, traits, new TreeSet<String>(), new TreeSet<String>(), contents, container.getHashTuple().getHashingFactory());
		
		// Remove objects from state, add in their hidden copies.
		List<ObjectInstance> objectsToAdd = new ArrayList<ObjectInstance>(hidden_copies.size());
		List<ObjectInstance> objectsToRemove = new ArrayList<ObjectInstance>(hidden_copies.size());
				
				
		// manipulate container's references
		ObjectInstance newContainer = ContainerFactory.removeContents(container);
		newContainer = ContainerFactory.addIngredient(newContainer, newIngredient.getName());
		newIngredient = IngredientFactory.changeIngredientContainer(newIngredient, container.getName());
		
		objectsToAdd.add(newContainer);
		objectsToRemove.add(container);
		
		for (ObjectInstance ob : hidden_copies) {
			objectsToRemove.add(state.getObject(ob.getName()));
			objectsToAdd.add(ob);
		}
		
		state = state.replaceAllObjects(objectsToRemove, objectsToAdd);
		
		// Check to see if resulting ingreient should be heated or baked depending on the space
		// it is in.
		ObjectInstance receivingSpace = state.getObject(ContainerFactory.getSpaceName(container));
		if (SpaceFactory.isBaking(receivingSpace) && SpaceFactory.getOnOff(receivingSpace)) {
			newIngredient = IngredientFactory.bakeIngredient(newIngredient);
		} else if (SpaceFactory.isHeating(receivingSpace) && SpaceFactory.getOnOff(receivingSpace)) {
			state = state.appendObject(newIngredient);
			return Knowledgebase.heatContainer(state, container);
		}
		
		return state.appendObject(newIngredient);
	}
	
	
	public State makeSwappedIngredient(State state, StateBuilder builder, Domain domain, ObjectInstance container, 
			List<IngredientRecipe> combinations, Collection<ObjectInstance> combinedIngredients) {
		IngredientRecipe newIngredient = combinations.get(0);
		
		//get the actual traits from the trait map
		Set<String> ings = ContainerFactory.getContentNames(container);
		ObjectInstance newIng = 
				IngredientFactory.getNewComplexIngredientObjectInstance(domain, newIngredient, Recipe.NO_ATTRIBUTES, ings, true, container.getName(), container.getHashTuple().getHashingFactory());
		
		// Make the hidden Copies
		List<ObjectInstance> objectsToRemove = new ArrayList<ObjectInstance>(ings.size());
		Set<ObjectInstance> hiddenCopies = new HashSet<ObjectInstance>();
		for (String name : ings) {
			ObjectInstance ob = state.getObject(name);
			objectsToRemove.add(ob);
			hiddenCopies.add(IngredientFactory.makeHiddenObjectCopy(state, domain, ob));
		}
		ObjectInstance newContainer = ContainerFactory.removeContents(container);
		newContainer = ContainerFactory.addIngredient(newContainer, newIng.getName());
		builder.replace(container, newContainer);
		builder.removeAll(objectsToRemove);
		builder.addAll(hiddenCopies);
		
		ContainerFactory.addIngredient(newContainer, newIng.getName());
		newIng = IngredientFactory.changeIngredientContainer(newIng, container.getName());
		
		ObjectInstance receivingSpace = state.getObject(ContainerFactory.getSpaceName(container));
		
		if (SpaceFactory.isBaking(receivingSpace) && SpaceFactory.getOnOff(receivingSpace)) {
			newIng = IngredientFactory.bakeIngredient(newIng);
		} else if (SpaceFactory.isHeating(receivingSpace) && SpaceFactory.getOnOff(receivingSpace)) {
			newIng = IngredientFactory.heatIngredient(newIng);
		}
		builder.add(newIng);
		
		return builder.toState();
	}
	
	/*
	public State combineIngredients(State state, Domain domain, ObjectInstance container, String toswap) {
		Set<String> traits = new HashSet<String>(knowledgebase.getTraits(recipe.getName()));
		//get the actual traits from the trait map
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
	
	// This method should make and ingredientObject of our swapped object that was just created.
	// This object should be a subgoal ingredient for the recipe. If w return null, then that
	// means that the complex ingredient we created is not a subgoal ingredient.
	public State makeSwappedIngredient(State state) {
		// if our complex ingredient is a succesful swapped ingredient, then we swap it for a copy of said swapped ingredient.
		// By calling fakeAttributeCopy, we can create our swapped ingredient even if the recipe calls for it to be baked
		// or melted (in which case, Recipe.isSuccess would return false).
		if (Recipe.isSuccess(state, ingredient.makeFakeAttributeCopy(newIngredient), newIngredient)) {
			return ExperimentHelper.makeSwappedIngredientObject(ingredient.makeFakeAttributeCopy(newIngredient),
					state, asList(newIngredient), state.getObjectsOfTrueClass(ContainerFactory.ClassName));
		} else {
			//For the online game, ingredient is always the topLevelIngredient, so check all possible
			// swapped ingredients in our recipe and check if newIngredient matches any of those.
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
	*/
}