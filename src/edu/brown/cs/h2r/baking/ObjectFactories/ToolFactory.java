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
						Attribute.AttributeType.RELATIONAL));
		
		objectClass.addAttribute(
				new Attribute(domain, ToolFactory.attributeToolAttribute, 
						Attribute.AttributeType.RELATIONAL));
		
		objectClass.addAttribute(
				new Attribute(domain, ToolFactory.attributeSpace,
						Attribute.AttributeType.RELATIONAL));
		return objectClass;
	}
	
	public static ObjectInstance getNewToolObjectInstance(Domain domain, String name, 
			String trait, String attribute, String containerSpace) {
		ObjectClass oc = domain.getObjectClass(ToolFactory.ClassName);
		return getNewToolObjectInstance(oc, name, trait, attribute, containerSpace);
	}
	
	private static ObjectInstance getNewToolObjectInstance(ObjectClass toolClass, String name, 
			String trait, String attribute, String containerSpace) {
		ObjectInstance newInstance = new ObjectInstance(toolClass, name);
		newInstance.addRelationalTarget(ToolFactory.attributeToolTrait, trait);
		newInstance.addRelationalTarget(ToolFactory.attributeToolAttribute, attribute);
		
		if (containerSpace != null || containerSpace != "")
		{
			newInstance.addRelationalTarget(ToolFactory.attributeSpace, containerSpace);
		}
		return newInstance;
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
	
	//public static Boolean 
}
