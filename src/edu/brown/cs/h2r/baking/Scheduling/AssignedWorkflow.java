package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import edu.brown.cs.h2r.baking.Scheduling.AssignedWorkflow.ActionTime;

public class AssignedWorkflow implements Iterable<ActionTime> {
	private final String agent;
	private final List<ActionTime> actionTimes;
	private double time;
	
	public AssignedWorkflow(String agent) {
		this.agent = agent;
		this.actionTimes = new ArrayList<ActionTime>();
		this.time = 0;
	}
	
	public AssignedWorkflow(AssignedWorkflow other) {
		this.agent = other.agent;
		this.actionTimes = new ArrayList<ActionTime>(other.actionTimes);
		this.time = other.time();
	}
	
	public AssignedWorkflow(String agent, List<Workflow.Node> assignedActions, List<Double> times) {
		this.agent = agent;
		this.actionTimes = this.buildActionTimes(assignedActions, times);
	}
	
	public Iterator<ActionTime> iterator() {
		return new ConditionalIterator(this.actionTimes);
	}
	
	public void addAction(Workflow.Node node, double time) {
		this.actionTimes.add(new ActionTime(node, time));
		this.time += time;
	}
	
	public void add(ActionTime actionTime) {
		this.actionTimes.add(actionTime);
		this.time += actionTime.getTime();
	}
	
	private List<ActionTime> buildActionTimes(List<Workflow.Node> assignedActions, List<Double> times) {
		this.time = 0.0;
		if (assignedActions.size() != times.size()){ 
			return null;
		}
		List<ActionTime> actionTimes = new ArrayList<ActionTime>(assignedActions.size());
		
		for (int i = 0; i < assignedActions.size(); i++) {
			Workflow.Node node = assignedActions.get(i);
			Double time = times.get(i);
			this.time += time;
			ActionTime actionTime = new ActionTime(node, time);
			actionTimes.add(actionTime);
		}
		
		return actionTimes;
	}
	
	public String getId() {
		return this.agent;
	}
	
	public Double nextTime(double currentTime) {
		double sum = 0.0;
		for (ActionTime actionTime : this.actionTimes) {
			sum += actionTime.getTime();
			if (sum > currentTime) {
				return sum;
			}
		}
		return null;
	}
	
	public List<Workflow.Node> nodes(double time) { 
		List<Workflow.Node> nodes = new ArrayList<Workflow.Node>();
		double sum = 0.0;
		for (ActionTime actionTime : this.actionTimes) {
			sum += actionTime.getTime();
			if (sum > time) {
				break;
			}
			nodes.add(actionTime.getNode());
		}
		return nodes;
	}
	
	public boolean waitUntil(double time) {
		if (this.time < time) {
			this.addAction(null, time - this.time);
			return true;
		}
		return false;
	}
	
	public int size() {
		return this.actionTimes.size();
	}
	
	public int realSize() {
		int size = 0;
		for (ActionTime actionTime : this.actionTimes) {
			if (actionTime.getNode() != null) {
				size++;
			}
		}
		return size;
	}
	
	public double time() {
		double t = 0.0;
		for (ActionTime actionTime : this) {
			t += actionTime.getTime();
		}
		this.time = t;
		return t;
	}
	
	public boolean isBusy(double time) {
		return this.time < time;
	}
	
	public static class ConditionalIterator implements Iterator<ActionTime> {

		private final List<ActionTime> list;
		private int currentIndex;
		private ConditionalIterator(List<ActionTime> nodes) {
			this.list = nodes;
			this.currentIndex = 0;
		}
		@Override
		public boolean hasNext() {
			return this.currentIndex < this.list.size();
		}

		@Override
		public ActionTime next() {
			return (this.hasNext()) ? this.list.get(this.currentIndex++) : null;
		}
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		public boolean isNextAvailable(Set<Workflow.Node> visitedNodes) {
			if (!this.hasNext()) {
				return false;
			}
			ActionTime item = this.list.get(this.currentIndex);
			Workflow.Node node = item.getNode();
			return node.isAvailable(visitedNodes);
		}	
	}
	
	public static class ActionTime {
		private final Workflow.Node node;
		private final Double time;
		public ActionTime(Workflow.Node node, Double time) {
			this.node = node;
			this.time = time;
		}
		
		public Workflow.Node getNode() {
			return this.node;
		}
		
		public Double getTime() {
			return this.time;
		}
		
		@Override
		public String toString() {
			return this.node.toString() + ": " + time;
		}
	}
	
}
