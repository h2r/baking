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
}
