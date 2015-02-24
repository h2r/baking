package edu.brown.cs.h2r.baking.Scheduling.Multitiered;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class SubtaskAvailability {
	private final double time;
	private final Subtask subtask;
	public SubtaskAvailability(Subtask subtask, double time) {
		this.time = time;
		this.subtask = subtask;
	}
	
	public Subtask getSubtask() {
		return this.subtask;
	}
	
	public double getTime() {
		return this.time;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(this.time, this.subtask);
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof SubtaskAvailability)) {
			return false;
		}
		SubtaskAvailability sa = (SubtaskAvailability)other;
		if (this.time != sa.time){ 
			return false;
		}
		if (!this.subtask.equals(sa.subtask)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		return this.subtask.toString() + ": " + this.time;
	}
	
	public static class SAComparator implements Comparator<SubtaskAvailability> {
		@Override
		public int compare(SubtaskAvailability o1, SubtaskAvailability o2) {
			return -Double.compare(o1.time, o2.time);
		}
		
	}

}
