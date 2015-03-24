package edu.brown.cs.h2r.baking.Agents;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.behavior.statehashing.StateHashFactory;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;

public abstract class Agent{
	
	private final String agentName;
	private final boolean isRobot;
	protected GroundedAction lastAction;
	private State startingState;
	private final boolean isLeadAgent;
	
	public Agent(String name) {
		this.agentName = name;
		this.isLeadAgent = (this.agentName.equals("human"));
		this.isRobot = false;
	}
	
	public Agent(String name, boolean isRobot) {
		this.agentName = name;
		this.isLeadAgent = (this.agentName.equals("human"));
		this.isRobot = isRobot;
	}
	
	public Agent(Map<String, Object> objectMap) {
		this.agentName = (String)objectMap.get("name");
		this.isLeadAgent = (this.agentName.equals("human"));
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
	
	protected State getStartState() {
		return this.startingState;
	}
	
	public abstract void addObservation(State state, GroundedAction agentsAction);
	public ObjectInstance getAgentObject(Domain domain, StateHashFactory hashingFactory) {
		return (this.isRobot) ?
				AgentFactory.getNewRobotAgentObjectInstance(domain, this.getAgentName(), hashingFactory.getObjectHashFactory()) :
				AgentFactory.getNewHumanAgentObjectInstance(domain, this.getAgentName(), hashingFactory.getObjectHashFactory());
	}
	public void setInitialState(State state) {
		this.startingState = state;
	}
	public abstract void reset();
	public abstract void performResetAction();
	public abstract AbstractGroundedAction getActionInState(State state);
	public abstract AbstractGroundedAction getActionInStateWithScheduler(State state, List<String> agents, boolean finishRecipe, GroundedAction partnersAction);
	
	public final AbstractGroundedAction getAction(State state, List<String> agents, boolean finishRecipe, boolean isFirstAction, GroundedAction partnersAction) {
		if (!this.isLeadAgent && isFirstAction) {
			this.lastAction = null;
			return lastAction;
		} 
		
		if (!(this instanceof Human) || (this instanceof RandomRecipeAgent)) {
			partnersAction = null;
		}
		this.lastAction = (GroundedAction)this.getActionInStateWithScheduler(state, agents, finishRecipe, partnersAction);
		
		return this.lastAction;
		
	}
	

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
