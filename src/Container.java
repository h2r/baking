import java.util.Arrays;
import java.util.List;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;


public class Container {
	
	private final Domain domain;
	private final ObjectClass containerClass;
	private static final String attributeMixing = "mixing";
	private static final String attributeHeating = "heating";
	private static final String attributeReceiving = "receiving";
	private static final String attributeContains = "contains";
	private static final String attributeInSpace = "in_space";

	public Container(Domain domain)
	{
		this.domain = domain;
		this.containerClass = this.createObjectClass();
	}
	
	private ObjectClass createObjectClass()
	{
		ObjectClass objectClass = new ObjectClass(this.domain, "container");
		Attribute mixingAttribute = 
				new Attribute(this.domain, Container.attributeMixing, Attribute.AttributeType.DISC);
		mixingAttribute.setDiscValuesForRange(0,1,1);
		objectClass.addAttribute(mixingAttribute);
		
		Attribute heatingAttribute = 
				new Attribute(this.domain, Container.attributeHeating, Attribute.AttributeType.DISC);
		heatingAttribute.setDiscValuesForRange(0,1,1);
		objectClass.addAttribute(heatingAttribute);
		
		Attribute receivingAttribute =
				new Attribute(this.domain, Container.attributeReceiving, Attribute.AttributeType.DISC);
		receivingAttribute.setDiscValuesForRange(0,1,1);
		objectClass.addAttribute(receivingAttribute);
		
		objectClass.addAttribute(
				new Attribute(this.domain, Container.attributeContains, 
						Attribute.AttributeType.MULTITARGETRELATIONAL));
		
		objectClass.addAttribute(
				new Attribute(this.domain, Container.attributeInSpace,
						Attribute.AttributeType.RELATIONAL));
		return objectClass;
	}
	
	public ObjectClass getObjectClass() {
		return this.containerClass;
	}
	
	public ObjectInstance getNewObjectInstance(String name, 
			Boolean mixing, Boolean heating, Boolean receiving, List<String> contents, String containerSpace) {
		ObjectInstance newInstance = new ObjectInstance(this.containerClass, name);
		newInstance.setValue(Container.attributeMixing, mixing ? 1 : 0);
		newInstance.setValue(Container.attributeHeating, heating ? 1 : 0);
		newInstance.setValue(Container.attributeReceiving, receiving ? 1 : 0);
		if (contents != null)
		{
			for (String ingredient : contents)
			{
				newInstance.addRelationalTarget(Container.attributeContains, ingredient);
			}
		}
		if (containerSpace != null || containerSpace != "")
		{
			newInstance.addRelationalTarget(Container.attributeInSpace, containerSpace);
		}
		return newInstance;
	}
	
	public ObjectInstance getNewMixingContainerObjectInstance(String name, List<String> contents, String containerSpace) {
		return this.getNewObjectInstance(name, true, false, true, contents, containerSpace);
	}
	
	public ObjectInstance getNewHeatingContainerObjectInstance(String name, List<String> contents, String containerSpace) {
		return this.getNewObjectInstance(name, true, true, true, contents, containerSpace);
	}
	
	public ObjectInstance getNewIngredientContainerObjectInstance(String name, String ingredient, String containerSpace) {
		return this.getNewObjectInstance(name, false, false, false, Arrays.asList(ingredient), containerSpace);
	}

	public static void addIngredient(ObjectInstance container, ObjectInstance ingredient) {
		container.addRelationalTarget(Container.attributeContains, ingredient.getName());
	}
	
	public static void addIngredientList(ObjectInstance container, List<ObjectInstance> ingredientList) {
		if (ingredientList != null) {
			for (ObjectInstance objectInstance : ingredientList) {
				Container.addIngredient(container, objectInstance);
			}
		}
	}
	
	public static void changeContainerSpace(ObjectInstance container, String containerSpace) {
		if (containerSpace != null) {
			container.addRelationalTarget(Container.attributeInSpace, containerSpace);
		}
	}
}
