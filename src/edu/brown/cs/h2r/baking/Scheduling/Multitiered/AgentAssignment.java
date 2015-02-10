package edu.brown.cs.h2r.baking.Scheduling.Multitiered;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.Set;

import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;
import edu.brown.cs.h2r.baking.Scheduling.Assignment.ActionTime;
import edu.brown.cs.h2r.baking.Scheduling.Workflow.Node;

public class AgentAssignment implements Iterable<AssignedSubtask> {
	private final String agent;
	private final List<Double> times;
	private final List<Double> completionTimes;
	private final List<Subtask> subtasks; 
	private final ActionTimeGenerator timeGenerator;

	private double time;
	private final boolean useActualValues;
	
	public AgentAssignment(String agent, ActionTimeGenerator timeGenerator, boolean useActualValues) {
		this.agent = agent;
		this.subtasks = new ArrayList<Subtask>();
		this.time = 0;
		this.times = new ArrayList<Double>();
		this.completionTimes = new ArrayList<Double>();
		this.timeGenerator = timeGenerator;
		this.useActualValues = useActualValues;
	}
	
	public AgentAssignment(AgentAssignment other) {
		this.agent = other.agent;
		this.subtasks = new ArrayList<Subtask>(other.subtasks);
		this.time = other.time;
		this.times = new ArrayList<Double>(other.times);
		this.completionTimes = new ArrayList<Double>(other.completionTimes);
		this.timeGenerator = other.timeGenerator;
		this.useActualValues = other.getUseActualValues();
	}
	
	public AgentAssignment(List<AssignedSubtask> assignedSubtasks, String agent, ActionTimeGenerator timeGenerator, boolean useActualValues) {
		this.agent = agent;
		this.timeGenerator = timeGenerator;
		this.useActualValues = useActualValues;
		
		this.subtasks = new ArrayList<Subtask>(assignedSubtasks.size());
		this.times = new ArrayList<Double>(assignedSubtasks.size());
		this.completionTimes = new ArrayList<Double>(assignedSubtasks.size());
		
		this.time = 0;
		for (AssignedSubtask subtask : assignedSubtasks) {
			this.subtasks.add(subtask.getSubtask());
			double time = subtask.getTime();
			this.times.add(time);
			this.time += time;
			this.completionTimes.add(this.time);
		}
	}
	
