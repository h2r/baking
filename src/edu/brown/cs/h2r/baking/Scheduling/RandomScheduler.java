package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class RandomScheduler implements Scheduler {
	Random random = new Random();
	
	@Override
	public List<AssignedWorkflow> schedule(Workflow workflow,
			Map<String, Map<Workflow.Node, Double>> actionTimeLookup) {
		List<AssignedWorkflow> assignedWorkflows = new ArrayList<AssignedWorkflow>();
		List<String> agents = new ArrayList<String>(actionTimeLookup.keySet());
		for (String agent : agents) {
			AssignedWorkflow assignedWorkflow = new AssignedWorkflow(agent);
			assignedWorkflows.add(assignedWorkflow);
		}
		
		for (Workflow.Node node : workflow) {
			int choice = random.nextInt(agents.size());
			String agent = agents.get(choice);
			double time = actionTimeLookup.get(agent).get(node);
			assignedWorkflows.get(choice).addAction(node, time);
		}
		
		return assignedWorkflows;
	}

}
