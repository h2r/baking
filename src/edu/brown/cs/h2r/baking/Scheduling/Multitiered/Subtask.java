package edu.brown.cs.h2r.baking.Scheduling.Multitiered;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;
import burlap.oomdp.singleagent.GroundedAction;

public class Subtask {
	private final Task task;
	private final Map<Subtask, TemporalConstraint> constraints;
	private final Set<Subtask> children;
	private final Integer id;
	private final GroundedAction action;
	private final Set<String> resources;
	private double duration;
	private double deadline;
	
	public Subtask(Task task, int id, GroundedAction action) {
		this.task = task;
		this.id = id;
		this.action = action;
		Set<String> resources = new HashSet<String>(Arrays.asList(Arrays.copyOfRange(action.params, 2, action.params.length)));
		this.resources = Collections.unmodifiableSet(resources);
		this.constraints = new HashMap<Subtask, TemporalConstraint>();
		this.children = new HashSet<Subtask>();
		this.deadline = Double.MAX_VALUE;
	}
	
	public Subtask(Task task, int id, Set<String> resources) {
		this.task = task;
		this.id = id;
		this.action = new GroundedAction(null, new String[]{"agent", Integer.toString(this.id)});
		this.resources = Collections.unmodifiableSet(resources);
		this.constraints = new HashMap<Subtask, TemporalConstraint>();
		this.children = new HashSet<Subtask>();
		this.deadline = Double.MAX_VALUE;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.getTask().toString()).append(" - ").append(this.id);
		buffer.append("(" + this.resources.toString() + ")");
		buffer.append(this.constraints.values().toString());
		return buffer.toString();
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Subtask)) {
			return false;
		}
		
		Subtask sOther = (Subtask)other;
		if (this.id != sOther.id){ 
			return false;
		}
		
		/*if (!this.resources.equals(sOther.resources)) {
			return false;
		}*/
		
		/*if (!this.constraints.equals(sOther.constraints)) {
			return false;
		}*/
		
		return true;
	}
	
	@Override
	public int hashCode() {
		return this.id.hashCode();
	}
	
	public String getId() {
		return this.task.getId() + " - " + this.id;
	}
	
	public boolean setDeadline(double deadline) {
		if (deadline <= this.duration) {
			return false;
		}
		this.deadline = deadline;
		return true;
	}
	
	public boolean setDuration(double duration) {
		if (duration >= this.deadline) {
			return false;
		}
		this.duration = duration;
		return true;
	}
	
	public GroundedAction getAction() {
		return new GroundedAction(this.action.action, this.action.params);
	}
	
	public GroundedAction getAction(String agent) {
		String [] params = Arrays.copyOf(this.action.params, this.action.params.length);
		params[0] = agent;
		return new GroundedAction(this.action.action, params);
	}
	
	public Collection<TemporalConstraint> getConstraints() {
		return this.constraints.values();
	}
	
	public boolean addConstraint(Subtask to, double lowerBound, double upperBound) {
		if (lowerBound > upperBound) {
			return false;
		}
		
		TemporalConstraint constraint = this.constraints.get(to);
		if (constraint == null) {
			constraint = new TemporalConstraint(to, lowerBound, upperBound);
			if (!this.checkCycle(to)) {
				this.constraints.put(to, constraint);
			} else {
				return false;
			}
		}
		
		to.children.add(this);
		this.getTask().sort();
		return constraint.tightenConstraint(lowerBound, upperBound);
		
	}
	
	public boolean checkCycle(Subtask child) {
		for (Subtask subtask : child.constraints.keySet()) {
			if (this.equals(subtask)) {
				return true;
			}
			if (this.checkCycle(subtask)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isConstrainedTo(Subtask to) {
		return this.constraints.containsKey(to);
	}
	
	public Double getWait(Subtask to) {
		TemporalConstraint constraint = this.constraints.get(to);
		if (constraint == null) {
			return null;
		}
		
		return constraint.lowerBound;
	}
	
	public Double getDeadline(Subtask to) {
		TemporalConstraint constraint = this.constraints.get(to);
		if (constraint == null) {
			return null;
		}
		
		return constraint.upperBound;
	}
	
	public boolean isAncestorOf(Subtask secondNode) {
		if (this.equals(secondNode)) {
			return false;
		}
		for (Subtask node : this.children) {
			if (node.equals(secondNode) || node.isAncestorOf(secondNode)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean isAvailable(Set<Subtask> subtasks) {
		for (Subtask subtask : this.constraints.keySet()){
			if (!subtasks.contains(subtask)) {
				return false;
			}
		}
		return true;		
	}
	
	public boolean resourceConflicts(Subtask secondNode) {
		for (String resource : this.resources) {
			if (secondNode.resources.contains(resource)) {
				return true;
			}
		}
		return false;
	}
	
	public boolean resourceConflicts(Collection<Subtask> subtasks) {
		Set<String> resources = new HashSet<String>();
		for (Subtask task : subtasks) {
			if (task != null) {
				resources.addAll(task.resources);
			}
		}
		for (String resource : this.resources) {
			if (resources.contains(resource)) {
				return true;
			}
		}
		return false;
	}

	public Set<String> getResources() {
		return this.resources;
	}

	public double getMaxDuration(ActionTimeGenerator timeGenerator, Collection<String> agents) {
		double maxDuration = 0.0;
		for (String agent : agents) {
			GroundedAction action = this.getAction(agent);
			double time = timeGenerator.get(action, false);
			maxDuration = Math.max(maxDuration, time);
		}
		return maxDuration;
	}
	
	public double getMinDuration(ActionTimeGenerator timeGenerator, Collection<String> agents) {
		double maxDuration = Double.MAX_VALUE;
		for (String agent : agents) {
			GroundedAction action = this.getAction(agent);
			double time = timeGenerator.get(action, false);
			maxDuration = Math.min(maxDuration, time);
		}
		return maxDuration;
	}

	public Task getTask() {
		return task;
	}

	public List<Subtask> getChildren() {
		return new ArrayList<Subtask>(children);
	}
	
	public Set<Subtask> getChildDeadlines() {
		Set<Subtask> childDeadlines = new HashSet<Subtask>();
		for (Subtask child : this.children) {
			if (child.constraints.get(this).upperBound < Double.MAX_VALUE) {
				childDeadlines.add(child);
			}
		}
		return childDeadlines;
	}

	

	

	
}
