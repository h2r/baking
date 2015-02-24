package edu.brown.cs.h2r.baking.Scheduling.Multitiered;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import burlap.datastructures.HashIndexedHeap;
import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;

public class TercioSequencer implements Sequencer {
	private final boolean useActualValues;
	private final Map<String, Set<Subtask>> subtasksForResources, subtasksForAgents;
	private Set<Subtask> subtasksToWatch;
	public TercioSequencer(boolean useActualValues) {
		this.useActualValues = useActualValues;
		this.subtasksForAgents = new HashMap<String, Set<Subtask>>();
		this.subtasksForResources = new HashMap<String, Set<Subtask>>();
		this.subtasksToWatch = new HashSet<Subtask>();
		
	}
	
	public String getDescription() {
		return this.getClass().getSimpleName();
	}
	
	public Assignments continueSequence(Assignments sequenced, Subtask subtask, String agent, ActionTimeGenerator timeGenerator, Workflow workflow) {
		return null;
	}

	
	@Override
	public Assignments sequence(Assignments assignments, ActionTimeGenerator timeGenerator, Workflow workflow) {
		Assignments sequenced = new Assignments(assignments.getAgents(), timeGenerator, this.useActualValues);
		HashIndexedHeap<TercioNode> priorityQueue = new HashIndexedHeap<TercioNode>(new TercioNode.TercioComparator(), workflow.size() * 2);
		
		Set<Subtask> assigned = new HashSet<Subtask>(); 
		Set<Subtask> unassigned = new HashSet<Subtask>();
		Map<String, Set<Subtask>> available = new HashMap<String, Set<Subtask>>();
		HashIndexedHeap<SubtaskAvailability> nextAvailableSubtasks = 
				new HashIndexedHeap<SubtaskAvailability>(new SubtaskAvailability.SAComparator(),  workflow.size() * 2);
		PriorityQueue<Double> timesQueue = new PriorityQueue<Double>();
		timesQueue.add(0.0);
		for (AgentAssignment assignment : assignments.getAssignments()) {
			for (Subtask subtask : assignment.nodes()) {
				if (subtask != null) unassigned.add(subtask);
			}
			for (Subtask subtask : assignment.availableSubtasks(assigned)) {
				
				SubtaskAvailability sa = new SubtaskAvailability(subtask, 0.0);
				nextAvailableSubtasks.insert(sa);
			}
		}
		this.subtasksToWatch.clear();
		this.subtasksForAgents.clear();
		this.subtasksForResources.clear();
		double currentTime = -1.0;
		while (timesQueue.peek() != null) {
			boolean gotNext = false;
			while (timesQueue.peek() != null) {
				Double nextTime = timesQueue.poll();
				if (nextTime > currentTime) {
					currentTime = nextTime;
					gotNext = true;
					break;
				}
			}
			if (!gotNext) {
				System.err.println("Times queue ran out");
				return null;
			}
			this.addNextAvailable(assignments, sequenced, assigned, available, nextAvailableSubtasks, currentTime);
			priorityQueue = constructQueue(assignments, unassigned, available);
			
			this.addAll(assignments, timeGenerator, workflow, sequenced, timesQueue, priorityQueue, currentTime, assigned, unassigned, available,  nextAvailableSubtasks);
			
			if (assignments.allNodesAssigned(assigned)) {
				return sequenced;
			}
			
			if (!this.checkConstraints(sequenced, unassigned, currentTime)) {
				this.checkConstraints(sequenced, unassigned, currentTime);
				return null;
			}
			for (AgentAssignment assignment : sequenced.getAssignments()) {
				assignment.waitUntil(currentTime);
			}
		}
		if (!assignments.allNodesAssigned(assigned)) {
			return null;
		}
		return sequenced;
	}
	
	private boolean checkConstraints(Assignments sequenced, Set<Subtask> unassigned, double currentTime) {
		for (Subtask subtask : unassigned) {
			for (TemporalConstraint constraint : subtask.getConstraints()) {
				if (sequenced.isAssigned(constraint.subtask)) {
					Double endTime = sequenced.getSubtaskEndTime(constraint.subtask) + constraint.upperBound;
					if (endTime < currentTime) {
						return false;
					}
				}
			}
		}
		return true;
	}

	private void addNextAvailable(Assignments assignments, Assignments sequenced,
			Set<Subtask> assigned, Map<String, Set<Subtask>> available,
			HashIndexedHeap<SubtaskAvailability> nextAvailableSubtasks,
			double currentTime) {
		while (nextAvailableSubtasks.peek() != null) {
			SubtaskAvailability sa = nextAvailableSubtasks.poll();
			Subtask subtask = sa.getSubtask();
			if (assigned.contains(subtask)) {
				continue;
			}
			if (sa.getTime() > currentTime) {
				nextAvailableSubtasks.insert(sa);
				return;
			}
			
			String agent = assignments.getAgent(subtask);
			
			if (agent == null) {
				nextAvailableSubtasks.insert(sa);
				return;
			}
			Set<Subtask> set = available.get(agent);
			if (set == null) {
				set = new HashSet<Subtask>();
				available.put(agent, set);
			}
			set.add(subtask);
		}
	}
	
