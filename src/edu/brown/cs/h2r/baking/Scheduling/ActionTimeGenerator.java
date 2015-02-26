package edu.brown.cs.h2r.baking.Scheduling;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import burlap.oomdp.core.Domain;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;

public class ActionTimeGenerator {
	private final Map<GroundedAction, Double> actionTimeLookup;
	private final Map<GroundedAction, String> realDataChoice;
	private String humanSubject;
	private final Map<String, Map<String, Map<String, List<Double>>>> humanParsedTimes; 
	private final Map<String, Map<String, Map<String, List<Double>>>> robotParsedTimes; 
	private final Map<String, Double> biasFactors;
	private final Random random = new Random();
	private final ReadWriteLock lock = new ReentrantReadWriteLock();
	private final Lock readLock = lock.readLock();
	private final Lock writeLock = lock.writeLock();
	private final Map<String, String> conversions;
	
	
	
	public ActionTimeGenerator(Map<String, Double> mapFactorLookup) {
		this.biasFactors = Collections.unmodifiableMap(mapFactorLookup);
		this.actionTimeLookup = new HashMap<GroundedAction, Double>();
		this.realDataChoice = new HashMap<GroundedAction, String>();
		this.humanParsedTimes = null;
		this.robotParsedTimes = null;
		this.conversions = null;
	}
	
	private ActionTimeGenerator(Map<String, Double> biasFactors, Map<GroundedAction, Double> actionTimes, Map<GroundedAction, String> realDataChoice,
			Map<String, Map<String, Map<String, List<Double>>>> humanParsedTimes, Map<String, Map<String, Map<String, List<Double>>>> robotParsedTimes,
			String humanSubject) {
		this.biasFactors = (biasFactors == null) ? new HashMap<String, Double>() : Collections.unmodifiableMap(biasFactors);
		this.actionTimeLookup = new HashMap<GroundedAction, Double>(actionTimes);
		this.realDataChoice = new HashMap<GroundedAction, String>(realDataChoice);
		this.humanParsedTimes = humanParsedTimes;
		this.robotParsedTimes = robotParsedTimes;
		this.conversions = new HashMap<String, String>();
		this.conversions.put("stir", "mix");
		this.conversions.put("put", "move");
		this.humanSubject = humanSubject;
		
	}
	
	public ActionTimeGenerator(Boolean generateFromFiles, Boolean leaveOneOut) {
		this.conversions = new HashMap<String, String>();
		this.conversions.put("stir", "mix");
		this.conversions.put("put", "move");
		this.realDataChoice = new HashMap<GroundedAction, String>();
		List<String> humanSubjects = new ArrayList<String>();
		this.humanParsedTimes = this.parseFromFile("action_times.csv", humanSubjects);
		this.robotParsedTimes = this.parseFromFile("robot_times.csv", null);
		if (leaveOneOut) {
			Integer choice = this.random.nextInt(humanSubjects.size());
			this.humanSubject = humanSubjects.get(choice); 
		} else {
			this.humanSubject = null;
		}
		this.biasFactors = new HashMap<String, Double>();
		this.actionTimeLookup = new HashMap<GroundedAction, Double>();
		
		
	}
	
	public double get(GroundedAction action, boolean actualValue) {
		// if not using parsed table, use lookup
		// if using parsed table, and !actualValue generate a new time with mean
		// if using parsed table, and actualValue generate a sample from the distribution
		// 
		Double time = null;
		
		if (actualValue) {
			time = this.getActualValue(action);
			if (time != null) {
				return time;
			}
		}
		
		try {
			this.readLock.lock();
			time = this.actionTimeLookup.get(action);
		} finally {
			this.readLock.unlock();
		}
		if (time == null) {
			time = this.generateNewTime(action);
		}
		
		if (actualValue) {
			double newTime = -1.0;
			while (newTime <= 0.0) {
				double randomValue = this.random.nextDouble();
				newTime = randomValue * (0.25 * time) + time;
			}
			time = newTime;
		}
		
		return time;
	}
	
	public Map<GroundedAction, Double> getMap() {
		return new HashMap<GroundedAction, Double>(this.actionTimeLookup);
	}
	
//	private final Map<GroundedAction, Double> actionTimeLookup;
//	private final Map<GroundedAction, String> realDataChoice;
//	private String humanSubject;
//	private final Map<String, Map<String, Map<String, List<Double>>>> humanParsedTimes; 
//	private final Map<String, Map<String, Map<String, List<Double>>>> robotParsedTimes; 
//	private final Map<String, Double> biasFactors;
	
