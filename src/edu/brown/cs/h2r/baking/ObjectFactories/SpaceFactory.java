package edu.brown.cs.h2r.baking.ObjectFactories;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;


public class SpaceFactory {

	public static final String ClassName = "space";
	private static final String attributeOnOff = "onoff";
	private static final String attributeSwitchable = "switchable";
	private static final String attributeBaking = "baking";
	private static final String attributeHeating = "heating";
	private static final String attributeWorking = "working";
	private static final String attributeContains = "contains";
	private static final String attributeAgent = "agent";
	
	public static final String SPACE_OVEN = "oven";
	public static final String SPACE_STOVE = "stove";
	public static final String SPACE_COUNTER = "counter";
	public static final int NO_ATTRIBUTES= 0 ;
	public static final int BAKING = 1;
	public static final int HEATING = 2;
	public static final int WORKING = 4;
	public static final int SWITCHABLE = 8;
	
	public static ObjectClass createObjectClass(Domain domain)
	{
		ObjectClass objectClass = domain.getObjectClass(SpaceFactory.ClassName);
		if (objectClass == null) {
			objectClass = new ObjectClass(domain, SpaceFactory.ClassName);
			Attribute mixingAttribute = 
					new Attribute(domain, SpaceFactory.attributeBaking, Attribute.AttributeType.BOOLEAN);
			objectClass.addAttribute(mixingAttribute);
			
			Attribute heatingAttribute = 
					new Attribute(domain, SpaceFactory.attributeHeating, Attribute.AttributeType.BOOLEAN);
			objectClass.addAttribute(heatingAttribute);
			
			Attribute receivingAttribute =
					new Attribute(domain, SpaceFactory.attributeWorking, Attribute.AttributeType.BOOLEAN);
			objectClass.addAttribute(receivingAttribute);
			
			Attribute switchableAttribute =
					new Attribute(domain, SpaceFactory.attributeSwitchable, Attribute.AttributeType.BOOLEAN);
			objectClass.addAttribute(switchableAttribute);
			
			Attribute onoffAttribute =
					new Attribute(domain, SpaceFactory.attributeOnOff, Attribute.AttributeType.BOOLEAN);
			objectClass.addAttribute(onoffAttribute);
			
			objectClass.addAttribute(
					new Attribute(domain, SpaceFactory.attributeContains, 
							Attribute.AttributeType.MULTITARGETRELATIONAL));
			
			objectClass.addAttribute(
					new Attribute(domain, SpaceFactory.attributeAgent, 
							Attribute.AttributeType.RELATIONAL));
		}
		return objectClass;
	}
	
	public static ObjectInstance getNewObjectInstance(ObjectClass spaceClass, String name, 
			int attributes, List<String> containers, String agent) {
		ObjectInstance newInstance = new ObjectInstance(spaceClass, name);
		newInstance = changeAttributes(newInstance, attributes);
		newInstance = newInstance.changeValue(SpaceFactory.attributeOnOff, 0);
		if (agent == null) {
			agent = "";
		}
		newInstance = newInstance.appendRelationalTarget(SpaceFactory.attributeAgent, agent);
		containers = (containers == null) ? new ArrayList<String>() : containers;
		newInstance = newInstance.appendAllRelationTargets(SpaceFactory.attributeContains, containers);
		return newInstance;
	}
	
	public static ObjectInstance getNewObjectInstance(Domain domain, String name,int attributes, List<String> containers, String agent) {
		return SpaceFactory.getNewObjectInstance(
				domain.getObjectClass(SpaceFactory.ClassName), name, attributes,containers, agent);
	}
	
	
	
	public static ObjectInstance getNewWorkingSpaceObjectInstance(ObjectClass spaceClass, 
			String name, List<String> containers, String agent) {
		return SpaceFactory.getNewObjectInstance(spaceClass, name, SpaceFactory.WORKING, containers, agent);
	}
	
	public static ObjectInstance getNewWorkingSpaceObjectInstance(Domain domain, 
			String name, List<String> containers, String agent) {
		return SpaceFactory.getNewObjectInstance(
				SpaceFactory.createObjectClass(domain), name, SpaceFactory.WORKING, containers, agent);
	}
	
	public static ObjectInstance getNewHeatingSpaceObjectInstance(ObjectClass spaceClass, 
			String name, List<String> containers, String agent) {
		return SpaceFactory.getNewObjectInstance(spaceClass, name, SpaceFactory.HEATING | SpaceFactory.SWITCHABLE, containers, agent);
	}
	
