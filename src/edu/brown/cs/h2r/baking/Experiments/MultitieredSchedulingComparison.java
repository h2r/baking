package edu.brown.cs.h2r.baking.Experiments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;
import edu.brown.cs.h2r.baking.Scheduling.Multitiered.Assignments;
import edu.brown.cs.h2r.baking.Scheduling.Multitiered.ECTScheduler;
import edu.brown.cs.h2r.baking.Scheduling.Multitiered.HeuristicSearchScheduler;
import edu.brown.cs.h2r.baking.Scheduling.Multitiered.MILPScheduler;
import edu.brown.cs.h2r.baking.Scheduling.Multitiered.OrderPreservingSequencer;
import edu.brown.cs.h2r.baking.Scheduling.Multitiered.Scheduler;
import edu.brown.cs.h2r.baking.Scheduling.Multitiered.Subtask;
import edu.brown.cs.h2r.baking.Scheduling.Multitiered.Task;
import edu.brown.cs.h2r.baking.Scheduling.Multitiered.TercioScheduler;
import edu.brown.cs.h2r.baking.Scheduling.Multitiered.TercioSequencer;
import edu.brown.cs.h2r.baking.Scheduling.Multitiered.Workflow;

public class MultitieredSchedulingComparison {

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
					//workflow.connect(subtask, child, waitDuration, Double.MAX_VALUE);
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
						minValue = 5.0 * task.getMinRequiredTimeBetweenSubtasks(subtask, child, timeGenerator, agents);
						
					}
					if (minValue < 0.0) {
						System.err.println("Something is still wrong");
					}
					double subTaskDeadline = 1.0;
					while (subTaskDeadline < minValue) {
						subTaskDeadline = rando.nextGaussian() * minValue + minValue;
					}
					//workflow.connect(subtask, child, 0.0, subTaskDeadline);
				}
			}
		}
		return workflow;
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
		List<Workflow> workflowsToTest = new ArrayList<Workflow>();
		List<Double> milpTimes = new ArrayList<Double>();
		List<Assignments> allAssignments = new ArrayList<Assignments>();
		List<ActionTimeGenerator> timeGenerators = new ArrayList<ActionTimeGenerator>();
		
		Workflow.listFromYAMLFile("/Users/brawner/workspace/baking/results/workflows.test", null, workflowsToTest, timeGenerators);
		List<String> agents = Arrays.asList("human", "friend", "friend1", "friend2");
		
		TercioSequencer tSequencer = new TercioSequencer(false);
		OrderPreservingSequencer opSequencer = new OrderPreservingSequencer(false);
		//ECTScheduler ectScheduler = new ECTScheduler(tSequencer, false);
		ECTScheduler ectScheduler2 = new ECTScheduler(opSequencer, false);
		
		List<Scheduler> schedulers = Arrays.asList(
				ectScheduler2,
				//(Scheduler)new HeuristicSearchScheduler(ectScheduler, tSequencer, false),
				(Scheduler)new HeuristicSearchScheduler(ectScheduler2, opSequencer, false),
				new TercioScheduler(false)
				);
				
		
		//problematicWorkflows.clear();
		//problematicTimeMaps.clear();
		for (int j = 5; j < 20; j++) {
			for (int i = 0; i < 10; i++) {
				Map<String, Double> results = new HashMap<String, Double>();
				Map<String, Long> resultTimes = new HashMap<String, Long>();
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
				Workflow workflow = MultitieredSchedulingComparison.buildSortedWorkflow(numTasks, numSubtasks, numEdges, numResources, numResourcesPerNode, timeGenerator, agents);
				Assignments assignments = new Assignments(agents, timeGenerator, false);
				double time = 0.0;
				
				long end = 0, start = 0;
				while (time < 0.0) {
					resultTimes.clear();
					results.clear();
					
					workflow = MultitieredSchedulingComparison.buildSortedWorkflow(numTasks, numSubtasks, numEdges, numResources, numResourcesPerNode, timeGenerator, agents);
					 
					 start = System.nanoTime();
					 time = milp.schedule(workflow, assignments, timeGenerator, null);
					 end = System.nanoTime();
				}
				//System.out.println(workflow.toString());
				resultTimes.put("milp", end - start);
				results.put("milp", time);
				
				for (Scheduler scheduler : schedulers) {
					start = System.nanoTime();
					Assignments schedule = scheduler.schedule(workflow, agents, timeGenerator);
					end = System.nanoTime();
					if (schedule != null) {
						if (schedule.time() < time) {
							//System.err.println("This one is invalid");
							//MILPScheduler.checkAssignments(workflow, schedule);
						}
						results.put(scheduler.getDescription(), schedule.time());
						resultTimes.put(scheduler.getDescription(), end - start);
					}
				}
				
				Iterator<Map.Entry<String, Double>> it = results.entrySet().iterator();
				System.out.print(numTasks + ", ");
				while (it.hasNext()){
					Map.Entry<String, Double> entry = it.next();
					long t = resultTimes.get(entry.getKey());
					time = (double)t / 1000000000.0;
					System.out.print(entry.getKey() + ", " + entry.getValue() + ", " + time);
					if (it.hasNext()) {
						System.out.print(", ");
					}
				}
				System.out.print("\n");
				
				
			}
		}
		
	}

	
}
