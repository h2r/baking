package edu.brown.cs.h2r.baking.Testing;

import org.junit.Assert;

import java.util.Arrays;
import java.util.List;

import org.junit.*;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.SADomain;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.IngredientKnowledgebase;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.SpaceFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ToolFactory;
import edu.brown.cs.h2r.baking.Recipes.*;
import edu.brown.cs.h2r.baking.actions.*;

public class TestActions {

	IngredientKnowledgebase knowledgebase;
	State state;
	Domain domain;
	List<ObjectInstance> allIngredients;
	IngredientRecipe topLevelIngredient;
	
	@Before
	public void setUp() {
		domain = new SADomain();
		this.setUpDomain();
		state = new State();
		state.addObject(AgentFactory.getNewHumanAgentObjectInstance(domain, "human"));
		List<String> containers = Arrays.asList("mixing_bowl_1", "mixing_bowl_2");
		state.addObject(SpaceFactory.getNewWorkingSpaceObjectInstance(domain, "counter", containers, "human"));

		for (String container : containers) { 
			state.addObject(ContainerFactory.getNewMixingContainerObjectInstance(domain, container, null, "counter"));
		}
	}
	
	public void setUpState() {
		ObjectClass containerClass = domain.getObjectClass(ContainerFactory.ClassName);		
		ObjectInstance counterSpace = state.getObject("counter");

		List<ObjectInstance> ingredientInstances = allIngredients;
		List<ObjectInstance> containerInstances = Recipe.getContainers(containerClass, ingredientInstances, counterSpace.getName());
		
		
		for (ObjectInstance ingredientInstance : ingredientInstances) {
			if (state.getObject(ingredientInstance.getName()) == null) {
				state.addObject(ingredientInstance);
			}
		}
		
		for (ObjectInstance containerInstance : containerInstances) {
			if (state.getObject(containerInstance.getName()) == null) {
				ContainerFactory.changeContainerSpace(containerInstance, counterSpace.getName());
				state.addObject(containerInstance);
			}
		}
		
		for (ObjectInstance ingredientInstance : ingredientInstances) {
			if (IngredientFactory.getUseCount(ingredientInstance) >= 1) {
				ObjectInstance ing = state.getObject(ingredientInstance.getName());
				IngredientFactory.changeIngredientContainer(ing, ing.getName()+"_bowl");
				ContainerFactory.addIngredient(state.getObject(ing.getName()+"_bowl"), ing.getName());
			}
		}
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
		topLevelIngredient = new Brownies().topLevelIngredient;
		knowledgebase = new IngredientKnowledgebase();
		allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, topLevelIngredient);
		knowledgebase.newCombinationMap("IngredientCombinations.txt");
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
	}
	
	@Test
	public void testMoveAction() {	
		topLevelIngredient = new Brownies().topLevelIngredient;
		knowledgebase = new IngredientKnowledgebase();
		allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, topLevelIngredient);
		this.setUpState();
		Action move = new MoveAction(domain, topLevelIngredient);
		
		ObjectInstance oven = SpaceFactory.getNewBakingSpaceObjectInstance(domain, "oven", null, "");
		ObjectInstance stove = SpaceFactory.getNewHeatingSpaceObjectInstance(domain, "stove", null, "");
		ObjectInstance counter = state.getObject("counter");
		state.addObject(oven);
		state.addObject(stove);

		ObjectInstance meltingPot = ContainerFactory.getNewHeatingContainerObjectInstance(domain, "melting_pot", null, "counter");
		ObjectInstance bakingPan = ContainerFactory.getNewBakingContainerObjectInstance(domain, "baking_pan", null, "counter");
		state.addObject(meltingPot);
		state.addObject(bakingPan);
		SpaceFactory.addContainer(counter, meltingPot);
		SpaceFactory.addContainer(counter, bakingPan);
		
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
		topLevelIngredient = new Brownies().topLevelIngredient;
		knowledgebase = new IngredientKnowledgebase();
		allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, topLevelIngredient);
		this.setUpState();
		Action move = new MoveAction(domain, topLevelIngredient);
		Action switch_a = new SwitchAction(domain);
		
		ObjectInstance oven = SpaceFactory.getNewBakingSpaceObjectInstance(domain, "oven", null, "");
		state.addObject(oven);

		ObjectInstance bakingPan = ContainerFactory.getNewBakingContainerObjectInstance(domain, "baking_pan", null, "counter");
		state.addObject(bakingPan);
		
		ObjectInstance brownies = IngredientFactory.getNewIngredientInstance(this.topLevelIngredient, "brownies", domain.getObjectClass(IngredientFactory.ClassNameComplex));
		//raw brownies
		brownies.setValue("baked", 0);
		state.addObject(brownies);
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
		topLevelIngredient = new Brownies().topLevelIngredient;
		knowledgebase = new IngredientKnowledgebase();
		allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, topLevelIngredient);
		this.setUpState();
		Action move = new MoveAction(domain, topLevelIngredient);
		Action switchA = new SwitchAction(domain);
		
		ObjectInstance stove = SpaceFactory.getNewHeatingSpaceObjectInstance(domain, "stove", null, "");
		state.addObject(stove);

		ObjectInstance melting_pot = ContainerFactory.getNewHeatingContainerObjectInstance(domain, "melting_pot", null, "counter");
		state.addObject(melting_pot);
		
		ContainerFactory.addIngredient(melting_pot, "butter");
		
		
		BakingAsserts.assertIsNotMelted(state.getObject("butter"));
		state = move.performAction(state, new String[] {"human", "melting_pot", "stove"});
		state = move.performAction(state, new String[] {"human", "melting_pot", "counter"});
		BakingAsserts.assertIsNotMelted(state.getObject("butter"));
		
		state = switchA.performAction(state, new String[] {"human", "stove"});
		state = move.performAction(state, new String[] {"human", "melting_pot", "stove"});
		state = move.performAction(state, new String[] {"human", "melting_pot", "counter"});
		BakingAsserts.assertIsMelted(state.getObject("butter"));
	}
	
	@Test
	public void testPourAction() {
		topLevelIngredient = new Brownies().topLevelIngredient;
		knowledgebase = new IngredientKnowledgebase();
		allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, topLevelIngredient);
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
	}
	
	@Test
	public void testSwitchAction() {
		topLevelIngredient = new Brownies().topLevelIngredient;
		knowledgebase = new IngredientKnowledgebase();
		allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, topLevelIngredient);
		this.setUpState();
		Action switchA = new SwitchAction(domain);
		
		ObjectInstance oven = SpaceFactory.getNewBakingSpaceObjectInstance(domain, "oven", null, "");
		ObjectInstance stove = SpaceFactory.getNewHeatingSpaceObjectInstance(domain, "stove", null, "");
		state.addObject(oven);
		state.addObject(stove);
		
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
		topLevelIngredient = new Brownies().topLevelIngredient;
		knowledgebase = new IngredientKnowledgebase();
		allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, topLevelIngredient);
		this.setUpState();
		Action move = new MoveAction(domain, topLevelIngredient);
		Action switchA = new SwitchAction(domain);
		
		ObjectInstance oven = SpaceFactory.getNewBakingSpaceObjectInstance(domain, "oven", null, "");
		state.addObject(oven);

		ObjectInstance bakingPan = ContainerFactory.getNewBakingContainerObjectInstance(domain, "baking_pan", null, "counter");
		state.addObject(bakingPan);
		
		ObjectInstance brownies = IngredientFactory.getNewIngredientInstance(this.topLevelIngredient, "brownies", domain.getObjectClass(IngredientFactory.ClassNameComplex));
		//raw brownies
		brownies.setValue("baked", 0);
		state.addObject(brownies);
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
		topLevelIngredient = new Brownies().topLevelIngredient;
		knowledgebase = new IngredientKnowledgebase();
		allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, topLevelIngredient);
		this.setUpState();
		Action move = new MoveAction(domain, topLevelIngredient);
		Action switchA = new SwitchAction(domain);
		
		ObjectInstance stove = SpaceFactory.getNewHeatingSpaceObjectInstance(domain, "stove", null, "");
		state.addObject(stove);

		ObjectInstance meltingPot = ContainerFactory.getNewHeatingContainerObjectInstance(domain, "melting_pot", null, "counter");
		state.addObject(meltingPot);
		
		ContainerFactory.addIngredient(meltingPot, "butter");
		
		BakingAsserts.assertIsNotMelted(state.getObject("butter"));
		state = move.performAction(state, new String[] {"human", "melting_pot", "stove"});
		BakingAsserts.assertIsNotMelted(state.getObject("butter"));
		state = switchA.performAction(state, new String[] {"human", "stove"});
		BakingAsserts.assertIsMelted(state.getObject("butter"));
	}
	
	@Test
	public void testUseAction() {
		topLevelIngredient = new MashedPotatoes().topLevelIngredient;
		knowledgebase = new IngredientKnowledgebase();
		allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, topLevelIngredient);
		this.setUpState();
		Action use = new UseAction(domain, topLevelIngredient);
		
		ObjectInstance peeler = ToolFactory.getNewSimpleToolObjectInstance(domain, "peeler", "peelable", "peeled", "counter");
		state.addObject(peeler);
		SpaceFactory.addContainer(state.getObject("counter"), state.getObject("peeler"));
		
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
}
