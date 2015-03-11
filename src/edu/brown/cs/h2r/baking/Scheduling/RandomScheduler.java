package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class RandomScheduler implements Scheduler {
	Random random = new Random();
	private boolean useActualValues;
	
	public RandomScheduler(boolean useActualValues) {
		this.useActualValues = useActualValues;
	}
	
	public boolean isUsingActualValues() {
		return this.useActualValues;
	}
	
	@Override
	public Assignments schedule(Workflow workflow,
			List<String> agents, ActionTimeGenerator timeGenerator) {
		Assignments assignments = new Assignments(timeGenerator, agents, workflow.getStartState(), this.useActualValues, false);
		return assignments;
	}
	
	public Assignments finishSchedule(Workflow workflow, Assignments assignments, ActionTimeGenerator actionTimeLookup) {
		List<String> agents = new ArrayList<String>(assignments.agents());
		for (Workflow.Node node : workflow) {
			int choice = random.nextInt(agents.size());
			String agent = agents.get(choice);
			assignments.add(node, agent);
		}
		
		return assignments;
	}
	
	
}
