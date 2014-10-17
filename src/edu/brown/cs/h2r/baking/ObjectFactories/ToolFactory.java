package edu.brown.cs.h2r.baking.ObjectFactories;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.actions.PourAction;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;

public class ToolFactory {

	public static final String ClassName = "tool";
	private static final String attributeToolTrait = "toolTrait";
	private static final String attributeToolAttribute = "toolAttribute";
	private static final String attributeSpace = "space";
	private static final String attributeContains = "contains";
	private static final String attributeCanCarry = "canCarry";
	private static final String attributeInclude = "incldue";
	private static final String attributeExclude = "exclude";
	
	public static final String fakeContainerName = "fakeContainer";
	
	public static ObjectClass createObjectClass(Domain domain) {
		ObjectClass objectClass = new ObjectClass(domain, ToolFactory.ClassName);
		
		objectClass.addAttribute(
				new Attribute(domain, ToolFactory.attributeToolTrait, 
						Attribute.AttributeType.RELATIONAL));
		
		objectClass.addAttribute(
				new Attribute(domain, ToolFactory.attributeToolAttribute, 
						Attribute.AttributeType.RELATIONAL));
		
		objectClass.addAttribute(
				new Attribute(domain, ToolFactory.attributeSpace,
						Attribute.AttributeType.RELATIONAL));
		
		objectClass.addAttribute(
				new Attribute(domain, ToolFactory.attributeContains,
						Attribute.AttributeType.MULTITARGETRELATIONAL));
		
		Attribute transportableAttribute = 
				new Attribute(domain, ToolFactory.attributeCanCarry, Attribute.AttributeType.BOOLEAN);
		objectClass.addAttribute(transportableAttribute);
		
		
		Attribute includeAttribute = new Attribute(domain, ToolFactory.attributeInclude, Attribute.AttributeType.MULTITARGETRELATIONAL);
		objectClass.addAttribute(includeAttribute);
		
		Attribute excludeAttribute = new Attribute(domain, ToolFactory.attributeExclude, Attribute.AttributeType.MULTITARGETRELATIONAL);
		objectClass.addAttribute(excludeAttribute);
		
		return objectClass;
	}
	
	public static ObjectInstance getNewObjectInstance(Domain domain, String name, 
			String trait, String attribute, String containerSpace) {
		ObjectClass oc = domain.getObjectClass(ToolFactory.ClassName);
		return getNewObjectInstance(oc, name, trait, attribute, containerSpace);
	}
	
	private static ObjectInstance getNewObjectInstance(ObjectClass toolClass, String name, 
			String trait, String attribute, String containerSpace) {
		ObjectInstance newInstance = new ObjectInstance(toolClass, name);
		newInstance = newInstance.appendRelationalTarget(ToolFactory.attributeToolTrait, trait);
		newInstance = newInstance.appendRelationalTarget(ToolFactory.attributeToolAttribute, attribute);
		//newInstance.addRelationalTarget(ToolFactory.attributeContains, null);
		newInstance = newInstance.changeValue(ToolFactory.attributeCanCarry, false);
		
		if (containerSpace != null || containerSpace != "")
		{
			newInstance = newInstance.appendRelationalTarget(ToolFactory.attributeSpace, containerSpace);
		}
		return newInstance;
	}
	
	public static ObjectInstance getNewSimpleToolObjectInstance(Domain domain, String name, 
			String trait, String attribute, String containerSpace) {
		return getNewObjectInstance(domain, name, trait, attribute, containerSpace);
	}
	
	public static ObjectInstance getNewCarryingToolObjectInstance(Domain domain, String name, 
			String trait, String attribute, String containerSpace, Collection<String> include,
			Collection<String> exclude) {
		ObjectInstance tool = getNewObjectInstance(domain, name, trait, attribute, containerSpace);
		tool = tool.changeValue(ToolFactory.attributeCanCarry, true);
		tool = tool.appendAllRelationTargets(ToolFactory.attributeInclude, include);
		tool = tool.appendAllRelationTargets(ToolFactory.attributeExclude, exclude);
		return tool;
	}
	
	public static String getToolTrait(ObjectInstance tool) {
		Set<String> traits = tool.getAllRelationalTargets(ToolFactory.attributeToolTrait);
		if (traits != null && !traits.isEmpty()) {
			return traits.iterator().next();
		}
		return null;
	}
	
	public static String getToolAttribute(ObjectInstance tool) {
		Set<String> attributes = tool.getAllRelationalTargets(ToolFactory.attributeToolAttribute);
		if (attributes != null && !attributes.isEmpty()) {
			return attributes.iterator().next();
		}
		return null;
	}
	
	public static String getSpaceName(ObjectInstance tool) {
		Set<String> spaces =  tool.getAllRelationalTargets(ToolFactory.attributeSpace);
		if (spaces != null && !spaces.isEmpty()) {
			return spaces.iterator().next();
		}
		return null;
	}
	
	public static boolean toolCanBeUsed(ObjectInstance tool, IngredientRecipe ingredient) {
		return ingredient.hasToolTrait(ToolFactory.getToolTrait(tool));
	}
	
	public static boolean toolCanBeUsed(ObjectInstance tool, ObjectInstance ingredient) {
		return IngredientFactory.getToolTraits(ingredient).contains(ToolFactory.getToolTrait(tool));
	}
	
