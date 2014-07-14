package edu.brown.cs.h2r.baking.actions;
import java.util.Collection;
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
import edu.brown.cs.h2r.baking.Recipes.Recipe;


public class MixAction extends BakingAction {	
	public static final String className = "mix";
	private IngredientKnowledgebase knowledgebase;
	public MixAction(Domain domain, IngredientRecipe ingredient) {
		super(MixAction.className, domain, ingredient, new String[] {AgentFactory.ClassName, ContainerFactory.ClassName});
		this.knowledgebase = new IngredientKnowledgebase();
	}
	
	@Override
	public BakingActionResult checkActionIsApplicableInState(State state, String[] params) {
		BakingActionResult superResult = super.checkActionIsApplicableInState(state, params);
		
		if (!superResult.getIsSuccess()) {
			return superResult;
		}

		String agentName = params[0];
		ObjectInstance agent =  state.getObject(params[0]);
		
		if (AgentFactory.isRobot(agent)) {
			return BakingActionResult.failure("Robot cannot perform this action");
		}
		
		String containerName = params[1];
		ObjectInstance containerInstance = state.getObject(containerName);
		
		if (ContainerFactory.getContentNames(containerInstance).isEmpty()) {
			return BakingActionResult.failure(containerName + " is empty");
		}
		
		/*if (!ContainerFactory.isMixingContainer(containerInstance)) {
			return BakingActionResult.failure(containerName + " is not a mixing container");
		}*/
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
		
		/*String agentOfSpace = SpaceFactory.getAgent(mixingContainerSpaceName).iterator().next();
		if (!agentOfSpace.equalsIgnoreCase(agent.getName()))
		{		
			return BakingActionResult.failure(agentName + " cannot perform actions in " + containerSpaceName);
		}*/
				
		/*if (!SpaceFactory.isWorking(mixingContainerSpaceName)) {
			return BakingActionResult.failure(mixingContainerSpaceName + " is not suitable for mixing");
		}*/
		if (SpaceFactory.isBaking(mixingContainerSpaceName)) {
			return BakingActionResult.failure(mixingContainerSpaceName + " is not suitable for mixing!");
		}
		return BakingActionResult.success();
	}
	
	@Override
	public boolean applicableInState(State state, String[] params) {
		return this.checkActionIsApplicableInState(state, params).getIsSuccess();
	}

	@Override
	protected State performActionHelper(State state, String[] params) {
		super.performActionHelper(state, params);
		ObjectInstance containerInstance = state.getObject(params[1]);
		this.mix(state, containerInstance);
		return state;
	}
	private void mix(State state, String container) {
		mix(state, state.getObject(container));
	}
	private void mix(State state, ObjectInstance container)
	{	
		ObjectClass complexIngredientClass = this.domain.getObjectClass(IngredientFactory.ClassNameComplex);
		Random rando = new Random();
		Set<String> contents = ContainerFactory.getContentNames(container);
		String res;
		if (!(res  = knowledgebase.canCombine(state, container)).equals("")) {
			this.combineIngredients(state, domain, ingredient, container, res);
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
			Set<String> allTraits = IngredientFactory.getTraits(objectArray[0]);
			for (String trait: allTraits) {
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
			
			ObjectInstance newIngredient = 
					IngredientFactory.getNewComplexIngredientObjectInstance(complexIngredientClass, 
							Integer.toString(rando.nextInt()), Recipe.NO_ATTRIBUTES, false, container.getName(),
							traits, new TreeSet<String>(), new TreeSet<String>(), contents);
			state.addObject(newIngredient);
			ContainerFactory.removeContents(container);
			for (ObjectInstance ob : hidden_copies) {
				state.removeObject(state.getObject(ob.getName()));
				state.addObject(ob);
			}
			
			ContainerFactory.addIngredient(container, newIngredient.getName());
			IngredientFactory.changeIngredientContainer(newIngredient, container.getName());
			
			this.makeSwappedIngredient(state, newIngredient);
		}
	}
	
	public void changeKnowledgebase(IngredientKnowledgebase kb) {
		this.knowledgebase = kb;
	}
	
	public void combineIngredients(State state, Domain domain, IngredientRecipe recipe, ObjectInstance container, String toswap) {
		Set<String> traits = new HashSet<String>(recipe.getTraits());
		//get the actual traits from the trait thing
		Set<String> ings = ContainerFactory.getContentNames(container);
		ObjectInstance new_ing = IngredientFactory.getNewComplexIngredientObjectInstance(
				domain.getObjectClass(IngredientFactory.ClassNameComplex), toswap, Recipe.NO_ATTRIBUTES, true, "",new TreeSet<String>(), new TreeSet<String>(), traits, ings);
		// Make the hidden Copies
		Set<ObjectInstance> hidden_copies = new HashSet<ObjectInstance>();
		for (String name : ings) {
			ObjectInstance ob = state.getObject(name);
			if (!IngredientFactory.isSimple(ob)) {
				hidden_copies.add(IngredientFactory.makeHiddenObjectCopy(state, domain, ob));
			}
		}
		ContainerFactory.removeContents(container);
		for (String name : ings) {
			state.removeObject(state.getObject(name));
		}
		for (ObjectInstance ob : hidden_copies) {
			state.addObject(ob);
		}
		ContainerFactory.addIngredient(container, toswap);
		IngredientFactory.changeIngredientContainer(new_ing, container.getName());
		state.addObject(new_ing);
	}
	
	public void makeSwappedIngredient(State state, ObjectInstance newIngredient) {
		// Call to makeFakeAttributeCopy here is to ensure we can make a swapped ingredient even
		// if in reality, said swapped ingredient has to eventually be baked/melted/peeled...
		if (Recipe.isSuccess(state, ingredient.makeFakeAttributeCopy(newIngredient), newIngredient)) {
			ExperimentHelper.checkIngredientCompleted(ingredient.makeFakeAttributeCopy(newIngredient),
					state, asList(newIngredient), state.getObjectsOfTrueClass(ContainerFactory.ClassName));
		} else {
			//For the online game, ingredient is always the topLevelIngredient, so check all possible
			// swapped ingredients
			Collection<IngredientRecipe> swappedIngs= IngredientRecipe.getRecursiveSwappedIngredients(ingredient).values();
			for (IngredientRecipe swapped : swappedIngs) {
				IngredientRecipe swappedCopy = swapped.makeFakeAttributeCopy(newIngredient);
				if (Recipe.isSuccess(state, swapped.makeFakeAttributeCopy(newIngredient), newIngredient)) {
					ExperimentHelper.checkIngredientCompleted(swappedCopy, state, 
							asList(newIngredient), state.getObjectsOfTrueClass(ContainerFactory.ClassName));
					break;
				}
			}
		}
	}
}