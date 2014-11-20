package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import edu.brown.cs.h2r.baking.Scheduling.Workflow.Node;

public class Workflow implements Iterable<Node> {

	private final List<Node> actions;
	public Workflow(List<Node> actions) {
		this.actions = new ArrayList<Node>(actions);
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
		
		boolean keepGoing = true;
		while (keepGoing) {
			keepGoing = false;
			for (Node node : this.actions) {
				if (node.isAvailable(sortedNodes)) {
					keepGoing |= sortedNodes.add(node);
				}
			}
		}
		
		if (sortedNodes.size() != this.actions.size()) {
			System.err.println("Sorting failed");
		}
		return new Workflow(new ArrayList<Node>(sortedNodes));
	}
	
	public List<Node> getReadyNodes() {
		List<Node> nodes = new ArrayList<Node>();
		for (Node node : this.actions) {
			if (node.degree() == 0) {
				nodes.add(node);
			}
		}
		return nodes;
	}
	
	public List<Node> getAvailableNodes(Set<Workflow.Node> visitedNodes) {
		List<Node> nodes = new ArrayList<Node>();
		for (Node node : this.actions) {
			if (node.isAvailable(visitedNodes) && !visitedNodes.contains(node)) {
				nodes.add(node);
			}
		}
		return nodes;
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
		this.actions.set(node.id, null);
		return true;
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
		private int degree;
		private final Set<Node> parents;
		private final Set<Node> children;
		public Node(int id) {
			this.id = id;
			this.degree = 0;
			this.parents = new HashSet<Node>();
			this.children = new HashSet<Node>();
		}
		
		public Set<Node> parents() {
			return Collections.unmodifiableSet(this.parents);
		}
		
		public Set<Node> children() {
			return Collections.unmodifiableSet(this.children);
		}
		
		public boolean isAvailable(Set<Node> accomplishedNodes) {
			return this.children.isEmpty() || accomplishedNodes.containsAll(this.children);
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
			for (Node parent : this.parents) {
				parent.removeChild(this);
			}
			for (Node child : this.children) {
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
			return otherNode.id == this.id;
		}
		
		@Override
		public int hashCode() {
			return this.id;
		}
		
		@Override
		public String toString() {
			return Integer.toString(this.id);
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
	}
	
}
