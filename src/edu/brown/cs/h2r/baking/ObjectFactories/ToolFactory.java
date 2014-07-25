package edu.brown.cs.h2r.baking.ObjectFactories;

import java.util.List;
import java.util.Set;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;

public class ToolFactory {
	public static final String ClassName = "tool";

	private static final String attributeX = "attX";
	private static final String attributeY = "attY";
	private static final String attributeZ = "attZ";
	private static final String attributeSpace = "space";
	private static final String attributeUsed = "used";
	private static final String attributeType = "type";
	
	public static final String WHISK = "whisk";
	public static final String whiskType = "dry";
	public static final String SPATULA = "spatula";
	public static final String spatulaType = "wet";

	
	public static ObjectClass createObjectClass(Domain domain)
	{
		ObjectClass objectClass = new ObjectClass(domain, ToolFactory.ClassName);
		
		Attribute usedAttribute =
				new Attribute(domain, ToolFactory.attributeUsed, Attribute.AttributeType.BOOLEAN);
		objectClass.addAttribute(usedAttribute);
		
		objectClass.addAttribute(
				new Attribute(domain, ToolFactory.attributeSpace,
						Attribute.AttributeType.RELATIONAL));
		
		objectClass.addAttribute(
				new Attribute(domain, ToolFactory.attributeType,
						Attribute.AttributeType.RELATIONAL));
		
		objectClass.addAttribute (
				new Attribute(domain, ToolFactory.attributeX, 
						Attribute.AttributeType.REALUNBOUND));
		
		objectClass.addAttribute (
				new Attribute(domain, ToolFactory.attributeY, 
						Attribute.AttributeType.REALUNBOUND));
		
		objectClass.addAttribute (
				new Attribute(domain, ToolFactory.attributeZ, 
						Attribute.AttributeType.REALUNBOUND));
		return objectClass;
	}
	
	public static ObjectInstance getNewObjectInstance(ObjectClass toolClass, String name, 
			String type, String toolSpace, double x, double y, double z) {
		ObjectInstance newInstance = new ObjectInstance(toolClass, name);
		newInstance.setValue(ToolFactory.attributeUsed, false);
		newInstance.addRelationalTarget(ToolFactory.attributeType, type);
		
		newInstance.setValue(ToolFactory.attributeX, x);
		newInstance.setValue(ToolFactory.attributeY, y);
		newInstance.setValue(ToolFactory.attributeZ, z);
		
		if (toolSpace != null || toolSpace != "")
		{
			newInstance.addRelationalTarget(ToolFactory.attributeSpace, toolSpace);
		}
		return newInstance;
	}
	
	public static ObjectInstance getNewObjectInstance(Domain domain, String name, 
			String type, String containerSpace, double x, double y, double z) {
		return ToolFactory.getNewObjectInstance(
				domain.getObjectClass(ToolFactory.ClassName), name, type, containerSpace, x, y, z);
	}
	
	public static void changetoolSpace(ObjectInstance tool, String toolSpace) {
		if (toolSpace != null) {
			tool.addRelationalTarget(ToolFactory.attributeSpace, toolSpace);
		}
	}
	
	public static String getSpaceName(ObjectInstance tool) {
		Set<String> spaces = tool.getAllRelationalTargets(ToolFactory.attributeSpace);
		if (spaces != null && !spaces.isEmpty()) {
			return spaces.iterator().next();
		}
		return null;
	}
	
	public static double getX(ObjectInstance tool) {
		return tool.getRealValForAttribute(ToolFactory.attributeX);
	}
	
	public static double getY(ObjectInstance tool) {
		return tool.getRealValForAttribute(ToolFactory.attributeY);
	}
	
	public static double getZ(ObjectInstance tool) {
		return tool.getRealValForAttribute(ToolFactory.attributeZ);
	}
	
	public static void setX(ObjectInstance tool, double x) {
		tool.setValue(ToolFactory.attributeX, x);
	}
	
	public static void setY(ObjectInstance tool, double y) {
		tool.setValue(ToolFactory.attributeY, y);
	}
	
	public static void setZ(ObjectInstance tool, double z) {
		tool.setValue(ToolFactory.attributeZ, z);
	}
	
	public static void setUsed(ObjectInstance tool) {
		tool.setValue(ToolFactory.attributeUsed, true);
	}
	
	public static boolean getUsed(ObjectInstance tool) {
		return tool.getBooleanValue(ToolFactory.attributeUsed);
	}
	
	public static void setType(ObjectInstance tool, String type) {
		tool.setValue(ToolFactory.attributeType, type);
	}
	
	public static String getType(ObjectInstance tool) {
		Set<String> type = tool.getAllRelationalTargets(ToolFactory.attributeType);
		if (type != null && !type.isEmpty()) {
			return type.iterator().next();
		}
		return null;
	}
	
	public static String determineType(String name) {
		if (name.equals(ToolFactory.WHISK)) {
			return ToolFactory.whiskType;
		}
		return ToolFactory.spatulaType;
	}
}
