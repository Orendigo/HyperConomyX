package regalowl.hyperconomy.inventory;

import java.util.ArrayList;
import java.util.HashMap;

import regalowl.hyperconomy.account.HyperPlayer;
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
    public String serialize() {
        HashMap<String,String> data = super.getMap();
        ArrayList<String> cps = new ArrayList<String>();
		for(HItemStack is:chargedProjectiles) {
            cps.add(is.serialize());
        }
		return CommonFunctions.implodeMap(data); 
    }

    @Override
    public ArrayList<String> displayInfo(HyperPlayer p, String color1, String color2) {
        ArrayList<String> info = super.displayInfo(p, color1, color2);
		String chargedProjectilesString = "";
		if (p != null && chargedProjectiles.size() > 0) {
			for(HItemStack pe:chargedProjectiles) {
				chargedProjectilesString += "Material:"+pe.getMaterial();
			}
			chargedProjectilesString = chargedProjectilesString.substring(0, chargedProjectilesString.length() - 1);
		}
		info.add(color1 + "Charged Projectiles: " + color2 + chargedProjectilesString);
		return info;

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