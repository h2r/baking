package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.brown.cs.h2r.baking.Scheduling.AssignedWorkflow.ActionTime;
import edu.brown.cs.h2r.baking.Scheduling.AssignedWorkflow.ConditionalIterator;
import edu.brown.cs.h2r.baking.Scheduling.Workflow.Node;

public class GreedyScheduler implements Scheduler {

	@Override
	public List<AssignedWorkflow> schedule(Workflow workflow,
			Map<String, Map<Node, Double>> actionTimeLookup) {
		
		// The scheduled assignments we are returning
		List<AssignedWorkflow> assignedWorkflows = new ArrayList<AssignedWorkflow>();
		
		// Need a list of the agents
		List<String> agents = new ArrayList<String>(actionTimeLookup.keySet());
		
		// Create the new workflow specific to the agents
		for (String agent : agents) {
			AssignedWorkflow assignedWorkflow = new AssignedWorkflow(agent);
			assignedWorkflows.add(assignedWorkflow);
		}
				
		return this.schedule(workflow, actionTimeLookup, assignedWorkflows, new HashSet<Workflow.Node>());
	}
	
	public List<AssignedWorkflow> schedule(Workflow workflow, Map<String, Map<Node, Double>> actionTimeLookup, 
			List<AssignedWorkflow> assignedWorkflows, Set<Workflow.Node> visitedNodes ) {
		
		
		// Iterate through all the workflow nodes in dependency order
		for (Workflow.Node node : workflow) {
			if (!visitedNodes.add(node)){
				continue;
			}
			
			// Copy the assigned workflows for our hypothetical additions
			List<AssignedWorkflow> copied = SchedulingHelper.copy(assignedWorkflows);
			
			// 
			List<AssignedWorkflow> bufferedWorkflows = SchedulingHelper.getBufferedWorkflows(copied);
			
			int bestChoice = 0;
			double bestTime = Double.MAX_VALUE;
			double bestActionTime = 0.0;
			for (int i = 0; i < bufferedWorkflows.size(); i++) {
				AssignedWorkflow bufferedWorkflow = bufferedWorkflows.get(i);
				double actionTime = actionTimeLookup.get(bufferedWorkflow.getId()).get(node);
				double anticipatedTime = bufferedWorkflow.time() + actionTime;
				
				if (anticipatedTime < bestTime) {
					bestTime = anticipatedTime;
					bestChoice = i;
					bestActionTime = actionTime;
				}
			}
			
			//Add the action to the assignment that would finish this job the soonest
			assignedWorkflows.get(bestChoice).addAction(node, bestActionTime);
			
			
		}
		
		return assignedWorkflows;
	}
	

}
