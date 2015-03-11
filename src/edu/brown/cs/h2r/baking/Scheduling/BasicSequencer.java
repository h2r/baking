package edu.brown.cs.h2r.baking.Scheduling;

import java.util.HashMap;
import java.util.Map;

import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.Scheduling.Assignment.ActionTime;
import edu.brown.cs.h2r.baking.Scheduling.Assignment.AssignmentIterator;

public class BasicSequencer extends Sequencer {
	public BasicSequencer(boolean useActualValues) {
		super(useActualValues);
	}

	public Assignments finishSequence(Assignments assignments, Assignments sequenced, ActionTimeGenerator timeGenerator) {
		Map<String, AssignmentIterator> iterators = new HashMap<String, AssignmentIterator>();
		for (Assignment assignment : assignments.getAssignmentMap().values()) {
			iterators.put(assignment.getId(), (AssignmentIterator)assignment.iterator());
		}
		
		int numAdded = -1;
		while (numAdded != 0) {
			numAdded = 0;
			for (Map.Entry<String, AssignmentIterator> entry : iterators.entrySet()) {
				
				String agent = entry.getKey();
				AssignmentIterator it = entry.getValue();
				while (it.hasNext()) {
					ActionTime actionTime = it.next();
					if (!sequenced.add(actionTime.getNode(), agent)) {
						it.previous();
						break;
					} else {
						numAdded++;
					}
				}
			}
		}
		return sequenced;
	}
	
	@Override
	public String getDescription() {
		return this.getClass().getSimpleName();
	}
	
	public boolean add(Assignments sequenced, ActionTimeGenerator timeGenerator, Workflow.Node node, String agent) {
		Assignment assignment = sequenced.getAssignment(agent);
		
		if (assignment.contains(node)) {
			return true;
		}
		
		double assignmentTime = assignment.time();
		
		GroundedAction action = node.getAction(agent);
		double actionDuration = timeGenerator.get(action, this.useActualValues);
		
		double time = sequenced.getTimeNodeIsAvailable(node, assignment, assignmentTime, actionDuration);
		State currentState = sequenced.getStateAtTime(time);
		if (!action.action.applicableInState(currentState, action.params)) {
			return false;
		}
		
		if (time > assignmentTime) {
			assignment.waitUntil(time);
		} else if (time < 0.0) {
			return false;
		}
		
		
		assignment.add(node);
		
		return true;
	}
}
