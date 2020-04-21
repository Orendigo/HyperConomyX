package regalowl.hyperconomy.inventory;

import java.util.ArrayList;
import java.util.HashMap;

import regalowl.simpledatalib.CommonFunctions;

public class HCrossbowMeta extends HItemMeta {

    private ArrayList<HItemStack> chargedProjectiles = new ArrayList<HItemStack>();

    public HCrossbowMeta(String displayName, ArrayList<String> lore, ArrayList<HEnchantment> enchantments, ArrayList<HItemFlag> itemFlags, boolean unbreakable, int repairCost, ArrayList<HItemStack> chargedProjectiles) {
        super(displayName, lore, enchantments, itemFlags, unbreakable, repairCost);
        this.chargedProjectiles = chargedProjectiles;
    }

    public HCrossbowMeta(String serialized) {
        super(serialized);
        HashMap<String,String> data = CommonFunctions.explodeMap(serialized);
        ArrayList<String> sChargedProjectiles = CommonFunctions.explode(data.get("chargedProjectiles"));
		for (String c:sChargedProjectiles) {
			chargedProjectiles.add(new HItemStack(c));
		}
    }

    public HCrossbowMeta(HCrossbowMeta meta) {
        super(meta);
        chargedProjectiles = meta.chargedProjectiles;
    }

    @Override
    public HItemMetaType getType() {
        return HItemMetaType.CROSSBOW;
    }

    public ArrayList<HItemStack> getChargedProjectiles() {
        return chargedProjectiles;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((enchantments == null) ? 0 : enchantments.hashCode());
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
		HCrossbowMeta other = (HCrossbowMeta) obj;
		if (chargedProjectiles != other.chargedProjectiles)
			return false;
		return true;
	}

}