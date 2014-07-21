package edu.brown.cs.h2r.baking.Knowledgebase;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


public class BakingInformation extends LinkedHashMap<String, Object> {

	private static final String ERROR_KEY= "Error";
	public static final String toolTrait = "trait";
	public static final String toolAttribute = "attribute";
	public static final String toolCanCarry = "canCarry";
	public static final String toolInclude = "include";
	public static final String toolExclude = "exclude";
	
	public static final String ingredientTraits = "traits";
	public static final String ingredientToolTraits = "toolTraits";
	public static final String ingredientHeatingInformation = "heatingInformation";
	
	public static final String combinationTraits = "traits";
	public static final String combinationPossibleCombinations = "possibleCombinations";
	
	public BakingInformation () {
	}
	
	public BakingInformation(AbstractMap<String, Object> map) {
		this.putAll(map);
	}
	
	public Object getObject(String key) {
		return this.get(key);
	}
		
	public void setObject(String key, Object object) {
		this.put(key, object);
	}
	
	public void setError(boolean isError) {
		this.put(BakingInformation.ERROR_KEY, isError);
	}
	
	public boolean getError() {
		if (this.containsKey(BakingInformation.ERROR_KEY)) {
			try {
				return this.getBoolean(BakingInformation.ERROR_KEY);
			} catch (BakingCastException e) {
				return true;
			}
		}
		return false;
	}
	
	public String getString(String key) throws BakingCastException {
		try {
			Object string = this.getObject(key);
			if (string == null) {
				return "";
			}
			return (String)this.getObject(key);
		}
		catch (ClassCastException e) {
			throw e;
		}
	}
	
	public void setString(String key, String value) {
		this.setObject(key, value);
	}
	
	public boolean getBoolean(String key)  throws BakingCastException  {
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
	
	public BakingInformation getMap(String key) throws BakingCastException {
		try {
			return (BakingInformation)this.getMap(key);
		}
		catch (ClassCastException e) {
			throw e;
		}
	}
	
	public List<String> getListOfString(String key) throws BakingCastException {
		try {
			return (ArrayList<String>)this.get(key);
		}
		catch (ClassCastException e) {
			throw e;
		}
	}
	
	public List<List<String>> getListOfList(String key) throws BakingCastException {
		try {
			return (ArrayList<List<String>>)this.get(key);
		}
		catch (ClassCastException e) {
			throw e;
		}
	}
}
