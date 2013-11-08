import burlap.oomdp.core.Attribute;
import burlap.oomdp.core.Domain;
import burlap.oomdp.core.ObjectClass;


public class IngredientClass extends ObjectClass {

	public static final String className = "ingredient";
	public static final String attBaked = "baked";
	public IngredientClass(Domain domain) {
		super(domain, IngredientClass.className);
		this.addAttributes();
	}	
	
	protected void addAttributes()
	{
		Attribute bakedAttribute = 
				new Attribute(this.domain, IngredientClass.attBaked, Attribute.AttributeType.DISC);
		bakedAttribute.setDiscValuesForRange(0,1,1);
		this.addAttribute(bakedAttribute);
	}
}
