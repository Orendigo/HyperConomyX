package regalowl.hyperconomy.inventory;

import java.util.ArrayList;
import java.util.HashMap;

import regalowl.hyperconomy.account.HyperPlayer;
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
    public String serialize() {
        HashMap<String,String> data = super.getMap();
		ArrayList<String> sEffects = new ArrayList<String>();
		for (HPotionEffect e:potionEffects) {
			sEffects.add(e.serialize());
		}
		data.put("potionEffects", CommonFunctions.implode(sEffects));
		return CommonFunctions.implodeMap(data);
    }

    @Override
	public ArrayList<String> displayInfo(HyperPlayer p, String color1, String color2) {
		ArrayList<String> info = super.displayInfo(p, color1, color2);
		String potionEffectString = "";
		if (potionEffects != null && potionEffects.size() > 0) {
			for(HPotionEffect pe:potionEffects) {
				potionEffectString += "Type:"+pe.getType()+","+"Amplifier:"+pe.getAmplifier()+
						","+"Duration:"+pe.getDuration()+","+"Ambient:"+pe.isAmbient()+";";
			}
			potionEffectString = potionEffectString.substring(0, potionEffectString.length() - 1);
		}
		info.add(color1 + "Potion Effects: " + color2 + potionEffectString);
		return info;
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