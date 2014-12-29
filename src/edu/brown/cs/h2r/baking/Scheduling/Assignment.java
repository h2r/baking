package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import edu.brown.cs.h2r.baking.Scheduling.Assignment.ActionTime;
import edu.brown.cs.h2r.baking.Scheduling.Workflow.Node;

public class Assignment implements Iterable<ActionTime> {
	private final String agent;
	private final List<Double> times;
	private final List<Double> completionTimes;
	private final List<Workflow.Node> nodes; 
	private double time;
	
	@Override
	public String toString() {
		return nodes.toString();
	}
	
	public Assignment(String agent) {
		this.agent = agent;
		this.nodes = new ArrayList<Workflow.Node>();
		this.time = 0;
		this.times = new ArrayList<Double>();
		this.completionTimes = new ArrayList<Double>();
	}
	
	public Assignment(Assignment other) {
		this.agent = other.agent;
		this.nodes = new ArrayList<Workflow.Node>(other.nodes);
		this.time = other.time;
		this.times = new ArrayList<Double>(other.times);
		this.completionTimes = new ArrayList<Double>(other.completionTimes);
	}
	
	public Assignment(String agent, List<Workflow.Node> assignedActions, List<Double> times) {
		this.agent = agent;
		this.nodes = Collections.unmodifiableList(assignedActions);
		this.times = Collections.unmodifiableList(times);
		double sum = 0.0;
		List<Double> completionTimes = new ArrayList<Double>(times.size());
		for (Double time : times) {
			sum += time;
			completionTimes.add(sum);
		}
		this.completionTimes = Collections.unmodifiableList(completionTimes);
		this.time = sum;
		
	}

	@Override 
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		
		if (!(other instanceof Assignment)) {
			return false;
		}
		
		Assignment workflow = (Assignment)other;
		
		if (this.nodes.size() != workflow.nodes.size()) {
			return false;
		}
		
		Iterator<ActionTime> thisIt = this.iterator();
		Iterator<ActionTime> otherIt = workflow.iterator();
		while (thisIt.hasNext() && otherIt.hasNext()) {
			if (!thisIt.next().equals(otherIt.next())) {
				return false;
			}
		}
		return true;
	}
	
	public Iterator<ActionTime> iterator() {
		return new AssignmentIterator(this.nodes, this.times);
	}
	
	public void add(Workflow.Node node, double time) {
		this.nodes.add(node);
		this.times.add(time);
		this.time += time;
		this.completionTimes.add(this.time);
	}
	
	public String getId() {
		return this.agent;
	}
	
	public boolean contains(Node node) {
		return this.nodes.contains(node);
	}
	
	
	public Integer position(double time) {
		Integer place = Collections.binarySearch(this.completionTimes, time);
		
		if (place < 0) {
			place = - (place + 1);
		} 
		
		if (place == this.completionTimes.size()) {
			return null;
		}
		
		return place;
	}
	
	public Double nextTime(double currentTime) {
		if (this.completionTimes.isEmpty()) {
			return null;
		}
		
		Integer place = Collections.binarySearch(this.completionTimes, time);
		
		if (place < 0) {
			place = - (place + 1);
		} else {
			place++;
		}
		
		if (place == this.completionTimes.size()) {
			return this.completionTimes.get(place - 1);
		}
		
		return this.completionTimes.get(place);
	}
	
	public List<Workflow.Node> nodes() { 
		return this.nodes;
	}
	
	public List<Workflow.Node> nodes(double time) { 
		Integer end = this.position(time);
		end = (end == null) ? this.nodes.size() : end + 1;
		
		return this.nodes.subList(0, end);
	}
	
	public List<Workflow.Node> nodes(double beginTime, double endTime) { 
		if (beginTime >= this.time || beginTime >= endTime) {
			return null;
		}
		
		Integer begin = this.position(beginTime);
		Integer end = this.position(endTime);
		end = (end == null) ? this.nodes.size() : end + 1;
		return this.nodes.subList(begin, end);
	}
	
	public boolean waitUntil(double time) {
		if (this.time < time) {
			this.add(null, time - this.time);
			return true;
		}
		return false;
	}
	
	public int size() {
		return this.nodes.size();
	}
	
	public int realSize() {
		int size = 0;
		for (Workflow.Node node : this.nodes) {
			if (node != null) {
				size++;
			}
		}
		return size;
	}
	
	public double time() {
		return this.time;
	}
	
	public boolean isBusy(double time) {
		return this.time < time;
	}
	
	public static class AssignmentIterator implements ListIterator<ActionTime> {

		private final List<Workflow.Node> nodes;
		private final List<Double> times;
		private int currentIndex;
		private AssignmentIterator(List<Workflow.Node> nodes, List<Double> times) {
			this.nodes = nodes;
			this.times = times;
			this.currentIndex = 0;
		}
		
		@Override
		public boolean hasNext() {
			return this.currentIndex < this.nodes.size();
		}

		@Override
		public ActionTime next() {
			if (!this.hasNext()) {
				return null;
			}
			Workflow.Node node = this.nodes.get(this.currentIndex);
			Double time = this.times.get(this.currentIndex);
			this.currentIndex++;
			return new ActionTime(node, time);
		}
		
		@Override
		public int nextIndex() {
			return this.currentIndex;
		}

		
		public boolean isNextAvailable(Set<Workflow.Node> visitedNodes) {
			if (!this.hasNext()) {
				return false;
			}
			Workflow.Node node = this.nodes.get(this.currentIndex);
			return node.isAvailable(visitedNodes);
		}

		@Override
		public boolean hasPrevious() {
			return this.currentIndex > 0;
		}

		@Override
		public ActionTime previous() {
			if (!this.hasPrevious()) {
				return null;
			}
			this.currentIndex--;
			Workflow.Node node = this.nodes.get(this.currentIndex);
			Double time = this.times.get(this.currentIndex);
			return new ActionTime(node, time);
		}

		
		@Override
		public int previousIndex() {
			return this.currentIndex - 1;
		}
		
		@Override
		public void remove() {
			throw new UnsupportedOperationException();
		}
		
		

		@Override
		public void set(ActionTime e) {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public void add(ActionTime e) {
			throw new UnsupportedOperationException();			
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
		public boolean equals(Object other) {
			if (this == other) {
				return true;
			}
			if (!(other instanceof ActionTime)) {
				return false;
			}
			ActionTime otherTime = (ActionTime)other;
			return (this.time == otherTime.time && this.node.equals(otherTime.node));
		}
		
		@Override
		public String toString() {
			return this.node.toString() + ": " + time;
		}
	}

	
}