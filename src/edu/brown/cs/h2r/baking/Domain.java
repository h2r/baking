package edu.brown.cs.h2r.baking;


import java.util.*;

import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.PropositionalFunction;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.stocashticgames.SingleAction;
import burlap.oomdp.singleagent.SADomain;


/**
 * Proposed Subclass of James MacGlashan's Domain class (using some of Dave's ideas) such that
 * the program could handle affordances. Currently NOT in use (use has been commented out).
 */
public class Domain extends SADomain {
	
	
	protected List <ObjectClass>						objectClasses;			//list of object classes
	protected Map <String, ObjectClass>					objectClassMap;			//look up object classes by name
	
	protected List <Attribute>							attributes;				//list of attributes
	protected Map <String, Attribute>					attributeMap;			//lookup attributes by name
	
	protected List <PropositionalFunction>				propFunctions;			//list of propositional functions
	protected Map <String, PropositionalFunction> 		propFunctionMap;		//lookup propositional functions by name
	
	protected boolean									nameDependentDomain = false;
	protected HashMap<String, Boolean>					affordances;
	protected boolean										affordanceMode = true;
	protected boolean hasAffordances;
	
	public Domain(){
		
		objectClasses = new ArrayList <ObjectClass>();
		objectClassMap = new HashMap <String, ObjectClass>();
		
		attributes = new ArrayList <Attribute>();
		attributeMap = new HashMap <String, Attribute>();
		
		propFunctions = new ArrayList <PropositionalFunction>();
		propFunctionMap = new HashMap <String, PropositionalFunction>();
		
		hasAffordances = false;
		
	}
	
	// Affordance stuff v1
	/*
	public void setAffordances(HashMap<String,Boolean> affordanceList) {
		affordances = affordanceList;
	}
	
	public HashMap<String,Boolean> getAffordances() {
		this.hasAffordances = true;
		return this.affordances;
	}
	
	public Boolean hasAffordances() {
		return this.hasAffordances;
	}*/
	

	
	/**
	 * Sets whether this domain's states are object name dependent or independent. In an OO-MDP states are represented
	 * as a set of object instances; therefore state equality can either be determined by whether there is a
	 * bijection between the states such that the matched objects have the same value (name independent), or whether the same
	 * object references have the same values (name dependent). For instance, imagine a state s_1 with two objects of the same class,
	 * o_1 and o_2 with value assignments v_a and v_b, respectively. Imagine a corresponding state s_2, also with objects o_1 and
	 * o_2; however, in s_2, the value assignment is o_1=v_b and o_2=v_a. If the domain is name independent, then s_1 == s_2,
	 * because you can match o_1 in s_1 to o_2 in s_2 (and symmetrically for the other objects). However, if the domain is
	 * name dependent, then s_1 != s_2, because the specific object references have different values in each state.
	 * @param nameDependent sets whether this domain's states are object name dependent (true) or not (false).
	 */
	public void setNameDependence(boolean nameDependent){
		this.nameDependentDomain = nameDependent;
	}
	
	
	/**
	 * Returns whether this domain's states are object name dependent. In an OO-MDP states are represented
	 * as a set of object instances; therefore state equality can either be determined by whether there is a
	 * bijection between the states such that the matched objects have the same value (name independent), or whether the same
	 * object references have the same values (name dependent). For instance, imagine a state s_1 with two objects of the same class,
	 * o_1 and o_2 with value assignments v_a and v_b, respectively. Imagine a corresponding state s_2, also with objects o_1 and
	 * o_2; however, in s_2, the value assignment is o_1=v_b and o_2=v_a. If the domain is name independent, then s_1 == s_2,
	 * because you can match o_1 in s_1 to o_2 in s_2 (and symmetrically for the other objects). However, if the domain is
	 * name dependent, then s_1 != s_2, because the specific object references have different values in each state.
	 * @return true if this domain is name dependent and false if it object name independent.
	 */
	public boolean isNameDependent(){
		return this.nameDependentDomain;
	}
	
	
	/**
	 * Will return a new instance of this Domain's class (either SADomain or SGDomain)
	 * @return a new instance of this Domain's class (either SADomain or SGDomain)
	 */
	public Domain newInstance() {
		return new Domain();
	}
	
	/**
	 * This will return a new domain object populated with copies of this Domain's ObjectClasses. Note that propositional
	 * functions and actions are not copied into the new domain
	 * @return a new Domain object with copies of this Domain's ObjectClasses
	 */
	public Domain getNewDomainWithCopiedObjectClasses(){
		Domain d = this.newInstance();
		for(Attribute a : this.attributes){
			a.copy(d);
		}
		
		return d;
	}
	
	public void addObjectClass(ObjectClass oc){
		if(!objectClassMap.containsKey(oc.name)){
			objectClasses.add(oc);
			objectClassMap.put(oc.name, oc);
		}
	}
	
	public void addAttribute(Attribute att){
		if(!attributeMap.containsKey(att.name)){
			attributes.add(att);
			attributeMap.put(att.name, att);
		}
	}
	
	public void addPropositionalFunction(PropositionalFunction prop){
		if(!propFunctionMap.containsKey(prop.getName())){
			propFunctions.add(prop);
			propFunctionMap.put(prop.getName(), prop);
		}
	}
	
	
	public List <ObjectClass> getObjectClasses(){
		return new ArrayList <ObjectClass>(objectClasses);
	}
	
	public ObjectClass getObjectClass(String name){
		return objectClassMap.get(name);
	}
		
	public List <Attribute> getAttributes(){
		return new ArrayList <Attribute>(attributes);
	}
	
	public Attribute getAttribute(String name){
		return attributeMap.get(name);
	}
	
	
	public List <PropositionalFunction> getPropFunctions(){
		return new ArrayList <PropositionalFunction>(propFunctions);
	}
	
	public PropositionalFunction getPropFunction(String name){
		return propFunctionMap.get(name);
	}
	
	// Maps propFuncClass -> propList
	// eg: color -> isWhite, isBlue, isYellow...
	public Map<String, Set<PropositionalFunction>> getPropositionlFunctionsMap() {
		HashMap<String, Set<PropositionalFunction>> propFuncs = new HashMap<String, Set<PropositionalFunction>>();
		for(PropositionalFunction pf : this.propFunctions) {

			String propFuncClass = pf.getClassName();
			Set<PropositionalFunction> propList = propFuncs.get(propFuncClass);
			if(propList == null) {
				propList = new HashSet<PropositionalFunction>();
			}

			propList.add(pf);
			propFuncs.put(propFuncClass, propList);
			
		}
		return propFuncs;
	}

	public Map<String, Set<PropositionalFunction>> getPropositionlFunctionsFromObjectClass(String objectName) {
		HashMap<String, Set<PropositionalFunction>> propFuncs = new HashMap<String, Set<PropositionalFunction>>();
		for(PropositionalFunction pf : this.propFunctions) {
			for(String paramClass : pf.getParameterClasses()) {
				if(paramClass.equals(objectName)) {
					String propFuncClass = pf.getClassName();
					Set<PropositionalFunction> propList = propFuncs.get(propFuncClass);
					if(propList == null) {
						propList = new HashSet<PropositionalFunction>();
					}

					propList.add(pf);
					propFuncs.put(propFuncClass, propList);
				}
			}
		}
		return propFuncs;
	}

}
