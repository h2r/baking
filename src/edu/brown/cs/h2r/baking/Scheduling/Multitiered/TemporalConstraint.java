package edu.brown.cs.h2r.baking.Scheduling.Multitiered;

public class TemporalConstraint {
	Subtask subtask;
	double lowerBound;
	double upperBound;
	
	public TemporalConstraint(Subtask subtask, double lowerBound, double upperBound) {
		this.subtask = subtask;
		this.lowerBound = lowerBound;
		this.upperBound = upperBound;
	}
	
	public boolean tightenConstraint(double lowerBound, double upperBound) {
		if (lowerBound > this.upperBound || upperBound < this.lowerBound) {
			return false;
		}
		this.lowerBound = Math.max(this.lowerBound, lowerBound);
		this.upperBound = Math.min(this.upperBound, upperBound);
		return true;
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append(this.subtask.getId());
		if (lowerBound != 0.0) {
			buffer.append(" lb: ").append(this.lowerBound);
		}
		if (this.upperBound != Double.MAX_VALUE) {
			buffer.append(" ub: ").append(this.upperBound);
		}
		return buffer.toString();
	}
}