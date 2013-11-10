
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import burlap.behavior.statehashing.DiscreteStateHashFactory;
import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.singleagent.EpisodeAnalysis;
import burlap.behavior.singleagent.Policy;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.deterministic.DDPlannerPolicy;
import burlap.behavior.singleagent.planning.deterministic.SDPlannerPolicy;
import burlap.behavior.singleagent.planning.deterministic.TFGoalCondition;
import burlap.behavior.singleagent.planning.deterministic.informed.Heuristic;
import burlap.behavior.singleagent.planning.deterministic.informed.astar.AStar;
import burlap.oomdp.auxiliary.DomainGenerator;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.core.TerminalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;
import burlap.oomdp.singleagent.RewardFunction;
import burlap.oomdp.singleagent.SADomain;
import burlap.oomdp.singleagent.common.UniformCostRF;


public class SingleAgentKitchen implements DomainGenerator {
	
	public static final String 							ATTHASFLOUR = "has_flour";
	public static final String 							ATTHASCOCOA = "has_cocoa";
	public static final String 							ATTHASBUTTER = "has_butter";
	public static final String							ATTMIXED = "mixed";
	public static final String							ATTBAKED = "baked";
	public static final String							ATTINCONTAINER = "in_container";
	public static final String							ATTROBOT = "robot";
	public static final String							ATTMIXINGSPACE = "mixing_space";
	public static final String							ATTINSPACE = "in_space";
	
	public static final String							CLASSSPACE = "space";
	public static final String							CLASSAGENT = "agent";
	public static final String							CLASSINGREDIENT = "cocoa";
	public static final String							CLASSMIXCONTAINER = "mix_container";
	public static final String							CLASSINGREDIENTCONTAINER = "ingredient_container";
	
	public static final String							ACTIONADD = "add";
	public static final String							ACTIONMIX = "mix";
	public static final String							ACTIONBAKE = "bake";
	public static final String							ACTIONPOUR = "pour";
	public static final String							ACTIONMOVE = "move";
	
	@Override
	public Domain generateDomain() {
		Domain domain = new SADomain();
		ObjectClass containerClass = new ObjectClass(domain, ContainerClass.className);
		
		Attribute mixingAttribute = 
				new Attribute(domain, ContainerClass.ATTMIXING, Attribute.AttributeType.DISC);
		mixingAttribute.setDiscValuesForRange(0,1,1);
		
		Attribute heatingAttribute = 
				new Attribute(domain, ContainerClass.ATTHEATING, Attribute.AttributeType.DISC);
		heatingAttribute.setDiscValuesForRange(0,1,1);
		
		Attribute receivingAttribute =
				new Attribute(domain, ContainerClass.ATTRECEIVING, Attribute.AttributeType.DISC);
		receivingAttribute.setDiscValuesForRange(0,1,1);
		Attribute inSpaceAttribute =
				new Attribute(domain, ATTINSPACE, Attribute.AttributeType.RELATIONAL);
		
		containerClass.addAttribute(mixingAttribute);
		containerClass.addAttribute(heatingAttribute);
		containerClass.addAttribute(receivingAttribute);
		containerClass.addAttribute(new Attribute(domain, ContainerClass.ATTCONTAINS, 
						Attribute.AttributeType.MULTITARGETRELATIONAL));
		containerClass.addAttribute(inSpaceAttribute);
		
		Attribute bakedAttribute = 
				new Attribute(domain, Recipe.Ingredient.attBaked, Attribute.AttributeType.DISC);
		bakedAttribute.setDiscValuesForRange(0,1,1);
		Attribute mixedAttribute = 
				new Attribute(domain, Recipe.Ingredient.attMixed, Attribute.AttributeType.DISC);
		mixedAttribute.setDiscValuesForRange(0,1,1);
		Attribute meltedAttribute = 
				new Attribute(domain, Recipe.Ingredient.attMelted, Attribute.AttributeType.DISC);
		meltedAttribute.setDiscValuesForRange(0,1,1);
		Attribute containsAttribute = 
				new Attribute(domain, Recipe.ComplexIngredient.attContains, Attribute.AttributeType.MULTITARGETRELATIONAL);
		
		ObjectClass spaceClass = new ObjectClass(domain, CLASSSPACE);
		Attribute mixingSpace = new Attribute(domain, ATTMIXINGSPACE, Attribute.AttributeType.DISC);
		mixingSpace.setDiscValuesForRange(0,1,1);
		spaceClass.addAttribute(mixingSpace);
		
		ObjectClass simpleIngredientClass = new ObjectClass(domain, Recipe.SimpleIngredient.className);
		simpleIngredientClass.addAttribute(bakedAttribute);
		simpleIngredientClass.addAttribute(mixedAttribute);
		simpleIngredientClass.addAttribute(meltedAttribute);
		domain.addObjectClass(simpleIngredientClass);		
		
		ObjectClass complexIngredientClass = new ObjectClass(domain, Recipe.ComplexIngredient.className);
		complexIngredientClass.addAttribute(bakedAttribute);
		complexIngredientClass.addAttribute(mixedAttribute);
		complexIngredientClass.addAttribute(meltedAttribute);
		complexIngredientClass.addAttribute(containsAttribute);
		domain.addObjectClass(complexIngredientClass);
		
		ObjectClass agent = new ObjectClass(domain, this.CLASSAGENT);
		Attribute robot = new Attribute(domain, this.ATTROBOT, Attribute.AttributeType.DISC);
		robot.setDiscValuesForRange(0, 1, 1);
		agent.addAttribute(robot);
		
		Action mix = new MixAction(ACTIONMIX, domain);
		Action bake = new BakeAction(ACTIONBAKE, domain);
		Action pour = new PourAction(ACTIONPOUR, domain);
		Action move = new MoveAction(ACTIONMOVE, domain);
		return domain;
	}
	
