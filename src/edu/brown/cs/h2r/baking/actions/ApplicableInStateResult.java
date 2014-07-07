package edu.brown.cs.h2r.baking.actions;

public class ApplicableInStateResult {
	protected boolean isApplicable;
	protected String whyNot;

	protected static final ApplicableInStateResult TRUE = new ApplicableInStateResult(true, "");
	
	protected ApplicableInStateResult(boolean isApplicable, String whyNot) {
		this.isApplicable = isApplicable;
		this.whyNot = whyNot;
	}
	
	public static ApplicableInStateResult False(String why) {
		return new ApplicableInStateResult(false, why);
	}
	
	public static ApplicableInStateResult True() {
		return ApplicableInStateResult.TRUE;
	}
	
	public String getWhyNot() {
		return whyNot;
	}
	
	public boolean getIsApplicable() {
		return this.isApplicable;
	}
}
