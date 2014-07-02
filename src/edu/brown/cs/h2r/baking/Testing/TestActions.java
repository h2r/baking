package edu.brown.cs.h2r.baking.Testing;

import static org.junit.Assert.*;

import java.util.Arrays;
import java.util.List;

import org.junit.*;
import org.junit.Test;

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
		setUpDomain();
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
	}
	
	@After
	public void tearDown() {
		knowledgebase = null;
		state = null;
		domain = null;
		allIngredients = null;
		topLevelIngredient = null;
	}
	
	/*@Test
	public void testMeltAction() {
		topLevelIngredient = new Brownies().topLevelIngredient;
		knowledgebase = new IngredientKnowledgebase();
		allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, topLevelIngredient);
		setUpState();
		Action melt = new MeltAction(domain, topLevelIngredient);
		
		// Can't melt an empty bowl
		BakingAsserts.assertActionNotApplicable(melt, state, new String[] {"human", "mixing_bowl_1"});
		
		// can't melt a non-meltable ingredient
		ObjectInstance olive_oil = state.getObject("olive_oil");
		BakingAsserts.assertHasTrait(olive_oil, IngredientKnowledgebase.NONMELTABLE);
		BakingAsserts.assertIsNotMelted(olive_oil);
		BakingAsserts.assertActionNotApplicable(melt, state, new String[] {"human", "olive_oil_bowl"});
		
		
		// can melt an ingredient
		ObjectInstance butter = state.getObject("butter");
		BakingAsserts.assertIsNotMelted(butter);
		((MeltAction)melt).melt(state, state.getObject(IngredientFactory.getContainer(butter)));
		BakingAsserts.assertIsMelted(butter);
		
		// can't melt a melted ingredient
		BakingAsserts.assertActionNotApplicable(melt, state, new String[] {"human", "butter_bowl"});
		BakingAsserts.assertIsMelted(butter);
	}*/
	
	@Test
	public void testMixAction() {
		topLevelIngredient = new Brownies().topLevelIngredient;
		knowledgebase = new IngredientKnowledgebase();
		allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, topLevelIngredient);
		knowledgebase.newCombinationMap("IngredientCombinations.txt");
		allIngredients.add(IngredientFactory.getNewIngredientInstance(knowledgebase.
				getIngredient("orange_juice"), "orange_juice", domain.getObjectClass("simple_ingredient")));
		setUpState();
		Action mix = new MixAction(domain, topLevelIngredient);
		((MixAction)mix).changeKnowledgebase(knowledgebase);
		Action pour = new PourAction(domain, topLevelIngredient);
		
		// can't mix an empty bowl
		assertEquals(ContainerFactory.getContentNames(state.getObject("mixing_bowl_1")).size(), 0);
		BakingAsserts.assertActionNotApplicable(mix, state, new String[] {"human", "mixing_bowl_1"});
		
		// can't mix a bowl with just 1 ingredient
		((PourAction)pour).pour(state, "flour_bowl", "mixing_bowl_1");
		assertEquals(ContainerFactory.getContentNames(state.getObject("mixing_bowl_1")).size(), 1);
		BakingAsserts.assertActionNotApplicable(mix, state, new String[] {"human", "mixing_bowl_1"});

		// can't mix a non-mixing bowl
		BakingAsserts.assertActionNotApplicable(mix, state, new String[] {"human", "cocoa_bowl"});
		
		// Mix a bowl with two random (non-combinable) ingredients
		((PourAction)pour).pour(state, "cocoa_bowl", "mixing_bowl_2");
		((PourAction)pour).pour(state, "baking_powder_bowl", "mixing_bowl_2");
		assertEquals(ContainerFactory.getContentNames(state.getObject("mixing_bowl_2")).size(), 2);
		BakingAsserts.assertActionApplicable(mix, state, new String[] {"human", "mixing_bowl_2"});
		((MixAction)mix).mix(state, "mixing_bowl_2");
		assertEquals(ContainerFactory.getContentNames(state.getObject("mixing_bowl_2")).size(), 1);
		ObjectInstance named_ing = null;
		for (String name : ContainerFactory.getContentNames(state.getObject("mixing_bowl_2"))) {
			named_ing = state.getObject(name);
			break;
		}
		BakingAsserts.assertIngredientContains(named_ing, Arrays.asList("cocoa", "baking_powder"));
		
		
		// TODO: Test the swapping out here.
		((PourAction)pour).pour(state, "orange_juice_bowl", "mixing_bowl_1");
		assertEquals(ContainerFactory.getContentNames(state.getObject("mixing_bowl_1")).size(), 2);
		BakingAsserts.assertActionApplicable(mix, state, new String[] {"human", "mixing_bowl_1"});
		((MixAction)mix).mix(state, "mixing_bowl_1");
		assertEquals(ContainerFactory.getContentNames(state.getObject("mixing_bowl_1")).size(), 1);
		BakingAsserts.assertContainerContains(state.getObject("mixing_bowl_1"), "dough");
	}
	
	@Test
	//TODO: Write tests?
	public void testMoveAction() {	
		topLevelIngredient = new Brownies().topLevelIngredient;
		knowledgebase = new IngredientKnowledgebase();
		allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, topLevelIngredient);
		setUpState();
		Action move = new MoveAction(domain, topLevelIngredient);
		
		ObjectInstance oven = SpaceFactory.getNewBakingSpaceObjectInstance(domain, "oven", null, "");
		ObjectInstance stove = SpaceFactory.getNewHeatingSpaceObjectInstance(domain, "stove", null, "");
		state.addObject(oven);
		state.addObject(stove);

		ObjectInstance melting_pot = ContainerFactory.getNewHeatingContainerObjectInstance(domain, "melting_pot", null, "counter");
		ObjectInstance baking_pan = ContainerFactory.getNewBakingContainerObjectInstance(domain, "baking_pan", null, "counter");
		state.addObject(melting_pot);
		state.addObject(baking_pan);
		
		BakingAsserts.assertActionNotApplicable(move, state, new String[] {"human", "baking_pan", "stove"});
		BakingAsserts.assertActionNotApplicable(move, state, new String[] {"human", "mixing_bowl_1", "stove"});
		BakingAsserts.assertActionNotApplicable(move, state, new String[] {"human", "melting_pot", "oven"});
		BakingAsserts.assertActionNotApplicable(move, state, new String[] {"human", "mixing_bowl_1", "oven"});
		
		assertEquals(SpaceFactory.getContents(oven).size(), 0);
		((MoveAction)move).move(state, baking_pan, oven);
		assertEquals(ContainerFactory.getSpaceName(baking_pan), "oven");
		assertEquals(SpaceFactory.getContents(oven).size(), 1);
		
		assertEquals(SpaceFactory.getContents(stove).size(), 0);
		((MoveAction)move).move(state, melting_pot, stove);
		assertEquals(ContainerFactory.getSpaceName(melting_pot), "stove");
		assertEquals(SpaceFactory.getContents(stove).size(), 1);
	}
	
	@Test
	public void testMoveActionBake() {
		topLevelIngredient = new Brownies().topLevelIngredient;
		knowledgebase = new IngredientKnowledgebase();
		allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, topLevelIngredient);
		setUpState();
		Action move = new MoveAction(domain, topLevelIngredient);
		Action switch_a = new SwitchAction(domain);
		
		ObjectInstance oven = SpaceFactory.getNewBakingSpaceObjectInstance(domain, "oven", null, "");
		state.addObject(oven);

		ObjectInstance baking_pan = ContainerFactory.getNewBakingContainerObjectInstance(domain, "baking_pan", null, "counter");
		state.addObject(baking_pan);
		
		ObjectInstance brownies = IngredientFactory.getNewIngredientInstance(this.topLevelIngredient, "brownies", domain.getObjectClass(IngredientFactory.ClassNameComplex));
		//raw brownies
		brownies.setValue("baked", 0);
		state.addObject(brownies);
		ContainerFactory.addIngredient(baking_pan, brownies.getName());
		
		
		//move into an oven that's turned off!
		((MoveAction)move).move(state, baking_pan, oven);
		((MoveAction)move).move(state, baking_pan, state.getObject("counter"));
		BakingAsserts.assertIsNotBaked(brownies);
		
		
		//turn oven on
		((SwitchAction)switch_a).switchOnOff(state, oven);
		((MoveAction)move).move(state, baking_pan, oven);
		((MoveAction)move).move(state, baking_pan, state.getObject("counter"));
		BakingAsserts.assertIsBaked(brownies);
	}
	
	@Test
	public void testMoveActionMelt() {
		topLevelIngredient = new Brownies().topLevelIngredient;
		knowledgebase = new IngredientKnowledgebase();
		allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, topLevelIngredient);
		setUpState();
		Action move = new MoveAction(domain, topLevelIngredient);
		Action switch_a = new SwitchAction(domain);
		
		ObjectInstance stove = SpaceFactory.getNewHeatingSpaceObjectInstance(domain, "stove", null, "");
		state.addObject(stove);

		ObjectInstance melting_pot = ContainerFactory.getNewHeatingContainerObjectInstance(domain, "melting_pot", null, "counter");
		state.addObject(melting_pot);
		
		ContainerFactory.addIngredient(melting_pot, "butter");
		
		ObjectInstance butter = state.getObject("butter");
		
		BakingAsserts.assertIsNotMelted(butter);
		((MoveAction)move).move(state, melting_pot, stove);
		((MoveAction)move).move(state, melting_pot, state.getObject("counter"));
		BakingAsserts.assertIsNotMelted(butter);
		
		((SwitchAction)switch_a).switchOnOff(state, stove);
		((MoveAction)move).move(state, melting_pot, stove);
		((MoveAction)move).move(state, melting_pot, state.getObject("counter"));
		BakingAsserts.assertIsMelted(butter);
	}
	
	@Test
	public void testPeelAction() {
		topLevelIngredient = new MashedPotatoes().topLevelIngredient;
		knowledgebase = new IngredientKnowledgebase();
		allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, topLevelIngredient);
		setUpState();
		Action peel = new PeelAction(domain, topLevelIngredient);
		
		// can melt an ingredient
		ObjectInstance potatoes = state.getObject("potatoes");
		BakingAsserts.assertIsNotPeeled(potatoes);
		((PeelAction)peel).peel(potatoes);
		BakingAsserts.assertIsPeeled(potatoes);
		
		// can't melt a melted ingredient
		BakingAsserts.assertActionNotApplicable(peel, state, new String[] {"human", "potatoes"});
		BakingAsserts.assertIsPeeled(potatoes);
	}
	
	/*@Test
	public void testBakeAction() {
		topLevelIngredient = new Brownies().topLevelIngredient;
		knowledgebase = new IngredientKnowledgebase();
		allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, topLevelIngredient);
		setUpState();
		Action bake = new BakeAction(domain, topLevelIngredient);
		
		// can melt an ingredient
		ObjectInstance brownies = IngredientFactory.getNewIngredientInstance(topLevelIngredient, 
				"brownies", domain.getObjectClass("complex_ingredient"));
		state.addObject(brownies);
		BakingAsserts.assertIsNotBaked(brownies);
		((BakeAction)bake).bake(state, brownies);
		BakingAsserts.assertIsBaked(brownies);
		
		// can't melt a melted ingredient
		BakingAsserts.assertActionNotApplicable(bake, state, new String[] {"human", "brownies"});
		BakingAsserts.assertIsBaked(brownies);
	}*/
	
	@Test
	public void testPourAction() {
		topLevelIngredient = new Brownies().topLevelIngredient;
		knowledgebase = new IngredientKnowledgebase();
		allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, topLevelIngredient);
		setUpState();
		Action pour = new PourAction(domain, topLevelIngredient);
		
		//empty pouring container
		BakingAsserts.assertActionNotApplicable(pour, state, new String[] {"human", "mixing_bowl_1", "mixing_bowl_2"});
		
		//Check that pouring works how it is supposed to
		BakingAsserts.assertActionApplicable(pour, state, new String[] {"human", "flour_bowl", "mixing_bowl_1"});
		((PourAction)pour).pour(state, "flour_bowl", "mixing_bowl_1");
		BakingAsserts.assertContainerContains(state.getObject("mixing_bowl_1"), "flour");
		assertEquals(ContainerFactory.getContentNames(state.getObject("flour_bowl")).size(), 0);
		assertEquals(ContainerFactory.getContentNames(state.getObject("mixing_bowl_1")).size(), 1);
		
		//pouring into a non-mixing container
		BakingAsserts.assertActionNotApplicable(pour, state, new String[] {"human",  "mixing_bowl_1", "flour_bowl"});
		
	}
	
	@Test
	public void testSwitchAction() {
		topLevelIngredient = new Brownies().topLevelIngredient;
		knowledgebase = new IngredientKnowledgebase();
		allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, topLevelIngredient);
		setUpState();
		Action move = new MoveAction(domain, topLevelIngredient);
		Action switch_a = new SwitchAction(domain);
		
		ObjectInstance oven = SpaceFactory.getNewBakingSpaceObjectInstance(domain, "oven", null, "");
		ObjectInstance stove = SpaceFactory.getNewHeatingSpaceObjectInstance(domain, "stove", null, "");
		state.addObject(oven);
		state.addObject(stove);
		
		BakingAsserts.assertActionNotApplicable(switch_a, state, new String[] {"human", "counter"});
		
		BakingAsserts.assertSpaceOff(oven);
		BakingAsserts.assertSpaceOff(stove);
		((SwitchAction)switch_a).switchOnOff(state, oven);
		((SwitchAction)switch_a).switchOnOff(state, stove);
		BakingAsserts.assertSpaceOn(oven);
		BakingAsserts.assertSpaceOn(stove);
		
		
	}
	
	@Test
	public void testSwitchActionBake() {
		topLevelIngredient = new Brownies().topLevelIngredient;
		knowledgebase = new IngredientKnowledgebase();
		allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, topLevelIngredient);
		setUpState();
		Action move = new MoveAction(domain, topLevelIngredient);
		Action switch_a = new SwitchAction(domain);
		
		ObjectInstance oven = SpaceFactory.getNewBakingSpaceObjectInstance(domain, "oven", null, "");
		state.addObject(oven);

		ObjectInstance baking_pan = ContainerFactory.getNewBakingContainerObjectInstance(domain, "baking_pan", null, "counter");
		state.addObject(baking_pan);
		
		ObjectInstance brownies = IngredientFactory.getNewIngredientInstance(this.topLevelIngredient, "brownies", domain.getObjectClass(IngredientFactory.ClassNameComplex));
		//raw brownies
		brownies.setValue("baked", 0);
		state.addObject(brownies);
		ContainerFactory.addIngredient(baking_pan, brownies.getName());
		
		
		//move into an oven that's turned off!
		BakingAsserts.assertIsNotBaked(brownies);
		((MoveAction)move).move(state, baking_pan, oven);
		BakingAsserts.assertIsNotBaked(brownies);
		//turn oven on
		((SwitchAction)switch_a).switchOnOff(state, oven);
		BakingAsserts.assertIsBaked(brownies);
	}
	
	@Test
	public void testSwitchActionMelt() {
		topLevelIngredient = new Brownies().topLevelIngredient;
		knowledgebase = new IngredientKnowledgebase();
		allIngredients = knowledgebase.getPotentialIngredientObjectInstanceList(state, domain, topLevelIngredient);
		setUpState();
		Action move = new MoveAction(domain, topLevelIngredient);
		Action switch_a = new SwitchAction(domain);
		
		ObjectInstance stove = SpaceFactory.getNewHeatingSpaceObjectInstance(domain, "stove", null, "");
		state.addObject(stove);

		ObjectInstance melting_pot = ContainerFactory.getNewHeatingContainerObjectInstance(domain, "melting_pot", null, "counter");
		state.addObject(melting_pot);
		
		ContainerFactory.addIngredient(melting_pot, "butter");
		
		ObjectInstance butter = state.getObject("butter");
		
		BakingAsserts.assertIsNotMelted(butter);
		((MoveAction)move).move(state, melting_pot, stove);
		BakingAsserts.assertIsNotMelted(butter);
		((SwitchAction)switch_a).switchOnOff(state, stove);
		BakingAsserts.assertIsMelted(butter);
	}
}
