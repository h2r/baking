package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

import burlap.datastructures.HashIndexedHeap;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.GroundedAction;
import edu.brown.cs.h2r.baking.Scheduling.Assignment.ActionTime;
import edu.brown.cs.h2r.baking.Scheduling.Assignment.AssignmentIterator;
import edu.brown.cs.h2r.baking.actions.BakingAction;
import edu.brown.cs.h2r.baking.actions.BakingActionResult;

public class Assignments implements Iterable<Assignment> {
	private final Map<String, Assignment> adjustedAssignments;
	private final Map<String, State> states;
	private final State startingState;
	private double time;
	private double earliestTime;
	private final ActionTimeGenerator timeGenerator;
	private Set<Workflow.Node> completedAtEarliest; 
	private final Set<Workflow.Node> subtasks; 
	private final boolean useActualValues;
	private final boolean rearrangeOrder;
	
	public Assignments(ActionTimeGenerator timeGenerator, Collection<String> agents, State startState, boolean useActualValues, boolean rearrangeOrder) {
		this.adjustedAssignments = new HashMap<String, Assignment>();
		this.states = new HashMap<String, State>();
		this.startingState = startState;
		this.subtasks = new HashSet<Workflow.Node>();
		for (String agent : agents) {
			this.adjustedAssignments.put(agent, new Assignment(agent, timeGenerator, useActualValues));
		}
		this.completedAtEarliest = new HashSet<Workflow.Node>();
		this.earliestTime = 0.0;
		this.timeGenerator = timeGenerator;
		this.useActualValues = useActualValues;
		this.rearrangeOrder = rearrangeOrder;
	}
	
	public Assignments(Assignments other) {
		this.rearrangeOrder = other.rearrangeOrder;
		this.startingState = other.startingState;
		this.subtasks = new HashSet<Workflow.Node>(other.subtasks);
		this.adjustedAssignments = SchedulingHelper.copyMap(other.adjustedAssignments);
		this.states = new HashMap<String, State>(other.states);
		this.time = other.time;
		this.earliestTime = other.earliestTime;
		this.completedAtEarliest = new HashSet<Workflow.Node>(other.completedAtEarliest);
		this.timeGenerator = other.timeGenerator;
		this.useActualValues = other.useActualValues;
	}
	
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		
		if (!(other instanceof Assignments)) {
			return false;
		}
		
