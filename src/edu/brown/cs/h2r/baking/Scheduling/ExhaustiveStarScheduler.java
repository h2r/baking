package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.datastructures.HashIndexedHeap;
import burlap.debugtools.DPrint;
import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.Scheduling.Assignment.ActionTime;

public class ExhaustiveStarScheduler implements Scheduler {
	public static final String GROUNDED_ACTION_CLASSNAME = "grounded_action";
	public static final String AGENT_CLASSNAME = "agent";
	public static final String ASSIGNMENTS = "assignments";
	public static final String ASSIGNMENT_CLASS = "assignment";
	private final Scheduler heuristic;
	private static final int debugCode = 101;
	private final Boolean useActualValues;
	
	public ExhaustiveStarScheduler(boolean useActualValues) {
		DPrint.toggleCode(debugCode, false);
		this.useActualValues = useActualValues;
		this.heuristic = new GreedyScheduler(this.useActualValues);
	}
	
	public ExhaustiveStarScheduler(Scheduler heuristic) {
		DPrint.toggleCode(debugCode, false);
		this.useActualValues = heuristic.isUsingActualValues();
		this.heuristic = heuristic;
	}

	public boolean isUsingActualValues() {
		return this.useActualValues;
	}
	@Override
	public List<Assignment> schedule(Workflow workflow, List<String> agents,
			ActionTimeGenerator actionTimeLookup) {
		HashIndexedHeap<AssignmentNode> openQueue = new HashIndexedHeap<AssignmentNode>(new AssignmentNode.AssignmentComparator());
		List<Assignment> assignments = new ArrayList<Assignment>();
		for (String agent : agents) assignments.add(new Assignment(agent, actionTimeLookup, this.useActualValues));
		
		List<Assignment> completedAssignments = this.getCompleted(workflow, actionTimeLookup, assignments);
		BufferedAssignments buffered = new BufferedAssignments(completedAssignments);		
		
		AssignmentNode firstNode = 
				new AssignmentNode(assignments, this.useActualValues, actionTimeLookup, buffered.time(), completedAssignments);
		openQueue.insert(firstNode);
		Map<String, Assignment> assignedWorkflows = new HashMap<String, Assignment>();
		
		for (String agent : agents) {
			Assignment assignedWorkflow = new Assignment(agent, actionTimeLookup, this.useActualValues);
			assignedWorkflows.put(agent, assignedWorkflow);
		}
		return this.assignActions(workflow, actionTimeLookup, openQueue, agents);
	}
	
	public List<Assignment> finishSchedule(Workflow workflow, ActionTimeGenerator actionTimeLookup, 
			List<Assignment> assignedWorkflows, BufferedAssignments bufferedWorkflows, Set<Workflow.Node> visitedNodes) {
		
		Map<String, Assignment> assignedWorkflowMap = new HashMap<String, Assignment>();
		
		for (Assignment assignedWorkflow : assignedWorkflows) {
			assignedWorkflowMap.put(assignedWorkflow.getId(), assignedWorkflow);
		}
		List<String> agents = new ArrayList<String>(assignedWorkflowMap.keySet());
		
		List<Assignment> completedAssignments = this.getCompleted(workflow, actionTimeLookup, assignedWorkflows);
		BufferedAssignments buffered = new BufferedAssignments(completedAssignments);		
		
		HashIndexedHeap<AssignmentNode> openQueue = new HashIndexedHeap<AssignmentNode>(new AssignmentNode.AssignmentComparator());
		
		
		AssignmentNode firstNode = new AssignmentNode(assignedWorkflows, this.useActualValues, actionTimeLookup, buffered.time(), completedAssignments);
		openQueue.insert(firstNode);
		
		return this.assignActions(workflow, actionTimeLookup, openQueue, agents);
	}
	
	
	
	private List<Assignment> assignActions(Workflow workflow, ActionTimeGenerator actionTimeLookup, 
			HashIndexedHeap<AssignmentNode> openQueue, List<String> agents) {
		int checkedNodes = 0;
		while(openQueue.peek() != null) {
			checkedNodes++;
			//DPrint.toggleCode(101, true);
			DPrint.cl(debugCode, "Number explored: " + checkedNodes);
			AssignmentNode node = openQueue.poll();
			if (node.complete(workflow)) {
				return node.getCompletedAssignments();
			}
			DPrint.cl(debugCode, "Current value: " + node.getTime());
			
			
			List<Workflow.Node> availableActions = workflow.getAvailableNodes(node.getAssignedNodes());
			
			int numAddedNodes = 0;
			for (Workflow.Node action : availableActions) {
				for (String agent : agents){
					GroundedAction ga = action.getAction();
					ga.params[0] = agent;
					double time = actionTimeLookup.get(ga, this.useActualValues);
					ActionTime actionTime = new ActionTime(action, time);
					List<Assignment> completedAssignments = this.getCompleted(workflow, actionTimeLookup, node.getAssignments());
					BufferedAssignments buffered = new BufferedAssignments(completedAssignments);	
					AssignmentNode newNode = new AssignmentNode(node, agent, actionTime, buffered.time(), completedAssignments);
					if (openQueue.containsInstance(newNode) == null && !newNode.equals(node)) {
						openQueue.insert(newNode);
						numAddedNodes++;
					}
					
				}
			}
			if (numAddedNodes == 0) {
				return node.getCompletedAssignments();
			}
			DPrint.cl(debugCode, "Added " + numAddedNodes);
		}
		return null;
	}	
	
	private List<Assignment> getCompleted(Workflow workflow, ActionTimeGenerator timeGenerator, List<Assignment> assignments) {
		Set<Workflow.Node> visited = new HashSet<Workflow.Node>();
		for (Assignment assignment : assignments) visited.addAll(assignment.nodes());
		BufferedAssignments buffered = new BufferedAssignments(assignments);
	
		return this.heuristic.finishSchedule(workflow, timeGenerator, assignments, buffered, visited);
	}
}
