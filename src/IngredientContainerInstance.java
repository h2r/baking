
public class IngredientContainerInstance extends ContainerInstance {

	public IngredientContainerInstance(IngredientContainerInstance containerInstance) {
		super(containerInstance);

	}

	public IngredientContainerInstance(ContainerClass containerClass,
			String name) {
		super(containerClass, name);
		
	}
	
	@Override
	protected void setAttributes()
	{
		this.setValue(ContainerClass.ATTRECEIVING, 0);
		this.setValue(ContainerClass.ATTHEATING, 0);
		this.setValue(ContainerClass.ATTMIXING, 0);
	}

	@Override
	public IngredientContainerInstance copy() {
		return new IngredientContainerInstance(this);
	}

}