	public void PlanRecipe(Domain domain, Recipe recipe)
	{
		State state = new State();
		
		ObjectInstance human = new ObjectInstance(domain.getObjectClass(CLASSAGENT), "human");
		human.setValue(ATTROBOT, 0);
		state.addObject(human);
		
		ObjectInstance robot = new ObjectInstance(domain.getObjectClass(CLASSAGENT), "robot");
		robot.setValue(ATTROBOT, 1);
		state.addObject(robot);
		
		ObjectInstance shelfSpace = new ObjectInstance(domain.getObjectClass(CLASSSPACE), "shelf");
		shelfSpace.setValue(ATTMIXINGSPACE, 0);
		state.addObject(shelfSpace);
		ObjectInstance counterSpace = new ObjectInstance(domain.getObjectClass(CLASSSPACE), "counter");
		counterSpace.setValue(ATTMIXINGSPACE, 1);
		state.addObject(counterSpace);
		
		ObjectInstance mixingBowl = 
				new ObjectInstance(
						domain.getObjectClass(ContainerClass.className), 
						"mixing_bowl_1");
		mixingBowl.setValue(ContainerClass.ATTRECEIVING, 1);
		mixingBowl.setValue(ContainerClass.ATTHEATING, 0);
		mixingBowl.setValue(ContainerClass.ATTMIXING, 1);
		
		state.addObject(mixingBowl);
		ObjectClass simpleIngredientClass = domain.getObjectClass(Recipe.SimpleIngredient.className);
		
		ObjectClass containerClass = domain.getObjectClass(ContainerClass.className);
		
		List<ObjectInstance> ingredientInstances = recipe.getRecipeList(simpleIngredientClass);
		List<ObjectInstance> containerInstances = recipe.getContainers(containerClass, ingredientInstances);
		
		for (ObjectInstance ingredientInstance : ingredientInstances)
		{
			state.addObject(ingredientInstance);
		}
		for (ObjectInstance containerInstance : containerInstances)
		{
			containerInstance.addRelationalTarget(ATTINSPACE, shelfSpace.getName());
			state.addObject(containerInstance);
			
		}
		
		State finalState = this.PlanIngredient(domain, state, (Recipe.ComplexIngredient)recipe.topLevelIngredient);
		
	}
	
