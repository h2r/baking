package edu.brown.cs.h2r.baking.Scheduling;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface Scheduler {
	Assignments schedule(Workflow workflow, List<String> agents, ActionTimeGenerator  timeGenerator);
	Assignments finishSchedule(Workflow workflow, Assignments assignments, ActionTimeGenerator actionTimeLookup);
	boolean isUsingActualValues();
}