		Assignments bOther = (Assignments)other;
		if (this.time != bOther.time){ 
			return false;
		}
		if (!this.adjustedAssignments.equals(bOther.adjustedAssignments)) { 
			return false;
		}
		return true;
	}
	
	
	@Override
	public int hashCode() {
		return this.adjustedAssignments.hashCode();
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(this.time).append(", ");
		builder.append(adjustedAssignments.toString());
		
		return builder.toString();
	}
	
	public void clear() {
		this.adjustedAssignments.clear();
		this.time = 0.0;
		this.earliestTime = 0.0;
		this.completedAtEarliest.clear();
	}
	
	public Iterator<Assignment> iterator() {
		return this.adjustedAssignments.values().iterator();
	}
	
	public Assignment getAssignment(String agent) {
		return this.adjustedAssignments.get(agent);
	}
	public String getFullString() {
		StringBuilder builder = new StringBuilder();
		for (Map.Entry<String, Assignment> entry : this.adjustedAssignments.entrySet()) {
			String agent = entry.getKey();
			builder.append(agent).append("\n");
			
			Assignment assignment = entry.getValue();
			
			double sum = 0.0;
			for (ActionTime actionTime : assignment) {
				Workflow.Node node = actionTime.getNode();
				String nodeStr = (node == null) ? "wait" : node.getAction().toString();
				
				String previous = String.format("%4f", sum);
				sum += actionTime.getTime();
				String next = String.format("%4f", sum);
				builder.append("\t").append(nodeStr).append(" - ").append(previous).append(", ").append(next).append("\n");
			}
		}
		
		return builder.toString();
	}
	
	public String visualString() {
		StringBuilder builder = new StringBuilder();
		for (Assignment assignedWorkflow : this.adjustedAssignments.values()) {
			for (ActionTime time : assignedWorkflow) {
				int duration = time.getTime().intValue();
				String label = (time.getNode() == null ) ? "." : time.getNode().toString();
				int length = duration * 3;
				length = Math.max(1, length);
				if (length > 0) {
					label = String.format("%" + length + "s", label);
					builder.append(label.replace(' ', '.'));
				}
				
			}
			builder.append("\n");
		}
		return builder.toString();
	}
	
	public void sequenceTasksWithTercio(Collection<Assignment> assignments) {
		HashIndexedHeap<TercioNode> priorityQueue = new HashIndexedHeap<TercioNode>(new TercioNode.TercioComparator());
		
		double currentTime = 0.0;
		Set<Workflow.Node> visited = new HashSet<Workflow.Node>();
		Set<Workflow.Node> assigned = new HashSet<Workflow.Node>(); 
		boolean keepGoing = true;
		while (keepGoing) {
			for (Assignment assignment : this.adjustedAssignments.values()) {
				assignment.waitUntil(currentTime);
			}
			keepGoing = false;
			priorityQueue = new HashIndexedHeap<TercioNode>(new TercioNode.TercioComparator());
			Map<String, List<Workflow.Node>> availableNodes = new HashMap<String, List<Workflow.Node>>();
			for (Assignment assignment : assignments) {
				String agent = assignment.getId();
				List<Workflow.Node> nodes = new ArrayList<Workflow.Node>();
				for (Workflow.Node node : assignment.nodes(visited)) {
					if (!assigned.contains(node)) {
						nodes.add(node);
					}
				}
				availableNodes.put(agent, nodes);
			}
			
			for (Map.Entry<String, List<Workflow.Node>> entry : availableNodes.entrySet()) {
				String agent = entry.getKey();
				List<Workflow.Node> nodes = entry.getValue(); 
				for (Workflow.Node node : nodes) {
					
					double hA = TercioNode.computeA(nodes);
					double hR = TercioNode.computeR(assignments, node.getResources());
					double hP = TercioNode.computeP(agent, node, assignments, visited);
					double hD = 0.0;
					TercioNode tNode = new TercioNode(node, agent, hA, hR, hP, hD);
					priorityQueue.insert(tNode);
				}
			}
			
			for (TercioNode tNode : priorityQueue) {
				Workflow.Node node = tNode.getNode();
				String agent = tNode.getAgent();
				if (this.add(node, agent)) {
					assigned.add(node);
				}
			}
			
			double nextTime = Double.MAX_VALUE;
			for (Assignment assignment : this.adjustedAssignments.values()) {
				Double time = assignment.nextTime(currentTime);
				if (time != null && time > currentTime && time < nextTime) {
					nextTime = time;
				}
			}
			if (nextTime == Double.MAX_VALUE) {
				break;
			}
			keepGoing = (currentTime != nextTime);
			
			currentTime = nextTime;
			for (Assignment assignment : this.adjustedAssignments.values()) {
				keepGoing |= visited.addAll(assignment.nodes(currentTime));
			}
		}
		while (this.shrinkAssignments()) {
			
		}
	}
	
	public boolean shrinkAssignments() {
		// For convience store assignments into list
		List<Assignment> assignments = new ArrayList<Assignment>(this.adjustedAssignments.values());
		
		// Do some pre-trimming
		for (Assignment assignment : assignments) {
			assignment.trim();
		}
		
		
		boolean keepGoing = true;
		double currentTime = -1.0;
		boolean modified = false;
		while(keepGoing) {
			// List to keep track of assignments that are waiting at the current time
			List<Assignment> assignmentsWaiting = new ArrayList<Assignment>();
			double timeFirstWait = Double.MAX_VALUE;
			for (Assignment assignment : assignments) {
				for (int j = 0; j < assignment.size(); j++) {
					Workflow.Node node = assignment.nodes().get(j);
					double endTime = assignment.completionTimes().get(j);
					double startTime = endTime - assignment.times().get(j);
					
					// if the first node after the current time is a waiting node, and it's earlier than the previously found one
					// then that's the new timeFirstWait. If there are more than one, add them to the list
					if (node == null && startTime > currentTime && startTime <= timeFirstWait) {
						if (startTime < timeFirstWait) {
							timeFirstWait = startTime;
							assignmentsWaiting.clear();
						}						
						assignmentsWaiting.add(assignment);
					} else if (startTime > timeFirstWait){ // if we've gone past a previously found timeFirstWait then break
						break;
					}
				}
			}
			
			if (timeFirstWait == Double.MAX_VALUE) {
				return modified;
			}
			
			// For each assignment that's currently waiting, we need to see if we can bump up actions
			for (Assignment waiting : assignmentsWaiting) {
				Set<Workflow.Node> slice = new HashSet<Workflow.Node>();
				Set<Workflow.Node> visited = new HashSet<Workflow.Node>();
				// get the position of the node after the first waiting (2 because 1 for completionTimes, and 1 for the nextnode
				int position = waiting.position(timeFirstWait);
				if (position < waiting.completionTimes().get(position)) {
					position++;
				} else {
					position += 2;
				}
				while (position < waiting.size() && waiting.nodes().get(position) == null) {
					position++;
				}
				if (position >= waiting.size()) {
					continue;
				}
				double newEndTime = waiting.time();
				// find the end time if we were to bump of this action
				if (position < waiting.size() - 1) {
					newEndTime = timeFirstWait + waiting.times().get(position);
				} else {
					continue;
				}
				
				// get the comppleted nodes at the timefirstwait, as well as the slice of nodes occuring during this action
				for (Assignment assignment : assignments) {
					slice.addAll(assignment.slice(timeFirstWait, newEndTime));
					List<Workflow.Node> list = assignment.nodes(0.0, timeFirstWait);
					if (list != null) {
						visited.addAll(list);
					}
				} 
				
				// check of this node is available at the new time, and it doesn't conflict with any others
				// if so, swap the wait node and node
				Workflow.Node nodeToSwitch = waiting.nodes().get(position);
				if (nodeToSwitch.isAvailable(visited) && !nodeToSwitch.resourceConflicts(slice)) {
					waiting.swap(position - 1, position);
					modified = true;
				}
				waiting.trim();
			}
			
			currentTime = timeFirstWait;
		}
		return modified;
	}
	
	public void sequenceTasksWithReorder(Collection<Assignment> assignments) {
		double currentTime = 0.0;
		Set<Workflow.Node> visited = new HashSet<Workflow.Node>();
		boolean keepGoing = true;
		while (keepGoing) {
			keepGoing = false;
			for (Assignment assignment : assignments) {
				Set<Workflow.Node> futureVisited = new HashSet<Workflow.Node>(visited); 
				String agent = assignment.getId();
				for (Workflow.Node node : assignment.nodes(futureVisited)) {
					if(this.add(node, agent)) {
						futureVisited.add(node);
					}
				}
			}
			double nextTime = Double.MAX_VALUE;
			for (Assignment assignment : this.adjustedAssignments.values()) {
				Double time = assignment.nextTime(currentTime);
				if (time != null && time > currentTime && time < nextTime) {
					nextTime = time;
				}
			}
			if (nextTime == Double.MAX_VALUE) {
				break;
			}
			keepGoing = (currentTime != nextTime);
			
			currentTime = nextTime;
			for (Assignment assignment : this.adjustedAssignments.values()) {
				keepGoing |= visited.addAll(assignment.nodes(currentTime));
			}
		}
	}
	
	public void sequenceTasksWithoutReorder(Collection<Assignment> assignments) {
		Map<String, AssignmentIterator> iterators = new HashMap<String, AssignmentIterator>();
		for (Assignment assignment : assignments) {
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
					if (!this.add(actionTime.getNode(), agent)) {
						it.previous();
						break;
					} else {
						numAdded++;
					}
				}
			}
		}
		
	}
	
	public void buildAdjustedAssignments(Collection<Assignment> assignments, boolean correct) {
		int expectedSize = 0;
		for (Assignment assignment : assignments) {
			String agent = assignment.getId();
			expectedSize += assignment.size();
			this.adjustedAssignments.put(agent, new Assignment(agent, assignment.getTimeGenerator(), assignment.getUseActualValues()));
		}
		
		if (correct){
			this.sequenceTasksWithTercio(assignments);
		} else {
			this.sequenceTasksWithoutReorder(assignments);
		}
		
		this.time = 0.0;
		for (Assignment assignment : this.adjustedAssignments.values()) {
			this.time = Math.max(this.time, assignment.time());
		}
		
		if (this.realSize() != expectedSize) {
			this.time = -1.0;
			//throw new RuntimeException("Buffered assignments could not be completed");
		}
	}
	
	public Assignments copyAndFinish(Collection<Assignment> assignments) {
		int expectedSize = 0;
		for (Assignment assignment : assignments) {
			expectedSize += assignment.realSize();
		}
		Assignments copy = new Assignments(this);
		if (this.rearrangeOrder){
			copy.sequenceTasksWithReorder(assignments);
		} else {
			copy.sequenceTasksWithoutReorder(assignments);
		}
		
		if (copy.realSize() != expectedSize) {
			this.time = -1.0;
			//throw new RuntimeException("Buffered assignments could not be completed");
		}
		return copy;
	}
	
	public Assignments copy() {
		return new Assignments(this);
	}
	
	public GroundedAction getFirstAction(String agent) {
		Assignment assignment = this.adjustedAssignments.get(agent);
		if (assignment == null) {
			return null;
		}
		Workflow.Node node = assignment.first();
		if (node == null) {
			return null;
		}
		return node.getAction(agent);
	}
	
	public boolean add(Workflow.Node node, String agent) {
		Assignment assignment = this.adjustedAssignments.get(agent);
		if (assignment == null) {
			return false;
		}
		if (this.subtasks.contains(node)) {
			return true;
		}
		double assignmentTime = assignment.time();
		
		GroundedAction action = node.getAction(agent);
		double actionDuration = this.timeGenerator.get(action, this.useActualValues);
		
		double time = this.getTimeNodeIsAvailable(node, assignment, assignmentTime, actionDuration);
		State currentState = this.getStateAtTime(time);
			if (currentState != null) {
			BakingAction bakingAction = (BakingAction)action.action;
			if (currentState != null) {
				BakingActionResult result = bakingAction.checkActionIsApplicableInState(currentState, action.params);
				if (!result.getIsSuccess()) {
					//System.out.println("This action cannot be scheduled");
					//System.out.println(result.getWhyFailed());
					return false;
				}
			}
		}
		
		if (time > assignmentTime) {
			assignment.waitUntil(time);
		} else if (time < 0.0) {
			return false;
		}
		
		
		assignment.add(node);
		this.subtasks.add(node);
		this.time = Math.max(this.time, assignment.time());
		return true;
	}
	
	public void waitAgentUntil(String agent, Double endTime) {
		Assignment assignment = this.adjustedAssignments.get(agent);
		assignment.waitUntil(endTime);
		this.time = Math.max(this.time, assignment.time());
	}
	
	public State getStateAtTime(double time) {
		int initialSize = Math.max(10, this.subtasks().size());
		PriorityQueue<ActionTime> queue = new PriorityQueue<ActionTime>(initialSize, Assignment.ActionTime.comparator());
		for (Assignment assignment : this.adjustedAssignments.values()) {
			List<ActionTime> actionTimes = assignment.nodesBeforeTime(time);
			queue.addAll(actionTimes);
		}
		
		State state = this.startingState;
		if (state == null) {
			return null;
		}
		while (queue.peek() != null) {
			ActionTime next = queue.poll();
			if (next.getTime() > time) {
				return state;
			}
			Workflow.Node node = next.getNode();
			if (node != null) {
				GroundedAction groundedAction = node.getAction();
				state = groundedAction.executeIn(state);
			}
		}
		return state;
	}
	
	private void updateEarliest() {
		this.earliestTime = Double.MAX_VALUE;
		for (Assignment workflow : this.adjustedAssignments.values()) {
			this.earliestTime = Math.min(this.earliestTime, workflow.time());
		}
		
		List<Workflow.Node> nodes = null; 
		for (Assignment workflow : this.adjustedAssignments.values()) {
			nodes = workflow.nodes(0.0, this.earliestTime);
			if (nodes != null) {
				this.completedAtEarliest.addAll(nodes);
			}
		}
	}
	
	double getTimeNodeIsAvailable(Workflow.Node node, Assignment assignment, double seed, double actionDuration) {
		Set<Workflow.Node> completed = new HashSet<Workflow.Node>(this.completedAtEarliest);
		double previous = this.earliestTime;
		double currentTime = assignment.time();
		List<Workflow.Node> nodes = null; 
		for (Assignment workflow : this.adjustedAssignments.values()) {
			nodes = workflow.nodes(previous, currentTime);
			if (nodes != null) {
				completed.addAll(nodes);
			}
		}
		
		List<Workflow.Node> slice = this.getNodesAtTime(currentTime, currentTime + actionDuration );
		
		nodes = null; 
		while (!node.isAvailable(completed) || node.resourceConflicts(slice)) {
			
			double nextTime = Double.MAX_VALUE;
			for (Assignment workflow : this.adjustedAssignments.values()) {
				Double time = workflow.nextTime(currentTime);
				if (time != null && time > currentTime && time < nextTime) {
					nextTime = time;
				}
			}
			if (nextTime == Double.MAX_VALUE) {
				return -1.0;
			}
			previous = currentTime;
			currentTime = nextTime;
			
			for (Assignment workflow : this.adjustedAssignments.values()) {
				nodes = workflow.nodes(previous, currentTime);
				if (nodes != null) {
					completed.addAll(nodes);
				}
			}
			
			slice = this.getNodesAtTime(currentTime, currentTime + actionDuration);
			
		}
		
		return currentTime;
		
	}
	
	private List<Workflow.Node> getNodesAtTime(double startTime, double endTime) {
		List<Workflow.Node> nodes = new ArrayList<Workflow.Node>();
		
		for (Assignment assignment : this.adjustedAssignments.values()) {
			List<Workflow.Node> nodesInDuration = assignment.slice(startTime, endTime);
			if (nodesInDuration != null) nodes.addAll(nodesInDuration);
		}
		
		return nodes;
	}
	
	public Double getTimeAssigningNodeToAgent(Workflow.Node node, double actionTime, String agent) {
		Assignment assignment = this.adjustedAssignments.get(agent);
		if (assignment == null) {
			return null;
		}
		double assignmentTime = assignment.time();
		double time = this.getTimeNodeIsAvailable(node, assignment, assignmentTime, actionTime);
		
		time = Math.max(time, assignmentTime);
		return time + actionTime;
	}
	
	public double time() {
		return this.time;
	}
	
	public int size() {
		return this.adjustedAssignments.size();
	}
	
	public Collection<Workflow.Node> subtasks() {
		return new HashSet<Workflow.Node>(this.subtasks);
	}
	
	public int realSize() {
		int size = 0;
		for (Assignment assignment : this.adjustedAssignments.values()) {
			size += assignment.realSize();
		}
		return size;
	}

	public Map<String, Assignment> getAssignmentMap() {
		return SchedulingHelper.copyMap(this.adjustedAssignments);
	}

	public Collection<String> agents() {
		return this.adjustedAssignments.keySet();
	}

	public State getStartState() {
		return this.startingState;
	}

	

	
}
