package edu.brown.cs.h2r.baking.actions;

public class BakingActionResult {
	protected boolean isSuccess;
	protected String whyFailed;

	protected static final BakingActionResult SUCCESS = new BakingActionResult(true, "");
	
	protected BakingActionResult(boolean isSuccess, String whyFailure) {
		this.isSuccess = isSuccess;
		this.whyFailed = whyFailure;
	}
	
	public static BakingActionResult failure(String whyFailure) {
		return new BakingActionResult(false, whyFailure);
	}
	
	public static BakingActionResult success() {
		return BakingActionResult.SUCCESS;
	}
	
	public String getWhyFailed() {
		return whyFailed;
	}
	
	public boolean getIsSuccess() {
		return this.isSuccess;
	}
}
