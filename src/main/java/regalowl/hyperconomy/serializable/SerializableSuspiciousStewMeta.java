package regalowl.hyperconomy.serializable;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SuspiciousStewMeta;
import org.bukkit.potion.PotionEffect;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class SerializableSuspiciousStewMeta extends SerializableItemMeta {
    
    private List<SerializablePotionEffect> potionEffects = new ArrayList<SerializablePotionEffect>();

    public SerializableSuspiciousStewMeta(ItemMeta im) {
		super(im);
		if (im instanceof SuspiciousStewMeta) {
            SuspiciousStewMeta ssm = (SuspiciousStewMeta)im;
			for(PotionEffect pe:ssm.getCustomEffects()) {
				this.potionEffects.add(new SerializablePotionEffect(pe));
			}
		}
    }

    public SerializableSuspiciousStewMeta(String base64String) {
		super(base64String);
    	try {
			byte[] data = Base64Coder.decode(base64String);
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
			Object o = ois.readObject();
			ois.close();
			if (!(o instanceof SerializableSuspiciousStewMeta)) {return;}
			SerializableSuspiciousStewMeta ss = (SerializableSuspiciousStewMeta)o;
			this.potionEffects = ss.getPotionEffects();
    	} catch (Exception e) {
    		
    	}
    }

    @Override
	public ItemMeta getItemMeta() {
		ItemStack s = new ItemStack(Material.SUSPICIOUS_STEW);
		SuspiciousStewMeta ssm = (SuspiciousStewMeta)s.getItemMeta();
		ssm.setDisplayName(displayName);
		ssm.setLore(lore);
		for (SerializablePotionEffect im:potionEffects) {
            ssm.addCustomEffect(im.getPotionEffect(), true);
        }
        return ssm;
	}

    public List<SerializablePotionEffect> getPotionEffects() {
        return potionEffects;
    }
}