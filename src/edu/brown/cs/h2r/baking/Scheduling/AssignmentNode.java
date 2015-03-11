package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.brown.cs.h2r.baking.Scheduling.Assignment.ActionTime;

public class AssignmentNode {
	private final double time;
	private final Assignments assignments;
	private final Assignments sequenced;
	private final Workflow workflow;
	
	public AssignmentNode(Workflow workflow, Assignments assignments, Assignments sequenced) {
		this.assignments = assignments;
		this.sequenced = sequenced;
		this.workflow = workflow;
		this.time = sequenced.time();
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		
		if (!(other instanceof AssignmentNode)) {
			return false;
		}
		
		AssignmentNode node = (AssignmentNode)other;
		
		return (this.time == node.time && this.assignments.equals(node.assignments) && this.sequenced.equals(node.sequenced));
	}
	
	@Override
	public int hashCode() {
		return this.assignments.hashCode();
	}
	
	public double getTime() {
		return this.time;
	}
	
	public Assignments getAssignments() {
		return this.assignments;
	}
	
	public Assignments getSequenced() {
		return this.sequenced;
	}
	
	public boolean complete() {
		return this.workflow.allSubtasksAssigned(this.assignments.subtasks());
	}
	
	public Collection<Workflow.Node> getAssignedNodes() {
		return this.assignments.subtasks();
	}

	public static class AssignmentComparator implements Comparator <AssignmentNode>{

		@Override
		public int compare(AssignmentNode lhs, AssignmentNode rhs) {
			return Double.compare(rhs.time, lhs.time);
		}
	}
}