	public static boolean toolHasBeenUsed(ObjectInstance tool, IngredientRecipe ingredient) {
		return ingredient.hasToolAttribute(ToolFactory.getToolAttribute(tool));
	}
	
	public static boolean toolHasBeenUsed(ObjectInstance tool, ObjectInstance ingredient) {
		return IngredientFactory.getToolAttributes(ingredient).contains(ToolFactory.getToolAttribute(tool));
	}
	
	public static Set<String> getContents(ObjectInstance tool) {
		return tool.getAllRelationalTargets(ToolFactory.attributeContains);
	}
	
	public static ObjectInstance removeContents(ObjectInstance tool) {
		return tool.removeAllRelationalTarget(ToolFactory.attributeContains);
	}
	
	public static ObjectInstance removeIngredient(ObjectInstance tool, ObjectInstance ingredient) {
		return tool.replaceRelationalTarget(ToolFactory.attributeContains, ingredient.getName());
	}
	
	public static ObjectInstance removeIngredient(ObjectInstance tool, IngredientRecipe ingredient) {
		return tool.replaceRelationalTarget(ToolFactory.attributeContains, ingredient.getName());
	}
	
	public static boolean toolCanCarry(ObjectInstance tool) {
		return tool.getValueForAttribute(ToolFactory.attributeCanCarry).getBooleanValue();
	}
	
	public static boolean toolContainsIngredient(ObjectInstance tool, ObjectInstance ingredient) {
		if (!ToolFactory.toolCanCarry(tool)) {
			return false;
		}
		return ToolFactory.getContents(tool).contains(ingredient.getName());
	}
	
	public static boolean toolContainsIngredient(ObjectInstance tool, IngredientRecipe ingredient) {
		if (!ToolFactory.toolCanCarry(tool)) {
			return false;
		}
		return ToolFactory.getContents(tool).contains(ingredient.getName());
	}
	
	public static boolean isEmpty(ObjectInstance tool) {
		if (!ToolFactory.toolCanCarry(tool)) {
			return true;
		}
		return ToolFactory.getContents(tool).isEmpty();
	}
	
	public static ObjectInstance addInclude(ObjectInstance tool, Collection<String> includes) {
		return tool.appendAllRelationTargets(ToolFactory.attributeInclude, includes);
	}
	
	public static ObjectInstance addInclude(ObjectInstance tool, String include) {
		return tool.appendRelationalTarget(ToolFactory.attributeInclude, include);
	}
	
	public static ObjectInstance addExclude(ObjectInstance tool, String exclude) {
		return tool.appendRelationalTarget(ToolFactory.attributeExclude, exclude);
	}
	
	public static ObjectInstance addExclude(ObjectInstance tool, Collection<String> excludes) {
		return tool.appendAllRelationTargets(ToolFactory.attributeExclude, excludes);
	}
	
	public static Set<String> getIncludes(ObjectInstance tool) {
		Set<String> includes;
		if ((includes = tool.getAllRelationalTargets(ToolFactory.attributeInclude)) != null) {
			return includes;
		}
		return new HashSet<String>();
	}
	
	public static Set<String> getExcludes(ObjectInstance tool) {
		Set<String> includes;
		if ((includes = tool.getAllRelationalTargets(ToolFactory.attributeExclude)) != null) {
			return includes;
		}
		return new HashSet<String>();
	}
	
	public static State addIngredientToTool(State state, ObjectInstance tool, ObjectInstance ingredient) {
		ObjectInstance ingredientContainer = state.getObject(IngredientFactory.getContainer(ingredient));
		
		ObjectInstance newTool = tool.appendRelationalTarget(ToolFactory.attributeContains, ingredient.getName());
		state = state.replaceObject(tool, newTool);
		
		ObjectInstance newIngredient = IngredientFactory.changeIngredientContainer(ingredient, tool.getName());
		state = state.replaceObject(ingredient, newIngredient);
		
		ObjectInstance newContainer = ContainerFactory.removeIngredient(ingredientContainer, ingredient.getName());	
		state = state.replaceObject(ingredientContainer,  newContainer);
		
		return state;
	}
	
	
	public static State pourIngredients(Domain domain, State state, ObjectInstance tool, 
			ObjectInstance container, String agent) {
		Set<String> contents = ToolFactory.getContents(tool);
		String spaceName = ToolFactory.getSpaceName(tool);
		ObjectInstance fakeContainer = ContainerFactory.getNewFakeToolContainerObjectInstance(domain, 
				ToolFactory.fakeContainerName, contents, spaceName);
		state = state.appendObject(fakeContainer);
		PourAction pour = (PourAction)domain.getAction(PourAction.className);
		
		State newState = pour.performAction(state, new String[] {agent, ToolFactory.fakeContainerName, container.getName()});
		state = state.remove(container);
		state = state.appendObject(newState.getObject(container.getName()));
		for (String name : ToolFactory.getContents(tool)) {
			state = state.remove(name);
			state = state.appendObject(newState.getObject(name));
		}
		state = state.remove(fakeContainer);
		ObjectInstance newTool = ToolFactory.removeContents(tool);
		return state.replaceObject(tool, newTool);
	}
}