package edu.brown.cs.h2r.baking.Scheduling;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Scheduler {
	List<AssignedWorkflow> schedule(Workflow workflow, List<String> agents, ActionTimeGenerator  timeGenerator);
	List<AssignedWorkflow> finishSchedule(Workflow workflow, ActionTimeGenerator actionTimeLookup, 
			List<AssignedWorkflow> assignedWorkflows, Set<Workflow.Node> visitedNodes);
}
