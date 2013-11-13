import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;

public abstract class Ingredient {

	private final Domain domain;
	private final ObjectClass simpleIngredientClass;
	private final ObjectClass complexIngredientClass;
	private static final String attributeBaked = "baked";
	private static final String attributeMelted = "melted";
	private static final String attributeMixed = "mixed";
	private static final String attributeContainer = "container";
	private static final String attributeContains = "contents";
	
	
	public Ingredient(Domain domain) {
		this.domain = domain;
		this.simpleIngredientClass = this.createSimpleIngredientObjectClass();
		this.complexIngredientClass = this.createComplexIngredientObjectClass();
	}
	
	private ObjectClass createObjectClass(String className) {
		ObjectClass objectClass = new ObjectClass(this.domain, className);
		Attribute mixingAttribute = 
				new Attribute(this.domain, Ingredient.attributeBaked, Attribute.AttributeType.DISC);
		mixingAttribute.setDiscValuesForRange(0,1,1);
		objectClass.addAttribute(mixingAttribute);
		
		Attribute heatingAttribute = 
				new Attribute(this.domain, Ingredient.attributeMelted, Attribute.AttributeType.DISC);
		heatingAttribute.setDiscValuesForRange(0,1,1);
		objectClass.addAttribute(heatingAttribute);
		
		Attribute receivingAttribute =
				new Attribute(this.domain, Ingredient.attributeMixed, Attribute.AttributeType.DISC);
		receivingAttribute.setDiscValuesForRange(0,1,1);
		objectClass.addAttribute(receivingAttribute);
		
		objectClass.addAttribute(
				new Attribute(this.domain, Ingredient.attributeContainer,
						Attribute.AttributeType.RELATIONAL));
		return objectClass;
	}
	private ObjectClass createSimpleIngredientObjectClass() {
		return this.createObjectClass("simple_ingredient");
	}
	
	private ObjectClass createComplexIngredientObjectClass() {
		ObjectClass objectClass = this.createObjectClass("complex_ingredient");
		objectClass.addAttribute(
				new Attribute(this.domain, Ingredient.attributeContains, 
						Attribute.AttributeType.MULTITARGETRELATIONAL));
		return objectClass;
	}
	
	public ObjectClass getSimpleIngredientObjectClass() {
		return this.simpleIngredientClass;
	}
	
	public ObjectClass getComplexIngredientObjectClass() {
		return this.complexIngredientClass;
	}
	
	public ObjectInstance getNewSimpleIngredientObjectInstance(String name, 
			Boolean baked, Boolean melted, Boolean mixed, String ingredientContainer) {
		ObjectInstance newInstance = new ObjectInstance(this.simpleIngredientClass, name);
		newInstance.setValue(Ingredient.attributeBaked, baked ? 1 : 0);
		newInstance.setValue(Ingredient.attributeMelted, melted ? 1 : 0);
		newInstance.setValue(Ingredient.attributeMixed, mixed ? 1 : 0);
		if (ingredientContainer != null || ingredientContainer != "")
		{
			newInstance.addRelationalTarget(Ingredient.attributeContainer, ingredientContainer);
		}
		return newInstance;		
	}
	
	public ObjectInstance getNewComplexIngredientObjectInstance(String name, 
			Boolean baked, Boolean melted, Boolean mixed, String ingredientContainer, List<String> contents) {
		ObjectInstance newInstance = new ObjectInstance(this.complexIngredientClass, name);
		newInstance.setValue(Ingredient.attributeBaked, baked ? 1 : 0);
		newInstance.setValue(Ingredient.attributeMelted, melted ? 1 : 0);
		newInstance.setValue(Ingredient.attributeMixed, mixed ? 1 : 0);
		
		if (ingredientContainer != null || ingredientContainer != "") {
			newInstance.addRelationalTarget(Ingredient.attributeContainer, ingredientContainer);
		}
		
		if (contents != null) {
			for (String ingredient : contents) {
				newInstance.addRelationalTarget(Ingredient.attributeContains, ingredient);
			}
		}
		
		return newInstance;
	}

	public static void addIngredient(ObjectInstance complexIngredient, ObjectInstance ingredient) {
		complexIngredient.addRelationalTarget(Ingredient.attributeContains, ingredient.getName());
	}
	
	public static void addIngredientList(ObjectInstance complexIngredient, List<ObjectInstance> ingredientList) {
		for (ObjectInstance ingredient : ingredientList) {
			Ingredient.addIngredient(complexIngredient, ingredient);
		}
	}
	
	public static void changeIngredientContainer(ObjectInstance ingredient, String ingredientContainer) {
		if (ingredientContainer != null) {
			ingredient.addRelationalTarget(Ingredient.attributeContainer, ingredientContainer);
		}
	}
	
	public abstract ObjectInstance getObjectInstance(ObjectClass ingredientClass);
	public abstract List<ObjectInstance> getSimpleObjectInstances(ObjectClass ingredientClass);
	
}