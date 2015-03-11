package edu.brown.cs.h2r.baking.Scheduling;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import edu.brown.cs.h2r.baking.Scheduling.Workflow.Node;

public class CombinedHeuristic extends HeuristicScheduler {

	List<HeuristicScheduler> schedulers;
	public CombinedHeuristic(boolean useActualValues, HeuristicScheduler... schedulers) {
		super(useActualValues);
		this.schedulers = Arrays.asList(schedulers);
	}

	@Override
	protected Map<Node, Map<String, Double>> getWeights(
			Map<Node, Map<String, Double>> times,
			Assignments assignments) {
		// TODO Auto-generated method stub
		return null;
	}


}
