package edu.brown.cs.h2r.baking.Testing;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.SADomain;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.Knowledgebase;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ToolFactory;
import edu.brown.cs.h2r.baking.Recipes.Brownies;
import edu.brown.cs.h2r.baking.Recipes.MashedPotatoes;
import edu.brown.cs.h2r.baking.Recipes.Recipe;
import edu.brown.cs.h2r.baking.actions.MixAction;
import edu.brown.cs.h2r.baking.actions.MoveAction;
import edu.brown.cs.h2r.baking.actions.PourAction;
import edu.brown.cs.h2r.baking.actions.SwitchAction;
import edu.brown.cs.h2r.baking.actions.UseAction;

public class TestActions {

	Knowledgebase knowledgebase;
	State state;
	Domain domain;
	List<ObjectInstance> allIngredients;
	IngredientRecipe topLevelIngredient;
	
	@Before
	public void setUp() {
		domain = new SADomain();
		this.setUpDomain();
		List<ObjectInstance> objects = new ArrayList<ObjectInstance>();
		ObjectInstance agent = AgentFactory.getNewHumanAgentObjectInstance(domain, "human");
		objects.add(agent);
		List<String> containers = Arrays.asList("mixing_bowl_1", "mixing_bowl_2");
		ObjectInstance counter = SpaceFactory.getNewWorkingSpaceObjectInstance(domain, "counter", containers, "human");
		objects.add(counter);
		
		for (String container : containers) { 
			ObjectInstance containerInstance = ContainerFactory.getNewMixingContainerObjectInstance(domain, container, null, "counter");
			objects.add(containerInstance);
		}
		this.state = new State(objects);
	}
	
	public void setUpState() {
		ObjectClass containerClass = domain.getObjectClass(ContainerFactory.ClassName);		
		ObjectInstance counterSpace = state.getObject("counter");

		List<ObjectInstance> ingredientInstances = allIngredients;
		List<ObjectInstance> containerInstances = 
				Recipe.getContainersAndIngredients(containerClass, ingredientInstances, counterSpace.getName());
		
		List<ObjectInstance> objectsToAdd = new ArrayList<ObjectInstance>();
		objectsToAdd.addAll(ingredientInstances);
		objectsToAdd.addAll(containerInstances);
		
		this.state = this.state.appendAllObjects(objectsToAdd);
	}
	
	private void setUpDomain() {
		domain.addObjectClass(ContainerFactory.createObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createSimpleIngredientObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createComplexIngredientObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createSimpleHiddenIngredientObjectClass(domain));
		domain.addObjectClass(IngredientFactory.createComplexHiddenIngredientObjectClass(domain));
		domain.addObjectClass(SpaceFactory.createObjectClass(domain));		
		domain.addObjectClass(AgentFactory.getObjectClass(domain));
		domain.addObjectClass(ToolFactory.createObjectClass(domain));
	}
	
	@After
	public void tearDown() {
		knowledgebase = null;
		state = null;
		domain = null;
		allIngredients = null;
		topLevelIngredient = null;
	}
	
