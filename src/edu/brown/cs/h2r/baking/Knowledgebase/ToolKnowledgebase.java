package edu.brown.cs.h2r.baking.Knowledgebase;

import java.util.AbstractMap;

public class ToolKnowledgebase {
	private static final String TOOLS = "Tools.txt";
	
	
	private AbstractMap<String, String[]> toolMap;
	
	public ToolKnowledgebase() {
		this.toolMap = new ToolParser(TOOLS).getMap();
	}
	
	public AbstractMap<String, String[]> getToolMap() {
		return this.toolMap;
	}
}
