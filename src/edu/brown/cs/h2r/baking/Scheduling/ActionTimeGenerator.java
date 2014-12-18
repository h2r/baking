package edu.brown.cs.h2r.baking.Scheduling;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import burlap.oomdp.singleagent.GroundedAction;

public class ActionTimeGenerator {
	private final Map<String, Double> actionTimeLookup;
	private final Map<String, Double> biasFactors;
	private final Random random = new Random();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();
	public ActionTimeGenerator(Map<String, Double> mapFactorLookup) {
		this.biasFactors = Collections.unmodifiableMap(mapFactorLookup);
		this.actionTimeLookup = new HashMap<String, Double>();
	}
	
	
	public double get(GroundedAction action) {
		Double time = null;
		try {
			this.readLock.lock();
			time = this.actionTimeLookup.get(action.toString());
		} finally {
			this.readLock.unlock();
		}
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
		
		try {
			this.writeLock.lock();
			this.actionTimeLookup.put(action.toString(), time);
		} finally {
			this.writeLock.unlock();
		}
		
		return time;
	}
	
	public void clear() {
		this.actionTimeLookup.clear();
	}
}
