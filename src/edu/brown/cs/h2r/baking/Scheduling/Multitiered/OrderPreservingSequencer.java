package edu.brown.cs.h2r.baking.Scheduling.Multitiered;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;
import edu.brown.cs.h2r.baking.Scheduling.Multitiered.AgentAssignment.AssignmentIterator;

public class OrderPreservingSequencer implements Sequencer {
	private final Map<String, Set<Subtask>> subtasksForResources, subtasksForAgents;
	private final TreeSet<Double> timesQueue;
	private final boolean useActualValues;
	public OrderPreservingSequencer(boolean useActualValues) {
		this.useActualValues = useActualValues;
		this.subtasksForAgents = new HashMap<String, Set<Subtask>>();
		this.subtasksForResources = new HashMap<String, Set<Subtask>>();
		this.timesQueue = new TreeSet<Double>();
	}
	
	public String getDescription() {
		return this.getClass().getSimpleName();
	}

	@Override
	public Assignments sequence(Assignments assignments, ActionTimeGenerator timeGenerator, Workflow workflow) {
		Assignments sequenced = new Assignments(assignments.getAgents(), timeGenerator, this.useActualValues);
		
		Map<String, Iterator<AssignedSubtask>> iterators = new HashMap<String, Iterator<AssignedSubtask>>();
		for (AgentAssignment assignment : assignments.getAssignments()) {
			iterators.put(assignment.getId(), assignment.iterator());
		}
		
		int numAdded = -1;
		this.subtasksForAgents.clear();
		this.subtasksForResources.clear();
		this.timesQueue.add(0.0);
		Set<Subtask> assigned = new HashSet<Subtask>();
		Set<Subtask> unassigned = new HashSet<Subtask>(assignments.getAllSubtasks());
		while (!unassigned.isEmpty()) {
			if (numAdded == 0) {
				return null;
			}
			numAdded = 0;
			for (Map.Entry<String, Iterator<AssignedSubtask>> entry : iterators.entrySet()) {
				
				String agent = entry.getKey();
				AssignmentIterator it = (AssignmentIterator)entry.getValue();
				while (it.hasNext()) {
					AssignedSubtask assignedSubtask = it.next();
					Subtask subtask = assignedSubtask.getSubtask();
					GroundedAction action = subtask.getAction(agent);
					double actionDuration = timeGenerator.get(action, this.useActualValues);
					if (!this.add(sequenced, assignments, assignedSubtask.getSubtask(), agent, assigned, unassigned, actionDuration)) {
						it.previous();
						break;
					} else {
						numAdded++;
					}
				}
			}
			
		}
		
		
		return sequenced;
	}
	public Double getEstimatedCompletionTime(Assignments sequenced, Subtask subtask, String agent,
			ActionTimeGenerator timeGenerator, Workflow workflow) {
		GroundedAction ga = subtask.getAction(agent);
		double actionDuration = timeGenerator.get(ga, false);
		Set<Subtask> assigned = new HashSet<Subtask>(sequenced.getAllSubtasks());
		Set<Subtask> unassigned = new HashSet<Subtask>(workflow.getSubtasks());
		unassigned.removeAll(assigned);
		
		if (sequenced.isAssigned(subtask, agent)) {
			return sequenced.time();
		}
		Double agentsCurrentTime =
				OrderPreservingSequencer.getTimeNodeIsAvailable(sequenced, timeGenerator, subtask, agent, assigned, unassigned, actionDuration);
		
		if (agentsCurrentTime == null) {
			return null;
		}
		
		return agentsCurrentTime + actionDuration;
	}
	
	public Assignments continueSequence(Assignments sequenced, Subtask subtask, String agent,
			ActionTimeGenerator timeGenerator, Workflow workflow) {
		sequenced = sequenced.copy();
		GroundedAction ga = subtask.getAction(agent);
		double actionDuration = timeGenerator.get(ga, false);
		Set<Subtask> assigned = new HashSet<Subtask>(sequenced.getAllSubtasks());
		Set<Subtask> unassigned = new HashSet<Subtask>(workflow.getSubtasks());
		unassigned.removeAll(assigned);
		
		if (OrderPreservingSequencer.add(sequenced, timeGenerator, subtask, agent, assigned, unassigned, actionDuration)) {
			return sequenced;
		}
		return null;
	}
	
