
public abstract class IngredientRecipe {
	
	private Boolean mixed;
	private Boolean melted;
	private Boolean baked;
	private String name;
	
	public IngredientRecipe (String name, Boolean mixed, Boolean melted, Boolean baked) {
		this.name = name;
		this.mixed = mixed;
		this.melted = melted;
		this.baked = baked;
	}
	
	public Boolean getMixed () {
		return this.mixed;
	}
	
	public Boolean getMelted () {
		return this.melted;
	}
	
	public Boolean baked () {
		return this.baked;
	}
	
	public String getName() {
		return this.name;
	}
}
