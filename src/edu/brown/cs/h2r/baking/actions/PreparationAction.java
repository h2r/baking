package edu.brown.cs.h2r.baking.actions;

import java.util.Set;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.core.StateBuilder;
import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.Knowledgebase.Knowledgebase;
import edu.brown.cs.h2r.baking.ObjectFactories.AgentFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.ContainerFactory;
import edu.brown.cs.h2r.baking.ObjectFactories.IngredientFactory;

public class PreparationAction extends BakingAction {
	private final Knowledgebase knowledgebase;
	public static final String className = "prepare";
	public PreparationAction(Domain domain) {
		super(PreparationAction.className, domain, new String[] 
				{AgentFactory.ClassName, IngredientFactory.ClassNameSimple, ContainerFactory.ClassName});
		this.knowledgebase = Knowledgebase.getKnowledgebase(domain);
	}
	
	@Override
	public BakingActionResult checkActionIsApplicableInState(State state, String[] params) {
		BakingActionResult superResult = super.checkActionIsApplicableInState(state, params);
		
		if (!superResult.getIsSuccess()) {
			return superResult;
		}
		
		String ingredientName = params[1];
		ObjectInstance ingredient = state.getObject(ingredientName);
		
		if (IngredientFactory.getPrepTraits(ingredient).isEmpty()) {
			return BakingActionResult.failure(ingredient + " cannot be prepared further");
		}
		
		return BakingActionResult.success();
	}
	
	@Override
	public boolean applicableInState(State state, String[] params) {
		return this.checkActionIsApplicableInState(state, params).getIsSuccess();
	}

	@Override
	protected State performActionHelper(State state, String[] params) {
		super.performActionHelper(state, params);
		StateBuilder builder = new StateBuilder(state);
		
		ObjectInstance ingredient = state.getObject(params[1]);
		Set<String> prepTraits = IngredientFactory.getPrepTraits(ingredient);
		String newIngredientName = prepTraits.iterator().next();
		
		IngredientRecipe ingredientRecipe = this.knowledgebase.getIngredient(newIngredientName);
		ObjectClass simpleIngredientClass = this.domain.getObjectClass(IngredientFactory.ClassNameSimple);
		ObjectInstance newIngredient = IngredientFactory.getNewIngredientInstance(ingredientRecipe, newIngredientName, simpleIngredientClass, ingredient.getHashTuple().getHashingFactory());
		
		String containerName = IngredientFactory.getContainer(ingredient);
		String newContainerName = params[2];
		ObjectInstance previousContainer = state.getObject(containerName);
		ObjectInstance newPreviousContainer = ContainerFactory.removeIngredient(previousContainer, params[1]);
		
		ObjectInstance newContainer = (containerName.equals(newContainerName)) ? newPreviousContainer :
			state.getObject(newContainerName);
		
		ObjectInstance newNewContainer = ContainerFactory.addIngredient(newContainer, newIngredientName);
		
		builder.replace(ingredient, newIngredient);
		builder.replace(previousContainer, newPreviousContainer);
		builder.replace(newContainer, newNewContainer);
		return builder.toState();
	}
}