	public static ActionTimeGenerator fromMap(Domain domain, Map<String, Object> map) {
		
		String humanSubject = (String)map.get("human_subject");
		Map<String, Map<String, Map<String, List<Double>>>> humanParsedTimes = 
				(Map<String, Map<String, Map<String, List<Double>>>>)map.get("human_parsed_times");
		Map<String, Map<String, Map<String, List<Double>>>> robotParsedTimes = 
				(Map<String, Map<String, Map<String, List<Double>>>>)map.get("robot_parsed_times");
		Map<String, Double> biasFactors = (Map<String, Double>)map.get("bias_factors");
		
		Map<List<String>, Double> actionTimesStr = (Map<List<String>, Double>)map.get("action_times");
		Map<GroundedAction, Double> actionTimes = new HashMap<GroundedAction, Double>();
		for (Map.Entry<List<String>, Double> entry : actionTimesStr.entrySet()) {
			List<String> params = entry.getKey();
			Action action = (domain == null || params.get(0) == null) ? null : domain.getAction(params.get(0));
			String[] paramsArry = params.subList(1, params.size()).toArray(new String[params.size() - 1]);
			GroundedAction ga = new GroundedAction(action, paramsArry);
			actionTimes.put(ga, entry.getValue());
		}
		
		Map<List<String>, String> realDataChoiceStr = (Map<List<String>, String>)map.get("real_data_choice");
		Map<GroundedAction, String> realDataChoice = new HashMap<GroundedAction, String>();
		for (Map.Entry<List<String>, String> entry : realDataChoiceStr.entrySet()) {
			List<String> params = entry.getKey();
			Action action = (domain == null || params.get(0) == null) ? null : domain.getAction(params.get(0));
			String[] paramsArry = params.subList(1, params.size()).toArray(new String[params.size() - 1]);
			GroundedAction ga = new GroundedAction(action, paramsArry);
			realDataChoice.put(ga, entry.getValue());
		}
		
		return new ActionTimeGenerator(biasFactors, actionTimes, realDataChoice, humanParsedTimes, robotParsedTimes, humanSubject);
	}
	
	public Map<String, Object> toMap() {
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("bias_factors", this.biasFactors);
		if (this.humanParsedTimes != null) {
			map.put("human_parsed_times", this.humanParsedTimes);
			map.put("robot_parsed_times", this.robotParsedTimes);
		}
		if (this.humanSubject != null) {
			map.put("human_subject", this.humanSubject);
		}
		
		Map<List<String>, Double> actionTimes = new HashMap<List<String>, Double>();
		for (Map.Entry<GroundedAction, Double> entry : actionTimeLookup.entrySet()) {
			GroundedAction ga = entry.getKey();
			List<String> params = new ArrayList<String>(Arrays.asList(ga.params));
			String actionName = (ga.action == null) ? null : ga.actionName();
			params.add(0, actionName);
			actionTimes.put(params, entry.getValue());
		}
		map.put("action_times", actionTimes);
		
		Map<List<String>, String> realDataChoice = new HashMap<List<String>, String>();
		for (Map.Entry<GroundedAction, String> entry : this.realDataChoice.entrySet()) {
			GroundedAction ga = entry.getKey();
			List<String> params = new ArrayList<String>(Arrays.asList(ga.params));
			String actionName = (ga.action == null) ? null : ga.actionName();
			params.add(0, actionName);
			realDataChoice.put(params, entry.getValue());
		}
		map.put("real_data_choice", realDataChoice);
		
		return map;
	}
	
	private Double getActualValue(GroundedAction action) {
		
		if (!action.params[0].equals("human")) {
			return null;
		}
		if (this.humanParsedTimes == null) {
			return null;
		}
		
		String choice = this.realDataChoice.get(action.toString());
		String actionName = action.actionName();
		Map<String, Map<String, List<Double>>> actionData = this.humanParsedTimes.get(actionName);
		if (actionData == null) {
			return null;
		}
		
		Map<String, List<Double>> agentsData = actionData.get(choice);
		if (agentsData == null) {
			return null;
		}
		
		List<Double> times = agentsData.get(this.humanSubject);
		if (times == null) {
			return null;
		}
		Integer randomChoice = this.random.nextInt(times.size());
		return times.get(randomChoice);
		
	}
	
