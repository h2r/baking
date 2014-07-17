package edu.brown.cs.h2r.baking.Knowledgebase;

import java.util.AbstractMap;
import java.util.LinkedHashMap;


public class ToolInformation extends LinkedHashMap<String, Object> {

	private static final String ERROR_KEY= "Error";
	public static final String fieldTrait = "trait";
	public static final String fieldAttribute = "attribute";
	public static final String fieldTransportable = "transportable";
	
	public ToolInformation () {
	}
	
	public ToolInformation(AbstractMap<String, Object> map) {
		this.putAll(map);
	}
	
	public Object getObject(String key) {
		return this.get(key);
	}
		
	public void setObject(String key, Object object) {
		this.put(key, object);
	}
	
	public void setError(boolean isError) {
		this.put(ToolInformation.ERROR_KEY, isError);
	}
	
	public boolean getError() {
		if (this.containsKey(ToolInformation.ERROR_KEY)) {
			try {
				return this.getBoolean(ToolInformation.ERROR_KEY);
			} catch (ToolCastException e) {
				return true;
			}
		}
		return false;
	}
	
	public String getString(String key) throws ToolCastException {
		try {
			return (String)this.getObject(key);
		}
		catch (ClassCastException e) {
			throw e;
		}
	}
	
	public void setString(String key, String value) {
		this.setObject(key, value);
	}
	
	public boolean getBoolean(String key)  throws ToolCastException  {
		try {
			return (boolean)this.getObject(key);
		}
		catch (ClassCastException e) {
			throw e;
		}
	}
	
	public void setBoolean(String key, boolean value) {
		this.setObject(key, value);
	}
}