	private boolean add(Assignments sequenced, Assignments assignments, Subtask task, String agent, Set<Subtask> assigned, Set<Subtask> unassigned, double actionDuration) {
		if (sequenced.isAssigned(task, agent)) {
			return true;
		}
		Double agentsCurrentTime = this.getTimeNodeIsAvailable(sequenced, assignments, task, agent, assigned, unassigned, actionDuration);
		if (agentsCurrentTime == null) {
			return false;
		}
		sequenced.agentWaitUntil(agent, agentsCurrentTime);
		if (!sequenced.add(task, agent)) {
			return false;
		}
		assigned.add(task);
		unassigned.remove(task);
		double endTime = sequenced.getSubtaskEndTime(task);
		timesQueue.add(endTime);
		OrderPreservingSequencer.addSubtaskToHashMaps(task, assignments, this.subtasksForAgents, this.subtasksForResources);
		// for those with lowerbound waits, they will become available after the lowerbound is available
		for (Subtask child : task.getChildren()) {
			if (unassigned.contains(child) && child.isAvailable(assigned)) {
				double latestEndTime = agentsCurrentTime;
				boolean allConstraintsSatisfied = true;
				for (TemporalConstraint constraint : child.getConstraints()) {
					Double constraintEndTime = sequenced.getSubtaskEndTime(constraint.subtask);
					if (constraintEndTime == null) {
						allConstraintsSatisfied = false;
						break;
					}
					latestEndTime = Math.max(latestEndTime, constraintEndTime + constraint.lowerBound);
				}
				if (allConstraintsSatisfied) {
					this.timesQueue.add(latestEndTime);
				}
			}					
		}
		// For the unassigned with child deadlines, they might be able available by endTime - duration
		for (Subtask unassign : unassigned) {
			if (unassign.getChildDeadlines().size() > 0) {
				double duration = assignments.getSubtaskDuration(unassign);
				timesQueue.add(endTime - duration);
			}
			
		}
		return true;
	}
	
	private static boolean add(Assignments sequenced, ActionTimeGenerator timeGenerator, Subtask task, String agent, Set<Subtask> assigned, Set<Subtask> unassigned, double actionDuration) {
		if (sequenced.isAssigned(task, agent)) {
			return true;
		}
		Double agentsCurrentTime = OrderPreservingSequencer.getTimeNodeIsAvailable(sequenced, timeGenerator, task, agent, assigned, unassigned, actionDuration);
		if (agentsCurrentTime == null) {
			return false;
		}
		sequenced.agentWaitUntil(agent, agentsCurrentTime);
		if (!sequenced.add(task, agent)) {
			return false;
		}
		assigned.add(task);
		unassigned.remove(task);
		return true;
	}
	
	private Double getTimeNodeIsAvailable(Assignments sequenced, Assignments assignments,
			Subtask task, String agent, Set<Subtask> assigned, Set<Subtask> unassigned, double actionDuration) {
		double earliestTime = sequenced.getEarliestTime();
		List<Double> toRemove = new ArrayList<Double>(this.timesQueue.size());
		
		double timeNodeIsAvailable = earliestTime;
		double timeNodeMustStart = Double.MAX_VALUE;
		for (TemporalConstraint constraint : task.getConstraints()) {
			Double endTime = sequenced.getSubtaskEndTime(constraint.subtask);
			if (endTime == null) {
				return -1.0;
			}
			if (timeNodeIsAvailable < constraint.lowerBound + endTime) {
				timeNodeIsAvailable = constraint.lowerBound + endTime;
			}
			if (timeNodeMustStart > constraint.upperBound + endTime) {
				timeNodeMustStart = constraint.upperBound + endTime;
			}
		}
		if (timeNodeMustStart < timeNodeIsAvailable) {
			return -1.0;
		}
		this.timesQueue.add(timeNodeIsAvailable);
		
		for (Double currentTime : this.timesQueue) {
			if (currentTime < earliestTime) {
				toRemove.add(currentTime);
				continue;
			}
			if (currentTime < timeNodeIsAvailable) {
				continue;
			}
			
			List<Subtask> slice = sequenced.getSubtasksAtTime(currentTime, currentTime + actionDuration);
			for (Subtask subtask : slice) {
				if (subtask != null && task.resourceConflicts(subtask)) {
					Double endTime = sequenced.getSubtaskEndTime(subtask);
					timeNodeIsAvailable = Math.max(timeNodeIsAvailable, endTime);
				}
			}
			if (timeNodeIsAvailable > timeNodeMustStart) {
				return -1.0;
			} else if (timeNodeIsAvailable > currentTime) {
				continue;
			}
			
			for (TemporalConstraint constraint : task.getConstraints()) {
				Double endTime = sequenced.getSubtaskEndTime(constraint.subtask);
				if (currentTime > constraint.upperBound + endTime) {
					//System.err.println("This constraint can no longer be satisfied");
					this.timesQueue.removeAll(toRemove);
					return -1.0;
				}
			}
	
	
			if (this.taskViolateCurrentDeadlines(sequenced, assignments, task, currentTime, actionDuration, unassigned)) {
				continue;
			}
 
			if (!this.activatingDeadlinesFeasible(sequenced, assignments, currentTime, task, agent, assigned, unassigned)) {
				continue;
			}
			this.timesQueue.removeAll(toRemove);
			return currentTime;
		}
		this.timesQueue.removeAll(toRemove);
		return null;
	}
	
