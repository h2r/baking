package edu.brown.cs.h2r.baking.Scheduling.Multitiered;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.brown.cs.h2r.baking.Scheduling.Workflow.Node;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;

public class Workflow implements Iterable<Subtask> {

	private final List<Task> tasks;
	private final List<Subtask> actions;
	private final State startState;
	public Workflow(State startState) {
		this.tasks = new ArrayList<Task>();
		this.actions = new ArrayList<Subtask>();
		this.startState = startState;
	}
	
	public Workflow(List<Subtask> actions) {
		this.actions = new ArrayList<Subtask>(actions);
		this.tasks = this.buildTasks();
		this.startState = null;
	}
	
	public Workflow(State startState, List<Subtask> actions) {
		this.actions = new ArrayList<Subtask>(actions);
		this.tasks = this.buildTasks();
		this.startState = startState;
	}
	
	@Override
	public String toString() {
		LinkedHashSet<Task> tasks = new LinkedHashSet<Task>();
		for (Subtask subtask : this.actions) {
			tasks.add(subtask.getTask());
		}
		StringBuffer buffer = new StringBuffer();
		for (Task task : tasks) {
			String subtasksStr = task.getSubtasks().toString();
			buffer.append(task.toString()).append(subtasksStr).append("\n");
		}
		return buffer.toString();
	}
	
	private List<Task> buildTasks() {
		LinkedHashSet<Task> tasks = new LinkedHashSet<Task>();
		for (Subtask subtask : this.actions){ 
			tasks.add(subtask.getTask());
		}
		return new ArrayList<Task>(tasks);
	}
	
	public List<Task> getTasks() {
		return new ArrayList<Task>(this.tasks);
	}
	
	public boolean add(Task task){
		return this.actions.addAll(task.getSubtasks());
	}
	
	public boolean connect(Subtask from, Subtask to, double wait, double deadline) {
		if (from.equals(to)) {
			return false;
		}
		return from.addConstraint(to, wait, deadline);
	}
	
	public Workflow sort() {
		Set<Subtask> sortedTasks = new LinkedHashSet<Subtask>();
		List<Subtask> subtasks = new ArrayList<Subtask>(this.actions);
		
		Collections.shuffle(subtasks);
		while (!subtasks.isEmpty()) {
			boolean addedToSorted = false;
			for (Subtask subtask : subtasks) {
				if (subtask.isAvailable(sortedTasks)) {
					addedToSorted |= sortedTasks.add(subtask);
				}
			}
			if (!addedToSorted) {
				System.err.println("Failed to sort workflow. The failed subtasks were: ");
				System.err.println(subtasks.toString());
			}
			subtasks.removeAll(sortedTasks);
		}
		
		return new Workflow(new ArrayList<Subtask>(sortedTasks));
	}

	@Override
	public Iterator<Subtask> iterator() {
		return this.actions.iterator();
	}
	
	
	
	
	
}
