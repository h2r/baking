package edu.brown.cs.h2r.baking.Scheduling.Multitiered;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;


public class Task {
	private final int id;
	private final List<Subtask> subtasks;
	private final double deadline;
	private final double delay;
	
	public Task(int id, double deadline, double delay) {
		this.id = id;
		this.subtasks = new ArrayList<Subtask>();
		this.deadline = deadline;
		this.delay = delay;
	}
	
	@Override
	public String toString() {
		return Integer.toString(this.id);
	}
	
	public Subtask addSubtask(int id, GroundedAction action) {
		Subtask subtask = new Subtask(this, id, action);
		this.subtasks.add(subtask);
		this.sort();
		return subtask;
	}
	
	public Subtask addSubtask(int id, Set<String> resources) {
		Subtask subtask = new Subtask(this, id, resources);
		this.subtasks.add(subtask);
		this.sort();
		return subtask;
	}

	public List<Subtask> getSubtasks() {
		return new ArrayList<Subtask>(this.subtasks);
	}
	
	public void sort() {
		
		LinkedHashSet<Subtask> sorted = new LinkedHashSet<Subtask>();
		Set<Subtask> subtasks = new HashSet<Subtask>(this.subtasks);
		while (subtasks.size() != 0) {
			for (Subtask subtask : this.subtasks) {
				if (subtask.isAvailable(sorted)) {
					sorted.add(subtask);
				}
			}
			subtasks.removeAll(sorted);
		}
		this.subtasks.clear();
		this.subtasks.addAll(sorted);
	}
	
	public Double getMinRequiredTime(ActionTimeGenerator timeGenerator, List<String> agents) {
		Map<Subtask, Double> startTimes = new HashMap<Subtask, Double>(),
				endTimes = new HashMap<Subtask, Double>();
		this.generateStartEndTimes(timeGenerator, agents, startTimes, endTimes);
		return Collections.max(endTimes.values());
	}

	public Double getMinRequiredTimeForSubtask(Subtask subtask, ActionTimeGenerator timeGenerator, List<String> agents) {
		Map<Subtask, Double> startTimes = new HashMap<Subtask, Double>(),
				endTimes = new HashMap<Subtask, Double>();
		this.generateStartEndTimes(timeGenerator, agents, startTimes, endTimes);
		return endTimes.get(subtask);
	}
	
	public Double getMinRequiredTimeBetweenSubtasks(Subtask from, Subtask to, ActionTimeGenerator timeGenerator, List<String> agents) {
		Map<Subtask, Double> startTimes = new HashMap<Subtask, Double>(),
				endTimes = new HashMap<Subtask, Double>();
		this.generateStartEndTimes(timeGenerator, agents, startTimes, endTimes);
		return endTimes.get(to) - startTimes.get(from);
	}
	
	private void generateStartEndTimes(ActionTimeGenerator timeGenerator,
			List<String> agents, Map<Subtask, Double> startTimes,
			Map<Subtask, Double> endTimes) {
		for (Subtask subtask : this.subtasks){ 
			double startTime = 0.0;
			for (TemporalConstraint constraint : subtask.getConstraints()) {
				startTime = Math.max(startTime, constraint.lowerBound + endTimes.get(constraint.subtask));
			}
			startTimes.put(subtask, startTime);
			endTimes.put(subtask, startTime + subtask.getMaxDuration(timeGenerator, agents));
			
		}
	}

	public String getId() {
		return Integer.toString(this.id);
	}
	
	
}

