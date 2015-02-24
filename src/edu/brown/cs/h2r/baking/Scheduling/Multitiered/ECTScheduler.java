package edu.brown.cs.h2r.baking.Scheduling.Multitiered;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;

public class ECTScheduler implements Scheduler {
	private final boolean useActualValues;
	private final OrderPreservingSequencer sequencer;
	public ECTScheduler(OrderPreservingSequencer sequencer, boolean useActualValues) {
		this.useActualValues = useActualValues;
		this.sequencer = sequencer;
	}

	@Override
	public Assignments schedule(Workflow workflow, List<String> agents,
			ActionTimeGenerator timeGenerator) {
		Assignments assignments = new Assignments(agents, timeGenerator, this.useActualValues);
		return this.assignActions(workflow, assignments, timeGenerator);
	}

	@Override
	public Assignments finishSchedule(Workflow workflow,
			Assignments assignments, ActionTimeGenerator timeGenerator) {
		return this.assignActions(workflow, assignments.copy(), timeGenerator);
	}
	
	private Assignments assignActions(Workflow workflow,
			Assignments assignments, ActionTimeGenerator timeGenerator) {
		
		Set<Subtask> visited = new HashSet<Subtask>(assignments.getAllSubtasks());
		Assignments completedSequenced = assignments;
		while (!visited.containsAll(workflow.getSubtasks())) {
			Subtask bestSubtask = null;
			Double bestTime = Double.MAX_VALUE;
			for (Subtask subtask : workflow.getAvailableSubtasks(visited)) {
				for (String agent : assignments.getAgents()) {
					Double sequencedTime = this.sequencer.getEstimatedCompletionTime(assignments, subtask, agent, timeGenerator, workflow);
					if (sequencedTime != null && sequencedTime < bestTime) {
						Assignments sequenced = this.sequencer.continueSequence(assignments, subtask, agent, timeGenerator, workflow);
						bestTime = sequenced.time();
						bestSubtask = subtask;
						completedSequenced = sequenced;
					} else if (sequencedTime == null){
						sequencedTime = this.sequencer.getEstimatedCompletionTime(assignments, subtask, agent, timeGenerator, workflow);
						
						
						//System.err.println("Actions not sequenced " + copy.getAllSubtasks().size());
					}
				}
			}
			if (bestSubtask != null) {
				assignments = completedSequenced;
				visited.add(bestSubtask);
			} else {
				return null;
			}
		}
		
		return completedSequenced;
	}

	@Override
	public boolean isUsingActualValues() {
		return this.useActualValues;
	}
	
	public String getDescription() {
		return this.getClass().getSimpleName() + " - " + this.sequencer.getDescription();
	}

}
