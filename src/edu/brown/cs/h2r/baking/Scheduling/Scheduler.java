package edu.brown.cs.h2r.baking.Scheduling;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Scheduler {
	List<Assignment> schedule(Workflow workflow, List<String> agents, ActionTimeGenerator  timeGenerator);
	List<Assignment> finishSchedule(Workflow workflow, ActionTimeGenerator actionTimeLookup, 
			List<Assignment> assignedWorkflows, BufferedAssignments bufferedAssignments, Set<Workflow.Node> visitedNodes);
}
