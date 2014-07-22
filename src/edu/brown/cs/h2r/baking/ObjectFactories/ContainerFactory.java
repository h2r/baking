package edu.brown.cs.h2r.baking.ObjectFactories;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.HashSet;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;


public class ContainerFactory {
	
	public static final String ClassName = "container";
	private static final String attributeBaking = "baking";
	private static final String attributeMixing = "mixing";
	private static final String attributeReceiving = "receiving";
	private static final String attributeContains = "contains";
	private static final String attributeSpace = "space";
	
	private static final String attributeX = "attX";
	private static final String attributeY = "attY";
	private static final String attributeZ = "attZ";
	
	public static final int NO_ATTRIBUTES = 0;
	public static final int BAKING = 1;
	public static final int MIXING = 2;
	public static final int RECEIVING = 4;
	
	public static ObjectClass createObjectClass(Domain domain)
	{
		ObjectClass objectClass = new ObjectClass(domain, ContainerFactory.ClassName);
		Attribute mixingAttribute = 
				new Attribute(domain, ContainerFactory.attributeMixing, Attribute.AttributeType.BOOLEAN);
		objectClass.addAttribute(mixingAttribute);
		
		Attribute bakingAttribute =
				new Attribute(domain, ContainerFactory.attributeBaking, Attribute.AttributeType.BOOLEAN);
		objectClass.addAttribute(bakingAttribute);
		
		Attribute receivingAttribute =
				new Attribute(domain, ContainerFactory.attributeReceiving, Attribute.AttributeType.BOOLEAN);
		objectClass.addAttribute(receivingAttribute);
		
		objectClass.addAttribute(
				new Attribute(domain, ContainerFactory.attributeContains, 
						Attribute.AttributeType.MULTITARGETRELATIONAL));
		
		objectClass.addAttribute(
				new Attribute(domain, ContainerFactory.attributeSpace,
						Attribute.AttributeType.RELATIONAL));
		
		objectClass.addAttribute (
				new Attribute(domain, ContainerFactory.attributeX, 
						Attribute.AttributeType.REALUNBOUND));
		
		objectClass.addAttribute (
				new Attribute(domain, ContainerFactory.attributeY, 
						Attribute.AttributeType.REALUNBOUND));
		
		objectClass.addAttribute (
				new Attribute(domain, ContainerFactory.attributeZ, 
						Attribute.AttributeType.REALUNBOUND));
		return objectClass;
	}
	
	public static ObjectInstance getNewObjectInstance(ObjectClass containerClass, String name, 
			int attributes, List<String> contents, String containerSpace) {
		ObjectInstance newInstance = new ObjectInstance(containerClass, name);
		setAttributes(newInstance, attributes);
		
		newInstance.setValue(ContainerFactory.attributeX, 0);
		newInstance.setValue(ContainerFactory.attributeY, 0);
		newInstance.setValue(ContainerFactory.attributeZ, 0);
		
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
	
	public static ObjectInstance getNewObjectInstance(ObjectClass containerClass, String name, 
			int attributes, List<String> contents, String containerSpace, double x, double y, double z) {
		ObjectInstance newInstance = new ObjectInstance(containerClass, name);
		setAttributes(newInstance, attributes);
		
		newInstance.setValue(ContainerFactory.attributeX, x);
		newInstance.setValue(ContainerFactory.attributeY, y);
		newInstance.setValue(ContainerFactory.attributeZ, z);
		
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
				domain.getObjectClass(ContainerFactory.ClassName), name, generateAttributeNumber(baking, heating, mixing, 
						receiving), contents, containerSpace);
	}
	
	public static ObjectInstance getNewObjectInstance(Domain domain, String name, Boolean mixing, Boolean heating, Boolean baking,
			Boolean receiving, List<String> contents, String containerSpace, double x, double y, double z) {
		return ContainerFactory.getNewObjectInstance(
				domain.getObjectClass(ContainerFactory.ClassName), name, generateAttributeNumber(baking, heating, mixing, 
						receiving), contents, containerSpace, x, y, z);
	}
	