	private static Double getTimeNodeIsAvailable(Assignments sequenced, ActionTimeGenerator timeGenerator,
			Subtask task, String agent, Set<Subtask> assigned, Set<Subtask> unassigned, double actionDuration) {
		double earliestTime = sequenced.getEarliestTime();
		double timeNodeIsAvailable = earliestTime;
		double timeNodeMustStart = Double.MAX_VALUE;
		for (TemporalConstraint constraint : task.getConstraints()) {
			Double endTime = sequenced.getSubtaskEndTime(constraint.subtask);
			if (endTime == null) {
				return null;
			}
			if (timeNodeIsAvailable < constraint.lowerBound + endTime) {
				timeNodeIsAvailable = constraint.lowerBound + endTime;
			}
			if (timeNodeMustStart > constraint.upperBound + endTime) {
				timeNodeMustStart = constraint.upperBound + endTime;
			}
		}
		if (timeNodeMustStart < timeNodeIsAvailable) {
			return null;
		}
		
		Set<Subtask> constrainedSubtasks = new HashSet<Subtask>();
		Map<String, Set<Subtask>> subtasksForResources = new HashMap<String, Set<Subtask>>();
		for (Subtask subtask : assigned) {
			for (Subtask child : subtask.getChildDeadlines()) {
				if (!sequenced.isAssigned(child)) {
					constrainedSubtasks.add(child);
					for (String resource : child.getResources()) {
						Set<Subtask> subtasksForResource = subtasksForResources.get(resource);
						if (subtasksForResource == null) {
							subtasksForResource = new HashSet<Subtask>();
							subtasksForResources.put(resource, subtasksForResource);
						}
						subtasksForResource.add(child);
					}
				}
			}
		}
		
		SortedSet<Double> timesQueue = sequenced.getImportantTimes(timeNodeIsAvailable, timeNodeMustStart);
		Iterator<Double> it = timesQueue.iterator();
		//for (int i = 0; i < timesQueue.size(); i++) {
		while (it.hasNext()) {
			double currentTime = it.next();
			List<Subtask> slice = sequenced.getSubtasksAtTime(currentTime, currentTime + actionDuration);
			for (Subtask subtask : slice) {
				if (subtask != null && task.resourceConflicts(subtask)) {
					Double endTime = sequenced.getSubtaskEndTime(subtask);
					timeNodeIsAvailable = Math.max(timeNodeIsAvailable, endTime);
				}
			}
			if (timeNodeIsAvailable > currentTime) {
				it = timesQueue.tailSet(timeNodeIsAvailable).iterator();
				continue;
			}
			
			for (TemporalConstraint constraint : task.getConstraints()) {
				Double endTime = sequenced.getSubtaskEndTime(constraint.subtask);
				if (currentTime > constraint.upperBound + endTime) {
					return -1.0;
				}
			}
	
			if (!OrderPreservingSequencer.allDeadlinesCanBeScheduled(sequenced, timeGenerator, agent, task, currentTime, actionDuration, unassigned, constrainedSubtasks)) {
				continue;
			}
	
			/*if (OrderPreservingSequencer.taskViolateCurrentDeadlines(sequenced, timeGenerator, agent, task, currentTime, actionDuration, unassigned, constrainedSubtasks, subtasksForResources)) {
				continue;
			}
 
			if (!OrderPreservingSequencer.activatingDeadlinesFeasible(sequenced, timeGenerator, currentTime, task, agent, assigned, unassigned, constrainedSubtasks, subtasksForResources)) {
				continue;
			}*/
			return currentTime;
		}
		return null;
	}
	
	
	
