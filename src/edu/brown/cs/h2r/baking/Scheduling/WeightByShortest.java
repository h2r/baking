package edu.brown.cs.h2r.baking.Scheduling;

import java.util.HashMap;
import java.util.Map;

import edu.brown.cs.h2r.baking.Scheduling.Workflow.Node;

public class WeightByShortest extends HeuristicScheduler {
	public WeightByShortest(boolean useActualValues) {
		super(useActualValues);
	}
	@Override
	protected Map<Node, Map<String, Double>> getWeights(
			Map<Node, Map<String, Double>> times, Assignments assignments) {
		Map<Node, Map<String, Double>> weights = new HashMap<Node, Map<String, Double>>();
		
		double baseTime = assignments.time();
		
		
		for(Map.Entry<Node, Map<String,Double>> entry : times.entrySet()) {
			Map<String, Double> agentTimes = entry.getValue();
			Workflow.Node node = entry.getKey();
			
			Map<String, Double> weightMap = new HashMap<String, Double>();
			weights.put(node, weightMap);
			
			for (Map.Entry<String, Double> entry2 : agentTimes.entrySet()) {
				String agent = entry2.getKey();
				double time = assignments.getAssignment(agent).time();
				time += entry2.getValue();
				weightMap.put(agent, baseTime - time);
			}
		}
		
		return weights;
	}
	

}
