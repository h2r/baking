package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.brown.cs.h2r.baking.Scheduling.Assignment.ActionTime;

public class SequenceNode {
	private final double time;
	private final List<Assignment> assignments;
	private final BufferedAssignments bufferedAssignments;
	private final BufferedAssignments completedBuffered;
	private final boolean rearrangeOrder;
	public SequenceNode(List<Assignment> assignments, boolean rearrangeOrder) {
		this.rearrangeOrder = rearrangeOrder;
		this.assignments = SchedulingHelper.copy(assignments);
		List<String> agents = new ArrayList<String>();
		ActionTimeGenerator timeGenerator = null;
		boolean useActualValues = false;
		for (Assignment assignment : this.assignments) {
			if (timeGenerator == null) {
				timeGenerator = assignment.getTimeGenerator();
			}
			agents.add(assignment.getId());
			useActualValues |= assignment.getUseActualValues();
			
		}
		this.bufferedAssignments = new BufferedAssignments(timeGenerator, agents, useActualValues, this.rearrangeOrder);
		this.completedBuffered = this.bufferedAssignments.copyAndFinish(assignments);
		this.time = this.completedBuffered.time();
	}
	
	public SequenceNode(SequenceNode previous, BufferedAssignments buffered) {
		this.assignments = previous.assignments;
		this.bufferedAssignments = buffered;
		this.completedBuffered = this.bufferedAssignments.copyAndFinish(assignments);
		this.time = this.completedBuffered.time();
		this.rearrangeOrder = previous.rearrangeOrder;
	}
	
	public static SequenceNode add(SequenceNode previous, Workflow.Node node, String agent) {
		BufferedAssignments buffered = previous.bufferedAssignments.copy();
		if (buffered.add(node, agent)) {
			return new SequenceNode(previous, buffered);
		}
		return null;	
	}
	
	public static SequenceNode addAndRearrange(SequenceNode previous, Workflow.Node node, String agent) {
		BufferedAssignments buffered = previous.bufferedAssignments.copy();
		Map<String, Assignment> sequencedAssignments = buffered.getAssignmentMap();
		List<Assignment> condensedAssignments = new ArrayList<Assignment>();
		for (Assignment assignment : sequencedAssignments.values()) {
			Assignment condensed = assignment.condense();
			if (assignment.getId().equals(agent)) {
				condensed.add(node);
			}
			condensedAssignments.add(condensed);
		}
		
		buffered.clear();
		buffered.sequenceTasksWithReorder(condensedAssignments);
		return new SequenceNode(previous, buffered);
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		
		if (!(other instanceof SequenceNode)) {
			return false;
		}
		
		SequenceNode node = (SequenceNode)other;
		
		if (this.time != node.time){ 
			return false;
		}
		if (!this.bufferedAssignments.equals(node.bufferedAssignments)) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		return this.bufferedAssignments.hashCode();
	}
	
	@Override
	public String toString() {
		return this.bufferedAssignments.toString() + "\ntime:" + this.time; 
	}
	
	public double getTime() {
		return this.time;
	}
	
	public boolean complete() {
		Set<Workflow.Node> bufferedNodes = new HashSet<Workflow.Node>();
		Set<Workflow.Node> assignmentNodes = new HashSet<Workflow.Node>();
		for (Assignment assignment : this.assignments) {
			for (Workflow.Node node : assignment.nodes()) {
				if (node != null) {
					assignmentNodes.add(node);
				}
			}
		}
		
		for (Assignment assignment : this.bufferedAssignments.getAssignmentMap().values()) {
			for (Workflow.Node node : assignment.nodes()) {
				if (node != null) {
					bufferedNodes.add(node);
				}
			}
		}
		assignmentNodes.removeAll(bufferedNodes);
		return assignmentNodes.isEmpty();
	}
	
	public BufferedAssignments getBufferedAssignments() {
		return this.bufferedAssignments;
	}
	
	public BufferedAssignments getCompletedBuffered() {
		return this.completedBuffered;
	}
	
	public static class SequenceComparator implements Comparator <SequenceNode>{

		@Override
		public int compare(SequenceNode lhs, SequenceNode rhs) {
			int timeRes = Double.compare(rhs.time, lhs.time);
			if (timeRes != 0) {
				return timeRes;
			}
			return Double.compare(lhs.bufferedAssignments.time(), rhs.bufferedAssignments.time()); 
		}
	}
}