	@Test
	public void testMixAction() {
		Recipe recipe = new Brownies();
		knowledgebase = new Knowledgebase();
		allIngredients = knowledgebase.getRecipeObjectInstanceList(domain, recipe);
		allIngredients.add(IngredientFactory.getNewIngredientInstance(knowledgebase.
				getIngredient("orange_juice"), "orange_juice", domain.getObjectClass("simple_ingredient")));
		this.setUpState();
		Action mix = new MixAction(domain, topLevelIngredient);
		((MixAction)mix).changeKnowledgebase(knowledgebase);
		Action pour = new PourAction(domain, topLevelIngredient);
		
		// can't mix an empty bowl
		Assert.assertEquals(ContainerFactory.getContentNames(state.getObject("mixing_bowl_1")).size(), 0);
		BakingAsserts.assertActionNotApplicable(mix, state, new String[] {"human", "mixing_bowl_1"});
		
		// can't mix a bowl with just 1 ingredient
		state = pour.performAction(state, new String[] {"human", "flour_bowl", "mixing_bowl_1"});
		Assert.assertEquals(ContainerFactory.getContentNames(state.getObject("mixing_bowl_1")).size(), 1);
		BakingAsserts.assertActionNotApplicable(mix, state, new String[] {"human", "mixing_bowl_1"});

		// can't mix a non-mixing bowl
		BakingAsserts.assertActionNotApplicable(mix, state, new String[] {"human", "cocoa_bowl"});
		
		// Mix a bowl with two random (non-combinable) ingredients
		state = pour.performAction(state, new String[] {"human", "baking_powder_bowl", "mixing_bowl_2"});
		state = pour.performAction(state, new String[] {"human", "cocoa_bowl", "mixing_bowl_2"});
		Assert.assertEquals(ContainerFactory.getContentNames(state.getObject("mixing_bowl_2")).size(), 2);
		BakingAsserts.assertActionApplicable(mix, state, new String[] {"human", "mixing_bowl_2"});

		state = mix.performAction(state, new String[] {"human", "mixing_bowl_2"});
		Assert.assertEquals(ContainerFactory.getContentNames(state.getObject("mixing_bowl_2")).size(), 1);
		ObjectInstance namedIng = null;
		for (String name : ContainerFactory.getContentNames(state.getObject("mixing_bowl_2"))) {
			namedIng = state.getObject(name);
			break;
		}
		BakingAsserts.assertIngredientContains(namedIng, Arrays.asList("cocoa", "baking_powder"));
		
		state = pour.performAction(state, new String[] {"human", "orange_juice_bowl", "mixing_bowl_1"});
		Assert.assertEquals(ContainerFactory.getContentNames(state.getObject("mixing_bowl_1")).size(), 2);
		BakingAsserts.assertActionApplicable(mix, state, new String[] {"human", "mixing_bowl_1"});
		state = mix.performAction(state, new String[] {"human", "mixing_bowl_1"});
		Assert.assertEquals(ContainerFactory.getContentNames(state.getObject("mixing_bowl_1")).size(), 1);
		BakingAsserts.assertContainerContains(state.getObject("mixing_bowl_1"), "dough");
		
		// Mix two ingredients on the stove, show that resulting ingredient is heated
		
		ObjectInstance stove = SpaceFactory.getNewHeatingSpaceObjectInstance(domain, "stove", null, "");
		ObjectInstance meltingPot = ContainerFactory.getNewHeatingContainerObjectInstance(domain, "melting_pot", null, "counter");
		this.state = this.state.appendObject(meltingPot);
		this.state = this.state.appendObject(stove);
		Action switchA = new SwitchAction(domain);
		Action move = new MoveAction(domain, topLevelIngredient);
		
		state = switchA.performAction(state, new String[] {"human", "stove"});
		state = move.performAction(state, new String[] {"human", "melting_pot", "stove"});
		
		state = pour.performAction(state, new String[] {"human", "butter_bowl", "melting_pot"});
		state = pour.performAction(state, new String[] {"human", "cocoa_bowl", "melting_pot"});
	}
	
	@Test
	public void testMixActionOnStove() {
		Recipe recipe = new Brownies();
		knowledgebase = new Knowledgebase();
		allIngredients = knowledgebase.getRecipeObjectInstanceList(domain, recipe);
		this.setUpState();
		Action mix = new MixAction(domain, topLevelIngredient);
		((MixAction)mix).changeKnowledgebase(knowledgebase);
		Action pour = new PourAction(domain, topLevelIngredient);
		Action switchA = new SwitchAction(domain);
		Action move = new MoveAction(domain, topLevelIngredient);
		// Mix two ingredients on the stove, show that resulting ingredient is heated
		
		ObjectInstance stove = SpaceFactory.getNewHeatingSpaceObjectInstance(domain, "stove", null, "");
		ObjectInstance meltingPot = ContainerFactory.getNewHeatingContainerObjectInstance(domain, "melting_pot", null, "counter");
		this.state = this.state.appendObject(meltingPot);
		this.state = this.state.appendObject(stove);
		
		state = switchA.performAction(state, new String[] {"human", "stove"});
		state = move.performAction(state, new String[] {"human", "melting_pot", "stove"});
		
		state = pour.performAction(state, new String[] {"human", "butter_bowl", "melting_pot"});
		state = pour.performAction(state, new String[] {"human", "cocoa_bowl", "melting_pot"});
		state = mix.performAction(state, new String[] {"human", "melting_pot"});
		
		ObjectInstance complexIng = state.getFirstObjectOfClass(IngredientFactory.ClassNameComplex);
		BakingAsserts.assertIsHeated(complexIng);
	}
		
