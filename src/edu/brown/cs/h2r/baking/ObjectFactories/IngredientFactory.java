package edu.brown.cs.h2r.baking.ObjectFactories;
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
import edu.brown.cs.h2r.baking.Knowledgebase.IngredientKnowledgebase;
import edu.brown.cs.h2r.baking.Recipes.Recipe;

public class IngredientFactory {

	public static final String ClassNameSimple = "simple_ingredient";
	public static final String ClassNameComplex = "complex_ingredient";
	public static final String ClassNameSimpleHidden = "simple_hidden_ingredient";
	public static final String ClassNameComplexHidden = "complex_hidden_ingredient";
	private static final String attributeBaked = "baked";
	private static final String attributeMelted = "melted";
	private static final String attributeMixed = "mixed";
	private static final String attributePeeled = "peeled";
	private static final String attributeContainer = "container";
	private static final String attributeContains = "contents";
	private static final String attributeTraits = "traits";
	private static final String attributeSwapped = "swapped";
	private static final String attributeUseCount = "useCount";
	private static final String attributeToolTraits = "toolTrait";
	private static final String attributeToolAttributes = "toolAttribute";
	private static final String[] booleanAttributes= {IngredientFactory.attributeBaked, 
		IngredientFactory.attributeMelted, IngredientFactory.attributeMixed, IngredientFactory.attributePeeled};

