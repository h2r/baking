package edu.brown.cs.h2r.baking.Scheduling.Multitiered;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import scpsolver.lpsolver.LinearProgramSolver;
import scpsolver.lpsolver.SolverFactory;
import scpsolver.problems.LPSolution;
import scpsolver.problems.LPWizard;
import scpsolver.problems.LPWizardConstraint;
import scpsolver.problems.LinearProgram;
import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;
import edu.brown.cs.h2r.baking.Scheduling.Assignment.ActionTime;
import edu.brown.cs.h2r.baking.Scheduling.Workflow.Node;
import gurobi.GRB;
import gurobi.GRB.DoubleAttr;
import gurobi.GRBConstr;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class MILPScheduler {
	public static double TOLERANCE = 0.000001;
	private static double M = 1000.0;
	private final boolean useActualValues;
	public MILPScheduler(boolean useActualValues) {
		this.useActualValues = useActualValues;
	}

	public  double schedule(Workflow workflow, Assignments assignments, ActionTimeGenerator timeGenerator) {
		return this.assignTasks(workflow, assignments, timeGenerator);
	}

	public double assignTasks(Workflow workflow, Assignments assignments, ActionTimeGenerator timeGenerator) {
		double time = -1.0;
		try {
			GRBEnv    env   = new GRBEnv("mip1.log");
			env.set(GRB.IntParam.OutputFlag, 0); 
			GRBModel  model = new GRBModel(env);
			
			// Create variables

			Map<String, GRBVar> modelVariables = new HashMap<String, GRBVar>();
			GRBVar v = setupModelVariables(workflow, assignments, model, modelVariables);

			model.update();

			// Set objective: maximize x + y + 2 z

			GRBLinExpr expr = new GRBLinExpr();
			expr.addTerm(1.0, v);
			model.setObjective(expr, GRB.MINIMIZE);

			MILPScheduler.addConstraints(workflow, assignments, timeGenerator, model, modelVariables, v, this.useActualValues);

			// Add constraint: x + y >= 1

			// Optimize model
			model.update();
			
			model.presolve();
			
			model.optimize();
			int status = model.get(GRB.IntAttr.Status);
			if (status != 2) {
				System.err.println("Non-optimal solution found");
				return -1.0;
			}
			
			List<List<Double>> startTimes = this.extractAssignments(workflow, assignments, modelVariables);
			//printResults(model, modelVariables, false, false);
			time = v.get(GRB.DoubleAttr.X);
			//this.printResults(model, modelVariables, startTimes);

			model.dispose();
			env.dispose();

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " +
					e.getMessage());
		}
		return time;
	}

	public static boolean checkAssignments(Workflow workflow, Assignments assignments){
		ActionTimeGenerator timeGenerator = null;
		for (AgentAssignment assignment : assignments.getAssignments()) {
			timeGenerator = assignment.getTimeGenerator();
			break;
		}
		
		OrderPreservingSequencer sequencer = new OrderPreservingSequencer(false);
		Assignments buffered = sequencer.sequence(assignments, timeGenerator, workflow);
		return MILPScheduler.checkAssignments(workflow, assignments, buffered);
	}
	
	public static boolean checkAssignments(Workflow workflow, Assignments assignments, Assignments buffered){
		try {
			GRBEnv env = new GRBEnv("mip1.log");
			env.set(GRB.IntParam.OutputFlag, 0); 
			GRBModel  model = new GRBModel(env);
			
			ActionTimeGenerator timeGenerator = null;
			for (AgentAssignment assignment : assignments.getAssignments()) {
				timeGenerator = assignment.getTimeGenerator();
				break;
			}
			// Create variables

			Map<String, GRBVar> modelVariables = new HashMap<String, GRBVar>();
			GRBVar v = setupModelVariables(workflow, assignments, model,
					modelVariables);
			model.update();
			
			setVariableValues(workflow, buffered, modelVariables, v);
			// Integrate new variables
			
			model.update();	
			MILPScheduler.addConstraints(workflow, assignments, timeGenerator, model,
					modelVariables, v, false);
			model.update();
			return printResults(model, modelVariables, true, true);
		} catch (GRBException e) {
			System.out.println("Code: " + e.getErrorCode());
			System.out.println(e.getCause());
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
		
		return false;
	}


	private static void setVariableValues(Workflow workflow, Assignments buffered,
			Map<String, GRBVar> modelVariables, GRBVar v) throws GRBException {
		
		List<String> agents = new ArrayList<String>(buffered.getAgents());
		List<AgentAssignment> assignments = new ArrayList<AgentAssignment>(buffered.getAssignments());
		List<Subtask> nodes = new ArrayList<Subtask>();
		List<Double> startTimes = new ArrayList<Double>();
		List<Double> endTimes = new ArrayList<Double>();
		
		for (AgentAssignment assignment : assignments) {
			String agent = assignment.getId();
			double sum = 0.0;
			for (AssignedSubtask assignedSubtask : assignment) {
				Subtask subtask = assignedSubtask.getSubtask();
				if (subtask != null) {
					
					// set the action var corresponding to this agent
					GroundedAction action = subtask.getAction(assignment.getId());
					GRBVar actionVar = modelVariables.get(action.toString());
					actionVar.set(GRB.DoubleAttr.Start, 1.0);
					
					// for all other agents, set it to 0.0
					for (String agent2 : agents) {
						if (!agent.equals(agent2)) {
							action = subtask.getAction(agent2);
							actionVar = modelVariables.get(action.toString());
							actionVar.set(GRB.DoubleAttr.Start, 0.0);
						}
					}
					
					// get all nodes, start times and end times
					nodes.add(subtask);
					startTimes.add(sum);
					endTimes.add(sum + assignedSubtask.getTime());
				}
				
				sum += assignedSubtask.getTime();
				
			}
		}
		
		// set V to the last end time
		v.set(GRB.DoubleAttr.Start, Collections.max(endTimes));
		
		// set precedence bit for the nodes
		for (int i = 0; i < nodes.size(); i++) {
			String firstNodeStr = nodes.get(i).toString();
			double firstNodeStart = startTimes.get(i);
			double firstNodeEnd = endTimes.get(i);
			
			String firstNodeStartStr = firstNodeStr + "_START", firstNodeEndStr = firstNodeStr + "_END";
			GRBVar nodeStart = modelVariables.get(firstNodeStartStr);
			GRBVar nodeEnd = modelVariables.get(firstNodeEndStr);
			
			nodeStart.set(GRB.DoubleAttr.Start, firstNodeStart);
			nodeEnd.set(GRB.DoubleAttr.Start, firstNodeEnd);
			
			for (int j = 0; j < nodes.size(); j++) {
				if (i == j) {
					continue;
				}
				
				String secondNodeStr = nodes.get(j).toString();
				String firstBeforeSecondStr = firstNodeStr + "->" + secondNodeStr;
				
				double secondNodeStart = startTimes.get(j);
				GRBVar firstBeforeSecond = modelVariables.get(firstBeforeSecondStr);
				
				double value = (firstNodeEnd - TOLERANCE < secondNodeStart) ? 1.0 : 0.0;
				firstBeforeSecond.set(GRB.DoubleAttr.Start, value);	
			}
		}
	}

	private static GRBVar setupModelVariables(Workflow workflow,
			Assignments assignments, GRBModel model,
			Map<String, GRBVar> modelVariables) throws GRBException {
		GRBVar v = model.addVar(0.0, GRB.INFINITY, 1.0, GRB.CONTINUOUS, "v");
		modelVariables.put("v", v);

		for (Subtask node : workflow) {
			String nodeStr = node.toString();
			String nodeStartStr = nodeStr + "_START", nodeEndStr = nodeStr + "_END";

			GRBVar nodeStart = model.addVar(0.0, GRB.INFINITY, 1.0, GRB.CONTINUOUS, nodeStartStr);
			GRBVar nodeEnd = model.addVar(0.0, GRB.INFINITY, 1.0, GRB.CONTINUOUS, nodeEndStr);

			modelVariables.put(nodeStartStr, nodeStart);
			modelVariables.put(nodeEndStr, nodeEnd);

			for (AgentAssignment assignment : assignments.getAssignments()) {
				GroundedAction action = node.getAction(assignment.getId());
				String actionStr = action.toString();
				GRBVar actionAgent = model.addVar(0.0, 1.0, 1.0, GRB.BINARY, actionStr);
				modelVariables.put(actionStr, actionAgent);
			}
			for (Subtask secondNode : workflow) {
				if (!node.equals(secondNode)) {
					String firstNodeStr = node.toString();
					String secondNodeStr = secondNode.toString();
					
					String firstBeforeSecondStr = firstNodeStr + "->" + secondNodeStr;
					GRBVar firstBeforeSecond = model.addVar(0.0, 1.0, 1.0, GRB.BINARY, firstBeforeSecondStr);
					modelVariables.put(firstBeforeSecondStr, firstBeforeSecond);
				}
			}
		}
		return v;
	}
	
	private static void addConstraints(Workflow workflow,
			Assignments assignments, ActionTimeGenerator timeGenerator,
			GRBModel model, Map<String, GRBVar> modelVariables, GRBVar v, boolean useActualValues)
					throws GRBException {
		GRBLinExpr expr;
		// v is the latest action end time
		for (Subtask node : workflow) {
			String nodeStr = node.toString();
			String nodeStartStr = nodeStr + "_START", nodeEndStr = nodeStr + "_END";
			GRBVar nodeStart = modelVariables.get(nodeStartStr), nodeEnd = modelVariables.get(nodeEndStr);

			// v is the maximum of all action ending times
			expr = new GRBLinExpr();
			expr.addTerm(-1.0, v); expr.addTerm(1.0, nodeEnd);
			model.addConstr(expr, GRB.LESS_EQUAL, 0.0, nodeStr + "_v");

			// all start times must be greater or equal to 0. is this needed?
			expr = new GRBLinExpr();
			expr.addTerm(1.0, nodeStart);
			model.addConstr(expr, GRB.GREATER_EQUAL, 0.0, nodeStr + "_START");

		}

		// all actions are assigned exactly once (eq 2);
		for (Subtask node : workflow) {
			String nodeStr = node.toString();

			expr = new GRBLinExpr();
			for (AgentAssignment assignment : assignments.getAssignments()) {
				String agent = assignment.getId();
				GroundedAction action = node.getAction(agent);
				String actionStr = action.toString();
				GRBVar actionVar = modelVariables.get(actionStr);
				expr.addTerm(1.0, actionVar);
			}
			model.addConstr(expr, GRB.EQUAL, 1.0, nodeStr);
		}

		// if an action depends on another action, that one must go first
		for (Subtask firstNode : workflow) {
			String firstNodeStr = firstNode.toString();
			String firstNodeEndStr = firstNodeStr + "_END";
			GRBVar firstNodeEnd = modelVariables.get(firstNodeEndStr);

			for (Subtask secondNode : workflow) {
				if (secondNode.isConstrainedTo(firstNode)) {
					String secondNodeStr = secondNode.toString();
					String secondNodeStartStr = secondNodeStr + "_START";
					GRBVar secondNodeStart = modelVariables.get(secondNodeStartStr);

					String firstBeforeSecondDeadline = firstNodeStr + "_deadline_" + secondNodeStr;
					double deadline = secondNode.getDeadline(firstNode);
					expr = new GRBLinExpr();
					expr.addTerm(1.0, secondNodeStart); expr.addTerm(-1.0, firstNodeEnd);
					model.addConstr(expr, GRB.LESS_EQUAL, deadline, firstBeforeSecondDeadline);
					
					String firstBeforeSecondWait = firstNodeStr + "_wait_" + secondNodeStr;
					double wait = secondNode.getWait(firstNode);
					expr = new GRBLinExpr();
					expr.addTerm(1.0, secondNodeStart); expr.addTerm(-1.0, firstNodeEnd);
					model.addConstr(expr, GRB.GREATER_EQUAL, wait, firstBeforeSecondWait);
				}
			}
		}

		// end time - start time must equal the expected time of the action for the agent (if its assigned), otherwise greater than 0.0
		for (AgentAssignment assignment : assignments.getAssignments()) {
			String agent = assignment.getId();

			for (Subtask node : workflow) {
				String nodeStr = node.toString();
				String nodeStartStr = nodeStr + "_START", nodeEndStr = nodeStr + "_END";
				GRBVar nodeStart = modelVariables.get(nodeStartStr), nodeEnd = modelVariables.get(nodeEndStr);


				GroundedAction action = node.getAction(agent);
				String actionStr = action.toString();
				GRBVar actionVar = modelVariables.get(actionStr);
				double time = timeGenerator.get(action, useActualValues );
				
				expr = new GRBLinExpr();
				expr.addTerm(1.0, nodeEnd); expr.addTerm(-1.0, nodeStart);expr.addTerm(-time, actionVar);
				model.addConstr(expr, GRB.GREATER_EQUAL, 0.0, actionStr + "_lb");						
			}
		}
		
		
		
		// If two actions have a resource conflict, or are assigned to the same agent they cannot overlap
		for (Subtask firstNode : workflow) {
			String firstNodeStr = firstNode.toString();
			String firstNodeEndStr = firstNodeStr + "_END";
			GRBVar firstNodeEnd = modelVariables.get(firstNodeEndStr);

			for (Subtask secondNode : workflow) {
				if (firstNode.equals(secondNode)) {
					continue;
				}
				String secondNodeStr = secondNode.toString();
				String secondNodeStartStr = secondNodeStr + "_START";
				GRBVar secondNodeStart = modelVariables.get(secondNodeStartStr);

				
				String firstBeforeSecondStr = firstNodeStr + "->" + secondNodeStr;
				GRBVar firstBeforeSecond = modelVariables.get(firstBeforeSecondStr);
				
				expr = new GRBLinExpr();
				expr.addTerm(1.0, secondNodeStart); 
				expr.addTerm(-1.0, firstNodeEnd);
				expr.addTerm(-M, firstBeforeSecond);
				model.addConstr(expr, GRB.GREATER_EQUAL, -M, firstBeforeSecondStr);
			}
		}
		
		// If an agent is assigned two actions or they have resource conflict, the precedent variable must be set
		for (Subtask firstNode : workflow) {
			String firstNodeStr = firstNode.toString();
			String firstNodeEndStr = firstNodeStr + "_END";
			GRBVar firstNodeEnd = modelVariables.get(firstNodeEndStr);
			
			for (Subtask secondNode : workflow) {
				if (firstNode.equals(secondNode)) {
					continue;
				}
				String secondNodeStr = secondNode.toString();
				String secondNodeStartStr = secondNodeStr + "_START";
				GRBVar secondNodeStart = modelVariables.get(secondNodeStartStr);

				String firstBeforeSecondStr = firstNodeStr + "->" + secondNodeStr;
				GRBVar firstBeforeSecond = modelVariables.get(firstBeforeSecondStr);
				String secondBeforeFirstStr = secondNodeStr + "->" + firstNodeStr;
				GRBVar secondBeforeFirst = modelVariables.get(secondBeforeFirstStr);
				
				String overlapName = firstNodeStr + "_" + secondNodeStr + "_precedence";
				
				if (firstNode.resourceConflicts(secondNode)) {
					// One of the precedence bits must be set
					String resourceConflict = firstNodeStr + "_" + secondNodeStr + "_conflict";
					expr = new GRBLinExpr();
					expr.addTerm(1.0, firstBeforeSecond); 
					expr.addTerm(1.0, secondBeforeFirst);
					model.addConstr(expr, GRB.EQUAL, 1, resourceConflict);
				} else {
					// otherwise, they just must be less than 1
					expr = new GRBLinExpr();
					expr.addTerm(1.0, firstBeforeSecond); 
					expr.addTerm(1.0, secondBeforeFirst);
					model.addConstr(expr, GRB.LESS_EQUAL, 1, overlapName);
					
					// if an two actions are assigned to the same agent, then the precedence bits must be 1, otherwise they can be 0
					for (AgentAssignment assignment : assignments.getAssignments()) {
						
						String firstActionStr = firstNode.getAction(assignment.getId()).toString();
						GRBVar firstAction = modelVariables.get(firstActionStr);
						String secondActionStr = secondNode.getAction(assignment.getId()).toString();
						GRBVar secondAction = modelVariables.get(secondActionStr);
						
						
						expr = new GRBLinExpr();
						expr.addTerm(1.0, firstBeforeSecond); 
						expr.addTerm(1.0, secondBeforeFirst); 
						expr.addTerm(-1, firstAction);
						expr.addTerm(-1, secondAction);
						model.addConstr(expr, GRB.GREATER_EQUAL, -1, firstBeforeSecondStr + "_" + assignment.getId());
						
						
					}
				}
				
				
			}
		}
		
	}

	
	private static boolean printResults(GRBModel model,
			Map<String, GRBVar> modelVariables, boolean useStart, boolean printOnError)
			throws GRBException {
		GRBLinExpr expr;
		DoubleAttr variableValue = (useStart) ? GRB.DoubleAttr.Start : GRB.DoubleAttr.X;
		boolean hadErrors = false;
		List<String> constraintLines = new ArrayList<String>();
		for (GRBConstr constraint : model.getConstrs()) {
			
			expr = model.getRow(constraint);
			double lhs = expr.getConstant();
			StringBuffer varStringBuf = new StringBuffer(), valueStringBuf = new StringBuffer();
			varStringBuf.append(lhs);
			valueStringBuf.append(lhs);
			for (int i = 0 ; i < expr.size(); i++) {
				double coeff = expr.getCoeff(i);
				double value = expr.getVar(i).get(variableValue);
				lhs += coeff * value;
				varStringBuf.append(" + ").append(expr.getCoeff(i)).append(" * ").append(expr.getVar(i).get(GRB.StringAttr.VarName));
				valueStringBuf.append(" + ").append(coeff).append(" * ").append(value);
			}
			double rhs = constraint.get(GRB.DoubleAttr.RHS);
			char sense = constraint.get(GRB.CharAttr.Sense);
			varStringBuf.append(" " + sense).append(" " + rhs);
			valueStringBuf.append(" " + sense).append(" " + rhs);
			boolean printThisOne = !printOnError;
			boolean error = false;
			if (sense == '=' && (Math.abs(lhs - rhs) > 0.001) ||
					(sense == '>' && (lhs < rhs - 0.001)) ||
					(sense == '<' &&  (lhs > rhs + 0.001))) {
				printThisOne = true;
				hadErrors = true;
				error = true;
			} 
			if (printThisOne) {
				constraintLines.add(varStringBuf.toString() + "\n" +
						valueStringBuf.toString() + "\n" + 
						constraint.get(GRB.StringAttr.ConstrName) + ": " + lhs + " " + sense + " " + rhs + "\n" + 
						((error) ? "Violates Constraints\n" : "\n"));
			}
			
		}
		
		Collections.sort(constraintLines);
		List<String> varLines = new ArrayList<String>();
		if (!printOnError || hadErrors) {
			for (GRBVar variable : modelVariables.values()) {
				varLines.add(variable.get(GRB.StringAttr.VarName)
						+ " " +variable.get(variableValue));
			}
			
		}
		Collections.sort(varLines);
		if (varLines.size() > 0) {
			varLines.add("\n");
		}
		List<List<String>> lines = Arrays.asList(constraintLines, varLines);
		for (List<String> list : lines) {
			for (String line : list) {
				System.out.println(line);
			}
		}
		
		
		return hadErrors;
	}

	
	private List<List<Double>> extractAssignments(Workflow workflow,
			Assignments assignments, Map<String, GRBVar> modelVariables) throws GRBException {
		List<List<Double>> startTimes = new ArrayList<List<Double>>();
		for (AgentAssignment assignment : assignments.getAssignments()) {
			startTimes.add(new ArrayList<Double>());
		}
		List<AgentAssignment> assignmentList = assignments.getAssignments();
		for (Subtask node : workflow) {
			String nodeStartStr = node.toString() + "_START";
			GRBVar nodeStart = modelVariables.get(nodeStartStr);

			
			for (int i = 0; i < assignmentList.size(); i++) {
				AgentAssignment assignment = assignmentList.get(i);
				List<Double> times = startTimes.get(i);
				String agent = assignment.getId();
				String actionStr = node.getAction(agent).toString();
				GRBVar actionVar = modelVariables.get(actionStr);
				double value = actionVar.get(GRB.DoubleAttr.X);
				if (value > 0.5) {
					double startTime = nodeStart.get(GRB.DoubleAttr.X);
					int pos = Collections.binarySearch(times, startTime);
					if (pos < 0) {
						pos = -(pos + 1);
					}
					times.add(pos, startTime);
					assignment.add(pos, node);
					
				}
			}
		}
		return startTimes;
	}
	
	public boolean isUsingActualValues() {
		return this.useActualValues;
	}

}
