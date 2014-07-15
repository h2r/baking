package edu.brown.cs.h2r.baking.Knowledgebase;

public class ToolCastException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ToolCastException(ClassCastException e) {
		this.setStackTrace(e.getStackTrace());
	}
}