	private static void addSubtaskToHashMaps(Subtask subtask, Assignments assignments, Map<String, Set<Subtask>> subtasksForAgents, Map<String, Set<Subtask>> subtasksForResources ) {
		for (Subtask child : subtask.getChildDeadlines()) {
			// build subtasks for agents
			String subtaskAgent = assignments.getAgent(subtask);
			Set<Subtask> subtasks = subtasksForAgents.get(subtaskAgent);
			if (subtasks == null) {
				subtasks = new HashSet<Subtask>();
				subtasksForAgents.put(subtaskAgent, subtasks);
			}
			subtasks.add(child);
			
			
			// build subtasks for resources
			for (String resource : child.getResources()) {
				subtasks = subtasksForResources.get(resource);
				if (subtasks == null) {
					subtasks = new HashSet<Subtask>();
					subtasksForResources.put(resource, subtasks);
				}
				subtasks.add(child);
			}
		}
		String agent = assignments.getAgent(subtask);
		Set<Subtask> subtasks =subtasksForAgents.get(agent);
		if (subtasks != null) {
			subtasks.remove(subtask);
		}
		for (String resource : subtask.getResources()) {
			subtasks = subtasksForResources.get(resource);
			if (subtasks != null) {
				subtasks.remove(subtask);
			}
		}
	}
	
	private boolean taskViolateCurrentDeadlines(Assignments sequenced, Assignments assignments, 
			Subtask taskToCheck, double currentTime, double actionDuration, Collection<Subtask> unassigned) {
		String agent = assignments.getAgent(taskToCheck);
		Set<Subtask> activeNext = new HashSet<Subtask>();
		for (Subtask subtask : unassigned) {
			Collection<TemporalConstraint> constraints = subtask.getConstraints();
			boolean activeAndNext = !constraints.isEmpty();
			for (TemporalConstraint constraint : constraints) {
				if (unassigned.contains(constraint.subtask) || constraint.upperBound == Double.MAX_VALUE) {
					activeAndNext = false;
				}
			}
			if (activeAndNext) {
				activeNext.add(subtask);
			}
		}
		activeNext.remove(taskToCheck);
		// for each subtask that's unassigned
		
		Set<Subtask> subtasksForAgent = subtasksForAgents.get(agent);
		Set<String> allResources = new HashSet<String>(activeNext.size() * 2 * 2);
		Collection<String> agents = sequenced.getAgents();
		for (Subtask subtask : activeNext) allResources.addAll(subtask.getResources());
		// for each active and next subtask
		for (Subtask subtask : activeNext) {
			//if there is an agent conflict
			
			for (String a : agents) {
				subtasksForAgent = this.subtasksForAgents.get(a);
				double agentSlack = this.computeTSlackAgent(subtask, currentTime, sequenced, assignments, a, subtasksForAgent);
				//System.out.println(agent + " slack " + agentSlack);
				
				// if agent slack is less than the action duration, then we can't sequence this action
				if (0.0 > agentSlack) {
					return true;
				}
			}
			
			
			
			// for each conflicting resource
			for (String resource : allResources) {
				// compute resource slack
				Set<Subtask> subtasksForResource = subtasksForResources.get(resource);
				double resourceSlack = this.computeTSlackResource(subtask, currentTime, sequenced, assignments, resource, subtasksForResource);
				//System.out.println(resource + " slack " + resourceSlack);
				
				// if the action duration is greater than the slack, then we can't sequence this action
				if (0.0 > resourceSlack) {
					return true;
				}
			}
			
		}
		return false;
	}
	