	public State PlanIngredient(Domain domain, State startingState, Recipe.ComplexIngredient ingredient)
	{
		State currentState = new State(startingState);
		for (Recipe.Ingredient subIngredient : ingredient.Contents)
		{
			if (subIngredient instanceof Recipe.ComplexIngredient)
			{
				currentState = this.PlanIngredient(domain, currentState, (Recipe.ComplexIngredient)subIngredient);
			}
		}
		final PropositionalFunction isSuccess = new RecipeFinished("success", domain, ingredient);
		PropositionalFunction isFailure = new RecipeBotched("botched", domain, ingredient);
		//RewardFunction recipeRewardFunction = new RecipeRewardFunction(brownies);
		RewardFunction recipeRewardFunction = new RecipeRewardFunction();
		TerminalFunction recipeTerminalFunction = new RecipeTerminalFunction(isSuccess, isFailure);
		
		StateHashFactory hashFactory = new NameDependentStateHashFactory();
		
		StateConditionTest goalCondition = new StateConditionTest()
		{
			@Override
			public boolean satisfies(State s) {
				return s.somePFGroundingIsTrue(isSuccess);
			}
		};
		Heuristic heuristic = new Heuristic() {
			@Override
			public double h(State state) {
				return 0;
			}
		};
		AStar aStar = new AStar(domain, recipeRewardFunction, goalCondition, 
				hashFactory, heuristic);
		aStar.planFromState(currentState);
		Policy policy = new DDPlannerPolicy(aStar);
		
		EpisodeAnalysis episodeAnalysis = 
				policy.evaluateBehavior(currentState, recipeRewardFunction, recipeTerminalFunction);	
		for (int i =0 ; i < episodeAnalysis.actionSequence.size(); ++i)
		{
			GroundedAction action = episodeAnalysis.actionSequence.get(i);
			
			double reward = episodeAnalysis.rewardSequence.get(i);
			System.out.print("Cost: " + reward + " " + action.action.getName() + " ");
			for (int j = 0; j < action.params.length; ++j)
			{
				System.out.print(action.params[j] + " ");
			}
			System.out.print("\n");
		}
		State endState = episodeAnalysis.getState(episodeAnalysis.stateSequence.size() - 1);
		List<ObjectInstance> finalObjects = 
				new ArrayList<ObjectInstance>(endState.getObjectsOfTrueClass(Recipe.ComplexIngredient.className));
		ObjectInstance namedIngredient = null;
		for (ObjectInstance obj : finalObjects)
		{
			if (Recipe.isSuccess(endState, ingredient, obj))
			{
				namedIngredient = SingleAgentKitchen.getNewNamedComplexIngredient(obj, ingredient.Name);
				endState.removeObject(obj);
				endState.addObject(namedIngredient);
				return endState;
			}
		}
		return endState;
	}
	
	public static ObjectInstance getNewNamedComplexIngredient(ObjectInstance unnamedIngredient, String name)
	{
		ObjectInstance namedIngredient = new ObjectInstance(unnamedIngredient.getObjectClass(), name);
		int baked = unnamedIngredient.getDiscValForAttribute(Recipe.Ingredient.attBaked);
		namedIngredient.setValue(Recipe.Ingredient.attBaked, baked);
		int mixed = unnamedIngredient.getDiscValForAttribute(Recipe.Ingredient.attMixed);
		namedIngredient.setValue(Recipe.Ingredient.attMixed, mixed);
		int melted = unnamedIngredient.getDiscValForAttribute(Recipe.Ingredient.attMelted);
		namedIngredient.setValue(Recipe.Ingredient.attMelted, melted);
		
		Set<String> contents = unnamedIngredient.getAllRelationalTargets(Recipe.ComplexIngredient.attContains);
		for (String subIngredient : contents)
		{
			namedIngredient.addRelationalTarget(Recipe.ComplexIngredient.attContains, subIngredient);
		}
		
		return namedIngredient;		
	}
	
	public static State getOneAgent(Domain domain){
		State state = new State();
		
		ObjectInstance human = new ObjectInstance(domain.getObjectClass(CLASSAGENT), "human");
		human.setValue(ATTROBOT, 0);
		state.addObject(human);
		
		ObjectInstance robot = new ObjectInstance(domain.getObjectClass(CLASSAGENT), "robot");
		robot.setValue(ATTROBOT, 1);
		state.addObject(robot);
		
		ObjectInstance shelfSpace = new ObjectInstance(domain.getObjectClass(CLASSSPACE), "shelf");
		shelfSpace.setValue(ATTMIXINGSPACE, 0);
		state.addObject(shelfSpace);
		ObjectInstance counterSpace = new ObjectInstance(domain.getObjectClass(CLASSSPACE), "counter");
		counterSpace.setValue(ATTMIXINGSPACE, 1);
		state.addObject(counterSpace);
		
		ObjectInstance mixingBowl = 
				new ObjectInstance(
						domain.getObjectClass(ContainerClass.className), 
						"mixing_bowl_1");
		mixingBowl.setValue(ContainerClass.ATTRECEIVING, 1);
		mixingBowl.setValue(ContainerClass.ATTHEATING, 0);
		mixingBowl.setValue(ContainerClass.ATTMIXING, 1);
		
		state.addObject(mixingBowl);
		ObjectClass simpleIngredientClass = domain.getObjectClass(Recipe.SimpleIngredient.className);
		
		Recipe brownies = new Brownies();
		ObjectClass containerClass = domain.getObjectClass(ContainerClass.className);
		
		List<ObjectInstance> ingredientInstances = brownies.getRecipeList(simpleIngredientClass);
		List<ObjectInstance> containerInstances = brownies.getContainers(containerClass, ingredientInstances);
		
		for (ObjectInstance ingredientInstance : ingredientInstances)
		{
			state.addObject(ingredientInstance);
		}
		for (ObjectInstance containerInstance : containerInstances)
		{
			containerInstance.addRelationalTarget(ATTINSPACE, shelfSpace.getName());
			state.addObject(containerInstance);
			
		}
		return state;
	}
	
