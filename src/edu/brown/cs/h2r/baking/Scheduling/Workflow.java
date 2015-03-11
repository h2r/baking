package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.Scheduling.Workflow.Node;

public class Workflow implements Iterable<Node> {
	private static List<String> shareableResources = Arrays.asList("counter", "stove", "oven");
	private final List<Node> actions;
	private final State startState;
	public Workflow(State startState) {
		this.actions = new ArrayList<Node>();
		this.startState = startState;
	}
	
	public Workflow(List<Node> actions) {
		this.actions = new ArrayList<Node>(actions);
		this.startState = null;
	}
	
	public Workflow(State startState, List<Node> actions) {
		this.actions = new ArrayList<Node>(actions);
		this.startState = startState;
	}
	
	public boolean add(Node node){
		return this.actions.add(node);
	}
	
	public boolean add(Node node, List<Integer> dependencies) {
		boolean result = this.actions.add(node);
		for (Integer i : dependencies) {
			if (!this.connect(i, node.id)) {
				System.err.println("Cycle detected in this graph, that shouldn't happen");
				return false;
			}
			
		}
		return result;
	}
	
	public static Workflow buildWorkflow(State state, List<AbstractGroundedAction> actions) {
		Workflow workflow = new Workflow(state);
		
		for (int i = 0; i < actions.size(); i++) {
			GroundedAction action = (GroundedAction)actions.get(i);
			Node node = new Node(i, action);
			List<Integer> dependencies = Workflow.getDependencies(action, workflow);
			
			workflow.add(node, dependencies);
		}
		
		return workflow;
	}
	
	public static List<Integer> getDependencies(GroundedAction action, Workflow workflow) {
		List<Integer> dependencies = new ArrayList<Integer>();
		
		if (action.action == null) {
			return dependencies;
		}
		State compareState = workflow.getEndState();
		compareState = action.executeIn(compareState);
		List<Node> leaves = workflow.getLeafNodes();
		Node newNode = new Node(workflow.size(), action);
		
		for (Node node : leaves) {
			Workflow sorted = workflow.sort();
			sorted.add(newNode);
			sorted.swap(node, newNode);
			State state = sorted.getEndState();
			if (!state.equals(compareState)) {
				dependencies.add(node.id);
			}
		}
		return dependencies;
	}
	
	private List<Node> getLeafNodes() {
		List<Node> leaves = new ArrayList<Node>();
		for (Node node : this) {
			if (node.children.isEmpty()) {
				leaves.add(node);
			}
		}
		return leaves;
	}

	public Node get(int i) {
		if (i < this.actions.size()) {
			return this.actions.get(i);
		}
		return null;
	}
	
	public Node get(String actionName) {
		for (Node node : this.actions){ 
			if (node.getAction().toString().equals(actionName)) {
				return node;
			}
		}
		return null;
	}
	
	public State getStartState() {
		return this.startState;
	}
	
	public State getEndState() {
		State state = this.startState;
		for (Node node : this) {
			if (node == null) {
				continue;
			}
			GroundedAction action = node.action;
			if (!action.action.applicableInState(state, action.params)) {
				return state;
			}
			state = action.executeIn(state);
		}
		
		return state;
	}
	@Override
	public Iterator<Node> iterator() {
		return this.actions.iterator();
	}
	
	public int size() {
		return this.actions.size();
	}
	
	public boolean connect(int fromId, int toId) {
		Node from = this.actions.get(fromId);
		Node to = this.actions.get(toId);
		return from.addChild(to);
	}
	
	public int degree() {
		int max = 0;
		for (Node node : this.actions) {
			max = Math.max(node.degree(), max);
		}
		return max;
	}
	
	public Workflow sort() {
		Set<Node> sortedNodes = new LinkedHashSet<Node>();
		List<Node> nodes = new ArrayList<Node>(this.actions);
		Collections.shuffle(nodes);
		boolean keepGoing = true;
		while (keepGoing) {
			keepGoing = false;
			for (Node node : nodes) {
				if (node.isAvailable(sortedNodes)) {
					keepGoing |= sortedNodes.add(node);
				}
			}
		}
		
		if (sortedNodes.size() != this.actions.size()) {
			System.err.println("Sorting failed");
		}
		
		return new Workflow(this.startState, new ArrayList<Node>(sortedNodes));
	}
	
