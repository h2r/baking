package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.Scheduling.Assignment.ActionTime;
import edu.brown.cs.h2r.baking.Scheduling.Assignment.AssignmentIterator;

public class SchedulingHelper {

	public static List<Assignment> getBufferedWorkflows(Collection<Assignment> assignedWorkflows) {
		// The workflows buffered with required waits
		List<Assignment> bufferedWorkflows = new ArrayList<Assignment>();
		
		// List of iterators into the assignments
		List<AssignmentIterator> workflowIterators = new ArrayList<AssignmentIterator>();
		
		// Iterate through assignments, and setup initial lists
		int size = 0;
		for (Assignment workflow : assignedWorkflows) {
			bufferedWorkflows.add(new Assignment(workflow.getId()));
			workflowIterators.add((AssignmentIterator)workflow.iterator());
			size += workflow.size();
		}
		
		// flag to check if we need to continue looping
		boolean keepGoing =  true;
		
		// Time at current point in loop
		double currentTime = 0.0;
		
		// A list of all nodes visited by the current time
		int defaultSize = (int)((double)size / 0.5) + 1;
		Set<Workflow.Node> futureVisitedNodes = new HashSet<Workflow.Node>(2 * defaultSize, 0.5f); 
		List<Assignment> updatedWorkflows = new ArrayList<Assignment>();
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
			for (AssignmentIterator it : workflowIterators) {
				haveMoreElements |= it.hasNext();
			}
			if (haveMoreElements) {
				keepGoing = true;
			}
		}
		