	public class PourAction extends Action {
		public PourAction(String name, Domain domain) {
			super(name, domain, new String[] {SingleAgentKitchen.CLASSAGENT, ContainerClass.className, ContainerClass.className});
		}
		
		@Override
		public boolean applicableInState(State state, String[] params) {
			ObjectInstance agent = state.getObject(params[0]);
			if (agent.getDiscValForAttribute(SingleAgentKitchen.ATTROBOT) == 1)
			{
				return false;
			}
			ObjectInstance pouringContainer = state.getObject(params[1]);
			if (pouringContainer.getAllRelationalTargets(ContainerClass.ATTCONTAINS).size() == 0)
			{
				return false;
			}
			ObjectInstance recievingContainer = state.getObject(params[2]);
			if (recievingContainer.getDiscValForAttribute(ContainerClass.ATTRECEIVING) == 0)
			{
				return false;
			}
			Set<String> pouringContainerSpace = pouringContainer.getAllRelationalTargets(ATTINSPACE);
			Set<String> receivingContainerSpace = recievingContainer.getAllRelationalTargets(ATTINSPACE);
			if (pouringContainerSpace.size() == 0 || receivingContainerSpace.size() == 0)
			{
				throw new RuntimeException("One of the pouring containers is not in any space");
			}
			
			if (pouringContainerSpace.iterator().next() != receivingContainerSpace.iterator().next())
			{
				return false;
			}
			ObjectInstance pouringContainerSpaceObject = state.getObject(pouringContainerSpace.iterator().next());
			
			if (pouringContainerSpaceObject.getDiscValForAttribute(ATTMIXINGSPACE)== 0)
			{
				return false;
			}
			return true;
		}

		@Override
		protected State performActionHelper(State state, String[] params) {
			ObjectInstance agent = state.getObject(params[0]);
			ObjectInstance pouringContainer = state.getObject(params[1]);
			ObjectInstance recievingContainer = state.getObject(params[2]);
			this.pour(pouringContainer, recievingContainer);
			System.out.println("Pour contents of container " + params[0] + " container " + params[1]);
			return state;
		}
		
		protected void pour(ObjectInstance pouringContainer, ObjectInstance receivingContainer)
		{
			Set<String> ingredients = pouringContainer.getAllRelationalTargets(ContainerClass.ATTCONTAINS);
			for (String ingredient : ingredients)
			{
				receivingContainer.addRelationalTarget(ContainerClass.ATTCONTAINS, ingredient);
			}
			pouringContainer.clearRelationalTargets(ContainerClass.ATTCONTAINS);
		}
	}
	
	public class MixAction extends Action {	
		public MixAction(String name, Domain domain) {
			super(name, domain, new String[] {SingleAgentKitchen.CLASSAGENT, ContainerClass.className});
		}
		
		@Override
		public boolean applicableInState(State state, String[] params) {
			ObjectInstance agent =  state.getObject(params[0]);
			if (agent.getDiscValForAttribute(SingleAgentKitchen.ATTROBOT) == 1)
			{
				return false;
			}
			ObjectInstance containerInstance = state.getObject(params[1]);
			if (containerInstance.getDiscValForAttribute(ContainerClass.ATTMIXING) != 1)
			{
				return false;
			}
			if (containerInstance.getAllRelationalTargets(ContainerClass.ATTCONTAINS).size() == 0)
			{
				return false;
			}

			Set<String> containerSpace = containerInstance.getAllRelationalTargets(ATTINSPACE);
			if (containerSpace.size() == 0 || containerSpace.size() == 0)
			{
				throw new RuntimeException("Mixing container is not in any space");
			}

			ObjectInstance pouringContainerSpaceObject = state.getObject(containerSpace.iterator().next());
			
			if (pouringContainerSpaceObject.getDiscValForAttribute(ATTMIXINGSPACE)== 0)
			{
				return false;
			}
			return true;
		}
	