	@Test
	public void testMoveAction() {	
		Recipe recipe = new Brownies();
		knowledgebase = new Knowledgebase();
		allIngredients = knowledgebase.getRecipeObjectInstanceList(domain, recipe);
		this.setUpState();
		Action move = new MoveAction(domain, topLevelIngredient);
		
		ObjectInstance oven = SpaceFactory.getNewBakingSpaceObjectInstance(domain, "oven", null, "");
		ObjectInstance stove = SpaceFactory.getNewHeatingSpaceObjectInstance(domain, "stove", null, "");
		ObjectInstance counter = state.getObject("counter");
		this.state = this.state.appendObject(oven);
		this.state = this.state.appendObject(stove);
		
		ObjectInstance meltingPot = ContainerFactory.getNewHeatingContainerObjectInstance(domain, "melting_pot", null, "counter");
		ObjectInstance bakingPan = ContainerFactory.getNewBakingContainerObjectInstance(domain, "baking_pan", null, "counter");
		this.state = this.state.appendObject(meltingPot);
		this.state = this.state.appendObject(bakingPan);
		ObjectInstance newCounter = SpaceFactory.addContainer(counter, meltingPot);
		newCounter = SpaceFactory.addContainer(counter, bakingPan);
		this.state = this.state.replaceObject(counter, newCounter);
		
		BakingAsserts.assertActionNotApplicable(move, state, new String[] {"human", "baking_pan", "stove"});
		BakingAsserts.assertActionNotApplicable(move, state, new String[] {"human", "mixing_bowl_1", "stove"});
		BakingAsserts.assertActionNotApplicable(move, state, new String[] {"human", "melting_pot", "oven"});
		BakingAsserts.assertActionNotApplicable(move, state, new String[] {"human", "mixing_bowl_1", "oven"});
		
		int counterObjects = SpaceFactory.getContents(state.getObject("counter")).size();
		Assert.assertEquals(SpaceFactory.getContents(state.getObject("oven")).size(), 0);
		Assert.assertEquals(SpaceFactory.getContents(state.getObject("counter")).size(), counterObjects);
		state = move.performAction(state, new String[] {"human", "baking_pan", "oven"});
		Assert.assertEquals(ContainerFactory.getSpaceName(state.getObject("baking_pan")), "oven");
		Assert.assertEquals(SpaceFactory.getContents(state.getObject("counter")).size(), counterObjects-1);
		Assert.assertEquals(SpaceFactory.getContents(state.getObject("oven")).size(), 1);
		
		Assert.assertEquals(SpaceFactory.getContents(state.getObject("stove")).size(), 0);
		state = move.performAction(state, new String[] {"human", "melting_pot", "stove"});
		Assert.assertEquals(ContainerFactory.getSpaceName(state.getObject("melting_pot")), "stove");
		Assert.assertEquals(SpaceFactory.getContents(state.getObject("counter")).size(), counterObjects-2);
		Assert.assertEquals(SpaceFactory.getContents(state.getObject("stove")).size(), 1);
		
		state = move.performAction(state, new String[] {"human", "melting_pot", "counter"});
		state = move.performAction(state, new String[] {"human", "baking_pan", "counter"});
		Assert.assertEquals(SpaceFactory.getContents(state.getObject("stove")).size(), 0);
		Assert.assertEquals(SpaceFactory.getContents(state.getObject("oven")).size(), 0);
		Assert.assertEquals(SpaceFactory.getContents(state.getObject("counter")).size(), counterObjects);
	}
	
