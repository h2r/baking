package edu.brown.cs.h2r.baking.Knowledgebase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;


public class TraitParser {

	private AbstractMap<String, Set<String>> map;
	public TraitParser(String filename) {
		this.map = generateMap(filename);
	}

	private AbstractMap<String,Set<String>> generateMap(String filename) {
		HashMap<String,Set<String>> ingredients = new HashMap<String,Set<String>>();
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

		BufferedReader br = null;
		try {
			br = new BufferedReader(new InputStreamReader(in));
			String line;
			while((line = br.readLine())!=null) {
				int end = line.indexOf("'", 1);
				String name = line.substring(1, end);

				String trait_list = line.substring(line.indexOf("[")+1, line.indexOf("]"));
				Set<String> traits = new TreeSet<String>();
				if (!trait_list.isEmpty()) {
					for (String trait : trait_list.split(", ")) {
						traits.add(trait);
					}
					ingredients.put(name, traits);
				} else {
					ingredients.put(name, null);
				}
			}
			br.close();
		} catch (IOException ex) {
			System.err.println(ex);
			System.exit(1);
		}
		return ingredients;
	}
	
	public AbstractMap<String, Set<String>> getMap() {
		return this.map;
	}
	
	public Set<String> getIngredientNames() {
		return this.map.keySet();
	}
}

