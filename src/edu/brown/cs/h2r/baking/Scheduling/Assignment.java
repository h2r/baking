package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
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
	
	public Assignment(List<ActionTime> actionTimes, String agent, ActionTimeGenerator timeGenerator, boolean useActualValues) {
		this.agent = agent;
		this.timeGenerator = timeGenerator;
		this.useActualValues = useActualValues;
		
		this.nodes = new ArrayList<Workflow.Node>(actionTimes.size());
		this.times = new ArrayList<Double>(actionTimes.size());
		this.completionTimes = new ArrayList<Double>(actionTimes.size());
		
		this.time = 0;
		for (ActionTime actionTime : actionTimes) {
			this.nodes.add(actionTime.getNode());
			double time = actionTime.getTime();
			this.times.add(time);
			this.time += time;
			this.completionTimes.add(this.time);
		}
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
	
	@Override 
	public int hashCode() {
		return Objects.hash(this.nodes, this.times);
	}
	
	public Iterator<ActionTime> iterator() {
		return new AssignmentIterator(this.nodes, this.times);
	}
	
	public void shuffle() {
		List<ActionTime> actionTimes = new ArrayList<ActionTime>();
		for (ActionTime actionTime : this) {
			actionTimes.add(actionTime);
		}
		Collections.shuffle(actionTimes);
		
		this.nodes.clear();
		this.times.clear();
		this.completionTimes.clear();
		
		
		double sum = 0.0;
		for (ActionTime actionTime : actionTimes) {
			this.nodes.add(actionTime.getNode());
			double time = actionTime.getTime();
			this.times.add(time);
			sum += time;
			this.completionTimes.add(sum);
		}
		this.time = sum;
	}
	
	public Workflow.Node first() {
		if (this.size() == 0) {
			return null;
		}
		return this.nodes.get(0);
	}
	
	public void add(Workflow.Node node) {
		GroundedAction ga = node.getAction(this.agent);
		double time = this.timeGenerator.get(ga, this.getUseActualValues());
		this.nodes.add(node);
		this.times.add(time);
		this.time += time;
		this.completionTimes.add(this.time);
	}
	
	public void add(int position, Workflow.Node node) {
		GroundedAction ga = node.getAction(this.agent);
		double time = this.timeGenerator.get(ga, this.getUseActualValues());
		
		this.nodes.add(position, node);
		this.times.add(position, time);
		this.completionTimes.add(this.time);
		double sum = 0.0;
		for (int i = position ; i < this.completionTimes.size(); i++) {
			sum += this.times.get(i);
			this.completionTimes.set(i, sum);
		}
		
	}
	
	private void rebuildCompletionTimes(int start, int end) {
		start = Math.max(0, start);
		end = Math.min(end, this.size());
		if (start >= end) {
			return;
		}
		
		double sum = (start == 0) ? 0.0 : this.completionTimes.get(start - 1);
		for (int i = start; i < end; i++) {
			double time = this.times.get(i);
			sum += time;
			this.completionTimes.set(i, sum);
		}
	}
	
	public void swap(int first, int second) {
		if (first < this.size() && second < this.size()) {
			Workflow.Node nodeTmp = this.nodes.get(first);
			double timeTmp = this.times.get(first);
			
			this.nodes.set(first, this.nodes.get(second));
			this.times.set(first, this.times.get(second));
			this.nodes.set(second, nodeTmp);
			this.times.set(second, timeTmp);
		}
		this.rebuildCompletionTimes(first, second);
	}
	
	public void trim() {
		for (int i = this.nodes.size() - 1; i >= 0; i--) {
			if (this.nodes.get(i) == null) {
				this.nodes.remove(i);
				this.times.remove(i);
				this.completionTimes.remove(i);
			} else {
				return;
			}
		}
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
	
	// Not the ideal sorted workflow, as it allows actions with dependencies on others go first
	public Assignment sort(Workflow workflow) {
		Set<Workflow.Node> nodes = new HashSet<Workflow.Node>();
		for (Workflow.Node node : workflow) nodes.add(node);
		nodes.removeAll(this.nodes);
		
		List<ActionTime> sorted = new ArrayList<ActionTime>(this.size());
		boolean keepGoing = true;
		while (keepGoing) {
			keepGoing = false;
			for (ActionTime actionTime : this) {
				Workflow.Node node = actionTime.getNode();
				if (node.isAvailable(nodes) && !nodes.contains(node)) {
					sorted.add(actionTime);
					nodes.add(node);
					keepGoing = true;
				}
			}
		}
		if (sorted.size() != this.size()) {
			throw new RuntimeException("Sorting failed");
		}
		
		return new Assignment(sorted, this.agent, this.timeGenerator, this.useActualValues);
	}
	
	public Assignment condense() {
		List<ActionTime> sorted = new ArrayList<ActionTime>(this.size());
		for (ActionTime actionTime : this) {
			if (actionTime.node != null) {
				sorted.add(actionTime);
			}
		}
		return new Assignment(sorted, this.agent, this.timeGenerator, this.useActualValues);
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
		
		Integer place = Collections.binarySearch(this.completionTimes, currentTime);
		
		if (place < 0) {
			place = - (place + 1);
		} else if (currentTime == this.completionTimes.get(place)) {
			place++;
		}
		
		if (place == this.completionTimes.size()) {
			return this.completionTimes.get(place - 1);
		}
		
		return this.completionTimes.get(place);
	}
	
	public List<Workflow.Node> nodes(Set<Workflow.Node> visited) { 
		List<Workflow.Node> nodes = new ArrayList<Workflow.Node>();
		for (Workflow.Node node : this.nodes){ 
			if (node != null && node.isAvailable(visited)) {
				nodes.add(node);
			}
		}
		
		return nodes;
	}
	
	public List<Double> times() {
		return this.times;
	}
	
	public List<Double> completionTimes() {
		return this.completionTimes;
	}
	
	public List<Workflow.Node> nodes() { 
		return this.nodes;
	}
	
	
	
	public List<Workflow.Node> nodes(double time) { 
		return this.nodes(0.0, time);
	}
	
	// Includes nodes that are overlap with beginTime, endTime and occur inbetween
	public List<Workflow.Node> slice(double beginTime, double endTime) {
		if (beginTime >= this.time || beginTime > endTime) {
			return new ArrayList<Workflow.Node>();
		}
		
		Integer begin = this.position(beginTime);
		Integer end = this.position(endTime);
		if (begin == null) {
			return new ArrayList<Workflow.Node>();
		} else if (beginTime == this.completionTimes.get(begin)) {
			begin++;
		}
		
		if (end == null) {
			end = this.nodes.size();
		} else if (endTime < this.completionTimes.get(end)) {
			end++;
		}
		
		return this.nodes.subList(begin, end);
	}
	
	// Only includes nodes completely between beginTime and endTime
	public List<Workflow.Node> nodes(double beginTime, double endTime) { 
		if (beginTime >= this.time || beginTime >= endTime) {
			return new ArrayList<Workflow.Node>();
		}
		
		Integer begin = this.position(beginTime);
		Integer end = this.position(endTime);
		if (begin == null) {
			return null;
		} else if (beginTime == this.completionTimes.get(begin)) {
			begin++;
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
			if (!this.time.equals(otherTime.time)){ 
				return false;
			}
			
			if ((this.node == null) != (otherTime.node == null)) {
				return false;
			}
			
			if (this.node == null) {
				return true;
			}
			
			if (!this.node.equals(otherTime.node)) {
				return false;
			}
			return true;
		}
		
		@Override
		public String toString() {
			return this.node.toString() + ": " + time;
		}
	}

	
}