	public static ObjectInstance getNewMixingContainerObjectInstance(ObjectClass containerClass, 
			String name, List<String> contents, String containerSpace) {
		return ContainerFactory.getNewObjectInstance(containerClass, name, ContainerFactory.MIXING|
				ContainerFactory.RECEIVING, contents, containerSpace);
	}
	
	public static ObjectInstance getNewMixingContainerObjectInstance(Domain domain, 
			String name, List<String> contents, String containerSpace) {
		return ContainerFactory.getNewObjectInstance(
				domain.getObjectClass(ContainerFactory.ClassName), name, ContainerFactory.MIXING|
				ContainerFactory.RECEIVING, contents, containerSpace);
	}
	
	public static ObjectInstance getNewBakingContainerObjectInstance(ObjectClass containerClass, 
			String name, List<String> contents, String containerSpace) {
		return ContainerFactory.getNewObjectInstance(containerClass, name,
				ContainerFactory.BAKING|ContainerFactory.RECEIVING, contents, containerSpace);
	}
	
	public static ObjectInstance getNewBakingContainerObjectInstance(Domain domain, 
			String name, List<String> contents, String containerSpace) {
		return ContainerFactory.getNewObjectInstance(domain.getObjectClass(ContainerFactory.ClassName), 
				name, ContainerFactory.BAKING|ContainerFactory.RECEIVING, 
				contents, containerSpace);
	}
	
	public static ObjectInstance getNewIngredientContainerObjectInstance(ObjectClass containerClass, 
			String name, String ingredient, String containerSpace) {
		return ContainerFactory.getNewObjectInstance(containerClass, name, ContainerFactory.NO_ATTRIBUTES, Arrays.asList(ingredient), containerSpace);
	}
	
	public static ObjectInstance getNewIngredientContainerObjectInstance(Domain domain, 
			String name, String ingredient, String containerSpace) {
		return ContainerFactory.getNewObjectInstance(domain.getObjectClass(ContainerFactory.ClassName), 
				name, ContainerFactory.NO_ATTRIBUTES, Arrays.asList(ingredient), containerSpace);
	}
	
	// X, Y, Z copies
	public static ObjectInstance getNewMixingContainerObjectInstance(ObjectClass containerClass, 
			String name, List<String> contents, String containerSpace, double x, double y, double z) {
		return ContainerFactory.getNewObjectInstance(containerClass, name, ContainerFactory.MIXING|
				ContainerFactory.RECEIVING, contents, containerSpace, x, y,z);
	}
	
	public static ObjectInstance getNewMixingContainerObjectInstance(Domain domain, 
			String name, List<String> contents, String containerSpace, double x, double y, double z) {
		return ContainerFactory.getNewObjectInstance(
				domain.getObjectClass(ContainerFactory.ClassName), name, ContainerFactory.MIXING|
				ContainerFactory.RECEIVING, contents, containerSpace, x, y, z);
	}
	
	public static ObjectInstance getNewBakingContainerObjectInstance(ObjectClass containerClass, 
			String name, List<String> contents, String containerSpace, double x, double y, double z) {
		return ContainerFactory.getNewObjectInstance(containerClass, name,
				ContainerFactory.BAKING|ContainerFactory.RECEIVING, contents, containerSpace, x, y, z);
	}
	
	public static ObjectInstance getNewBakingContainerObjectInstance(Domain domain, 
			String name, List<String> contents, String containerSpace, double x, double y, double z) {
		return ContainerFactory.getNewObjectInstance(domain.getObjectClass(ContainerFactory.ClassName), 
				name, ContainerFactory.BAKING|ContainerFactory.RECEIVING, 
				contents, containerSpace, x, y, z);
	}
	
	public static ObjectInstance getNewIngredientContainerObjectInstance(ObjectClass containerClass, 
			String name, String ingredient, String containerSpace, double x, double y, double z) {
		return ContainerFactory.getNewObjectInstance(containerClass, name, ContainerFactory.NO_ATTRIBUTES,
				Arrays.asList(ingredient), containerSpace, x, y, z);
	}
	
