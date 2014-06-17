package edu.brown.cs.h2r.baking.ObjectFactories;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.Value;
import edu.brown.cs.h2r.baking.IngredientRecipe;

public class IngredientFactory {

	public static final String ClassNameSimple = "simple_ingredient";
	public static final String ClassNameComplex = "complex_ingredient";
	public static final String ClassNameSimpleHidden = "simple_hidden_ingredient";
	public static final String ClassNameComplexHidden = "complex_hidden_ingredient";
	private static final String attributeBaked = "baked";
	private static final String attributeMelted = "melted";
	private static final String attributeMixed = "mixed";
	private static final String attributeContainer = "container";
	private static final String attributeContains = "contents";
	private static final String attributeTraits = "traits";
	private static final String attributeSwapped = "swapped";

	private static ObjectClass createObjectClass(Domain domain, String className) {
		ObjectClass objectClass = new ObjectClass(domain, className);
		Attribute mixingAttribute = 
				new Attribute(domain, IngredientFactory.attributeBaked, Attribute.AttributeType.DISC);
		mixingAttribute.setDiscValuesForRange(0,1,1);
		objectClass.addAttribute(mixingAttribute);
		
		Attribute heatingAttribute = 
				new Attribute(domain, IngredientFactory.attributeMelted, Attribute.AttributeType.DISC);
		heatingAttribute.setDiscValuesForRange(0,1,1);
		objectClass.addAttribute(heatingAttribute);
		
		Attribute receivingAttribute =
				new Attribute(domain, IngredientFactory.attributeMixed, Attribute.AttributeType.DISC);
		receivingAttribute.setDiscValuesForRange(0,1,1);
		objectClass.addAttribute(receivingAttribute);

		Attribute traitAttribute = new Attribute(domain, IngredientFactory.attributeTraits, Attribute.AttributeType.MULTITARGETRELATIONAL);
		objectClass.addAttribute(traitAttribute);
		
		
		objectClass.addAttribute(
				new Attribute(domain, IngredientFactory.attributeContainer,
						Attribute.AttributeType.RELATIONAL));
		return objectClass;
	}
	
	public static ObjectClass createSimpleHiddenIngredientObjectClass(Domain domain) {
		ObjectClass oc = IngredientFactory.createObjectClass(domain, IngredientFactory.ClassNameSimpleHidden);
		oc.hidden = true;
		return oc;
		
	}
	
	public static ObjectClass createComplexHiddenIngredientObjectClass(Domain domain) {
		ObjectClass objectClass = IngredientFactory.createObjectClass(domain, IngredientFactory.ClassNameComplexHidden);
		objectClass.addAttribute(
				new Attribute(domain, IngredientFactory.attributeContains, 
						Attribute.AttributeType.MULTITARGETRELATIONAL));
		Attribute swappedAttribute =
				new Attribute(domain, IngredientFactory.attributeSwapped, Attribute.AttributeType.DISC);
		swappedAttribute.setDiscValuesForRange(0,1,1);
		objectClass.addAttribute(swappedAttribute);
		
		objectClass.hidden = true;
		return objectClass;
	}
	
	public static ObjectClass createSimpleIngredientObjectClass(Domain domain) {
		return IngredientFactory.createObjectClass(domain, IngredientFactory.ClassNameSimple);
	}
	
	public static ObjectClass createComplexIngredientObjectClass(Domain domain) {
		ObjectClass objectClass = IngredientFactory.createObjectClass(domain, IngredientFactory.ClassNameComplex);
		objectClass.addAttribute(
				new Attribute(domain, IngredientFactory.attributeContains, 
						Attribute.AttributeType.MULTITARGETRELATIONAL));
		
		Attribute swappedAttribute =
				new Attribute(domain, IngredientFactory.attributeSwapped, Attribute.AttributeType.DISC);
		swappedAttribute.setDiscValuesForRange(0,1,1);
		objectClass.addAttribute(swappedAttribute);
		
		return objectClass;
	}
		
