package edu.brown.cs.h2r.baking.Experiments;

import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;
import edu.brown.cs.h2r.baking.Scheduling.Multitiered.AgentAssignment;
import edu.brown.cs.h2r.baking.Scheduling.Multitiered.Assignments;
import edu.brown.cs.h2r.baking.Scheduling.Multitiered.MILPScheduler;
import edu.brown.cs.h2r.baking.Scheduling.Multitiered.Subtask;
import edu.brown.cs.h2r.baking.Scheduling.Multitiered.Task;
import edu.brown.cs.h2r.baking.Scheduling.Multitiered.TercioSequencer;
import edu.brown.cs.h2r.baking.Scheduling.Multitiered.Workflow;

public class SequencerComparison {

	public static Workflow buildSortedWorkflow(int numberTasks, int numberSubtasks, int numberEdges, int numberResources, int resourcesPerNode, ActionTimeGenerator timeGenerator, List<String> agents) {
		Random rando = new Random();
		List<Subtask> subtasks = new ArrayList<Subtask>();
		
		List<Task> tasks = new ArrayList<Task>();
		
		int index = 0;
		for (int i = 0; i < numberTasks; i++) {
			double deadline = 0;
			double suspension = 0;
			List<Subtask> tasksSubtasks = new ArrayList<Subtask>();
			Task task = new Task(i, deadline, suspension);
			int numSubtasks = rando.nextInt(numberSubtasks) + numberSubtasks / 4;
			for (int j = 0; j < numSubtasks; j++) {
				Set<String> resources = new HashSet<String>();
				while (resources.size() < resourcesPerNode) {
					resources.add(Integer.toString(rando.nextInt(numberResources)));
				}
				
				List<String> resList = new ArrayList<String>(resources);
				resList.add(0, "agent");
				resList.add(1, "" + i + " - " + j);
				GroundedAction action = new GroundedAction(null, resList.toArray(new String[resList.size()]));
				double maxDuration = 0;
				for (String agent : agents) {
					GroundedAction ga = (GroundedAction)action.copy();
					ga.params[0] = agent;
					double duration = timeGenerator.get(ga, false);
					maxDuration = Math.max(maxDuration, duration);
				}
				tasksSubtasks.add(task.addSubtask(index++, action));
			}
			tasks.add(task);
			subtasks.addAll(tasksSubtasks);
		}
		
		Workflow workflow = new Workflow(subtasks);
		
		for (Task task : tasks) {
			for (Subtask subtask : task.getSubtasks()) {
				List<Subtask> validSubtasks = new ArrayList<Subtask>(task.getSubtasks());
				validSubtasks.remove(subtask);
				for (Subtask child : task.getSubtasks()) {
					if (child.isAncestorOf(subtask)) {
						validSubtasks.remove(child);
					}
				}
				
				boolean wait = rando.nextDouble() <= 0.25;
				if (wait && !validSubtasks.isEmpty()) {
					Subtask child = validSubtasks.get(rando.nextInt(validSubtasks.size()));
					double waitDuration = rando.nextDouble() * 9 + 4.5;
					workflow.connect(subtask, child, waitDuration, Double.MAX_VALUE);
				}
			}
		}
			
		for (Task task : tasks) {
			for (Subtask subtask : task.getSubtasks()) {
				if (subtask.getConstraints().size() > 0) {
					continue;
				}
				List<Subtask> validSubtasks = new ArrayList<Subtask>(task.getSubtasks());
				validSubtasks.remove(subtask);
				for (Subtask child : task.getSubtasks()) {
					if (child.isAncestorOf(subtask)) {
						validSubtasks.remove(child);
					}
				}
				
				boolean deadline = rando.nextDouble() <= 0.25;
				if (deadline && !validSubtasks.isEmpty()) {
					Subtask child = validSubtasks.get(rando.nextInt(validSubtasks.size()));
					double minValue = task.getMinRequiredTimeBetweenSubtasks(subtask, child, timeGenerator, agents);
					if (minValue < 0.0) {
						Subtask tmp = child;
						child = subtask;
						subtask = tmp;
						minValue = task.getMinRequiredTimeBetweenSubtasks(subtask, child, timeGenerator, agents);
						
					}
					if (minValue < 0.0) {
						System.err.println("Something is still wrong");
					}
					double subTaskDeadline = 1.0;
					while (subTaskDeadline < minValue) {
						subTaskDeadline = rando.nextGaussian() * minValue + minValue;
					}
					workflow.connect(subtask, child, 0.0, subTaskDeadline);
				}
			}
		}
		return workflow.sort();
	}
	
