package edu.brown.cs.h2r.baking.Scheduling.Multitiered;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;

public class Assignments implements Iterable<Entry<String, AgentAssignment>>{
	private final Map<String, AgentAssignment> assignments;
	private final Map<Subtask, String> agentLookup;
	private final Map<Subtask, Double> completionTimes;
	private final Map<String, List<Subtask>> resourceLookup;
	private final TreeSet<Double> importantTimes;
	public Assignments(Collection<String> agents, ActionTimeGenerator timeGenerator, boolean useActualValues) {
		this.assignments = new HashMap<String, AgentAssignment>(agents.size() * 2);
		this.agentLookup = new HashMap<Subtask, String>();
		this.completionTimes = new HashMap<Subtask, Double>();
		this.resourceLookup = new HashMap<String, List<Subtask>>();
		this.importantTimes = new TreeSet<Double>();
		this.importantTimes.add(0.0);
		for (String agent : agents) {
			this.assignments.put(agent, new AgentAssignment(agent, timeGenerator, useActualValues));
		}
	}
	
	private Assignments(Map<String, AgentAssignment> assignments, Map<Subtask, String> agentLookup, Map<Subtask, Double> completionTimes, Map<String, List<Subtask>> resourceLookup, TreeSet<Double> importantTimes) {
		this.assignments = assignments;
		this.agentLookup = agentLookup;
		this.completionTimes = completionTimes;
		this.resourceLookup = new HashMap<String, List<Subtask>>(resourceLookup);
		this.importantTimes = importantTimes;
		
	}
	
	private TreeSet<Double> buildTimesQueue() {
		TreeSet<Double> timesQueue = new TreeSet<Double>();
		timesQueue.add(0.0);
		// Add in all times that are constrained by upper bounds on its children
		for (Subtask subtask : this.completionTimes.keySet()) {
			this.addConstraintDeadlineTimes(timesQueue, subtask);
		}
		
		return timesQueue;
	}

	private void addConstraintDeadlineTimes(TreeSet<Double> timesQueue,
			Subtask subtask) {
		timesQueue.add(this.completionTimes.get(subtask));
		for (Subtask child : subtask.getChildren()) {
			
			timesQueue.add(this.completionTimes.get(subtask) + child.getWait(subtask));
			
			double ub = child.getDeadline(subtask);
			for (TemporalConstraint constraint : child.getConstraints()) {
				Double constraintEndTime = this.completionTimes.get(constraint.subtask);
				if (constraintEndTime != null) {
					double time = constraintEndTime - ub;
					if (time > 0.0) {
						timesQueue.add(time);
					}
				}
			}
		}
	}

	@Override
	public Iterator<Entry<String, AgentAssignment>> iterator() {
		return this.assignments.entrySet().iterator();
	}
	
	public List<AgentAssignment> getAssignments() {
		return new ArrayList<AgentAssignment>(this.assignments.values());
	}
	
	public Set<String> getAgents() {
		return Collections.unmodifiableSet(this.assignments.keySet());
	}
	
	public boolean add(Subtask subtask, String agent) {
		if (this.agentLookup.containsKey(subtask)) {
			return false;
		}
		AgentAssignment assignment = this.assignments.get(agent);
		if (assignment == null) {
			return false;
		}
		if (!assignment.add(subtask)) {
			return false;
		}
		this.agentLookup.put(subtask, agent);
		this.completionTimes.put(subtask, assignment.time());
		this.addConstraintDeadlineTimes(this.importantTimes, subtask);
		for (String resource : subtask.getResources()) {
			List<Subtask> list = this.resourceLookup.get(resource);
			if (list == null) {
				list = new ArrayList<Subtask>();
				this.resourceLookup.put(resource, list);
			}
			list.add(subtask);
		}
		return true;
	}
	
	public boolean add(Subtask subtask, String agent, Double currentTime) {
		if (this.agentLookup.containsKey(subtask)) {
			return false;
		}
		AgentAssignment assignment = this.assignments.get(agent);
		if (assignment == null) {
			return false;
		}
		assignment.waitUntil(currentTime);
		if (!assignment.add(subtask)) {
			return false;
		}
		this.agentLookup.put(subtask, agent);
		this.completionTimes.put(subtask, assignment.time());
		this.addConstraintDeadlineTimes(this.importantTimes, subtask);
		
		for (String resource : subtask.getResources()) {
			List<Subtask> list = this.resourceLookup.get(resource);
			if (list == null) {
				list = new ArrayList<Subtask>();
				this.resourceLookup.put(resource, list);
			}
			list.add(subtask);
		}
		return true;
	}
	
	public Assignments copy() {
		Map<String, AgentAssignment> assignments = new HashMap<String, AgentAssignment>();
		for (Map.Entry<String, AgentAssignment> entry : this.assignments.entrySet()) {
			assignments.put(entry.getKey(), entry.getValue().copy());
		}
		return new Assignments(assignments, new HashMap<Subtask, String>(this.agentLookup), new HashMap<Subtask, Double>(this.completionTimes), this.resourceLookup, this.importantTimes);
	}
	
