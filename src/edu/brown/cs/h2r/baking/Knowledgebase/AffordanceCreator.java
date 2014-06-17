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
	
	
	//private ArrayList<AbstractGroundedAction> meltActions = new ArrayList<AbstractGroundedAction>();
	//private ArrayList<AbstractGroundedAction> mixActions = new ArrayList<AbstractGroundedAction>();
	//private ArrayList<AbstractGroundedAction> bakeActions = new ArrayList<AbstractGroundedAction>();
	//private ArrayList<AbstractGroundedAction> moveActions = new ArrayList<AbstractGroundedAction>();
	//private ArrayList<AbstractGroundedAction> pourActions = new ArrayList<AbstractGroundedAction>();
	
	private ArrayList<PFAtom> meltPFAtoms;
	private ArrayList<PFAtom> mixPFAtoms;
	private ArrayList<PFAtom> bakePFAtoms;
	private ArrayList<PFAtom> movePFAtoms;
	private ArrayList<PFAtom> pourPFAtoms;
	private ArrayList<PFAtom> finishedPFAtoms;
	private ArrayList<PFAtom> botchedPFAtoms;
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
		
		
		//setupActions(domain, state);
		setupPFAtoms(domain, state);
		setupAffordances(domain, state);
	}
	
	
	/*public void setupActions(Domain domain, State state) {
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
	}*/
	
	
	public void setupPFAtoms(Domain domain, State state) {
		PropositionalFunction meltPF = domain.getPropFunction(MELTPF);
		this.meltPFAtoms = new ArrayList<PFAtom>();
		List<GroundedProp> meltGroundedProps = state.getAllGroundedPropsFor(meltPF);
		for (GroundedProp meltGroundedProp : meltGroundedProps) {
			this.meltPFAtoms.add(new PFAtom(meltGroundedProp));
		}
		
		PropositionalFunction mixPF = domain.getPropFunction(MIXPF);
		this.mixPFAtoms = new ArrayList<PFAtom>();
		List<GroundedProp> mixGroundedProps = state.getAllGroundedPropsFor(mixPF);
		for (GroundedProp mixGroundedProp : mixGroundedProps) {
			this.mixPFAtoms.add(new PFAtom(mixGroundedProp));
		}
		
		//PropositionalFunction bakePF = domain.getPropFunction(BAKEPF);
		//List<GroundedProp> bakeGroundedProps = state.getAllGroundedPropsFor(bakePF);
		//GroundedProp bakeGroundedProp = bakeGroundedProps.get(0);
		//this.bakePFAtom = new PFAtom(bakeGroundedProp);
		
		PropositionalFunction movePF = domain.getPropFunction(MOVEPF);
		this.movePFAtoms = new ArrayList<PFAtom>();
		List<GroundedProp> moveGroundedProps = state.getAllGroundedPropsFor(movePF);
		for (GroundedProp moveGroundedProp : moveGroundedProps) {
			this.movePFAtoms.add(new PFAtom(moveGroundedProp));
		}
		
		PropositionalFunction pourPF = domain.getPropFunction(POURPF);
		this.pourPFAtoms = new ArrayList<PFAtom>();
		List<GroundedProp> pourGroundedProps = state.getAllGroundedPropsFor(pourPF);
		for (GroundedProp pourGroundedProp : pourGroundedProps) {
			this.pourPFAtoms.add(new PFAtom(pourGroundedProp));
		}
		
		PropositionalFunction finishedPF = domain.getPropFunction(FINISHPF);
		this.finishedPFAtoms = new ArrayList<PFAtom>();
		List<GroundedProp> finishedGroundedProps = state.getAllGroundedPropsFor(finishedPF);
		for (GroundedProp finishedGroundedProp : finishedGroundedProps) {
			this.finishedPFAtoms.add(new PFAtom(finishedGroundedProp));
		}
		this.finishedPFAtom = this.finishedPFAtoms.get(0);
		
		PropositionalFunction botchedPF = domain.getPropFunction(BOTCHEDPF);
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
		for (PFAtom meltPFAtom : meltPFAtoms) {
			AbstractGroundedAction act = new GroundedAction(domain.getAction("melt"), meltPFAtom.getGroundedProp().params);
			List<AbstractGroundedAction> list = new ArrayList<AbstractGroundedAction>();
			list.add(act);
			HardAffordance meltAff= new HardAffordance(meltPFAtom, finishedPFAtom, list);
			AffordanceDelegate meltDelegate = new AffordanceDelegate(meltAff);
			affDelegates.add(meltDelegate);
		}
		
		for (PFAtom mixPFAtom : mixPFAtoms) {
			AbstractGroundedAction act = new GroundedAction(domain.getAction("mix"), mixPFAtom.getGroundedProp().params);
			List<AbstractGroundedAction> list = new ArrayList<AbstractGroundedAction>();
			list.add(act);
			HardAffordance meltAff= new HardAffordance(mixPFAtom, finishedPFAtom, list);
			AffordanceDelegate mixDelegate = new AffordanceDelegate(meltAff);
			affDelegates.add(mixDelegate);
		}
		
		for (PFAtom movePFAtom : movePFAtoms) {
			AbstractGroundedAction act = new GroundedAction(domain.getAction("move"), movePFAtom.getGroundedProp().params);
			List<AbstractGroundedAction> list = new ArrayList<AbstractGroundedAction>();
			list.add(act);
			HardAffordance moveAff= new HardAffordance(movePFAtom, finishedPFAtom, list);
			AffordanceDelegate moveDelegate = new AffordanceDelegate(moveAff);
			affDelegates.add(moveDelegate);
		}
		for (PFAtom pourPFAtom : pourPFAtoms) {
			AbstractGroundedAction act = new GroundedAction(domain.getAction("pour"), pourPFAtom.getGroundedProp().params);
			List<AbstractGroundedAction> list = new ArrayList<AbstractGroundedAction>();
			list.add(act);
			HardAffordance pourAff= new HardAffordance(pourPFAtom, finishedPFAtom, list);
			AffordanceDelegate pourDelegate = new AffordanceDelegate(pourAff);
			affDelegates.add(pourDelegate);
		}
		this.affController = new AffordancesController(affDelegates);
	}
	
	public AffordancesController getAffController() {
		return this.affController;
	}

}
