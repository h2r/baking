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
import edu.brown.cs.h2r.baking.actions.ResetAction;

public class ExhaustiveStarScheduler implements Scheduler {
	public static final String GROUNDED_ACTION_CLASSNAME = "grounded_action";
	public static final String AGENT_CLASSNAME = "agent";
	public static final String ASSIGNMENTS = "assignments";
	public static final String ASSIGNMENT_CLASS = "assignment";
	private final Sequencer sequencer;
	private static final int MAX_SHUFFLES = 10;
	private final Scheduler heuristic;
	private static final int debugCode = 101;
	private final Boolean useActualValues;
	
	public ExhaustiveStarScheduler(boolean useActualValues) {
		DPrint.toggleCode(debugCode, false);
		this.useActualValues = useActualValues;
		this.heuristic = new GreedyScheduler(this.useActualValues);
		this.sequencer = new BasicSequencer(this.useActualValues);
	}
	
	public ExhaustiveStarScheduler(Scheduler heuristic) {
		DPrint.toggleCode(debugCode, false);
		this.useActualValues = heuristic.isUsingActualValues();
		this.heuristic = heuristic;
		this.sequencer = new BasicSequencer(this.useActualValues);
	}

	public boolean isUsingActualValues() {
		return this.useActualValues;
	}
	@Override
	public Assignments schedule(Workflow workflow, List<String> agents,
			ActionTimeGenerator timeGenerator) {
		Assignments bestSchedule = null;
		double bestTime = Double.MAX_VALUE;
		int choice = 0;
		for (int i = 0; i < MAX_SHUFFLES; i++) {
			HashIndexedHeap<AssignmentNode> openQueue = new HashIndexedHeap<AssignmentNode>(new AssignmentNode.AssignmentComparator());
			Assignments assignments = new Assignments(timeGenerator, agents, workflow.getStartState(), this.useActualValues, false);
			
			Assignments sequenced = this.heuristic.finishSchedule(workflow, assignments, timeGenerator);
			if (sequenced == null) {
				throw new RuntimeException("Scheduling failed");
			}
			
			AssignmentNode firstNode = new AssignmentNode(workflow, assignments, sequenced);
			openQueue.insert(firstNode);
			
			Workflow sorted = workflow.sort();
			Assignments completedAssignments = this.assignActions(sorted, openQueue, timeGenerator);
			
			if (completedAssignments.time() < bestTime) {
				bestSchedule = completedAssignments;
				bestTime = completedAssignments.time();
				choice = i;
			}
			//System.out.println(i + " " + newBuffered.time());
			
		}
		//System.out.println("Best choice " + choice);
		return bestSchedule;
	}
	
	public Assignments finishSchedule(Workflow workflow, Assignments assignments, ActionTimeGenerator timeGenerator) {
		
		HashIndexedHeap<AssignmentNode> openQueue = new HashIndexedHeap<AssignmentNode>(new AssignmentNode.AssignmentComparator());
		Assignments sequenced = this.heuristic.finishSchedule(workflow, assignments, timeGenerator);
		if (sequenced == null) {
			throw new RuntimeException("Scheduling failed");
		}
		AssignmentNode firstNode = new AssignmentNode(workflow, assignments, sequenced);
		openQueue.insert(firstNode);
		
		return this.assignActions(workflow, openQueue, timeGenerator);
	}
	
	
	
	private Assignments assignActions(Workflow workflow, HashIndexedHeap<AssignmentNode> openQueue, ActionTimeGenerator timeGenerator) {
		int checkedNodes = 0;
		if (openQueue.peek() == null) {
			return null;
		}
		//DPrint.toggleCode(debugCode, true);
		while(openQueue.peek() != null) {
			checkedNodes++;
			AssignmentNode node = openQueue.poll();
			if (node.complete()) {
				return node.getSequenced();
			}
			DPrint.cl(debugCode, "Current value: " + node.getTime());
			
			
			List<Workflow.Node> availableActions = workflow.getAvailableNodes(node.getAssignedNodes());
			
			int numAddedNodes = 0;
			Assignments currentAssignments = node.getAssignments();
			DPrint.cl(debugCode, "Current depth: " + currentAssignments.subtasks().size());
			
			for (Workflow.Node action : availableActions) {
				for (String agent : currentAssignments.agents()){
					if (action.getAction().action instanceof ResetAction && !agent.equals("human")) {
						continue;
					}
					
					Assignments added = this.sequencer.continueSequence(currentAssignments, action, agent, timeGenerator, workflow);
					
					if (added == null) {
						continue;
					}
					
					Assignments completed = this.heuristic.finishSchedule(workflow, added, timeGenerator);
					if (completed == null) {
						completed = this.heuristic.finishSchedule(workflow, added, timeGenerator);
					} else {
						Set<Workflow.Node> assigned = new HashSet<Workflow.Node>(); 
						for (Assignment assignment : completed) {
							for (Workflow.Node subtask : assignment.nodes()) {
								if (subtask != null) {
									if (!assigned.add(subtask)) {
										System.err.println("Duplicates assigned");
									}
								}
							}
						}
						
						
						
						AssignmentNode newNode = new AssignmentNode(workflow, added, completed);
						if (openQueue.containsInstance(newNode) == null && !newNode.equals(node)) {
							openQueue.insert(newNode);
							numAddedNodes++;
						}
					}
				}
			}
			if (numAddedNodes == 0) {
				return node.getSequenced();
			}
		}
		return null;
	}
}