	public double time() {
		double time = 0.0;
		for (AgentAssignment assignment : this.assignments.values()) {
			time = Math.max(assignment.time(), time);
		}
		return time;
	}
	
	public boolean isAssigned(Subtask subtask) {
		return this.agentLookup.containsKey(subtask);
	}
	
	public boolean isAssigned(Subtask subtask, String agent) {
		String actualAgent = this.agentLookup.get(subtask);
		return (actualAgent != null && actualAgent.equals(agent));
	}
	
	
	public boolean isSubtaskAvailable(Subtask task, String agent, double currentTime, double actionDuration) {
		AgentAssignment agentAssignment = this.assignments.get(agent);
		if (agentAssignment == null) {
			return false;
		}
		
		if (agentAssignment.time() > currentTime) {
			return false;
		}
		
		Map<Subtask, Double> completed = new HashMap<Subtask, Double>(this.completionTimes.size() * 2);
		for (Map.Entry<Subtask, Double> entry : this.completionTimes.entrySet()) {
			double endTime = entry.getValue();
			if (endTime <= currentTime) {
				completed.put(entry.getKey(), endTime);
			}
		}
		
		List<Subtask> slice = this.getSubtasksAtTime(currentTime, currentTime + actionDuration );
		if (!task.isAvailable(completed.keySet()) || task.resourceConflicts(slice)) {
			return false;
		}
		
		if (subtaskViolateItsOwnConstraints(task, currentTime)) {
			return false;
		}
		
		return true;
	}

	public boolean subtaskViolateItsOwnConstraints(Subtask task,
			double currentTime) {
		for (TemporalConstraint constraint : task.getConstraints()) {
			Double endTime = this.completionTimes.get(constraint.subtask);
			if (currentTime < constraint.lowerBound + endTime) {
				return true;
			}
			if (currentTime > constraint.upperBound + endTime) {
				//System.err.println("This constraint can no longer be satisfied");
				return true;
			}
		}
		return false;
	}
	
	public List<Subtask> getSubtasksAtTime(double startTime, double endTime) {
		List<Subtask> subtasks = new ArrayList<Subtask>();
		
		for (AgentAssignment assignment : this.assignments.values()) {
			List<Subtask> slice = assignment.slice(startTime, endTime);
			if (slice != null) subtasks.addAll(slice);
		}
		
		return subtasks;
	}
	
	public List<Subtask> getAllSubtasks() {
		return new ArrayList<Subtask>(this.agentLookup.keySet());
	}
	
	public List<Subtask> completed(double time) {
		return this.completed(0.0, time);
	}
	
	public List<Subtask> completed(double start, double end) {
		List<Subtask> completed = new ArrayList<Subtask>();
		for (AgentAssignment assignment : this.assignments.values()) {
			completed.addAll(assignment.completedSubtasks(start, end));
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
	
	public List<Double> getImportantTimes() {
		return new ArrayList<Double>(this.importantTimes);
	}
	
	public SortedSet<Double> getImportantTimes(double startTime, double endTime) {
		if (this.importantTimes.isEmpty()) {
			return null;
		}
		return this.importantTimes.subSet(startTime, endTime);
	}

	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		for (Map.Entry<String, AgentAssignment> entry : this.assignments.entrySet()) {
			buffer.append(entry.getKey()).append(": ").append(entry.getValue().toString()).append("\n");
		}
		return buffer.toString();
	}
	
	public boolean allNodesAssigned(Collection<Subtask> subtasks) {
		if (this.agentLookup.size() != subtasks.size()) {
			return false;
		}
		for (Subtask subtask : subtasks) {
			if (!this.agentLookup.containsKey(subtask)) {
				return false;
			}
		}
		return true;
	}
	
	public Double getSubtaskEndTime(Subtask subtask) {
		return this.completionTimes.get(subtask);
	}

	public Double getSubtaskDuration(Subtask subtask) {
		String agent = this.agentLookup.get(subtask);
		if (agent == null) {
			return null;
		}
		AgentAssignment assignment = this.assignments.get(agent);
		return assignment.subtaskDuration(subtask);
	}

	public String getAgent(Subtask subtask) {
		return this.agentLookup.get(subtask);
	}

	public int size() {
		return this.agentLookup.size();
	}

	public double getEarliestTime() {
		double min = Double.MAX_VALUE;
		for (AgentAssignment assignment : this.assignments.values()) {
			min = Math.min(min, assignment.time());
		}
		return min;
	}

	public double getEarliestTimeForResource(Assignments sequenced,
			String resource) {
		List<Subtask> list = this.resourceLookup.get(resource);
		if (list == null) {
			return 0.0;
		}
		double latestTime = 0.0;
		for (Subtask subtask : list) {
			latestTime = Math.max(latestTime, this.completionTimes.get(subtask));
		}
		return latestTime;
	}
}
