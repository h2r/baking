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

	public Assignments schedule(Workflow workflow, List<String> agents,
			ActionTimeGenerator timeGenerator) {
		Assignments assignments = new Assignments(timeGenerator, agents, workflow.getStartState(), this.useActualValues, false);
		return this.finishSchedule(workflow, assignments, timeGenerator);
	}

	public Assignments finishSchedule(Workflow workflow, Assignments assignments, ActionTimeGenerator timeGenerator) {
		SchedulerCallable callable = new SchedulerCallable(workflow, timeGenerator, assignments);
		List<Assignments> results = Parallel.ForEach(this.schedulers, callable);
		
		Assignments best = null;
		double minTime = Double.MAX_VALUE;
		for (Assignments assignment : results) {
			if (assignments.time() < minTime) {
				best = assignment;
				minTime = assignment.time();
			}
		}
		return best;
	}
	
	@Override
	public boolean isUsingActualValues() {
		return this.useActualValues;
	}
	
	public class SchedulerCallable extends ForEachCallable<Scheduler, Assignments> {
		private final Workflow workflow;
		private final Assignments assignments;
		private final ActionTimeGenerator timeGenerator;
		private final Scheduler scheduler;
		
		public SchedulerCallable(Workflow workflow, ActionTimeGenerator timeGenerator, Assignments assignments) {
			this.workflow = workflow;
			this.timeGenerator = timeGenerator;
			this.assignments = assignments;
			this.scheduler = null;
		}
		
		public SchedulerCallable(SchedulerCallable other, Scheduler scheduler) {
			this.workflow = other.workflow;
			this.timeGenerator = other.timeGenerator;
			this.assignments = other.assignments.copy();
			this.scheduler = scheduler;
		}
		
		@Override
		public Assignments call() throws Exception {
			return this.scheduler.finishSchedule(workflow, assignments, timeGenerator);
		}

		@Override
		public ForEachCallable<Scheduler, Assignments> init(
				Scheduler current) {
			return new SchedulerCallable(this, current);
		}
		
	}

}
