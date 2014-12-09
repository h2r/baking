package edu.brown.cs.h2r.baking.ObjectFactories;
import java.util.Set;

import burlap.behavior.statehashing.ObjectHashFactory;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;


public class MakeSpanFactory {

	public static final String ClassName = "MakeSpan";
	private static final String attributeAgentList = "AgentList";
	private static final String attributeAgentCount = "AgentCount";
	private static final String attributePrimaryAgent = "PrimaryAgent";

	public static ObjectClass getObjectClass(Domain domain)
	{
		ObjectClass objectClass = domain.getObjectClass(MakeSpanFactory.ClassName);
		if (objectClass == null) {
			objectClass = new ObjectClass(domain, MakeSpanFactory.ClassName);
			
			Attribute agentList =
					new Attribute(domain, MakeSpanFactory.attributeAgentList, Attribute.AttributeType.MULTITARGETRELATIONAL);
			objectClass.addAttribute(agentList);
			
			Attribute agentCount = 
					new Attribute(domain, MakeSpanFactory.attributeAgentCount, Attribute.AttributeType.DISC);
			agentCount.setDiscValuesForRange(0, 10, 1);
			objectClass.addAttribute(agentCount);
			
			Attribute primaryAgent =
					new Attribute(domain, MakeSpanFactory.attributePrimaryAgent, Attribute.AttributeType.RELATIONAL);
			objectClass.addAttribute(primaryAgent);
		}
		return objectClass;
	}
	
	public static ObjectInstance getNewObjectInstance(Domain domain, String name, int agentCount, ObjectHashFactory hashingFactory) {
		return MakeSpanFactory.getNewObjectInstance(MakeSpanFactory.getObjectClass(domain), name, agentCount, hashingFactory);
	}
	
	public static ObjectInstance getNewObjectInstance(ObjectClass agentClass, String name, int agentCount, ObjectHashFactory hashingFactory) {
		ObjectInstance newInstance = new ObjectInstance(agentClass, name);
		return newInstance.changeValue(MakeSpanFactory.attributeAgentCount, agentCount);
	}
	
	public static Set<String> getOccupiedAgentNames(ObjectInstance makeSpanObject) {
		return makeSpanObject.getAllRelationalTargets(MakeSpanFactory.attributeAgentList);
	}
	
	public static String getPrimaryAgent(ObjectInstance makeSpanObject) {
		return makeSpanObject.getValueForAttribute(MakeSpanFactory.attributePrimaryAgent).getStringVal();
	}
	
	public static ObjectInstance setPrimaryAgent(ObjectInstance makeSpanObject, String primaryAgent) {
		return makeSpanObject.changeValue(MakeSpanFactory.attributePrimaryAgent, primaryAgent);
	}
	
	public static int getAgentCount(ObjectInstance makeSpanObject) {
		return makeSpanObject.getDiscValForAttribute(MakeSpanFactory.attributeAgentCount);
	}
	
	public static boolean isAgentIsFree(ObjectInstance makeSpanObject, String agentName) {
		java.util.Set<String> occupiedAgentNames = MakeSpanFactory.getOccupiedAgentNames(makeSpanObject);
		return (!occupiedAgentNames.contains(agentName));
	}
	
	public static ObjectInstance occupyAgent(ObjectInstance makeSpanObject, String agentName) {
		if (!MakeSpanFactory.isAgentIsFree(makeSpanObject, agentName)) {
			makeSpanObject = makeSpanObject.removeAllRelationalTarget(MakeSpanFactory.attributeAgentList);
		}
		return makeSpanObject.replaceRelationalTarget(MakeSpanFactory.attributeAgentList, agentName);
	}
}
