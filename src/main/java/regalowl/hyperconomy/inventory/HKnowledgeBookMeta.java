package regalowl.hyperconomy.inventory;

import java.util.ArrayList;
import java.util.HashMap;

public class HKnowledgeBookMeta extends HItemMeta {
    private HashMap<String,String> recipes = new HashMap<String,String>();

    public HKnowledgeBookMeta(String displayName, ArrayList<String> lore, ArrayList<HEnchantment> enchantments, ArrayList<HItemFlag> itemFlags, boolean unbreakable, int repairCost, HashMap<String,String> recipes) {
        super(displayName, lore, enchantments, itemFlags, unbreakable, repairCost);
        this.recipes = recipes;
    }

    public HKnowledgeBookMeta(HKnowledgeBookMeta meta) {
        super(meta);
        recipes = meta.recipes;
    }

    @Override
    public HItemMetaType getType() {
        return HItemMetaType.KNOWLEDGEBOOK;
    }

    public HashMap<String,String> getRecipes() {
        return recipes;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((recipes == null) ? 0 : recipes.hashCode());
		return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (!super.equals(obj))
            return false;
        if (getClass() != obj.getClass())
            return false;
            HKnowledgeBookMeta other = (HKnowledgeBookMeta) obj;
        if (recipes != other.recipes)
            return false;
        return true;
    }
}