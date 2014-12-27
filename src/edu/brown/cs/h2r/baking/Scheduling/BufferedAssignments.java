package edu.brown.cs.h2r.baking.Scheduling;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.brown.cs.h2r.baking.Scheduling.Assignment.ActionTime;
import edu.brown.cs.h2r.baking.Scheduling.Assignment.AssignmentIterator;

public class BufferedAssignments {
	private final Map<String, Assignment> adjustedAssignments;
	private double time;
	private double earliestTime;
	private Set<Workflow.Node> completedAtEarliest; 
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.time).append(", ");
		builder.append(adjustedAssignments.toString());
		
		return builder.toString();
	}
	
	public String visualString() {
		StringBuilder builder = new StringBuilder();
		for (Assignment assignedWorkflow : this.adjustedAssignments.values()) {
			for (ActionTime time : assignedWorkflow) {
				int duration = (int)(time.getTime() * 10);
				String label = (time.getNode() == null ) ? "." : time.getNode().toString();
				int length = duration * 3;
				length = Math.max(1, length);
				if (length > 0) {
					label = String.format("%" + length + "s", label);
					builder.append(label.replace(' ', '.'));
				}
				
			}
			builder.append("\n");
		}
		return builder.toString();
	}
	public BufferedAssignments() {
		this.adjustedAssignments = new HashMap<String, Assignment>();
		this.completedAtEarliest = new HashSet<Workflow.Node>();
		this.earliestTime = 0.0;
	}
	
	public BufferedAssignments(Collection<Assignment> assignments) {
		this.adjustedAssignments = new HashMap<String, Assignment>();
		this.completedAtEarliest = new HashSet<Workflow.Node>();
		this.earliestTime = 0.0;
		
		
		this.buildAdjustedAssignments(assignments);
		this.updateEarliest();
	}

	private void buildAdjustedAssignments(Collection<Assignment> assignments) {
		
		boolean keepGoing = true;
		Map<String, AssignmentIterator> iterators = new HashMap<String, AssignmentIterator>();
		for (Assignment assignment : assignments) {
			String agent = assignment.getId();
			iterators.put(agent, (AssignmentIterator)assignment.iterator());
			this.adjustedAssignments.put(agent, new Assignment(agent));
			
		}
		while (keepGoing) {
			keepGoing = false;
			
			for (Map.Entry<String, AssignmentIterator> entry : iterators.entrySet()) {
				
				String agent = entry.getKey();
				AssignmentIterator it = entry.getValue();
				while (it.hasNext()) {
					ActionTime actionTime = it.next();
					if (!this.add(actionTime.getNode(), actionTime.getTime(), agent)) {
						it.previous();
						break;
					}
				}
				keepGoing |= it.hasNext();
			}
		}		
	}
	
	public BufferedAssignments(BufferedAssignments other) {
		this.adjustedAssignments = SchedulingHelper.copyMap(other.adjustedAssignments);
		this.time = other.time;
		this.earliestTime = other.earliestTime;
		this.completedAtEarliest = new HashSet<Workflow.Node>(other.completedAtEarliest);
	}
	
	public BufferedAssignments copy() {
		return new BufferedAssignments(this);
	}
	
	public boolean add(Workflow.Node node, Double actionTime, String agent) {
		Assignment assignment = this.adjustedAssignments.get(agent);
		if (assignment == null) {
			assignment = new Assignment(agent);
			this.adjustedAssignments.put(agent, assignment);
		}
		double assignmentTime = assignment.time();
		boolean isEarliestAssignment = (assignmentTime == this.earliestTime);
		double time = this.getTimeNodeIsAvailable(node, assignmentTime);
		if (time > assignmentTime) {
			assignment.add(null, time - assignmentTime);
		} else if (time < 0.0) {
			return false;
		}
		assignment.add(node, actionTime);
		if (isEarliestAssignment) {
			this.updateEarliest();
		}
		this.time = Math.max(this.time, assignmentTime + actionTime);
		return true;
	}
	
	private void updateEarliest() {
		double previous = this.earliestTime;
		this.earliestTime = Double.MAX_VALUE;
		for (Assignment workflow : this.adjustedAssignments.values()) {
			this.earliestTime = Math.min(this.earliestTime, workflow.time());
		}
		
		List<Workflow.Node> nodes = null; 
		for (Assignment workflow : this.adjustedAssignments.values()) {
			nodes = workflow.nodes(previous, this.earliestTime);
			if (nodes != null) {
				this.completedAtEarliest.addAll(nodes);
			}
		}
	}
	
	private double getTimeNodeIsAvailable(Workflow.Node node, double seed) {
		Set<Workflow.Node> completed = new HashSet<Workflow.Node>(this.completedAtEarliest);
		double previous = this.earliestTime;
		double currentTime = seed;
		List<Workflow.Node> nodes = null; 
		for (Assignment workflow : this.adjustedAssignments.values()) {
			nodes = workflow.nodes(previous, currentTime);
			if (nodes != null) {
				completed.addAll(nodes);
			}
		}
		nodes = null; 
		while (!node.isAvailable(completed)) {
			
			double nextTime = Double.MAX_VALUE;
			for (Assignment workflow : this.adjustedAssignments.values()) {
				Double time = workflow.nextTime(currentTime);
				if (time != null && time > currentTime && time < nextTime) {
					nextTime = time;
				}
			}
			if (nextTime == Double.MAX_VALUE) {
				return -1.0;
			}
			currentTime = nextTime;
			
			for (Assignment workflow : this.adjustedAssignments.values()) {
				nodes = workflow.nodes(previous, currentTime);
				if (nodes != null) {
					completed.addAll(nodes);
				}
			}
		}
		
		return currentTime;
		
	}
	
	public Double getTimeAssigningNodeToAgent(Workflow.Node node, double actionTime, String agent) {
		Assignment assignment = this.adjustedAssignments.get(agent);
		if (assignment == null) {
			return null;
		}
		double assignmentTime = assignment.time();
		double time = this.getTimeNodeIsAvailable(node, assignmentTime);
		
		time = Math.max(time, assignmentTime);
		return time + actionTime;
	}
	
	public double time() {
		return this.time;
	}
	
	public int size() {
		return this.adjustedAssignments.size();
	}

	public Map<String, Assignment> getAssignmentMap() {
		return SchedulingHelper.copyMap(this.adjustedAssignments);
	}

	
}