	private static boolean allDeadlinesCanBeScheduled(Assignments sequenced, ActionTimeGenerator timeGenerator, String agent,
			Subtask taskToCheck, double currentTime, double actionDuration, Collection<Subtask> unassigned,
			Set<Subtask> constrainedSubtasks) {
		
		Map<String, Set<Subtask>> subtasksForAgents = new HashMap<String, Set<Subtask>>();
		Map<String, Set<Subtask>> subtasksForResources = new HashMap<String, Set<Subtask>>();
		Map<Subtask, Double> durations = new HashMap<Subtask, Double>();
		
		for (String a : sequenced.getAgents()) {
			subtasksForAgents.put(a, new HashSet<Subtask>());
		}
		subtasksForAgents.get(agent).add(taskToCheck);
		durations.put(taskToCheck, actionDuration);
		Set<Subtask> activeNext = new HashSet<Subtask>(constrainedSubtasks);
		activeNext.remove(taskToCheck);
		activeNext.addAll(taskToCheck.getChildDeadlines());
		
		Set<String> allResources = new HashSet<String>(activeNext.size() * 2 * 2);
		Collection<String> agents = sequenced.getAgents();
		for (Subtask subtask : activeNext) {
			Collection<String> resources = subtask.getResources();
			allResources.addAll(resources);
			for (String resource : resources) {
				Set<Subtask> list = subtasksForResources.get(resource);
				if (list == null) {
					list = new HashSet<Subtask>();
					subtasksForResources.put(resource, list);
				}
				list.add(subtask);
			}
			durations.put(subtask, subtask.getMaxDuration(timeGenerator, agents));
		}
		
		
		
		for (Subtask subtask : activeNext) {
			//if there is an agent conflict
			
			Double bestSlack = -Double.MAX_VALUE;
			String bestAgent = null;
			for (String a : agents) {
				Set<Subtask>subtasksForAgent = subtasksForAgents.get(a);
				List<Double> durationsForAgent = new ArrayList<Double>(subtasksForAgent.size());
				for (Subtask st : subtasksForAgent) durationsForAgent.add(durations.get(st));
				double start = Math.max(0.0, sequenced.getAgentsAssignmentTime(a));
				double agentSlack = OrderPreservingSequencer.computeWorstCaseSlack(currentTime, start, sequenced, subtasksForAgent, durationsForAgent);
				//System.out.println(agent + " slack " + agentSlack);
				
				// if agent slack is less than the action duration, then we can't sequence this action
				if (0.0 > agentSlack) {
					return true;
				}
				if (agentSlack > bestSlack) {
					bestSlack = agentSlack;
					bestAgent = a;
				}
			}
			subtasksForAgents.get(bestAgent).add(subtask);
			GroundedAction ga = subtask.getAction(bestAgent);
			durations.put(subtask, timeGenerator.get(ga, false));
			// for each conflicting resource
			for (String resource : allResources) {
				// compute resource slack
				Set<Subtask> subtasksForResource = subtasksForResources.get(resource);
				List<Double> durationsForResource = new ArrayList<Double>(subtasksForResource.size());
				for (Subtask st : subtasksForResource) durationsForResource.add(durations.get(st));
				double start = Math.max(0.0, sequenced.getEarliestTimeForResource(sequenced, resource) - currentTime);
				double resourceSlack = OrderPreservingSequencer.computeWorstCaseSlack(currentTime, start, sequenced, subtasksForResource, durationsForResource);
				//System.out.println(resource + " slack " + resourceSlack);
				
				// if the action duration is greater than the slack, then we can't sequence this action
				if (0.0 > resourceSlack) {
					return true;
				}
			}
			
		}
		
		return true;
	}
	
	
	/*
	private static boolean taskViolateCurrentDeadlines(Assignments sequenced, ActionTimeGenerator timeGenerator, String agent,
			Subtask taskToCheck, double currentTime, double actionDuration, Collection<Subtask> unassigned,
			Set<Subtask> constrainedSubtasks, Map<String, Set<Subtask>> subtasksForResources) {
		Set<Subtask> activeNext = new HashSet<Subtask>(constrainedSubtasks);
		Map<String, Set<Subtask>> subtasksForAgents = new HashMap<String, Set<Subtask>>();
		for (String a : sequenced.getAgents()) {
			subtasksForAgents.put(a, new HashSet<Subtask>());
		}
		activeNext.remove(taskToCheck);
		// for each subtask that's unassigned
		
		Set<String> allResources = new HashSet<String>(activeNext.size() * 2 * 2);
		Collection<String> agents = sequenced.getAgents();
		for (Subtask subtask : activeNext) allResources.addAll(subtask.getResources());
		// for each active and next subtask
		for (Subtask subtask : activeNext) {
			//if there is an agent conflict
			
			Double bestSlack = -Double.MAX_VALUE;
			String bestAgent = null;
			for (String a : agents) {
				Set<Subtask>subtasksForAgent = subtasksForAgents.get(a);
				double agentSlack = OrderPreservingSequencer.computeWorstCaseSlack(currentTime, sequenced, subtasksForAgent);
				//System.out.println(agent + " slack " + agentSlack);
				
				// if agent slack is less than the action duration, then we can't sequence this action
				if (0.0 > agentSlack) {
					return true;
				}
				if (agentSlack > bestSlack) {
					bestSlack = agentSlack;
					bestAgent = a;
				}
			}
			subtasksForAgents.get(bestAgent).add(subtask);
			
			// for each conflicting resource
			for (String resource : allResources) {
				// compute resource slack
				Set<Subtask> subtasksForResource = subtasksForResources.get(resource);
				double resourceSlack = OrderPreservingSequencer.computeWorstCaseSlack(currentTime, sequenced, subtasksForResource);
				//System.out.println(resource + " slack " + resourceSlack);
				
				// if the action duration is greater than the slack, then we can't sequence this action
				if (0.0 > resourceSlack) {
					return true;
				}
			}
			
		}
		return false;
	}*/
	
