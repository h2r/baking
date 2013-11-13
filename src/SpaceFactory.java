import java.util.Arrays;
import java.util.List;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;


public class SpaceFactory {

	private static final String attributeBaking = "mixing";
	private static final String attributeHeating = "heating";
	private static final String attributeWorking = "working";
	private static final String attributeContains = "contains";

	public static ObjectClass createObjectClass(Domain domain)
	{
		ObjectClass objectClass = new ObjectClass(domain, "space");
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
		return objectClass;
	}
	
	public static ObjectInstance getNewObjectInstance(ObjectClass spaceClass, String name, 
			Boolean baking, Boolean heating, Boolean working, List<String> containers) {
		ObjectInstance newInstance = new ObjectInstance(spaceClass, name);
		newInstance.setValue(SpaceFactory.attributeBaking, baking ? 1 : 0);
		newInstance.setValue(SpaceFactory.attributeHeating, heating ? 1 : 0);
		newInstance.setValue(SpaceFactory.attributeWorking, working ? 1 : 0);
		if (containers != null)
		{
			for (String container : containers)
			{
				newInstance.addRelationalTarget(SpaceFactory.attributeBaking, container);
			}
		}
		return newInstance;
	}
	
	public static ObjectInstance getNewWorkingSpaceObjectInstance(ObjectClass spaceClass, 
			String name, List<String> containers) {
		return SpaceFactory.getNewObjectInstance(spaceClass, name, false, false, true, containers);
	}
	
	public static ObjectInstance getNewHeatingSpaceObjectInstance(ObjectClass spaceClass, 
			String name, List<String> containers) {
		return SpaceFactory.getNewObjectInstance(spaceClass, name, false, true, false, containers);
	}
	
	public static ObjectInstance getNewBakingSpaceObjectInstance(ObjectClass spaceClass, 
			String name, List<String> containers) {
		return SpaceFactory.getNewObjectInstance(spaceClass, name, true, false, false, containers);
	}

	public static void addContainer(ObjectInstance space, ObjectInstance container) {
		space.addRelationalTarget(SpaceFactory.attributeContains, container.getName());
	}
}