		Iterator<Assignment> iterator = assignedWorkflows.iterator();
		for (int i = 0; i < bufferedWorkflows.size(); i++) {
			Assignment assignedWorkflow = iterator.next();
			Assignment bufferedWorkflow = bufferedWorkflows.get(i);
			if (assignedWorkflow.realSize() != bufferedWorkflow.realSize()) {
				System.err.println("Not all assigned actions were copied");
			
			}
		
		}
		return bufferedWorkflows;
	}
	
	
	public static double updateBufferedWorkflows(Collection<Assignment> assignedWorkflows, List<AssignmentIterator> workflowIterators,
			List<Assignment> bufferedWorkflows, Set<Workflow.Node> visitedNodes, double currentTime) {
		// flag to check if we need to continue looping
		boolean keepGoing =  true;
		int numTimesCalled = 0;
		
		// A list of all nodes visited by the current time
		List<Assignment> updatedWorkflows = new ArrayList<Assignment>();
		while(keepGoing) {
			// Set initially to false
			keepGoing = false;
			numTimesCalled++;
			boolean assignedWorkflowChanges = addFeasibleActions(
					assignedWorkflows, bufferedWorkflows, workflowIterators,
					currentTime, visitedNodes, updatedWorkflows);
			
			
			double nextTime = getNextTime(bufferedWorkflows, workflowIterators,
					currentTime, updatedWorkflows);
			if (nextTime == currentTime) {
				//System.err.println("Time did not advance");
			}
			// Set current time to the next time
			currentTime = nextTime;
			
			boolean haveMoreElements = false;
			// Keep going if any iterator has another element
			for (AssignmentIterator it : workflowIterators) {
				haveMoreElements |= it.hasNext();
			}
			if (haveMoreElements) {
				keepGoing = true;
			}
		}
		
		Iterator<Assignment> iterator = assignedWorkflows.iterator();
		for (int i = 0; i < bufferedWorkflows.size(); i++) {
			Assignment assignedWorkflow = iterator.next();
			Assignment bufferedWorkflow = bufferedWorkflows.get(i);
			if (assignedWorkflow.realSize() != bufferedWorkflow.realSize()) {
				System.err.println("Not all assigned actions were copied");
			
			}
		
		}
		if (numTimesCalled > 1) {
			System.out.println("Number times called, " + numTimesCalled + ", " + visitedNodes.size());
		}
		return currentTime;
	}

	private static double getNextTime(List<Assignment> bufferedWorkflows,
			List<AssignmentIterator> workflowIterators, double currentTime,
			List<Assignment> updatedWorkflows) {
		double nextAvailableTime = Double.MAX_VALUE;
		// Iterate through the buffered workflows
		for (Assignment workflow : bufferedWorkflows) {
			
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
			for (Assignment workflow : updatedWorkflows) {
				
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
				Assignment bufferedWorkflow = bufferedWorkflows.get(i);
				AssignmentIterator it = workflowIterators.get(i);
				
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
			Collection<Assignment> assignedWorkflows,
			List<Assignment> bufferedWorkflows,
			List<AssignmentIterator> workflowIterators, double currentTime,
			Set<Workflow.Node> futureVisitedNodes,
			List<Assignment> updatedWorkflows) {
		
		updatedWorkflows.clear();
		int size = 0;
		for (Assignment workflow : bufferedWorkflows) size += workflow.size();
		// Clear visited nodes, and add in all that would have been completed by the current time
		List<Workflow.Node> visitedNodes = new ArrayList<Workflow.Node>(size);
		
		for (Assignment workflow : bufferedWorkflows) {
			visitedNodes.addAll(workflow.nodes(currentTime));
		}
		boolean assignedWorkflowChanges = false;
		// Iterate through all assignments
		Iterator<Assignment> iterator = assignedWorkflows.iterator();
		for (int i = 0; i < assignedWorkflows.size(); i++) {
			Assignment assignedWorkflow = iterator.next();
			futureVisitedNodes.clear();
			futureVisitedNodes.addAll(visitedNodes);
			
			assignedWorkflowChanges = addAllAvailableNodes(assignedWorkflow,
					bufferedWorkflows.get(i), workflowIterators.get(i),
					futureVisitedNodes, updatedWorkflows,
					assignedWorkflowChanges, i);	
		}
		return assignedWorkflowChanges;
	}

	private static boolean addAllAvailableNodes(
			Assignment workflow,
			Assignment bufferedWorkflow,
			AssignmentIterator it,
			Set<Workflow.Node> futureVisitedNodes,
			List<Assignment> updatedWorkflows,
			boolean assignedWorkflowChanges, int index) {
		
		// Create list of visited nodes that includes those that we add here
		boolean addOnce = false;
		
		// Iterate through all nodes that are in the assignment, and can be accomplished 
		// after the futureVisitedNodes are accomplished
		while(it.isNextAvailable(futureVisitedNodes)) {
			addOnce = SchedulingHelper.addAvailableNodes(futureVisitedNodes,
					updatedWorkflows, addOnce, bufferedWorkflow, it);
		}
		return assignedWorkflowChanges;
	}

	private static boolean addAvailableNodes(
			Set<Workflow.Node> futureVisitedNodes,
			List<Assignment> updatedWorkflows, boolean addOnce,
			Assignment bufferedWorkflow, AssignmentIterator it) {
		
		// Get item
		ActionTime item = it.next();
		
		//Add this node to the future visited list
		futureVisitedNodes.add(item.getNode());
		
		// Add this item to the buffered workflow
		bufferedWorkflow.add(item.getNode(), item.getTime());
		if (!addOnce) {
			updatedWorkflows.add(bufferedWorkflow);
			addOnce = true;
		}
		
		return addOnce;
	}
	
	public static List<Assignment> copy(List<Assignment> other) {
		List<Assignment> copyOf = new ArrayList<Assignment>(other.size());
		for (Assignment workflow : other) {
			copyOf.add(new Assignment(workflow));
		}
		return copyOf;
	}
	
	public static Map<String, Assignment> copyMap(Map<String, Assignment> other) {
		Map<String, Assignment> copyOf = new HashMap<String, Assignment>();
		for (Map.Entry<String, Assignment> entry : other.entrySet()) {
			copyOf.put(entry.getKey(), new Assignment(entry.getValue()));
		}
		return copyOf;
	}
	
	public static double getLongestAssignment(List<Assignment> bufferedWorkflows) {
		double maxTime = 0.0;
		for (Assignment workflow : bufferedWorkflows) {
			maxTime = Math.max(maxTime, workflow.time());
		}
		return maxTime;
	}
	
	public static void addActionToBuffered(List<Assignment> bufferedWorkflows, ActionTime action, String agent) {
		Assignment agentsWorkflow = null;
		for (Assignment workflow : bufferedWorkflows) {
			if (workflow.getId().equals(agent)) {
				agentsWorkflow = workflow;
			}
		}
		if (agentsWorkflow == null) {
			return;
		}
		
		double time = SchedulingHelper.getTimeNodeIsAvailable(bufferedWorkflows, action.getNode(), agentsWorkflow.time());
		if (time > agentsWorkflow.time()) {
			agentsWorkflow.add(null, time - agentsWorkflow.time());
		}
		agentsWorkflow.add(action.getNode(), action.getTime());
	}
	
	public static double getTimeNodeIsAvailable(List<Assignment> bufferedWorkflows, Workflow.Node node, double seed) {
		Set<Workflow.Node> completed = new HashSet<Workflow.Node>();
		double currentTime = seed;
		for (Assignment workflow : bufferedWorkflows) {
			completed.addAll(workflow.nodes(currentTime));
		}
		
		while (!node.isAvailable(completed)) {
			for (Assignment workflow : bufferedWorkflows) {
				completed.addAll(workflow.nodes(currentTime));
			}
			double nextTime = Double.MAX_VALUE;
			for (Assignment workflow : bufferedWorkflows) {
				Double time = workflow.nextTime(currentTime);
				if (time != null && time > currentTime && time < nextTime) {
					nextTime = time;
				}
			}
			currentTime = nextTime;
		}
		
		return currentTime;
		
	}


	public static double computeSequenceTime( State state,
			List<AbstractGroundedAction> actionSequence, ActionTimeGenerator timeGenerator) {
		Workflow workflow = Workflow.buildWorkflow(state, actionSequence);
		Map<String, Assignment> sortedActions = new HashMap<String, Assignment>();
		
		for (Workflow.Node node : workflow) {
			GroundedAction ga = node.getAction();
			String agent = ga.params[0];
			Assignment assignment = sortedActions.get(agent);
			if (assignment == null) {
				assignment = new Assignment(agent);
				sortedActions.put(agent, assignment);
			}
			double time = timeGenerator.get(ga);
			assignment.add(node, time);
		}
		BufferedAssignments buffered = new BufferedAssignments(sortedActions.values());
		
		return buffered.time();
	}
}
