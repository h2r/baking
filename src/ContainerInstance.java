import java.util.ArrayList;
import java.util.Random;
import java.util.List;

import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;


public abstract class ContainerInstance extends ObjectInstance {

	List<IngredientInstance> contents;
	public ContainerInstance(ContainerInstance containerInstance) {
		super(containerInstance);
		this.init();
		for (IngredientInstance ingredientInstance : containerInstance.contents)
		{
			this.add(ingredientInstance);
		}
		int c = 1;
	}

	public ContainerInstance(ContainerClass containerClass, String name) {
		super(containerClass, name);
		this.init();
	}

	private void init()
	{
		this.contents = new ArrayList<IngredientInstance>();
		this.setAttributes();
	}
	protected void setAttributes()
	{
		this.setValue(ContainerClass.ATTRECEIVING, 0);
		this.setValue(ContainerClass.ATTHEATING, 0);
		this.setValue(ContainerClass.ATTMIXING, 0);
	}
	
	public abstract ContainerInstance copy();

	public void add(IngredientInstance ingredient)
	{
		this.contents.add(ingredient);
		this.addRelationalTarget(ContainerClass.ATTCONTAINS, ingredient.getName());
	}
	
	public void add(List<IngredientInstance> ingredientInstances)
	{
		for (IngredientInstance ingredientInstance : ingredientInstances)
		{
			this.contents.add(ingredientInstance);
		}
	}
	
	public void remove(IngredientInstance ingredient)
	{
		this.contents.remove(ingredient);	
	}
	
	public void pour(ContainerInstance receivingContainer)
	{
		receivingContainer.add(this.contents);
		this.contents.clear();
		this.clearRelationalTargets(ContainerClass.ATTCONTAINS);
	}
	
	public void mix()
	{
		Random rando = new Random();
		String instanceName = Integer.toString(rando.nextInt());
		Domain domain = this.obClass.domain;
		
		int numProducedIngredients = 0;
		ProducedIngredient batter = null;
		for (IngredientInstance ingredientInstance : this.contents)
		{
			if (ingredientInstance instanceof ProducedIngredient)
			{
				batter = (ProducedIngredient) ingredientInstance;
				numProducedIngredients++;
			}
		}
		
		if (batter != null && numProducedIngredients == 1)
		{
			for (IngredientInstance ingredientInstance : this.contents)
			{
				if (batter != ingredientInstance)
				{
					batter.simpleContents.add((SimpleIngredientInstance)ingredientInstance);
				}
			}
			this.contents.clear();
			this.clearRelationalTargets(ContainerClass.ATTCONTAINS);
			this.add(batter);
		}
		else
		{
			IngredientClass ingredientClass = 
					(IngredientClass)domain.getObjectClass(IngredientClass.className);
			
			IngredientInstance mixedIngredient = 
					new ProducedIngredient(ingredientClass, instanceName, this.contents);
			
			this.contents.clear();
			this.clearRelationalTargets(ContainerClass.ATTCONTAINS);
			this.add(mixedIngredient);
		}
	}
}
