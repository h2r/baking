package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import burlap.oomdp.singleagent.GroundedAction;

public class RandomScheduler implements Scheduler {
	Random random = new Random();
	
	@Override
	public List<Assignment> schedule(Workflow workflow,
			List<String> agents, ActionTimeGenerator timeGenerator) {
		List<Assignment> assignedWorkflows = new ArrayList<Assignment>();
		for (String agent : agents) {
			Assignment assignedWorkflow = new Assignment(agent);
			assignedWorkflows.add(assignedWorkflow);
		}
		
		for (Workflow.Node node : workflow) {
			int choice = random.nextInt(agents.size());
			String agent = agents.get(choice);
			GroundedAction ga = node.getAction();
			ga.params[0] = agent;
			double time = timeGenerator.get(ga);
			assignedWorkflows.get(choice).add(node, time);
		}
		
		return assignedWorkflows;
	}
	
	public List<Assignment> finishSchedule(Workflow workflow, ActionTimeGenerator actionTimeLookup, 
			List<Assignment> assignedWorkflows, BufferedAssignments bufferedWorkflows, Set<Workflow.Node> visitedNodes) {

		for (Workflow.Node node : workflow) {
			int choice = random.nextInt(assignedWorkflows.size());
			Assignment assignedWorkflow = assignedWorkflows.get(choice);
			GroundedAction ga = node.getAction();
			ga.params[0] = assignedWorkflow.getId();
			double time = actionTimeLookup.get(ga);
			assignedWorkflow.add(node, time);
		}
		
		return assignedWorkflows;
	}
	
	
}