	public AgentAssignment(String agent, List<Subtask> assignedActions,  ActionTimeGenerator timeGenerator, boolean useActualValues ) {
		this.agent = agent;
		this.subtasks = Collections.unmodifiableList(assignedActions);
		this.timeGenerator = timeGenerator;
		List<Double> times = new ArrayList<Double>(this.subtasks.size());
		for (Subtask subtask : this.subtasks) {
			GroundedAction ga = subtask.getAction();
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

	public AgentAssignment copy() {
		return new AgentAssignment(this);
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		Iterator<AssignedSubtask> it = this.iterator();
		while (it.hasNext()) {
			buffer.append(it.next().toString());
			if (it.hasNext()) {
				buffer.append(", ");
			}
		}
		return buffer.toString();
	}
	
	
	
	@Override 
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		
		if (!(other instanceof AgentAssignment)) {
			return false;
		}
		
		AgentAssignment workflow = (AgentAssignment)other;
		
		if (this.subtasks.size() != workflow.subtasks.size()) {
			return false;
		}
		
		Iterator<AssignedSubtask> thisIt = this.iterator();
		Iterator<AssignedSubtask> otherIt = workflow.iterator();
		while (thisIt.hasNext() && otherIt.hasNext()) {
			if (!thisIt.next().equals(otherIt.next())) {
				return false;
			}
		}
		return true;
	}
	
	@Override 
	public int hashCode() {
		return Objects.hash(this.subtasks, this.times);
	}
	
	public Iterator<AssignedSubtask> iterator() {
		return new AssignmentIterator(this.subtasks, this.times);
	}
	
	public void shuffle() {
		List<AssignedSubtask> subtasks = new ArrayList<AssignedSubtask>();
		for (AssignedSubtask subtask : this) {
			subtasks.add(subtask);
		}
		Collections.shuffle(subtasks);
		
		this.subtasks.clear();
		this.times.clear();
		this.completionTimes.clear();
		
		
		double sum = 0.0;
		for (AssignedSubtask subtask : subtasks) {
			this.subtasks.add(subtask.getSubtask());
			double time = subtask.getTime();
			this.times.add(time);
			sum += time;
			this.completionTimes.add(sum);
		}
		this.time = sum;
	}
	
	public Subtask first() {
		if (this.size() == 0) {
			return null;
		}
		return this.subtasks.get(0);
	}
	
	public boolean add(Subtask node) {
		GroundedAction ga = node.getAction(this.agent);
		double time = this.timeGenerator.get(ga, this.getUseActualValues());
		this.subtasks.add(node);
		this.times.add(time);
		this.time += time;
		this.completionTimes.add(this.time);
		return true;
	}
	
	public boolean add(int position, Subtask node) {
		GroundedAction ga = node.getAction(this.agent);
		double time = this.timeGenerator.get(ga, this.getUseActualValues());
		
		this.subtasks.add(position, node);
		this.times.add(position, time);
		this.completionTimes.add(this.time);
		double sum = 0.0;
		for (int i = position ; i < this.completionTimes.size(); i++) {
			sum += this.times.get(i);
			this.completionTimes.set(i, sum);
		}
		return true;
		
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
			Subtask nodeTmp = this.subtasks.get(first);
			double timeTmp = this.times.get(first);
			
			this.subtasks.set(first, this.subtasks.get(second));
			this.times.set(first, this.times.get(second));
			this.subtasks.set(second, nodeTmp);
			this.times.set(second, timeTmp);
		}
		this.rebuildCompletionTimes(first, second);
	}
	
