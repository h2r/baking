package edu.brown.cs.h2r.baking.ObjectFactories;
import java.util.List;
import java.util.Set;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;


public class SpaceFactory {

	public static final String ClassName = "space";
	private static final String attributeWorking = "working";
	private static final String attributeContains = "contains";
	private static final String attributeAgent = "agent";
	
	private static final String	attributeTop = "top";
	private static final String	attributeBottom = "bottom";
	private static final String	attributeLeft = "left";
	private static final String attributeRight = "right";

	public static final String SPACE_COUNTER = "counter";
	
	public static final String SPACE_HUMAN = "humanCounter";
	public static final String SPACE_ROBOT = "robotCounter";
	public static final int NO_ATTRIBUTES= 0 ;
	public static final int WORKING = 1;
	
	public static ObjectClass createObjectClass(Domain domain)
	{
		ObjectClass objectClass = domain.getObjectClass(SpaceFactory.ClassName);
		if (objectClass == null) {
			objectClass = new ObjectClass(domain, SpaceFactory.ClassName);
			
			Attribute receivingAttribute =
					new Attribute(domain, SpaceFactory.attributeWorking, Attribute.AttributeType.BOOLEAN);
			objectClass.addAttribute(receivingAttribute);
			
			objectClass.addAttribute(
					new Attribute(domain, SpaceFactory.attributeContains, 
							Attribute.AttributeType.MULTITARGETRELATIONAL));
			
			objectClass.addAttribute(
					new Attribute(domain, SpaceFactory.attributeAgent, 
							Attribute.AttributeType.RELATIONAL));
			
			objectClass.addAttribute (
					new Attribute(domain, SpaceFactory.attributeTop, 
							Attribute.AttributeType.REALUNBOUND));
			
			objectClass.addAttribute (
					new Attribute(domain, SpaceFactory.attributeBottom, 
							Attribute.AttributeType.REALUNBOUND));
			
			objectClass.addAttribute (
					new Attribute(domain, SpaceFactory.attributeLeft, 
							Attribute.AttributeType.REALUNBOUND));
			
			objectClass.addAttribute (
					new Attribute(domain, SpaceFactory.attributeRight, 
							Attribute.AttributeType.REALUNBOUND));
		}
		return objectClass;
	}
	
	public static ObjectInstance getNewObjectInstance(ObjectClass spaceClass, String name, 
			int attributes, List<String> containers, String agent) {
		ObjectInstance newInstance = new ObjectInstance(spaceClass, name);
		setAttributes(newInstance, attributes);
		if (agent == null) {
			agent = "";
		}
		newInstance.addRelationalTarget(SpaceFactory.attributeAgent, agent);
		
		//default values so planner doesn't get mad!
		newInstance.setValue(SpaceFactory.attributeTop, 0);
		newInstance.setValue(SpaceFactory.attributeBottom, 0);
		newInstance.setValue(SpaceFactory.attributeLeft, 0);
		newInstance.setValue(SpaceFactory.attributeRight, 0);

		if (containers != null)
		{
			for (String container : containers)
			{
				newInstance.addRelationalTarget(SpaceFactory.attributeContains, container);
			}
		}
		return newInstance;
	}
	
	public static ObjectInstance getNewObjectInstance(ObjectClass spaceClass, String name, 
			int attributes, List<String> containers, String agent, double top, double bottom,
			double left, double right) {
		ObjectInstance newInstance = new ObjectInstance(spaceClass, name);
		setAttributes(newInstance, attributes);
		if (agent == null) {
			agent = "";
		}
		newInstance.addRelationalTarget(SpaceFactory.attributeAgent, agent);
		newInstance.setValue(SpaceFactory.attributeTop, top);
		newInstance.setValue(SpaceFactory.attributeBottom, bottom);
		newInstance.setValue(SpaceFactory.attributeLeft, left);
		newInstance.setValue(SpaceFactory.attributeRight, right);

		if (containers != null)
		{
			for (String container : containers)
			{
				newInstance.addRelationalTarget(SpaceFactory.attributeContains, container);
			}
		}
		return newInstance;
	}
	
