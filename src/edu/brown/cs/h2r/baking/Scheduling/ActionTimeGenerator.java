package edu.brown.cs.h2r.baking.Scheduling;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import burlap.oomdp.singleagent.GroundedAction;

public class ActionTimeGenerator {
	private final Map<String, Double> actionTimeLookup;
	private final Map<String, List<List<Double>>> humanParsedTimes; 
	private final Map<String, List<List<Double>>> robotParsedTimes; 
	private final Map<String, Double> biasFactors;
	private final Random random = new Random();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();
	private final Map<String, String> conversions;
	
	
	
	public ActionTimeGenerator(Map<String, Double> mapFactorLookup) {
		this.biasFactors = Collections.unmodifiableMap(mapFactorLookup);
		this.actionTimeLookup = new HashMap<String, Double>();
		this.humanParsedTimes = null;
		this.robotParsedTimes = null;
		this.conversions = null;
	}
	
	public ActionTimeGenerator(Boolean generateFromFiles) {
		this.conversions = new HashMap<String, String>();
		this.conversions.put("stir", "mix");
		this.conversions.put("put", "move");
		this.humanParsedTimes = this.parseFromFile("action_times.csv");
		this.robotParsedTimes = this.parseFromFile("robot_times.csv");
		this.biasFactors = new HashMap<String, Double>();
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
		double time = -1.0;
		if (this.humanParsedTimes != null) {
			time = this.generateNewTimeFromParsedData(action);
		}
		if (time < 0.0) {
			String agent = action.params[0];
			Double factor = this.biasFactors.get(agent);
			factor = (factor == null) ? 10.0 : factor;
			double roll = this.random.nextDouble();
			time = factor * roll;
		}
		try {
			this.writeLock.lock();
			this.actionTimeLookup.put(action.toString(), time);
		} finally {
			this.writeLock.unlock();
		}
		
		return time;
	}
	
	private Double generateNewTimeFromParsedData(GroundedAction action) {
		String actionName = action.actionName();
		
		
		String agent = action.params[0];
		
		 Map<String, List<List<Double>>> mapToUse = (agent.equals("human")) ?
				 this.humanParsedTimes : this.robotParsedTimes;
		List<List<Double>> map = mapToUse.get(actionName);
		if (map == null) {
			return -1.0;
		}
		
		int choice = this.random.nextInt(map.size());
		List<Double> selection = map.get(choice);
		double mean = selection.get(0);
		double std = selection.get(1);
		
		double time = -1.0;
		while (time < 0.0) {
			time = this.random.nextGaussian() * std + mean;
		}
		System.out.println(action + ", " + time);
		return time;
		
	}
	
	private Map<String, List<List<Double>>> parseFromFile(String filename) {
		
		ClassLoader CLDR = this.getClass().getClassLoader();
		
		URL resourceURL = CLDR.getResource(filename);
		if (resourceURL == null) {
			throw new RuntimeException("File " + filename + " does not exist in directory " + CLDR.getResource(".").getFile());
		}
		
		InputStream in;
		try {
			in = resourceURL.openStream();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
			
		}
		if (in == null) {
			throw new RuntimeException("File " + filename + " does not exist in directory " + CLDR.getResource(".").getFile());
		}
		
		Map<String, List<List<Double>>> timesMap = new HashMap<String, List<List<Double>>>();
		
		BufferedReader fileReader = new BufferedReader(new InputStreamReader(in));
		
		String line = null;
		try {
			line = fileReader.readLine();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		while (line != null) {
			String[] items = line.split(", ");
			String action = items[0];
			
			String converted = this.conversions.get(action);
			action = (converted != null) ? converted : action;
			
			
			
			List<String> params = new ArrayList<String>();
			List<Double> times = new ArrayList<Double>();
			
			for (int i = 1; i < items.length; i++) {
				if (i < items.length - 2) {
					params.add(items[i]);
				} else {
					times.add(Double.valueOf(items[i]));
				}
			}
			if (times.size() == 1) {
				times.add(times.get(0) * 0.25);
			}
			
			List<List<Double>> allTimes = timesMap.get(action);
			if (allTimes == null) {
				allTimes = new ArrayList<List<Double>>();
				timesMap.put(action, allTimes);
			}
			allTimes.add(times);
			
			try {
				line = fileReader.readLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			fileReader.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return timesMap;
	}
	
	public void clear() {
		this.actionTimeLookup.clear();
	}
}
