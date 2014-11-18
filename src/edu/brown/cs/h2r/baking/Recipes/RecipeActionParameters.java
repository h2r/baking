package edu.brown.cs.h2r.baking.Recipes;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import burlap.oomdp.core.Domain;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;

public class RecipeActionParameters {
	AbstractMap<String, List<String[]>> recipeActionParams;
	Domain domain;
	public RecipeActionParameters(Domain domain) {
		this.recipeActionParams = new HashMap<String, List<String[]>>();
		this.domain = domain;
		generateBrowniesParams();/*
		generateCucumberSaladParams();
		generateDeviledEggsParams();
		generateMoltenLavaCakeParams();*/
		generatePeanutButerCookiesParams();/*
		generatePecanPieParams();
		generateChocolateChipCookiesParams();
		generateCranberryWalnutCookiesParams();
		generateBannanaBreadParams();
		generateCherryBlondiesParams();
		generateCherryPieParams();
		generateBlueberryMuffinsParams();*/
	}
	
	public List<String[]> getRecipeParams(String recipeName) {
		return this.recipeActionParams.get(recipeName);
	}
	
	public void generateBrowniesParams() {
		Brownies brownies = Brownies.getRecipe(domain);
		List<String[]> actionParams = Arrays.asList(
			new String[] {"switch", "human", SpaceFactory.SPACE_OVEN},
				
			new String[] {"pour", "human", "butter_bowl", "melting_pot"},
			new String[] {"move", "human", "melting_pot", SpaceFactory.SPACE_STOVE},
			new String[] {"switch", "human", SpaceFactory.SPACE_STOVE},
			new String[] {"move", "human", "melting_pot", SpaceFactory.SPACE_COUNTER},
			
			
			new String[] {"pour", "human", "vanilla_bowl", "mixing_bowl_1"},
			new String[] {"pour", "human", "eggs_bowl", "mixing_bowl_1"},
			new String[] {"mix", "human", "mixing_bowl_1", "whisk"},
			
			new String[] {"pour", "human", "flour_bowl", "mixing_bowl_2"},
			new String[] {"pour", "human", "salt_bowl", "mixing_bowl_2"},
			new String[] {"pour", "human", "cocoa_bowl", "mixing_bowl_2"},
			new String[] {"mix", "human", "mixing_bowl_2", "spoon"},
			
			new String[] {"pour", "human", "mixing_bowl_1", "mixing_bowl_2"},
			new String[] {"mix", "human", "mixing_bowl_2", "whisk"},
			
			new String[] {"pour", "human", "mixing_bowl_2", "baking_dish"},
			
			new String[] {"move", "human", "baking_dish", SpaceFactory.SPACE_OVEN}
		);
		this.recipeActionParams.put(brownies.getRecipeName(), actionParams);
	}
	
