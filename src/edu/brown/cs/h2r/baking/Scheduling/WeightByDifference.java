package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.brown.cs.h2r.baking.Scheduling.Workflow.Node;

public class WeightByDifference extends HeuristicScheduler {
	public WeightByDifference(boolean useActualValues) {
		super(useActualValues);
	}
	@Override
	protected Map<Node, Map<String, Double>> getWeights(
			Map<Node, Map<String, Double>> times, Assignments assignments) {
		Map<Node, Map<String, Double>> weights = new HashMap<Node, Map<String, Double>>();
		
		for(Map.Entry<Node, Map<String,Double>> entry : times.entrySet()) {
			Map<String, Double> agentTimes = entry.getValue();
			Workflow.Node node = entry.getKey();
			
			Map<String, Double> weightMap = new HashMap<String, Double>();
			weights.put(node, weightMap);
			
			List<Double> maxValues = this.maxValue(agentTimes);
			double difference = maxValues.get(1) - maxValues.get(0);
			if (difference < 0) {
				System.err.println("Difference is less than 0");
			}
			for (Map.Entry<String, Double> entry2 : agentTimes.entrySet()) {
				String agent = entry2.getKey();
				if (maxValues.get(0) == entry2.getValue()) {
					weightMap.put(agent, difference);
				} else {
					weightMap.put(agent, 0.0);
				}
			}
		}
		
		return weights;
	}
	
	private List<Double> maxValue(Map<String, Double> times) {
		List<Double> topValues = new ArrayList<Double>();
		for (Map.Entry<String, Double> entry : times.entrySet()) {
			topValues.add(entry.getValue());
			Collections.sort(topValues);
			if (topValues.size() > 2) {
				topValues.remove(2);
			}
		}
		return topValues;
	}
}