	public static ObjectInstance getNewHeatingSpaceObjectInstance(Domain domain, 
			String name, List<String> containers, String agent) {
		return SpaceFactory.getNewObjectInstance(
				domain.getObjectClass(SpaceFactory.ClassName), name, SpaceFactory.HEATING | SpaceFactory.SWITCHABLE, containers, agent);
	}
	
	public static ObjectInstance getNewBakingSpaceObjectInstance(ObjectClass spaceClass, 
			String name, List<String> containers, String agent) {
		return SpaceFactory.getNewObjectInstance(spaceClass, name, SpaceFactory.BAKING|SpaceFactory.SWITCHABLE, 
				containers, agent);
	}

	public static ObjectInstance getNewBakingSpaceObjectInstance(Domain domain, 
			String name, List<String> containers, String agent) {
		return SpaceFactory.getNewObjectInstance(
				domain.getObjectClass(SpaceFactory.ClassName), name, SpaceFactory.BAKING|SpaceFactory.SWITCHABLE, 
				containers, agent);
	}
	
	public static ObjectInstance addContainer(ObjectInstance space, ObjectInstance container) {
		return space.appendRelationalTarget(SpaceFactory.attributeContains, container.getName());
	}
	
	public static ObjectInstance addAllContainers(ObjectInstance space, Collection<ObjectInstance> containers) {
		List<String> containerNames = new ArrayList<String>(containers.size());
		for (ObjectInstance container : containers) {
			containerNames.add(container.getName());
		}
		return space.appendAllRelationTargets(SpaceFactory.attributeContains, containerNames);
	}
	
	public static ObjectInstance removeContainer(ObjectInstance space, ObjectInstance container) {
		return space.replaceRelationalTarget(SpaceFactory.attributeContains, container.getName());
	}

	public static Boolean isBaking(ObjectInstance objectInstance) {
		return (objectInstance.getDiscValForAttribute(SpaceFactory.attributeBaking) == 1);
	}
	
	public static Boolean isHeating(ObjectInstance objectInstance) {
		return (objectInstance.getDiscValForAttribute(SpaceFactory.attributeHeating)== 1);
	}
	
	public static Boolean isWorking(ObjectInstance objectInstance) {
		return (objectInstance.getDiscValForAttribute(SpaceFactory.attributeWorking) == 1);
	}
	
	public static Boolean isSwitchable(ObjectInstance objectInstance) {
		return (objectInstance.getDiscValForAttribute(SpaceFactory.attributeSwitchable) == 1);
	}
	
	public static Set<String> getContents(ObjectInstance objectInstance) {
		return (objectInstance.getAllRelationalTargets(SpaceFactory.attributeContains));
	}
	
	public static Set<String> getAgent(ObjectInstance objectInstance) {
		return (objectInstance.getAllRelationalTargets(SpaceFactory.attributeAgent));
	}
	
	public static Boolean getOnOff(ObjectInstance objectInstance) {
		return (objectInstance.getDiscValForAttribute(SpaceFactory.attributeOnOff) == 1);
	}
	
	public static ObjectInstance setOnOff(ObjectInstance objectInstance, boolean isOn) {
		return objectInstance.changeValue(SpaceFactory.attributeOnOff, isOn ? 1 : 0);
	}
	
	public static ObjectInstance changeAttributes(ObjectInstance object, int attributes) {
		object = object.changeValue(SpaceFactory.attributeBaking, ((attributes & SpaceFactory.BAKING) == SpaceFactory.BAKING) ? 1: 0);
		object = object.changeValue(SpaceFactory.attributeHeating, ((attributes & SpaceFactory.HEATING) == SpaceFactory.HEATING) ? 1: 0);
		object = object.changeValue(SpaceFactory.attributeWorking, ((attributes & SpaceFactory.WORKING) == SpaceFactory.WORKING) ? 1: 0);
		object = object.changeValue(SpaceFactory.attributeSwitchable, ((attributes & SpaceFactory.SWITCHABLE) == SpaceFactory.SWITCHABLE) ? 1: 0);
		return object;
	}
	
	public static int generateAttributeNumber(Boolean baking, Boolean heating, Boolean working, Boolean switchable) {
		int bakingInt = baking ? SpaceFactory.BAKING : 0;
		int heatingInt = heating ? SpaceFactory.HEATING : 0;
		int workingInt = working ? SpaceFactory.WORKING : 0;
		int switchableInt = switchable ? SpaceFactory.SWITCHABLE : 0;
		return bakingInt|heatingInt|workingInt|switchableInt;
	}
	
	public static boolean spaceWillHeat(ObjectInstance space) {
		return SpaceFactory.isHeating(space) && SpaceFactory.getOnOff(space);
	}
}