	public void generateCucumberSaladParams() {
		CucumberSalad recipe = new CucumberSalad(domain);
		List<String[]> actionParams = Arrays.asList(
			new String[] {"pour", "human", "red_onion_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "red_onion_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "tomatoes_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "tomatoes_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "cucumber_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "cucumber_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"hand", "baxter", "spoon", SpaceFactory.SPACE_COUNTER},
			
			new String[] {"mix", "human", "mixing_bowl_1", "spoon"},
			
			new String[] {"pour", "human", "lemon_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "lemon_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "olive_oil_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "olive_oil_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "salt_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "salt_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "pepper_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "pepper_bowl", SpaceFactory.SPACE_SINK},
			
			new String[] {"hand", "baxter", "whisk", SpaceFactory.SPACE_COUNTER},
			
			new String[] {"mix", "human", "mixing_bowl_2", "whisk"},
			new String[] {"hand", "human", "whisk", SpaceFactory.SPACE_ROBOT},
			
			new String[] {"hand", "baxter", "whisk", SpaceFactory.SPACE_SINK},
			
			new String[] {"pour", "human", "mixing_bowl_1", "mixing_bowl_2"},
			new String[] {"move", "baxter", "mixing_bowl_1", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_2", "spoon"},
			new String[] {"hand", "human", "spoon", SpaceFactory.SPACE_ROBOT},
			new String[] {"hand", "baxter", "spoon", SpaceFactory.SPACE_SINK}
		);
		this.recipeActionParams.put(recipe.getRecipeName(), actionParams);
	}
	
	public void generateDeviledEggsParams() {
		DeviledEggs recipe = new DeviledEggs(domain);
		List<String[]> actionParams = Arrays.asList(
			new String[] {"pour", "human", "egg_yolk_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "egg_yolk_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "pepper_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "pepper_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "dijon_mustard_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "dijon_mustard_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "salt_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "salt_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"hand", "baxter", "whisk", SpaceFactory.SPACE_COUNTER},
			
			new String[] {"mix", "human", "mixing_bowl_1", "whisk"},
			
			new String[] {"pour", "human", "chopped_tarragon_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "chooped_tarragon_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "sweet_gherkins_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "sweet_gherkins_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "shallots_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "shallots_bowl", SpaceFactory.SPACE_SINK},
							
			new String[] {"mix", "human", "mixing_bowl_1", "whisk"},
			new String[] {"hand", "human", "whisk", SpaceFactory.SPACE_ROBOT},
			
			new String[] {"hand", "baxter", "whisk", SpaceFactory.SPACE_SINK},
			
			new String[] {"hand", "baxter", "spoon", SpaceFactory.SPACE_COUNTER},
			new String[] {"pour", "human", "egg_whites_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "egg_whites_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_1", "spoon"},
			new String[] {"hand", "human", "spoon", SpaceFactory.SPACE_ROBOT},
			new String[] {"hand", "baxter", "spoon", SpaceFactory.SPACE_SINK}
		);
		this.recipeActionParams.put(recipe.getRecipeName(), actionParams);
	}
	
	public void generateMashedPotatoesParams() {
		MashedPotatoes recipe = new MashedPotatoes(domain);
		List<String[]> actionParams = Arrays.asList(
			
		);
		this.recipeActionParams.put(recipe.getRecipeName(), actionParams);
	}
	
	public void generateMoltenLavaCakeParams() {
		MoltenLavaCake recipe = new MoltenLavaCake(domain);
		List<String[]> actionParams = Arrays.asList(
			new String[] {"switch", "baxter", SpaceFactory.SPACE_OVEN},
				
			new String[] {"pour", "human", "chocoloate_squares_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "chocolate_squares_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "butter_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "butter_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"hand", "baxter", "whisk", SpaceFactory.SPACE_COUNTER},
			new String[] {"mix", "human", "mixing_bowl_1", "whisk"},
			new String[] {"hand", "human", "whisk", SpaceFactory.SPACE_ROBOT},
			new String[] {"hand", "baxter", "whisk", SpaceFactory.SPACE_SINK},

			new String[] {"pour", "human", "white_sugar_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "white_sugar_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human",  "flour_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "flour_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"hand", "baxter", "spoon", SpaceFactory.SPACE_COUNTER},
			new String[] {"mix", "human", "mixing_bowl_1", "spoon"},
			
			new String[] {"pour", "human", "eggs_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "eggs_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "egg_yolks_bowl_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "egg_tolks_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_1", "spoon"},
			
			new String[] {"pour", "human", "vanilla_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "vanilla_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "orange_liqueur_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "orange_liqueur_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_1", "spoon"},
											
			new String[] {"pour", "human", "mixing_bowl_1", "baking_dish"},
			new String[] {"move", "baxter", "mixing_bowl_1", SpaceFactory.SPACE_SINK},
			
			new String[] {"move", "human", "baking_dish", SpaceFactory.SPACE_OVEN},
			new String[] {"hand", "human", "spoon", SpaceFactory.SPACE_ROBOT},
			new String[] {"hand", "baxter", "spoon", SpaceFactory.SPACE_SINK}
		);
		this.recipeActionParams.put(recipe.getRecipeName(), actionParams);
	}
	
	public void generatePeanutButerCookiesParams() {
		PeanutButterCookies recipe = PeanutButterCookies.getRecipe(domain);
		List<String[]> actionParams = Arrays.asList(
			new String[] {"switch", "human", SpaceFactory.SPACE_OVEN},
			new String[] {"pour", "human", "butter_bowl", "mixing_bowl_1"},
			//new String[] {"move", "human", "melting_pot", SpaceFactory.SPACE_STOVE},
			//new String[] {"switch", "human", SpaceFactory.SPACE_STOVE},
			//new String[] {"move", "human", "melting_pot", SpaceFactory.SPACE_COUNTER},
				
				
			new String[] {"pour", "human", "peanut_butter_bowl", "mixing_bowl_1"},
			//new String[] {"pour", "human", "melting_pot", "mixing_bowl_1"},
			new String[] {"mix", "human", "mixing_bowl_1", "whisk"},
			
			new String[] {"pour", "human", "eggs_bowl", "mixing_bowl_1"},
			new String[] {"mix", "human", "mixing_bowl_1", "whisk"},

			new String[] {"pour", "human",  "baking_powder_bowl", "mixing_bowl_2"},
			new String[] {"pour", "human",  "baking_soda_bowl", "mixing_bowl_2"},
			new String[] {"mix", "human", "mixing_bowl_2", "spoon"},
			
			new String[] {"pour", "human", "mixing_bowl_1", "mixing_bowl_2"},
			new String[] {"mix", "human", "mixing_bowl_2", "spoon"},
			
			new String[] {"pour", "human", "mixing_bowl_2", "baking_dish"},
			
			new String[] {"move", "human", "baking_dish", SpaceFactory.SPACE_OVEN}
			//new String[] {"hand", "baxter", "spoon", SpaceFactory.SPACE_SINK}
		);
		this.recipeActionParams.put(recipe.getRecipeName(), actionParams);
	}
	
	public void generatePecanPieParams() {
		PecanPie recipe = new PecanPie(domain);
		List<String[]> actionParams = Arrays.asList(
			new String[] {"pour", "human", "flour_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "flour_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "salt_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "salt_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "white_sugar_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "white_sugar_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"hand", "baxter", "whisk", SpaceFactory.SPACE_COUNTER},
			
			new String[] {"mix", "human", "mixing_bowl_1", "whisk"},
			new String[] {"pour", "human", "butter_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "butter_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_1", "whisk"},
			new String[] {"pour", "human", "eggs_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "eggs_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_1", "whisk"},
			
			new String[] {"hand", "human", "whisk", SpaceFactory.SPACE_ROBOT},
			new String[] {"hand", "baxter", "whisk", SpaceFactory.SPACE_SINK},

			new String[] {"pour", "human",  "butter_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "butter_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human",  "white_sugar_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "white_sugar_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human",  "salt_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "salt_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human",  "light_corn_syrup_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "light_corn_syrup_bowl", SpaceFactory.SPACE_SINK},
			
			new String[] {"hand", "baxter", "spoon", SpaceFactory.SPACE_COUNTER},
			new String[] {"mix", "human", "mixing_bowl_2", "spoon"},
			new String[] {"pour", "human",  "pecans_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "pecans_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human",  "bourbon_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "bourbon_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human",  "vanilla_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "vanilla_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_2", "spoon"},
			new String[] {"pour", "human",  "eggs_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "eggs_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_2", "spoon"},
			
			new String[] {"pour", "human", "mixing_bowl_1", "mixing_bowl_2"},
			new String[] {"move", "baxter", "mixing_bowl_1", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_2", "spoon"},
			new String[] {"hand", "human", "spoon", SpaceFactory.SPACE_ROBOT},
			
			new String[] {"pour", "human", "mixing_bowl_2", "baking_dish"},
			new String[] {"move", "baxter", "mixing_bowl_2", SpaceFactory.SPACE_SINK},
			
			new String[] {"switch", "baxter", SpaceFactory.SPACE_OVEN},
			new String[] {"move", "human", "baking_dish", SpaceFactory.SPACE_OVEN},
			new String[] {"hand", "baxter", "spoon", SpaceFactory.SPACE_SINK}	
		);
		this.recipeActionParams.put(recipe.getRecipeName(), actionParams);
	}
	
	public void generateChocolateChipCookiesParams() {
		ChocolateChipCookies cookies = new ChocolateChipCookies(domain);
		List<String[]> actionParams = Arrays.asList(
			new String[] {"hand", "baxter", "whisk", SpaceFactory.SPACE_COUNTER},	
			new String[] {"pour", "human", "butter_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "butter_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "brown_sugar_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "brown_sugar_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "white_sugar_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "white_sugar_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_1", "whisk"},
			
			new String[] {"hand", "baxter", "spoon", SpaceFactory.SPACE_COUNTER},
			new String[] {"pour", "human", "flour_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "flour_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "baking_soda_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "baking_soda_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "salt_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "salt_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_2", "spoon"},
			
			new String[] {"pour", "human", "vanilla_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "vanilla_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "eggs_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "eggs_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_1", "whisk"},
			
			new String[] {"pour", "human", "mixing_bowl_2", "mixing_bowl_1"},
			new String[] {"move", "baxter", "mixing_bowl_2", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "chocolate_chips_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "chocolate_chips_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_1", "spoon"},	
			new String[] {"hand", "human", "spoon", SpaceFactory.SPACE_ROBOT},
			new String[] {"pour", "human", "mixing_bowl_1", "baking_dish"},
			new String[] {"move", "baxter", "mixing_bowl_1", SpaceFactory.SPACE_SINK},
			
			new String[] {"switch", "baxter", SpaceFactory.SPACE_OVEN},
			new String[] {"move", "human", "baking_dish", SpaceFactory.SPACE_OVEN},
			new String[] {"hand", "baxter", "spoon", SpaceFactory.SPACE_SINK},
			new String[] {"hand", "human", "whisk", SpaceFactory.SPACE_ROBOT},
			new String[] {"hand", "baxter", "whisk", SpaceFactory.SPACE_SINK}
		);
		this.recipeActionParams.put(cookies.getRecipeName(), actionParams);
	}
	
	public void generateCranberryWalnutCookiesParams() {
		CranberryWalnutCookies cookies = new CranberryWalnutCookies(domain);
		List<String[]> actionParams = Arrays.asList(
			new String[] {"hand", "baxter", "whisk", SpaceFactory.SPACE_COUNTER},	
			new String[] {"pour", "human", "butter_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "butter_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "brown_sugar_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "brown_sugar_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "white_sugar_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "white_sugar_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_1", "whisk"},
			
			new String[] {"hand", "baxter", "spoon", SpaceFactory.SPACE_COUNTER},
			new String[] {"pour", "human", "flour_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "flour_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "baking_soda_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "baking_soda_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "salt_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "salt_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "cinnamon_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "cinnamon_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_2", "spoon"},
			
			new String[] {"pour", "human", "vanilla_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "vanilla_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "eggs_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "eggs_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_1", "whisk"},
			
			new String[] {"pour", "human", "mixing_bowl_2", "mixing_bowl_1"},
			new String[] {"move", "baxter", "mixing_bowl_2", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "dried_cranberries_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "dried_cranberries_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "chopped_walnuts_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "chopped_walnuts_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_1", "spoon"},	
			new String[] {"hand", "human", "spoon", SpaceFactory.SPACE_ROBOT},
			new String[] {"pour", "human", "mixing_bowl_1", "baking_dish"},
			new String[] {"move", "baxter", "mixing_bowl_1", SpaceFactory.SPACE_SINK},
			
			new String[] {"switch", "baxter", SpaceFactory.SPACE_OVEN},
			new String[] {"move", "human", "baking_dish", SpaceFactory.SPACE_OVEN},
			new String[] {"hand", "baxter", "spoon", SpaceFactory.SPACE_SINK},
			new String[] {"hand", "human", "whisk", SpaceFactory.SPACE_ROBOT},
			new String[] {"hand", "baxter", "whisk", SpaceFactory.SPACE_SINK}
		);
		this.recipeActionParams.put(cookies.getRecipeName(), actionParams);
	}
	
	public void generateBannanaBreadParams() {
		BannanaBread bread  = new BannanaBread(domain);
		List<String[]> actionParams = Arrays.asList(
			new String[] {"hand", "baxter", "whisk", SpaceFactory.SPACE_COUNTER},	
			new String[] {"pour", "human", "butter_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "butter_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "bannanas_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "bannanas_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_1", "whisk"},
			
			new String[] {"hand", "baxter", "spoon", SpaceFactory.SPACE_COUNTER},
			new String[] {"pour", "human", "flour_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "flour_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "baking_soda_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "baking_soda_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "salt_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "salt_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_1", "spoon"},
			
			new String[] {"pour", "human", "vanilla_bowl", "mixing_bowl_1"},
			new String[] {"pour", "human", "brown_sugar_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "brown_sugar_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"move", "baxter", "vanilla_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "eggs_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "eggs_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_1", "whisk"},
			
			new String[] {"pour", "human", "mixing_bowl_1", "baking_dish"},
			new String[] {"move", "baxter", "mixing_bowl_1", SpaceFactory.SPACE_SINK},
			
			new String[] {"switch", "baxter", SpaceFactory.SPACE_OVEN},
			new String[] {"move", "human", "baking_dish", SpaceFactory.SPACE_OVEN},
			new String[] {"hand", "baxter", "spoon", SpaceFactory.SPACE_SINK},
			new String[] {"hand", "human", "whisk", SpaceFactory.SPACE_ROBOT},
			new String[] {"hand", "baxter", "whisk", SpaceFactory.SPACE_SINK}
		);
		this.recipeActionParams.put(bread.getRecipeName(), actionParams);
	}
	
	public void generateCherryBlondiesParams() {
		CherryBlondies blondies = new CherryBlondies(domain);
		List<String[]> actionParams = Arrays.asList(
			new String[] {"hand", "baxter", "whisk", SpaceFactory.SPACE_COUNTER},	
			new String[] {"pour", "human", "butter_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "butter_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "brown_sugar_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "brown_sugar_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "almond_extract_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "almond_extract_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "eggs_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "eggs_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_1", "whisk"},
			
			new String[] {"hand", "baxter", "spoon", SpaceFactory.SPACE_COUNTER},
			new String[] {"pour", "human", "flour_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "flour_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "baking_powder_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "baking_powder_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "salt_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "salt_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_2", "spoon"},
			
			new String[] {"pour", "human", "mixing_bowl_2", "mixing_bowl_1"},
			new String[] {"move", "baxter", "mixing_bowl_2", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "dried_cherries_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "dried_cherries_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "sliced_almonds_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "sliced_almonds_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "white_chocolate_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "white_chocolate_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_1", "spoon"},	
			new String[] {"hand", "human", "spoon", SpaceFactory.SPACE_ROBOT},
			new String[] {"pour", "human", "mixing_bowl_1", "baking_dish"},
			new String[] {"move", "baxter", "mixing_bowl_1", SpaceFactory.SPACE_SINK},
			
			new String[] {"switch", "baxter", SpaceFactory.SPACE_OVEN},
			new String[] {"move", "human", "baking_dish", SpaceFactory.SPACE_OVEN},
			new String[] {"hand", "baxter", "spoon", SpaceFactory.SPACE_SINK},
			new String[] {"hand", "human", "whisk", SpaceFactory.SPACE_ROBOT},
			new String[] {"hand", "baxter", "whisk", SpaceFactory.SPACE_SINK}
		);
		this.recipeActionParams.put(blondies.getRecipeName(), actionParams);
	}
	
	public void generateCherryPieParams() {
		CherryPie recipe = new CherryPie(domain);
		List<String[]> actionParams = Arrays.asList(
			new String[] {"pour", "human", "flour_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "flour_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "salt_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "salt_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "white_sugar_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "white_sugar_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"hand", "baxter", "spoon", SpaceFactory.SPACE_COUNTER},
			
			new String[] {"mix", "human", "mixing_bowl_1", "spoon"},
			new String[] {"pour", "human", "butter_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "butter_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_1", "whisk"},
			new String[] {"pour", "human", "eggs_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "eggs_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_1", "whisk"},
			
			new String[] {"hand", "human", "spoon", SpaceFactory.SPACE_ROBOT},
			new String[] {"hand", "baxter", "spoon", SpaceFactory.SPACE_SINK},

			new String[] {"pour", "human",  "frozen_cherries_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "frozen_cherries_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human",  "white_sugar_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "white_sugar_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human",  "cornstarch_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "cornstarch_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human",  "almond_extract_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "almond_extract_bowl", SpaceFactory.SPACE_SINK},
			
			new String[] {"pour", "human", "mixing_bowl_1", "mixing_bowl_2"},
			new String[] {"move", "baxter", "mixing_bowl_1", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_2", "whisk"},
			new String[] {"hand", "human", "spoon", SpaceFactory.SPACE_ROBOT},
			
			new String[] {"pour", "human", "mixing_bowl_2", "baking_dish"},
			new String[] {"move", "baxter", "mixing_bowl_2", SpaceFactory.SPACE_SINK},
			
			new String[] {"switch", "baxter", SpaceFactory.SPACE_OVEN},
			new String[] {"move", "human", "baking_dish", SpaceFactory.SPACE_OVEN},
			new String[] {"hand", "baxter", "spoon", SpaceFactory.SPACE_SINK}	
		);
		this.recipeActionParams.put(recipe.getRecipeName(), actionParams);
	}
	
	public void generateBlueberryMuffinsParams() {
		BlueberryMuffins muffins = new BlueberryMuffins(domain);
		List<String[]> actionParams = Arrays.asList(
			new String[] {"hand", "baxter", "whisk", SpaceFactory.SPACE_COUNTER},	
			new String[] {"pour", "human", "butter_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "butter_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "brown_sugar_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "brown_sugar_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "vanilla_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "vanilla_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "sour_cream_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "sour_cream_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "whole_milk_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "whole_milk_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "eggs_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "eggs_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_1", "whisk"},
			
			new String[] {"hand", "baxter", "spoon", SpaceFactory.SPACE_COUNTER},
			new String[] {"pour", "human", "flour_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "flour_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "baking_powder_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "baking_powder_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "baking_soda_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "baking_soda_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "salt_bowl", "mixing_bowl_2"},
			new String[] {"move", "baxter", "salt_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_2", "spoon"},
			
			new String[] {"pour", "human", "mixing_bowl_2", "mixing_bowl_1"},
			new String[] {"move", "baxter", "mixing_bowl_2", SpaceFactory.SPACE_SINK},
			new String[] {"pour", "human", "blueberries_bowl", "mixing_bowl_1"},
			new String[] {"move", "baxter", "blueberries_bowl", SpaceFactory.SPACE_SINK},
			new String[] {"mix", "human", "mixing_bowl_1", "spoon"},	
			new String[] {"hand", "human", "spoon", SpaceFactory.SPACE_ROBOT},
			new String[] {"pour", "human", "mixing_bowl_1", "baking_dish"},
			new String[] {"move", "baxter", "mixing_bowl_1", SpaceFactory.SPACE_SINK},
			
			new String[] {"switch", "baxter", SpaceFactory.SPACE_OVEN},
			new String[] {"move", "human", "baking_dish", SpaceFactory.SPACE_OVEN},
			new String[] {"hand", "baxter", "spoon", SpaceFactory.SPACE_SINK},
			new String[] {"hand", "human", "whisk", SpaceFactory.SPACE_ROBOT},
			new String[] {"hand", "baxter", "whisk", SpaceFactory.SPACE_SINK}
		);
		this.recipeActionParams.put(muffins.getRecipeName(), actionParams);
	}

}
