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
	public List<Assignment> schedule(Workflow workflow,
			List<String> agents, ActionTimeGenerator timeGenerator) {
		List<Assignment> assignedWorkflows = new ArrayList<Assignment>();
		for (String agent : agents) {
			Assignment assignedWorkflow = new Assignment(agent, timeGenerator, false);
			assignedWorkflows.add(assignedWorkflow);
		}
		
		for (Workflow.Node node : workflow) {
			int choice = random.nextInt(agents.size());
			assignedWorkflows.get(choice).add(node);
		}
		
		return assignedWorkflows;
	}
	
	public List<Assignment> finishSchedule(Workflow workflow, ActionTimeGenerator actionTimeLookup, 
			List<Assignment> assignedWorkflows, BufferedAssignments bufferedWorkflows, Set<Workflow.Node> visitedNodes) {

		for (Workflow.Node node : workflow) {
			int choice = random.nextInt(assignedWorkflows.size());
			Assignment assignedWorkflow = assignedWorkflows.get(choice);
			assignedWorkflow.add(node);
		}
		
		return assignedWorkflows;
	}
	
	
}
