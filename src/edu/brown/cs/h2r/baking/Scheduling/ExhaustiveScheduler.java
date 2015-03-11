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
	public Assignments schedule(Workflow workflow, List<String> agents,
			ActionTimeGenerator actionTimeLookup) {
		Assignments assignments = new Assignments(actionTimeLookup, agents, workflow.getStartState(), this.useActualValues, false);
		
		Queue<AssignmentNode> queue =  new LinkedList<AssignmentNode>();
		AssignmentNode first = new AssignmentNode(workflow, assignments, assignments);
		queue.add(first);
		return this.assignActions(workflow, actionTimeLookup,queue, agents);
	}
	
	public Assignments finishSchedule(Workflow workflow, Assignments assignments, ActionTimeGenerator actionTimeLookup) {
		
		Queue<AssignmentNode> queue = new LinkedList<AssignmentNode>();
		AssignmentNode first = new AssignmentNode(workflow, assignments, assignments);
		queue.add(first);
		return this.assignActions(workflow, actionTimeLookup, queue, assignments.agents());
	}
	
	private Assignments assignActions(Workflow workflow, ActionTimeGenerator actionTimeLookup, 
			Queue<AssignmentNode> openQueue, Collection<String> agents) {
		int checkedNodes = 0;
		
		AssignmentNode bestNode = null;
		double minTime = Double.MAX_VALUE;
		
		while(openQueue.peek() != null) {
			checkedNodes++;
			AssignmentNode node = openQueue.poll();
			if (node.complete()) {
				if (node.getTime() < minTime) {
					bestNode = node;
					minTime = node.getTime();
				}
				continue;
			}
			
			List<Workflow.Node> availableActions = workflow.getAvailableNodes(node.getAssignedNodes());
			
			int numAddedNodes = 0;
			for (Workflow.Node nextNode : availableActions) {
				for (String agent : agents){
					Assignments currentAssignments = node.getAssignments();
					Assignments copied = currentAssignments.copy();
					if (copied.add(nextNode, agent)) {
						AssignmentNode newNode = new AssignmentNode(workflow, copied, copied);
						openQueue.add(newNode);
						numAddedNodes++;
					}
				}
			}
		}
		
		return (bestNode == null ) ? null : bestNode.getSequenced();
	}	
	
	

}
