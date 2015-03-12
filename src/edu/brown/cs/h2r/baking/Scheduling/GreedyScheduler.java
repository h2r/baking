package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.Experiments.SchedulingComparison;
import edu.brown.cs.h2r.baking.Scheduling.Assignment.ActionTime;
import edu.brown.cs.h2r.baking.Scheduling.Assignment.AssignmentIterator;

public class GreedyScheduler implements Scheduler {
	private final boolean useActualValues;
	private final Sequencer sequencer;
	public GreedyScheduler(boolean useActualValues) {
		this.useActualValues = useActualValues;
		this.sequencer = new BasicSequencer(this.useActualValues);
	}

	public boolean isUsingActualValues() {
		return this.useActualValues;
	}
	
	public Assignments schedule(Workflow workflow, List<String> agents,
			ActionTimeGenerator timeGenerator) {
		
		Assignments assignments = new Assignments(timeGenerator, agents, workflow.getStartState(), this.useActualValues, false);
		
		return this.finishSchedule(workflow, assignments, timeGenerator);
	}
	
	public Assignments finishSchedule(Workflow workflow, Assignments assignments, ActionTimeGenerator actionTimeLookup) {
		
		Set<Workflow.Node> visited = new HashSet<Workflow.Node>(assignments.subtasks()); 
		
		for (Workflow.Node node : workflow) {
			if (!visited.add(node)){
				continue;
			}
			
			double bestTime = Double.MAX_VALUE;
			Assignments bestSequence = null;
			
			for (String agent : assignments.agents()) {
				Assignments copied = assignments.copy();
				if (copied.add(node, agent) && copied.time() < bestTime) {
					bestTime = copied.time();
					bestSequence = copied;
				}
			}
			if (bestSequence == null) {
				/*for (String agent : assignments.agents()) {
					Assignments copied = assignments.copy();
					if (!copied.add(node, agent)) {
						System.err.println("Couldn't add " + node.toString() + " to " + agent + "'s assignment");
					}
				}*/
				return null;
				
			}
			
			assignments = bestSequence;
		}
		
		return assignments;
	}
	
	

}
