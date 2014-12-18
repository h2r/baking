package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.planning.deterministic.informed.Heuristic;
import burlap.behavior.singleagent.planning.deterministic.informed.PrioritizedSearchNode;
import burlap.behavior.singleagent.planning.deterministic.informed.astar.AStar;
import burlap.datastructures.HashIndexedHeap;
import burlap.debugtools.DPrint;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import edu.brown.cs.h2r.baking.Scheduling.AssignedWorkflow.ActionTime;

public class ExhaustiveStarScheduler implements Scheduler {
	public static final String GROUNDED_ACTION_CLASSNAME = "grounded_action";
	public static final String AGENT_CLASSNAME = "agent";
	public static final String ASSIGNMENTS = "assignments";
	public static final String ASSIGNMENT_CLASS = "assignment";
	private static final int debugCode = 101;
	
	private final int maxDepth;
	
	public ExhaustiveStarScheduler() {
		this.maxDepth = -1;
		DPrint.toggleCode(debugCode, false);
	}
	
	public ExhaustiveStarScheduler(int maxDepth) {
		this.maxDepth = maxDepth;
	}

	@Override
	public List<AssignedWorkflow> schedule(Workflow workflow, List<String> agents,
			ActionTimeGenerator actionTimeLookup) {
		HashIndexedHeap<AssignmentNode> openQueue = new HashIndexedHeap<AssignmentNode>(new AssignmentNode.AssignmentComparator());
		AssignmentNode firstNode = new AssignmentNode(workflow, new GreedyScheduler(), actionTimeLookup, agents);
		openQueue.insert(firstNode);
		Map<String, AssignedWorkflow> assignedWorkflows = new HashMap<String, AssignedWorkflow>();
		
		for (String agent : agents) {
			AssignedWorkflow assignedWorkflow = new AssignedWorkflow(agent);
			assignedWorkflows.put(agent, assignedWorkflow);
		}
		int previousSize = 0;
		return this.assignActions(workflow, actionTimeLookup, openQueue, agents);
	}
	
	public List<AssignedWorkflow> finishSchedule(Workflow workflow, ActionTimeGenerator actionTimeLookup, 
			List<AssignedWorkflow> assignedWorkflows, Set<Workflow.Node> visitedNodes) {
		
		HashIndexedHeap<AssignmentNode> openQueue = new HashIndexedHeap<AssignmentNode>(new AssignmentNode.AssignmentComparator());
		
		Map<String, AssignedWorkflow> assignedWorkflowMap = new HashMap<String, AssignedWorkflow>();
		
		for (AssignedWorkflow assignedWorkflow : assignedWorkflows) {
			assignedWorkflowMap.put(assignedWorkflow.getId(), assignedWorkflow);
		}
		List<String> agents = new ArrayList<String>(assignedWorkflowMap.keySet());
		int previousSize = 0;
		while (previousSize != workflow.size()) {
			this.assignActions(workflow, actionTimeLookup, openQueue, agents);
			
			previousSize = 0;
			for (AssignedWorkflow assignedWorkflow : assignedWorkflows) {
				previousSize += assignedWorkflow.size();
			}
		}
		
		return new ArrayList<AssignedWorkflow>(assignedWorkflows);
	}
	
	
	
	private List<AssignedWorkflow> assignActions(Workflow workflow, ActionTimeGenerator actionTimeLookup, 
			HashIndexedHeap<AssignmentNode> openQueue, List<String> agents) {
		int checkedNodes = 0;
		while(openQueue.peek() != null) {
			checkedNodes++;
			
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
					double time = actionTimeLookup.get(ga);
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
	
	
	protected Domain generateDomain(List<String> agents) {
		Domain domain = new SADomain();
		ObjectClass assignment = new ObjectClass(domain, ASSIGNMENT_CLASS);
		for (String agent : agents) {
			assignment.addAttribute(new Attribute(domain, agent, Attribute.AttributeType.STRING));
		}
		
		Action assign = new AssignAction(domain);
		return domain;
	}
	
	public static class AssignAction extends Action {
		public AssignAction(Domain domain) {
			super("assign", domain, new String[] {GROUNDED_ACTION_CLASSNAME, AGENT_CLASSNAME});
		}

		@Override
		protected State performActionHelper(State s, String[] params) {
			ObjectInstance assignments = s.getObject(ASSIGNMENTS);
			String agent = params[0];
			String action = params[1];
			String currentAssignments = assignments.getStringValForAttribute(agent);
			currentAssignments = currentAssignments.concat("," + action);
			ObjectInstance newAssignments = assignments.changeValue(agent, currentAssignments);
			return s.replaceObject(assignments, newAssignments);
		}
	}
	
	public static class AssignmentNode {
		private final double time;
		private final Map<String, AssignedWorkflow> assignments;
		private final Set<Workflow.Node> assignedNodes;
		private final Workflow workflow;
		private final Scheduler scheduler;
		private final ActionTimeGenerator timeGenerator;
		private final List<AssignedWorkflow> completedAssignments;
		
		public AssignmentNode(Workflow workflow, Scheduler heuristicScheduler, ActionTimeGenerator timeGenerator, List<String> agents) {
			this.assignments = new HashMap<String, AssignedWorkflow>();
			for (String agent : agents) {
				this.assignments.put(agent, new AssignedWorkflow(agent));
			}
			this.assignedNodes = new HashSet<Workflow.Node>();
			this.workflow = workflow;
			this.scheduler = heuristicScheduler;
			this.timeGenerator = timeGenerator;
			List<AssignedWorkflow> copied = SchedulingHelper.copy(new ArrayList<AssignedWorkflow>(this.assignments.values()));
			Set<Workflow.Node> visitedNodes = new HashSet<Workflow.Node>(this.assignedNodes); 
			this.completedAssignments = 
					this.scheduler.finishSchedule(workflow, this.timeGenerator, copied , visitedNodes);
			
			this.time = SchedulingHelper.computeSequenceTime(copied);
			
		}
		
		public AssignmentNode(AssignmentNode node, String agent, ActionTime action) {
			this.workflow = node.workflow;
			this.scheduler = node.scheduler;
			this.timeGenerator = node.timeGenerator;

			Map<String, AssignedWorkflow> assignments = SchedulingHelper.copyMap(node.assignments);
			assignments.get(agent).add(action);
			Set<Workflow.Node> visitedNodes = new HashSet<Workflow.Node>(node.assignedNodes);
			visitedNodes.add(action.getNode());
			List<AssignedWorkflow> copied = SchedulingHelper.copy(new ArrayList<AssignedWorkflow>(assignments.values()));
			
			this.completedAssignments = this.scheduler.finishSchedule(workflow, this.timeGenerator, copied , visitedNodes);
			double newTime = SchedulingHelper.computeSequenceTime(copied);
			
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
		
		public List<AssignedWorkflow> getAssignments() {
			return new ArrayList<AssignedWorkflow>(this.assignments.values());
		}
		
		public List<AssignedWorkflow> getCompletedAssignments() {
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
