package edu.brown.cs.h2r.baking.Scheduling.Multitiered;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;
import edu.brown.cs.h2r.baking.Scheduling.Assignment;

public class Assignments implements Iterable<Entry<String, AgentAssignment>>{
	private final Map<String, AgentAssignment> assignments;
	public Assignments(List<String> agents, ActionTimeGenerator timeGenerator, boolean useActualValues) {
		this.assignments = new HashMap<String, AgentAssignment>();
		for (String agent : agents) {
			this.assignments.put(agent, new AgentAssignment(agent, timeGenerator, useActualValues));
		}
	}
	
	public Assignments(Collection<AgentAssignment> assignments) {
		this.assignments = new HashMap<String, AgentAssignment>();
		double time = 0.0;
		for (AgentAssignment assignment : assignments) {
			this.assignments.put(assignment.getId(), assignment);
			time = Math.max(time, assignment.time());
		}
	}

	@Override
	public Iterator<Entry<String, AgentAssignment>> iterator() {
		return this.assignments.entrySet().iterator();
	}
	
	public List<AgentAssignment> getAssignments() {
		return new ArrayList<AgentAssignment>(this.assignments.values());
	}
	
	public List<String> getAgents() {
		return new ArrayList<String>(this.assignments.keySet());
	}
	
	public boolean add(Subtask subtask, String agent) {
		AgentAssignment assignment = this.assignments.get(agent);
		if (assignment == null) {
			return false;
		}
		return assignment.add(subtask);
	}
	
	public Assignments copy() {
		List<AgentAssignment> assignments = new ArrayList<AgentAssignment>();
		for (AgentAssignment assignment : this.getAssignments()) {
			assignments.add(assignment.copy());
		}
		return new Assignments(assignments);
	}
	
	public double time() {
		double time = 0.0;
		for (AgentAssignment assignment : this.assignments.values()) {
			time = Math.max(assignment.time(), time);
		}
		return time;
	}
	
	public boolean isAssigned(Subtask subtask, String agent) {
		AgentAssignment assignment = this.assignments.get(agent);
		return (assignment != null && assignment.contains(subtask));
	}
	
	public boolean isAssigned(Subtask subtask) {
		for (AgentAssignment assignment : this.assignments.values()) {
			if (assignment.contains(subtask)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isSubtaskAvailable(Subtask task, String agent, double currentTime, double actionDuration) {
		AgentAssignment agentAssignment = this.assignments.get(agent);
		if (agentAssignment == null) {
			return false;
		}
		
		if (agentAssignment.time() > currentTime) {
			return false;
		}
		
		Map<Subtask, List<Double>> completed = new HashMap<Subtask, List<Double>>();
		List<Subtask> subtasks = null; 
		for (AgentAssignment assignment : this.assignments.values()) {
			subtasks = assignment.completedSubtasks(currentTime);
			List<Double> completionTimes = assignment.completionTimes();
			double startTime = 0.0;
			for (int i = 0; i < subtasks.size(); i++) {
				Subtask subtask = subtasks.get(i);
				Double endTime = completionTimes.get(i);
				completed.put(subtask, Arrays.asList(startTime, endTime));
				startTime = endTime;
			}
		}
		
		List<Subtask> slice = this.getSubtasksAtTime(currentTime, currentTime + actionDuration );
		if (!task.isAvailable(completed.keySet()) || task.resourceConflicts(slice)) {
			return false;
		}
		
		for (TemporalConstraint constraint : task.getConstraints()) {
			List<Double> times = completed.get(constraint.subtask);
			double endTime = times.get(1);
			if (currentTime < constraint.lowerBound + endTime) {
				return false;
			}
			if (currentTime > constraint.upperBound + endTime) {
				return false;
			}
		}
		
		return true;
	}
	
	public List<Subtask> getSubtasksAtTime(double startTime, double endTime) {
		List<Subtask> subtasks = new ArrayList<Subtask>();
		
		for (AgentAssignment assignment : this.assignments.values()) {
			List<Subtask> slice = assignment.slice(startTime, endTime);
			if (slice != null) subtasks.addAll(slice);
		}
		
		return subtasks;
	}
	
	public Set<Subtask> completed(double time) {
		Set<Subtask> completed = new HashSet<Subtask>();
		for (AgentAssignment assignment : this.assignments.values()) {
			completed.addAll(assignment.completedSubtasks(time));
		}
		return completed;
	}

	public Double getAgentsAssignmentTime(String agent) {
		AgentAssignment agentsAssignment = this.assignments.get(agent);
		if (agentsAssignment == null) {
			return null;
		}
		return agentsAssignment.time();		
	}

	public void agentWaitUntil(String agent, Double endTime) {
		AgentAssignment agentsAssignment = this.assignments.get(agent);
		if (agentsAssignment == null) {
			return;
		}
		agentsAssignment.waitUntil(endTime);	
	}
	
	public Double nextTime(double seed) {
		double nextTime = Double.MAX_VALUE;
		for (AgentAssignment assignment : this.assignments.values()) {
			Double time = assignment.nextTime(seed);
			if (time != null && time > seed && time < nextTime) {
				nextTime = time;
			}
		}
		return (nextTime == Double.MAX_VALUE) ? null : nextTime;
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (Map.Entry<String, AgentAssignment> entry : this.assignments.entrySet()) {
			buffer.append(entry.getKey()).append(": ").append(entry.getValue().toString()).append("\n");
		}
		return buffer.toString();
	}
	
	public boolean allNodesAssigned(Set<Subtask> subtasks) {
		Set<Subtask> completed = new HashSet<Subtask>();
		for (AgentAssignment assignment : this.assignments.values()) {
			completed.addAll(assignment.nodes());
		}
		completed.removeAll(subtasks);
		return completed.isEmpty();
	}
	
	public Double getSubtaskEndTime(Subtask subtask) {
		for (AgentAssignment assignment : this.assignments.values()) {
			Double endTime = assignment.getEndTime(subtask);
			if (endTime != null) {
				return endTime;
			}
		}
		return null;
	}
}
