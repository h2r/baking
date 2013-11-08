import burlap.oomdp.core.ObjectClass;
import burlap.oomdp.core.ObjectInstance;

public class MixingContainerInstance extends ContainerInstance {

	public MixingContainerInstance(MixingContainerInstance mixingContainerInstance) {
		super(mixingContainerInstance);
		this.setAttributes();
	}

	public MixingContainerInstance(ContainerClass containerClass, String name) {
		super(containerClass, name);
		this.setAttributes();
	}
	
	@Override
	protected void setAttributes()
	{
		this.setValue(ContainerClass.ATTRECEIVING, 1);
		this.setValue(ContainerClass.ATTHEATING, 0);
		this.setValue(ContainerClass.ATTMIXING, 1);
	}

	@Override
	public MixingContainerInstance copy() {
		return new MixingContainerInstance(this);
	}
}