	@Test
	public void testMoveActionBake() {
		Recipe recipe = new Brownies();
		knowledgebase = new Knowledgebase();
		allIngredients = knowledgebase.getRecipeObjectInstanceList(domain, recipe);
		this.setUpState();
		Action move = new MoveAction(domain, topLevelIngredient);
		Action switch_a = new SwitchAction(domain);
		
		ObjectInstance oven = SpaceFactory.getNewBakingSpaceObjectInstance(domain, "oven", null, "");
		this.state = this.state.appendObject(oven);
		ObjectInstance bakingPan = ContainerFactory.getNewBakingContainerObjectInstance(domain, "baking_pan", null, "counter");
		this.state = this.state.appendObject(bakingPan);
		
		ObjectInstance brownies = IngredientFactory.getNewIngredientInstance(this.topLevelIngredient, "brownies", domain.getObjectClass(IngredientFactory.ClassNameComplex));
		//raw brownies
		brownies.setValue("baked", 0);
		this.state = this.state.appendObject(brownies);
		ContainerFactory.addIngredient(bakingPan, brownies.getName());
		
		
		//move into an oven that's turned off!
		state = move.performAction(state, new String[] {"human", "baking_pan", "oven"});
		state = move.performAction(state, new String[] {"human", "baking_pan", "counter"});
		BakingAsserts.assertIsNotBaked(state.getObject("brownies"));
		
		
		//turn oven on
		state = switch_a.performAction(state, new String[] {"human", "oven"});
		state = move.performAction(state, new String[] {"human", "baking_pan", "oven"});
		state = move.performAction(state, new String[] {"human", "baking_pan", "counter"});
		BakingAsserts.assertIsBaked(state.getObject("brownies"));
	}
	
	@Test
	public void testMoveActionMelt() {
		Recipe recipe = new Brownies();
		knowledgebase = new Knowledgebase();
		allIngredients = knowledgebase.getRecipeObjectInstanceList(domain, recipe);
		this.setUpState();
		Action move = new MoveAction(domain, topLevelIngredient);
		Action switchA = new SwitchAction(domain);
		
		ObjectInstance stove = SpaceFactory.getNewHeatingSpaceObjectInstance(domain, "stove", null, "");
		this.state = this.state.appendObject(stove);

		ObjectInstance melting_pot = ContainerFactory.getNewHeatingContainerObjectInstance(domain, "melting_pot", null, "counter");
		this.state = this.state.appendObject(melting_pot);
		
		ContainerFactory.addIngredient(melting_pot, "butter");
		
		
		BakingAsserts.assertIsNotHeated(state.getObject("butter"));
		state = move.performAction(state, new String[] {"human", "melting_pot", "stove"});
		state = move.performAction(state, new String[] {"human", "melting_pot", "counter"});
		BakingAsserts.assertIsNotHeated(state.getObject("butter"));
		
		state = switchA.performAction(state, new String[] {"human", "stove"});
		state = move.performAction(state, new String[] {"human", "melting_pot", "stove"});
		state = move.performAction(state, new String[] {"human", "melting_pot", "counter"});
		BakingAsserts.assertIsHeated(state.getObject("butter"));
	}
	