	private void addSubtaskToHashMaps(Subtask subtask, Assignments assignments) {
		for (Subtask child : subtask.getChildDeadlines()) {
			// build subtasks for agents
			String subtaskAgent = assignments.getAgent(subtask);
			Set<Subtask> subtasks = this.subtasksForAgents.get(subtaskAgent);
			if (subtasks == null) {
				subtasks = new HashSet<Subtask>();
				this.subtasksForAgents.put(subtaskAgent, subtasks);
			}
			subtasks.add(child);
			
			
			// build subtasks for resources
			for (String resource : child.getResources()) {
				subtasks = subtasksForResources.get(resource);
				if (subtasks == null) {
					subtasks = new HashSet<Subtask>();
					this.subtasksForResources.put(resource, subtasks);
				}
				subtasks.add(child);
			}
		}
		String agent = assignments.getAgent(subtask);
		Set<Subtask> subtasks = this.subtasksForAgents.get(agent);
		if (subtasks != null) {
			subtasks.remove(subtask);
		}
		for (String resource : subtask.getResources()) {
			subtasks = this.subtasksForResources.get(resource);
			if (subtasks != null) {
				subtasks.remove(subtask);
			}
		}
	}

	private int addAll(Assignments assignments,
			ActionTimeGenerator timeGenerator, Workflow workflow,
			Assignments sequenced, Queue<Double> timesQueue, HashIndexedHeap<TercioNode> priorityQueue,
			double currentTime, Set<Subtask> assigned, Set<Subtask> unassigned,
			Map<String, Set<Subtask>> available,
			HashIndexedHeap<SubtaskAvailability> nextAvailableTasks) {
		
		int numAdded = 0;
		Set<Subtask> watchedUnassigned = new HashSet<Subtask>(this.subtasksToWatch);
		for (TercioNode tNode : priorityQueue) {
			watchedUnassigned.remove(tNode.getSubtask());
		}
		for (Subtask subtask : watchedUnassigned) {
			System.out.println(subtask.toString() + " was not added");
		}
		
		while (priorityQueue.peek() != null) {
			TercioNode tNode = priorityQueue.poll();
			
			if (this.add(tNode, sequenced, assignments, assigned, unassigned, available, timesQueue, nextAvailableTasks, timeGenerator, currentTime, false)) {
				numAdded++;
			} else {
				this.subtasksToWatch.add(tNode.getSubtask());
			}
		}
		return numAdded;
	}

	private HashIndexedHeap<TercioNode> constructQueue(Assignments assignments, Set<Subtask> unassigned, 
			Map<String, Set<Subtask>> available) {
		
		HashIndexedHeap<TercioNode> priorityQueue = new HashIndexedHeap<TercioNode>(new TercioNode.TercioComparator());
		
		for (Map.Entry<String, Set<Subtask>> entry : available.entrySet()) {
			String agent = entry.getKey();
			double hA = TercioNode.computeA(agent, available);
			
			for (Subtask subtask : entry.getValue()) {
				double hR = TercioNode.computeR(subtask, unassigned);
				double hP = TercioNode.computeP(subtask, agent, assignments);
				double hD = 0.0;
				TercioNode tNode = new TercioNode(subtask, agent, hA, hR, hP, hD);
				priorityQueue.insert(tNode);
			}
		}
		return priorityQueue;
	}