		@Override
		protected State performActionHelper(State state, String[] params) {
			ObjectInstance agent = state.getObject(params[0]);
			ObjectInstance containerInstance = state.getObject(params[1]);
			System.out.println("Mixing ingredients in container " + containerInstance.getName());
			this.mix(state, containerInstance);
			return state;
		}
		
		protected void mix(State state, ObjectInstance container)
		{
			ObjectClass complexIngredientClass = this.domain.getObjectClass(Recipe.ComplexIngredient.className);
			Random rando = new Random();
			ObjectInstance newIngredient = new ObjectInstance(complexIngredientClass, Integer.toString(rando.nextInt()));
			newIngredient.setValue(Recipe.Ingredient.attBaked, 0);
			newIngredient.setValue(Recipe.Ingredient.attMelted, 0);
			newIngredient.setValue(Recipe.Ingredient.attMixed, 0);
			
			Set<String> contents = container.getAllRelationalTargets(ContainerClass.ATTCONTAINS);
			for (String ingredient : contents)
			{
				newIngredient.addRelationalTarget(Recipe.ComplexIngredient.attContains, ingredient);
			}
			container.clearRelationalTargets(ContainerClass.ATTCONTAINS);
			state.addObject(newIngredient);
			
		}
	}
	
	public class BakeAction extends Action {
		public BakeAction(String name, Domain domain) {
			super(name, domain, "");
		}
		
		@Override
		public boolean applicableInState(State s, String[] params) {
			
			return false;
		}
	
		@Override
		protected State performActionHelper(State state, String[] params) {
			this.bake(state, this.domain);
			//System.out.println("Bake!");
			return state;
		}
		
		public void bake(State state, Domain domain)
		{
		}
	}
	
	public class MoveAction extends Action {
		public MoveAction(String name, Domain domain) {
			super(name, domain, new String[] {SingleAgentKitchen.CLASSAGENT, ContainerClass.className, SingleAgentKitchen.CLASSSPACE});
		}
		
		@Override
		protected State performActionHelper(State state, String[] params) {
			System.out.println("Moving container " + params[1] + " to " + params[2]);
			ObjectInstance containerInstance = state.getObject(params[1]);
			containerInstance.addRelationalTarget(ATTINSPACE, params[2]);
			return state;
		}
	}
	
	public static void main(String[] args) {
		SingleAgentKitchen kitchen = new SingleAgentKitchen();
		Domain domain = kitchen.generateDomain();
		
		kitchen.PlanRecipe(domain, new Brownies());
		
		/*
		State state = SingleAgentKitchen.getOneAgent(domain);
		
		final Recipe brownies = new Brownies();
		final PropositionalFunction isSuccess = new RecipeFinished("success", domain, brownies.topLevelIngredient);
		PropositionalFunction isFailure = new RecipeBotched("botched", domain, brownies.topLevelIngredient);
		//RewardFunction recipeRewardFunction = new RecipeRewardFunction(brownies);
		RewardFunction recipeRewardFunction = new RecipeRewardFunction();
		TerminalFunction recipeTerminalFunction = new RecipeTerminalFunction(isSuccess, isFailure);
		
		StateHashFactory hashFactory = new NameDependentStateHashFactory();
		
		StateConditionTest goalCondition = new StateConditionTest()
		{
			@Override
			public boolean satisfies(State s) {
				return s.somePFGroundingIsTrue(isSuccess);
			}
		};
		Heuristic heuristic = new Heuristic() {
			@Override
			public double h(State state) {
				return 0;
			}
		};
		AStar aStar = new AStar(domain, recipeRewardFunction, goalCondition, 
				hashFactory, heuristic);
		aStar.planFromState(state);
		Policy policy = new DDPlannerPolicy(aStar);
		
		EpisodeAnalysis episodeAnalysis = 
				policy.evaluateBehavior(state, recipeRewardFunction, recipeTerminalFunction);
		for (int i =0 ; i < episodeAnalysis.actionSequence.size(); ++i)
		{
			GroundedAction action = episodeAnalysis.actionSequence.get(i);
			
			double reward = episodeAnalysis.rewardSequence.get(i);
			System.out.print("Cost: " + reward + " " + action.action.getName() + " ");
			for (int j = 0; j < action.params.length; ++j)
			{
				System.out.print(action.params[j] + " ");
			}
			System.out.print("\n");
		}
		//System.out.println("Action Sequence\n" + 
		//		episodeAnalysis.getActionSequenceString());
		 * 
		 */
	}
}
