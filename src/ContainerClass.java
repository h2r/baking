import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;


public class ContainerClass extends ObjectClass {

	public static final String ATTMIXING = "mixing";
	public static final String ATTHEATING = "heating";
	public static final String ATTRECEIVING = "receiving";
	public static final String ATTCONTAINS = "contains";
	public static final String className = "container";
	public ContainerClass(Domain domain) {
		super(domain, ContainerClass.className);
		this.addAttributes();
	}

	public ContainerClass(Domain domain, boolean hidden) {
		super(domain, ContainerClass.className, hidden);
		this.addAttributes();
	}
	
	protected void addAttributes()
	{
		Attribute mixingAttribute = 
				new Attribute(this.domain, ContainerClass.ATTMIXING, Attribute.AttributeType.DISC);
		mixingAttribute.setDiscValuesForRange(0,1,1);
		this.addAttribute(mixingAttribute);
		
		Attribute heatingAttribute = 
				new Attribute(this.domain, ContainerClass.ATTHEATING, Attribute.AttributeType.DISC);
		heatingAttribute.setDiscValuesForRange(0,1,1);
		this.addAttribute(heatingAttribute);
		
		Attribute receivingAttribute =
				new Attribute(this.domain, ContainerClass.ATTRECEIVING, Attribute.AttributeType.DISC);
		receivingAttribute.setDiscValuesForRange(0,1,1);
		this.addAttribute(receivingAttribute);
		
		this.addAttribute(
				new Attribute(this.domain, ContainerClass.ATTCONTAINS, 
						Attribute.AttributeType.MULTITARGETRELATIONAL));
	}
	
	
}
