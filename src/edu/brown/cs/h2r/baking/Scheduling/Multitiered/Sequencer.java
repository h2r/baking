package edu.brown.cs.h2r.baking.Scheduling.Multitiered;

import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;

public interface Sequencer {
	Assignments sequence(Assignments assignments, ActionTimeGenerator timeGenerator, Workflow workflow);
	Assignments continueSequence(Assignments sequenced, Subtask subtask, String agent,
			ActionTimeGenerator timeGenerator, Workflow workflow);
	String getDescription();
}
