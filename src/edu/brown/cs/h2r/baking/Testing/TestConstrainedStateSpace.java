package edu.brown.cs.h2r.baking.Testing;

import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import burlap.behavior.singleagent.planning.OOMDPPlanner;
import burlap.behavior.singleagent.planning.StateConditionTest;
import burlap.behavior.singleagent.planning.deterministic.uninformed.bfs.BFS;
import burlap.behavior.statehashing.NameDependentStateHashFactory;
import burlap.behavior.statehashing.StateHashFactory;
import burlap.behavior.statehashing.StateHashTuple;
import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;
import burlap.oomdp.core.State;
import burlap.oomdp.singleagent.Action;
import burlap.oomdp.singleagent.SADomain;
import edu.brown.cs.h2r.baking.ConstrainedStateSpaceExplorer;

public class TestConstrainedStateSpace {
	
	Domain domain;
	OOMDPPlanner planner;
	int goalValue;
	StateHashFactory stateHash;
	@Before
	public void setup() {
		this.domain = new SADomain();
		ObjectClass valueClass = new ObjectClass(this.domain, "Value");
		Attribute valueAtt = new Attribute(this.domain, "value", Attribute.AttributeType.INT);
		valueAtt.setDiscValuesForRange(-100, 100, 1);
		valueClass.addAttribute(valueAtt);
		this.domain.addObjectClass(valueClass);
		
		Action add = new AddAction("add", this.domain);
		//Action subtract = new SubtractAction("subtract", this.domain);
		Action multiply = new MultiplyAction("multiple", this.domain);
		this.domain.addAction(add);
		//this.domain.addAction(subtract);
		this.domain.addAction(multiply);
		
		this.goalValue = 10;
		StateConditionTest gc = new StateConditionTest() {
			@Override
			public boolean satisfies(State s) {
				for (ObjectInstance obj : s.getObjectsOfTrueClass("Value")) {
					return  obj.getDiscValForAttribute("value") == TestConstrainedStateSpace.this.goalValue;
				}
				return false;
			}
		};
		this.stateHash = new NameDependentStateHashFactory();
		this.planner = new BFS(domain, gc, this.stateHash);
	}
	
	@Test
	public void testExplorer() {
		State startState = new State();
		ObjectInstance value = new ObjectInstance(this.domain.getObjectClass("Value"), "val");
		startState.addObject(value);
		for (int startValue = -49; startValue < 50; startValue ++ )
		{
			value = startState.getObject("val");
			value.setValue("value", startValue);
			
			ConstrainedStateSpaceExplorer explorer = new ConstrainedStateSpaceExplorer(this.planner, this.domain.getActions());
			List<StateHashTuple> states = explorer.getConstrainedStatesAccessibleFromState(startState);
			
			Set<Integer> values = new TreeSet<Integer>();
			for (StateHashTuple state : states) {
				value = state.s.getObject("val");
				values.add(value.getDiscValForAttribute("value"));
			}
			
			if (startValue > this.goalValue){ 
				Assert.assertEquals(0, states.size());
			}
			else
			{
				Assert.assertEquals(this.goalValue - startValue + 1, states.size());
			}
		}
	}
	
	public abstract class TestAction extends Action {
		public TestAction(String name, Domain domain) {
			super(name, domain, "");
		}
		@Override
		public boolean applicableInState(State s, String[] params) {
			for (ObjectInstance obj : s.getObjectsOfTrueClass("Value")) {
				int value = obj.getDiscValForAttribute("value");
				return value > -50 && value < 50 ;
			}
			return false;
		}
	}
	public class AddAction extends TestAction {
		public AddAction(String name, Domain domain) {
			super(name, domain);
		}
		
		@Override
		protected State performActionHelper(State s, String[] params) {
			for (ObjectInstance obj : s.getObjectsOfTrueClass("Value")) {
				int value = obj.getDiscValForAttribute("value");
				obj.setValue("value", value + 1);
			}
			return s;
		}
	}
	
	public class SubtractAction extends TestAction {
		public SubtractAction(String name, Domain domain) {
			super(name, domain);
		}
		@Override
		protected State performActionHelper(State s, String[] params) {
			for (ObjectInstance obj : s.getObjectsOfTrueClass("Value")) {
				int value = obj.getDiscValForAttribute("value");
				obj.setValue("value", value - 1);
			}
			return s;
		}
	}
	
	public class MultiplyAction extends TestAction {
		public MultiplyAction(String name, Domain domain) {
			super(name, domain);
		}
		@Override
		protected State performActionHelper(State s, String[] params) {
			for (ObjectInstance obj : s.getObjectsOfTrueClass("Value")) {
				int value = obj.getDiscValForAttribute("value");
				value *= (value < 0) ? -2 : 2;
				obj.setValue("value", value);
			}
			return s;
		}
	}
}
