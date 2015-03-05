package edu.brown.cs.h2r.baking.Scheduling.Multitiered;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.Scheduling.ActionTimeGenerator;
import gurobi.GRB;
import gurobi.GRB.DoubleAttr;
import gurobi.GRBConstr;
import gurobi.GRBEnv;
import gurobi.GRBException;
import gurobi.GRBLinExpr;
import gurobi.GRBModel;
import gurobi.GRBVar;

public class TercioScheduler implements Scheduler {
	private static double M = 1000.0;
	private static long MAX_SOLUTIONS = 10;
	private final boolean useActualValues;
	private final Sequencer sequencer;
	
	public TercioScheduler(boolean useActualValues) {
		this.useActualValues = useActualValues;
		this.sequencer = new TercioSequencer(useActualValues);
	}

	public String getDescription() {
		return this.getClass().getSimpleName() + " - " + this.sequencer.getDescription();
	}

	@Override
	public Assignments schedule(Workflow workflow, List<String> agents,
			ActionTimeGenerator timeGenerator) {
		Assignments assignments = new Assignments(agents, timeGenerator, this.useActualValues);
		return this.assignTasks(workflow, assignments, timeGenerator);
	}

	@Override
	public Assignments finishSchedule(Workflow workflow, Assignments assignments, ActionTimeGenerator timeGenerator) {
		return this.assignTasks(workflow, assignments, timeGenerator);
	}

