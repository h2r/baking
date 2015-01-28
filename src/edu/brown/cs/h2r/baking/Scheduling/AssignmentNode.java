package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.brown.cs.h2r.baking.Scheduling.Assignment.ActionTime;

public class AssignmentNode {
	private final double time;
	private final Map<String, Assignment> assignments;
	private final Set<Workflow.Node> assignedNodes;
	private final List<Assignment> completedAssignments;
	
	public AssignmentNode(List<Assignment> assignments, boolean useActualValues, ActionTimeGenerator timeGenerator, Double initialTime, List<Assignment> completedAssignments) {
		this.assignments = new HashMap<String, Assignment>();
		this.assignedNodes = new HashSet<Workflow.Node>();
		for (Assignment assignment : assignments) {
			this.assignments.put(assignment.getId(), assignment);
			this.assignedNodes.addAll(assignment.nodes());
		}
		
		this.completedAssignments = completedAssignments;
		if (initialTime == null) {
			BufferedAssignments buffered = new BufferedAssignments(assignments);
			this.time = buffered.time();
		} else {
			this.time = initialTime;
		}
	}
	
	public AssignmentNode(AssignmentNode node, String agent, ActionTime action, Double initialTime, List<Assignment> completedAssignments) {
		
		Map<String, Assignment> assignments = SchedulingHelper.copyMap(node.assignments);
		Assignment agentsAssignment = assignments.get(agent);
		if (node.getAssignedNodes().contains(action.getNode())) {
			this.time = node.time;
			this.assignments = node.assignments;
			this.assignedNodes = node.getAssignedNodes();
			this.completedAssignments = node.completedAssignments;
			return;
		}
		
		agentsAssignment.add(action.getNode());
		Set<Workflow.Node> visitedNodes = new HashSet<Workflow.Node>(node.getAssignedNodes());
		visitedNodes.add(action.getNode());
		
		this.completedAssignments = completedAssignments;
		
		if (initialTime == null) {
			BufferedAssignments buffered = new BufferedAssignments(assignments.values());
			this.time = buffered.time();
		} else {
			this.time = initialTime;
		}
		
		this.assignments = assignments;
		this.assignedNodes = visitedNodes;
	}
	
	/*
	public AssignmentNode(Workflow workflow, Scheduler heuristicScheduler, ActionTimeGenerator timeGenerator, List<String> agents, boolean useActualValues) {
		this.assignments = new HashMap<String, Assignment>();
		for (String agent : agents) {
			this.assignments.put(agent, new Assignment(agent, timeGenerator,useActualValues));
		}
		this.assignedNodes = new HashSet<Workflow.Node>();
		this.workflow = workflow;
		this.scheduler = heuristicScheduler;
		this.timeGenerator = timeGenerator;
		
		List<Assignment> copied = SchedulingHelper.copy(new ArrayList<Assignment>(this.assignments.values()));
		this.bufferedAssignments = new BufferedAssignments(copied);
		BufferedAssignments completedBuffered = this.bufferedAssignments.copy();
		
		Set<Workflow.Node> visitedNodes = new HashSet<Workflow.Node>(this.getAssignedNodes()); 
		this.completedAssignments = (this.scheduler == null) ? null : 
				this.scheduler.finishSchedule(workflow, this.timeGenerator, copied, completedBuffered, visitedNodes);
		
		this.time = completedBuffered.time();
		
	}
	
	public AssignmentNode(Workflow workflow, List<Assignment> assignments, Scheduler heuristicScheduler, ActionTimeGenerator timeGenerator) {
		this.assignments = new HashMap<String, Assignment>();
		this.assignedNodes = new HashSet<Workflow.Node>();
		
		for (Assignment assignment : assignments) {
			this.assignments.put(assignment.getId(), assignment);
			this.getAssignedNodes().addAll(assignment.nodes());
		}
		
		this.workflow = workflow;
		this.scheduler = heuristicScheduler;
		this.timeGenerator = timeGenerator;
		
		List<Assignment> copied = SchedulingHelper.copy(new ArrayList<Assignment>(this.assignments.values()));
		this.bufferedAssignments = new BufferedAssignments(copied);
		BufferedAssignments completedBuffered = this.bufferedAssignments.copy();
		
		Set<Workflow.Node> visitedNodes = new HashSet<Workflow.Node>(this.getAssignedNodes()); 
		this.completedAssignments = (this.scheduler == null) ? null : 
				this.scheduler.finishSchedule(workflow, this.timeGenerator, copied, completedBuffered, visitedNodes);
		
		this.time = completedBuffered.time();
		
	}
	
	public AssignmentNode(AssignmentNode node, String agent, ActionTime action) {
		this.workflow = node.workflow;
		this.scheduler = node.scheduler;
		this.timeGenerator = node.timeGenerator;

		Map<String, Assignment> assignments = SchedulingHelper.copyMap(node.assignments);
		Assignment agentsAssignment = assignments.get(agent);
		if (node.getAssignedNodes().contains(action.getNode())) {
			this.time = node.time;
			this.assignments = node.assignments;
			this.assignedNodes = node.getAssignedNodes();
			this.bufferedAssignments = node.bufferedAssignments;
			this.completedAssignments = node.completedAssignments;
			return;
		}
		
		
		agentsAssignment.add(action.getNode());
		Set<Workflow.Node> visitedNodes = new HashSet<Workflow.Node>(node.getAssignedNodes());
		visitedNodes.add(action.getNode());
		
		this.bufferedAssignments = node.bufferedAssignments.copy();
		this.bufferedAssignments.add(action.getNode(), agent);
		BufferedAssignments completedBuffered = this.bufferedAssignments.copy();
		List<Assignment> copied = SchedulingHelper.copy(new ArrayList<Assignment>(assignments.values()));
		this.completedAssignments = (this.scheduler == null) ? null :
				this.scheduler.finishSchedule(workflow, this.timeGenerator, copied, completedBuffered, visitedNodes);
		double newTime = completedBuffered.time();
		
		if (newTime <= node.time || this.scheduler == null){ 
			this.time = newTime;
			this.assignments = assignments;
			this.assignedNodes = new HashSet<Workflow.Node>(node.getAssignedNodes());
			this.getAssignedNodes().add(action.getNode());
		} else {
			this.time = node.time;
			this.assignments = node.assignments;
			this.assignedNodes = node.getAssignedNodes();
		}
	}*/
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		
		if (!(other instanceof AssignmentNode)) {
			return false;
		}
		
		AssignmentNode node = (AssignmentNode)other;
		
		return (this.time == node.time && this.getAssignedNodes().equals(node.getAssignedNodes()));
	}
	
	@Override
	public int hashCode() {
		return this.assignments.hashCode();
	}
	
	public double getTime() {
		return this.time;
	}
	
	public List<Assignment> getAssignments() {
		return new ArrayList<Assignment>(this.assignments.values());
	}
	
	public List<Assignment> getCompletedAssignments() {
		return this.completedAssignments;
	}
	
	public boolean complete(Workflow workflow) {
		return workflow.notVisitedNodes(this.getAssignedNodes()).isEmpty();
	}
	
	public Set<Workflow.Node> getAssignedNodes() {
		return assignedNodes;
	}

	public static class AssignmentComparator implements Comparator <AssignmentNode>{

		@Override
		public int compare(AssignmentNode lhs, AssignmentNode rhs) {
			return Double.compare(rhs.time, lhs.time);
		}
	}
}