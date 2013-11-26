import java.util.ArrayList;
import java.util.List;


public class IngredientRecipe {
	
	private Boolean mixed;
	private Boolean melted;
	private Boolean baked;
	private String name;
	private List<IngredientRecipe> contents;
	
	public IngredientRecipe (String name, Boolean mixed, Boolean melted, Boolean baked) {
		this.name = name;
		this.mixed = mixed;
		this.melted = melted;
		this.baked = baked;
	}
	
	public IngredientRecipe (String name, Boolean mixed, Boolean melted, Boolean baked, List<IngredientRecipe> contents) {
		this.name = name;
		this.mixed = mixed;
		this.melted = melted;
		this.baked = baked;
		this.contents = contents;
	}
	
	public Boolean isSimple() {
		return this.contents == null;
	}
	
	public Boolean getMixed () {
		return this.mixed;
	}
	
	public Boolean getMelted () {
		return this.melted;
	}
	
	public Boolean getBaked () {
		return this.baked;
	}
	
	public String getName() {
		return this.name;
	}
	
	public List<IngredientRecipe> getContents() {
		return new ArrayList<IngredientRecipe>(this.contents);
	}
	
	public int getConstituentIngredientsCount() {
		int count = 0;
		if (this.contents != null ) {
			for (IngredientRecipe subIngredient : this.contents) {
				count += subIngredient.getConstituentIngredientsCount();
			}
		}
		else {
			count++;
		}
		return count;
	}
}
