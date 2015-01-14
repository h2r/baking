package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.Experiments.SchedulingComparison;
import edu.brown.cs.h2r.baking.Scheduling.Assignment.ActionTime;
import edu.brown.cs.h2r.baking.Scheduling.Assignment.AssignmentIterator;

public class GreedyScheduler implements Scheduler {

	@Override
	public List<Assignment> schedule(Workflow workflow, List<String> agents,
			ActionTimeGenerator actionTimeLookup) {
		
		// The scheduled assignments we are returning
		List<Assignment> assignedWorkflows = new ArrayList<Assignment>();
				
		// Create the new workflow specific to the agents
		for (String agent : agents) {
			Assignment assignedWorkflow = new Assignment(agent);
			assignedWorkflows.add(assignedWorkflow);
		}
				
		BufferedAssignments buffered = new BufferedAssignments(assignedWorkflows);
		return this.finishSchedule(workflow, actionTimeLookup, assignedWorkflows, buffered, new HashSet<Workflow.Node>());
	}
	
	public List<Assignment> finishSchedule(Workflow workflow, ActionTimeGenerator actionTimeLookup, 
			List<Assignment> assignedWorkflows, BufferedAssignments bufferedAssignments, Set<Workflow.Node> visitedNodes ) {
		
		// Iterate through all the workflow nodes in dependency order
		List<AssignmentIterator> workflowIterators = new ArrayList<AssignmentIterator>();
		
		// Iterate through assignments, and setup initial lists
		int size = 0;
		for (Assignment assignedWorkflow : assignedWorkflows) {
			workflowIterators.add((AssignmentIterator)assignedWorkflow.iterator());
			size += workflow.size();
		}
		
		for (Workflow.Node node : workflow) {
			if (!visitedNodes.add(node)){
				continue;
			}
			
			
			
			// 
			//currentTime = 
			//		SchedulingHelper.updateBufferedWorkflows(assignedWorkflows, workflowIterators, 
			//				bufferedWorkflows, visitedNodes, currentTime);
			
			int bestChoice = 0;
			double bestTime = Double.MAX_VALUE;
			double bestActionTime = 0.0;
			String bestAgent = null;
			for (int i = 0; i < assignedWorkflows.size(); i++) {
				Assignment assignment = assignedWorkflows.get(i);
				String agent = assignment.getId();
				GroundedAction action = node.getAction();
				action.params[0] = agent;
				double actionTime = actionTimeLookup.get(node.getAction(), false);
				
				double anticipatedTime = 
						bufferedAssignments.getTimeAssigningNodeToAgent(node, actionTime, agent);
				
				if (anticipatedTime < bestTime) {
					bestTime = anticipatedTime;
					bestAgent = agent;
					bestChoice = i;
					bestActionTime = actionTime;
				}
			}
			
			//Add the action to the assignment that would finish this job the soonest
			assignedWorkflows.get(bestChoice).add(node, bestActionTime);
			bufferedAssignments.add(node, bestActionTime, bestAgent);
		}
		
		return assignedWorkflows;
	}
	
	

}
