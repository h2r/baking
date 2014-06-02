package edu.brown.cs.h2r.baking.actions;
import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;
import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.Value;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.actions.BakingAction;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.Experiments.ExperimentHelper;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.Recipes.Recipe;


public class MixAction extends BakingAction {	
	public static final String className = "mix";
	public AbstractMap<String, Set<String>> switches;
	public MixAction(Domain domain) {
		super(MixAction.className, domain, new String[] {AgentFactory.ClassName, ContainerFactory.ClassName});
		// At some point, generate the actual map
		this.switches = generateSwitches();
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
		if (!ContainerFactory.isMixingContainer(containerInstance)) {
			return false;
		}
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
		//String name;
		String res;
		if (!(res  = canCombine(state, container)).equals("")) {
			combineIngredients(state, container, res);
		} else {
			
			/* Trait stuff 
			Set<String> traits = new TreeSet<String>();
			Set<ObjectInstance> hidden_copies = new HashSet<ObjectInstance>();
			for (String content : contents) {
				ObjectInstance ob = state.getObject(content);
				if (ob.getObjectClass() == null) {
					System.out.println("help!");
				}
				Set<String> ob_traits = ob.getAllRelationalTargets("traits");
				for (String trait : ob_traits) {
					traits.add(trait);
				}
				hidden_copies.add(hideObject(state, ob));
			}
			end traits */
			Set<ObjectInstance> hidden_copies = new HashSet<ObjectInstance>();
			Set<String> traits = new TreeSet<String>();
			Set<ObjectInstance> objects = new HashSet<ObjectInstance>();
			for (String obj : contents) {
				objects.add(state.getObject(obj));
			}
			ObjectInstance[] objectArray = new ObjectInstance[objects.size()];
			objects.toArray(objectArray);
			//find mutual traits
			for (String trait: objectArray[0].getAllRelationalTargets("traits")) {
				if (objectArray[1].getAllRelationalTargets("traits").contains(trait)) {
					traits.add(trait);
				}
			}
			// hide objects
			for (String name: contents) {
				hidden_copies.add(hideObject(state, state.getObject(name)));
			}
			
			
			ObjectInstance newIngredient = 
					IngredientFactory.getNewComplexIngredientObjectInstance(complexIngredientClass, 
							Integer.toString(rando.nextInt()), false, false, false, container.getName(), traits, contents);
			
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
	}
	
	public String canCombine(State state, ObjectInstance container) {
		Set<ObjectInstance> contains = new HashSet<>();
		for (String content : ContainerFactory.getContentNames(container)) {
			contains.add(state.getObject(content));
		}
		for (String key : this.switches.keySet()) {
			Set<String> traits = this.switches.get(key);
			/*
			 * Substitution looking for many ingredients of same type (say, dry ingredients). Therefore,
			 * logic follows that both ingredients must have the trait we're looking so. If so, success!
			 */
			if (traits.size() == 1) {
				String[] traitArray = new String[1];
				String trait = traits.toArray(traitArray)[0];
				Boolean match = true;
				for (ObjectInstance obj : contains) {
					if (!obj.getAllRelationalTargets("traits").contains(trait)) {
						match = false;
					}
				}
				if (match) {
					return key;
				}
				
			/*
			 * The subgoal we're looking for will have two traits it needs to fulfill,
			 * say dough = {is_flour, is_water}. So, either first ingredient has first trait
			 * and second ingredient has second, or the other way around. The reason why this
			 * check if necessary is for some ingredient that could potentially have both traits,
			 * we don't want one.
			 */
			} else {
				String[] traitArray = new String[traits.size()];
				traits.toArray(traitArray);
				ObjectInstance[] contentArray = new ObjectInstance[contains.size()];
				contains.toArray(contentArray);
				if ((contentArray[0].getAllRelationalTargets("traits").contains(traitArray[0])) 
						&& (contentArray[1].getAllRelationalTargets("traits").contains(traitArray[1]))) {
					return key;
				}
				if ((contentArray[0].getAllRelationalTargets("traits").contains(traitArray[1])) 
						&& (contentArray[1].getAllRelationalTargets("traits").contains(traitArray[0]))) {
					return key;
				}
			}
		}
		return "";
	}
	
	public void combineIngredients(State state, ObjectInstance container, String toswap) {
		//Set<String> ings = this.switches.get(toswap);
		Set<String> traits = new TreeSet<String>();
		//get the actual traits from the trait thing
		for (String trait : Recipe.getTraits(toswap)) {
			traits.add(trait);
		}
		Set<String> ings = ContainerFactory.getContentNames(container);
		ObjectInstance new_ing = IngredientFactory.getNewComplexIngredientObjectInstance(domain.getObjectClass(IngredientFactory.ClassNameComplex), toswap, false, false, false, "", traits, ings);
		
		// Make the hidden Copies
		Set<ObjectInstance> hidden_copies = new HashSet<ObjectInstance>();
		for (String name : ings) {
			ObjectInstance ob = state.getObject(name);
			hidden_copies.add(hideObject(state, ob));
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
	
	public AbstractMap<String, Set<String>> generateSwitches() {
		AbstractMap<String, Set<String>> switches = new HashMap<String, Set<String>>();
		Set<String> value;
		
		value = new TreeSet<String>();
		value.add("dry");
		switches.put("dry_stuff", value);
		
		value = new TreeSet<String>();
		value.add("wet");
		switches.put("wet_stuff", value);
		
		return switches;
	}
	
	public ObjectInstance hideObject(State s, ObjectInstance object) {
		ObjectInstance hidden;
		ObjectClass oc;
		if (IngredientFactory.isSimple(object)) {
			oc = this.domain.getObjectClass(IngredientFactory.ClassNameSimpleHidden);
			
		} else {
			oc = this.domain.getObjectClass(IngredientFactory.ClassNameComplexHidden);
		}
		hidden = new ObjectInstance(oc, object.getName());
		
		hidden.initializeValueObjects();
		for (Value v : hidden.getValues()) {
			String name = v.attName();
			if (name.equals("traits") || name.equals("contents")) {
				for (String val : object.getAllRelationalTargets(name)) {
					hidden.addRelationalTarget(name, val);
				}
			} else {
				hidden.setValue(name, object.getValueForAttribute(name).getStringVal());
			}
		}
		return hidden;
	}
}
