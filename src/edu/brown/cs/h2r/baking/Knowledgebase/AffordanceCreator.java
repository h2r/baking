package edu.brown.cs.h2r.baking.Knowledgebase;

import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.PropositionalFunctions.*;
import edu.brown.cs.h2r.baking.actions.*;
import burlap.behavior.affordances.AffordanceDelegate;
import burlap.behavior.affordances.AffordancesController;
import burlap.behavior.affordances.HardAffordance;
import burlap.oomdp.core.AbstractGroundedAction;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.GroundedProp;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.core.State;
import burlap.oomdp.logicalexpressions.PFAtom;
import burlap.oomdp.singleagent.GroundedAction;

public class AffordanceCreator {
	public static final String HEAT_PF = "heatPF";
	public static final String BAKE_PF = "bakePF";
	public static final String MIX_PF = "mixPF";
	public static final String MOVE_PF = "movePF";
	public static final String POUR_PF = "pourPF";
	public static final String HAND_PF = "handPF";
	public static final String FINISH_PF = "success";
	public static final String BOTCHED_PF = "botched";
	public static final String CONTAINERGREASED_PF = "containerGreasedPF";
	public static final String INGREDIENT_PF = "ingredientPF";
	public static final String CLEAN_PF = "clean";
	
	private ArrayList<PFAtom> mixPFAtoms;
	private ArrayList<PFAtom> movePFAtoms;
	private ArrayList<PFAtom> pourPFAtoms;
	private ArrayList<PFAtom> handPFAtoms;
	private ArrayList<PFAtom> finishedPFAtoms;
	private ArrayList<PFAtom> botchedPFAtoms;
	private List<AffordanceDelegate> affDelegates;
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
		final PropositionalFunction allowHanding = new AllowHanding(AffordanceCreator.HAND_PF, domain, ingredient);
		final PropositionalFunction isSuccess = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, ingredient);
		final PropositionalFunction isFailure = new RecipeBotched(AffordanceCreator.BOTCHED_PF, domain, ingredient);
		this.affDelegates = new ArrayList<AffordanceDelegate>();
		setupPFAtoms(domain, state);
		setupAffordances(domain, state);
	}
	
	public void setupPFAtoms(Domain domain, State state) {
		PropositionalFunction mixPF = domain.getPropFunction(AffordanceCreator.MIX_PF);
		this.mixPFAtoms = new ArrayList<PFAtom>();
		List<GroundedProp> mixGroundedProps = mixPF.getAllGroundedPropsForState(state);
		for (GroundedProp mixGroundedProp : mixGroundedProps) {
			this.mixPFAtoms.add(new PFAtom(mixGroundedProp));
		}
		
		PropositionalFunction movePF = domain.getPropFunction(AffordanceCreator.MOVE_PF);
		this.movePFAtoms = new ArrayList<PFAtom>();
		List<GroundedProp> moveGroundedProps = movePF.getAllGroundedPropsForState(state);
		for (GroundedProp moveGroundedProp : moveGroundedProps) {
			this.movePFAtoms.add(new PFAtom(moveGroundedProp));
		}
		
		PropositionalFunction pourPF = domain.getPropFunction(AffordanceCreator.POUR_PF);
		this.pourPFAtoms = new ArrayList<PFAtom>();
		List<GroundedProp> pourGroundedProps = pourPF.getAllGroundedPropsForState(state);
		for (GroundedProp pourGroundedProp : pourGroundedProps) {
			this.pourPFAtoms.add(new PFAtom(pourGroundedProp));
		}
		
		PropositionalFunction handPF = domain.getPropFunction(AffordanceCreator.HAND_PF);
		this.handPFAtoms = new ArrayList<PFAtom>();
		List<GroundedProp> handGroundedProps = handPF.getAllGroundedPropsForState(state);
		for (GroundedProp handGroundedProp : handGroundedProps) {
			this.handPFAtoms.add(new PFAtom(handGroundedProp));
		}
		
		PropositionalFunction finishedPF = domain.getPropFunction(AffordanceCreator.FINISH_PF);
		this.finishedPFAtoms = new ArrayList<PFAtom>();
		List<GroundedProp> finishedGroundedProps = finishedPF.getAllGroundedPropsForState(state);
		for (GroundedProp finishedGroundedProp : finishedGroundedProps) {
			this.finishedPFAtoms.add(new PFAtom(finishedGroundedProp));
		}
		this.finishedPFAtom = this.finishedPFAtoms.get(0);
		
		PropositionalFunction botchedPF = domain.getPropFunction(AffordanceCreator.BOTCHED_PF);
		this.botchedPFAtoms = new ArrayList<PFAtom>();
		List<GroundedProp> botchedGroundedProps = botchedPF.getAllGroundedPropsForState(state);
		for (GroundedProp botchedGroundedProp : botchedGroundedProps) {
			this.botchedPFAtoms.add(new PFAtom(botchedGroundedProp));
		}
		this.botchedPFAtom = this.botchedPFAtoms.get(0);
	}
	
	public void setupDelegate(Domain domain, List<PFAtom> PFAtoms, String actionName, PFAtom goal) {
		for (PFAtom pfAtom : PFAtoms) {
			AbstractGroundedAction act = new GroundedAction(domain.getAction(actionName), pfAtom.getGroundedProp().params);
			List<AbstractGroundedAction> list = new ArrayList<AbstractGroundedAction>();
			list.add(act);
			HardAffordance aff= new HardAffordance(pfAtom, goal, list);
			AffordanceDelegate affDelegate = new AffordanceDelegate(aff);
			this.affDelegates.add(affDelegate);
		}
	}
	
	
	public void setupAffordances(Domain domain, State state) {
		setupDelegate(domain, mixPFAtoms, MixAction.className, finishedPFAtom);
		setupDelegate(domain, movePFAtoms, MoveAction.className, finishedPFAtom);
		setupDelegate(domain, pourPFAtoms, PourAction.className, finishedPFAtom);
		setupDelegate(domain, handPFAtoms, HandAction.className, finishedPFAtom);
		this.affController = new AffordancesController(affDelegates);
	}
	
	public AffordancesController getAffController() {
		return this.affController;
	}

}