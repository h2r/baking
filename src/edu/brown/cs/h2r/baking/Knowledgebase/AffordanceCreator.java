/*package edu.brown.cs.h2r.baking.Knowledgebase;

import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.PropositionalFunctions.*;
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
	public static final String MELTPF = "meltPF";
	public static final String BAKEPF = "bakePF";
	public static final String MIXPF = "mixPF";
	public static final String MOVEPF = "movePF";
	public static final String POURPF = "pourPF";
	public static final String FINISHPF = "success";
	public static final String BOTCHEDPF = "botched";
	public static final String INGREDIENTPF = "ingredientPF";
	
	
	private ArrayList<AbstractGroundedAction> meltActions = new ArrayList<AbstractGroundedAction>();
	private ArrayList<AbstractGroundedAction> mixActions = new ArrayList<AbstractGroundedAction>();
	private ArrayList<AbstractGroundedAction> bakeActions = new ArrayList<AbstractGroundedAction>();
	private ArrayList<AbstractGroundedAction> moveActions = new ArrayList<AbstractGroundedAction>();
	private ArrayList<AbstractGroundedAction> pourActions = new ArrayList<AbstractGroundedAction>();
	
	private PFAtom meltPFAtom;
	private PFAtom mixPFAtom;
	private PFAtom bakePFAtom;
	private PFAtom movePFAtom;
	private PFAtom pourPFAtom;
	private PFAtom finishedPFAtom;
	private PFAtom botchedPFAtom;
	
	private AffordancesController affController;
	
	public AffordanceCreator(Domain domain, State state, IngredientRecipe ingredient) {
		// Add prop functions to Domain
		final PropositionalFunction allowMelting = new AllowMelting(AffordanceCreator.MELTPF, domain, ingredient);
		final PropositionalFunction allowMixing = new AllowMixing(AffordanceCreator.MIXPF, domain, ingredient);
		final PropositionalFunction allowPouring= new AllowPouring(AffordanceCreator.POURPF, domain, ingredient);
		final PropositionalFunction allowMoving = new AllowMoving(AffordanceCreator.MOVEPF, domain, ingredient);
		//final PropositionalFunction shouldPour = new ShouldPour(AffordanceCreator.SHOULDPOURPF, domain, ingredient);
		//final PropositionalFunction isSuccess = new RecipeFinished(AffordanceCreator.FINISHPF, domain, ingredient);
		//final PropositionalFunction isFailure = new RecipeBotched(AffordanceCreator.BOTCHEDPF, domain, ingredient);
		//final PropositionalFunction allowBaking = new AllowBaking(AffordanceCreator.BAKEPF, domain, ingredient);
		//final PropositionalFunction ingNecessary = new IngredientNecessaryForRecipe(AffordanceCreator.INGREDIENTPF, domain, ingredient);
		
		
		setupActions(domain, state);
		setupPFAtoms(domain, state);
		setupAffordances(domain, state);
	}
	
	
	public void setupActions(Domain domain, State state) {
		for (GroundedAction a : state.getAllGroundedActionsFor(domain.getAction("melt"))) {
			this.meltActions.add(a);
		}
		for (GroundedAction a : state.getAllGroundedActionsFor(domain.getAction("mix"))) {
			this.mixActions.add(a);
		}
		for (GroundedAction a : state.getAllGroundedActionsFor(domain.getAction("move"))) {
			this.moveActions.add(a);
		}
		for (GroundedAction a : state.getAllGroundedActionsFor(domain.getAction("pour"))) {
			this.pourActions.add(a);
		}
		//for (GroundedAction a : state.getAllGroundedActionsFor(domain.getAction("bake"))) {
		//	this.bakeActions.add(a);
		//}
	}
	
	
	public void setupPFAtoms(Domain domain, State state) {
		PropositionalFunction meltPF = domain.getPropFunction(MELTPF);
		List<GroundedProp> meltGroundedProps = state.getAllGroundedPropsFor(meltPF);
		GroundedProp meltGroundedProp = meltGroundedProps.get(0);
		this.meltPFAtom = new PFAtom(meltGroundedProp);
		
		PropositionalFunction mixPF = domain.getPropFunction(MIXPF);
		List<GroundedProp> mixGroundedProps = state.getAllGroundedPropsFor(mixPF);
		GroundedProp mixGroundedProp = mixGroundedProps.get(0);
		this.mixPFAtom = new PFAtom(mixGroundedProp);
		
		//PropositionalFunction bakePF = domain.getPropFunction(BAKEPF);
		//List<GroundedProp> bakeGroundedProps = state.getAllGroundedPropsFor(bakePF);
		//GroundedProp bakeGroundedProp = bakeGroundedProps.get(0);
		//this.bakePFAtom = new PFAtom(bakeGroundedProp);
		
		PropositionalFunction movePF = domain.getPropFunction(MOVEPF);
		List<GroundedProp> moveGroundedProps = state.getAllGroundedPropsFor(movePF);
		GroundedProp moveGroundedProp = moveGroundedProps.get(0);
		this.movePFAtom = new PFAtom(moveGroundedProp);
		
		PropositionalFunction pourPF = domain.getPropFunction(POURPF);
		List<GroundedProp> pourGroundedProps = state.getAllGroundedPropsFor(pourPF);
		GroundedProp pourGroundedProp = pourGroundedProps.get(0);
		this.pourPFAtom = new PFAtom(pourGroundedProp);
		
		PropositionalFunction finishedPF = domain.getPropFunction(FINISHPF);
		List<GroundedProp> finishedGroundedProps = state.getAllGroundedPropsFor(finishedPF);
		GroundedProp finishedGroundedProp = finishedGroundedProps.get(0);
		this.finishedPFAtom = new PFAtom(finishedGroundedProp);
		
		PropositionalFunction botchedPF = domain.getPropFunction(BOTCHEDPF);
		List<GroundedProp> botchedGroundedProps = state.getAllGroundedPropsFor(botchedPF);
		GroundedProp botchedGroundedProp = botchedGroundedProps.get(0);
		this.botchedPFAtom = new PFAtom(botchedGroundedProp);
		
		// unsure as to where this is going to be used??
		//PropositionalFunction pourPF = domain.getPropFunction(POURPF);
		//List<GroundedProp> pourGroundedProps = state.getAllGroundedPropsFor(pourPF);
		//GroundedProp pourGroundedProp = pourGroundedProps.get(0);
		//PFAtom pourPFAtom = new PFAtom(pourGroundedProp);
	}
	
	public void setupAffordances(Domain domain, State state) {
		
		HardAffordance meltAff= new HardAffordance(meltPFAtom, finishedPFAtom, this.meltActions);
		AffordanceDelegate meltDelegate = new AffordanceDelegate(meltAff);
		
		HardAffordance mixAff= new HardAffordance(mixPFAtom, finishedPFAtom, this.mixActions);
		AffordanceDelegate mixDelegate = new AffordanceDelegate(mixAff);
		
		//HardAffordance bakeAff= new HardAffordance(meltPFAtom, finishedPFAtom, this.bakeActions);
		//AffordanceDelegate bakeDelegate = new AffordanceDelegate(bakeAff);
		
		HardAffordance moveAff= new HardAffordance(movePFAtom, finishedPFAtom, this.moveActions);
		AffordanceDelegate moveDelegate = new AffordanceDelegate(moveAff);
		
		HardAffordance pourAff= new HardAffordance(pourPFAtom, finishedPFAtom, this.pourActions);
		AffordanceDelegate pourDelegate = new AffordanceDelegate(pourAff);
		
		List<AffordanceDelegate> affDelegates = new ArrayList<AffordanceDelegate>();
		affDelegates.add(meltDelegate);
		affDelegates.add(mixDelegate);
		//affDelegates.add(bakeDelegate);
		affDelegates.add(moveDelegate);
		affDelegates.add(pourDelegate);
		
		this.affController = new AffordancesController(affDelegates);
	}
	
	public AffordancesController getAffController() {
		return this.affController;
	}
}*/
package edu.brown.cs.h2r.baking.Knowledgebase;

