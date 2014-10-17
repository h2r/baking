package edu.brown.cs.h2r.baking.ObjectFactories;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;


public class AgentFactory {
	
	public static final String ClassName = "agent";
	private static final String attributeRobot = "robot";
	
	public static ObjectClass getObjectClass(Domain domain)
	{
		ObjectClass objectClass = domain.getObjectClass(AgentFactory.ClassName);
		if (objectClass == null) {
			objectClass = new ObjectClass(domain, AgentFactory.ClassName);
			Attribute robotAttribute =
					new Attribute(domain, AgentFactory.attributeRobot, Attribute.AttributeType.DISC);
			robotAttribute.setDiscValuesForRange(0,1,1);
			objectClass.addAttribute(robotAttribute);
		}
		return objectClass;
	}
	
	public static ObjectInstance getNewObjectInstance(ObjectClass agentClass, String name, Boolean robot) {
		ObjectInstance newInstance = new ObjectInstance(agentClass, name);
		return newInstance.changeValue(AgentFactory.attributeRobot, robot ? 1 : 0);
	}
	
	public static ObjectInstance getNewObjectInstance(Domain domain, String name, Boolean robot) {
		return AgentFactory.getNewObjectInstance(AgentFactory.getObjectClass(domain), name, robot);
	}
	
	public static ObjectInstance getNewHumanAgentObjectInstance(ObjectClass agentClass, String name) {
		return AgentFactory.getNewObjectInstance(agentClass, name, false);
	}
	
	public static ObjectInstance getNewHumanAgentObjectInstance(Domain domain, String name) {
		return AgentFactory.getNewObjectInstance(domain, name, false);
	}
	
	public static ObjectInstance getNewRobotAgentObjectInstance(ObjectClass agentClass, String name) {
		return AgentFactory.getNewObjectInstance(agentClass, name, true);
	}
	
	public static ObjectInstance getNewRobotAgentObjectInstance(Domain domain, String name) {
		return AgentFactory.getNewObjectInstance(domain, name, true);
	}
	
	public static Boolean isRobot(ObjectInstance objectInstance) {
		return (objectInstance.getDiscValForAttribute(AgentFactory.attributeRobot) == 1);
	}
	
}
