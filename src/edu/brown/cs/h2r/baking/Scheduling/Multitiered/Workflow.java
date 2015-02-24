package edu.brown.cs.h2r.baking.Scheduling.Multitiered;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.yaml.snakeyaml.Yaml;

import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;
import edu.brown.cs.h2r.baking.Scheduling.Workflow.Node;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.Attribute.AttributeType;
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
	
	public static Workflow fromYAML(String string) {
		Yaml yaml = new Yaml();
		List<?> objects = (List<?>)yaml.load(string);
		return Workflow.fromObject(objects);		
	}
	
	
	public static void listFromYAML(String string, List<Workflow> workflows, List<ActionTimeGenerator> timeGenerators) {
		
		Yaml yaml = new Yaml();
		List<?> objects = (List<?>)yaml.load(string);
		parseYAML(objects, workflows, timeGenerators);
	}
	
	public static void listFromYAMLFile(String filename, List<Workflow> workflows, List<ActionTimeGenerator> timeGenerators) {
		Yaml yaml = new Yaml();
		try {
			FileReader fr = new FileReader(filename);
			List<?> objects = (List<?>)yaml.load(fr);
			fr.close();
			if (objects == null) {
				return;
			}
			parseYAML(objects, workflows, timeGenerators);
			
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
	}

	private static void parseYAML(List<?> objects, List<Workflow> workflows,
			List<ActionTimeGenerator> timeGenerators) {
		for (Object o : objects) {
			Map<String, Object> map = (Map<String, Object>)o;
			
			Workflow workflow = Workflow.fromObject((List<?>)map.get("workflow"));
			Map<List<String>, Double> times = (Map<List<String>, Double>)map.get("times");
			Map<GroundedAction, Double> timeMap = new HashMap<GroundedAction, Double>();
			for (Map.Entry<List<String>, Double> entry : times.entrySet()) {
				List<String> list = entry.getKey();
				String[] params = list.toArray(new String[list.size()]);
				timeMap.put(new GroundedAction(null, params), entry.getValue());
			}
			ActionTimeGenerator timeGenerator = new ActionTimeGenerator(timeMap, true);
			workflows.add(workflow);
			timeGenerators.add(timeGenerator);
		}
	}
	
	public static Workflow fromObject(List<?> objects) {
		List<Subtask> subtasks = new ArrayList<Subtask>();
		Map<String, Subtask> subtaskMap = new HashMap<String, Subtask>();
		Map<Subtask, List<?>> constraintMap = new HashMap<Subtask, List<?>>();
		Map<Integer, Task> taskMap = new HashMap<Integer, Task>();
		
		for(Object o : objects){
			Map<?,?> oMap = (Map<?, ?>)o;
			
			String fullId = (String)oMap.get("id");
			int id = Integer.parseInt(fullId.split(" - ")[1]);
			int taskId = Integer.parseInt((String)oMap.get("task"));
			List<?> params = (List<?>)oMap.get("params");
			List<?> constraintsStr = (List<?>)oMap.get("constraints");
			
			GroundedAction action = new GroundedAction(null, params.toArray(new String[params.size()]));
			Task task = taskMap.get(taskId);
			if (task == null) {
				task = new Task(taskId, 0.0, 0.0);
				taskMap.put(taskId, task);
			}
			Subtask subtask = task.addSubtask(id, action);
			constraintMap.put(subtask, constraintsStr);
			subtaskMap.put(fullId, subtask);
			subtasks.add(subtask);
		}
		
		for (Map.Entry<Subtask, List<?>> entry : constraintMap.entrySet()) {
			Subtask from = entry.getKey();
			List<?> constraints = entry.getValue();
			for (Object o : constraints) {
				Map<String, Object> cMap = (Map<String, Object>)o;
				String constraintFullId = (String)cMap.get("id");
				Double deadline = (Double)cMap.get("deadline");
				double wait = (Double)cMap.get("wait");
				Subtask cSubtask = subtaskMap.get(constraintFullId);
				from.addConstraint(cSubtask, wait, deadline);
			}
			
		}
		
		return new Workflow(subtasks);
	}
	
	public static String toYAML(List<Workflow> workflows, List<ActionTimeGenerator> timeMaps) {
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		for (int i = 0; i < workflows.size(); i++) {
			Map<String, Object> map = new HashMap<String, Object>();
			map.put("workflow", workflows.get(i).toObjectList());
			Map<GroundedAction, Double> timemap = timeMaps.get(i).getMap();
			Map<String[], Double> converted = new HashMap<String[], Double>();
			for (Map.Entry<GroundedAction, Double> entry : timemap.entrySet()) {
				converted.put(entry.getKey().params, entry.getValue());
			}
			map.put("times", converted);
			list.add(map);
		}
		Yaml yaml = new Yaml();
		String str = yaml.dump(list);
		return str;
	}
	
	public String toYaml() {
		Yaml yaml = new Yaml();
		String output = yaml.dump(this.toObjectList());
		return output;
	}
	
	public List<Map<String, Object>> toObjectList() {
		List<Map<String, Object>> objects = new ArrayList<Map<String, Object>>();
		for (Subtask subtask : this.actions) {
			Map<String, Object> map = new HashMap<String, Object>();
			
			map.put("id", subtask.getId());
			map.put("task", subtask.getTask().getId());
			map.put("params", subtask.getAction().params);
			List<Map<String, Object>> constraintStrs = new ArrayList<Map<String, Object>>(); 
			for (TemporalConstraint constraint : subtask.getConstraints()) {
				Map<String, Object> cMap = new HashMap<String, Object>();
				cMap.put("id", constraint.subtask.getId());
				cMap.put("wait", constraint.lowerBound);
				cMap.put("deadline", constraint.upperBound);
				constraintStrs.add(cMap);
			}
			map.put("constraints", constraintStrs);
			
			objects.add(map);
		}
		return objects;
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
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof Workflow)) {
			return false;
		}
		
		Workflow wother = (Workflow)other;
		if (this.actions.size() != wother.actions.size()) {
			return false;
		}
		
		Map<String, Subtask> otherMap = new HashMap<String, Subtask>();
		for (Subtask subtask : wother.actions) {
			otherMap.put(subtask.getId(), subtask);
		}
		for (Subtask subtask : this.actions) {
			Subtask otherSubtask = otherMap.get(subtask.getId());
			if (otherSubtask == null) {
				return false;
			}
			if (!subtask.equals(otherSubtask)) {
				subtask.equals(otherSubtask);
				return false;
				
			}
		}
		
		return true;
	}
	
	@Override
	public int hashCode() {
		return this.actions.hashCode();
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
	
	public List<Subtask> getAvailableSubtasks(Set<Subtask> completed) {
		List<Subtask> available = new ArrayList<Subtask>();
		for (Subtask subtask : this.actions) {
			if (subtask.isAvailable(completed) && !completed.contains(subtask)) {
				available.add(subtask);
			}
		}
		return available;
	}
	
	public List<Subtask> getSubtasks() {
		return new ArrayList<Subtask>(this.actions);
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

	public int size() {
		return this.actions.size();
	}
	
	
	
	
	
}
