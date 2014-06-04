package edu.brown.cs.h2r.baking;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeSet;

public class TraitKnowledgebase {

	private AbstractMap<String, Set<String>> traitMap;
	
	public TraitKnowledgebase() {
		this.traitMap = generateTraitMap();
	}
	
	private AbstractMap<String, Set<String>> generateTraitMap() {
		AbstractMap<String, Set<String>> traitMap = new HashMap<String, Set<String>>();
		Set<String> traits;
		
		traits = new TreeSet<String>();
		traits.add("dry");
		traitMap.put("dry_stuff", traits);
		
		traits = new TreeSet<String>();
		traits.add("dry");
		traitMap.put("baking_soda", traits);
		
		traits = new TreeSet<String>();
		traits.add("dry");
		traits.add("flour");
		traitMap.put("flour", traits);
		
		traits = new TreeSet<String>();
		traits.add("dry");
		traitMap.put("baking_powder", traits);
		
		traits = new TreeSet<String>();
		traits.add("dry");
		traits.add("seasoning");
		traits.add("salt");
		traitMap.put("salt", traits);
		
		traits = new TreeSet<String>();
		traits.add("wet");
		traits.add("sugar");
		traitMap.put("white_sugar", traits);
		
		traits = new TreeSet<String>();
		traits.add("wet");
		traits.add("sugar");
		traitMap.put("brown_sugar", traits);
		
		traits = new TreeSet<String>();
		traits.add("wet");
		traits.add("eggs");
		traitMap.put("eggs", traits);
		
		traits = new TreeSet<String>();
		traits.add("wet");
		traits.add("fat");
		traits.add("butter");
		traitMap.put("butter", traits);
		
		traits = new TreeSet<String>();
		traits.add("wet");
		traitMap.put("cocoa", traits);
		
		traits = new TreeSet<String>();
		traits.add("wet");
		traitMap.put("wet_stuff", traits);
		
		traits = new TreeSet<String>();
		traitMap.put("brownies", traits);
		
		//System.out.println(traitMap);
		return traitMap;
	}
	
	public AbstractMap<String, Set<String>> getTraitMap() {
		return this.traitMap;
	}
	
	public Set<String> getTraits(String ingredient) {
		if (this.traitMap.containsKey(ingredient)) {
			return this.traitMap.get(ingredient);
		}
		return new TreeSet<String>();
	}
}
