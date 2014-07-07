package edu.brown.cs.h2r.baking.Knowledgebase;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.Set;
import java.util.TreeSet;


public class CombinationParser {

	private AbstractMap<String, ArrayList<Set<String>>> map;
	public CombinationParser(String filename) {
		this.map = generateMap(filename);
	}

	private AbstractMap<String,ArrayList<Set<String>>> generateMap(String filename) {
		HashMap<String,ArrayList<Set<String>>> ingredients = new HashMap<String,ArrayList<Set<String>>>();
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

				String combination_list = line.substring(line.indexOf("{")+1, line.indexOf("}"));
				ArrayList<Set<String>> combinations = new ArrayList<Set<String>>();
				
				int comb_start = 0;
				int comb_end = 0;
				while (true) {
					TreeSet<String> possible_combination = new TreeSet<String>();
					comb_start = combination_list.indexOf("[", comb_start);
					comb_end = combination_list.indexOf("]", comb_end);
					if (comb_start == -1 || comb_end == -1) {
						break;
					}
					String trait_list = combination_list.substring(comb_start+1, comb_end);
					for (String trait : trait_list.split(", ")) {
						possible_combination.add(trait);
					}
					combinations.add(possible_combination);
					comb_start++;
					comb_end++;
				}
				ingredients.put(name, combinations);
			}
			br.close();
		} catch (IOException ex) {
			System.err.println(ex);
			System.exit(1);
		}
		return ingredients;
	}
	
	public AbstractMap<String, ArrayList<Set<String>>> getMap() {
		return this.map;
	}
	
	public Set<String> getIngredientNames() {
		return this.map.keySet();
	}
}

