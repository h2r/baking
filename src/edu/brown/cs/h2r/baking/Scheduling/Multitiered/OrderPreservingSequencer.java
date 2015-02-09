package edu.brown.cs.h2r.baking.Scheduling.Multitiered;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;
import edu.brown.cs.h2r.baking.Scheduling.Multitiered.AgentAssignment.AssignmentIterator;

public class OrderPreservingSequencer implements Sequencer {

	private final boolean useActualValues;
	public OrderPreservingSequencer(boolean useActualValues) {
		this.useActualValues = useActualValues;
	}

	@Override
	public Assignments sequence(Assignments assignments, ActionTimeGenerator timeGenerator) {
		Assignments sequenced = new Assignments(assignments.getAgents(), timeGenerator, this.useActualValues);
		
		Map<String, Iterator<AssignedSubtask>> iterators = new HashMap<String, Iterator<AssignedSubtask>>();
		for (AgentAssignment assignment : assignments.getAssignments()) {
			iterators.put(assignment.getId(), assignment.iterator());
		}
		
		int numAdded = -1;
		while (numAdded != 0) {
			numAdded = 0;
			for (Map.Entry<String, Iterator<AssignedSubtask>> entry : iterators.entrySet()) {
				
				String agent = entry.getKey();
				AssignmentIterator it = (AssignmentIterator)entry.getValue();
				while (it.hasNext()) {
					AssignedSubtask assignedSubtask = it.next();
					Subtask subtask = assignedSubtask.getSubtask();
					GroundedAction action = subtask.getAction(agent);
					double actionDuration = timeGenerator.get(action, this.useActualValues);
					if (!this.add(sequenced, assignedSubtask.getSubtask(), agent, actionDuration)) {
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
	
	private boolean add(Assignments assignments, Subtask task, String agent, double actionDuration) {
		if (assignments.isAssigned(task, agent)) {
			return true;
		}
		Double agentsCurrentTime = this.getTimeNodeIsAvailable(assignments, task, agent, actionDuration);
		if (agentsCurrentTime == null) {
			return false;
		}
		assignments.agentWaitUntil(agent, agentsCurrentTime);
		return assignments.add(task, agent);
	}
	
	private Double getTimeNodeIsAvailable(Assignments assignments, Subtask task, String agent, double actionDuration) {
		double previous = assignments.getAgentsAssignmentTime(agent);
		Set<Subtask> completed = assignments.completed(previous);
		
		double currentTime = previous;
		List<Subtask> slice = assignments.getSubtasksAtTime(currentTime, currentTime + actionDuration);
		
		while (!task.isAvailable(completed) || task.resourceConflicts(slice)) {
			
			Double nextTime = assignments.nextTime(currentTime);
			if (nextTime == null) {
				return null;
			}
			currentTime = nextTime;
			completed.addAll(assignments.completed(previous));
			slice = assignments.getSubtasksAtTime(currentTime, currentTime + actionDuration);
			
		}
		
		return currentTime;
		
	}
}
