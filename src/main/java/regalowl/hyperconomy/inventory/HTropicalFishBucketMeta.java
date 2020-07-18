package regalowl.hyperconomy.inventory;

import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Color;
import org.bukkit.DyeColor;

import regalowl.hyperconomy.account.HyperPlayer;
import regalowl.simpledatalib.CommonFunctions;

public class HTropicalFishBucketMeta extends HItemMeta {
    private HColor bodyColor;
    private String pattern;
    private HColor patternColor;

    public HTropicalFishBucketMeta(String displayName, ArrayList<String> lore, ArrayList<HEnchantment> enchantments, ArrayList<HItemFlag> itemFlags, boolean unbreakable, int repairCost, HColor bodyColor, String pattern, HColor patternColor) {
        super(displayName, lore, enchantments, itemFlags, unbreakable, repairCost);
        this.bodyColor = bodyColor;
        this.pattern = pattern;
        this.patternColor = patternColor;
    }

    public HTropicalFishBucketMeta(String serialized) {
        super(serialized);
        HashMap<String,String> data = CommonFunctions.explodeMap(serialized);
        this.bodyColor = new HColor(data.get("bodyColor"));
        this.pattern = data.get("pattern");
        this.patternColor = new HColor(data.get("patternColor"));
    }

    public HTropicalFishBucketMeta(HTropicalFishBucketMeta meta) {
        super(meta);
        bodyColor = meta.bodyColor;
        pattern = meta.pattern;
        patternColor = meta.patternColor;
    }

    public String serialize() {
		HashMap<String,String> data = super.getMap();
        ArrayList<String> sEffects = new ArrayList<String>();
        data.put("bodyColor", bodyColor.serialize());
        data.put("pattern", pattern);
        data.put("patternColor", patternColor.serialize());
		return CommonFunctions.implodeMap(data);
    }
    
    @Override
    public ArrayList<String> displayInfo(HyperPlayer p, String color1, String color2) {
		ArrayList<String> info = super.displayInfo(p, color1, color2);
		info.add(color1 + "Body Color: " + color2 + DyeColor.getByColor(Color.fromRGB(bodyColor.getRed(), bodyColor.getGreen(), bodyColor.getBlue())));
        info.add(color1 + "Pattern: " + color2 + pattern);
        info.add(color1 + "Pattern Color: " + color2 + DyeColor.getByColor(Color.fromRGB(patternColor.getRed(), patternColor.getGreen(), patternColor.getBlue())));
		return info;
	}

    @Override
    public HItemMetaType getType() {
        return HItemMetaType.TROPICAL_FISH;
    }

    public HColor getBodyColor() {
        return bodyColor;
    }

    public String getPattern() {
        return pattern;
    }

    public HColor getPatternColor() {
        return patternColor;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
		int result = super.hashCode();
        result = prime * result + ((bodyColor == null) ? 0 : bodyColor.hashCode());
        result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
        result = prime * result + ((patternColor == null) ? 0 : patternColor.hashCode());
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
        HTropicalFishBucketMeta other = (HTropicalFishBucketMeta) obj;
		if (bodyColor != other.bodyColor)
            return false;
        if (pattern != other.pattern)
            return false;
        if (patternColor != other.patternColor)
			return false;
		return true;
	}
}