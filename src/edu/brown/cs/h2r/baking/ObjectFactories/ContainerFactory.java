package edu.brown.cs.h2r.baking.ObjectFactories;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;


public class ContainerFactory {
	
	public static final String ClassName = "container";
	private static final String attributeBaking = "baking";
	private static final String attributeMixing = "mixing";
	private static final String attributeHeating = "heating";
	private static final String attributeReceiving = "receiving";
	private static final String attributeContains = "contains";
	private static final String attributeGreased = "greased";
	private static final String attributeSpace = "space";
	
	public static final int NO_ATTRIBUTES = 0;
	public static final int BAKING = 1;
	public static final int MIXING = 2;
	public static final int HEATING = 4;
	public static final int RECEIVING = 8;
	
	public static ObjectClass createObjectClass(Domain domain)
	{
		ObjectClass objectClass = new ObjectClass(domain, ContainerFactory.ClassName);
		Attribute mixingAttribute = 
				new Attribute(domain, ContainerFactory.attributeMixing, Attribute.AttributeType.BOOLEAN);
		objectClass.addAttribute(mixingAttribute);
		
		Attribute heatingAttribute = 
				new Attribute(domain, ContainerFactory.attributeHeating, Attribute.AttributeType.BOOLEAN);
		objectClass.addAttribute(heatingAttribute);
		
		Attribute bakingAttribute =
				new Attribute(domain, ContainerFactory.attributeBaking, Attribute.AttributeType.BOOLEAN);
		objectClass.addAttribute(bakingAttribute);
		
		Attribute receivingAttribute =
				new Attribute(domain, ContainerFactory.attributeReceiving, Attribute.AttributeType.BOOLEAN);
		objectClass.addAttribute(receivingAttribute);
		
		Attribute greasedAttribute = 
				new Attribute(domain, ContainerFactory.attributeGreased, Attribute.AttributeType.BOOLEAN);
		objectClass.addAttribute(greasedAttribute);
		
		objectClass.addAttribute(
				new Attribute(domain, ContainerFactory.attributeContains, 
						Attribute.AttributeType.MULTITARGETRELATIONAL));
		
		objectClass.addAttribute(
				new Attribute(domain, ContainerFactory.attributeSpace,
						Attribute.AttributeType.RELATIONAL));
		return objectClass;
	}
	
	public static ObjectInstance getNewObjectInstance(ObjectClass containerClass, String name, 
			int attributes, Collection<String> contents, String containerSpace) {
		ObjectInstance newInstance = new ObjectInstance(containerClass, name);
		newInstance = ContainerFactory.changeAttributes(newInstance, attributes);
		newInstance = newInstance.changeValue(ContainerFactory.attributeGreased, 0);
		if (contents != null)
		{
			newInstance = newInstance.appendAllRelationTargets(ContainerFactory.attributeContains, contents);
		}
		if (containerSpace != null || containerSpace != "")
		{
			newInstance = newInstance.appendRelationalTarget(ContainerFactory.attributeSpace, containerSpace);
		}
		return newInstance;
	}
	
	public static ObjectInstance getNewObjectInstance(Domain domain, String name, Boolean mixing, Boolean heating, Boolean baking,
			Boolean receiving, List<String> contents, String containerSpace) {
		return ContainerFactory.getNewObjectInstance(
				domain.getObjectClass(ContainerFactory.ClassName), name, generateAttributeNumber(baking, heating, mixing, 
						receiving), contents, containerSpace);
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
	
	public static ObjectInstance getNewHeatingContainerObjectInstance(ObjectClass containerClass, 
			String name, List<String> contents, String containerSpace) {
		return ContainerFactory.getNewObjectInstance(containerClass, name,
				ContainerFactory.HEATING|ContainerFactory.RECEIVING, contents, containerSpace);
	}
	
	public static ObjectInstance getNewHeatingContainerObjectInstance(Domain domain, 
			String name, List<String> contents, String containerSpace) {
		return ContainerFactory.getNewObjectInstance(domain.getObjectClass(ContainerFactory.ClassName), 
				name, ContainerFactory.HEATING|ContainerFactory.RECEIVING, 
				contents, containerSpace);
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
	
	public static ObjectInstance getNewFakeToolContainerObjectInstance(ObjectClass containerClass, 
			String name, Collection<String> contents, String containerSpace) {
		return ContainerFactory.getNewObjectInstance(containerClass, name,
				ContainerFactory.NO_ATTRIBUTES, contents, containerSpace);
	}
	
	public static ObjectInstance getNewFakeToolContainerObjectInstance(Domain domain, String name,
			Collection<String> contents, String containerSpace) {
		return ContainerFactory.getNewObjectInstance(domain.getObjectClass(ContainerFactory.ClassName), 
				name, ContainerFactory.NO_ATTRIBUTES, contents, containerSpace);
	}

	public static ObjectInstance addIngredient(ObjectInstance container, String ingredient) {
		return container.appendRelationalTarget(ContainerFactory.attributeContains, ingredient);
	}
	
	public static ObjectInstance addIngredients(ObjectInstance container, Collection<String> ingredients) {
		return container.appendAllRelationTargets(ContainerFactory.attributeContains, ingredients);
	}
	
	public static ObjectInstance removeContents(ObjectInstance container) {
		return container.removeAllRelationalTarget(ContainerFactory.attributeContains);
	}
	
	public static ObjectInstance changeContainerSpace(ObjectInstance container, String containerSpace) {
		return container.appendRelationalTarget(ContainerFactory.attributeSpace, containerSpace);
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
	
	public static Boolean isGreasedContainer(ObjectInstance container) {
		return (container.getDiscValForAttribute(ContainerFactory.attributeGreased) == 1);
	}
	
	public static ObjectInstance greaseContainer(ObjectInstance container) {
		return container.changeValue(ContainerFactory.attributeGreased, 1);
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
	
	public static ObjectInstance removeIngredient(ObjectInstance container, String name) {
		return container.replaceRelationalTarget(ContainerFactory.attributeContains, name);
	}
	
	public static ObjectInstance changeAttributes(ObjectInstance object, int attributes) {
		object = object.changeValue(ContainerFactory.attributeBaking, ((attributes & ContainerFactory.BAKING) == ContainerFactory.BAKING) ? 1: 0);
		object = object.changeValue(ContainerFactory.attributeMixing, ((attributes & ContainerFactory.MIXING) == ContainerFactory.MIXING) ? 1: 0);
		object = object.changeValue(ContainerFactory.attributeHeating, ((attributes & ContainerFactory.HEATING) == ContainerFactory.HEATING) ? 1: 0);
		object = object.changeValue(ContainerFactory.attributeReceiving, ((attributes & ContainerFactory.RECEIVING) == ContainerFactory.RECEIVING) ? 1: 0);
		return object;
	}
	
	public static int generateAttributeNumber(Boolean baking, Boolean mixing, Boolean heating, Boolean receiving) {
		int baking_int = baking ? ContainerFactory.BAKING : 0;
		int mixing_int = mixing ? ContainerFactory.MIXING : 0;
		int heating_int = heating ? ContainerFactory.HEATING : 0;
		int receiving_int = receiving ? ContainerFactory.RECEIVING: 0;
		return baking_int|mixing_int|heating_int|receiving_int;
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
	
	public static boolean hasAHeatedContent(State state, ObjectInstance container) {
		Set<String> receivingContentNames = ContainerFactory.getContentNames(container);
		for (String name : receivingContentNames) {
			if (IngredientFactory.isHeatedIngredient(state.getObject(name))) {
				return true;
			}
		}
		return false;
	}
	
}
