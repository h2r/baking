package edu.brown.cs.h2r.baking.Scheduling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import burlap.parallel.Parallel;
import burlap.parallel.Parallel.ForEachCallable;
import edu.brown.cs.h2r.baking.Scheduling.Workflow.Node;

public class CombinedScheduler implements Scheduler {
	private final List<Scheduler> schedulers;
	private final boolean useActualValues;
	public CombinedScheduler(Scheduler... schedulers) {
		this.schedulers = Arrays.asList(schedulers);
		boolean usingActual = schedulers[0].isUsingActualValues();
		for (Scheduler scheduler : schedulers) {
			if (usingActual != scheduler.isUsingActualValues()) {
				throw new RuntimeException("Inconsistent agreemente about using acual values");
			}
			usingActual = scheduler.isUsingActualValues();
		}
		this.useActualValues = usingActual;
	}

	@Override
	public List<Assignment> schedule(Workflow workflow, List<String> agents,
			ActionTimeGenerator timeGenerator) {
		List<Assignment> assignments = new ArrayList<Assignment>();
		for (String agent : agents) {
			assignments.add(new Assignment(agent, timeGenerator, this.isUsingActualValues()));
		}
		BufferedAssignments buffered = new BufferedAssignments(assignments);
		Set<Workflow.Node> visited = new HashSet<Workflow.Node>();
		return this.assignActions(workflow, timeGenerator, assignments, buffered, visited);
	}

	@Override
	public List<Assignment> finishSchedule(Workflow workflow,
			ActionTimeGenerator timeGenerator,
			List<Assignment> assignments,
			BufferedAssignments buffered, Set<Node> visitedNodes) {
		return this.assignActions(workflow, timeGenerator, assignments, buffered, visitedNodes);
	}
	
	public List<Assignment> assignActions(Workflow workflow,
			ActionTimeGenerator timeGenerator,
			List<Assignment> assignments,
			BufferedAssignments buffered, Set<Node> visitedNodes) {
		SchedulerCallable callable = new SchedulerCallable(workflow, timeGenerator, assignments, buffered, visitedNodes);
		List<List<Assignment>> results = Parallel.ForEach(this.schedulers, callable);
		
		List<Assignment> best = null;
		double minTime = Double.MAX_VALUE;
		for (List<Assignment> assignment : results) {
			BufferedAssignments buff = new BufferedAssignments(assignment);
			if (buff.time() < minTime) {
				best = assignment;
				minTime = buff.time();
			}
		}
		return best;
	}
	@Override
	public boolean isUsingActualValues() {
		return this.useActualValues;
	}
	
	public class SchedulerCallable extends ForEachCallable<Scheduler, List<Assignment>> {
		private final Workflow workflow;
		private final List<Assignment> assignments;
		private final BufferedAssignments buffered;
		private final Set<Workflow.Node> visited; 
		private final ActionTimeGenerator timeGenerator;
		private final Scheduler scheduler;
		
		public SchedulerCallable(Workflow workflow, ActionTimeGenerator timeGenerator, List<Assignment> assignments, BufferedAssignments buffered, Set<Workflow.Node> visited) {
			this.workflow = workflow;
			this.timeGenerator = timeGenerator;
			this.buffered = buffered;
			this.visited = visited;
			this.assignments = assignments;
			this.scheduler = null;
		}
		
		public SchedulerCallable(SchedulerCallable other, Scheduler scheduler) {
			this.workflow = other.workflow;
			this.timeGenerator = other.timeGenerator;
			this.buffered = other.buffered.copy();
			this.visited = new HashSet<Workflow.Node>(other.visited);
			this.assignments = SchedulingHelper.copy(other.assignments);
			this.scheduler = scheduler;
		}
		
		@Override
		public List<Assignment> call() throws Exception {
			return this.scheduler.finishSchedule(workflow, timeGenerator, assignments, buffered, visited);
		}

		@Override
		public ForEachCallable<Scheduler, List<Assignment>> init(
				Scheduler current) {
			return new SchedulerCallable(this, current);
		}
		
	}

}
