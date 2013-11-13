import java.util.Arrays;
import java.util.List;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;


public class ContainerFactory {
	
	private final Domain domain;
	private final ObjectClass containerClass;
	private static final String attributeMixing = "mixing";
	private static final String attributeHeating = "heating";
	private static final String attributeReceiving = "receiving";
	private static final String attributeContains = "contains";
	private static final String attributeInSpace = "in_space";

	public ContainerFactory(Domain domain)
	{
		this.domain = domain;
		this.containerClass = this.createObjectClass();
	}
	
	public static ObjectClass createObjectClass(Domain domain)
	{
		ObjectClass objectClass = new ObjectClass(domain, "container");
		Attribute mixingAttribute = 
				new Attribute(domain, ContainerFactory.attributeMixing, Attribute.AttributeType.DISC);
		mixingAttribute.setDiscValuesForRange(0,1,1);
		objectClass.addAttribute(mixingAttribute);
		
		Attribute heatingAttribute = 
				new Attribute(domain, ContainerFactory.attributeHeating, Attribute.AttributeType.DISC);
		heatingAttribute.setDiscValuesForRange(0,1,1);
		objectClass.addAttribute(heatingAttribute);
		
		Attribute receivingAttribute =
				new Attribute(domain, ContainerFactory.attributeReceiving, Attribute.AttributeType.DISC);
		receivingAttribute.setDiscValuesForRange(0,1,1);
		objectClass.addAttribute(receivingAttribute);
		
		objectClass.addAttribute(
				new Attribute(domain, ContainerFactory.attributeContains, 
						Attribute.AttributeType.MULTITARGETRELATIONAL));
		
		objectClass.addAttribute(
				new Attribute(domain, ContainerFactory.attributeInSpace,
						Attribute.AttributeType.RELATIONAL));
		return objectClass;
	}
	
	public static ObjectInstance getNewObjectInstance(ObjectClass containerClass, String name, 
			Boolean mixing, Boolean heating, Boolean receiving, List<String> contents, String containerSpace) {
		ObjectInstance newInstance = new ObjectInstance(containerClass, name);
		newInstance.setValue(ContainerFactory.attributeMixing, mixing ? 1 : 0);
		newInstance.setValue(ContainerFactory.attributeHeating, heating ? 1 : 0);
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
			newInstance.addRelationalTarget(ContainerFactory.attributeInSpace, containerSpace);
		}
		return newInstance;
	}
	
	public static ObjectInstance getNewMixingContainerObjectInstance(ObjectClass containerClass, 
			String name, List<String> contents, String containerSpace) {
		return ContainerFactory.getNewObjectInstance(containerClass, name, true, false, true, contents, containerSpace);
	}
	
	public static ObjectInstance getNewHeatingContainerObjectInstance(ObjectClass containerClass, 
			String name, List<String> contents, String containerSpace) {
		return ContainerFactory.getNewObjectInstance(containerClass, name, true, true, true, contents, containerSpace);
	}
	
	public static ObjectInstance getNewIngredientContainerObjectInstance(ObjectClass containerClass, 
			String name, String ingredient, String containerSpace) {
		return ContainerFactory.getNewObjectInstance(containerClass, name, false, false, false, Arrays.asList(ingredient), containerSpace);
	}

	public static void addIngredient(ObjectInstance container, ObjectInstance ingredient) {
		container.addRelationalTarget(ContainerFactory.attributeContains, ingredient.getName());
	}
	
	public static void addIngredientList(ObjectInstance container, List<ObjectInstance> ingredientList) {
		if (ingredientList != null) {
			for (ObjectInstance objectInstance : ingredientList) {
				ContainerFactory.addIngredient(container, objectInstance);
			}
		}
	}
	
	public static void changeContainerSpace(ObjectInstance container, String containerSpace) {
		if (containerSpace != null) {
			container.addRelationalTarget(ContainerFactory.attributeInSpace, containerSpace);
		}
	}
}