	/*
	 * Checks if activating the deadlines for this subtask will result in any conflicts
	 */
	private boolean activatingDeadlinesFeasible(Assignments sequenced, Assignments assignments, double currentTime,
			Subtask subtaskToCheck, String agent, Collection<Subtask> assigned, Collection<Subtask> unassigned) {
		
		List<Subtask> activeNext = new ArrayList<Subtask>();
		
		Map<String, Set<Subtask>>subtasksForAgents = new HashMap<String, Set<Subtask>>(this.subtasksForAgents);
		Map<String, Set<Subtask>>subtasksForResources = new HashMap<String, Set<Subtask>>(this.subtasksForResources);
		sequenced = sequenced.copy();
		sequenced.add(subtaskToCheck, agent);
		
		Set<Subtask> subtasksForAgent = subtasksForAgents.get(agent);
		if (subtasksForAgent == null) {
			subtasksForAgent = new HashSet<Subtask>();
		} else {
			subtasksForAgent = new HashSet<Subtask>(subtasksForAgent);
		}
		subtasksForAgent.add(subtaskToCheck);
		subtasksForAgents.put(agent, subtasksForAgent);
		
		for (Subtask subtask : unassigned) {
			Collection<TemporalConstraint> constraints = subtask.getConstraints();
			boolean activeAndNext = !constraints.isEmpty();
			for (TemporalConstraint constraint : constraints) {
				if (!assigned.contains(constraint.subtask) || constraint.upperBound == Double.MAX_VALUE) {
					activeAndNext = false;
				}
			}
			if (activeAndNext) {
				activeNext.add(subtask);
			}
		}
		
		Set<String> allResources = new HashSet<String>();
		for (Subtask subtask : activeNext) allResources.addAll(subtask.getResources());
		
		for (Subtask child : subtaskToCheck.getChildDeadlines()) {
			if (!assignments.isAssigned(child)) {
				continue;
			}
			
			String childAgent = assignments.getAgent(child);
			subtasksForAgent = subtasksForAgents.get(childAgent);
			double agentSlack = this.computeTSlackAgent(child, currentTime, sequenced, assignments, childAgent, subtasksForAgent);
			//System.out.println(a + " slack " + agentSlack);
			
			// if agent slack is less than the action duration, then we can't sequence this action
			if (0.0 > agentSlack) {
				return false;
			}
			for (String a : assignments.getAgents()) {
				subtasksForAgent = subtasksForAgents.get(a);
			}
			
			for (String resource : child.getResources()) {
				// compute resource slack
				Set<Subtask> subtasksForResource = subtasksForResources.get(resource);
				
				double resourceSlack = this.computeTSlackResource(child, currentTime, sequenced, assignments, resource, subtasksForResource);
				//System.out.println(resource + " slack " + resourceSlack);
				
				// if the action duration is greater than the slack, then we can't sequence this action
				if (0.0 > resourceSlack) {
					return false;
				}
			}
		}
		
		return true;
	}
	
	/*
	private static boolean activatingDeadlinesFeasible(Assignments sequenced, ActionTimeGenerator timeGenerator, double currentTime,
			Subtask subtaskToCheck, String agent, Collection<Subtask> assigned, Collection<Subtask> unassigned,
			Set<Subtask> constrainedSubtasks, Map<String, Set<Subtask>> subtasksForResources) {
		
		List<Subtask> activeNext = new ArrayList<Subtask>(constrainedSubtasks);
		Map<String, Set<Subtask>> subtasksForAgents = new HashMap<String, Set<Subtask>>();
		for (String a : sequenced.getAgents()) {
			subtasksForAgents.put(a, new HashSet<Subtask>());
		}
		subtasksForAgents.get(agent).add(subtaskToCheck);
		
		subtasksForResources = new HashMap<String, Set<Subtask>>(subtasksForResources);
		sequenced = sequenced.copy();
		sequenced.add(subtaskToCheck, agent);
		
		Set<String> allResources = new HashSet<String>();
		for (Subtask subtask : activeNext) allResources.addAll(subtask.getResources());
		
		
		for (Subtask child : subtaskToCheck.getChildDeadlines()) {
			// These children haven't been allocated yet, so we assume that they are assigned to the agents with the most slack
			double bestSlack = -Double.MAX_VALUE;
			String bestAgent = null;
			for (Map.Entry<String, Set<Subtask>> entry : subtasksForAgents.entrySet()) {
				String childAgent = entry.getKey();
				Set<Subtask> subtasksForAgent = entry.getValue();
				double agentSlack = OrderPreservingSequencer.computeWorstCaseSlack(currentTime, sequenced, subtasksForAgent);
				if (agentSlack > bestSlack) {
					bestSlack = agentSlack;
					bestAgent = childAgent;
				}
			}
			//System.out.println(a + " slack " + agentSlack);
			
			// if agent slack is less than the action duration, then we can't sequence this action
			if (0.0 > bestSlack) {
				return false;
			}
			subtasksForAgents.get(bestAgent).add(child);
			for (String resource : child.getResources()) {
				// compute resource slack
				Set<Subtask> subtasksForResource = subtasksForResources.get(resource);
				
				double resourceSlack = OrderPreservingSequencer.computeWorstCaseSlack(currentTime, sequenced, subtasksForResource);
				//System.out.println(resource + " slack " + resourceSlack);
				
				// if the action duration is greater than the slack, then we can't sequence this action
				if (0.0 > resourceSlack) {
					return false;
				}
			}
		}
		
		return true;
	}*/
	
