package edu.brown.cs.h2r.baking.Agents;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;

public abstract class Agent{
	
	private final String agentName;
	private final boolean isRobot;
	public Agent(String name) {
		this.agentName = name;
		this.isRobot = false;
	}
	
	public Agent(String name, boolean isRobot) {
		this.agentName = name;
		this.isRobot = isRobot;
	}
	
	public Agent(Map<String, Object> objectMap) {
		this.agentName = (String)objectMap.get("name");
		this.isRobot = (Boolean)objectMap.get("is_robot");
	}
	
	protected Map<String, Object> toMap() {
		Map<String, Object> objectMap = new HashMap<String, Object>();
		objectMap.put("name", this.agentName);
		objectMap.put("is_robot", this.isRobot);
		return objectMap;
	}
	
	public final String getAgentName() {
		return this.agentName;
	}
	
	public abstract void addObservation(State state);
	public abstract ObjectInstance getAgentObject();
	public abstract void setInitialState(State state);
	public abstract void reset();
	public abstract void performResetAction();
	public abstract AbstractGroundedAction getAction(State state);
	public abstract AbstractGroundedAction getActionWithScheduler(State state, List<String> agents, boolean finishRecipe);
	
	public static Map<String, Object> toMap(Agent agent) {
		String type = agent.getClass().getSimpleName();
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("type", type);
		map.put("agent", agent.toMap());
		return map;
	}
	
	public static Agent fromMap(Domain domain, Map<String, Object> map, ActionTimeGenerator timeGenerator, State startState, List<Recipe> recipes) {
		String type = (String)map.get("type");
		Map<String, Object> agentData = (Map<String, Object>)map.get("agent");
		switch(type) {
		case "Expert":
			return new Expert(domain, agentData, timeGenerator, startState, recipes);
		case "Human":
			return new Human(domain, agentData, timeGenerator, startState, recipes);
		case "AdaptiveByFlow":
			return new AdaptiveByFlow(domain, agentData, timeGenerator, recipes, startState);
		case "RandomRecipeAgent":
			return new RandomRecipeAgent(domain, agentData, timeGenerator, startState, recipes);
		case "RandomActionAgent":
			return new RandomActionAgent(domain);
			default:
				return null;
		
		}
		
	}
}
