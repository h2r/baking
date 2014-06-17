package edu.brown.cs.h2r.baking.ObjectFactories;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;


public class ContainerFactory {
	
	public static final String ClassName = "container";
	private static final String attributeBaking = "baking";
	private static final String attributeMixing = "mixing";
	private static final String attributeHeating = "heating";
	private static final String attributeReceiving = "receiving";
	private static final String attributeContains = "contains";
	private static final String attributeSpace = "space";
	
	public static ObjectClass createObjectClass(Domain domain)
	{
		ObjectClass objectClass = new ObjectClass(domain, ContainerFactory.ClassName);
		Attribute mixingAttribute = 
				new Attribute(domain, ContainerFactory.attributeMixing, Attribute.AttributeType.DISC);
		mixingAttribute.setDiscValuesForRange(0,1,1);
		objectClass.addAttribute(mixingAttribute);
		
		Attribute heatingAttribute = 
				new Attribute(domain, ContainerFactory.attributeHeating, Attribute.AttributeType.DISC);
		heatingAttribute.setDiscValuesForRange(0,1,1);
		objectClass.addAttribute(heatingAttribute);
		
		Attribute bakingAttribute =
				new Attribute(domain, ContainerFactory.attributeBaking, Attribute.AttributeType.DISC);
		bakingAttribute.setDiscValuesForRange(0,1,1);
		objectClass.addAttribute(bakingAttribute);
		
		Attribute receivingAttribute =
				new Attribute(domain, ContainerFactory.attributeReceiving, Attribute.AttributeType.DISC);
		receivingAttribute.setDiscValuesForRange(0,1,1);
		objectClass.addAttribute(receivingAttribute);
		
		objectClass.addAttribute(
				new Attribute(domain, ContainerFactory.attributeContains, 
						Attribute.AttributeType.MULTITARGETRELATIONAL));
		
		objectClass.addAttribute(
				new Attribute(domain, ContainerFactory.attributeSpace,
						Attribute.AttributeType.RELATIONAL));
		return objectClass;
	}
	
	public static ObjectInstance getNewObjectInstance(ObjectClass containerClass, String name, 
			Boolean mixing, Boolean heating, Boolean baking, Boolean receiving, List<String> contents, String containerSpace) {
		ObjectInstance newInstance = new ObjectInstance(containerClass, name);
		newInstance.setValue(ContainerFactory.attributeMixing, mixing ? 1 : 0);
		newInstance.setValue(ContainerFactory.attributeHeating, heating ? 1 : 0);
		newInstance.setValue(ContainerFactory.attributeBaking, baking ? 1 : 0);
		newInstance.setValue(ContainerFactory.attributeReceiving, receiving ? 1 : 0);
		if (contents != null)
		{
			for (String ingredient : contents)
			{
				newInstance.addRelationalTarget(ContainerFactory.attributeContains, ingredient);
			}
		}
		if (containerSpace != null || containerSpace != "")
		{
			newInstance.addRelationalTarget(ContainerFactory.attributeSpace, containerSpace);
		}
		return newInstance;
	}
	
	public static ObjectInstance getNewObjectInstance(Domain domain, String name, Boolean mixing, Boolean heating, Boolean baking,
			Boolean receiving, List<String> contents, String containerSpace) {
		return ContainerFactory.getNewObjectInstance(
				domain.getObjectClass(ContainerFactory.ClassName), name, mixing, heating, baking, receiving, contents, containerSpace);
	}
	
	public static ObjectInstance getNewMixingContainerObjectInstance(ObjectClass containerClass, 
			String name, List<String> contents, String containerSpace) {
		return ContainerFactory.getNewObjectInstance(containerClass, name, true, false, false, true, contents, containerSpace);
	}
	
	public static ObjectInstance getNewMixingContainerObjectInstance(Domain domain, 
			String name, List<String> contents, String containerSpace) {
		return ContainerFactory.getNewObjectInstance(
				domain.getObjectClass(ContainerFactory.ClassName), name, true, false, false, true, contents, containerSpace);
	}
	
	public static ObjectInstance getNewHeatingContainerObjectInstance(ObjectClass containerClass, 
			String name, List<String> contents, String containerSpace) {
		return ContainerFactory.getNewObjectInstance(containerClass, name, true, true, false, true, contents, containerSpace);
	}
	
	public static ObjectInstance getNewHeatingContainerObjectInstance(Domain domain, 
			String name, List<String> contents, String containerSpace) {
		return ContainerFactory.getNewObjectInstance(domain.getObjectClass(ContainerFactory.ClassName), 
				name, true, true, false, true, contents, containerSpace);
	}
	
	public static ObjectInstance getNewIngredientContainerObjectInstance(ObjectClass containerClass, 
			String name, String ingredient, String containerSpace) {
		return ContainerFactory.getNewObjectInstance(containerClass, name, false, false, false, false, Arrays.asList(ingredient), containerSpace);
	}
	
	public static ObjectInstance getNewIngredientContainerObjectInstance(Domain domain, 
			String name, String ingredient, String containerSpace) {
		return ContainerFactory.getNewObjectInstance(domain.getObjectClass(ContainerFactory.ClassName), 
				name, false, false, false, false, Arrays.asList(ingredient), containerSpace);
	}

	public static void addIngredient(ObjectInstance container, String ingredient) {
		container.addRelationalTarget(ContainerFactory.attributeContains, ingredient);
	}
	
	public static void addIngredients(ObjectInstance container, Iterable<String> ingredients) {
		if (ingredients != null) {
			for (String ingredient : ingredients) {
				ContainerFactory.addIngredient(container, ingredient);
			}
		}
	}
	
	public static void removeContents(ObjectInstance container) {
		container.clearRelationalTargets(ContainerFactory.attributeContains);
	}
	
	public static void changeContainerSpace(ObjectInstance container, String containerSpace) {
		if (containerSpace != null) {
			container.addRelationalTarget(ContainerFactory.attributeSpace, containerSpace);
		}
	}
	
	public static Boolean isMixingContainer(ObjectInstance container) {
		return (container.getDiscValForAttribute(ContainerFactory.attributeMixing)== 1); 
	}
	
	public static Boolean isHeatingContainer(ObjectInstance container) {
		return (container.getDiscValForAttribute(ContainerFactory.attributeHeating)== 1); 
	}

	public static Boolean isBakingContainer(ObjectInstance container) {
		return (container.getDiscValForAttribute(ContainerFactory.attributeBaking)== 1); 
	}
	
	public static Boolean isReceivingContainer(ObjectInstance container) {
		int rec = container.getDiscValForAttribute(ContainerFactory.attributeReceiving);
		Boolean isReceiving = (rec == 1);
		return isReceiving;
	}

	public static Set<String> getContentNames(ObjectInstance container) {
		Set<String> names = container.getAllRelationalTargets(ContainerFactory.attributeContains);
		return new TreeSet<String>(names);
	}
	
	public static String getSpaceName(ObjectInstance container) {
		Set<String> spaces = container.getAllRelationalTargets(ContainerFactory.attributeSpace);
		if (spaces != null && !spaces.isEmpty()) {
			return spaces.iterator().next();
		}
		return null;
	}
	
	public static Boolean isEmptyContainer(ObjectInstance container) {
		return getContentNames(container).isEmpty();
	}
}