	private double computeTMaxAgent(Subtask child, double currentTime, String agent, Assignments sequenced, Assignments assignments) {
		double minTMax = Double.MAX_VALUE;
		Queue<TemporalConstraint> subtasksToVisit = new LinkedList<TemporalConstraint>();
		subtasksToVisit.addAll(child.getConstraints());
		
		// for each child and their children
		while (subtasksToVisit.peek() != null) {
			TemporalConstraint constraint = subtasksToVisit.poll();
			String constraintAgent = assignments.getAgent(constraint.subtask);
			// endTime = sequenced.getAgentsAssignmentTime(constraintAgent) + assignments.getSubtaskDuration(constraint.subtask);
			if (sequenced.isAssigned(constraint.subtask)) {
				Double endTime = sequenced.getSubtaskEndTime(constraint.subtask);
				double tMax = constraint.upperBound + endTime;
				minTMax = Math.min(minTMax, tMax);
				
			}
			if (assignments.getAgent(constraint.subtask).equals(agent)) {
				if (!sequenced.isAssigned(constraint.subtask)){
					subtasksToVisit.addAll(constraint.subtask.getConstraints());
				}
			} 
		}
		
		
		return minTMax - currentTime;
	}
	
	private static double computeWorstCaseMax(double currentTime, Assignments sequenced, Collection<Subtask> subtasksForAgent) {
		double minTMax = Double.MAX_VALUE;
		for (Subtask subtask : subtasksForAgent) {
			for (TemporalConstraint constraint : subtask.getConstraints()){
				Double endTime = sequenced.getSubtaskEndTime(constraint.subtask);
				if (endTime != null) {
					double tMax = constraint.upperBound + endTime;
					minTMax = Math.min(minTMax, tMax);
				}
			}
		}
		return minTMax - currentTime;
		
		/*
		Queue<TemporalConstraint> subtasksToVisit = new LinkedList<TemporalConstraint>();
		subtasksToVisit.addAll(child.getConstraints());
		
		// for each child and their children
		while (subtasksToVisit.peek() != null) {
			TemporalConstraint constraint = subtasksToVisit.poll();
			// endTime = sequenced.getAgentsAssignmentTime(constraintAgent) + assignments.getSubtaskDuration(constraint.subtask);
			if (sequenced.isAssigned(constraint.subtask)) {
				Double endTime = sequenced.getSubtaskEndTime(constraint.subtask);
				double tMax = constraint.upperBound + endTime;
				minTMax = Math.min(minTMax, tMax);
				
			}
			if (!sequenced.isAssigned(constraint.subtask)){
				subtasksToVisit.addAll(constraint.subtask.getConstraints());
			}
		}*/
		
		
		//return minTMax - currentTime;
	}
	
	private double computeTMaxResource(Subtask child, double currentTime, String resource, Assignments sequenced, Assignments assignments) {
		double minTMax = Double.MAX_VALUE;
		Queue<TemporalConstraint> subtasksToVisit = new LinkedList<TemporalConstraint>();
		subtasksToVisit.addAll(child.getConstraints());
		
		// for each child and their children
		while (subtasksToVisit.peek() != null) {
			TemporalConstraint constraint = subtasksToVisit.poll();
			if (constraint.subtask.getResources().contains(resource)) {
				Double endTime = currentTime + assignments.getSubtaskDuration(constraint.subtask);
				if (sequenced.isAssigned(constraint.subtask)) {
					endTime = sequenced.getSubtaskEndTime(constraint.subtask);
				}
				double tMax = constraint.upperBound + endTime;
				minTMax = Math.min(minTMax, tMax);
			} 
			if (!sequenced.isAssigned(constraint.subtask)){
				subtasksToVisit.addAll(constraint.subtask.getConstraints());
			}
		}
		
		
		return minTMax - currentTime;
	}
	
	
	
