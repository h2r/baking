package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.brown.cs.h2r.baking.Scheduling.AssignedWorkflow.ActionTime;
import edu.brown.cs.h2r.baking.Scheduling.AssignedWorkflow.ConditionalIterator;

public class SchedulingHelper {

	public static List<AssignedWorkflow> getBufferedWorkflows(List<AssignedWorkflow> assignedWorkflows) {
		// The workflows buffered with required waits
		List<AssignedWorkflow> bufferedWorkflows = new ArrayList<AssignedWorkflow>();
		
		// List of iterators into the assignments
		List<ConditionalIterator> workflowIterators = new ArrayList<ConditionalIterator>();
		
		// Iterate through assignments, and setup initial lists
		int size = 0;
		for (AssignedWorkflow workflow : assignedWorkflows) {
			bufferedWorkflows.add(new AssignedWorkflow(workflow.getId()));
			workflowIterators.add((ConditionalIterator)workflow.iterator());
			size += workflow.size();
		}
		
		// flag to check if we need to continue looping
		boolean keepGoing =  true;
		
		// Time at current point in loop
		double currentTime = 0.0;
		
		// A list of all nodes visited by the current time
		int defaultSize = (int)((double)size / 0.75) + 1;
		Set<Workflow.Node> futureVisitedNodes = new HashSet<Workflow.Node>(defaultSize); 
		List<AssignedWorkflow> updatedWorkflows = new ArrayList<AssignedWorkflow>();
		while(keepGoing) {
			// Set initially to false
			keepGoing = false;
			
			boolean assignedWorkflowChanges = addFeasibleActions(
					assignedWorkflows, bufferedWorkflows, workflowIterators,
					currentTime, futureVisitedNodes, updatedWorkflows);
			
			
			double nextTime = getNextTime(bufferedWorkflows, workflowIterators,
					currentTime, updatedWorkflows);
			if (nextTime == currentTime) {
				//System.err.println("Time did not advance");
			}
			// Set current time to the next time
			currentTime = nextTime;
			
			boolean haveMoreElements = false;
			// Keep going if any iterator has another element
			for (ConditionalIterator it : workflowIterators) {
				haveMoreElements |= it.hasNext();
			}
			if (haveMoreElements) {
				keepGoing = true;
			}
		}
		for (int i = 0; i < bufferedWorkflows.size(); i++) {
			AssignedWorkflow assignedWorkflow = assignedWorkflows.get(i);
			AssignedWorkflow bufferedWorkflow = bufferedWorkflows.get(i);
			if (assignedWorkflow.realSize() != bufferedWorkflow.realSize()) {
				System.err.println("Not all assigned actions were copied");
			
			}
		
		}
		return bufferedWorkflows;
	}

	private static double getNextTime(List<AssignedWorkflow> bufferedWorkflows,
			List<ConditionalIterator> workflowIterators, double currentTime,
			List<AssignedWorkflow> updatedWorkflows) {
		double nextAvailableTime = Double.MAX_VALUE;
		// Iterate through the buffered workflows
		for (AssignedWorkflow workflow : bufferedWorkflows) {
			
			// Get the earliest time this workflow will be available
			nextAvailableTime = Math.min(nextAvailableTime, workflow.time());
		}
		double nextTime = nextAvailableTime;
		
		// If the nextAvailableTime is before this current time, that means one workflow is waiting for something to happen
		if (nextAvailableTime <= currentTime) {
			
			if (updatedWorkflows.isEmpty()) {
				updatedWorkflows.addAll(bufferedWorkflows);
			}
			// Need to compute the next time at which we should update dependencies
			nextTime = Double.MAX_VALUE;
			
			// Iterate through the buffered workflows
			for (AssignedWorkflow workflow : updatedWorkflows) {
				
				// Get the time after the current time when something new happens
				Double time = workflow.nextTime(currentTime);
				if (time != null) {
					
					// The next time is the earliest possible time (after current time) where something happened
					nextTime = Math.min(nextTime, time);
				}
			}
		}
		
		// This would be bad
		if (nextTime != Double.MAX_VALUE) {
			
			// Iterate through the workflows and tell them to wait if they aren't busy
			for (int i = 0; i < bufferedWorkflows.size(); i++) {
				AssignedWorkflow bufferedWorkflow = bufferedWorkflows.get(i);
				ConditionalIterator it = workflowIterators.get(i);
				
				// Only tell them to wait if there is another action they could do after this
				if (it.hasNext()) {
					
					// Set the buffered workflow to wait until the computed next time
					bufferedWorkflow.waitUntil(nextTime);
				}
			}
		}
		return nextTime;
	}