import java.util.ArrayList;
import java.util.List;

import edu.brown.cs.h2r.baking.IngredientRecipe;
import edu.brown.cs.h2r.baking.PropositionalFunctions.*;
import edu.brown.cs.h2r.baking.actions.BakeAction;
import edu.brown.cs.h2r.baking.actions.MeltAction;
import edu.brown.cs.h2r.baking.actions.MixAction;
import edu.brown.cs.h2r.baking.actions.MoveAction;
import edu.brown.cs.h2r.baking.actions.PeelAction;
import edu.brown.cs.h2r.baking.actions.PourAction;
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
	public static final String MELT_PF = "meltPF";
	public static final String BAKE_PF = "bakePF";
	public static final String MIX_PF = "mixPF";
	public static final String MOVE_PF = "movePF";
	public static final String POUR_PF = "pourPF";
	public static final String PEEL_PF = "peelPF";
	public static final String FINISH_PF = "success";
	public static final String BOTCHED_PF = "botched";
	public static final String INGREDIENT_PF = "ingredientPF";
	
	private ArrayList<PFAtom> meltPFAtoms;
	private ArrayList<PFAtom> mixPFAtoms;
	private ArrayList<PFAtom> bakePFAtoms;
	private ArrayList<PFAtom> movePFAtoms;
	private ArrayList<PFAtom> pourPFAtoms;
	private ArrayList<PFAtom> peelPFAtoms;
	private ArrayList<PFAtom> finishedPFAtoms;
	private ArrayList<PFAtom> botchedPFAtoms;
	private PFAtom finishedPFAtom;
	private PFAtom botchedPFAtom;
	private AffordancesController affController;
	
	public AffordanceCreator(Domain domain, State state, IngredientRecipe ingredient) {
		// Add prop functions to Domain
		final PropositionalFunction allowMelting = new AllowMelting(AffordanceCreator.MELT_PF, domain, ingredient);
		final PropositionalFunction allowMixing = new AllowMixing(AffordanceCreator.MIX_PF, domain, ingredient);
		final PropositionalFunction allowPouring= new AllowPouring(AffordanceCreator.POUR_PF, domain, ingredient);
		final PropositionalFunction allowMoving = new AllowMoving(AffordanceCreator.MOVE_PF, domain, ingredient);
		final PropositionalFunction allowPeeling = new AllowPeeling(AffordanceCreator.PEEL_PF, domain, ingredient);
		final PropositionalFunction isSuccess = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, ingredient);
		final PropositionalFunction isFailure = new RecipeBotched(AffordanceCreator.BOTCHED_PF, domain, ingredient);
		//final PropositionalFunction allowBaking = new AllowBaking(AffordanceCreator.BAKE_PF, domain, ingredient);
		//final PropositionalFunction ingNecessary = new IngredientNecessaryForRecipe(AffordanceCreator.INGREDIENTPF, domain, ingredient);
		
		setupPFAtoms(domain, state);
		setupAffordances(domain, state);
	}
	
	public void setupPFAtoms(Domain domain, State state) {
		PropositionalFunction meltPF = domain.getPropFunction(AffordanceCreator.MELT_PF);
		this.meltPFAtoms = new ArrayList<PFAtom>();
		List<GroundedProp> meltGroundedProps = state.getAllGroundedPropsFor(meltPF);
		for (GroundedProp meltGroundedProp : meltGroundedProps) {
			this.meltPFAtoms.add(new PFAtom(meltGroundedProp));
		}
		
		PropositionalFunction mixPF = domain.getPropFunction(AffordanceCreator.MIX_PF);
		this.mixPFAtoms = new ArrayList<PFAtom>();
		List<GroundedProp> mixGroundedProps = state.getAllGroundedPropsFor(mixPF);
		for (GroundedProp mixGroundedProp : mixGroundedProps) {
			this.mixPFAtoms.add(new PFAtom(mixGroundedProp));
		}
		
		/*PropositionalFunction bakePF = domain.getPropFunction(AffordanceCreator.BAKE_PF);
		this.bakePFAtoms = new ArrayList<PFAtom>();
		List<GroundedProp> bakeGroundedProps = state.getAllGroundedPropsFor(bakePF);
		for (GroundedProp bakeGroundedProp : bakeGroundedProps) {
			this.bakePFAtoms.add( new PFAtom(bakeGroundedProp));
		}*/
		
		PropositionalFunction movePF = domain.getPropFunction(AffordanceCreator.MOVE_PF);
		this.movePFAtoms = new ArrayList<PFAtom>();
		List<GroundedProp> moveGroundedProps = state.getAllGroundedPropsFor(movePF);
		for (GroundedProp moveGroundedProp : moveGroundedProps) {
			this.movePFAtoms.add(new PFAtom(moveGroundedProp));
		}
		
		PropositionalFunction pourPF = domain.getPropFunction(AffordanceCreator.POUR_PF);
		this.pourPFAtoms = new ArrayList<PFAtom>();
		List<GroundedProp> pourGroundedProps = state.getAllGroundedPropsFor(pourPF);
		for (GroundedProp pourGroundedProp : pourGroundedProps) {
			this.pourPFAtoms.add(new PFAtom(pourGroundedProp));
		}
		
		PropositionalFunction peelPF = domain.getPropFunction(AffordanceCreator.PEEL_PF);
		this.peelPFAtoms = new ArrayList<PFAtom>();
		List<GroundedProp> peelGroundedProps = state.getAllGroundedPropsFor(peelPF);
		for (GroundedProp peelGroundedProp : peelGroundedProps) {
			this.peelPFAtoms.add(new PFAtom(peelGroundedProp));
		}
		
		PropositionalFunction finishedPF = domain.getPropFunction(AffordanceCreator.FINISH_PF);
		this.finishedPFAtoms = new ArrayList<PFAtom>();
		List<GroundedProp> finishedGroundedProps = state.getAllGroundedPropsFor(finishedPF);
		for (GroundedProp finishedGroundedProp : finishedGroundedProps) {
			this.finishedPFAtoms.add(new PFAtom(finishedGroundedProp));
		}
		this.finishedPFAtom = this.finishedPFAtoms.get(0);
		
		PropositionalFunction botchedPF = domain.getPropFunction(AffordanceCreator.BOTCHED_PF);
		this.botchedPFAtoms = new ArrayList<PFAtom>();
		List<GroundedProp> botchedGroundedProps = state.getAllGroundedPropsFor(botchedPF);
		for (GroundedProp botchedGroundedProp : botchedGroundedProps) {
			this.botchedPFAtoms.add(new PFAtom(botchedGroundedProp));
		}
		this.botchedPFAtom = this.botchedPFAtoms.get(0);
		
		// unsure as to where this is going to be used??
		//PropositionalFunction pourPF = domain.getPropFunction(POURPF);
		//List<GroundedProp> pourGroundedProps = state.getAllGroundedPropsFor(pourPF);
		//GroundedProp pourGroundedProp = pourGroundedProps.get(0);
		//PFAtom pourPFAtom = new PFAtom(pourGroundedProp);
	}
	
	public void setupAffordances(Domain domain, State state) {
		List<AffordanceDelegate> affDelegates = new ArrayList<AffordanceDelegate>();
		/*for (PFAtom bakePFAtom : bakePFAtoms) {
			AbstractGroundedAction act = new GroundedAction(domain.getAction(BakeAction.className), bakePFAtom.getGroundedProp().params);
			List<AbstractGroundedAction> list = new ArrayList<AbstractGroundedAction>();
			list.add(act);
			HardAffordance bakeAff= new HardAffordance(bakePFAtom, finishedPFAtom, list);
			AffordanceDelegate bakeDelegate = new AffordanceDelegate(bakeAff);
			affDelegates.add(bakeDelegate);
		}*/
		
		for (PFAtom meltPFAtom : meltPFAtoms) {
			AbstractGroundedAction act = new GroundedAction(domain.getAction(MeltAction.className), meltPFAtom.getGroundedProp().params);
			List<AbstractGroundedAction> list = new ArrayList<AbstractGroundedAction>();
			list.add(act);
			HardAffordance meltAff= new HardAffordance(meltPFAtom, finishedPFAtom, list);
			AffordanceDelegate meltDelegate = new AffordanceDelegate(meltAff);
			affDelegates.add(meltDelegate);
		}
		
		for (PFAtom mixPFAtom : mixPFAtoms) {
			AbstractGroundedAction act = new GroundedAction(domain.getAction(MixAction.className), mixPFAtom.getGroundedProp().params);
			List<AbstractGroundedAction> list = new ArrayList<AbstractGroundedAction>();
			list.add(act);
			HardAffordance mixAff= new HardAffordance(mixPFAtom, finishedPFAtom, list);
			AffordanceDelegate mixDelegate = new AffordanceDelegate(mixAff);
			affDelegates.add(mixDelegate);
		}
		
		for (PFAtom movePFAtom : movePFAtoms) {
			AbstractGroundedAction act = new GroundedAction(domain.getAction(MoveAction.className), movePFAtom.getGroundedProp().params);
			List<AbstractGroundedAction> list = new ArrayList<AbstractGroundedAction>();
			list.add(act);
			HardAffordance moveAff= new HardAffordance(movePFAtom, finishedPFAtom, list);
			AffordanceDelegate moveDelegate = new AffordanceDelegate(moveAff);
			affDelegates.add(moveDelegate);
		}
		
		for (PFAtom pourPFAtom : pourPFAtoms) {
			AbstractGroundedAction act = new GroundedAction(domain.getAction(PourAction.className), pourPFAtom.getGroundedProp().params);
			List<AbstractGroundedAction> list = new ArrayList<AbstractGroundedAction>();
			list.add(act);
			HardAffordance pourAff= new HardAffordance(pourPFAtom, finishedPFAtom, list);
			AffordanceDelegate pourDelegate = new AffordanceDelegate(pourAff);
			affDelegates.add(pourDelegate);
		}
		
		for (PFAtom peelPFAtom : peelPFAtoms) {
			AbstractGroundedAction act = new GroundedAction(domain.getAction(PeelAction.className), peelPFAtom.getGroundedProp().params);
			List<AbstractGroundedAction> list = new ArrayList<AbstractGroundedAction>();
			list.add(act);
			HardAffordance peelAff= new HardAffordance(peelPFAtom, finishedPFAtom, list);
			AffordanceDelegate peelDelegate = new AffordanceDelegate(peelAff);
			affDelegates.add(peelDelegate);
		}
		
		this.affController = new AffordancesController(affDelegates);
		//this.affController = new AffordancesController(new ArrayList<AffordanceDelegate>());

	}
	
	public AffordancesController getAffController() {
		return this.affController;
	}

}