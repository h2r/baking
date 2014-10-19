package edu.brown.cs.h2r.baking.Knowledgebase;

import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.PropositionalFunctions.*;
import edu.brown.cs.h2r.baking.actions.*;
import burlap.behavior.affordances.AffordanceDelegate;
import burlap.behavior.affordances.AffordancesController;
import burlap.behavior.affordances.Affordance;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.logicalexpressions.PFAtom;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.GroundedAction;

public class AffordanceCreator {
	public static final String HEAT_PF = "heatPF";
	public static final String BAKE_PF = "bakePF";
	public static final String MIX_PF = "mixPF";
	public static final String MOVE_PF = "movePF";
	public static final String POUR_PF = "pourPF";
	public static final String USE_PF = "usePF";
	public static final String HAND_PF = "handPF";
	public static final String SWITCH_PF = "switchPF";
	public static final String GREASE_PF = "greasePF";
	public static final String FINISH_PF = "success";
	public static final String BOTCHED_PF = "botched";
	public static final String SPACEON_PF = "spaceOnPF";
	public static final String CONTAINERGREASED_PF = "containerGreasedPF";
	public static final String CONTAINERS_CLEANED_PF = "containerCleanedPF";
	
	
	public static final String INGREDIENT_PF = "ingredientPF";
	
	private ArrayList<String> goalPFs;
	private List<AffordanceDelegate> affDelegates;
	private ArrayList<PFAtom> PFAtoms;
	private PFAtom spaceOnPFAtom;
	private PFAtom containerGreasedPFAtom;
	private PFAtom finishedPFAtom;
	private PFAtom botchedPFAtom;
	private AffordancesController affController;
	
	public AffordanceCreator(Domain domain, State state, IngredientRecipe ingredient) {
		// Add prop functions to Domain
		final PropositionalFunction allowMixing = new AllowMixing(AffordanceCreator.MIX_PF, domain, ingredient);
		final PropositionalFunction allowPouring= new AllowPouring(AffordanceCreator.POUR_PF, domain, ingredient);
		final PropositionalFunction allowMoving = new AllowMoving(AffordanceCreator.MOVE_PF, domain, ingredient);
		final PropositionalFunction allowGreasing = new AllowGreasing(AffordanceCreator.GREASE_PF, domain, ingredient);
		final PropositionalFunction allowSwitching = new AllowSwitching(AffordanceCreator.SWITCH_PF, domain, ingredient);
		final PropositionalFunction allowUsing = new AllowUsingTool(AffordanceCreator.USE_PF, domain, ingredient);
		final PropositionalFunction allowHanding = new AllowHanding(AffordanceCreator.HAND_PF, domain, ingredient);
		
		//final PropositionalFunction allowPeeling = new AllowPeeling(AffordanceCreator.PEEL_PF, domain, ingredient);
		final PropositionalFunction isSuccess = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, ingredient);
		final PropositionalFunction isFailure = new RecipeBotched(AffordanceCreator.BOTCHED_PF, domain, ingredient);
		final PropositionalFunction spaceOn = new SpaceOn(AffordanceCreator.SPACEON_PF, domain, ingredient, "");
		final PropositionalFunction containerGreased = new ContainerGreased(AffordanceCreator.CONTAINERGREASED_PF, domain, ingredient);
		
		this.affDelegates = new ArrayList<AffordanceDelegate>();
		
