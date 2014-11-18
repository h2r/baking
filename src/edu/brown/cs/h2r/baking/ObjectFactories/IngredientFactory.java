package edu.brown.cs.h2r.baking.ObjectFactories;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.Set;

import burlap.behavior.statehashing.ObjectHashFactory;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.Value;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.Knowledgebase;
import edu.brown.cs.h2r.baking.Recipes.Recipe;

public class IngredientFactory {

	public static final String ClassNameSimple = "simple_ingredient";
	public static final String ClassNameComplex = "complex_ingredient";
	public static final String ClassNameSimpleHidden = "simple_hidden_ingredient";
	public static final String ClassNameComplexHidden = "complex_hidden_ingredient";
	private static final String attributeBaked = "baked";
	private static final String attributeHeated = "heated";
	private static final String attributeMixed = "mixed";
	private static final String attributeHeatingInfo = "heatingInfo";
	private static final String attributeHeatedState = "heatedState";
	private static final String attributeContainer = "container";
	private static final String attributeContains = "contents";
	private static final String attributeTraits = "traits";
	private static final String attributeSwapped = "swapped";
	private static final String attributeUseCount = "useCount";
	private static final String attributeToolTraits = "toolTrait";
	private static final String attributeToolAttributes = "toolAttribute";
	private static final String[] booleanAttributes= {IngredientFactory.attributeBaked, 
		IngredientFactory.attributeHeated, IngredientFactory.attributeMixed};

	private static ObjectClass createObjectClass(Domain domain, String className) {
		ObjectClass objectClass = new ObjectClass(domain, className);
		Attribute mixingAttribute = 
				new Attribute(domain, IngredientFactory.attributeBaked, Attribute.AttributeType.BOOLEAN);
		objectClass.addAttribute(mixingAttribute);
		
		Attribute heatingAttribute = 
				new Attribute(domain, IngredientFactory.attributeHeated, Attribute.AttributeType.BOOLEAN);
		objectClass.addAttribute(heatingAttribute);
		
		Attribute receivingAttribute =
				new Attribute(domain, IngredientFactory.attributeMixed, Attribute.AttributeType.BOOLEAN);
		objectClass.addAttribute(receivingAttribute);
		
		Attribute countAttribute = 
				new Attribute(domain, IngredientFactory.attributeUseCount, Attribute.AttributeType.DISC);
		countAttribute.setDiscValuesForRange(0,10,1);
		objectClass.addAttribute(countAttribute);

		Attribute traitAttribute = new Attribute(domain, IngredientFactory.attributeTraits, Attribute.AttributeType.MULTITARGETRELATIONAL);
		objectClass.addAttribute(traitAttribute);
		
		Attribute toolTraitAttribute = new Attribute(domain, IngredientFactory.attributeToolTraits, Attribute.AttributeType.MULTITARGETRELATIONAL);
		objectClass.addAttribute(toolTraitAttribute);
		
		Attribute toolAttributeAttribute = new Attribute(domain, IngredientFactory.attributeToolAttributes, Attribute.AttributeType.MULTITARGETRELATIONAL);
		objectClass.addAttribute(toolAttributeAttribute);
		
		Attribute heatingInfoAttribute = new Attribute(domain, IngredientFactory.attributeHeatingInfo, Attribute.AttributeType.RELATIONAL);
		objectClass.addAttribute(heatingInfoAttribute);
		
		Attribute heatingStateAttribute = new Attribute(domain, IngredientFactory.attributeHeatedState, Attribute.AttributeType.RELATIONAL);
		objectClass.addAttribute(heatingStateAttribute);
		
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
		
		Attribute swappedAttribute =
				new Attribute(domain, IngredientFactory.attributeSwapped, Attribute.AttributeType.BOOLEAN);
		objectClass.addAttribute(swappedAttribute);
		
		objectClass.addAttribute(
				new Attribute(domain, IngredientFactory.attributeContains, 
						Attribute.AttributeType.MULTITARGETRELATIONAL));
		
		objectClass.hidden = true;
		return objectClass;
	}
	
	public static ObjectClass createSimpleIngredientObjectClass(Domain domain) {
		return IngredientFactory.createObjectClass(domain, IngredientFactory.ClassNameSimple);
	}
	
