package regalowl.hyperconomy.inventory;

import java.util.ArrayList;
import java.util.HashMap;

import regalowl.simpledatalib.CommonFunctions;

public class HSuspiciousStewMeta extends HItemMeta {
    private ArrayList<HPotionEffect> potionEffects = new ArrayList<HPotionEffect>();

    public HSuspiciousStewMeta(String displayName, ArrayList<String> lore, ArrayList<HEnchantment> enchantments, ArrayList<HItemFlag> itemFlags, boolean unbreakable, int repairCost, ArrayList<HPotionEffect> potionEffects) {
        super(displayName, lore, enchantments, itemFlags, unbreakable, repairCost);
        this.potionEffects = potionEffects;
    }

    public HSuspiciousStewMeta(String serialized) {
        super(serialized);
        HashMap<String,String> data = CommonFunctions.explodeMap(serialized);
        ArrayList<String> sPotionEffects = CommonFunctions.explode(data.get("potionEffects"));
		for (String pe:sPotionEffects) {
			potionEffects.add(new HPotionEffect(pe));
		}
    }

    public HSuspiciousStewMeta(HSuspiciousStewMeta meta) {
        super(meta);
        potionEffects = meta.potionEffects;
    }

    @Override
    public HItemMetaType getType() {
        return HItemMetaType.SUSPICIOUS;
    }

    public ArrayList<HPotionEffect> getPotionEffects() {
        return potionEffects;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((potionEffects == null) ? 0 : potionEffects.hashCode());
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
        HSuspiciousStewMeta other = (HSuspiciousStewMeta) obj;
		if (potionEffects != other.potionEffects)
			return false;
		return true;
	}

}