	private static boolean addFeasibleActions(
			List<AssignedWorkflow> assignedWorkflows,
			List<AssignedWorkflow> bufferedWorkflows,
			List<ConditionalIterator> workflowIterators, double currentTime,
			Set<Workflow.Node> futureVisitedNodes,
			List<AssignedWorkflow> updatedWorkflows) {
		
		updatedWorkflows.clear();
		// Clear visited nodes, and add in all that would have been completed by the current time
		List<Workflow.Node> visitedNodes = new ArrayList<Workflow.Node>();
		
		for (AssignedWorkflow workflow : bufferedWorkflows) {
			visitedNodes.addAll(workflow.nodes(currentTime));
		}
		boolean assignedWorkflowChanges = false;
		
		// Iterate through all assignments
		for (int i = 0; i < assignedWorkflows.size(); i++) {
			futureVisitedNodes.clear();
			futureVisitedNodes.addAll(visitedNodes);
			
			assignedWorkflowChanges = addAllAvailableNodes(assignedWorkflows,
					bufferedWorkflows, workflowIterators,
					futureVisitedNodes, updatedWorkflows,
					assignedWorkflowChanges, i);	
		}
		return assignedWorkflowChanges;
	}

	private static boolean addAllAvailableNodes(
			List<AssignedWorkflow> assignedWorkflows,
			List<AssignedWorkflow> bufferedWorkflows,
			List<ConditionalIterator> workflowIterators,
			Set<Workflow.Node> futureVisitedNodes,
			List<AssignedWorkflow> updatedWorkflows,
			boolean assignedWorkflowChanges, int index) {
		
		// Create list of visited nodes that includes those that we add here
		boolean addOnce = false;
		
		// Get the assignment, and bufferedWorkflow and iterator
		AssignedWorkflow workflow = assignedWorkflows.get(index);
		AssignedWorkflow bufferedWorkflow = bufferedWorkflows.get(index);
		ConditionalIterator it = workflowIterators.get(index);
		
		// Iterate through all nodes that are in the assignment, and can be accomplished 
		// after the futureVisitedNodes are accomplished
		while(it.isNextAvailable(futureVisitedNodes)) {
			assignedWorkflowChanges = addAvailableNodes(futureVisitedNodes,
					updatedWorkflows, addOnce, bufferedWorkflow, it);
		}
		return assignedWorkflowChanges;
	}

	private static boolean addAvailableNodes(
			Set<Workflow.Node> futureVisitedNodes,
			List<AssignedWorkflow> updatedWorkflows, boolean addOnce,
			AssignedWorkflow bufferedWorkflow, ConditionalIterator it) {
		boolean assignedWorkflowChanges;
		// Get item
		ActionTime item = it.next();
		
		//Add this node to the future visited list
		futureVisitedNodes.add(item.getNode());
		
		// Add this item to the buffered workflow
		bufferedWorkflow.add(item);
		if (!addOnce) {
			updatedWorkflows.add(bufferedWorkflow);
			addOnce = true;
		}
		
		// Changes have been made, need to continue outer while loop
		assignedWorkflowChanges = true;
		return assignedWorkflowChanges;
	}
	
	public static List<AssignedWorkflow> copy(List<AssignedWorkflow> other) {
		List<AssignedWorkflow> copyOf = new ArrayList<AssignedWorkflow>(other.size());
		for (AssignedWorkflow workflow : other) {
			copyOf.add(new AssignedWorkflow(workflow));
		}
		return copyOf;
	}
	
	public static Map<String, AssignedWorkflow> copyMap(Map<String, AssignedWorkflow> other) {
		Map<String, AssignedWorkflow> copyOf = new HashMap<String, AssignedWorkflow>();
		for (Map.Entry<String, AssignedWorkflow> entry : other.entrySet()) {
			copyOf.put(entry.getKey(), new AssignedWorkflow(entry.getValue()));
		}
		return copyOf;
	}
	
	public static double computeSequenceTime(List<AssignedWorkflow> assignedWorkflows) {
		List<AssignedWorkflow> bufferedWorkflows = SchedulingHelper.getBufferedWorkflows(assignedWorkflows);
		return SchedulingHelper.getLongestAssignment(bufferedWorkflows);
	}
	
	public static double getLongestAssignment(List<AssignedWorkflow> bufferedWorkflows) {
		double maxTime = 0.0;
		for (AssignedWorkflow workflow : bufferedWorkflows) {
			maxTime = Math.max(maxTime, workflow.time());
		}
		return maxTime;
	}
}