	@Test
	public void testPourAction() {
		Recipe recipe = new Brownies();
		knowledgebase = new Knowledgebase();
		allIngredients = knowledgebase.getRecipeObjectInstanceList(domain, recipe);
		this.setUpState();
		Action pour = new PourAction(domain, topLevelIngredient);
		
		//empty pouring container
		BakingAsserts.assertActionNotApplicable(pour, state, new String[] {"human", "mixing_bowl_1", "mixing_bowl_2"});
		
		//Check that pouring works how it is supposed to
		BakingAsserts.assertActionApplicable(pour, state, new String[] {"human", "flour_bowl", "mixing_bowl_1"});
		state = pour.performAction(state, new String[] {"human", "flour_bowl", "mixing_bowl_1"});
		BakingAsserts.assertContainerContains(state.getObject("mixing_bowl_1"), "flour");
		Assert.assertEquals(ContainerFactory.getContentNames(state.getObject("flour_bowl")).size(), 0);
		Assert.assertEquals(ContainerFactory.getContentNames(state.getObject("mixing_bowl_1")).size(), 1);
		
		//pouring into a non-mixing container
		BakingAsserts.assertActionNotApplicable(pour, state, new String[] {"human",  "mixing_bowl_1", "flour_bowl"});
		
		//pouring into a baking spce
		ObjectInstance oven = SpaceFactory.getNewBakingSpaceObjectInstance(domain, "oven", null, "");
		ObjectInstance bakingDish = ContainerFactory.getNewBakingContainerObjectInstance(domain, "baking_dish", null, "counter");
		this.state = this.state.appendObject(oven);
		this.state = this.state.appendObject(bakingDish);
		Action move = new MoveAction(domain, topLevelIngredient);
		state = move.performAction(state, new String[] {"human", "baking_dish", "oven"});
	
		BakingAsserts.assertActionNotApplicable(pour, state, new String[] {"human", "butter_bowl", "oven"});
	}
	
	@Test
	public void testPourToStove() {
		Recipe recipe = new Brownies();
		knowledgebase = new Knowledgebase();
		allIngredients = knowledgebase.getRecipeObjectInstanceList(domain, recipe);
		this.setUpState();
		Action pour = new PourAction(domain, topLevelIngredient);
		Action switchA = new SwitchAction(domain);
		Action move = new MoveAction(domain, topLevelIngredient);
		
		ObjectInstance stove = SpaceFactory.getNewHeatingSpaceObjectInstance(domain, "stove", null, "");
		ObjectInstance meltingPot = ContainerFactory.getNewHeatingContainerObjectInstance(domain, "melting_pot", null, "counter");
		this.state = this.state.appendObject(stove);
		this.state = this.state.appendObject(meltingPot);
		
		state = switchA.performAction(state, new String[] {"human", "stove"});
		state = move.performAction(state, new String[] {"human", "melting_pot", "stove"});
		state = pour.performAction(state, new String[] {"human", "butter_bowl", "melting_pot"});
		ObjectInstance butter = state.getObject("butter");
		BakingAsserts.assertIsHeated(butter);
	}
	
	@Test
	public void testSwitchAction() {
		Recipe recipe = new Brownies();
		knowledgebase = new Knowledgebase();
		allIngredients = knowledgebase.getRecipeObjectInstanceList(domain, recipe);
		this.setUpState();
		Action switchA = new SwitchAction(domain);
		
		ObjectInstance oven = SpaceFactory.getNewBakingSpaceObjectInstance(domain, "oven", null, "");
		ObjectInstance stove = SpaceFactory.getNewHeatingSpaceObjectInstance(domain, "stove", null, "");
		this.state = this.state.appendObject(stove);
		this.state = this.state.appendObject(oven);
		
		BakingAsserts.assertActionNotApplicable(switchA, state, new String[] {"human", "counter"});
		
		BakingAsserts.assertSpaceOff(state.getObject("oven"));
		BakingAsserts.assertSpaceOff(state.getObject("stove"));
		state = switchA.performAction(state, new String[] {"human", "oven"});
		state = switchA.performAction(state, new String[] {"human", "stove"});

		BakingAsserts.assertSpaceOn(state.getObject("oven"));
		BakingAsserts.assertSpaceOn(state.getObject("stove"));
		
		
	}
	
