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
import edu.brown.cs.h2r.baking.ObjectFactories.ToolFactory;
import edu.brown.cs.h2r.baking.Recipes.Recipe;


public class MixAction extends BakingAction {
	public static final List<String> dries = Arrays.asList("flour", "cocoa", "salt", "baking_powder");
	public static final List<String> wets = Arrays.asList("eggs", "vanilla", "butter", "white_sugar");
	public static final List<String> simples = Arrays.asList("flour", "cocoa", "salt", "baking_powder", "eggs", "vanilla", "butter", "white_sugar");
	
	public static final String className = "mix";
	private Knowledgebase knowledgebase;
	public MixAction(Domain domain, IngredientRecipe ingredient) {
		super(MixAction.className, domain, ingredient, new String[] {AgentFactory.ClassName, ContainerFactory.ClassName, ToolFactory.ClassName});
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
		
		if (toolName.equals("whisk")) {
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
		}
		
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
		super.performActionHelper(state, params);
		ObjectInstance containerInstance = state.getObject(params[1]);
		ObjectInstance toolInstance = state.getObject(params[2]);
		ObjectInstance newTool = ToolFactory.changeUsed(toolInstance);
		state = state.replaceObject(toolInstance, newTool);
		return this.mix(state, containerInstance, toolInstance);
	}
	
	private State mix(State state, ObjectInstance container, ObjectInstance tool)
	{	
		ObjectClass complexIngredientClass = this.domain.getObjectClass(IngredientFactory.ClassNameComplex);
		Random rando = new Random();
		Set<String> contents = ContainerFactory.getContentNames(container);
		String res  = knowledgebase.canCombine(state, container);
		
		// can we make a premade combination (liquid + flour = dough?)
		if (!res.equals("")) {
			return this.combineIngredients(state, domain, ingredient, container, res);
		} 
		
		// get all of the objects for contents of container
		Set<ObjectInstance> objects = new HashSet<ObjectInstance>();
		for (String obj : contents) {
			objects.add(state.getObject(obj));
		}
		
		ObjectInstance[] objectArray = new ObjectInstance[objects.size()];
		objectArray = objects.toArray(objectArray);
		
		//find traits shared amongst ingredients
		Set<String> traits;
		Set<String> allTraits = IngredientFactory.getTraits(objectArray[0]);
		traits = new HashSet<String>(IngredientFactory.getTraits(objectArray[1]));
		traits.retainAll(allTraits);
	
		// hide objects
		// Hidden copies exist in the domain but aren't accounted for when planning.
		Set<ObjectInstance> hidden_copies = new HashSet<ObjectInstance>();
		for (String name: contents) {
			ObjectInstance ing = state.getObject(name);
			if (!IngredientFactory.isSimple(ing)) {
				hidden_copies.add(IngredientFactory.makeHiddenObjectCopy(state, this.domain, ing));
			}
		}
		
		// create the new ingredients
		ObjectInstance newIngredient = 
				IngredientFactory.getNewComplexIngredientObjectInstance(complexIngredientClass, 
						Integer.toString(rando.nextInt()), Recipe.NO_ATTRIBUTES, false, container.getName(),
						null, null, traits, new TreeSet<String>(), new TreeSet<String>(), contents);
		state = state.appendObject(newIngredient);
		
		// manipulate container's references
		ContainerFactory.removeContents(container);
		ContainerFactory.addIngredient(container, newIngredient.getName());
		IngredientFactory.changeIngredientContainer(newIngredient, container.getName());
		
		// Remove objects from state, add in their hidden copies.
		List<ObjectInstance> objectsToAdd = new ArrayList<ObjectInstance>(hidden_copies.size());
		List<ObjectInstance> objectsToRemove = new ArrayList<ObjectInstance>(hidden_copies.size());
		for (ObjectInstance ob : hidden_copies) {
			objectsToRemove.add(state.getObject(ob.getName()));
			objectsToAdd.add(ob);
		}
		
		state = state.replaceAllObjects(objectsToRemove, objectsToAdd);
		
		// Check to see if resulting ingreient should be heated or baked depending on the space
		// it is in.
		ObjectInstance receivingSpace = state.getObject(ContainerFactory.getSpaceName(container));
		if (SpaceFactory.isBaking(receivingSpace) && SpaceFactory.getOnOff(receivingSpace)) {
			IngredientFactory.bakeIngredient(newIngredient);
		} else if (SpaceFactory.isHeating(receivingSpace) && SpaceFactory.getOnOff(receivingSpace)) {
			IngredientFactory.heatIngredient(newIngredient);
		}
		
		// check to see if this new ingredient can be swapped for a swappedIngredient
		return this.makeSwappedIngredient(state, newIngredient);
	}
	
	public void changeKnowledgebase(Knowledgebase kb) {
		this.knowledgebase = kb;
	}
	
	public State combineIngredients(State state, Domain domain, IngredientRecipe recipe, ObjectInstance container, String toswap) {
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
	public State makeSwappedIngredient(State state, ObjectInstance newIngredient) {
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
}