	public static ObjectClass createComplexIngredientObjectClass(Domain domain) {
		ObjectClass objectClass = IngredientFactory.createObjectClass(domain, IngredientFactory.ClassNameComplex);
		
		Attribute swappedAttribute =
				new Attribute(domain, IngredientFactory.attributeSwapped, Attribute.AttributeType.BOOLEAN);
		objectClass.addAttribute(swappedAttribute);
		
		objectClass.addAttribute(
				new Attribute(domain, IngredientFactory.attributeContains, 
						Attribute.AttributeType.MULTITARGETRELATIONAL));
		
		return objectClass;
	}
		
	public static ObjectInstance getNewSimpleIngredientObjectInstance(ObjectClass simpleIngredientClass, String name, 
			int attributes, int useCount, String heatingInfo, String heatedState, Set<String> traits, Set<String> toolTraits,
			Set<String> toolAttributes, String ingredientContainer, ObjectHashFactory hashingFactory) {
		ObjectInstance newInstance = new ObjectInstance(simpleIngredientClass, name, hashingFactory);
		newInstance = IngredientFactory.changeAttributes(newInstance, attributes, toolAttributes);
		newInstance = newInstance.changeValue(IngredientFactory.attributeUseCount, useCount);
		if (heatingInfo != null) {
			newInstance = newInstance.appendRelationalTarget(IngredientFactory.attributeHeatingInfo, heatingInfo);
		}
		if (heatedState != null) {
			newInstance = newInstance.appendRelationalTarget(IngredientFactory.attributeHeatedState, heatedState);
		}
		for (String trait : traits) {
			newInstance = newInstance.appendRelationalTarget(IngredientFactory.attributeTraits, trait);
		}
		
		for (String toolTrait : toolTraits) {
			newInstance = newInstance.appendRelationalTarget(IngredientFactory.attributeToolTraits, toolTrait);
		}
		
		for (String toolAttribute : toolAttributes) {
			newInstance = newInstance.appendRelationalTarget(IngredientFactory.attributeToolAttributes, toolAttribute);
		}
		if (ingredientContainer != null || ingredientContainer != "")
		{
			newInstance = newInstance.appendRelationalTarget(IngredientFactory.attributeContainer, ingredientContainer);
		}
		return newInstance;		
	}
	
	public static ObjectInstance getNewComplexIngredientObjectInstance(Domain domain, IngredientRecipe ingredientRecipe, 
			boolean isSwapped, String container, ObjectHashFactory hashingFactory) {
		
		return IngredientFactory.getNewComplexIngredientObjectInstance(
				domain.getObjectClass(ClassNameComplex), 
				ingredientRecipe.getName(), 
				ingredientRecipe.getAttributeNumber(), 
				isSwapped, 
				container, 
				ingredientRecipe.getHeatingInfo(), 
				ingredientRecipe.getHeatedState(), 
				ingredientRecipe.getTraits(), 
				ingredientRecipe.getToolTraits(), 
				ingredientRecipe.getToolAttributes(), 
				ingredientRecipe.getContentNames(),
				hashingFactory);
	}
	
	public static ObjectInstance getNewComplexIngredientObjectInstance(ObjectClass complexIngredientClass, String name, 
			int attributes, boolean swapped, String ingredientContainer, String heatingInfo, String heatedState, Set<String> traits, 
			Set<String> toolTraits, Set<String> toolAttributes, Iterable<String> contents, ObjectHashFactory hashingFactory) {
		ObjectInstance newInstance = new ObjectInstance(complexIngredientClass, name, hashingFactory);
		newInstance = IngredientFactory.changeAttributes(newInstance, attributes, toolAttributes);
		newInstance = newInstance.changeValue(IngredientFactory.attributeUseCount, 1);
		newInstance = newInstance.changeValue(IngredientFactory.attributeSwapped, swapped ? 1 : 0);
		
		if (ingredientContainer != null || ingredientContainer != "") {
			newInstance = newInstance.appendRelationalTarget(IngredientFactory.attributeContainer, ingredientContainer);
		}
		if (heatingInfo != null) {
			newInstance = newInstance.appendRelationalTarget(IngredientFactory.attributeHeatingInfo, heatingInfo);
		}
		if (heatedState != null) {
			newInstance = newInstance.appendRelationalTarget(IngredientFactory.attributeHeatedState, heatedState);
		}
		for (String trait : traits) {
			newInstance = newInstance.appendRelationalTarget("traits", trait);
		}
		for (String toolTrait : toolTraits) {
			newInstance = newInstance.appendRelationalTarget(IngredientFactory.attributeToolTraits, toolTrait);
		}
		
		for (String toolAttribute : toolAttributes) {
			newInstance = newInstance.appendRelationalTarget(IngredientFactory.attributeToolAttributes, toolAttribute);
		}
		
		if (contents != null) {
			for (String ingredient : contents) {
				newInstance = newInstance.appendRelationalTarget(IngredientFactory.attributeContains, ingredient);
			}
		}
		
		return newInstance;
	}
	
