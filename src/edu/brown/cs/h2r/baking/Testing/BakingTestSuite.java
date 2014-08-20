package edu.brown.cs.h2r.baking.Testing;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	TestIngredientRecipe.class,
	TestRecipeSuccess.class,
	TestRecipeFailure.class,
	TestActions.class,
	TestConstrainedStateSpace.class
})
public class BakingTestSuite {

}