	public static ObjectInstance getNewSimpleIngredientObjectInstance(ObjectClass simpleIngredientClass, String name, 
			Boolean baked, Boolean melted, Boolean mixed, Set<String> traits, String ingredientContainer) {
		ObjectInstance newInstance = new ObjectInstance(simpleIngredientClass, name);
		newInstance.setValue(IngredientFactory.attributeBaked, baked ? 1 : 0);
		newInstance.setValue(IngredientFactory.attributeMelted, melted ? 1 : 0);
		newInstance.setValue(IngredientFactory.attributeMixed, mixed ? 1 : 0);
		for (String trait : traits) {
			newInstance.addRelationalTarget("traits", trait);
		}
		if (ingredientContainer != null || ingredientContainer != "")
		{
			newInstance.addRelationalTarget(IngredientFactory.attributeContainer, ingredientContainer);
		}
		return newInstance;		
	}
	
	public static ObjectInstance getNewComplexIngredientObjectInstance(ObjectClass complexIngredientClass, String name, 
			Boolean baked, Boolean melted, Boolean mixed, Boolean swapped, String ingredientContainer, Set<String> traits, Iterable<String> contents) {
		ObjectInstance newInstance = new ObjectInstance(complexIngredientClass, name);
		newInstance.setValue(IngredientFactory.attributeBaked, baked ? 1 : 0);
		newInstance.setValue(IngredientFactory.attributeMelted, melted ? 1 : 0);
		newInstance.setValue(IngredientFactory.attributeMixed, mixed ? 1 : 0);
		newInstance.setValue(IngredientFactory.attributeSwapped, swapped ? 1 : 0);
		
		if (ingredientContainer != null || ingredientContainer != "") {
			newInstance.addRelationalTarget(IngredientFactory.attributeContainer, ingredientContainer);
		}
		for (String trait : traits) {
			newInstance.addRelationalTarget("traits", trait);
		}
		
		if (contents != null) {
			for (String ingredient : contents) {
				newInstance.addRelationalTarget(IngredientFactory.attributeContains, ingredient);
			}
		}
		
		return newInstance;
	}
	
	public static ObjectInstance getNewIngredientInstance(ObjectClass simpleIngredientClass, 
			IngredientRecipe ingredientRecipe, String ingredientContainer) {
		
		if (ingredientRecipe.isSimple()) {
			return IngredientFactory.getNewSimpleIngredientObjectInstance(simpleIngredientClass, 
					ingredientRecipe.getName(), ingredientRecipe.getBaked(), ingredientRecipe.getMelted(), 
					ingredientRecipe.getMixed(), ingredientRecipe.getTraits(), ingredientContainer);
		}
		return null;
	}
	
	public static ObjectInstance getNewIngredientInstance(ObjectInstance objectInstance, String name) {
		Boolean baked = IngredientFactory.isBakedIngredient(objectInstance);
		Boolean mixed = IngredientFactory.isMixedIngredient(objectInstance);
		Boolean melted = IngredientFactory.isMeltedIngredient(objectInstance);
		Boolean swapped = IngredientFactory.isSwapped(objectInstance);
		Set<String> contents = IngredientFactory.getIngredientContents(objectInstance);
		String container = IngredientFactory.getContainer(objectInstance);
		Set<String> traits = IngredientFactory.getTraits(objectInstance);
		return IngredientFactory.getNewComplexIngredientObjectInstance(objectInstance.getObjectClass(), name, baked, melted, mixed, swapped, container, traits, contents);
	}
	
	public static ObjectInstance getNewIngredientInstance(IngredientRecipe ingredient, String name, ObjectClass oc) {
		Boolean baked = ingredient.getBaked();
		Boolean mixed = ingredient.getMixed();
		Boolean melted = ingredient.getMelted();
		Boolean swapped = ingredient.getSwapped();
		Set<String> contents = new TreeSet<String>();
		String container = "";
		Set<String> traits = ingredient.getTraits();
		if (ingredient.isSimple()) {
			return IngredientFactory.getNewSimpleIngredientObjectInstance(oc, name, baked, melted, mixed, traits, container);
		}
		for (IngredientRecipe ing : ingredient.getContents()) {
			contents.add(ing.getName());
		}
		return IngredientFactory.getNewComplexIngredientObjectInstance(oc, name, baked, melted, mixed, swapped, container, traits, contents);
	}
	
