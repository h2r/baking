package edu.brown.cs.h2r.baking.Scheduling;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.datastructures.HashIndexedHeap;

public class HeuristicSearchSequencer extends Sequencer {
	private final boolean rearrangeOrder;
	private final Sequencer sequencer;
	public HeuristicSearchSequencer(boolean useActualValues, boolean rearrangeOrder) {
		super(useActualValues);
		this.rearrangeOrder = rearrangeOrder;
		this.sequencer = new BasicSequencer(this.useActualValues);
	}
	
	public Assignments finishSequence(Assignments assignments, Assignments sequenced, ActionTimeGenerator timeGenerator) {
		HashIndexedHeap<SequenceNode> openQueue = new HashIndexedHeap<SequenceNode>(new SequenceNode.SequenceComparator());
		Assignments completed = this.sequencer.finishSequence(assignments, sequenced, timeGenerator);
		SequenceNode first = new SequenceNode(sequenced, completed);
		openQueue.insert(first);
		return this.sequence(openQueue, assignments, timeGenerator);
	}
	
	private Assignments sequence(HashIndexedHeap<SequenceNode> openQueue, Assignments assignments, ActionTimeGenerator timeGenerator) {
		while(openQueue.peek() != null) {
			SequenceNode next = openQueue.poll();
			
			if (next.complete()) {
				return next.getCompleted();
			}
			
			Collection<Workflow.Node> visited = next.getAssignments().subtasks();
			Assignments currentSequenced = next.getAssignments();
			for (Assignment assignment : assignments) {
				for (Workflow.Node node : assignment.nodes(visited)) {
					Assignments copied = currentSequenced.copy();
					if (copied.add(node, assignment.getId())) {
						Assignments completed = this.sequencer.finishSequence(assignments, copied, timeGenerator);
						SequenceNode newNode = new SequenceNode(copied, completed);
						openQueue.insert(newNode);
					}
				}
			}	
		}
		throw new RuntimeException("Could not complete sequencing");
	}
	public String getDescription() {
		return this.getClass().getSimpleName();
	}

}
