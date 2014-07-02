package edu.brown.cs.h2r.baking.ObjectFactories;
import java.util.List;
import java.util.Set;

import edu.brown.cs.h2r.baking.Recipes.Recipe;
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
		setAttributes(newInstance, attributes);
		newInstance.setValue(SpaceFactory.attributeOnOff, 0);
		if (agent == null) {
			agent = "";
		}
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
		return SpaceFactory.getNewObjectInstance(spaceClass, name, SpaceFactory.HEATING|SpaceFactory.SWITCHABLE,
				containers, agent);
	}
	
	public static ObjectInstance getNewHeatingSpaceObjectInstance(Domain domain, 
			String name, List<String> containers, String agent) {
		return SpaceFactory.getNewObjectInstance(
				domain.getObjectClass(SpaceFactory.ClassName), name, SpaceFactory.HEATING|SpaceFactory.SWITCHABLE,
				containers, agent);
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
	
	public static void addContainer(ObjectInstance space, ObjectInstance container) {
		space.addRelationalTarget(SpaceFactory.attributeContains, container.getName());
	}
	
	public static void removeContainer(ObjectInstance space, ObjectInstance container) {
		space.removeRelationalTarget(SpaceFactory.attributeContains, container.getName());
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
	
	public static void setOnOff(ObjectInstance objectInstance, boolean isOn) {
		objectInstance.setValue(SpaceFactory.attributeOnOff, isOn ? 1 : 0);
	}
	
	public static void setAttributes(ObjectInstance object, int attributes) {
		object.setValue(SpaceFactory.attributeBaking, ((attributes & SpaceFactory.BAKING) == SpaceFactory.BAKING) ? 1: 0);
		object.setValue(SpaceFactory.attributeHeating, ((attributes & SpaceFactory.HEATING) == SpaceFactory.HEATING) ? 1: 0);
		object.setValue(SpaceFactory.attributeWorking, ((attributes & SpaceFactory.WORKING) == SpaceFactory.WORKING) ? 1: 0);
		object.setValue(SpaceFactory.attributeSwitchable, ((attributes & SpaceFactory.SWITCHABLE) == SpaceFactory.SWITCHABLE) ? 1: 0);
	}
	
	public static int generateAttributeNumber(Boolean baking, Boolean heating, Boolean working, Boolean switchable) {
		int baking_int = baking ? SpaceFactory.BAKING : 0;
		int heating_int = heating ? SpaceFactory.HEATING : 0;
		int working_int = working ? SpaceFactory.WORKING : 0;
		int switchable_int = switchable ? SpaceFactory.SWITCHABLE : 0;
		return baking_int|heating_int|working_int|switchable_int;
	}
}
