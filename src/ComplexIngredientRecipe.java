import java.util.Collection;
import java.util.Set;
import java.util.TreeSet;


public class ComplexIngredientRecipe extends IngredientRecipe {

	private Set<String> contents;
	public ComplexIngredientRecipe (String name, Boolean mixed, Boolean melted, Boolean baked, Collection<String> contents) {
		super(name, mixed, melted, baked);
		this.contents = new TreeSet<String>(contents);
	}
	
	public Set<String> getContents() {
		return this.contents;
	}
}
