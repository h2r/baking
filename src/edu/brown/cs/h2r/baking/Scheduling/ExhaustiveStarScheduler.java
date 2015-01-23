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
	private static final int debugCode = 101;
	private final Boolean useActualValues;
	
	public ExhaustiveStarScheduler(boolean useActualValues) {
		DPrint.toggleCode(debugCode, false);
		this.useActualValues = useActualValues;
	}

	@Override
	public List<Assignment> schedule(Workflow workflow, List<String> agents,
			ActionTimeGenerator actionTimeLookup) {
		HashIndexedHeap<AssignmentNode> openQueue = new HashIndexedHeap<AssignmentNode>(new AssignmentNode.AssignmentComparator());
		AssignmentNode firstNode = new AssignmentNode(workflow, new GreedyScheduler(this.useActualValues), actionTimeLookup, agents, this.useActualValues);
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
		
		HashIndexedHeap<AssignmentNode> openQueue = new HashIndexedHeap<AssignmentNode>(new AssignmentNode.AssignmentComparator());
		AssignmentNode firstNode = new AssignmentNode(workflow, assignedWorkflows, new GreedyScheduler(this.useActualValues), actionTimeLookup);
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
			DPrint.cl(debugCode, "Current value: " + node.time);
			
			
			List<Workflow.Node> availableActions = workflow.getAvailableNodes(node.assignedNodes);
			
			int numAddedNodes = 0;
			for (Workflow.Node action : availableActions) {
				for (String agent : agents){
					GroundedAction ga = action.getAction();
					ga.params[0] = agent;
					double time = actionTimeLookup.get(ga, false);
					ActionTime actionTime = new ActionTime(action, time);
					AssignmentNode newNode = new AssignmentNode(node, agent, actionTime);
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
	

	public static class AssignmentNode {
		private final double time;
		private final Map<String, Assignment> assignments;
		private final BufferedAssignments bufferedAssignments;
		private final Set<Workflow.Node> assignedNodes;
		private final Workflow workflow;
		private final Scheduler scheduler;
		private final ActionTimeGenerator timeGenerator;
		private final List<Assignment> completedAssignments;
		
		public AssignmentNode(Workflow workflow, Scheduler heuristicScheduler, ActionTimeGenerator timeGenerator, List<String> agents, boolean useActualValues) {
			this.assignments = new HashMap<String, Assignment>();
			for (String agent : agents) {
				this.assignments.put(agent, new Assignment(agent, timeGenerator,useActualValues));
			}
			this.assignedNodes = new HashSet<Workflow.Node>();
			this.workflow = workflow;
			this.scheduler = heuristicScheduler;
			this.timeGenerator = timeGenerator;
			
			List<Assignment> copied = SchedulingHelper.copy(new ArrayList<Assignment>(this.assignments.values()));
			this.bufferedAssignments = new BufferedAssignments(copied);
			BufferedAssignments completedBuffered = this.bufferedAssignments.copy();
			
			Set<Workflow.Node> visitedNodes = new HashSet<Workflow.Node>(this.assignedNodes); 
			this.completedAssignments = 
					this.scheduler.finishSchedule(workflow, this.timeGenerator, copied, completedBuffered, visitedNodes);
			
			this.time = completedBuffered.time();
			
		}
		
		public AssignmentNode(Workflow workflow, List<Assignment> assignments, Scheduler heuristicScheduler, ActionTimeGenerator timeGenerator) {
			this.assignments = new HashMap<String, Assignment>();
			this.assignedNodes = new HashSet<Workflow.Node>();
			
			for (Assignment assignment : assignments) {
				this.assignments.put(assignment.getId(), assignment);
				this.assignedNodes.addAll(assignment.nodes());
			}
			
			this.workflow = workflow;
			this.scheduler = heuristicScheduler;
			this.timeGenerator = timeGenerator;
			
			List<Assignment> copied = SchedulingHelper.copy(new ArrayList<Assignment>(this.assignments.values()));
			this.bufferedAssignments = new BufferedAssignments(copied);
			BufferedAssignments completedBuffered = this.bufferedAssignments.copy();
			
			Set<Workflow.Node> visitedNodes = new HashSet<Workflow.Node>(this.assignedNodes); 
			this.completedAssignments = 
					this.scheduler.finishSchedule(workflow, this.timeGenerator, copied, completedBuffered, visitedNodes);
			
			this.time = completedBuffered.time();
			
		}
		
		public AssignmentNode(AssignmentNode node, String agent, ActionTime action) {
			this.workflow = node.workflow;
			this.scheduler = node.scheduler;
			this.timeGenerator = node.timeGenerator;

			Map<String, Assignment> assignments = SchedulingHelper.copyMap(node.assignments);
			Assignment agentsAssignment = assignments.get(agent);
			if (node.assignedNodes.contains(action.getNode())) {
				this.time = node.time;
				this.assignments = node.assignments;
				this.assignedNodes = node.assignedNodes;
				this.bufferedAssignments = node.bufferedAssignments;
				this.completedAssignments = node.completedAssignments;
				return;
			}
			
			
			agentsAssignment.add(action.getNode());
			Set<Workflow.Node> visitedNodes = new HashSet<Workflow.Node>(node.assignedNodes);
			visitedNodes.add(action.getNode());
			
			this.bufferedAssignments = node.bufferedAssignments.copy();
			this.bufferedAssignments.add(action.getNode(), agent);
			BufferedAssignments completedBuffered = this.bufferedAssignments.copy();
			List<Assignment> copied = SchedulingHelper.copy(new ArrayList<Assignment>(assignments.values()));
			this.completedAssignments = this.scheduler.finishSchedule(workflow, this.timeGenerator, copied, completedBuffered, visitedNodes);
			double newTime = completedBuffered.time();
			
			if (newTime <= node.time){ 
				this.time = newTime;
				this.assignments = assignments;
				this.assignedNodes = new HashSet<Workflow.Node>(node.assignedNodes);
				this.assignedNodes.add(action.getNode());
			} else {
				this.time = node.time;
				this.assignments = node.assignments;
				this.assignedNodes = node.assignedNodes;
			}
		}
		
		@Override
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			
			if (!(other instanceof AssignmentNode)) {
				return false;
			}
			
			AssignmentNode node = (AssignmentNode)other;
			if (this.workflow != node.workflow) {
				return false;
			}
			
			if (this.scheduler != node.scheduler){ 
				return false;
			}
			
			if (this.timeGenerator != node.timeGenerator) {
				return false;
			}
			
			return (this.time == node.time && this.assignedNodes.equals(node.assignedNodes));
		}
		
		@Override
		public int hashCode() {
			return this.assignments.hashCode();
		}
		
		public double getTime() {
			return this.time;
		}
		
		public List<Assignment> getAssignments() {
			return new ArrayList<Assignment>(this.assignments.values());
		}
		
		public List<Assignment> getCompletedAssignments() {
			return this.completedAssignments;
		}
		
		public boolean complete(Workflow workflow) {
			return workflow.notVisitedNodes(this.assignedNodes).isEmpty();
		}
		
		public static class AssignmentComparator implements Comparator <AssignmentNode>{

			@Override
			public int compare(AssignmentNode lhs, AssignmentNode rhs) {
				return Double.compare(rhs.time, lhs.time);
			}
		}
	}
}
