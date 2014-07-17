package edu.brown.cs.h2r.baking.Knowledgebase;

public class BakingCastException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public BakingCastException(ClassCastException e) {
		this.setStackTrace(e.getStackTrace());
	}
}