	public static ObjectInstance getNewIngredientContainerObjectInstance(Domain domain, 
			String name, String ingredient, String containerSpace, double x, double y, double z) {
		return ContainerFactory.getNewObjectInstance(domain.getObjectClass(ContainerFactory.ClassName), 
				name, ContainerFactory.NO_ATTRIBUTES, Arrays.asList(ingredient), containerSpace, x, y, z);
	}
	
	// end copies

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
		return new HashSet<String>(names);
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
	
	public static Set<String> getConstituentContentNames(ObjectInstance container, State state) {
		Set<String> names = new HashSet<String>();
		Set<String> contents = container.getAllRelationalTargets(ContainerFactory.attributeContains);
		for (String name : contents) {
			ObjectInstance ing = state.getObject(name);
			if (IngredientFactory.isSimple(ing)) {
				names.add(name);
			} else {
				names.addAll(IngredientFactory.getRecursiveContentsForIngredient(state, ing));
			}
		}
		return names;
	}
	
	public static Set<String> getConstituentSwappedContentNames(ObjectInstance container, State state) {
		Set<String> ingredients = new HashSet<String>();
		Set<String> contents = ContainerFactory.getContentNames(container);
		for (String name : contents) {
			ObjectInstance ing = state.getObject(name);
			if (IngredientFactory.isSimple(ing) || IngredientFactory.isSwapped(ing)) {
				ingredients.add(name);
			} else {
				ingredients.addAll(IngredientFactory.getRecursiveContentsAndSwapped(state, state.getObject(name)));
			}
		}
		return ingredients;
	}
	
	public static void removeIngredient(ObjectInstance container, String name) {
		container.removeRelationalTarget(ContainerFactory.attributeContains, name);
	}
	
	public static void setAttributes(ObjectInstance object, int attributes) {
		object.setValue(ContainerFactory.attributeBaking, ((attributes & ContainerFactory.BAKING) == ContainerFactory.BAKING) ? 1: 0);
		object.setValue(ContainerFactory.attributeMixing, ((attributes & ContainerFactory.MIXING) == ContainerFactory.MIXING) ? 1: 0);
		object.setValue(ContainerFactory.attributeReceiving, ((attributes & ContainerFactory.RECEIVING) == ContainerFactory.RECEIVING) ? 1: 0);
	}
	
	public static int generateAttributeNumber(Boolean baking, Boolean mixing, Boolean heating, Boolean receiving) {
		int baking_int = baking ? ContainerFactory.BAKING : 0;
		int mixing_int = mixing ? ContainerFactory.MIXING : 0;
		int receiving_int = receiving ? ContainerFactory.RECEIVING: 0;
		return baking_int|mixing_int|receiving_int;
	}
	
	public static boolean hasABakedContent(State state, ObjectInstance container) {
		Set<String> receivingContentNames = ContainerFactory.getContentNames(container);
		for (String name : receivingContentNames) {
			if (IngredientFactory.isBakedIngredient(state.getObject(name))) {
				return true;
			}
		}
		return false;
	}
	
	public static double getX(ObjectInstance container) {
		return container.getRealValForAttribute(ContainerFactory.attributeX);
	}
	
	public static double getY(ObjectInstance container) {
		return container.getRealValForAttribute(ContainerFactory.attributeY);
	}
	
	public static double getZ(ObjectInstance container) {
		return container.getRealValForAttribute(ContainerFactory.attributeZ);
	}
	
	public static void setX(ObjectInstance container, double x) {
		container.setValue(ContainerFactory.attributeX, x);
	}
	
	public static void setY(ObjectInstance container, double y) {
		container.setValue(ContainerFactory.attributeY, y);
	}
	
	public static void setZ(ObjectInstance container, double z) {
		container.setValue(ContainerFactory.attributeZ, z);
	}
}
