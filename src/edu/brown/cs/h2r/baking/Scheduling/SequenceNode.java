package edu.brown.cs.h2r.baking.Scheduling;

import java.util.Collection;
import java.util.Comparator;

public class SequenceNode {
	private final double time;
	private final Assignments assignments;
	private final Assignments completed;
	public SequenceNode(Assignments assignments, Assignments completed) {
		this.assignments = assignments;
		this.completed = completed;
		this.time = this.completed.time();
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		
		if (!(other instanceof SequenceNode)) {
			return false;
		}
		
		SequenceNode node = (SequenceNode)other;
		
		if (this.time != node.time){ 
			return false;
		}
		if (!this.completed.equals(node.completed)) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		return this.completed.hashCode();
	}
	
	@Override
	public String toString() {
		return this.completed.toString() + "\ntime:" + this.time; 
	}
	
	public double getTime() {
		return this.time;
	}
	
	public boolean complete() {
		Collection<Workflow.Node> completed = this.completed.subtasks();
		Collection<Workflow.Node> current = this.assignments.subtasks();
		if (completed.size() > current.size()) {
			return false;
		}
		return completed.containsAll(current);
	}
	
	public Assignments getAssignments() {
		return this.assignments;
	}
	
	public Assignments getCompleted() {
		return this.completed;
	}
	
	public static class SequenceComparator implements Comparator <SequenceNode>{

		@Override
		public int compare(SequenceNode lhs, SequenceNode rhs) {
			int timeRes = Double.compare(rhs.time, lhs.time);
			if (timeRes != 0) {
				return timeRes;
			}
			return Double.compare(lhs.assignments.time(), rhs.assignments.time()); 
		}
	}
}