	@Test
	public void testSwitchActionBake() {
		Recipe recipe = new Brownies();
		knowledgebase = new Knowledgebase();
		allIngredients = knowledgebase.getRecipeObjectInstanceList(domain, recipe);
		this.setUpState();
		Action move = new MoveAction(domain, topLevelIngredient);
		Action switchA = new SwitchAction(domain);
		
		ObjectInstance oven = SpaceFactory.getNewBakingSpaceObjectInstance(domain, "oven", null, "");
		this.state = this.state.appendObject(oven);

		ObjectInstance bakingPan = ContainerFactory.getNewBakingContainerObjectInstance(domain, "baking_pan", null, "counter");
		this.state = this.state.appendObject(bakingPan);
		
		ObjectInstance brownies = IngredientFactory.getNewIngredientInstance(this.topLevelIngredient, "brownies", domain.getObjectClass(IngredientFactory.ClassNameComplex));
		//raw brownies
		brownies.setValue("baked", 0);
		this.state = this.state.appendObject(brownies);
		ContainerFactory.addIngredient(bakingPan, brownies.getName());
		
		
		//move into an oven that's turned off!
		BakingAsserts.assertIsNotBaked(state.getObject("brownies"));
		state = move.performAction(state, new String[] {"human", "baking_pan", "oven"});
		BakingAsserts.assertIsNotBaked(state.getObject("brownies"));
		//turn oven on
		state = switchA.performAction(state, new String[] {"human", "oven"});

		BakingAsserts.assertIsBaked(state.getObject("brownies"));
	}
	
	@Test
	public void testSwitchActionMelt() {
		Recipe recipe = new Brownies();
		knowledgebase = new Knowledgebase();
		allIngredients = knowledgebase.getRecipeObjectInstanceList(domain, recipe);
		this.setUpState();
		Action move = new MoveAction(domain, topLevelIngredient);
		Action switchA = new SwitchAction(domain);
		
		ObjectInstance stove = SpaceFactory.getNewHeatingSpaceObjectInstance(domain, "stove", null, "");
		this.state = this.state.appendObject(stove);

		ObjectInstance meltingPot = ContainerFactory.getNewHeatingContainerObjectInstance(domain, "melting_pot", null, "counter");
		this.state = this.state.appendObject(meltingPot);
		
		ContainerFactory.addIngredient(meltingPot, "butter");
		
		BakingAsserts.assertIsNotHeated(state.getObject("butter"));
		state = move.performAction(state, new String[] {"human", "melting_pot", "stove"});
		BakingAsserts.assertIsNotHeated(state.getObject("butter"));
		state = switchA.performAction(state, new String[] {"human", "stove"});
		BakingAsserts.assertIsHeated(state.getObject("butter"));
	}
	
	@Test
	public void testUseActionSimple() {
		Recipe recipe = new MashedPotatoes();
		knowledgebase = new Knowledgebase();
		allIngredients = knowledgebase.getRecipeObjectInstanceList(domain, recipe);
		this.setUpState();
		Action use = new UseAction(domain, topLevelIngredient);
		
		ObjectInstance peeler = ToolFactory.getNewSimpleToolObjectInstance(domain, "peeler", "peelable", "peeled", "counter");
		this.state = this.state.appendObject(peeler);
		ObjectInstance counter = state.getObject("counter");
		ObjectInstance newCounter = SpaceFactory.addContainer(counter, state.getObject("peeler"));
		this.state = this.state.replaceObject(counter, newCounter);
		// Can't peel a non-peelabled
		BakingAsserts.assertActionNotApplicable(use, state, new String[] {"human", "peeler", "butter_bowl"});
		
		// Can peel a peelable
		BakingAsserts.assertHasToolTrait(state.getObject("potatoes"), "peelable");
		BakingAsserts.assertDoesntHaveToolAttribute(state.getObject("potatoes"), "peeled");
		BakingAsserts.assertActionApplicable(use, state, new String[] {"human", "peeler", "potatoes_bowl"});

		state = use.performAction(state, new String[] {"human", "peeler", "potatoes_bowl"});
		
		// Can't peel a peeled ingredient
		BakingAsserts.assertHasToolAttribute(state.getObject("potatoes"), "peeled");
		BakingAsserts.assertActionNotApplicable(use, state, new String[] {"human", "peeler", "butter_bowl"});

	}
	
