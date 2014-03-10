package edu.brown.cs.h2r.baking;
import java.util.List;
import java.util.Set;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;


public class SpaceFactory {

	public static final String ClassName = "space";
	private static final String attributeBaking = "baking";
	private static final String attributeHeating = "heating";
	private static final String attributeWorking = "working";
	private static final String attributeContains = "contains";
	private static final String attributeAgent = "agent";

	
	public static ObjectClass createObjectClass(Domain domain)
	{
		ObjectClass objectClass = domain.getObjectClass(SpaceFactory.ClassName);
		if (objectClass == null) {
			objectClass = new ObjectClass(domain, SpaceFactory.ClassName);
			Attribute mixingAttribute = 
					new Attribute(domain, SpaceFactory.attributeBaking, Attribute.AttributeType.DISC);
			mixingAttribute.setDiscValuesForRange(0,1,1);
			objectClass.addAttribute(mixingAttribute);
			
			Attribute heatingAttribute = 
					new Attribute(domain, SpaceFactory.attributeHeating, Attribute.AttributeType.DISC);
			heatingAttribute.setDiscValuesForRange(0,1,1);
			objectClass.addAttribute(heatingAttribute);
			
			Attribute receivingAttribute =
					new Attribute(domain, SpaceFactory.attributeWorking, Attribute.AttributeType.DISC);
			receivingAttribute.setDiscValuesForRange(0,1,1);
			objectClass.addAttribute(receivingAttribute);
			
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
			Boolean baking, Boolean heating, Boolean working, List<String> containers, String agent) {
		ObjectInstance newInstance = new ObjectInstance(spaceClass, name);
		newInstance.setValue(SpaceFactory.attributeBaking, baking ? 1 : 0);
		newInstance.setValue(SpaceFactory.attributeHeating, heating ? 1 : 0);
		newInstance.setValue(SpaceFactory.attributeWorking, working ? 1 : 0);
		newInstance.addRelationalTarget(SpaceFactory.attributeAgent, agent);
		if (containers != null)
		{
			for (String container : containers)
			{
				newInstance.addRelationalTarget(SpaceFactory.attributeContains, container);
			}
		}
		return newInstance;
	}
	
	public static ObjectInstance getNewObjectInstance(Domain domain, String name, Boolean baking, 
			Boolean heating, Boolean working, List<String> containers, String agent) {
		return SpaceFactory.getNewObjectInstance(
				domain.getObjectClass(SpaceFactory.ClassName), name, baking, heating, working, containers, agent);
	}
	
	public static ObjectInstance getNewWorkingSpaceObjectInstance(ObjectClass spaceClass, 
			String name, List<String> containers, String agent) {
		return SpaceFactory.getNewObjectInstance(spaceClass, name, false, false, true, containers, agent);
	}
	
	public static ObjectInstance getNewWorkingSpaceObjectInstance(Domain domain, 
			String name, List<String> containers, String agent) {
		return SpaceFactory.getNewObjectInstance(
				SpaceFactory.createObjectClass(domain), name, false, false, true, containers, agent);
	}
	
	public static ObjectInstance getNewHeatingSpaceObjectInstance(ObjectClass spaceClass, 
			String name, List<String> containers, String agent) {
		return SpaceFactory.getNewObjectInstance(spaceClass, name, false, true, false, containers, agent);
	}
	
	public static ObjectInstance getNewHeatingSpaceObjectInstance(Domain domain, 
			String name, List<String> containers, String agent) {
		return SpaceFactory.getNewObjectInstance(
				domain.getObjectClass(SpaceFactory.ClassName), name, false, true, false, containers, agent);
	}
	
	public static ObjectInstance getNewBakingSpaceObjectInstance(ObjectClass spaceClass, 
			String name, List<String> containers, String agent) {
		return SpaceFactory.getNewObjectInstance(spaceClass, name, true, false, false, containers, agent);
	}

	public static ObjectInstance getNewBakingSpaceObjectInstance(Domain domain, 
			String name, List<String> containers, String agent) {
		return SpaceFactory.getNewObjectInstance(
				domain.getObjectClass(SpaceFactory.ClassName), name, true, false, false, containers, agent);
	}
	
	public static void addContainer(ObjectInstance space, ObjectInstance container) {
		space.addRelationalTarget(SpaceFactory.attributeContains, container.getName());
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
	
	public static Set<String> getContents(ObjectInstance objectInstance) {
		return (objectInstance.getAllRelationalTargets(SpaceFactory.attributeContains));
	}
	
	public static Set<String> getAgent(ObjectInstance objectInstance) {
		return (objectInstance.getAllRelationalTargets(SpaceFactory.attributeAgent));
	}
}
