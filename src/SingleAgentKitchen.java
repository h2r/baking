
import java.util.List;

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
	
	public static final String							CLASSCONTENTS = "contents";
	public static final String							CLASSBATTER = "batter";
	public static final String							CLASSBROWNIES = "brownies";
	public static final String							CLASSINGREDIENT = "cocoa";
	public static final String							CLASSMIXCONTAINER = "mix_container";
	public static final String							CLASSINGREDIENTCONTAINER = "ingredient_container";
	
	public static final String							ACTIONADD = "add";
	public static final String							ACTIONMIX = "mix";
	public static final String							ACTIONBAKE = "bake";
	public static final String							ACTIONPOUR = "pour";
	
	@Override
	public Domain generateDomain() {
		Domain domain = new SADomain();
		ObjectClass containerClass = new ContainerClass(domain);
		ObjectClass ingredientClass = new IngredientClass(domain);
		
		
		Action mix = new MixAction(ACTIONMIX, domain);
		Action bake = new BakeAction(ACTIONBAKE, domain);
		Action pour = new PourAction(ACTIONPOUR, domain);
		return domain;
	}
	
	public static State getOneAgent(Domain domain){
		State state = new State();
		ContainerInstance mixingBowl = 
				new MixingContainerInstance(
						(ContainerClass)domain.getObjectClass(ContainerClass.className), 
						"mixing_bowl_1");
		
		state.addObject(mixingBowl);
		IngredientClass ingredientClass = 
				(IngredientClass)domain.getObjectClass(IngredientClass.className);
		
		Recipe brownies = new Brownies(ingredientClass, "brownies");
		ContainerClass containerClass = 
				(ContainerClass)domain.getObjectClass(ContainerClass.className);
		
		List<ContainerInstance> containerInstances = brownies.getContainers(containerClass);
		for (ContainerInstance containerInstance : containerInstances)
		{
			state.addObject(containerInstance);
			state.addObject(containerInstance.contents.get(0));
		}
		return state;
	}
	
	public void add(State state, Domain domain, ObjectInstance ingredient, ObjectInstance container)
	{
		List<ObjectInstance> contentInstances = state.getObjectsOfTrueClass(CLASSCONTENTS);
		if (contentInstances.size() > 0)
		{
			ObjectInstance contents = contentInstances.get(0);
			String attributeName = "has_" + ingredient.getName();
			contents.setValue(attributeName, 1);
			System.out.println("Adding: " + ingredient.getName());
		}
	}
	
	public void mix(State state, Domain domain)
	{
		List<ObjectInstance> contentInstances = state.getObjectsOfTrueClass(CLASSCONTENTS);
		if (contentInstances.size() > 0)
		{
			ObjectInstance contents = contentInstances.get(0);
				
			if (contents.getValueForAttribute(ATTHASFLOUR).getDiscVal() == 1)
			{
				state.addObject(new ObjectInstance(domain.getObjectClass(CLASSBATTER), CLASSBATTER+0));
				
				ObjectInstance batter = state.getObjectsOfTrueClass(CLASSBATTER).get(0);
				batter.setValue(ATTBAKED, 0);	
			}
					
		}
	}
	
	public void bake(State state, Domain domain)
	{
		List<ObjectInstance> contentInstances = state.getObjectsOfTrueClass(CLASSCONTENTS);
		List<ObjectInstance> batterInstances = state.getObjectsOfTrueClass(CLASSBATTER);
		if (contentInstances.size() > 0 && batterInstances.size() > 0)
		{
			ObjectInstance contents = contentInstances.get(0);
			ObjectInstance batter = batterInstances.get(0);
			state.addObject(new ObjectInstance(domain.getObjectClass(CLASSBROWNIES), CLASSBROWNIES+0));	
		}
	}
	
	public class PourAction extends Action {
		public PourAction(String name, Domain domain) {
			super(name, domain, new String[] {ContainerClass.className, ContainerClass.className});
		}
		
		@Override
		public boolean applicableInState(State state, String[] params) {
			ContainerInstance pouringContainer = (ContainerInstance)state.getObject(params[0]);
			if (pouringContainer.contents.size() == 0)
			{
				return false;
			}
			ContainerInstance recievingContainer = (ContainerInstance)state.getObject(params[1]);
			return (recievingContainer.getDiscValForAttribute(ContainerClass.ATTRECEIVING) == 1);			
		}

		@Override
		protected State performActionHelper(State state, String[] params) {
			ContainerInstance pouringContainer = (ContainerInstance)state.getObject(params[0]);
			ContainerInstance recievingContainer = (ContainerInstance)state.getObject(params[1]);
			pouringContainer.pour(recievingContainer);
			//System.out.println("Pour contents of container " + params[0] + " container " + params[1]);
			return state;
		}
	}
	
	public class MixAction extends Action {	
		public MixAction(String name, Domain domain) {
			super(name, domain, new String[] {ContainerClass.className});
		}
		
		@Override
		public boolean applicableInState(State state, String[] params) {
			ObjectInstance containerInstance = state.getObject(params[0]);
			ContainerInstance container = (ContainerInstance)containerInstance;
			if (container.getDiscValForAttribute(ContainerClass.ATTMIXING) != 1)
			{
				return false;
			}
			if (container.getAllRelationalTargets(ContainerClass.ATTCONTAINS).size() == 0)
			{
				return false;
			}
			return true;
		}
	
		@Override
		protected State performActionHelper(State state, String[] params) {
			ObjectInstance containerInstance = state.getObject(params[0]);
			ContainerInstance container = (ContainerInstance)containerInstance;
			//System.out.println("Mixing ingredients in container " + container.getName());
			container.mix();
			return state;
		}
	}
	
	public class BakeAction extends Action {
		public BakeAction(String name, Domain domain) {
			super(name, domain, "");
		}
		
		@Override
		public boolean applicableInState(State s, String[] params) {
			
			return true;
		}
	
		@Override
		protected State performActionHelper(State state, String[] params) {
			SingleAgentKitchen.this.bake(state, this.domain);
			//System.out.println("Bake!");
			return state;
		}
	}
	
	public static void main(String[] args) {
		SingleAgentKitchen kitchen = new SingleAgentKitchen();
		Domain domain = kitchen.generateDomain();
		State state = SingleAgentKitchen.getOneAgent(domain);
		
		IngredientClass ingredientClass = 
				(IngredientClass)domain.getObjectClass(IngredientClass.className);
		final Recipe brownies = new Brownies(ingredientClass, "brownies");
		//RewardFunction recipeRewardFunction = new RecipeRewardFunction(brownies);
		RewardFunction recipeRewardFunction = new UniformCostRF();
		TerminalFunction recipeTerminalFunction = new RecipeTerminalFunction(brownies);
		
		StateHashFactory hashFactory = new NameDependentStateHashFactory();
		
		StateConditionTest goalCondition = new StateConditionTest()
		{
			@Override
			public boolean satisfies(State s) {
				List<ObjectInstance> objects = s.getObjectsOfTrueClass(ContainerClass.className);
				for (ObjectInstance obj : objects)
				{
					ContainerInstance container = (ContainerInstance)obj;
					if (container.contents.size() == 1)
					{
						if (container.contents.get(0).equals(brownies))
						{
							// We're done!
							return true;
						}
					}
				}
				return false;
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
		System.out.println("Action Sequence\n" + 
				episodeAnalysis.getActionSequenceString());
	}
}