	public List<Node> getReadyNodes() {
		List<Node> nodes = new ArrayList<Node>();
		for (Node node : this.actions) {
			if (node.parents.size() == 0) {
				nodes.add(node);
			}
		}
		return nodes;
	}
	
	public List<Node> getAvailableNodes(Collection<Node> collection) {
		List<Node> nodes = new ArrayList<Node>();
		for (Node node : this.actions) {
			if (node.isAvailable(collection) && !collection.contains(node)) {
				nodes.add(node);
			}
		}
		return nodes;
	}
	
	public List<Node> notVisitedNodes(Set<Workflow.Node>visitedNodes ) {
		List<Node> nodes = new ArrayList<Node>(this.actions.size());
		for (Workflow.Node node : this.actions) {
			if (!visitedNodes.contains(node)) {
				nodes.add(node);
			}
		}
		return nodes;
	}
	


	public boolean allSubtasksAssigned(Collection<Node> subtasks) {
		if (this.actions.size() > subtasks.size()) {
			return false;
		}
		return this.actions.containsAll(subtasks);
	}
	
	public void insert(int position, Node node) {
		position = Math.min(this.actions.size(), position);
		this.actions.add(position, node);
	}
	
	
	
	public void swap(Node node1, Node node2) {
		int index1 = this.actions.indexOf(node1);
		int index2 = this.actions.indexOf(node2);
		if (index1 < 0 || index2 < 0) {
			return;
		}
		this.actions.set(index1, node2);
		this.actions.set(index2, node1);
	}
	
	public boolean remove(int id) {
		Node node = this.actions.get(id);
		if (node == null) {
			return false;
		}
		return this.remove(node);
	}
	
	public boolean remove(Node node) {
		if (node.degree() != 0) {
			return false;
		}
		
		node.pop();
		return this.actions.remove(node);
	}
	
	public Set<Node> remove(Set<Node> toRemove) {
		Set<Node> remaining = new HashSet<Node>(toRemove);
		Set<Node> removed = new HashSet<Node>();
		
		boolean keepGoing = true;
		while(keepGoing) {
			removed.clear();
			for (Node node : remaining) {
				if (this.remove(node)) {
					removed.add(node);
				}
			}
			remaining.removeAll(removed);
			keepGoing = !remaining.isEmpty() && !removed.isEmpty();
		}
		
		return remaining;
	}
	
	public static class Node {
		private final int id;
		private final GroundedAction action;
		private int degree;
		private final Set<Node> parents;
		private final Set<Node> children;
		private final Set<String> resources;
		
		public Node(int id) {
			this.id = id;
			this.action = new GroundedAction(null, new String[]{"agent", Integer.toString(this.id)});
			this.degree = 0;
			this.parents = new HashSet<Node>();
			this.children = new HashSet<Node>();
			this.resources = null;
		}
		
		public Node(int id, Set<String> resources) {
			this.id = id;
			this.action = new GroundedAction(null, new String[]{"agent", Integer.toString(this.id)});
			this.degree = 0;
			this.parents = new HashSet<Node>();
			this.children = new HashSet<Node>();
			this.resources = new HashSet<String>(resources);
		}
		
		public Node(int id, GroundedAction action) {
			this.id = id;
			this.action = action;
			this.degree = 0;
			this.parents = new HashSet<Node>();
			this.children = new HashSet<Node>();
			String[] resources = Arrays.copyOfRange(action.params, 1, action.params.length);
			this.resources = new HashSet<String>(Arrays.asList(resources));
			this.resources.removeAll(shareableResources);
		}
		
		public Set<Node> parents() {
			return Collections.unmodifiableSet(this.parents);
		}
		
		public Set<Node> children() {
			return Collections.unmodifiableSet(this.children);
		}
		
		public boolean isAvailable(Collection<Node> collection) {
			return this.parents.isEmpty() || collection.containsAll(this.parents);
		}
		
		public GroundedAction getAction() {
			return new GroundedAction(this.action.action, Arrays.copyOf(this.action.params, this.action.params.length));
		}
		
