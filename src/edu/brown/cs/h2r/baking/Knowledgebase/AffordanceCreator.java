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
	public static final String MELT_PF = "meltPF";
	public static final String BAKE_PF = "bakePF";
	public static final String MIX_PF = "mixPF";
	public static final String MOVE_PF = "movePF";
	public static final String POUR_PF = "pourPF";
	public static final String PEEL_PF = "peelPF";
	public static final String USE_PF = "usePF";
	public static final String SWITCH_PF = "switchPF";
	public static final String GREASE_PF = "greasePF";
	public static final String FINISH_PF = "success";
	public static final String BOTCHED_PF = "botched";
	public static final String SPACEON_PF = "spaceOnPF";
	public static final String CONTAINERGREASED_PF = "containerGreasedPF";
	public static final String INGREDIENT_PF = "ingredientPF";
	
	//private ArrayList<PFAtom> meltPFAtoms;
	private ArrayList<PFAtom> mixPFAtoms;
	//private ArrayList<PFAtom> bakePFAtoms;
	private ArrayList<PFAtom> movePFAtoms;
	private ArrayList<PFAtom> pourPFAtoms;
	private ArrayList<PFAtom> peelPFAtoms;
	private ArrayList<PFAtom> greasePFAtoms;
	private ArrayList<PFAtom> switchPFAtoms;
	private ArrayList<PFAtom> usePFAtoms;
	private ArrayList<PFAtom> containerGreasedPFAtoms;
	private ArrayList<PFAtom> spaceOnPFAtoms;
	private ArrayList<PFAtom> finishedPFAtoms;
	private ArrayList<PFAtom> botchedPFAtoms;
	private PFAtom spaceOnPFAtom;
	private PFAtom containerGreasedPFAtom;
	private PFAtom finishedPFAtom;
	private PFAtom botchedPFAtom;
	private AffordancesController affController;
	
	public AffordanceCreator(Domain domain, State state, IngredientRecipe ingredient) {
		// Add prop functions to Domain
		//final PropositionalFunction allowMelting = new AllowMelting(AffordanceCreator.MELT_PF, domain, ingredient);
		final PropositionalFunction allowMixing = new AllowMixing(AffordanceCreator.MIX_PF, domain, ingredient);
		final PropositionalFunction allowPouring= new AllowPouring(AffordanceCreator.POUR_PF, domain, ingredient);
		final PropositionalFunction allowMoving = new AllowMoving(AffordanceCreator.MOVE_PF, domain, ingredient);
		final PropositionalFunction allowPeeling = new AllowPeeling(AffordanceCreator.PEEL_PF, domain, ingredient);
		final PropositionalFunction allowGreasing = new AllowGreasing(AffordanceCreator.GREASE_PF, domain, ingredient);
		final PropositionalFunction allowSwitching = new AllowSwitching(AffordanceCreator.SWITCH_PF, domain, ingredient);
		final PropositionalFunction allowUsing = new AllowUsingTool(AffordanceCreator.USE_PF, domain, ingredient);
		final PropositionalFunction isSuccess = new RecipeFinished(AffordanceCreator.FINISH_PF, domain, ingredient);
		final PropositionalFunction isFailure = new RecipeBotched(AffordanceCreator.BOTCHED_PF, domain, ingredient);
		final PropositionalFunction spaceOn = new SpaceOn(AffordanceCreator.SPACEON_PF, domain, ingredient, "");
		final PropositionalFunction containerGreased = new ContainerGreased(AffordanceCreator.CONTAINERGREASED_PF, domain, ingredient);
		//final PropositionalFunction allowBaking = new AllowBaking(AffordanceCreator.BAKE_PF, domain, ingredient);
		//final PropositionalFunction ingNecessary = new IngredientNecessaryForRecipe(AffordanceCreator.INGREDIENTPF, domain, ingredient);
		
		setupPFAtoms(domain, state);
		setupAffordances(domain, state);
	}
	
	public void setupPFAtoms(Domain domain, State state) {
		/*PropositionalFunction meltPF = domain.getPropFunction(AffordanceCreator.MELT_PF);
		this.meltPFAtoms = new ArrayList<PFAtom>();
		List<GroundedProp> meltGroundedProps = state.getAllGroundedPropsFor(meltPF);
		for (GroundedProp meltGroundedProp : meltGroundedProps) {
			this.meltPFAtoms.add(new PFAtom(meltGroundedProp));
		}*/
		
		PropositionalFunction mixPF = domain.getPropFunction(AffordanceCreator.MIX_PF);
		this.mixPFAtoms = new ArrayList<PFAtom>();
		List<GroundedProp> mixGroundedProps = mixPF.getAllGroundedPropsForState(state);
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
		
		PropositionalFunction peelPF = domain.getPropFunction(AffordanceCreator.PEEL_PF);
		this.peelPFAtoms = new ArrayList<PFAtom>();
		List<GroundedProp> peelGroundedProps = peelPF.getAllGroundedPropsForState(state);
		for (GroundedProp peelGroundedProp : peelGroundedProps) {
			this.peelPFAtoms.add(new PFAtom(peelGroundedProp));
		}
		
		PropositionalFunction greasePF = domain.getPropFunction(AffordanceCreator.GREASE_PF);
		this.greasePFAtoms = new ArrayList<PFAtom>();
		List<GroundedProp> greaseGroundedProps = greasePF.getAllGroundedPropsForState(state);
		for (GroundedProp greaseGroundedProp : greaseGroundedProps) {
			this.greasePFAtoms.add(new PFAtom(greaseGroundedProp));
		}
		
		PropositionalFunction switchPF = domain.getPropFunction(AffordanceCreator.SWITCH_PF);
		this.switchPFAtoms = new ArrayList<PFAtom>();
		List<GroundedProp> switchGroundedProps = switchPF.getAllGroundedPropsForState(state);
		for (GroundedProp switchGroundedProp : switchGroundedProps) {
			this.switchPFAtoms.add(new PFAtom(switchGroundedProp));
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
		
		PropositionalFunction spaceOnPF = domain.getPropFunction(AffordanceCreator.SPACEON_PF);
		this.spaceOnPFAtoms = new ArrayList<PFAtom>();
		List<GroundedProp> spaceOnGroundedProps = spaceOnPF.getAllGroundedPropsForState(state);
		for (GroundedProp spaceOnGroundedProp : spaceOnGroundedProps) {
			this.spaceOnPFAtoms.add(new PFAtom(spaceOnGroundedProp));
		}
		this.spaceOnPFAtom = this.spaceOnPFAtoms.get(0);
		
		PropositionalFunction containerGreasedPF = domain.getPropFunction(AffordanceCreator.CONTAINERGREASED_PF);
		this.containerGreasedPFAtoms = new ArrayList<PFAtom>();
		List<GroundedProp> containerGreasedGroundedProps = containerGreasedPF.getAllGroundedPropsForState(state);
		for (GroundedProp containerGreasedGroundedProp : containerGreasedGroundedProps) {
			this.containerGreasedPFAtoms.add(new PFAtom(containerGreasedGroundedProp));
		}
		this.containerGreasedPFAtom = this.containerGreasedPFAtoms.get(0);
		
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
		}
		
		for (PFAtom meltPFAtom : meltPFAtoms) {
			AbstractGroundedAction act = new GroundedAction(domain.getAction(MeltAction.className), meltPFAtom.getGroundedProp().params);
			List<AbstractGroundedAction> list = new ArrayList<AbstractGroundedAction>();
			list.add(act);
			HardAffordance meltAff= new HardAffordance(meltPFAtom, finishedPFAtom, list);
			AffordanceDelegate meltDelegate = new AffordanceDelegate(meltAff);
			affDelegates.add(meltDelegate);
		}*/
		
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
		
		for (PFAtom greasePFAtom : greasePFAtoms) {
			AbstractGroundedAction act = new GroundedAction(domain.getAction(GreaseAction.className), greasePFAtom.getGroundedProp().params);
			List<AbstractGroundedAction> list = new ArrayList<AbstractGroundedAction>();
			list.add(act);
			HardAffordance greaseAff= new HardAffordance(greasePFAtom,  containerGreasedPFAtom, list);
			AffordanceDelegate greaseDelegate = new AffordanceDelegate(greaseAff);
			affDelegates.add(greaseDelegate);
		}
		
		for (PFAtom switchPFAtom : switchPFAtoms) {
			AbstractGroundedAction act = new GroundedAction(domain.getAction(SwitchAction.className), switchPFAtom.getGroundedProp().params);
			List<AbstractGroundedAction> list = new ArrayList<AbstractGroundedAction>();
			list.add(act);
			HardAffordance switchAff= new HardAffordance(switchPFAtom, spaceOnPFAtom, list);
			AffordanceDelegate switchDelegate = new AffordanceDelegate(switchAff);
			affDelegates.add(switchDelegate);
		}
		
		this.affController = new AffordancesController(affDelegates);

	}
	
	public AffordancesController getAffController() {
		return this.affController;
	}

}