		createAffordances(domain, state);
	}
	
	public void createGoalPFList() {
		this.goalPFs = new ArrayList<String>();
		this.goalPFs.add(AffordanceCreator.FINISH_PF);
		this.goalPFs.add(AffordanceCreator.BOTCHED_PF);
		this.goalPFs.add(AffordanceCreator.SPACEON_PF);
		this.goalPFs.add(AffordanceCreator.CONTAINERGREASED_PF);
		
	}
	
	public void createAffordances(Domain domain, State state) {
		setupGoalPFAtoms(domain, state);
		for (Action action : domain.getActions()) {
			String actionName = action.getName();
			ArrayList<PFAtom> pfatoms = new ArrayList<PFAtom>();
			PFAtom goalPF = getGoalPF(actionName);
			String actionPFName = getActionPF(actionName);
			setupPFAtom(domain, state, actionPFName, pfatoms);
			setupDelegate(domain, pfatoms, actionName, goalPF);
			// we make two delegates for switch action, one preheating, one for actual cooking
			if (actionName.equals(SwitchAction.className)) {
				setupDelegate(domain, pfatoms, actionName, finishedPFAtom);
			}
		}
		this.affController = new AffordancesController(affDelegates);
	}
	
	public void setupGoalPFAtoms(Domain domain, State state) {
		// these are the PFs that relate to goals.
		createGoalPFList();
		ArrayList<PFAtom> PFAtoms = new ArrayList<PFAtom>();
		for (String pf : this.goalPFs) {
			ArrayList<PFAtom> atomList = new ArrayList<PFAtom>();
			setupPFAtom(domain, state, pf, atomList);
			PFAtom atom = atomList.get(0);
			initializePFAtomVariable(atom);
			PFAtoms.add(atom);
			
		}
		this.PFAtoms = PFAtoms;
	}
	
	public void setupPFAtom(Domain domain, State state, String pfName, ArrayList<PFAtom> atomList) {
		PropositionalFunction pf = domain.getPropFunction(pfName);
		List<GroundedProp> groundedProps = pf.getAllGroundedPropsForState(state);
		for(GroundedProp gp : groundedProps) {
			atomList.add(new PFAtom(gp));
		}
	}
	
	public void setupDelegate(Domain domain, List<PFAtom> PFAtoms, String actionName, PFAtom goal) {
		for (PFAtom pfAtom : PFAtoms) {
			AbstractGroundedAction act = new GroundedAction(domain.getAction(actionName), pfAtom.getGroundedProp().params);
			List<AbstractGroundedAction> list = new ArrayList<AbstractGroundedAction>();
			list.add(act);
			Affordance aff= new Affordance(pfAtom, goal, list);
			AffordanceDelegate affDelegate = new AffordanceDelegate(aff);
			//TODO NOT USED affDelegate.resampleActionSet();
			this.affDelegates.add(affDelegate);
		}
	}
	
	public String getActionPF(String actionName) {
		switch(actionName) {
			case MixAction.className:
				return AffordanceCreator.MIX_PF;
			case MoveAction.className:
				return AffordanceCreator.MOVE_PF;
			case PourAction.className:
				return AffordanceCreator.POUR_PF;
			case UseAction.className:
				return AffordanceCreator.USE_PF;
			case GreaseAction.className:
				return AffordanceCreator.GREASE_PF;
			case SwitchAction.className:
				return AffordanceCreator.SWITCH_PF;
			default:
				System.err.println("Action " + actionName + " has no PF associated with it!");
				System.exit(0);
				return null;
		}
	}
	
	public PFAtom getGoalPF(String actionName) {
		switch(actionName) {
			case GreaseAction.className:
				return containerGreasedPFAtom;
			case SwitchAction.className:
				return spaceOnPFAtom;
			default:
				return finishedPFAtom;
		}
	}
	
	public void initializePFAtomVariable(PFAtom atom) {
		String atomName = atom.getGroundedProp().pf.getName();
		switch(atomName) {
			case AffordanceCreator.FINISH_PF:
				this.finishedPFAtom = atom;
				break;
			case AffordanceCreator.BOTCHED_PF:
				this.botchedPFAtom = atom;
				break;
			case AffordanceCreator.CONTAINERGREASED_PF:
				this.containerGreasedPFAtom = atom;
				break;
			case AffordanceCreator.SPACEON_PF:
				this.spaceOnPFAtom = atom;
				break;
			default:
				System.err.println("PFAtom " + atomName + " has no variable associated with it.");
		}
	}
	
	public AffordancesController getAffController() {
		return this.affController;
	}
	
	public PFAtom getPFAtom(String name) {
		for (PFAtom atom : this.PFAtoms) {
			if (atom.getGroundedProp().pf.getName().equals(name)) {
				return atom;
			}
		}
		return null;
	}
}