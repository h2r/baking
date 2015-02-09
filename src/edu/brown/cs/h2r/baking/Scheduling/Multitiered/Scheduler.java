package edu.brown.cs.h2r.baking.Scheduling.Multitiered;

import java.util.List;
import java.util.Set;

import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;
import edu.brown.cs.h2r.baking.Scheduling.Multitiered.Workflow;

public interface Scheduler {
	Assignments schedule(Workflow workflow, List<String> agents, ActionTimeGenerator timeGenerator);
	Assignments finishSchedule(Workflow workflow, ActionTimeGenerator actionTimeLookup, 
			Assignments assignments, Assignments bufferedAssignments, Set<Subtask> visitedNodes);
	boolean isUsingActualValues();
}