		public GroundedAction getAction(String agent) {
			String [] params = Arrays.copyOf(this.action.params, this.action.params.length);
			params[0] = agent;
			return new GroundedAction(this.action.action, params);
		}
		
		private int maxDegree() {
			int max = 0;
			for (Node node : this.children){
				max = Math.max(max, node.degree());
			}
			return max + 1;
		}
		
		public void computeDegree() {
			int before = this.degree;
			this.degree = this.maxDegree();
			if (this.degree != before) {
				for (Node parent : this.parents) {
					parent.computeDegree();
				}
			}
		}
		
		public boolean addParent(Node parent) {
			if (!this.parents.add(parent)) {
				return false;
			}
			parent.addChild(this);
			return true;
		}
		
		public boolean removeParent(Node parent) {
			if (!this.parents.remove(parent)) {
				return false;
			}
			parent.removeChild(this);
			return true;
		}
		
		public boolean addChild(Node child) {
			if (!this.checkChildCanBeAdded(child)) {
				return false;
			}
			
			if (!this.children.add(child)) {
				return false;
			}
			
			child.addParent(this);
			this.degree = Math.max(this.degree, child.degree() + 1);
			return true;
		}
		
		public boolean removeChild(Node child) {
			if (!this.children.remove(child)) {
				return false;
			}
			
			child.removeParent(this);
			this.computeDegree();
			return true; 
		}
		
		public void pop() {
			List<Node> parents = new ArrayList<Node>(this.parents);
			for (Node parent : parents) {
				parent.removeChild(this);
			}
			List<Node> children = new ArrayList<Node>(this.children);
			for (Node child : children) {
				child.removeParent(this);
			}
		}
		
		public int degree() {
			return this.degree;
		}
		
		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof Node)) {
				return false;
			}
			Node otherNode = (Node)other;
			return (otherNode.id == this.id && this.action.params.equals(otherNode.action.params));
		}
		
		@Override
		public int hashCode() {
			return this.id;
		}
		
		@Override
		public String toString() {
			return Integer.toString(this.id) + ((this.resources != null) ? this.resources.toString() : "");
		}
		
		private boolean checkChildCanBeAdded(Node child) {
			if (this == child) {
				return false;
			}
			
			Set<Node> visitedNodes = new HashSet<Node>();
			visitedNodes.add(this);
			visitedNodes.add(child);
			
			return this.checkCycle(visitedNodes, child);
		}
		
		private boolean checkCycle(Set<Node> visitedNodes, Node currentNode) {
			Set<Node> thisVisitedNodes = new HashSet<Node>(visitedNodes);
			Set<Node> nextNodes = currentNode.children;
			
			if (nextNodes.isEmpty()) {
				return true;
			}
			
			for (Node node : nextNodes) {
				if (!thisVisitedNodes.add(node)) {
					return false;
				} else if (!this.checkCycle(thisVisitedNodes, node)) {
					return false;
				}
			}
			return true;
		}

		public boolean ancestorOf(Node secondNode) {
			if (this.equals(secondNode)) {
				return false;
			}
			for (Node node : this.children) {
				if (node.equals(secondNode) || node.ancestorOf(secondNode)) {
					return true;
				}
			}
			return false;
		}
		
		public Set<String> getResources() {
			return new HashSet<String>(this.resources);
		}
		
		public boolean resourceConflicts(Node node) {
			Set<String> shorter = null;
			Set<String> longer = null;
			if (this.resources.size() < node.resources.size()) {
				shorter = this.resources; 
				longer = node.resources;
			} else {
				shorter = node.resources;
				longer = this.resources; 
			}
			
			for (String resource : shorter) {
				if (longer.contains(resource)) {
					return true;
				}
			}
			return false;
		}
		
		public boolean resourceConflicts(Collection<Node> nodes) {
			Set<String> resources = new HashSet<String>();
			for (Node node : nodes) {
				if (node != null) {
					resources.addAll(node.resources);
				}
			}
			for (String resource : this.resources) {
				if (resources.contains(resource)) {
					return true;
				}
			}
			return false;
		}
	}
	
}