	// Compute minimum time needed to accomplish child, and unfulfilled constraints for this agent
	private double computeTMinAgent(Subtask subtask, double currentTime, Assignments sequenced, Assignments assignments, String agent, Collection<Subtask> subtasksForAgent) {
		double sum = Math.max(0.0, sequenced.getAgentsAssignmentTime(agent) - currentTime);
		if (subtasksForAgent == null) {
			subtasksForAgent = new ArrayList<Subtask>();
		}
		// if any subtasks are already allocated to this agent or resource, then they are added in
		for (Subtask other : subtasksForAgent) {
			if (!subtask.equals(other)) {
				Double duration = assignments.getSubtaskDuration(other);
				if (duration != null) {
					sum += duration;
				}
				
			}
		}
		
		Queue<TemporalConstraint> subtasksToVisit = new LinkedList<TemporalConstraint>();
		for (TemporalConstraint constraint : subtask.getConstraints()) {
			if (constraint.upperBound < Double.MAX_VALUE) subtasksToVisit.add(constraint);
		}
		
		// for each descendent of the subtask, figure out how much time is needed to accomplish them
		while (subtasksToVisit.peek() != null) {
			TemporalConstraint constraint = subtasksToVisit.poll();
			if (assignments.getAgent(constraint.subtask).equals(agent)) {
				if (!subtasksForAgent.contains(constraint.subtask) && !sequenced.isAssigned(constraint.subtask)){ 
					sum += assignments.getSubtaskDuration(constraint.subtask) + constraint.lowerBound;
				}
			}
			for (TemporalConstraint c : constraint.subtask.getConstraints()) {
				if (constraint.upperBound < Double.MAX_VALUE) subtasksToVisit.add(c);
			}
		}
		
		return sum;
	}
	
	private static double computeWorstCaseMin(double start, Collection<Double> durations) {
		double sum = start;
		for (Double duration : durations) {
			sum += duration;
		}
		
		return sum;
	}
	
	
	// Compute minimum time needed to accomplish child, and unfulfilled constraints for this resource
		private double computeTMinResource(Subtask subtask, double currentTime, Assignments sequenced, Assignments assignments, String resource, Collection<Subtask> subtasksForResource) {
			double sum = 0.0;//Math.max(0.0, b)
			if (subtasksForResource == null) {
				subtasksForResource = new ArrayList<Subtask>();
			}
			// if any subtasks are already allocated to this agent or resource, then they are added in
			for (Subtask other : subtasksForResource) {
				if (!subtask.equals(other)) {
					Double duration = assignments.getSubtaskDuration(other);
					if (duration != null) {
						sum += duration;
					}
					
				}
			}
			
			Queue<TemporalConstraint> subtasksToVisit = new LinkedList<TemporalConstraint>();
			for (TemporalConstraint constraint : subtask.getConstraints()) {
				if (constraint.upperBound < Double.MAX_VALUE) subtasksToVisit.add(constraint);
			}
			
			// for each descendent of the subtask, figure out how much time is needed to accomplish them
			while (subtasksToVisit.peek() != null) {
				TemporalConstraint constraint = subtasksToVisit.poll();
				if (constraint.subtask.getResources().contains(resource)) {
					if (!subtasksForResource.contains(constraint.subtask) && !sequenced.isAssigned(constraint.subtask)){ 
						sum += assignments.getSubtaskDuration(constraint.subtask) + constraint.lowerBound;
					}
				}
				for (TemporalConstraint c : constraint.subtask.getConstraints()) {
					if (constraint.upperBound < Double.MAX_VALUE) subtasksToVisit.add(c);
				}
			}
			
			return sum;
		}
		
		
		private double computeTSlackAgent(Subtask subtask, double currentTime, Assignments sequenced, Assignments assignments, String agent, Collection<Subtask> subtasksForAgent) {
			
			return this.computeTMaxAgent(subtask, currentTime, agent, sequenced, assignments) - 
					this.computeTMinAgent(subtask, currentTime, sequenced, assignments, agent, subtasksForAgent);
		}
		
		private double computeTSlackResource(Subtask subtask, double currentTime, Assignments sequenced, Assignments assignments, String resource, Collection<Subtask> subtasksForResource) {
			
			return this.computeTMaxResource(subtask, currentTime, resource, sequenced, assignments) - 
					this.computeTMinResource(subtask, currentTime, sequenced, assignments, resource, subtasksForResource);
		}
		
		private static double computeWorstCaseSlack(double currentTime, double start, Assignments sequenced, Collection<Subtask> subtasks, Collection<Double> durations) {
			
			return OrderPreservingSequencer.computeWorstCaseMax(currentTime, sequenced, subtasks) - 
					OrderPreservingSequencer.computeWorstCaseMin(start, durations);
		}
		
		
}
