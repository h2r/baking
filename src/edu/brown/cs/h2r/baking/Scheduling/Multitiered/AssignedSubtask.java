package edu.brown.cs.h2r.baking.Scheduling.Multitiered;


public class AssignedSubtask {
	private final Subtask subtask;
	private final Double time;
	public AssignedSubtask(Subtask subtask, Double time) {
		this.subtask = subtask;
		this.time = time;
	}
	
	public Subtask getSubtask() {
		return this.subtask;
	}
	
	public Double getTime() {
		return this.time;
	}
	
	@Override
	public boolean equals(Object other) {
		if (this == other) {
			return true;
		}
		if (!(other instanceof AssignedSubtask)) {
			return false;
		}
		AssignedSubtask otherAssignedSubtask = (AssignedSubtask)other;
		if (!this.time.equals(otherAssignedSubtask.time)){ 
			return false;
		}
		
		if ((this.subtask == null) != (otherAssignedSubtask.subtask == null)) {
			return false;
		}
		
		if (this.subtask == null) {
			return true;
		}
		
		if (!this.subtask.equals(otherAssignedSubtask.subtask)) {
			return false;
		}
		return true;
	}
	
	@Override
	public String toString() {
		String base = (this.subtask == null) ? "wait" : this.subtask.toString();
		return base + ": " + time;
	}
}