	public static List<ObjectInstance> getIngredientInstancesList(ObjectClass simpleIngredientClass,
			IngredientRecipe ingredientRecipe) {
		List<ObjectInstance> newInstances = new ArrayList<ObjectInstance>();
		if (ingredientRecipe.isSimple()) {
			newInstances.add(IngredientFactory.getNewIngredientInstance(simpleIngredientClass, ingredientRecipe, null));
		}
		else {
			List<IngredientRecipe> subIngredients = ingredientRecipe.getContents();
			for (IngredientRecipe subIngredient : subIngredients) {
				newInstances.addAll(IngredientFactory.getIngredientInstancesList(simpleIngredientClass, subIngredient));
			}
		}
		return newInstances;
	}
	
	public static List<ObjectInstance> getSimpleIngredients(ObjectClass simpleIngredientClass,
			IngredientRecipe ingredientRecipe) {
		List<ObjectInstance> newInstances = new ArrayList<ObjectInstance>();
		
		for (IngredientRecipe subIngredient : ingredientRecipe.getContents()) {
			if (subIngredient.isSimple()) {
				newInstances.add(IngredientFactory.getNewIngredientInstance(simpleIngredientClass, subIngredient, null));
			}
		}
		return newInstances;
	}
	
	public static List<ObjectInstance> getComplexIngredients(ObjectClass complexIngredientClass,IngredientRecipe ingredientRecipe) {
		List<ObjectInstance> newInstances = new ArrayList<ObjectInstance>();
		
		for (IngredientRecipe subIngredient : ingredientRecipe.getContents()) {
			if (!subIngredient.isSimple()) {
				newInstances.add(IngredientFactory.getNewIngredientInstance(complexIngredientClass, subIngredient, null));
			}
		}
		return newInstances;
	}
		

	public static void addIngredient(ObjectInstance complexIngredient, ObjectInstance ingredient) {
		complexIngredient.addRelationalTarget(IngredientFactory.attributeContains, ingredient.getName());
	}
	
	public static void addIngredientList(ObjectInstance complexIngredient, Iterable<ObjectInstance> ingredientList) {
		for (ObjectInstance ingredient : ingredientList) {
			IngredientFactory.addIngredient(complexIngredient, ingredient);
		}
	}
	
	public static Set<String> getIngredientContents(ObjectInstance complexIngredient) {
		return complexIngredient.getAllRelationalTargets(IngredientFactory.attributeContains);
	}
	
	public static void changeIngredientContainer(ObjectInstance ingredient, String ingredientContainer) {
		if (ingredientContainer != null) {
			ingredient.addRelationalTarget(IngredientFactory.attributeContainer, ingredientContainer);
		}
	}
	
	public static String getContainer(ObjectInstance ingredient) {
		Set<String> containerInstances = ingredient.getAllRelationalTargets(IngredientFactory.attributeContainer);
		if (containerInstances != null && containerInstances.size() > 0 )
		{
			return containerInstances.iterator().next();
		}
		return null;
	}
	
	public static Set<String> getTraits(ObjectInstance ingredient) {
		return ingredient.getAllRelationalTargets(IngredientFactory.attributeTraits);
	}
	
	public static Boolean isBakedIngredient(ObjectInstance ingredient) {
		return ingredient.getDiscValForAttribute(IngredientFactory.attributeBaked) == 1;
	}
	
	public static Boolean isMixedIngredient(ObjectInstance ingredient) {
		return ingredient.getDiscValForAttribute(IngredientFactory.attributeMixed) == 1;
	}
	
