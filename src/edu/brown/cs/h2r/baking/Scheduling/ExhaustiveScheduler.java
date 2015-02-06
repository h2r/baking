package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;

import burlap.debugtools.DPrint;
import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.Scheduling.Assignment.ActionTime;

public class ExhaustiveScheduler implements Scheduler {
	private final int maxDepth;
	private final boolean useActualValues;
	private static final int debugCode = 102;
	
	public ExhaustiveScheduler(boolean useActualValues) {
		this.maxDepth = -1;
		this.useActualValues = useActualValues;
		DPrint.toggleCode(debugCode, false);
		
	}
	
	public ExhaustiveScheduler(int maxDepth, boolean useActualValues) {
		this.maxDepth = maxDepth;
		this.useActualValues = useActualValues;
		DPrint.toggleCode(debugCode, false);
	}
	
	public boolean isUsingActualValues() {
		return this.useActualValues;
	}

	@Override
	public List<Assignment> schedule(Workflow workflow, List<String> agents,
			ActionTimeGenerator actionTimeLookup) {
		List<Assignment> assignments = new ArrayList<Assignment>();
		for (String agent : agents) assignments.add(new Assignment(agent, actionTimeLookup, this.useActualValues));

		Queue<AssignmentNode> queue =  new LinkedList<AssignmentNode>();
		AssignmentNode first = new AssignmentNode(assignments, this.useActualValues, actionTimeLookup, null, null);
		queue.add(first);
		return this.assignActions(workflow, actionTimeLookup,queue, agents);
	}
	
	public List<Assignment> finishSchedule(Workflow workflow, ActionTimeGenerator actionTimeLookup, 
			List<Assignment> assignedWorkflows, BufferedAssignments bufferedWorkflows, Set<Workflow.Node> visitedNodes) {
		
		Map<String, Assignment> assignedWorkflowMap = new HashMap<String, Assignment>();
		for (Assignment assignedWorkflow : assignedWorkflows) {
			assignedWorkflowMap.put(assignedWorkflow.getId(), assignedWorkflow);
		}
		List<Assignment> assignments = new ArrayList<Assignment>(assignedWorkflowMap.values());
		Queue<AssignmentNode> queue = new LinkedList<AssignmentNode>();
		AssignmentNode first = new AssignmentNode(assignments, this.useActualValues, actionTimeLookup, null, null);
		queue.add(first);
		return this.assignActions(workflow, actionTimeLookup, queue, assignedWorkflowMap.keySet());
	}
	
	private List<Assignment> assignActions(Workflow workflow, ActionTimeGenerator actionTimeLookup, 
			Queue<AssignmentNode> openQueue, Collection<String> agents) {
		int checkedNodes = 0;
		List<AssignmentNode> completedNodes = new ArrayList<AssignmentNode>();
		while(openQueue.peek() != null) {
			checkedNodes++;
			AssignmentNode node = openQueue.poll();
			if (node.complete(workflow)) {
				completedNodes.add(node);
				continue;
			}
			
			List<Workflow.Node> availableActions = workflow.getAvailableNodes(node.getAssignedNodes());
			
			int numAddedNodes = 0;
			for (Workflow.Node nextNode : availableActions) {
				for (String agent : agents){
					Map<String, Assignment> currentAssignments = node.getAssignments();
					currentAssignments.get(agent).add(nextNode);
					AssignmentNode newNode = new AssignmentNode(currentAssignments, null, null);
					if (!newNode.equals(node)) {
						openQueue.add(newNode);
						numAddedNodes++;
					}
					
				}
			}
		}
		AssignmentNode bestNode = null;
		double minTime = Double.MAX_VALUE;
		for (int j = 0; j < completedNodes.size(); j++) {
			AssignmentNode node = completedNodes.get(j);
			List<Assignment> assignments = node.getAssignmentLists();
			double bufferedTime = 0.0;
			for (int i = 0; i < 3; i++) {
			BufferedAssignments buffered = new BufferedAssignments(assignments, false);
			if (bufferedTime > 0.0 && bufferedTime != buffered.time()) {
				System.err.println("Inconsistent buffered time " + bufferedTime + " " + buffered.time());
			}
			bufferedTime = buffered.time();
			}
			if (bufferedTime < minTime) {
				minTime = node.getTime();
				bestNode = node;
			}
		}
		return (bestNode == null ) ? null : bestNode.getAssignmentLists();
	}	
	
	

}