	public static ObjectInstance getNewIngredientInstance(ObjectInstance objectInstance, String name) {
		return new ObjectInstance(name, objectInstance);
		/*Boolean baked = IngredientFactory.isBakedIngredient(objectInstance);
		Boolean mixed = IngredientFactory.isMixedIngredient(objectInstance);
		Boolean heated = IngredientFactory.isHeatedIngredient(objectInstance);
		
		int attributes = IngredientRecipe.generateAttributeNumber(mixed, heated, baked);
		int useCount = IngredientFactory.getUseCount(objectInstance);
		String heatingInfo = IngredientFactory.getHeatingInfo(objectInstance);
		String heatedState = IngredientFactory.getHeatedState(objectInstance);
		String container = IngredientFactory.getContainer(objectInstance);
		Set<String> traits = IngredientFactory.getTraits(objectInstance);
		Set<String> toolTraits = IngredientFactory.getToolTraits(objectInstance);
		Set<String> toolAttributes = IngredientFactory.getToolAttributes(objectInstance);
		ObjectClass oc = objectInstance.getObjectClass();
		
		if (oc.name.equals(IngredientFactory.ClassNameSimple)) {
			return IngredientFactory.getNewSimpleIngredientObjectInstance(objectInstance.getObjectClass(), 
					name, useCount, attributes, heatingInfo, heatedState, traits, toolTraits, toolAttributes, container);
		}
		
		Boolean swapped = IngredientFactory.isSwapped(objectInstance);
		Set<String> contents = IngredientFactory.getIngredientContents(objectInstance);
		return IngredientFactory.getNewComplexIngredientObjectInstance(oc, name, attributes, swapped, 
				container, heatingInfo, heatedState, traits, toolTraits, toolAttributes, contents);*/
	}
	
	public static ObjectInstance getNewIngredientInstance(IngredientRecipe ingredient, String name, ObjectClass oc, ObjectHashFactory hashingFactory) {
		int attributes = IngredientRecipe.generateAttributeNumber(ingredient.getBaked(), ingredient.getMixed(), 
				ingredient.getHeated());
		Boolean swapped = ingredient.getSwapped();
		int useCount = ingredient.getUseCount();
		String container = "";
		String heatingInfo = ingredient.getHeatingInfo();
		String heatedState = ingredient.getHeatedState();
		Set<String> traits = ingredient.getTraits();
		Set<String> toolTraits = ingredient.getToolTraits();
		Set<String> toolAttributes= ingredient.getToolAttributes();
		if (ingredient.isSimple()) {
			return IngredientFactory.getNewSimpleIngredientObjectInstance(oc, name, attributes, useCount, heatingInfo, heatedState, traits, toolTraits, toolAttributes, container, hashingFactory);
		}
		Set<String> contents = new HashSet<String>();
		List<IngredientRecipe> ingContents = ingredient.getContents();
		for (IngredientRecipe ing : ingContents) {
			contents.add(ing.getName());
		}
		return IngredientFactory.getNewComplexIngredientObjectInstance(oc, name, attributes, swapped, container, heatingInfo, heatedState, traits, toolTraits, toolAttributes, contents, hashingFactory);
	}
	
	public static List<ObjectInstance> getIngredientInstancesList(ObjectClass simpleIngredientClass,
			IngredientRecipe ingredientRecipe, ObjectHashFactory hashingFactory) {
		List<ObjectInstance> newInstances = new ArrayList<ObjectInstance>();
		if (ingredientRecipe.isSimple()) {
			newInstances.add(IngredientFactory.getNewIngredientInstance(ingredientRecipe, ingredientRecipe.getName(), simpleIngredientClass, hashingFactory));
		}
		else {
			List<IngredientRecipe> subIngredients = ingredientRecipe.getContents();
			for (IngredientRecipe subIngredient : subIngredients) {
				newInstances.addAll(IngredientFactory.getIngredientInstancesList(simpleIngredientClass, subIngredient, hashingFactory));
			}
		}
		return newInstances;
	}
	
