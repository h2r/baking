package edu.brown.cs.h2r.baking.Scheduling;

public abstract class Sequencer {
	protected final boolean useActualValues;
	public Sequencer(boolean useActualValues) {
		this.useActualValues = useActualValues;
	}
	public Assignments sequence(Assignments assignments, ActionTimeGenerator timeGenerator, Workflow workflow) {
		Assignments sequenced = 
				new Assignments(timeGenerator, assignments.agents(), assignments.getStartState(), this.useActualValues, false);
		return this.finishSequence(assignments, sequenced, timeGenerator);
	}
	
	public Assignments continueSequence(Assignments sequenced, Workflow.Node subtask, String agent,
			ActionTimeGenerator timeGenerator, Workflow workflow) {
		if (!sequenced.add(subtask, agent)) {
			return null;
		}
		return sequenced;
	}
	
	public abstract Assignments finishSequence(Assignments assignments, Assignments sequenced, ActionTimeGenerator timeGenerator);
	public abstract String getDescription();
}
