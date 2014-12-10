package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import burlap.oomdp.singleagent.GroundedAction;

public class RandomScheduler implements Scheduler {
	Random random = new Random();
	
	@Override
	public List<AssignedWorkflow> schedule(Workflow workflow,
			List<String> agents, ActionTimeGenerator timeGenerator) {
		List<AssignedWorkflow> assignedWorkflows = new ArrayList<AssignedWorkflow>();
		for (String agent : agents) {
			AssignedWorkflow assignedWorkflow = new AssignedWorkflow(agent);
			assignedWorkflows.add(assignedWorkflow);
		}
		
		for (Workflow.Node node : workflow) {
			int choice = random.nextInt(agents.size());
			String agent = agents.get(choice);
			GroundedAction ga = node.getAction();
			ga.params[0] = agent;
			double time = timeGenerator.get(ga);
			assignedWorkflows.get(choice).addAction(node, time);
		}
		
		return assignedWorkflows;
	}
}