	public static List<ObjectInstance> getSimpleIngredients(ObjectClass simpleIngredientClass,
			IngredientRecipe ingredientRecipe, ObjectHashFactory hashingFactory) {
		List<ObjectInstance> newInstances = new ArrayList<ObjectInstance>();
		
		List<IngredientRecipe> subIngredients = ingredientRecipe.getContents();
		for (IngredientRecipe subIngredient : subIngredients) {
			if (subIngredient.isSimple()) {
				newInstances.add(IngredientFactory.getNewIngredientInstance(subIngredient, subIngredient.getName(), simpleIngredientClass, hashingFactory));
			}
		}
		return newInstances;
	}
	
	public static List<ObjectInstance> getComplexIngredients(ObjectClass complexIngredientClass,IngredientRecipe ingredientRecipe, ObjectHashFactory hashingFactory) {
		List<ObjectInstance> newInstances = new ArrayList<ObjectInstance>();
		
		List<IngredientRecipe> subIngredients = ingredientRecipe.getContents();
		for (IngredientRecipe subIngredient : subIngredients) {
				if (!subIngredient.isSimple()) {
				newInstances.add(IngredientFactory.getNewIngredientInstance(subIngredient, subIngredient.getName(), complexIngredientClass, hashingFactory));
			}
		}
		return newInstances;
	}
		

	public static ObjectInstance addIngredient(ObjectInstance complexIngredient, ObjectInstance ingredient) {
		return complexIngredient.appendRelationalTarget(IngredientFactory.attributeContains, ingredient.getName());
	}
	
	public static ObjectInstance addIngredientList(ObjectInstance complexIngredient, Collection<ObjectInstance> ingredientList) {
		List<String> targets = new ArrayList<String>();
		
		for (ObjectInstance ingredient : ingredientList) {
			targets.add(ingredient.getName());
		}
		
		return complexIngredient.appendAllRelationTargets(IngredientFactory.attributeContains, targets);
	}
	
	/*
	public static ObjectInstance getNewCopyObject(ObjectInstance object, State state) {
		Pattern charactersOnly = Pattern.compile("[a-z]");
		String objectName = object.getName();
		String baseName = objectName.replaceAll("\\d+.*", "");
		int index = 0;
		String currentName = baseName + index;
		while (state.getObject(currentName) != null) {
			index++;
		}
		return new ObjectInstance(currentName, object);
	}*/
	
	public static Set<String> getIngredientContents(ObjectInstance complexIngredient) {
		return complexIngredient.getAllRelationalTargets(IngredientFactory.attributeContains);
	}
	