	public void trim() {
		for (int i = this.subtasks.size() - 1; i >= 0; i--) {
			if (this.subtasks.get(i) == null) {
				this.subtasks.remove(i);
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
	
	public boolean contains(Subtask node) {
		return this.subtasks.contains(node);
	}
	
	// Not the ideal sorted workflow, as it allows actions with dependencies on others go first
	public AgentAssignment sort(Workflow workflow) {
		Set<Subtask> nodes = new HashSet<Subtask>();
		for (Subtask node : workflow) nodes.add(node);
		nodes.removeAll(this.subtasks);
		
		List<AssignedSubtask> sorted = new ArrayList<AssignedSubtask>(this.size());
		boolean keepGoing = true;
		while (keepGoing) {
			keepGoing = false;
			for (AssignedSubtask subtask : this) {
				Subtask node = subtask.getSubtask();
				if (node.isAvailable(nodes) && !nodes.contains(node)) {
					sorted.add(subtask);
					nodes.add(node);
					keepGoing = true;
				}
			}
		}
		if (sorted.size() != this.size()) {
			throw new RuntimeException("Sorting failed");
		}
		
		return new AgentAssignment(sorted, this.agent, this.timeGenerator, this.useActualValues);
	}
	
	public AgentAssignment condense() {
		List<AssignedSubtask> sorted = new ArrayList<AssignedSubtask>(this.size());
		for (AssignedSubtask subtask : this) {
			if (subtask.getSubtask() != null) {
				sorted.add(subtask);
			}
		}
		return new AgentAssignment(sorted, this.agent, this.timeGenerator, this.useActualValues);
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
	
	public List<Subtask> availableSubtasks(Set<Subtask> visited) { 
		List<Subtask> subtasks = new ArrayList<Subtask>();
		for (Subtask subtask : this.subtasks){ 
			if (subtask != null && subtask.isAvailable(visited)) {
				subtasks.add(subtask);
			}
		}
		
		return subtasks;
	}
	
	public List<Double> times() {
		return this.times;
	}
	
	public List<Double> completionTimes() {
		return this.completionTimes;
	}
	
	public List<Subtask> nodes() { 
		return this.subtasks;
	}
	
	
	
	public List<Subtask> completedSubtasks(double time) { 
		return this.completedSubtasks(0.0, time);
	}
	
	// Includes nodes that are overlap with beginTime, endTime and occur inbetween
	public List<Subtask> slice(double beginTime, double endTime) {
		if (beginTime >= this.time || beginTime > endTime) {
			return new ArrayList<Subtask>();
		}
		
		Integer begin = this.position(beginTime);
		Integer end = this.position(endTime);
		if (begin == null) {
			return new ArrayList<Subtask>();
		} else if (beginTime == this.completionTimes.get(begin)) {
			begin++;
		}
		
		if (end == null) {
			end = this.subtasks.size();
		} else if (endTime < this.completionTimes.get(end)) {
			end++;
		}
		
		return this.subtasks.subList(begin, end);
	}
	
	// Only includes nodes completely between beginTime and endTime
	public List<Subtask> completedSubtasks(double beginTime, double endTime) { 
		if (beginTime >= this.time || beginTime >= endTime) {
			return new ArrayList<Subtask>();
		}
		
		Integer begin = this.position(beginTime);
		Integer end = this.position(endTime);
		if (begin == null) {
			return null;
		} else if (beginTime == this.completionTimes.get(begin)) {
			begin++;
		}
		
		if (end == null) {
			end = this.subtasks.size();
		} else if (endTime == this.completionTimes.get(end)) {
			end++;
		}
		
		return this.subtasks.subList(begin, end);
	}
	
	public boolean waitUntil(double endTime) {
		if (this.time < endTime) {
			double waitTime = endTime - this.time;
			
			this.subtasks.add(null);
			this.times.add(waitTime);
			this.time += waitTime;
			this.completionTimes.add(this.time);
			return true;
		}
		return false;
	}
	
	public int size() {
		return this.subtasks.size();
	}
	
	public int realSize() {
		int size = 0;
		for (Subtask node : this.subtasks) {
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
	
	public Double getEndTime(Subtask subtask) {
		int index = this.subtasks.indexOf(subtask);
		if (index < 0) {
			return null;
		}
		
		return this.completionTimes.get(index);
		
	}

	public static class AssignmentIterator implements ListIterator<AssignedSubtask> {

		private final List<Subtask> subtasks;
		private final List<Double> times;
		private int currentIndex;
		private AssignmentIterator(List<Subtask> subtasks, List<Double> times) {
			this.subtasks = subtasks;
			this.times = times;
			this.currentIndex = 0;
		}
		
		@Override
		public boolean hasNext() {
			return this.currentIndex < this.subtasks.size();
		}

		@Override
		public AssignedSubtask next() {
			if (!this.hasNext()) {
				return null;
			}
			Subtask node = this.subtasks.get(this.currentIndex);
			Double time = this.times.get(this.currentIndex);
			this.currentIndex++;
			return new AssignedSubtask(node, time);
		}
		
		@Override
		public int nextIndex() {
			return this.currentIndex;
		}

		
		public boolean isNextAvailable(Set<Subtask> visitedNodes) {
			if (!this.hasNext()) {
				return false;
			}
			Subtask node = this.subtasks.get(this.currentIndex);
			return node.isAvailable(visitedNodes);
		}

		@Override
		public boolean hasPrevious() {
			return this.currentIndex > 0;
		}

		@Override
		public AssignedSubtask previous() {
			if (!this.hasPrevious()) {
				return null;
			}
			this.currentIndex--;
			Subtask node = this.subtasks.get(this.currentIndex);
			Double time = this.times.get(this.currentIndex);
			return new AssignedSubtask(node, time);
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
		public void set(AssignedSubtask e) {
			throw new UnsupportedOperationException();
			
		}

		@Override
		public void add(AssignedSubtask e) {
			throw new UnsupportedOperationException();			
		}	
	}
	
	

	
}