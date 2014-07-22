package edu.brown.cs.h2r.baking.Knowledgebase;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

import org.yaml.snakeyaml.Yaml;


public class BakingParser {
	private static final String INGREDIENTS = "Ingredients.yaml";

	private AbstractMap<String, BakingInformation> ingredientMap;
	public BakingParser() {
		this.ingredientMap = generateMap(BakingParser.INGREDIENTS);
	}

	private AbstractMap<String, BakingInformation> generateMap(String filename) {
		AbstractMap<String, BakingInformation> information = new HashMap<String, BakingInformation>();
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
				BakingInformation info = new BakingInformation(entry.getValue());
				String key = entry.getKey();
				information.put(key, info);
			}
		} catch (ClassCastException e) {
			e.printStackTrace();
		}
		return information;
	}
	
	public AbstractMap<String, BakingInformation> getIngredientMap() {
		return this.ingredientMap;
	}
}

