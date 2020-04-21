package regalowl.hyperconomy.inventory;

import java.util.ArrayList;
import java.util.HashMap;

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