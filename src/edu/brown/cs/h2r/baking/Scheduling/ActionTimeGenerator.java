package edu.brown.cs.h2r.baking.Scheduling;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import burlap.oomdp.singleagent.GroundedAction;

public class ActionTimeGenerator {
	private final Map<String, Double> actionTimeLookup;
	private final Map<String, Double> biasFactors;
	private final Random random = new Random();
	
	public ActionTimeGenerator(Map<String, Double> mapFactorLookup) {
		this.biasFactors = mapFactorLookup;
		this.actionTimeLookup = new HashMap<String, Double>();
	}
	
	public double get(GroundedAction action) {
		Double time = this.actionTimeLookup.get(action.params[0]);
		if (time == null) {
			time = this.generateNewTime(action);
		}
		return time;
	}
	
	private Double generateNewTime(GroundedAction action) {
		String agent = action.params[0];
		Double factor = this.biasFactors.get(agent);
		factor = (factor == null) ? 1.0 : factor;
		double roll = this.random.nextDouble();
		double time = factor * roll;
		this.actionTimeLookup.put(action.toString(), time);
		return time;
	}
	
	public void clear() {
		this.actionTimeLookup.clear();
	}
}