	public static ObjectInstance changeIngredientContainer(ObjectInstance ingredient, String ingredientContainer) {
		return ingredient.appendRelationalTarget(IngredientFactory.attributeContainer, ingredientContainer);
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
	
	public static int getUseCount(ObjectInstance ingredient) {
		return ingredient.getDiscValForAttribute(IngredientFactory.attributeUseCount);
	}
	
	public static Boolean isBakedIngredient(ObjectInstance ingredient) {
		return ingredient.getDiscValForAttribute(IngredientFactory.attributeBaked) == 1;
	}
	
	public static Boolean isMixedIngredient(ObjectInstance ingredient) {
		return ingredient.getDiscValForAttribute(IngredientFactory.attributeMixed) == 1;
	}
	
	public static Boolean isHeatedIngredient(ObjectInstance ingredient) {
		return ingredient.getDiscValForAttribute(IngredientFactory.attributeHeated) == 1;
	}
	
	public static boolean isHiddenIngredient(ObjectInstance ingredient) {
		String ocName = ingredient.getObjectClass().name;
		return (ocName.equals(IngredientFactory.ClassNameSimpleHidden) ||
				ocName.equals(IngredientFactory.ClassNameComplexHidden));
	}
	
	public static ObjectInstance mixIngredient(ObjectInstance ingredient) {
		return ingredient.changeValue(IngredientFactory.attributeMixed, 1);
	}
	
	public static ObjectInstance bakeIngredient(ObjectInstance ingredient) {
		return ingredient.changeValue(IngredientFactory.attributeBaked, 1);
	}
	
	public static ObjectInstance heatIngredient(ObjectInstance ingredient) {
		return ingredient.changeValue(IngredientFactory.attributeHeated, 1);
	}
	
	public static ObjectInstance changeUseCount(ObjectInstance ingredient, int count) {
		return ingredient.changeValue(IngredientFactory.attributeUseCount, count);
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
	
	public static Set<String> getContentsForIngredient(ObjectInstance ingredient) {
		return new HashSet<String>(ingredient.getAllRelationalTargets(IngredientFactory.attributeContains));
	}

	public static ObjectInstance changeSwapped(ObjectInstance ingredient) {
		return ingredient.changeValue(IngredientFactory.attributeSwapped, 1);
	}
	
	public static ObjectInstance addToolTrait(ObjectInstance object, String trait) {
		return object.appendRelationalTarget(IngredientFactory.attributeToolTraits, trait);
	}
	
	public static ObjectInstance addToolTraits(ObjectInstance object,Set<String> traits) {
		return object.appendAllRelationTargets(IngredientFactory.attributeToolTraits, traits);
	}
	
	public static Set<String> getToolTraits(ObjectInstance object) {
		return object.getAllRelationalTargets(IngredientFactory.attributeToolTraits);
	}
	
	public static Set<String> getToolAttributes(ObjectInstance object) {
		return object.getAllRelationalTargets(IngredientFactory.attributeToolAttributes);
	}
	
	public static boolean hasToolAttribute(ObjectInstance object, String attribute) {
		return IngredientFactory.getToolAttributes(object).contains(attribute);
	}
	
	public static ObjectInstance addToolAttribute(ObjectInstance object, String attribute) {
		return object.appendRelationalTarget(IngredientFactory.attributeToolAttributes, attribute);
	}
	
	public static ObjectInstance addToolAttribute(ObjectInstance object, Set<String> attributes) {
		return object.appendAllRelationTargets(IngredientFactory.attributeToolAttributes, attributes);
	}
	
	public static boolean hasToolTrait(ObjectInstance object, String trait) {
		return IngredientFactory.getToolTraits(object).contains(trait);
	}
	
	
	public static Set<String> getRecursiveContentsForIngredient(State state, ObjectInstance ingredient) {
		Set<String> contents = new HashSet<String>();
		Set<String> ingredientContents = IngredientFactory.getIngredientContents(ingredient);
		for (String contentName : ingredientContents) {
			ObjectInstance content = state.getObject(contentName);
			if (IngredientFactory.isSimple(content)) {
				contents.add(contentName);
			} else {
				contents.addAll(getRecursiveContentsForIngredient(state, content));
			}
		}
		return contents;
	}
	
	public static Set<String> getRecursiveContentsAndSwapped(State state, ObjectInstance ingredient) {
		Set<String> contents = new HashSet<String>();
		if (IngredientFactory.isSimple(ingredient)) {
			contents.add(ingredient.getName());
			return contents;
		}
		Set<String> ingredientContents = IngredientFactory.getIngredientContents(ingredient);
		for (String contentName : ingredientContents) {
			ObjectInstance content = state.getObject(contentName);
			if (IngredientFactory.isSimple(content) || IngredientFactory.isSwapped(content)) {
				contents.add(contentName);
			} else {
				contents.addAll(getRecursiveContentsAndSwapped(state, content));
			}
		}
		return contents;
	}
	
	public static ObjectInstance makeHiddenObjectCopy(State state, Domain domain, ObjectInstance object) {
		ObjectInstance hidden;
		ObjectClass oc;
		if (isSimple(object)) {
			oc = domain.getObjectClass(ClassNameSimpleHidden);
			
		} else {
			oc = domain.getObjectClass(ClassNameComplexHidden);
		}
		hidden = new ObjectInstance(oc, object.getName(), object.getHashTuple().getHashingFactory());
		
		hidden.initializeValueObjects(object.getHashTuple().getHashingFactory().getValueHashFactory());
		String multi = Attribute.AttributeType.MULTITARGETRELATIONAL.name();
		String relational = Attribute.AttributeType.RELATIONAL.name();
		
		List<Value> values = hidden.getValues();
		for (Value v : values) {
			String name = v.attName();
			String attributeType = v.getAttribute().type.name();
			if (attributeType.equals(multi) || attributeType.equals(relational)) {
				Set<String> targets = object.getAllRelationalTargets(name);
				for (String val : targets) {
					hidden = hidden.appendRelationalTarget(name, val);
				}
			} else {
				hidden = hidden.changeValue(name, object.getValueForAttribute(name).getStringVal());
			}
		}
		return hidden;
	}
	
	public static State hideUnecessaryIngredients(State state, Domain domain, IngredientRecipe goal, List<ObjectInstance>allIngredients) {
		Boolean match;
		for (ObjectInstance obj : allIngredients) {
			match = false;
			List<IngredientRecipe> contents = goal.getContents();
			contents.add(goal);
			for (IngredientRecipe ing : contents) {
				if (ing.getName().equals(obj.getName())) {
					obj = IngredientFactory.changeAttributes(obj, ing.getAttributeNumber(), ing.getToolAttributes());
					match = true;
					break;
				}
			}
			if (!match) {
				for (Entry<String, IngredientRecipe> entry : goal.getNecessaryTraits().entrySet()) {
					if (IngredientFactory.getTraits(obj).contains(entry.getKey())) {
						IngredientRecipe ing = entry.getValue();
						obj = IngredientFactory.changeAttributes(obj, ing.getAttributeNumber(), ing.getToolAttributes());
						match = true;
						break;
					}
				}
			}
			if (match) {
				// Check is swappedfor simple-ingredient subgoals
				boolean bool1 = obj.getName().equals(goal.getName());
				boolean bool2 = !goal.getSwapped();
				if (bool1 == bool2) {
					if (IngredientFactory.getUseCount(obj) <= 1) {
						ObjectInstance hidden = makeHiddenObjectCopy(state, domain, obj);
						ObjectInstance container = state.getObject(IngredientFactory.getContainer(hidden));
						ObjectInstance newContainer = ContainerFactory.removeIngredient(container, hidden.getName());
						state = state.replaceObject(container, newContainer);
						if (!IngredientFactory.isHiddenIngredient(state.getObject(obj.getName()))) {
							state = state.remove(obj);
						}
						state = state.appendObject(hidden);
					}
				}
				if (IngredientFactory.getUseCount(obj) > 0) {
					ObjectInstance newObj = IngredientFactory.changeUseCount(obj, IngredientFactory.getUseCount(obj) -1);
					state = state.replaceObject(obj, newObj);
				}
			}
		}
		
		return state;
	}
	
	/* Rids the object instance of any attrbiutes, such that I can make a recipe require
	 * a certain attribute for an ingredient but when the object instance is created for planning,
	 * this attribute is already fulfilled.
	 */
	public static ObjectInstance clearBooleanAttributes(ObjectInstance obj) {
		ObjectInstance newObject = obj.copy();
		for (String attName : IngredientFactory.booleanAttributes) {
			newObject = newObject.changeValue(attName, 0);
		}		
		return newObject;
	}
	
	public static ObjectInstance clearToolAttributes(ObjectInstance obj) {
		return obj.removeAllRelationalTarget(IngredientFactory.attributeToolAttributes);
	}

	public static boolean isMeltedAtRoomTemperature(ObjectInstance ingredient) {
		return IngredientFactory.getTraits(ingredient).contains(Knowledgebase.NONMELTABLE);
	}
	
	public static boolean isLubricant(ObjectInstance ingredient) {
		return IngredientFactory.getTraits(ingredient).contains(Knowledgebase.LUBRICANT);
	}
	
	public static ObjectInstance changeAttributes(ObjectInstance ingredient, int attributes, Set<String> toolAttributes) {
		ingredient = ingredient.changeValue(IngredientFactory.attributeBaked, ((attributes & Recipe.BAKED) == Recipe.BAKED) ? 1 : 0);
		ingredient = ingredient.changeValue(IngredientFactory.attributeHeated, ((attributes & Recipe.HEATED) == Recipe.HEATED) ? 1 : 0);
		ingredient = ingredient.changeValue(IngredientFactory.attributeMixed, ((attributes & Recipe.MIXED) == Recipe.MIXED) ? 1 : 0);
		for (String attribute : toolAttributes) {
			ingredient = IngredientFactory.addToolAttribute(ingredient, attribute);
		}
		return ingredient;
	}
	
	public static int getAttributeNumber(ObjectInstance object) {
		int mixedInt = object.getBooleanValue(IngredientFactory.attributeBaked) ? Recipe.BAKED : 0;
		int heatedInt = object.getBooleanValue(IngredientFactory.attributeHeated) ? Recipe.HEATED : 0;
		int bakedInt = object.getBooleanValue(IngredientFactory.attributeMixed) ? Recipe.MIXED : 0;
		return mixedInt|heatedInt|bakedInt;	
	}
	public static String getHeatingInfo(ObjectInstance obj) {
		return obj.getStringValForAttribute(IngredientFactory.attributeHeatingInfo);
	}
	
	public static String getHeatedState(ObjectInstance obj) {
		return obj.getStringValForAttribute(IngredientFactory.attributeHeatedState);
	}
	
	public static ObjectInstance changeHeatedState(ObjectInstance obj, String heatedState) {
		return obj.appendRelationalTarget(IngredientFactory.attributeHeatedState, heatedState);
	}
}