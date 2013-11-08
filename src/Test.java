import java.util.ArrayList;
import java.util.List;

import burlap.oomdp.core.Domain;
import burlap.oomdp.singleagent.SADomain;


public class Test {

	public static int testEquality(Object ob1, Object obj2, Boolean expectedResult)
	{
		if ((ob1.equals(obj2)) != expectedResult)
		{
			Boolean actualResult = (ob1.equals(obj2));
			System.out.println("Test failed checking equality between " + ob1.toString() + " and object " + obj2.toString());
			System.out.println("Expect: " + expectedResult.toString() + " Actual: " + actualResult.toString());
			return 1;
		}
		return 0;
	}
	
	public static int testContains(ComplexIngredientInstance goal, IngredientInstance ingredient, Boolean expectedResult)
	{
		if (goal.contains(ingredient) != expectedResult)
		{
			Boolean actualResult = (goal.contains(ingredient));
			System.out.println("Test failed checking if " + goal.toString() + " contains " + ingredient.toString());
			System.out.println("Expect: " + expectedResult.toString() + " Actual: " + actualResult.toString());
			return 1;
		}
		return 0;
	}
	 
	public static void main(String[] args)
	{
		Domain domain = new SADomain();
		IngredientClass ingredientClass = new IngredientClass(domain);
		SimpleIngredientInstance s1 = new SimpleIngredientInstance(ingredientClass, "s1");
		SimpleIngredientInstance s2 = new SimpleIngredientInstance(ingredientClass, "s2");
		SimpleIngredientInstance s3 = new SimpleIngredientInstance(ingredientClass, "s1");
		
		int failures = 0;
		failures += Test.testEquality(s1, s2, false);
		failures += Test.testEquality(s1, s3, true);
		failures += Test.testEquality(s2, s3, false);
		
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
		
		failures += Test.testEquality(p1, p2, true);
		failures += Test.testEquality(p1, p3, true);
		failures += Test.testEquality(p2, p3, true);
		
		List<IngredientInstance> ingredients3 = new ArrayList<IngredientInstance>();
		ingredients3.add(s1);
		ingredients3.add(s2);
		ingredients3.add(s5);
		
		ComplexIngredientInstance p4 = new ProducedIngredient(ingredientClass, "p4", ingredients3);
		
		failures += Test.testEquality(p1, p4, false);
		failures += Test.testEquality(p2, p4, false);
		failures += Test.testEquality(p3, p4, false);
		
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
		Recipe testRecipe = new TestRecipe(ingredientClass, "testRecipe");

		failures += Test.testEquality(testRecipe, p1, false);
		failures += Test.testEquality(testRecipe, p3, false);
		failures += Test.testEquality(testRecipe, p5, true);
		failures += Test.testEquality(testRecipe,  p6,  false);
		
		failures += Test.testContains(testRecipe, p1, true);
		failures += Test.testContains(testRecipe, p2, true);
		failures += Test.testContains(testRecipe, p5, true);
		failures += Test.testContains(testRecipe, p6, false);
		
		
		failures += Test.testContains(p5, p1, true);
		failures += Test.testContains(p1, p2, true);
		failures += Test.testContains(p1, p5, false);
		failures += Test.testContains(p3, p5, false);
		System.out.println(failures + " failures");
	}
	
	
}
