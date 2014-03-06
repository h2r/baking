package edu.brown.cs.h2r.baking.ObjectFactories;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import edu.brown.cs.h2r.baking.IngredientRecipe;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;

public class IngredientFactory {

	public static final String ClassNameSimple = "simple_ingredient";
	public static final String ClassNameComplex = "complex_ingredient";
	private static final String attributeBaked = "baked";
	private static final String attributeMelted = "melted";
	private static final String attributeMixed = "mixed";
	private static final String attributeContainer = "container";
	private static final String attributeContains = "contents";

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
		
		objectClass.addAttribute(
				new Attribute(domain, IngredientFactory.attributeContainer,
						Attribute.AttributeType.RELATIONAL));
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
		return objectClass;
	}
		
	public static ObjectInstance getNewSimpleIngredientObjectInstance(ObjectClass simpleIngredientClass, String name, 
			Boolean baked, Boolean melted, Boolean mixed, String ingredientContainer) {
		ObjectInstance newInstance = new ObjectInstance(simpleIngredientClass, name);
		newInstance.setValue(IngredientFactory.attributeBaked, baked ? 1 : 0);
		newInstance.setValue(IngredientFactory.attributeMelted, melted ? 1 : 0);
		newInstance.setValue(IngredientFactory.attributeMixed, mixed ? 1 : 0);
		if (ingredientContainer != null || ingredientContainer != "")
		{
			newInstance.addRelationalTarget(IngredientFactory.attributeContainer, ingredientContainer);
		}
		return newInstance;		
	}
	
	public static ObjectInstance getNewComplexIngredientObjectInstance(ObjectClass complexIngredientClass, String name, 
			Boolean baked, Boolean melted, Boolean mixed, String ingredientContainer, Iterable<String> contents) {
		ObjectInstance newInstance = new ObjectInstance(complexIngredientClass, name);
		newInstance.setValue(IngredientFactory.attributeBaked, baked ? 1 : 0);
		newInstance.setValue(IngredientFactory.attributeMelted, melted ? 1 : 0);
		newInstance.setValue(IngredientFactory.attributeMixed, mixed ? 1 : 0);
		
		if (ingredientContainer != null || ingredientContainer != "") {
			newInstance.addRelationalTarget(IngredientFactory.attributeContainer, ingredientContainer);
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
					ingredientRecipe.getMixed(), ingredientContainer);
		}
		return null;
	}
	
	public static ObjectInstance getNewIngredientInstance(ObjectInstance objectInstance, String name) {
		Boolean baked = IngredientFactory.isBakedIngredient(objectInstance);
		Boolean mixed = IngredientFactory.isMixedIngredient(objectInstance);
		Boolean melted = IngredientFactory.isMeltedIngredient(objectInstance);
		Set<String> contents = IngredientFactory.getContentsForIngredient(objectInstance);
		String container = IngredientFactory.getContainer(objectInstance);
		return IngredientFactory.getNewComplexIngredientObjectInstance(objectInstance.getObjectClass(), name, baked, melted, mixed, container, contents);
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
	
	public static Boolean isBakedIngredient(ObjectInstance ingredient) {
		return ingredient.getDiscValForAttribute(IngredientFactory.attributeBaked) == 1;
	}
	
	public static Boolean isMixedIngredient(ObjectInstance ingredient) {
		return ingredient.getDiscValForAttribute(IngredientFactory.attributeMixed) == 1;
	}
	
	public static Boolean isMeltedIngredient(ObjectInstance ingredient) {
		return ingredient.getDiscValForAttribute(IngredientFactory.attributeMelted) == 1;
	}
	
	public static Boolean isSimple(ObjectInstance ingredient) {
		return ingredient.getObjectClass().name == IngredientFactory.ClassNameSimple;
	}
	
	public static Set<String> getContentsForIngredient(ObjectInstance ingredient) {
		return new TreeSet<String>(ingredient.getAllRelationalTargets(IngredientFactory.attributeContains));
	}	
}