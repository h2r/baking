package edu.brown.cs.h2r.baking.Experiments;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import edu.brown.cs.h2r.baking.Scheduling.AssignedWorkflow;
import edu.brown.cs.h2r.baking.Scheduling.AssignedWorkflow.ActionTime;
import edu.brown.cs.h2r.baking.Scheduling.ExhaustiveScheduler;
import edu.brown.cs.h2r.baking.Scheduling.GreedyScheduler;
import edu.brown.cs.h2r.baking.Scheduling.RandomScheduler;
import edu.brown.cs.h2r.baking.Scheduling.Scheduler;
import edu.brown.cs.h2r.baking.Scheduling.SchedulingHelper;
import edu.brown.cs.h2r.baking.Scheduling.WeightByDifference;
import edu.brown.cs.h2r.baking.Scheduling.WeightByShortest;
import edu.brown.cs.h2r.baking.Scheduling.Workflow;
import edu.brown.cs.h2r.baking.Scheduling.Workflow.Node;

public class SchedulingComparison {

	public static Workflow buildSortedWorkflow(int numberNodes) {
		Random rando = new Random();
		List<Workflow.Node> nodes = new ArrayList<Workflow.Node>();
		
		for (int i = 0; i < numberNodes; i++) {
			nodes.add(new Workflow.Node(i));
		}
		Workflow workflow = new Workflow(nodes);
		
		for (int i = 0; i < numberNodes; i++) {
			int from = rando.nextInt(numberNodes);
			int to = rando.nextInt(numberNodes);
			workflow.connect(from, to);
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
	
	public static boolean verifyAssignments(Workflow workflow, List<AssignedWorkflow> assignedWorkflows) {
		int size = 0;
		for (AssignedWorkflow assignedWorkflow : assignedWorkflows) {
			size += assignedWorkflow.size();
			for (ActionTime time : assignedWorkflow) {
				int duration = (int)(time.getTime() * 10);
				String label = (time.getNode() == null ) ? "." : time.getNode().toString();
				int length = duration * 3;
				length = Math.max(1, length);
				if (length > 0) {
					label = String.format("%" + length + "s", label);
					System.out.print(label.replace(' ', '.'));
				}
				
			}
			System.out.print("\n");
		}
		System.out.println("\n\n");
		
		
		
		if (size != workflow.size()) {
			System.err.println(Integer.toString(size) + " actions were assigned. Should be " + workflow.size());
			return false;
		}
		return true;
	}
	public static void main(String argv[]) {
		List<Scheduler> schedulers = Arrays.asList(
				new RandomScheduler(),
				new GreedyScheduler(),
				new WeightByShortest(),
				new WeightByDifference(),
				new ExhaustiveScheduler(3)/*,
				new ExhaustiveScheduler()*/
				);
		
		int numTries = 100;
		/*for (Map.Entry<String, Map<Workflow.Node, Double>> entry : actionTimeLookup.entrySet()) {
			System.out.println("Workflow time for " + entry.getKey() + ": " + SchedulingComparison.getAgentsSoloTime(workflow, entry.getValue()));
		}*/
		
		for (Scheduler scheduler : schedulers) {
			double sum = 0.0;
			for (int i = 0; i < numTries; i++) {
				Workflow workflow = SchedulingComparison.buildSortedWorkflow(20);
				Map<String, Map<Workflow.Node, Double>> actionTimeLookup = SchedulingComparison.buildActionTimeLookup(workflow, 2);
				
				List<AssignedWorkflow> assignments = scheduler.schedule(workflow, actionTimeLookup);
				//SchedulingComparison.verifyAssignments(workflow, assignments);
				sum += SchedulingHelper.computeSequenceTime(assignments);
			}
			System.out.println(scheduler.getClass().getCanonicalName() + ": " + sum / numTries);
		}
	}
}