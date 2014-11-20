package edu.brown.cs.h2r.baking.Scheduling;

import java.util.List;
import java.util.Map;

public interface Scheduler {
	List<AssignedWorkflow> schedule(Workflow workflow, Map<String, Map<Workflow.Node, Double>>  actionTimeLookup);
}
