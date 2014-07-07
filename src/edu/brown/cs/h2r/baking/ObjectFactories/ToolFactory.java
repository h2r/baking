package edu.brown.cs.h2r.baking.ObjectFactories;

import java.util.List;
import java.util.Set;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;

public class ToolFactory {

	public static final String ClassName = "tool";
	private static final String attributeToolTrait = "toolTrait";
	private static final String attributeToolAttribute = "toolAttribute";
	private static final String attributeSpace = "space";
	
	public static ObjectClass createObjectClass(Domain domain) {
		ObjectClass objectClass = new ObjectClass(domain, ToolFactory.ClassName);
		
		objectClass.addAttribute(
				new Attribute(domain, ToolFactory.attributeToolTrait, 
						Attribute.AttributeType.MULTITARGETRELATIONAL));
		
		objectClass.addAttribute(
				new Attribute(domain, ToolFactory.attributeToolAttribute, 
						Attribute.AttributeType.MULTITARGETRELATIONAL));
		
		objectClass.addAttribute(
				new Attribute(domain, ToolFactory.attributeSpace,
						Attribute.AttributeType.RELATIONAL));
		return objectClass;
	}
	
	public static ObjectInstance getNewObjectInstance(ObjectClass toolClass, String name, 
			Set<String> traits, Set<String> attributes, String containerSpace) {
		ObjectInstance newInstance = new ObjectInstance(toolClass, name);
		for (String trait : traits) {
			newInstance.addRelationalTarget(ToolFactory.attributeToolTrait, trait);
		}
		for (String attribute : attributes) {
			newInstance.addRelationalTarget(ToolFactory.attributeToolAttribute, attribute);
		}
		
		if (containerSpace != null || containerSpace != "")
		{
			newInstance.addRelationalTarget(ToolFactory.attributeSpace, containerSpace);
		}
		return newInstance;
	}
	
	public static Set<String> getToolTraits(ObjectInstance tool) {
		return tool.getAllRelationalTargets(ToolFactory.attributeToolTrait);
	}
	
	public static Set<String> getToolAttributes(ObjectInstance tool) {
		return tool.getAllRelationalTargets(ToolFactory.attributeToolAttribute);
	}
	
	public static String getSpaceName(ObjectInstance tool) {
		Set<String> spaces =  tool.getAllRelationalTargets(ToolFactory.attributeSpace);
		if (spaces != null && !spaces.isEmpty()) {
			return spaces.iterator().next();
		}
		return null;
	}
	
	//public static Boolean 
}
