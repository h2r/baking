import java.util.Arrays;
import java.util.List;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;


public class AgentFactory {
	
	public static final String ClassName = "agent";
	private static final String attributeRobot = "robot";
	
	public static ObjectClass createObjectClass(Domain domain)
	{
		ObjectClass objectClass = new ObjectClass(domain, "container");
		Attribute robotAttribute =
				new Attribute(domain, AgentFactory.attributeRobot, Attribute.AttributeType.DISC);
		robotAttribute.setDiscValuesForRange(0,1,1);
		objectClass.addAttribute(robotAttribute);
		return objectClass;
	}
	
	public static ObjectInstance getNewObjectInstance(ObjectClass agentClass, String name, Boolean robot) {
		ObjectInstance newInstance = new ObjectInstance(agentClass, name);
		newInstance.setValue(AgentFactory.attributeRobot, robot ? 1 : 0);
		return newInstance;
	}
	
	public static ObjectInstance getNewObjectInstance(Domain domain, String name, Boolean robot) {
		return AgentFactory.getNewObjectInstance(domain.getObjectClass(AgentFactory.ClassName), name, robot);
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