	public static void main(String argv[]){
		MILPScheduler milp = new MILPScheduler(false);
		
		int trialId = new Random().nextInt();
		trialId = 2;
		if (argv.length == 2) {
			trialId = Integer.parseInt(argv[0]);
		}
		int numTries = 100;
		/*for (Map.Entry<String, Map<Workflow.Node, Double>> entry : actionTimeLookup.entrySet()) {
			System.out.println("Workflow time for " + entry.getKey() + ": " + SchedulingComparison.getAgentsSoloTime(workflow, entry.getValue()));
		}*/
		Map<String, Double> factors = new HashMap<String, Double>();
		
		System.out.println("MILP, Rearrange, Tercio, Search");
		List<Integer> connectedness = Arrays.asList(20);
		//HeuristicSearchSequencer sequencer = new HeuristicSearchSequencer(true);
		Random random = new Random();
		boolean shuffleOrder = true;
		//Collections.shuffle(connectedness);
		List<Workflow> problematicWorkflows = new ArrayList<Workflow>();
		Set<Workflow> uniqueProblematic = new HashSet<Workflow>();
		List<ActionTimeGenerator> problematicTimeMaps = new ArrayList<ActionTimeGenerator>();
		List<Workflow> workflowsToTest = new ArrayList<Workflow>(problematicWorkflows);
		List<ActionTimeGenerator> timeGenerators = new ArrayList<ActionTimeGenerator>(problematicTimeMaps);
		
		Workflow.listFromYAMLFile("/Users/brawner/workspace/baking/results/workflows.test", null, workflowsToTest, timeGenerators);
		List<String> agents = Arrays.asList("human", "friend", "friend1", "friend2");
		
		
		//problematicWorkflows.clear();
		//problematicTimeMaps.clear();
		for (int j = 4; j < 10; j++) {
			for (int i = 0; i < 5; i++) {
				ActionTimeGenerator timeGenerator = new ActionTimeGenerator(factors);
				
				int numTasks = j;
				int numSubtasks = j;
				int numEdges =  j/2;
				int numResources = j*j;
				int numResourcesPerNode = 1;
				if (trialId % 3 == 0) {
					numEdges = random.nextInt(2*j-4);
					numResources = random.nextInt(j);
					numResourcesPerNode = random.nextInt(Math.max(1, numResources/2));
				} else if (trialId % 3 == 1) {
					numEdges = 2 * j-4;
					numResources = j;
					numResourcesPerNode = 1;
				}
				Workflow workflow = null;
				Assignments assignments = new Assignments(agents, timeGenerator, false);
				double time = -1.0;
				
				while (time < 0.0) {
					 workflow = SequencerComparison.buildSortedWorkflow(numTasks, numSubtasks, numEdges, numResources, numResourcesPerNode, timeGenerator, agents);
					 //System.out.println(workflow.toString());
					 time = milp.schedule(workflow, assignments, timeGenerator, null);
					 //System.out.println(assignments.toString());
				}
				workflowsToTest.add(workflow);
				timeGenerators.add(timeGenerator);
				
				
				
			}
		}
		
		for (int i = 0; i < workflowsToTest.size(); i++) {
			Workflow workflow = workflowsToTest.get(i);
			
			ActionTimeGenerator timeGenerator = timeGenerators.get(i);
			Assignments assignments = new Assignments(agents, timeGenerator, false);
			double time = -1.0;
			int tries = 0;
			while (time < 0.0 && tries++ < 3) {
				time = milp.schedule(workflow, assignments, timeGenerator, null);
			}
			if (shuffleOrder) {
				for (AgentAssignment assignment : assignments.getAssignments()) {
					assignment.shuffle();
				}
			}
			
			boolean didPrint = false;
			//BufferedAssignments buffered =  new BufferedAssignments(timeGenerator, agents, false, false);
			//buffered.sequenceTasksWithReorder(assignments);
			//MILPScheduler.checkAssignments(workflow, assignments, buffered);
			//double bTime = buffered.time();
			
			//if (bTime - 0.0001 > time) {
				//System.out.println(buffered.visualString());
				//System.out.println(buffered.getFullString());
				//didPrint = true;
			//}
			List<String> lines = new ArrayList<String>();
			for (Subtask subtask : workflow) {
				lines.add(subtask.toString() + " - " + subtask.getResources().toString());
			}
			Collections.sort(lines);
			//for (String s : lines) System.out.println(s);
			TercioSequencer tSequencer = new TercioSequencer(false);
			Assignments tercio = tSequencer.sequence(assignments, timeGenerator, workflow);
			
			tries = 0;
			while (tercio == null && tries++ < 3) {
				tercio = tSequencer.sequence(assignments, timeGenerator, workflow);
			}
			
			if (tercio == null) {
				if (uniqueProblematic.add(workflow)) {
					problematicWorkflows.add(workflow);
					problematicTimeMaps.add(timeGenerator);
				}
				continue;
			}
			//System.out.println(tercio.toString());
			//MILPScheduler.checkAssignments(workflow, assignments, tercio);
			double tTime = tercio.time();
			if (tTime - 0.0001 > time) {
				//System.out.println(tercio.visualString());
				//System.out.println(tercio.getFullString());
				didPrint = true;
			}
			
			//BufferedAssignments search = sequencer.sequenceAssignments(assignments);
			//MILPScheduler.checkAssignments(workflow, assignments, search);
			
			//double sTime = search.time();
			//if (sTime -0.0001 > time) {
				//System.out.println(search.visualString());
				//System.out.println(search.getFullString());
				//didPrint = true;
			//}
			
			
			System.out.println(workflow.getTasks().size() + ", " + time + ", " + tTime);
		}
		FileWriter fw;
		try {
			fw = new FileWriter("/Users/brawner/workspace/baking/results/workflows.test");
			System.out.println("There were " + problematicWorkflows.size() + " workflows that have been written");
			String sL = Workflow.toYAML(problematicWorkflows, problematicTimeMaps);
			fw.write(sL);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}

	
}