	private Double generateNewTime(GroundedAction action) {
		Double time = null;
		if (this.humanParsedTimes != null) {
			time = this.generateNewTimeFromParsedData(action);
		}
		if (time == null) {
			String agent = action.params[0];
			Double factor = this.biasFactors.get(agent);
			factor = (factor == null) ? 10.0 : factor;
			double roll = this.random.nextDouble();
			time = 9.0 * roll + 1;
			time = (this.random.nextDouble() + 0.25) * time;
		}
		try {
			this.writeLock.lock();
			this.actionTimeLookup.put(action, time);
		} finally {
			this.writeLock.unlock();
		}
		
		return time;
	}
	
	private Double generateNewTimeFromParsedData(GroundedAction action) {
		String actionName = action.actionName();
		String agent = action.params[0];
		
		 Map<String, Map<String, Map<String, List<Double>>>> mapToUse = (agent.equals("human")) ?
				 this.humanParsedTimes : this.robotParsedTimes;
		Map<String, Map<String, List<Double>>> map = mapToUse.get(actionName);
		if (map == null) {
			return null;
		}
		
		List<String> possibleActions = new ArrayList<String>(map.keySet());
		Integer randomChoice = this.random.nextInt(possibleActions.size());
		String choice = possibleActions.get(randomChoice);
		Map<String, List<Double>> actionsMap = map.get(choice);
		if (actionsMap == null) {
			return null;
		}
		
		this.realDataChoice.put(action, choice);
		
		
		List<Double> times = new ArrayList<Double>();
		for (Map.Entry<String, List<Double>> entry : actionsMap.entrySet()) {
			String subject = entry.getKey();
			
			if (subject != this.humanSubject) {
				List<Double> subjectsTimes = entry.getValue();
				times.addAll(subjectsTimes);
			}
		}
		
		return this.mean(times);	
	}
	
	private double mean(List<Double> values) {
		if (values.isEmpty()) {
			throw new RuntimeException("Values list is empty");
		}
		double sum = 0.0;
		for (Double value : values) sum += value;
		return sum / values.size();
	}
	
	private double std(List<Double> values) {
		if (values.size() == 1) {
			return values.get(0) * 0.25;
		}
		double mean = this.mean(values);
		double sum = 0.0;
		for (Double value : values) sum += (value - mean) * (value - mean);
		return Math.sqrt(sum / values.size());
	}
	
	private Map<String, Map<String, Map<String, List<Double>>>> parseFromFile(String filename, List<String> humanAgents) {
		
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
		
		// Action, Params, Agent, times
		Map<String, Map<String, Map<String, List<Double>>>> timesMap = new HashMap<String, Map<String, Map<String, List<Double>>>>();
		
		BufferedReader fileReader = new BufferedReader(new InputStreamReader(in));
		
		String line = null;
		try {
			line = fileReader.readLine();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		while (line != null) {
			String[] items = line.split(", ");
			String action = items[0];
			
			String converted = this.conversions.get(action);
			action = (converted != null) ? converted : action;
			
			
			
			List<String> params = new ArrayList<String>();
			List<Double> times = new ArrayList<Double>();
			
			for (String item : items) {
				try {
					Double number = Double.parseDouble(item);
					times.add(number);
				} catch(NumberFormatException e) {
					params.add(item);
				}
			}
			
			Map<String, Map<String, List<Double>>> allTimes = timesMap.get(action);
			if (allTimes == null) {
				allTimes = new HashMap<String, Map<String, List<Double>>>();
				timesMap.put(action, allTimes);
			}
			
			List<String> actionParams = params.subList(2, params.size());
			String actionParamsStr = actionParams.toString();
			Map<String, List<Double>> parametrizedTimes = allTimes.get(actionParamsStr);
			if (parametrizedTimes == null) {
				parametrizedTimes = new HashMap<String, List<Double>>();
				allTimes.put(actionParamsStr, parametrizedTimes);
			}
			
			String subject = params.get(1);
			if (humanAgents != null) {
				humanAgents.add(subject);
			}
			List<Double> agentsTimes = parametrizedTimes.get(subject);
			if (agentsTimes == null) {
				agentsTimes = new ArrayList<Double>();
				parametrizedTimes.put(subject, agentsTimes);
			}
			agentsTimes.addAll(times);
			
			try {
				line = fileReader.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			fileReader.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return timesMap;
	}
	
	public void clear() {
		this.actionTimeLookup.clear();
		this.realDataChoice.clear();
	}
}