	public Assignments assignTasks(Workflow workflow, Assignments assignments, ActionTimeGenerator timeGenerator) {
		double cutoff = 0.0;
		Assignments bestAssignments = null;
		
		try {
			GRBEnv    env   = new GRBEnv("mip1.log");
			env.set(GRB.IntParam.OutputFlag, 0); 
			GRBModel  model = new GRBModel(env);
			
			// Create variables

			Map<String, GRBVar> modelVariables = new HashMap<String, GRBVar>();
			GRBVar v = setupModelVariables(workflow, assignments, model,
					modelVariables);

			model.update();

			// Set objective: maximize x + y + 2 z

			GRBLinExpr expr = new GRBLinExpr();
			expr.addTerm(1.0, v);
			model.setObjective(expr, GRB.MINIMIZE);

			TercioScheduler.addConstraints(workflow, assignments, timeGenerator, model,
					modelVariables, v, this.useActualValues);

			// Add constraint: x + y >= 1

			// Optimize model
			model.update();
			
			model.presolve();
			
			double makespanTime = Double.MAX_VALUE;
			long solutionCount = 0;
			while (solutionCount++ < MAX_SOLUTIONS && makespanTime > cutoff) {
				model.optimize();
				int numSolutions = model.get(GRB.IntAttr.SolCount);
				if ( numSolutions == 0) {
					break;
				} 
				int status = model.get(GRB.IntAttr.Status);
				if (status != 2) {
					System.err.println("Non-optimal solution found");
				}
				Assignments currentAssignments = assignments.copy();
				
				this.extractAssignments(workflow, currentAssignments, modelVariables);
				
				Assignments sequenced = this.sequencer.sequence(currentAssignments, timeGenerator, workflow);
				if (sequenced != null) {
					double time = sequenced.time();
					if (time < makespanTime) {
						//System.out.println(solutionCount + ", " + time);
						bestAssignments = sequenced;
						makespanTime = time;
					}
				} else {
					//System.err.println("Sequencing failed " + currentAssignments.getAllSubtasks().size());
				}
				if (!TercioScheduler.addConstraint(currentAssignments, workflow, model, modelVariables, solutionCount)) {
					break;
				}
				model.update();
			}
			
			
			
			//this.printResults(model, modelVariables, startTimes);

			model.dispose();
			env.dispose();

		} catch (GRBException e) {
			System.out.println("Error code: " + e.getErrorCode() + ". " +
					e.getMessage());
		}
		
		
		return bestAssignments;
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
			/*
			for (Workflow.Node secondNode : workflow) {
				if (!node.equals(secondNode)) {
					String firstNodeStr = node.toString();
					String secondNodeStr = secondNode.toString();
					
					String firstBeforeSecondStr = firstNodeStr + "->" + secondNodeStr;
					GRBVar firstBeforeSecond = model.addVar(0.0, 1.0, 1.0, GRB.BINARY, firstBeforeSecondStr);
					modelVariables.put(firstBeforeSecondStr, firstBeforeSecond);
				}
			}*/
		}
		return v;
	}
	
	private static boolean printResults(GRBModel model,
			Map<String, GRBVar> modelVariables, boolean useStart, boolean printOnError)
			throws GRBException {
		GRBLinExpr expr;
		DoubleAttr variableValue = (useStart) ? GRB.DoubleAttr.Start : GRB.DoubleAttr.X;
		if (!printOnError) {
			for (GRBVar variable : modelVariables.values()) {
				System.out.println(variable.get(GRB.StringAttr.VarName)
						+ " " +variable.get(variableValue));
			}
		}
		boolean hadErrors = false;
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
				System.out.println(varStringBuf.toString());
				System.out.println(valueStringBuf.toString());
				System.out.println(constraint.get(GRB.StringAttr.ConstrName) + ": " + lhs + " " + sense + " " + rhs);
				if (error) System.out.println("Violates constraints");
				
				System.out.println();
			}
			
		}
		return hadErrors;
	}
	
	private static boolean addConstraint(Assignments previousAssignments, Workflow workflow, GRBModel model, 
			Map<String, GRBVar> modelVariables, long index) throws GRBException {
		
		GRBLinExpr expr = new GRBLinExpr();
		double sum = 0;
		for (Subtask node : workflow) {
			for (AgentAssignment assignment : previousAssignments.getAssignments()) {
				GroundedAction action = node.getAction(assignment.getId());
				String actionStr = action.toString();
				GRBVar actionVar = modelVariables.get(actionStr);
				if (assignment.contains(node)) {
					expr.addTerm(-1.0, actionVar);
					sum++;
				} else {
					expr.addTerm(1.0, actionVar);
				}
			}
		}
		GRBConstr constraint = model.addConstr(expr, GRB.GREATER_EQUAL, -sum + 0.5, "previous_" + index);
		return (constraint != null);
	}

	private static void addConstraints(Workflow workflow,
			Assignments assignments, ActionTimeGenerator timeGenerator,
			GRBModel model, Map<String, GRBVar> modelVariables, GRBVar v, boolean useActualValues)
					throws GRBException {
		GRBLinExpr expr;
		
		// v max of the sums of all agents actions
		for (AgentAssignment assignment : assignments.getAssignments()) {
			String agent = assignment.getId();
			
			expr = new GRBLinExpr();
			expr.addTerm(-1.0, v);
			for (Subtask node : workflow) {
				GroundedAction action = node.getAction(agent);
				String actionStr = action.toString();
				GRBVar actionVar = modelVariables.get(actionStr);
				double time = timeGenerator.get(action, useActualValues);
				// v is the maximum of all action ending times
				expr.addTerm(time, actionVar);
			}
			model.addConstr(expr, GRB.LESS_EQUAL, 0.0, agent + "_v");
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

					String firstBeforeSecond = firstNodeStr + "_depends_" + secondNodeStr;
					expr = new GRBLinExpr();
					expr.addTerm(1.0, secondNodeStart); expr.addTerm(-1.0, firstNodeEnd);
					model.addConstr(expr, GRB.GREATER_EQUAL, 0.0, firstBeforeSecond);
				}
			}
		}
		/*
		// end time - start time must equal the expected time of the action for the agent (if its assigned), otherwise greater than 0.0
		for (Assignment assignment : assignments) {
			String agent = assignment.getId();

			for (Workflow.Node node : workflow) {
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
		
		// If an agents is assigned two actions, they cannot overlap
		for (Workflow.Node firstNode : workflow) {
			String firstNodeStr = firstNode.toString();
			String firstNodeEndStr = firstNodeStr + "_END";
			GRBVar firstNodeEnd = modelVariables.get(firstNodeEndStr);

			for (Workflow.Node secondNode : workflow) {
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
		
		// If an agent is assigned two actions, the precedent variable must be set
		for (Workflow.Node firstNode : workflow) {
			String firstNodeStr = firstNode.toString();
			String firstNodeEndStr = firstNodeStr + "_END";
			GRBVar firstNodeEnd = modelVariables.get(firstNodeEndStr);
			
			
			
			for (Workflow.Node secondNode : workflow) {
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
				expr = new GRBLinExpr();
				expr.addTerm(1.0, firstBeforeSecond); 
				expr.addTerm(1.0, secondBeforeFirst);
				model.addConstr(expr, GRB.LESS_EQUAL, 1, overlapName);
				
				
				for (Assignment assignment : assignments) {
					
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
		}*/
		
	}

	private void extractAssignments(Workflow workflow,
			Assignments assignments, Map<String, GRBVar> modelVariables) throws GRBException {
		Collection<String> agents = assignments.getAgents();
		for (Subtask node : workflow) {
			for (String agent : agents) {
				String actionStr = node.getAction(agent).toString();
				GRBVar actionVar = modelVariables.get(actionStr);
				double value = actionVar.get(GRB.DoubleAttr.X);
				if (value > 0.5) {
					assignments.add(node, agent);					
				}
			}
		}
	}

	
	@Override
	public boolean isUsingActualValues() {
		return this.useActualValues;
	}

}