	@Test
	public void testToolCarryEmpty() {
		Recipe recipe = new MashedPotatoes();		
		knowledgebase = new Knowledgebase();
		state = knowledgebase.addTools(domain, state, SpaceFactory.SPACE_COUNTER);
		allIngredients = knowledgebase.getRecipeObjectInstanceList(domain, recipe);
		this.setUpState();
		Action use = new UseAction(domain, topLevelIngredient);
		
		ObjectInstance potatoes = state.getObject("potatoes");
		ObjectInstance strainablePotatoes = IngredientFactory.addToolTrait(potatoes, "strainable");
		this.state = this.state.replaceObject(potatoes, strainablePotatoes);
		
		state = use.performAction(state, new String[] {"human", "strainer", "potatoes_bowl"});
		ObjectInstance strainer = state.getObject("strainer");
		BakingAsserts.assertToolHasIngredient(strainer, "potatoes");
		BakingAsserts.assertContainerIsEmpty(state.getObject("potatoes_bowl"));
	}
	
	@Test
	public void testToolCarryInclude() {
		Recipe recipe = new MashedPotatoes();		
		knowledgebase = new Knowledgebase();
		state = knowledgebase.addTools(domain, state, SpaceFactory.SPACE_COUNTER);
		allIngredients = knowledgebase.getRecipeObjectInstanceList(domain, recipe);
		this.setUpState();
		Action use = new UseAction(domain, topLevelIngredient);
		Action pour = new PourAction(domain, topLevelIngredient);
		ObjectInstance fakeTool = ToolFactory.getNewCarryingToolObjectInstance(domain, "fakeTool", "fake",
				"faked", SpaceFactory.SPACE_COUNTER, Arrays.asList("liquid"), new HashSet<String>());
		this.state = this.state.appendObject(fakeTool);
		
		ObjectInstance water = state.getObject("water");
		ObjectInstance fakeWater = IngredientFactory.addToolTrait(water, "fake");
		this.state = this.state.replaceObject(water, fakeWater);
		
		state = pour.performAction(state, new String[] {"human", "water_bowl", "mixing_bowl_1"});
		state = pour.performAction(state, new String[] {"human", "potatoes_bowl", "mixing_bowl_1"});
		state = use.performAction(state, new String[] {"human", "fakeTool", "mixing_bowl_1"});
		
		
		ObjectInstance strainer = state.getObject("fakeTool");
		BakingAsserts.assertToolHasIngredient(strainer, "water");
		BakingAsserts.assertContainerContains(state.getObject("mixing_bowl_1"), "potatoes");
	}
	
	@Test
	public void testToolCarryIncludeNotApplicable() {
		Recipe recipe = new MashedPotatoes();
		knowledgebase = new Knowledgebase();
		state = knowledgebase.addTools(domain, state, SpaceFactory.SPACE_COUNTER);
		allIngredients = knowledgebase.getRecipeObjectInstanceList(domain, recipe);
		this.setUpState();
		Action use = new UseAction(domain, topLevelIngredient);
		Action pour = new PourAction(domain, topLevelIngredient);
		ObjectInstance fakeTool = ToolFactory.getNewCarryingToolObjectInstance(domain, "fakeTool", "fake",
				"faked", SpaceFactory.SPACE_COUNTER, Arrays.asList("liquid", "salt"), new HashSet<String>());
		this.state = this.state.appendObject(fakeTool);
		
		ObjectInstance water = state.getObject("water");
		ObjectInstance fakeWater = IngredientFactory.addToolTrait(water, "fake");
		this.state = this.state.replaceObject(water, fakeWater);
		
		state = pour.performAction(state, new String[] {"human", "water_bowl", "mixing_bowl_1"});
		state = pour.performAction(state, new String[] {"human", "potatoes_bowl", "mixing_bowl_1"});
		state = pour.performAction(state, new String[] {"human", "salt_bowl", "mixing_bowl_1"});
		// Fake tool includes only liquids and salts. But, only water has the fake tooltrait, so
		// fake tool should be able to do stuff to salt (since it is a salt, which is included for the tool)
		// but salt doesn't have the correct tool trait.
		BakingAsserts.assertActionNotApplicable(use, state, new String[] {"human", "fakeTool", "mixing_bowl_1"});
	}
	
