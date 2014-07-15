package edu.brown.cs.h2r.baking.Knowledgebase;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.yaml.snakeyaml.Yaml;


public class ToolParser {

	private AbstractMap<String, ToolInformation> map;
	public ToolParser(String filename) {
		this.map = generateMap(filename);
	}

	private AbstractMap<String, ToolInformation> generateMap(String filename) {
		AbstractMap<String, ToolInformation> tools = new HashMap<String, ToolInformation>();
		ClassLoader CLDR = this.getClass().getClassLoader();
		
		URL resourceURL = CLDR.getResource(filename);
		if (resourceURL == null) {
			throw new RuntimeException("File " + filename + " does not exist in directory " + CLDR.getResource(".").getFile());
		}
		
		InputStream in;
		try {
			in = resourceURL.openStream();
		} catch (IOException e) {
			throw new RuntimeException(e.getMessage());
			
		}
		if (in == null) {
			throw new RuntimeException("File " + filename + " does not exist in directory " + CLDR.getResource(".").getFile());
		}
		Yaml yaml = new Yaml();
		try {
			AbstractMap<String, AbstractMap<String, Object>> data = ((AbstractMap<String, AbstractMap<String, Object>>)yaml.load(in));
			for (Entry<String, AbstractMap<String, Object>>  entry : data.entrySet()) {
				ToolInformation info = new ToolInformation(entry.getValue());
				String tool = entry.getKey();
				tools.put(tool, info);
			}
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		return tools;
	}
	
	public AbstractMap<String, ToolInformation> getMap() {
		return this.map;
	}
	
	public Set<String> getIngredientNames() {
		return this.map.keySet();
	}
}