	public static ObjectInstance getNewObjectInstance(Domain domain, String name,int attributes, List<String> containers, String agent) {
		return SpaceFactory.getNewObjectInstance(
				domain.getObjectClass(SpaceFactory.ClassName), name, attributes,containers, agent);
	}
	
	public static ObjectInstance getNewObjectInstance(Domain domain, String name,int attributes, List<String> containers, String agent,
			double top, double bottom, double left, double right) {
		return SpaceFactory.getNewObjectInstance(
				domain.getObjectClass(SpaceFactory.ClassName), name, attributes,containers, agent,
				top, bottom, left, right);
	}
	
	public static ObjectInstance getNewWorkingSpaceObjectInstance(ObjectClass spaceClass, 
			String name, List<String> containers, String agent) {
		return SpaceFactory.getNewObjectInstance(spaceClass, name, SpaceFactory.WORKING, containers, agent);
	}
	
	public static ObjectInstance getNewWorkingSpaceObjectInstance(ObjectClass spaceClass, 
			String name, List<String> containers, String agent, double top, double bottom,
			double left, double right) {
		return SpaceFactory.getNewObjectInstance(spaceClass, name, SpaceFactory.WORKING, containers, agent,
				top, bottom, left, right);
	}
	
	public static ObjectInstance getNewWorkingSpaceObjectInstance(Domain domain, 
			String name, List<String> containers, String agent, double top, double bottom,
			double left, double right) {
		return SpaceFactory.getNewObjectInstance(
				SpaceFactory.createObjectClass(domain), name, SpaceFactory.WORKING, containers, agent, top,
				bottom, left, right);
	}
	
	public static ObjectInstance getNewWorkingSpaceObjectInstance(Domain domain, 
			String name, List<String> containers, String agent) {
		return SpaceFactory.getNewObjectInstance(
				SpaceFactory.createObjectClass(domain), name, SpaceFactory.WORKING, containers, agent);
	}
	
	public static void addContainer(ObjectInstance space, ObjectInstance container) {
		space.addRelationalTarget(SpaceFactory.attributeContains, container.getName());
	}
	
	public static void removeContainer(ObjectInstance space, ObjectInstance container) {
		space.removeRelationalTarget(SpaceFactory.attributeContains, container.getName());
	}
	
	public static Boolean isWorking(ObjectInstance objectInstance) {
		return (objectInstance.getDiscValForAttribute(SpaceFactory.attributeWorking) == 1);
	}
	
	public static Set<String> getContents(ObjectInstance objectInstance) {
		return (objectInstance.getAllRelationalTargets(SpaceFactory.attributeContains));
	}
	
	public static Set<String> getAgent(ObjectInstance objectInstance) {
		return (objectInstance.getAllRelationalTargets(SpaceFactory.attributeAgent));
	}
	
	public static void setAttributes(ObjectInstance object, int attributes) {
		object.setValue(SpaceFactory.attributeWorking, ((attributes & SpaceFactory.WORKING) == SpaceFactory.WORKING) ? 1: 0);
	}
	
	public static int generateAttributeNumber(Boolean baking, Boolean heating, Boolean working, Boolean switchable) {
		int workingInt = working ? SpaceFactory.WORKING : 0;
		return workingInt;
	}
	
	public static double getTop(ObjectInstance space) {
		return space.getRealValForAttribute(SpaceFactory.attributeTop);
	}
	
	public static double getBottom(ObjectInstance space) {
		return space.getRealValForAttribute(SpaceFactory.attributeBottom);
	}
	
	public static double getLeft(ObjectInstance space) {
		return space.getRealValForAttribute(SpaceFactory.attributeLeft);
	}
	
	public static double getRight(ObjectInstance space) {
		return space.getRealValForAttribute(SpaceFactory.attributeRight);
	}
	
	public static boolean containerInSpace(ObjectInstance space, ObjectInstance container) {
		double left = SpaceFactory.getLeft(space);
		double right = SpaceFactory.getRight(space);
		double x = ContainerFactory.getX(container);
		
		if (!(x >= left) || !(x <= right)) {
			return false;
		}
		return true;
		// Since y should be whole length of the table, y shouldn't matter (?)
		/*double top = SpaceFactory.getTop(space);
		double bottom = SpaceFactory.getBottom(space);
		double y = ContainerFactory.getY(container);
		
		if ()*/
	}
}