	public static Boolean isMeltedIngredient(ObjectInstance ingredient) {
		return ingredient.getDiscValForAttribute(IngredientFactory.attributeMelted) == 1;
	}
	
	public static void mixIngredient(ObjectInstance ingredient) {
		ingredient.setValue(IngredientFactory.attributeMixed, 1);
	}
	
	public static void bakeIngredient(ObjectInstance ingredient) {
		ingredient.setValue(IngredientFactory.attributeBaked, 1);
	}
	
	public static void meltIngredient(ObjectInstance ingredient) {
		ingredient.setValue(IngredientFactory.attributeMelted, 1);
	}
	
	public static Boolean isSimple(ObjectInstance ingredient) {
		return (ingredient.getObjectClass().name == IngredientFactory.ClassNameSimple ||
				ingredient.getObjectClass().name == IngredientFactory.ClassNameSimpleHidden);
	}
	
	public static Boolean isSwapped(ObjectInstance ingredient) {
		if (!isSimple(ingredient)) {
			return ingredient.getDiscValForAttribute(IngredientFactory.attributeSwapped) == 1;
		}
		return false;
	}
	
	public static void setSwapped(ObjectInstance ingredient) {
		if (!isSimple(ingredient)) {
			ingredient.setValue(IngredientFactory.attributeSwapped, 1);
		}
	}
	
	public static Set<String> getRecursiveContentsForIngredient(State state, ObjectInstance ingredient) {
		//return new TreeSet<String>(ingredient.getAllRelationalTargets(IngredientFactory.attributeContains));
		Set<String> contents = new TreeSet<String>();
		for (String content_name : ingredient.getAllRelationalTargets(IngredientFactory.attributeContains)) {
			ObjectInstance content = state.getObject(content_name);
			if (IngredientFactory.isSimple(content)) {
				contents.add(content_name);
			} else {
				/*Set<String> toAdd = getContentsForIngredient(state, content);
				if (!toAdd.isEmpty()) {
					
				}*/
				contents.addAll(getRecursiveContentsForIngredient(state, content));
			}
		}
		return contents;
	}
	
	public static ObjectInstance makeHiddenObjectCopy(State s, Domain domain, ObjectInstance object) {
		ObjectInstance hidden;
		ObjectClass oc;
		if (isSimple(object)) {
			oc = domain.getObjectClass(ClassNameSimpleHidden);
			
		} else {
			oc = domain.getObjectClass(ClassNameComplexHidden);
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
	
	public static void removeUnecessaryTraitIngredients(State state, Domain domain, IngredientRecipe topLevelIngredient, IngredientRecipe currentGoal) {
		AbstractMap<String, IngredientRecipe> goalTraits = currentGoal.getConstituentNecessaryTraits();
		List<IngredientRecipe> necessaryIngredients = topLevelIngredient.getConstituentIngredients();
		List<ObjectInstance> toHide = new ArrayList<ObjectInstance>();
		Boolean match;
		for (ObjectInstance obj : state.getObjectsOfTrueClass(ClassNameSimple)) {
			match = false;
			String name = obj.getName();
			// Check if this is a required ingredient in recipe that hasn't yet been used
			for (IngredientRecipe ing : necessaryIngredients) {
				if (ing.getName().equals(name)) {
					match = true;
					break;
				}
			}
			// Check this ingredient could've fulfilled a trait at this step, but it wasn't
			// used. (perhaps there was another ingredient that could fill the same trait,
			// e.x. white and brown sugar.
			if (!match) {
				Set<String> objectTraits = IngredientFactory.getTraits(obj);
				for (String trait : goalTraits.keySet()) {
					if (objectTraits.contains(trait)) {
						match = true;
						break;
					}
				}
				if (match) {
					toHide.add(obj);
				}
				
			}
		}
		for (ObjectInstance hide : toHide) {
			ObjectInstance hidden = makeHiddenObjectCopy(state, domain, hide);
			ContainerFactory.removeContents(state.getObject(getContainer(hidden)));
			state.removeObject(hidden.getName());
			state.addObject(hidden);
		}
	}
}