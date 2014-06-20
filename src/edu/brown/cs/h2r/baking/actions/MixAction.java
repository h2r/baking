package edu.brown.cs.h2r.baking.actions;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
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
	public boolean applicableInState(State state, String[] params) {
		if (!super.applicableInState(state, params)) {
			return false;
		}
		
		ObjectInstance agent =  state.getObject(params[0]);
		
		if (AgentFactory.isRobot(agent)) {
			return false;
		}
		ObjectInstance containerInstance = state.getObject(params[1]);
		
		if (ContainerFactory.getContentNames(containerInstance).isEmpty()) {
			return false;
		}
		
		if (!ContainerFactory.isMixingContainer(containerInstance)) {
			return false;
		}
		// move to should mix probably!
		if (ContainerFactory.getContentNames(containerInstance).size() < 2) {
			return false;
		}
		
		String containerSpaceName = ContainerFactory.getSpaceName(containerInstance);
		if (containerSpaceName == null) {
			return false;
		}

		ObjectInstance pouringContainerSpaceObject = state.getObject(containerSpaceName);
		if (pouringContainerSpaceObject == null) {
			return false;
		}
		
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
		ObjectInstance containerInstance = state.getObject(params[1]);
		this.mix(state, containerInstance);
		return state;
	}
	
	protected void mix(State state, ObjectInstance container)
	{	
		ObjectClass complexIngredientClass = this.domain.getObjectClass(IngredientFactory.ClassNameComplex);
		Random rando = new Random();
		Set<String> contents = ContainerFactory.getContentNames(container);
<<<<<<< HEAD
		ObjectInstance newIngredient = 
				IngredientFactory.getNewComplexIngredientObjectInstance(complexIngredientClass, 
						Integer.toString(rando.nextInt()), false, false, false, false, container.getName(), contents);
		state.addObject(newIngredient);
		ContainerFactory.removeContents(container);
		ContainerFactory.addIngredient(container, newIngredient.getName());
		IngredientFactory.changeIngredientContainer(newIngredient, container.getName());
		
=======
		//String name;
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
			
			// TODO: Reevaluate the swapped false here? (4th one)
			ObjectInstance newIngredient = 
					IngredientFactory.getNewComplexIngredientObjectInstance(complexIngredientClass, 
							Integer.toString(rando.nextInt()), false, false, false, false, container.getName(), traits, contents);
			state.addObject(newIngredient);
			ContainerFactory.removeContents(container);
			
			/*for (String name : contents) {
				state.removeObject(state.getObject(name));
			}
			for (ObjectInstance ob : hidden_copies) {
				state.addObject(ob);
			}*/
			for (ObjectInstance ob : hidden_copies) {
				state.removeObject(state.getObject(ob.getName()));
				state.addObject(ob);
			}
			
			ContainerFactory.addIngredient(container, newIngredient.getName());
			IngredientFactory.changeIngredientContainer(newIngredient, container.getName());
		}
	}
	
	/*
	 * protected void mix(State state, ObjectInstance container)
	{	
		ObjectClass complexIngredientClass = this.domain.getObjectClass(IngredientFactory.ClassNameComplex);
		Random rando = new Random();
		Set<String> contents = ContainerFactory.getContentNames(container);
		//String name;
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
				hidden_copies.add(IngredientFactory.makeHiddenObjectCopy(state, this.domain, state.getObject(name)));
			}
			
			// TODO: Reevaluate the swapped false here? (4th one)
			ObjectInstance newIngredient = 
					IngredientFactory.getNewComplexIngredientObjectInstance(complexIngredientClass, 
							Integer.toString(rando.nextInt()), false, false, false, false, container.getName(), traits, contents);
			state.addObject(newIngredient);
			ContainerFactory.removeContents(container);
			
			for (String name : contents) {
				state.removeObject(state.getObject(name));
			}
			for (ObjectInstance ob : hidden_copies) {
				state.addObject(ob);
			}
			
			ContainerFactory.addIngredient(container, newIngredient.getName());
			IngredientFactory.changeIngredientContainer(newIngredient, container.getName());
		}
>>>>>>> 50deb471640c2f46d29b1bf7de91ad86dce379de
	}
*/
}
