package edu.brown.cs.h2r.baking.ObjectFactories;

import java.util.List;
import java.util.Set;

import edu.brown.cs.h2r.baking.IngredientRecipe;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public class ToolFactory {

	public static final String ClassName = "tool";
	private static final String attributeToolTrait = "toolTrait";
	private static final String attributeToolAttribute = "toolAttribute";
	private static final String attributeSpace = "space";
	private static final String attributeContains = "contains";
	private static final String attributeTransportable = "transportable";
	
	public static ObjectClass createObjectClass(Domain domain) {
		ObjectClass objectClass = new ObjectClass(domain, ToolFactory.ClassName);
		
		objectClass.addAttribute(
				new Attribute(domain, ToolFactory.attributeToolTrait, 
						Attribute.AttributeType.RELATIONAL));
		
		objectClass.addAttribute(
				new Attribute(domain, ToolFactory.attributeToolAttribute, 
						Attribute.AttributeType.RELATIONAL));
		
		objectClass.addAttribute(
				new Attribute(domain, ToolFactory.attributeSpace,
						Attribute.AttributeType.RELATIONAL));
		
		objectClass.addAttribute(
				new Attribute(domain, ToolFactory.attributeContains,
						Attribute.AttributeType.MULTITARGETRELATIONAL));
		
		Attribute transportableAttribute = 
				new Attribute(domain, ToolFactory.attributeTransportable, Attribute.AttributeType.BOOLEAN);
		objectClass.addAttribute(transportableAttribute);
		
		return objectClass;
	}
	
	public static ObjectInstance getNewObjectInstance(Domain domain, String name, 
			String trait, String attribute, String containerSpace) {
		ObjectClass oc = domain.getObjectClass(ToolFactory.ClassName);
		return getNewObjectInstance(oc, name, trait, attribute, containerSpace);
	}
	
	private static ObjectInstance getNewObjectInstance(ObjectClass toolClass, String name, 
			String trait, String attribute, String containerSpace) {
		ObjectInstance newInstance = new ObjectInstance(toolClass, name);
		newInstance.addRelationalTarget(ToolFactory.attributeToolTrait, trait);
		newInstance.addRelationalTarget(ToolFactory.attributeToolAttribute, attribute);
		//newInstance.addRelationalTarget(ToolFactory.attributeContains, null);
		newInstance.setValue(ToolFactory.attributeTransportable, false);
		
		if (containerSpace != null || containerSpace != "")
		{
			newInstance.addRelationalTarget(ToolFactory.attributeSpace, containerSpace);
		}
		return newInstance;
	}
	
	public static ObjectInstance getNewSimpleToolObjectInstance(Domain domain, String name, 
			String trait, String attribute, String containerSpace) {
		return getNewObjectInstance(domain, name, trait, attribute, containerSpace);
	}
	
	private static ObjectInstance getNewSimpleToolObjectInstance(ObjectClass toolClass, String name, 
			String trait, String attribute, String containerSpace) {
		return getNewObjectInstance(toolClass, name, trait, attribute, containerSpace);
	}
	
	public static ObjectInstance getNewTransportableToolObjectInstance(Domain domain, String name, 
			String trait, String attribute, String containerSpace) {
		ObjectInstance tool = getNewObjectInstance(domain, name, trait, attribute, containerSpace);
		tool.setValue(ToolFactory.attributeTransportable, true);
		return tool;
	}
	
	private static ObjectInstance getNewTransportableToolObjectInstance(ObjectClass toolClass, String name, 
			String trait, String attribute, String containerSpace) {
		ObjectInstance tool = getNewObjectInstance(toolClass, name, trait, attribute, containerSpace);
		tool.setValue(ToolFactory.attributeTransportable, true);
		return tool;
		
	}
	
	public static String getToolTrait(ObjectInstance tool) {
		Set<String> traits = tool.getAllRelationalTargets(ToolFactory.attributeToolTrait);
		if (traits != null && !traits.isEmpty()) {
			return traits.iterator().next();
		}
		return null;
	}
	
	public static String getToolAttribute(ObjectInstance tool) {
		Set<String> attributes = tool.getAllRelationalTargets(ToolFactory.attributeToolAttribute);
		if (attributes != null && !attributes.isEmpty()) {
			return attributes.iterator().next();
		}
		return null;
	}
	
	public static String getSpaceName(ObjectInstance tool) {
		Set<String> spaces =  tool.getAllRelationalTargets(ToolFactory.attributeSpace);
		if (spaces != null && !spaces.isEmpty()) {
			return spaces.iterator().next();
		}
		return null;
	}
	
	public static boolean toolCanBeUsed(ObjectInstance tool, IngredientRecipe ingredient) {
		return ingredient.hasToolTrait(ToolFactory.getToolTrait(tool));
	}
	
	public static boolean toolCanBeUsed(ObjectInstance tool, ObjectInstance ingredient) {
		return IngredientFactory.getToolTraits(ingredient).contains(ToolFactory.getToolTrait(tool));
	}
	
	public static boolean toolHasBeenUsed(ObjectInstance tool, IngredientRecipe ingredient) {
		return ingredient.hasToolAttribute(ToolFactory.getToolAttribute(tool));
	}
	
	public static boolean toolHasBeenUsed(ObjectInstance tool, ObjectInstance ingredient) {
		return IngredientFactory.getToolAttributes(ingredient).contains(ToolFactory.getToolAttribute(tool));
	}
	
	public static Set<String> getContents(ObjectInstance tool) {
		return tool.getAllRelationalTargets(ToolFactory.attributeContains);
	}
	
	public static void removeContents(ObjectInstance tool) {
		tool.clearRelationalTargets(ToolFactory.attributeContains);
	}
	
	public static void removeIngredient(ObjectInstance tool, ObjectInstance ingredient) {
		tool.removeRelationalTarget(ToolFactory.attributeContains, ingredient.getName());
	}
	
	public static void removeIngredient(ObjectInstance tool, IngredientRecipe ingredient) {
		tool.removeRelationalTarget(ToolFactory.attributeContains, ingredient.getName());
	}
	
	public static void addIngredient(ObjectInstance tool, ObjectInstance ingredient) {
		tool.addRelationalTarget(ToolFactory.attributeContains, ingredient.getName());
	}
	
	public static void addIngredient(ObjectInstance tool, IngredientRecipe ingredient) {
		tool.addRelationalTarget(ToolFactory.attributeContains, ingredient.getName());
	}
	
	public static boolean toolIsTransportable(ObjectInstance tool) {
		return tool.getValueForAttribute(ToolFactory.attributeTransportable).getBooleanValue();
	}
	
	public static boolean toolContainsIngredient(ObjectInstance tool, ObjectInstance ingredient) {
		if (!ToolFactory.toolIsTransportable(tool)) {
			return false;
		}
		return ToolFactory.getContents(tool).contains(ingredient.getName());
	}
	
	public static boolean toolContainsIngredient(ObjectInstance tool, IngredientRecipe ingredient) {
		if (!ToolFactory.toolIsTransportable(tool)) {
			return false;
		}
		return ToolFactory.getContents(tool).contains(ingredient.getName());
	}
	
	public static boolean isEmpty(ObjectInstance tool) {
		if (!ToolFactory.toolIsTransportable(tool)) {
			return true;
		}
		return ToolFactory.getContents(tool).isEmpty();
	}
	
	public static void pourIngredients(State state, ObjectInstance tool, ObjectInstance container) {
		Set<String> contents = ToolFactory.getContents(tool);
		for (String name : contents) {
			ObjectInstance ingredient = state.getObject(name);
			ContainerFactory.addIngredient(container, name);
			IngredientFactory.changeIngredientContainer(ingredient, container.getName());
		}
		ToolFactory.removeContents(tool);
	}
}
