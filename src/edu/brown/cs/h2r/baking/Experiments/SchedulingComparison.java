package edu.brown.cs.h2r.baking.Experiments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;
import edu.brown.cs.h2r.baking.Scheduling.Assignment;
import edu.brown.cs.h2r.baking.Scheduling.Assignment.ActionTime;
import edu.brown.cs.h2r.baking.Scheduling.BufferedAssignments;
import edu.brown.cs.h2r.baking.Scheduling.ExhaustiveStarScheduler;
import edu.brown.cs.h2r.baking.Scheduling.GreedyScheduler;
import edu.brown.cs.h2r.baking.Scheduling.RandomScheduler;
import edu.brown.cs.h2r.baking.Scheduling.Scheduler;
import edu.brown.cs.h2r.baking.Scheduling.WeightByDifference;
import edu.brown.cs.h2r.baking.Scheduling.WeightByShortest;
import edu.brown.cs.h2r.baking.Scheduling.Workflow;
import edu.brown.cs.h2r.baking.Scheduling.Workflow.Node;

public class SchedulingComparison {

	public static Workflow buildSortedWorkflow(int numberNodes, int numberEdges) {
		Random rando = new Random();
		List<Workflow.Node> nodes = new ArrayList<Workflow.Node>();
		
		for (int i = 0; i < numberNodes; i++) {
			nodes.add(new Workflow.Node(i));
		}
		Workflow workflow = new Workflow(nodes);
		
		int edges = 0;
		while(edges < numberEdges) {
			int from = rando.nextInt(numberNodes);
			int to = rando.nextInt(numberNodes);
			edges += (workflow.connect(from, to)) ? 1 : 0;
		}
		return workflow.sort();
	}
	
	public static Map<String, Map<Workflow.Node, Double>> buildActionTimeLookup(Workflow workflow, int numAgents) {
		Map<String, Map<Workflow.Node, Double>> actionTimeLookup = new HashMap<String, Map<Workflow.Node, Double>>();
		
		Random random = new Random();
		
		for (int i = 0; i < numAgents; i++) {
			String id = Integer.toString(i);
			Map<Workflow.Node, Double> times = new HashMap<Workflow.Node, Double>();
			for (Workflow.Node node : workflow) {
				times.put(node, random.nextDouble());
			}
			actionTimeLookup.put(id, times);
		}
		
		return actionTimeLookup;
	}
	
	
	
	public static double getAgentsSoloTime(Workflow workflow, Map<Workflow.Node, Double> timeLookup) {
		double sum = 0.0;
		for (Node node : workflow) {
			sum += timeLookup.get(node);
		}
		return sum;
	}
	
	public static boolean verifyAssignments(Workflow workflow, List<Assignment> assignedWorkflows) {
		int size = 0;
		Set<Workflow.Node> visited = new HashSet<Workflow.Node>();  
		for (Assignment assignment : assignedWorkflows) {
			size += assignment.size();
			visited.clear();
			for (Assignment assignment2 : assignedWorkflows) {
				if (assignment != assignment2) {
					visited.addAll(assignment2.nodes());
				}
			}
			for (ActionTime action : assignment) {
				if (!action.getNode().isAvailable(visited)) {
					System.err.println("This set of assignments is impossible to finish");
					return false;
				}
				visited.add(action.getNode());
			}
		}
		BufferedAssignments buffered = new BufferedAssignments(assignedWorkflows);
		System.out.println(buffered.visualString());
		
		if (size != workflow.size()) {
			System.err.println(Integer.toString(size) + " actions were assigned. Should be " + workflow.size());
			return false;
		}
		return true;
	}
	
	public static void main(String argv[]) throws InterruptedException {
		List<Scheduler> schedulers = Arrays.asList(
				//new RandomScheduler(),
				//(Scheduler)(new GreedyScheduler()),
				//new WeightByShortest(),
				//new WeightByDifference(),
				(Scheduler)(new ExhaustiveStarScheduler(false))
				);
		
		int numTries = 100;
		/*for (Map.Entry<String, Map<Workflow.Node, Double>> entry : actionTimeLookup.entrySet()) {
			System.out.println("Workflow time for " + entry.getKey() + ": " + SchedulingComparison.getAgentsSoloTime(workflow, entry.getValue()));
		}*/
		Map<String, Double> factors = new HashMap<String, Double>();
		factors.put("human", 1.0);
		
		ActionTimeGenerator timeGenerator = new ActionTimeGenerator(factors);
		List<Integer> connectedness = Arrays.asList(60, 40, 20);
		Collections.shuffle(connectedness);
		
		for (Integer edges : connectedness) {
			for (int i = 0; i < numTries; i++) {
				Workflow workflow = SchedulingComparison.buildSortedWorkflow(20, edges);
				
				for (Scheduler scheduler : schedulers) {
					List<Assignment> assignments;
					workflow = SchedulingComparison.buildSortedWorkflow(20, edges);
					
					while(true) {
					/*if (!SchedulingComparison.verifySortedWorkflow(workflow)) {
						System.err.println("Workflow is not actually sorted");
					}*/
					assignments = scheduler.schedule(workflow, Arrays.asList("human", "friend"), timeGenerator);
					
					/*if (!SchedulingComparison.verifyAssignments(workflow, assignments)) {
						System.err.println("Error with assignments");
					}*/
					}
					//double time = new BufferedAssignments(assignments).time();
					//System.out.println(scheduler.getClass().getSimpleName() + ", " + edges + ": " + time);
					
				}
				timeGenerator.clear();
				System.out.println("\n\n");
			}
		}
		
	}

	
}
