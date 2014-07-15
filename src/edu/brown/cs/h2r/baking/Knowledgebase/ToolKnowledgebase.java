package edu.brown.cs.h2r.baking.Knowledgebase;

import java.util.AbstractMap;
import java.util.Map.Entry;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.State;
import edu.brown.cs.h2r.baking.ObjectFactories.ToolFactory;

public class ToolKnowledgebase {
	private static final String TOOLYAML = "Tool.yaml";
	
	private AbstractMap<String, ToolInformation> toolMap;
	
	public ToolKnowledgebase() {
		this.toolMap = new ToolParser(TOOLYAML).getMap();
	}

	public void addTools(Domain domain, State state, String space) {
		for (Entry<String, ToolInformation> entry : this.toolMap.entrySet()) {
			String name = entry.getKey();
			ToolInformation info = entry.getValue();
			String toolTrait = null;
			String toolAttribute = null;
			boolean transportable = false;
			try {
				toolTrait = info.getString(ToolInformation.fieldTrait);
				toolAttribute = info.getString(ToolInformation.fieldAttribute);
				transportable = info.getBoolean(ToolInformation.fieldTransportable);
			} catch (ToolCastException e) {
				e.printStackTrace();
			}
			
			if (transportable) {
				state.addObject(ToolFactory.getNewTransportableToolObjectInstance(domain, name, toolTrait, toolAttribute, space));
			} else {
				state.addObject(ToolFactory.getNewSimpleToolObjectInstance(domain, name, toolTrait, toolAttribute, space));
			}
		}
	}
}
