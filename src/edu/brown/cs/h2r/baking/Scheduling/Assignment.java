package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.Scheduling.Assignment.ActionTime;
import edu.brown.cs.h2r.baking.Scheduling.Workflow.Node;

public class Assignment implements Iterable<ActionTime> {
	private final String agent;
	private final List<Double> times;
	private final List<Double> completionTimes;
	private final List<Workflow.Node> nodes; 
	private final ActionTimeGenerator timeGenerator;
	private double time;
	private final boolean useActualValues;
	
	@Override
	public String toString() {
		return nodes.toString();
	}
	
	public Assignment(String agent, ActionTimeGenerator timeGenerator, boolean useActualValues) {
		this.agent = agent;
		this.nodes = new ArrayList<Workflow.Node>();
		this.time = 0;
		this.times = new ArrayList<Double>();
		this.completionTimes = new ArrayList<Double>();
		this.timeGenerator = timeGenerator;
		this.useActualValues = useActualValues;
	}
	
	public Assignment(Assignment other) {
		this.agent = other.agent;
		this.nodes = new ArrayList<Workflow.Node>(other.nodes);
		this.time = other.time;
		this.times = new ArrayList<Double>(other.times);
		this.completionTimes = new ArrayList<Double>(other.completionTimes);
		this.timeGenerator = other.timeGenerator;
		this.useActualValues = other.getUseActualValues();
	}
	
	public Assignment(String agent, List<Workflow.Node> assignedActions,  ActionTimeGenerator timeGenerator, boolean useActualValues ) {
		this.agent = agent;
		this.nodes = Collections.unmodifiableList(assignedActions);
		this.timeGenerator = timeGenerator;
		List<Double> times = new ArrayList<Double>(this.nodes.size());
		for (Workflow.Node node : this.nodes) {
			GroundedAction ga = node.getAction();
			ga.params[0] = this.agent;
			double time = timeGenerator.get(ga, false);
			times.add(time);
		}
		this.useActualValues = useActualValues;
		
		
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
	
	public Workflow.Node first() {
		if (this.size() == 0) {
			return null;
		}
		return this.nodes.get(0);
	}
	
	public void add(Workflow.Node node) {
		GroundedAction ga = node.getAction();
		ga.params[0] = this.agent;
		double time = this.timeGenerator.get(ga, this.getUseActualValues());
		this.nodes.add(node);
		this.times.add(time);
		this.time += time;
		this.completionTimes.add(this.time);
	}
	
	public String getId() {
		return this.agent;
	}
	
	public ActionTimeGenerator getTimeGenerator() {
		return this.timeGenerator;
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
		if (begin == null) {
			return null;
		} else if (beginTime == this.completionTimes.get(begin)) {
			beginTime++;
		}
		
		if (end == null) {
			end = this.nodes.size();
		} else if (endTime == this.completionTimes.get(end)) {
			end++;
		}
		
		return this.nodes.subList(begin, end);
	}
	
	public boolean waitUntil(double endTime) {
		if (this.time < endTime) {
			double waitTime = endTime - this.time;
			
			this.nodes.add(null);
			this.times.add(waitTime);
			this.time += waitTime;
			this.completionTimes.add(this.time);
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
	
	public boolean getUseActualValues() {
		return useActualValues;
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