
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.SADomain;


public class Test {

	public static int testSuccess(State state, Recipe recipe, ObjectInstance obj, Boolean expectedResult)
	{
		if (recipe.isSuccess(state, obj) != expectedResult)
		{
			Boolean actualResult = recipe.isSuccess(state, obj);
			System.out.println("Test failed checking equality between " + recipe.toString() + " and object " + obj.toString());
			System.out.println("Expect: " + expectedResult.toString() + " Actual: " + actualResult.toString());
			return 1;
		}
		return 0;
	}
	
	public static int testFailure(State state, Recipe recipe, ObjectInstance obj, Boolean expectedResult)
	{
		if (recipe.isFailure(state, obj) != expectedResult)
		{
			Boolean actualResult = recipe.isFailure(state, obj);
			System.out.println("Test failed checking if " + recipe.toString() + " contains " + obj.toString());
			System.out.println("Expect: " + expectedResult.toString() + " Actual: " + actualResult.toString());
			return 1;
		}
		return 0;
	}
	
	public static void main(String[] args)
	{
		SingleAgentKitchen kitchen = new SingleAgentKitchen();
		Domain domain = kitchen.generateDomain();
		State state = SingleAgentKitchen.getOneAgent(domain);
		
		ObjectClass simpleClass = IngredientFactory.createSimpleIngredientObjectClass(domain);
		ObjectClass complexClass = IngredientFactory.createComplexIngredientObjectClass(domain);
		
		ObjectInstance s1 = IngredientFactory.getNewSimpleIngredientObjectInstance(simpleClass, "s1", false, false, false, "");
		ObjectInstance s2 = IngredientFactory.getNewSimpleIngredientObjectInstance(simpleClass, "s2", false, false, false, "");
		ObjectInstance s3 = IngredientFactory.getNewSimpleIngredientObjectInstance(simpleClass, "s1", false, false, false, "");
		ObjectInstance s4 = IngredientFactory.getNewSimpleIngredientObjectInstance(simpleClass, "s4", false, false, false, "");
		ObjectInstance s5 = IngredientFactory.getNewSimpleIngredientObjectInstance(simpleClass, "s5", false, false, false, "");
		state.addObject(s1);
		state.addObject(s2);
		state.addObject(s3);
		state.addObject(s4);
		state.addObject(s5);
		
		ObjectInstance p1 = 
				IngredientFactory.getNewComplexIngredientObjectInstance(
						complexClass, "p1", false, false, false, "", Arrays.asList("s1", "s2"));
		ObjectInstance p2 = 
				IngredientFactory.getNewComplexIngredientObjectInstance(
						complexClass, "p2", false, false, false, "", Arrays.asList("s1", "s3"));
		ObjectInstance p3 = 
				IngredientFactory.getNewComplexIngredientObjectInstance(
						complexClass, "p3", false, false, false, "", Arrays.asList("s1", "s2", "s4"));
		state.addObject(p1);
		state.addObject(p2);
		state.addObject(p3);
		TestRecipe r1 = new TestRecipe("s1", "s2");
		TestRecipe r2 = new TestRecipe("s2", "s3");
		TestRecipe r3 = new TestRecipe("s1", "s2", "s3");
		TestRecipe r4 = new TestRecipe("s1", "s2", "s4");
		
		int failures = 0;
		failures += Test.testSuccess(state, r1, p1, true);
		failures += Test.testSuccess(state, r1, p2, false);
		failures += Test.testSuccess(state, r2, p1, false);
		failures += Test.testSuccess(state, r2, p2, false);
		failures += Test.testSuccess(state, r3, p1, false);
		failures += Test.testSuccess(state, r3, p2, false);
		
		failures += Test.testFailure(state, r1, p1, false);
		//failures += Test.testFailure(state, r1, p2, true);
		//failures += Test.testFailure(state, r2, p1, true);
		//failures += Test.testFailure(state, r2, p2, true);
		//failures += Test.testFailure(state, r3, p1, true);
		//failures += Test.testFailure(state, r3, p2, true);
		failures += Test.testFailure(state, r4, p1, false);
		failures += Test.testFailure(state, r4, p2, true);
		failures += Test.testFailure(state, r1, p3, true);
		failures += Test.testFailure(state, r2, p3, true);
		failures += Test.testFailure(state, r4, p3, false);
		
		
		
		/*
		SimpleIngredientInstance s4 = new SimpleIngredientInstance(ingredientClass, "s4");
		SimpleIngredientInstance s5 = new SimpleIngredientInstance(ingredientClass, "s5");
		
		List<IngredientInstance> ingredients = new ArrayList<IngredientInstance>();
		ingredients.add(s1);
		ingredients.add(s2);
		ingredients.add(s4);
		ComplexIngredientInstance p1 = new ProducedIngredient(ingredientClass, "p1", ingredients);
		
		ComplexIngredientInstance p2 = new ProducedIngredient(ingredientClass, "p2", ingredients);
		
		List<IngredientInstance> ingredients2 = new ArrayList<IngredientInstance>();
		ingredients2.add(s4);
		ingredients2.add(s2);
		ingredients2.add(s1);
		
		ComplexIngredientInstance p3 = new ProducedIngredient(ingredientClass, "p3", ingredients2);
		
		failures += Test.testSuccess(p1, p2, true);
		failures += Test.testSuccess(p1, p3, true);
		failures += Test.testSuccess(p2, p3, true);
		
		List<IngredientInstance> ingredients3 = new ArrayList<IngredientInstance>();
		ingredients3.add(s1);
		ingredients3.add(s2);
		ingredients3.add(s5);
		
		ComplexIngredientInstance p4 = new ProducedIngredient(ingredientClass, "p4", ingredients3);
		
		failures += Test.testSuccess(p1, p4, false);
		failures += Test.testSuccess(p2, p4, false);
		failures += Test.testSuccess(p3, p4, false);
		
		List<IngredientInstance> ingredients4 = new ArrayList<IngredientInstance>();
		ingredients4.add(s1);
		ingredients4.add(s2);
		ingredients4.add(s4);
		ingredients4.add(s5);
		
		ComplexIngredientInstance p5 = new ProducedIngredient(ingredientClass, "p5", ingredients4);
		SimpleIngredientInstance s6 = new SimpleIngredientInstance(ingredientClass, "s6");
		List<IngredientInstance> ingredients5 = new ArrayList<IngredientInstance>(ingredients);
		ingredients5.add(s6);
		ComplexIngredientInstance p6 = new ProducedIngredient(ingredientClass, "p6", ingredients5);
		Recipe testRecipe = new TestRecipe();

		failures += Test.testSuccess(testRecipe, p1, false);
		failures += Test.testSuccess(testRecipe, p3, false);
		failures += Test.testSuccess(testRecipe, p5, true);
		failures += Test.testSuccess(testRecipe,  p6,  false);
		
		failures += Test.testContains(testRecipe, p1, true);
		failures += Test.testContains(testRecipe, p2, true);
		failures += Test.testContains(testRecipe, p5, true);
		failures += Test.testContains(testRecipe, p6, false);
		
		
		failures += Test.testContains(p5, p1, true);
		failures += Test.testContains(p1, p2, true);
		failures += Test.testContains(p1, p5, false);
		failures += Test.testContains(p3, p5, false);
		
		*/
		System.out.println(failures + " failures");
	}
	
	
}
