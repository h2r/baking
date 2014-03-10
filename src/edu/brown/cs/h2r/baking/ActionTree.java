package edu.brown.cs.h2r.baking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

public class ActionTree {
	private ActionNode root;
	public ActionTree(State state) {
		this.root = new ActionNode();
		this.root.state = new State(state);
	}
	
	public static class ActionNode {
		private State state;
		private GroundedAction groundedAction;
		private ActionNode parent;
		private List<ActionNode> children;
		
		public ActionNode() {
			this.state = null;
			this.groundedAction = null;
			this.children = new ArrayList<ActionNode>();
		}
		
		public ActionNode(ActionNode parent, State state, GroundedAction groundedAction) {
			this.parent = parent;
			this.state = new State(state);
			this.groundedAction = new GroundedAction(groundedAction.action, groundedAction.params);
			this.children = new ArrayList<ActionNode>();
		}
		
		public State getState() {
			return this.state;
		}
		
		public GroundedAction getGroundedAction() {
			return new GroundedAction(this.groundedAction.action, this.groundedAction.params);
		}
		
		public ActionNode getParent() {
			return this.parent;
		}
		
		public List<ActionNode> getChildren() {
			return this.children;
		}
		
		public void add(State state, GroundedAction groundedAction) {
			this.children.add(new ActionNode(this, state, groundedAction));
		}
	}
	
	public void add(State state, GroundedAction groundedAction) {
		this.root.children.add(new ActionNode(this.root, state, groundedAction));
	}
	
	public void add(ActionNode actionNode) {
		this.root.children.add(actionNode);
	}
	
	public void addAll(Collection<ActionNode> actionNodes) {
		this.root.children.addAll(actionNodes);
	}
	
	public GroundedAction getGroundedAction() {
		return this.root.groundedAction;
	}
	
	public State getState() {
		return new State(this.root.state);
	}
	
	public List<ActionNode> getChildren() {
		return new ArrayList<ActionNode>(this.root.children);
	}
}