	@Test
	public void testToolCarryExclude() {
		Recipe recipe = new MashedPotatoes();
		knowledgebase = new Knowledgebase();
		state = knowledgebase.addTools(domain, state, SpaceFactory.SPACE_COUNTER);
		allIngredients = knowledgebase.getRecipeObjectInstanceList(domain, recipe);
		this.setUpState();
		Action use = new UseAction(domain, topLevelIngredient);
		Action pour = new PourAction(domain, topLevelIngredient);
		
		ObjectInstance potatoes = state.getObject("potatoes");
		ObjectInstance strainablePotatoes = IngredientFactory.addToolTrait(potatoes, "strainable");
		this.state = this.state.replaceObject(potatoes, strainablePotatoes);
		
		state = pour.performAction(state, new String[] {"human", "water_bowl", "mixing_bowl_1"});
		state = pour.performAction(state, new String[] {"human", "potatoes_bowl", "mixing_bowl_1"});
		state = use.performAction(state, new String[] {"human", "strainer", "mixing_bowl_1"});
		
		
		ObjectInstance strainer = state.getObject("strainer");
		BakingAsserts.assertToolHasIngredient(strainer, "potatoes");
		BakingAsserts.assertContainerContains(state.getObject("mixing_bowl_1"), "water");
	}
	
	@Test
	public void testToolCarryExcludeNotApplicable() {
		Recipe recipe = new MashedPotatoes();
		knowledgebase = new Knowledgebase();
		state = knowledgebase.addTools(domain, state, SpaceFactory.SPACE_COUNTER);
		allIngredients = knowledgebase.getRecipeObjectInstanceList(domain, recipe);
		this.setUpState();
		Action use = new UseAction(domain, topLevelIngredient);
		Action pour = new PourAction(domain, topLevelIngredient);
		
		ObjectInstance potatoes = state.getObject("potatoes");
		ObjectInstance strainablePotatoes = IngredientFactory.addToolTrait(potatoes, "strainable");
		this.state = this.state.replaceObject(potatoes, strainablePotatoes);
		
		state = pour.performAction(state, new String[] {"human", "water_bowl", "mixing_bowl_1"});
		state = pour.performAction(state, new String[] {"human", "potatoes_bowl", "mixing_bowl_1"});
		state = pour.performAction(state, new String[] {"human", "salt_bowl", "mixing_bowl_1"});
		
		// Strainer would exclude all liquids (water), so it would work on salt and potatoes. But, only
		// potatoes are "strainable", so strainer can't be used on the bowl since salt should be strained,
		// but it doens't have the correct toolTrait.
		BakingAsserts.assertActionNotApplicable(use, state, new String[] {"human", "strainer", "mixing_bowl_1"});
	}

	
	@Test
	public void testToolCarryFull() {
		Recipe recipe = new MashedPotatoes();
		knowledgebase = new Knowledgebase();
		recipe.setUpSubgoals(domain);
		recipe.addIngredientSubgoals();
		allIngredients = knowledgebase.getRecipeObjectInstanceList(domain, recipe);
		state = knowledgebase.addTools(domain, state, SpaceFactory.SPACE_COUNTER);
		this.setUpState();
		Action use = new UseAction(domain, topLevelIngredient);
		Action pour = new PourAction(domain, topLevelIngredient);
		
		ObjectInstance potatoes = state.getObject("potatoes");
		ObjectInstance strainablePotatoes = IngredientFactory.addToolTrait(potatoes, "strainable");
		this.state = this.state.replaceObject(potatoes, strainablePotatoes);
		
		state = pour.performAction(state, new String[] {"human", "water_bowl", "mixing_bowl_1"});
		state = pour.performAction(state, new String[] {"human", "potatoes_bowl", "mixing_bowl_1"});
		state = use.performAction(state, new String[] {"human", "strainer", "mixing_bowl_1"});
		ObjectInstance strainer = state.getObject("strainer");
		BakingAsserts.assertToolHasIngredient(strainer, "potatoes");
		BakingAsserts.assertContainerContains(state.getObject("mixing_bowl_1"), "water");
		
		state = use.performAction(state, new String[] {"human", "strainer", "mixing_bowl_2"});
		BakingAsserts.assertToolIsEmpty(state.getObject("strainer"));
		BakingAsserts.assertContainerContains(state.getObject("mixing_bowl_2"), "potatoes");
	}
}