	private boolean add(TercioNode tNode, Assignments sequenced, Assignments assignments, Set<Subtask> assigned, Set<Subtask> unassigned,
			Map<String, Set<Subtask>> available, Queue<Double> timesQueue, HashIndexedHeap<SubtaskAvailability> nextAvailableTasks,
			ActionTimeGenerator timeGenerator, double currentTime, boolean checkWhyNot) {
		
		Subtask subtask = tNode.getSubtask();
		String agent = tNode.getAgent();
		GroundedAction action = subtask.getAction(agent);
		double actionDuration = timeGenerator.get(action, this.useActualValues);
		
		
		if (sequenced.isAssigned(subtask, agent)) {
			return true;
		}
		
		if (currentTime + 0.00001 < sequenced.getAgentsAssignmentTime(agent)) {
			if (checkWhyNot) System.out.println("Agent is occupied at this time");
			return false;
		}
		
		if (!sequenced.isSubtaskAvailable(subtask, agent, currentTime, actionDuration)) {
			if (checkWhyNot) System.out.println("Subtask is not available");
			return false;
		}
		
		if (this.taskViolateCurrentDeadlines(sequenced, assignments, subtask, currentTime, 
				actionDuration, unassigned)) {
			if (checkWhyNot) System.out.println("This subtask violates current deadlines");
			return false;
		}
		
		if (!this.activatingDeadlinesFeasible(sequenced, assignments, currentTime, subtask, agent, assigned, unassigned)) {
			if (checkWhyNot) System.out.println("Activating this subtask would require it to violate deadlines");
			return false;
		}
		
		if (!sequenced.add(subtask, agent, currentTime)) {
			return false;
		}
		double endTime = sequenced.getSubtaskEndTime(subtask);
		if (endTime <= currentTime) {
			System.err.println("WTF");
		}
		//System.out.println(subtask.toString() + " assigned at " + currentTime + " ends at " + endTime);
		assigned.add(subtask);
		timesQueue.add(endTime);
		unassigned.remove(subtask);
		this.addSubtaskToHashMaps(subtask, assignments);
		
		available.get(agent).remove(subtask);
		this.subtasksToWatch.remove(subtask);
		
		// for those with lowerbound waits, they will become available after the lowerbound is available
		for (Subtask child : subtask.getChildren()) {
			if (unassigned.contains(child) && child.isAvailable(assigned)) {
				double latestEndTime = currentTime;
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
					timesQueue.add(latestEndTime);
					nextAvailableTasks.insert(new SubtaskAvailability(child, latestEndTime));
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
	
	/*
	 * Checks if sequencing this task will result in current deadlines being violated
	 */
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
		Set<String> allResources = new HashSet<String>();
		Collection<String> agents = assignments.getAgents();
		for (Subtask subtask : unassigned) allResources.addAll(subtask.getResources());
		// for each active and next subtask
		for (Subtask subtask : activeNext) {
			//if there is an agent conflict
			if (assignments.isAssigned(subtask, agent)) {
				// compute agent slack
				for (String a : agents) {
					subtasksForAgent = this.subtasksForAgents.get(a);
					double agentSlack = this.computeTSlackAgent(subtask, currentTime, sequenced, assignments, a, subtasksForAgent);
					//System.out.println(agent + " slack " + agentSlack);
					
					// if agent slack is less than the action duration, then we can't sequence this action
					if (0.0 > agentSlack) {
						return true;
					}
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
			/*
			for (Subtask subtask : activeNext) {
				//if there is an agent conflict
				if (assignments.isAssigned(subtask, childAgent)) {
					// compute agent slack
					subtasksForAgent = subtasksForAgents.get(childAgent);
					agentSlack = this.computeTSlackAgent(subtask, currentTime, sequenced, assignments, childAgent, subtasksForAgent);
					System.out.println(agent + " slack " + agentSlack);
					
					// if agent slack is less than the action duration, then we can't sequence this action
					if (actionDuration > agentSlack) {
						return false;
					}
				}
				
				// get the conflicting resources
				Set<String> subtaskResources = subtask.getResources();
				subtaskResources.retainAll(childResources);
				
				// for each conflicting resource
				for (String resource : subtaskResources) {
					// compute resource slack
					Set<Subtask> subtasksForResource = subtasksForResources.get(resource);
					
					double resourceSlack = this.computeTSlackResource(subtask, currentTime, sequenced, assignments, resource, subtasksForResource);
					System.out.println(resource + " slack " + resourceSlack);
					
					// if the action duration is greater than the slack, then we can't sequence this action
					if (actionDuration > resourceSlack) {
						return false;
					}
				}
			}*/
		}
		
		
		/*
		for (Subtask child : subtaskToCheck.getChildren()) {
			String constraintAgent = assignments.getAgent(child);
			subtasksForAgent = subtasksForAgents.get(constraintAgent);
			Set<String> constraintResources = child.getResources();
			
			for (TemporalConstraint childConstraint : child.getConstraints()) {
				if (!childConstraint.subtask.equals(subtaskToCheck)) {
					for (Subtask subtask : activeNext) {
						String subtaskAgent = assignments.getAgent(subtask);
						if (constraintAgent.equals(subtaskAgent)){
							
							double tMaxSubtask = this.computeTMax(subtask, currentTime, assignments);
							double tSlackChild = this.computeTSlack(child, currentTime, assignments, subtasksForAgent);
							
							double tMaxChild = this.computeTMax(child, currentTime, assignments);
							double tSlackSubtask = this.computeTSlack(subtask, currentTime, assignments, subtasksForAgent);
							if (tMaxSubtask > tSlackChild && tMaxChild > tSlackSubtask) {
								return false;
							}
							
							
							for (String resource : constraintResources) {
								Set<Subtask> subtasksForResource = subtasksForResources.get(resource);
								
								tMaxSubtask = this.computeTMax(subtask, currentTime, assignments);
								tSlackChild = this.computeTSlack(child, currentTime, assignments, subtasksForResource);
								
								tMaxChild = this.computeTMax(child, currentTime, assignments);
								tSlackSubtask = this.computeTSlack(subtask, currentTime, assignments, subtasksForResource);
								if (tMaxSubtask > tSlackChild && tMaxChild > tSlackSubtask) {
									return false;
								}
										
							}
						}
					}
				}
			}
			
		}*/
		
		return true;
	}
	
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

}
