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
import edu.brown.cs.h2r.baking.Scheduling.HeuristicSearchSequencer;
import edu.brown.cs.h2r.baking.Scheduling.MILPScheduler;
import edu.brown.cs.h2r.baking.Scheduling.Workflow;
import edu.brown.cs.h2r.baking.Scheduling.Workflow.Node;

public class SequencerComparison {

	public static Workflow buildSortedWorkflow(int numberNodes, int numberEdges, int numberResources, int resourcesPerNode) {
		Random rando = new Random();
		List<Workflow.Node> nodes = new ArrayList<Workflow.Node>();
		
		for (int i = 0; i < numberNodes; i++) {
			Set<String> resources = new HashSet<String>();
			while (resources.size() < resourcesPerNode) {
				resources.add(Integer.toString(rando.nextInt(numberResources)));
			}
			nodes.add(new Workflow.Node(i, resources));
			
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
		BufferedAssignments buffered = new BufferedAssignments(assignedWorkflows, false);
		System.out.println(buffered.visualString());
		
		if (size != workflow.size()) {
			System.err.println(Integer.toString(size) + " actions were assigned. Should be " + workflow.size());
			return false;
		}
		return true;
	}
	
	public static void main(String argv[]){
		MILPScheduler milp = new MILPScheduler(false);
		
		int trialId = new Random().nextInt();
		trialId = 1;
		if (argv.length == 2) {
			trialId = Integer.parseInt(argv[0]);
		}
		int numTries = 1;
		/*for (Map.Entry<String, Map<Workflow.Node, Double>> entry : actionTimeLookup.entrySet()) {
			System.out.println("Workflow time for " + entry.getKey() + ": " + SchedulingComparison.getAgentsSoloTime(workflow, entry.getValue()));
		}*/
		Map<String, Double> factors = new HashMap<String, Double>();
		
		System.out.println("MILP, Rearrange, Tercio, Search");
		ActionTimeGenerator timeGenerator = new ActionTimeGenerator(factors);
		List<Integer> connectedness = Arrays.asList(20);
		HeuristicSearchSequencer sequencer = new HeuristicSearchSequencer(true);
		Random random = new Random();
		boolean shuffleOrder = true;
		//Collections.shuffle(connectedness);
		for (int j = 4; j < 40; j++) {
			for (int i = 0; i < numTries; i++) {
				int numEdges =  j;
				int numResources = j;
				int numResourcesPerNode = 3;
				if (trialId % 3 == 0) {
					numEdges = random.nextInt(2*j-4);
					numResources = random.nextInt(j);
					numResourcesPerNode = random.nextInt(Math.max(1, numResources/2));
				} else if (trialId % 3 == 1) {
					numEdges = 2 * j-4;
					numResources = j;
					numResourcesPerNode = 1;
				}
				Workflow workflow = SequencerComparison.buildSortedWorkflow(j, numEdges, numResources, numResourcesPerNode);
				List<Assignment> assignments = new ArrayList<Assignment>();
				
				double time = Double.MAX_VALUE;
				List<String> agents = Arrays.asList("human", "friend", "friend1", "friend2");
				time = milp.schedule(workflow, assignments, agents, timeGenerator);
				
				
				if (shuffleOrder) {
					for (Assignment assignment : assignments) {
						assignment.shuffle();
					}
				}
				
				boolean didPrint = false;
				BufferedAssignments buffered =  new BufferedAssignments(timeGenerator, agents, false, false);
				buffered.sequenceTasksWithReorder(assignments);
				//MILPScheduler.checkAssignments(workflow, assignments, buffered);
				double bTime = buffered.time();
				
				//if (bTime - 0.0001 > time) {
					//System.out.println(buffered.visualString());
					//System.out.println(buffered.getFullString());
					//didPrint = true;
				//}
				BufferedAssignments tercio = new BufferedAssignments(assignments, true);
				//MILPScheduler.checkAssignments(workflow, assignments, tercio);
				double tTime = tercio.time();
				if (tTime - 0.0001 > time) {
					//System.out.println(tercio.visualString());
					//System.out.println(tercio.getFullString());
					didPrint = true;
				}
				
				BufferedAssignments search = sequencer.sequenceAssignments(assignments);
				//MILPScheduler.checkAssignments(workflow, assignments, search);
				
				double sTime = search.time();
				if (sTime -0.0001 > time) {
					//System.out.println(search.visualString());
					//System.out.println(search.getFullString());
					didPrint = true;
				}
				
				
				System.out.println(j + ", " + time + ", " + bTime + ", " + tTime + ", " + sTime);
				
				timeGenerator.clear();
			}
		}
		
	}

	
}
