package edu.brown.cs.h2r.baking;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.Value;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;

public class CombinationKnowledgebase {
	AbstractMap<String, Set<String>> switches;
	
	public CombinationKnowledgebase() {
		this.switches = generateSwitches();
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
	
	public String canCombine(State state, ObjectInstance container) {
		Set<ObjectInstance> contains = new HashSet<>();
		for (String content : ContainerFactory.getContentNames(container)) {
			contains.add(state.getObject(content));
		}
		for (String key : this.switches.keySet()) {
			Set<String> traits = this.switches.get(key);
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
	
	public void combineIngredients(State state, Domain domain, IngredientRecipe recipe, ObjectInstance container, String toswap) {
		Set<String> traits = new TreeSet<String>();
		//get the actual traits from the trait thing
		for (String trait : recipe.getTraits()) {
			traits.add(trait);
		}
		Set<String> ings = ContainerFactory.getContentNames(container);
		ObjectInstance new_ing = IngredientFactory.getNewComplexIngredientObjectInstance(domain.getObjectClass(IngredientFactory.ClassNameComplex), toswap, false, false, false, "", traits, ings);
		
		// Make the hidden Copies
		Set<ObjectInstance> hidden_copies = new HashSet<ObjectInstance>();
		for (String name : ings) {
			ObjectInstance ob = state.getObject(name);
			hidden_copies.add(hideObject(state, domain, ob));
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
	
	public ObjectInstance hideObject(State s, Domain domain, ObjectInstance object) {
		ObjectInstance hidden;
		ObjectClass oc;
		if (IngredientFactory.isSimple(object)) {
			oc = domain.getObjectClass(IngredientFactory.ClassNameSimpleHidden);
			
		} else {
			oc = domain.getObjectClass(IngredientFactory.ClassNameComplexHidden);
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
