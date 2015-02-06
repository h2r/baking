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

	public static List<Assignment> copy(Collection<Assignment> other) {
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
}
