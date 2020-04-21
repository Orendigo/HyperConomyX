package regalowl.hyperconomy.serializable;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.entity.TropicalFish;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.TropicalFishBucketMeta;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class SerializableTropicalFishBucketMeta extends SerializableItemMeta {

    private static final long serialVersionUID = -4887181687506765909L;
    
    private SerializableColor bodyColor;
    private String pattern;
    private SerializableColor patternColor;

    public SerializableTropicalFishBucketMeta(ItemMeta im) {
		super(im);
		if (im instanceof TropicalFishBucketMeta) {
            TropicalFishBucketMeta tfb = (TropicalFishBucketMeta)im;
            this.bodyColor = new SerializableColor(tfb.getBodyColor().getColor());
            this.pattern = tfb.getPattern().name();
            this.patternColor = new SerializableColor(tfb.getPatternColor().getColor());
		}
    }

    public SerializableTropicalFishBucketMeta(String base64String) {
		super(base64String);
    	try {
			byte[] data = Base64Coder.decode(base64String);
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
			Object o = ois.readObject();
			ois.close();
			if (!(o instanceof SerializableCrossbowMeta)) {return;}
			SerializableTropicalFishBucketMeta tfb = (SerializableTropicalFishBucketMeta)o;
            this.bodyColor = new SerializableColor(tfb.getBodyColor().getColor());
            this.pattern = tfb.getPattern();
            this.patternColor = new SerializableColor(tfb.getPatternColor().getColor());
    	} catch (Exception e) {
    		
    	}
    }

    @Override
	public ItemMeta getItemMeta() {
		ItemStack s = new ItemStack(Material.TROPICAL_FISH_BUCKET);
		TropicalFishBucketMeta tfb = (TropicalFishBucketMeta)s.getItemMeta();
		tfb.setDisplayName(displayName);
        tfb.setLore(lore);
        tfb.setBodyColor(DyeColor.getByColor(bodyColor.getColor()));
        tfb.setPattern(TropicalFish.Pattern.valueOf(pattern));
        tfb.setPatternColor(DyeColor.getByColor(patternColor.getColor()));
        return tfb;
	}

    public SerializableColor getBodyColor() {
        return bodyColor;
    }

    public String getPattern() {
        return pattern;
    }

    public SerializableColor getPatternColor() {
        return patternColor;
    }
}