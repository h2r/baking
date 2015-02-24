package edu.brown.cs.h2r.baking.Scheduling.Multitiered;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import burlap.datastructures.HashIndexedHeap;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;

public class HeuristicSearchScheduler implements Scheduler {
	private final boolean useActualValues;
	private final Scheduler heuristic;
	private final Sequencer sequencer;
	public HeuristicSearchScheduler(Scheduler heuristic, Sequencer sequencer, boolean useActualValues) {
		this.useActualValues = useActualValues;
		this.heuristic = heuristic;
		this.sequencer = sequencer;
	}

	@Override
	public Assignments schedule(Workflow workflow, List<String> agents,
			ActionTimeGenerator timeGenerator) {
		Assignments assignments = null;
		while (assignments == null) {
			assignments = this.assignActions(workflow,  new Assignments(agents, timeGenerator, this.useActualValues), timeGenerator);
		}
		return assignments;
	}

	@Override
	public Assignments finishSchedule(Workflow workflow, Assignments assignments, ActionTimeGenerator timeGenerator) {
		return this.assignActions(workflow, assignments, timeGenerator);
	}
	
	public Assignments assignActions(Workflow workflow, Assignments assignments, ActionTimeGenerator timeGenerator, double actualTime) {
		HashIndexedHeap<HeuristicSearchNode> queue = new HashIndexedHeap<HeuristicSearchNode>(new HeuristicSearchNode.HeuristicSearchNodeComparator());
		Assignments fullySequenced = this.heuristic.finishSchedule(workflow, assignments.copy(), timeGenerator);
		if (fullySequenced == null) {
			System.err.println("Finishing schedule failed");
			return null;
		}
		HeuristicSearchNode first = new HeuristicSearchNode(workflow, assignments, fullySequenced);
		queue.insert(first);
		boolean didPrint = false;
		int numVisited = 0;
		double bestTime = fullySequenced.time();
		int numBestChanged = 0;
		while (queue.peek() != null) {
			HeuristicSearchNode next = queue.poll();
			if (next.complete()) {
				System.out.println("Visited, " + numVisited);
				System.out.println("Best changed, " + numBestChanged);
				return next.getSequencedAssignments();
			}
			Assignments currentAssignments = next.getCurrentAssignments();
			
			Set<Subtask> visited = new HashSet<Subtask>(currentAssignments.getAllSubtasks());
			Assignments nextSequenced =  next.getSequencedAssignments();
			List<Subtask> unassigned = workflow.getSubtasks();
			unassigned.removeAll(visited);
			//System.out.println(visited.size() + "/" + workflow.getSubtasks().size() + ", " + nextSequenced.time());
			//System.out.println(unassigned.toString());
			List<Subtask> available = workflow.getAvailableSubtasks(visited);
			for (Subtask subtask : available) {
				for (String agent : assignments.getAgents()) {
					numVisited++;
					
					Assignments partiallySequenced = 
							this.sequencer.continueSequence(currentAssignments, subtask, agent, timeGenerator, workflow);
					if (partiallySequenced != null) {
						fullySequenced = this.heuristic.finishSchedule(workflow, partiallySequenced, timeGenerator);
						if (fullySequenced != null) {
							//if (MILPScheduler.checkAssignments(workflow, sequenced)) {
							//	sequenced = this.heuristic.finishSchedule(workflow, copy.copy(), timeGenerator);
							//	System.err.println("Sequencing failed");
							//}
							double fStime = fullySequenced.time();
							double nSTime = nextSequenced.time();
							if (fStime <= nSTime) {
								if (fStime < bestTime) {
									numBestChanged++;
									bestTime = fStime;
								}
								if ((fStime - actualTime)/actualTime < 0.01 && !didPrint) {
									didPrint = true;
									int depth = visited.size() + 1;
									System.out.println("Best changed, " + numBestChanged);
									System.out.println("Found actual at depth, " + depth);
									System.out.println("Within tolerance visited, " + numVisited);
									return fullySequenced;
								}
								HeuristicSearchNode newNode = new HeuristicSearchNode(workflow, partiallySequenced, fullySequenced);
								queue.insert(newNode);
							}
							
						}
					}
						
					
				}
			}
			
		}
		System.err.println("Search scheduler could not complete assignments");
		return null;
	}
	
	private Assignments assignActions(Workflow workflow, Assignments assignments, ActionTimeGenerator timeGenerator) {
		HashIndexedHeap<HeuristicSearchNode> queue = new HashIndexedHeap<HeuristicSearchNode>(new HeuristicSearchNode.HeuristicSearchNodeComparator());
		Assignments fullySequenced = this.heuristic.finishSchedule(workflow, assignments.copy(), timeGenerator);
		if (fullySequenced == null) {
			System.err.println("Finishing schedule failed");
			return null;
		}
		HeuristicSearchNode first = new HeuristicSearchNode(workflow, assignments, fullySequenced);
		queue.insert(first);
		
		while (queue.peek() != null) {
			HeuristicSearchNode next = queue.poll();
			if (next.complete()) {
				return next.getSequencedAssignments();
			}
			
			Assignments currentAssignments = next.getCurrentAssignments();
			
			Set<Subtask> visited = new HashSet<Subtask>(currentAssignments.getAllSubtasks());
			Assignments nextSequenced =  next.getSequencedAssignments();
			List<Subtask> unassigned = workflow.getSubtasks();
			unassigned.removeAll(visited);
			List<Subtask> available = workflow.getAvailableSubtasks(visited);
			int numAdded = 0;
			if (available.size() > 0) {
				Collections.shuffle(available);
				for (Subtask subtask : available) {
					for (String agent : assignments.getAgents()) {
						Assignments partiallySequenced = 
								this.sequencer.continueSequence(currentAssignments, subtask, agent, timeGenerator, workflow);
						if (partiallySequenced != null) {
							fullySequenced = this.heuristic.finishSchedule(workflow, partiallySequenced, timeGenerator);
							if (fullySequenced != null) {
								//if (MILPScheduler.checkAssignments(workflow, sequenced)) {
								//	sequenced = this.heuristic.finishSchedule(workflow, copy.copy(), timeGenerator);
								//	System.err.println("Sequencing failed");
								//}
								double fStime = fullySequenced.time();
								double nSTime = nextSequenced.time();
								if (fStime < nSTime) {
									HeuristicSearchNode newNode = new HeuristicSearchNode(workflow, partiallySequenced, fullySequenced);
									queue.insert(newNode);
									numAdded++;
								}
								
							}
						}
						
							
					}
				}
				if (numAdded == 0) {
					return next.getSequencedAssignments();
				}
			}
			
		}
		System.err.println("Search scheduler could not complete assignments");
		return null;		
	}

	@Override
	public boolean isUsingActualValues() {
		return this.useActualValues;
	}
	
	public String getDescription() {
		return this.getClass().getSimpleName() + " - " + this.heuristic.getDescription();
	}

}
