package edu.brown.cs.h2r.baking.ObjectFactories;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import burlap.behavior.statehashing.ObjectHashFactory;
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
	private static final String attributePouring = "pouring";
	private static final String attributeContains = "contains";
	private static final String attributeGreased = "greased";
	private static final String attributeSpace = "space";
	private static final String attributeUsed = "used";
	
	public static final int NO_ATTRIBUTES = 0;
	public static final int BAKING = 1;
	public static final int MIXING = 2;
	public static final int HEATING = 4;
	public static final int RECEIVING = 8;
	public static final int POURING = 16;
	
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
		
		Attribute pouringAttribute = 
				new Attribute(domain, ContainerFactory.attributePouring, Attribute.AttributeType.BOOLEAN);
		objectClass.addAttribute(pouringAttribute);
		
		Attribute greasedAttribute = 
				new Attribute(domain, ContainerFactory.attributeGreased, Attribute.AttributeType.BOOLEAN);
		objectClass.addAttribute(greasedAttribute);
		
		Attribute usedAttribute =
				new Attribute(domain, ContainerFactory.attributeUsed, Attribute.AttributeType.BOOLEAN);
		objectClass.addAttribute(usedAttribute);
		
		objectClass.addAttribute(
				new Attribute(domain, ContainerFactory.attributeContains, 
						Attribute.AttributeType.MULTITARGETRELATIONAL));
		
		objectClass.addAttribute(
				new Attribute(domain, ContainerFactory.attributeSpace,
						Attribute.AttributeType.RELATIONAL));
		return objectClass;
	}
	
	public static ObjectInstance getNewObjectInstance(ObjectClass containerClass, String name, 
			int attributes, Collection<String> contents, String containerSpace, ObjectHashFactory hashingFactory) {
		ObjectInstance newInstance = new ObjectInstance(containerClass, name, hashingFactory);
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
			Boolean receiving, Boolean pouring, List<String> contents, String containerSpace, ObjectHashFactory hashingFactory) {
		return ContainerFactory.getNewObjectInstance(
				domain.getObjectClass(ContainerFactory.ClassName), name, generateAttributeNumber(baking, heating, mixing, 
						receiving, pouring), contents, containerSpace, hashingFactory);
	}
	
	public static ObjectInstance getNewMixingContainerObjectInstance(ObjectClass containerClass, 
			String name, List<String> contents, String containerSpace, ObjectHashFactory hashingFactory) {
		return ContainerFactory.getNewObjectInstance(containerClass, name, ContainerFactory.MIXING|
				ContainerFactory.RECEIVING|ContainerFactory.POURING, contents, containerSpace, hashingFactory);
	}
	
	public static ObjectInstance getNewMixingContainerObjectInstance(Domain domain, 
			String name, List<String> contents, String containerSpace, ObjectHashFactory hashingFactory) {
		return ContainerFactory.getNewObjectInstance(
				domain.getObjectClass(ContainerFactory.ClassName), name, ContainerFactory.MIXING|
				ContainerFactory.RECEIVING|ContainerFactory.POURING, contents, containerSpace, hashingFactory);
	}
	
	public static ObjectInstance getNewHeatingContainerObjectInstance(ObjectClass containerClass, 
			String name, List<String> contents, String containerSpace, ObjectHashFactory hashingFactory) {
		return ContainerFactory.getNewObjectInstance(containerClass, name,
				ContainerFactory.HEATING|ContainerFactory.RECEIVING|ContainerFactory.POURING, contents, containerSpace, hashingFactory);
	}
	
	public static ObjectInstance getNewHeatingContainerObjectInstance(Domain domain, 
			String name, List<String> contents, String containerSpace, ObjectHashFactory hashingFactory) {
		return ContainerFactory.getNewObjectInstance(domain.getObjectClass(ContainerFactory.ClassName), 
				name, ContainerFactory.HEATING|ContainerFactory.RECEIVING|ContainerFactory.POURING|ContainerFactory.MIXING, 
				contents, containerSpace, hashingFactory);
	}
	
	public static ObjectInstance getNewBakingContainerObjectInstance(ObjectClass containerClass, 
			String name, List<String> contents, String containerSpace, ObjectHashFactory hashingFactory) {
		return ContainerFactory.getNewObjectInstance(containerClass, name,
				ContainerFactory.BAKING|ContainerFactory.RECEIVING|ContainerFactory.POURING, contents, containerSpace, hashingFactory);
	}
	
	public static ObjectInstance getNewBakingContainerObjectInstance(Domain domain, 
			String name, List<String> contents, String containerSpace, ObjectHashFactory hashingFactory) {
		return ContainerFactory.getNewObjectInstance(domain.getObjectClass(ContainerFactory.ClassName), 
				name, ContainerFactory.BAKING|ContainerFactory.RECEIVING|ContainerFactory.POURING, 
				contents, containerSpace, hashingFactory);
	}
	
	public static ObjectInstance getNewIngredientContainerObjectInstance(ObjectClass containerClass, 
			String name, String ingredient, String containerSpace, ObjectHashFactory hashingFactory) {
		return ContainerFactory.getNewObjectInstance(containerClass, name, ContainerFactory.POURING, Arrays.asList(ingredient), containerSpace, hashingFactory);
	}
	
	public static ObjectInstance getNewIngredientContainerObjectInstance(Domain domain, 
			String name, String ingredient, String containerSpace, ObjectHashFactory hashingFactory) {
		return ContainerFactory.getNewObjectInstance(domain.getObjectClass(ContainerFactory.ClassName), 
				name, ContainerFactory.POURING, Arrays.asList(ingredient), containerSpace, hashingFactory);
	}
	
	public static ObjectInstance getNewTrashContainerObjectInstance(Domain domain,
			String name, String containerSpace, ObjectHashFactory hashingFactory) {
		return ContainerFactory.getNewObjectInstance(domain.getObjectClass(ContainerFactory.ClassName), 
				name, ContainerFactory.RECEIVING, null, containerSpace, hashingFactory);
	}
	
	public static ObjectInstance getNewFakeToolContainerObjectInstance(ObjectClass containerClass, 
			String name, Collection<String> contents, String containerSpace, ObjectHashFactory hashingFactory) {
		return ContainerFactory.getNewObjectInstance(containerClass, name,
				ContainerFactory.NO_ATTRIBUTES, contents, containerSpace, hashingFactory);
	}
	
	public static ObjectInstance getNewFakeToolContainerObjectInstance(Domain domain, String name,
			Collection<String> contents, String containerSpace, ObjectHashFactory hashingFactory) {
		return ContainerFactory.getNewObjectInstance(domain.getObjectClass(ContainerFactory.ClassName), 
				name, ContainerFactory.NO_ATTRIBUTES, contents, containerSpace, hashingFactory);
	}

	public static ObjectInstance addIngredient(ObjectInstance container, String ingredient) {
		container = container.changeValue(ContainerFactory.attributeUsed, true);
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
		return container.getBooleanValue(ContainerFactory.attributeReceiving);
	}
	
	public static Boolean isPouringContainer(ObjectInstance container) {
		return container.getBooleanValue(ContainerFactory.attributePouring);
	}
	
	public static boolean isIngredientContainer(ObjectInstance container) {
		return (ContainerFactory.getAttributeNumber(container) == ContainerFactory.POURING);
	}
	
	public static Boolean isGreasedContainer(ObjectInstance container) {
		return (container.getDiscValForAttribute(ContainerFactory.attributeGreased) == 1);
	}
	
	public static Boolean isTrashContainer(ObjectInstance container) {
		return container.getName().equals("trash");
	}
	
	public static ObjectInstance greaseContainer(ObjectInstance container) {
		return container.changeValue(ContainerFactory.attributeGreased, 1);
	}

	public static Set<String> getContentNames(ObjectInstance container) {
		return container.getAllRelationalTargets(ContainerFactory.attributeContains);
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
		object = object.changeValue(ContainerFactory.attributeBaking, (attributes & ContainerFactory.BAKING) != 0);
		object = object.changeValue(ContainerFactory.attributeMixing, (attributes & ContainerFactory.MIXING) != 0);
		object = object.changeValue(ContainerFactory.attributeHeating, (attributes & ContainerFactory.HEATING) != 0);
		object = object.changeValue(ContainerFactory.attributeReceiving, (attributes & ContainerFactory.RECEIVING) != 0);
		object = object.changeValue(ContainerFactory.attributePouring, (attributes & ContainerFactory.POURING) != 0);
		object = object.changeValue(ContainerFactory.attributeUsed, false);
		return object;
	}
	
	public static int getAttributeNumber(ObjectInstance object) {
		boolean baking = object.getBooleanValue(ContainerFactory.attributeBaking);
		boolean mixing = object.getBooleanValue(ContainerFactory.attributeMixing);
		boolean heating = object.getBooleanValue(ContainerFactory.attributeHeating);
		boolean receiving = object.getBooleanValue(ContainerFactory.attributeReceiving);
		boolean pouring = object.getBooleanValue(ContainerFactory.attributePouring);
		return ContainerFactory.generateAttributeNumber(baking, mixing, heating, receiving, pouring);
	}
	
	public static int generateAttributeNumber(Boolean baking, Boolean mixing, Boolean heating, Boolean receiving, Boolean pouring) {
		int baking_int = baking ? ContainerFactory.BAKING : 0;
		int mixing_int = mixing ? ContainerFactory.MIXING : 0;
		int heating_int = heating ? ContainerFactory.HEATING : 0;
		int receiving_int = receiving ? ContainerFactory.RECEIVING: 0;
		int pouring_int = pouring ? ContainerFactory.POURING: 0;
		return baking_int|mixing_int|heating_int|receiving_int|pouring_int;
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
	
	public static boolean getUsed(ObjectInstance container) {
		return container.getBooleanValue(ContainerFactory.attributeUsed);
	}

	
	
}
