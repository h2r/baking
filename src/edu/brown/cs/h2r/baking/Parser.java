package edu.brown.cs.h2r.baking;

import java.io.File;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;


public class Parser {

	String filename;
	public Parser(String filename) {
		this.filename = filename;
	}

	public HashMap<String,String> getAttributeMap() {
		HashMap<String,String> ingredients = new HashMap<String,String>();
		File fin = new File(this.filename);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(fin));
			String line;
			while((line = br.readLine())!=null) {
				int end = line.indexOf("'", 1);
				String name = line.substring(1, end);

				String attributes = line.substring(line.indexOf("[")+1, line.indexOf("]"));
				ingredients.put(name, attributes);
			}
			br.close();
		} catch (IOException ex) {
			System.err.println(ex);
			System.exit(1);
		}
		return ingredients;
	}

	public static void main(String[] args) {
		Parser parser = new Parser(args[0]);
		HashMap<String,String> map = parser.getAttributeMap();
		System.out.println(map);
	}
}