	private static ObjectClass createObjectClass(Domain domain, String className) {
		ObjectClass objectClass = new ObjectClass(domain, className);
		Attribute mixingAttribute = 
				new Attribute(domain, IngredientFactory.attributeBaked, Attribute.AttributeType.BOOLEAN);
		objectClass.addAttribute(mixingAttribute);
		
		Attribute heatingAttribute = 
				new Attribute(domain, IngredientFactory.attributeMelted, Attribute.AttributeType.BOOLEAN);
		objectClass.addAttribute(heatingAttribute);
		
		Attribute receivingAttribute =
				new Attribute(domain, IngredientFactory.attributeMixed, Attribute.AttributeType.BOOLEAN);
		objectClass.addAttribute(receivingAttribute);
		
		Attribute peelAttribute =
				new Attribute(domain, IngredientFactory.attributePeeled, Attribute.AttributeType.BOOLEAN);
		objectClass.addAttribute(peelAttribute);
		
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
		
	@Deprecated
	public static ObjectInstance getNewSimpleIngredientObjectInstance(ObjectClass simpleIngredientClass, String name, 
			int attributes, Set<String> traits, String ingredientContainer) {
		ObjectInstance newInstance = new ObjectInstance(simpleIngredientClass, name);
		setAttributes(newInstance, attributes);
		newInstance.setValue(IngredientFactory.attributeUseCount, 1);
		for (String trait : traits) {
			newInstance.addRelationalTarget("traits", trait);
		}
		if (ingredientContainer != null || ingredientContainer != "")
		{
			newInstance.addRelationalTarget(IngredientFactory.attributeContainer, ingredientContainer);
		}
		return newInstance;		
	}
	
	public static ObjectInstance getNewSimpleIngredientObjectInstance(ObjectClass simpleIngredientClass, String name, 
			int attributes, int useCount, Set<String> traits, Set<String> toolTraits,
			Set<String> toolAttributes, String ingredientContainer) {
		ObjectInstance newInstance = new ObjectInstance(simpleIngredientClass, name);
		setAttributes(newInstance, attributes);
		newInstance.setValue(IngredientFactory.attributeUseCount, useCount);
		for (String trait : traits) {
			newInstance.addRelationalTarget(IngredientFactory.attributeTraits, trait);
		}
		
		for (String toolTrait : toolTraits) {
			newInstance.addRelationalTarget(IngredientFactory.attributeToolTraits, toolTrait);
		}
		
		for (String toolAttribute : toolAttributes) {
			newInstance.addRelationalTarget(IngredientFactory.attributeToolAttributes, toolAttribute);
		}
		if (ingredientContainer != null || ingredientContainer != "")
		{
			newInstance.addRelationalTarget(IngredientFactory.attributeContainer, ingredientContainer);
		}
		return newInstance;		
	}
	
	public static ObjectInstance getNewComplexIngredientObjectInstance(ObjectClass complexIngredientClass, String name, 
			int attributes, boolean swapped, String ingredientContainer, Set<String> traits, 
			Set<String> toolTraits, Set<String> toolAttributes, Iterable<String> contents) {
		ObjectInstance newInstance = new ObjectInstance(complexIngredientClass, name);
		setAttributes(newInstance, attributes);
		newInstance.setValue(IngredientFactory.attributeUseCount, 1);
		newInstance.setValue(IngredientFactory.attributeSwapped, swapped ? 1 : 0);
		
		if (ingredientContainer != null || ingredientContainer != "") {
			newInstance.addRelationalTarget(IngredientFactory.attributeContainer, ingredientContainer);
		}
		for (String trait : traits) {
			newInstance.addRelationalTarget("traits", trait);
		}
		for (String toolTrait : toolTraits) {
			newInstance.addRelationalTarget(IngredientFactory.attributeToolTraits, toolTrait);
		}
		
		for (String toolAttribute : toolAttributes) {
			newInstance.addRelationalTarget(IngredientFactory.attributeToolAttributes, toolAttribute);
		}
		
		if (contents != null) {
			for (String ingredient : contents) {
				newInstance.addRelationalTarget(IngredientFactory.attributeContains, ingredient);
			}
		}
		
		return newInstance;
	}
	
	@Deprecated
	public static ObjectInstance getNewComplexIngredientObjectInstance(ObjectClass complexIngredientClass, String name, 
			Boolean baked, Boolean melted, Boolean mixed, Boolean peeled, Boolean swapped, int useCount, 
			String ingredientContainer, Set<String> traits, Iterable<String> contents) {
		ObjectInstance newInstance = new ObjectInstance(complexIngredientClass, name);
		newInstance.setValue(IngredientFactory.attributeBaked, baked ? 1 : 0);
		newInstance.setValue(IngredientFactory.attributeMelted, melted ? 1 : 0);
		newInstance.setValue(IngredientFactory.attributeMixed, mixed ? 1 : 0);
		newInstance.setValue(IngredientFactory.attributeUseCount, useCount);
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
	
	
	public static ObjectInstance getNewIngredientInstance(ObjectInstance objectInstance, String name) {
		Boolean baked = IngredientFactory.isBakedIngredient(objectInstance);
		Boolean mixed = IngredientFactory.isMixedIngredient(objectInstance);
		Boolean melted = IngredientFactory.isMeltedIngredient(objectInstance);
		Boolean peeled = IngredientFactory.isPeeledIngredient(objectInstance);
		
		int attributes = IngredientRecipe.generateAttributeNumber(mixed, melted, baked, peeled);
		int useCount = IngredientFactory.getUseCount(objectInstance);
		String container = IngredientFactory.getContainer(objectInstance);
		Set<String> traits = IngredientFactory.getTraits(objectInstance);
		Set<String> toolTraits = IngredientFactory.getToolTraits(objectInstance);
		Set<String> toolAttributes = IngredientFactory.getToolAttributes(objectInstance);
		ObjectClass oc = objectInstance.getObjectClass();
		
		if (oc.name.equals(IngredientFactory.ClassNameSimple)) {
			return IngredientFactory.getNewSimpleIngredientObjectInstance(objectInstance.getObjectClass(), 
					name, useCount, attributes, traits, toolTraits, toolAttributes, container);
		}
		
		Boolean swapped = IngredientFactory.isSwapped(objectInstance);
		Set<String> contents = IngredientFactory.getIngredientContents(objectInstance);
		return IngredientFactory.getNewComplexIngredientObjectInstance(oc, name, attributes, swapped, 
				container, traits, toolTraits, toolAttributes, contents);
	}
	
	@Deprecated
	public static ObjectInstance getNewIngredientInstance(ObjectClass simpleIngredientClass, 
			IngredientRecipe ingredientRecipe, String ingredientContainer) {
		/*	int attributes = ingredientRecipe.generateAttributeNumber(ingredientRecipe.getBaked(), 
					ingredientRecipe.getMelted(),  ingredientRecipe.getMixed(), 
					ingredientRecipe.getPeeled());
		if (ingredientRecipe.isSimple()) {
			return IngredientFactory.getNewSimpleIngredientObjectInstance(simpleIngredientClass, ingredientRecipe.getName(), 
					attributes, ingredientRecipe.getUseCount(), ingredientRecipe.getTraits(), ingredientContainer);
		}*/
		return null;
	}
	
	
	public static ObjectInstance getNewIngredientInstance(IngredientRecipe ingredient, String name, ObjectClass oc) {
		int attributes = IngredientRecipe.generateAttributeNumber(ingredient.getBaked(), ingredient.getMixed(), 
				ingredient.getMelted(), ingredient.getPeeled());
		Boolean swapped = ingredient.getSwapped();
		int useCount = ingredient.getUseCount();
		String container = "";
		Set<String> traits = ingredient.getTraits();
		Set<String> toolTraits = ingredient.getToolTraits();
		Set<String> toolAttributes= ingredient.getToolAttributes();
		if (ingredient.isSimple()) {
			return IngredientFactory.getNewSimpleIngredientObjectInstance(oc, name, attributes, useCount, traits, toolTraits, toolAttributes, container);
		}
		Set<String> contents = new TreeSet<String>();
		for (IngredientRecipe ing : ingredient.getContents()) {
			contents.add(ing.getName());
		}
		return IngredientFactory.getNewComplexIngredientObjectInstance(oc, name, attributes, swapped, container, traits, toolTraits, toolAttributes, contents);
	}
	
	public static List<ObjectInstance> getIngredientInstancesList(ObjectClass simpleIngredientClass,
			IngredientRecipe ingredientRecipe) {
		List<ObjectInstance> newInstances = new ArrayList<ObjectInstance>();
		if (ingredientRecipe.isSimple()) {
			newInstances.add(IngredientFactory.getNewIngredientInstance(ingredientRecipe, ingredientRecipe.getName(), simpleIngredientClass));
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
				newInstances.add(IngredientFactory.getNewIngredientInstance(subIngredient, subIngredient.getName(), simpleIngredientClass));
			}
		}
		return newInstances;
	}
	
	public static List<ObjectInstance> getComplexIngredients(ObjectClass complexIngredientClass,IngredientRecipe ingredientRecipe) {
		List<ObjectInstance> newInstances = new ArrayList<ObjectInstance>();
		
		for (IngredientRecipe subIngredient : ingredientRecipe.getContents()) {
			if (!subIngredient.isSimple()) {
				newInstances.add(IngredientFactory.getNewIngredientInstance(subIngredient, subIngredient.getName(), complexIngredientClass));
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
	
	public static int getUseCount(ObjectInstance ingredient) {
		return ingredient.getDiscValForAttribute(IngredientFactory.attributeUseCount);
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
	
	public static Boolean isPeeledIngredient(ObjectInstance ingredient){
		return ingredient.getDiscValForAttribute(IngredientFactory.attributePeeled) == 1;
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
	
	public static void setUseCount(ObjectInstance ingredient, int count) {
		ingredient.setValue(IngredientFactory.attributeUseCount, count);
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
		return new TreeSet<String>(ingredient.getAllRelationalTargets(IngredientFactory.attributeContains));
	}

	public static void setPeeled(ObjectInstance ingredient, boolean isPeeled) {
		ingredient.setValue(IngredientFactory.attributePeeled, isPeeled ? 1 : 0);
	}

	public static void setSwapped(ObjectInstance ingredient) {
		if (!isSimple(ingredient)) {
			ingredient.setValue(IngredientFactory.attributeSwapped, 1);
		}
	}
	
	public static void addToolTrait(ObjectInstance object, String trait) {
		object.addRelationalTarget(IngredientFactory.attributeToolTraits, trait);
	}
	
	public static void addToolTraits(ObjectInstance object,Set<String> traits) {
		for (String trait : traits) {
			object.addRelationalTarget(IngredientFactory.attributeToolTraits, trait);
		}
	}
	
	public static Set<String> getToolTraits(ObjectInstance object) {
		return object.getAllRelationalTargets(IngredientFactory.attributeToolTraits);
	}
	
	public static Set<String> getToolAttributes(ObjectInstance object) {
		return object.getAllRelationalTargets(IngredientFactory.attributeToolAttributes);
	}
	
	public static void addToolAttribute(ObjectInstance object, String attribute) {
		object.addRelationalTarget(IngredientFactory.attributeToolAttributes, attribute);
	}
	
	public static void addToolAttribute(ObjectInstance object, Set<String> attributes) {
		for (String attribute : attributes) {
			object.addRelationalTarget(IngredientFactory.attributeToolAttributes, attribute);
		}
	}
	
	
	public static Set<String> getRecursiveContentsForIngredient(State state, ObjectInstance ingredient) {
		Set<String> contents = new TreeSet<String>();
		for (String content_name : IngredientFactory.getIngredientContents(ingredient)) {
			ObjectInstance content = state.getObject(content_name);
			if (IngredientFactory.isSimple(content)) {
				contents.add(content_name);
			} else {
				contents.addAll(getRecursiveContentsForIngredient(state, content));
			}
		}
		return contents;
	}
	
	public static Set<String> getRecursiveContentsAndSwapped(State state, ObjectInstance ingredient) {
		Set<String> contents = new TreeSet<String>();
		if (IngredientFactory.isSimple(ingredient)) {
			contents.add(ingredient.getName());
			return contents;
		}
		for (String content_name : IngredientFactory.getIngredientContents(ingredient)) {
			ObjectInstance content = state.getObject(content_name);
			if (IngredientFactory.isSimple(content) || IngredientFactory.isSwapped(content)) {
				contents.add(content_name);
			} else {
				contents.addAll(getRecursiveContentsAndSwapped(state, content));
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
	
	public static void hideUnecessaryIngredients(State state, Domain domain, IngredientRecipe goal, List<ObjectInstance>allIngredients) {
		Boolean match;
		for (ObjectInstance obj : allIngredients) {
			match = false;
			for (IngredientRecipe ing : goal.getContents()) {
				if (ing.getName().equals(obj.getName())) {
					match = true;
					break;
				}
			}
			if (!match) {
				for (String trait : goal.getNecessaryTraits().keySet()) {
					if (IngredientFactory.getTraits(obj).contains(trait)) {
						match = true;
						break;
					}
				}
			}
			if (match) {
				if (IngredientFactory.getUseCount(obj) == 1) {
					ObjectInstance hidden = makeHiddenObjectCopy(state, domain, obj);
					ObjectInstance container = state.getObject(IngredientFactory.getContainer(hidden));
					ContainerFactory.removeIngredient(container, hidden.getName());
					state.removeObject(obj);
					state.addObject(hidden);
				} //else {
					IngredientFactory.setUseCount(obj, IngredientFactory.getUseCount(obj) -1);
				//}
			}
		}
		
	}
	
	/* Rids the object instance of any attrbiutes, such that I can make a recipe require
	 * a certain attribute for an ingredient but when the object instance is created for planning,
	 * this attribute is already fulfilled.
	 */
	public static void clearBooleanAttributes(ObjectInstance obj) {
		for (String att_name : IngredientFactory.booleanAttributes) {
			obj.setValue(att_name, 0);
		}		
	}

	public static boolean isMeltedAtRoomTemperature(ObjectInstance ingredient) {
		return IngredientFactory.getTraits(ingredient).contains(IngredientKnowledgebase.NONMELTABLE);
	}
	
	public static boolean isLubricant(ObjectInstance ingredient) {
		return IngredientFactory.getTraits(ingredient).contains(IngredientKnowledgebase.LUBRICANT);
	}
	
	public static void setAttributes(ObjectInstance ingredient, int attributes) {
		ingredient.setValue(IngredientFactory.attributeBaked, ((attributes & Recipe.BAKED) == Recipe.BAKED) ? 1 : 0);
		ingredient.setValue(IngredientFactory.attributeMelted, ((attributes & Recipe.MELTED) == Recipe.MELTED) ? 1 : 0);
		ingredient.setValue(IngredientFactory.attributeMixed, ((attributes & Recipe.MIXED) == Recipe.MIXED) ? 1 : 0);
		ingredient.setValue(IngredientFactory.attributePeeled, ((attributes & Recipe.PEELED) == Recipe.PEELED) ? 1 : 0);
	}
}