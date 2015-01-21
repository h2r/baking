package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.Scheduling.Assignment.ActionTime;
import edu.brown.cs.h2r.baking.Scheduling.Assignment.AssignmentIterator;

public class SchedulingHelper {

	public static List<Assignment> copy(List<Assignment> other) {
		List<Assignment> copyOf = new ArrayList<Assignment>(other.size());
		for (Assignment workflow : other) {
			copyOf.add(new Assignment(workflow));
		}
		return copyOf;
	}
	
	public static Map<String, Assignment> copyMap(Map<String, Assignment> other) {
		Map<String, Assignment> copyOf = new HashMap<String, Assignment>();
		for (Map.Entry<String, Assignment> entry : other.entrySet()) {
			copyOf.put(entry.getKey(), new Assignment(entry.getValue()));
		}
		return copyOf;
	}
	
	public static double computeSequenceTime( State state,
			List<AbstractGroundedAction> actionSequence, ActionTimeGenerator timeGenerator) {
		Workflow workflow = Workflow.buildWorkflow(state, actionSequence);
		Map<String, Assignment> sortedActions = new HashMap<String, Assignment>();
		
		for (Workflow.Node node : workflow) {
			GroundedAction ga = node.getAction();
			String agent = ga.params[0];
			Assignment assignment = sortedActions.get(agent);
			if (assignment == null) {
				assignment = new Assignment(agent, timeGenerator);
				sortedActions.put(agent, assignment);
			}
			assignment.add(node);
		}
		BufferedAssignments buffered = new BufferedAssignments(sortedActions.values());
		
		return buffered.time();
	}
}
