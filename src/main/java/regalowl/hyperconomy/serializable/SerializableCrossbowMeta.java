package regalowl.hyperconomy.serializable;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.CrossbowMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class SerializableCrossbowMeta extends SerializableItemMeta {

    private static final long serialVersionUID = 3422987555051283264L;

    private List<SerializableItemStack> chargedProjectiles = new ArrayList<SerializableItemStack>();

    public SerializableCrossbowMeta(ItemMeta im) {
		super(im);
		if (im instanceof CrossbowMeta) {
            CrossbowMeta cm = (CrossbowMeta)im;
			for(ItemStack is:cm.getChargedProjectiles()) {
				this.chargedProjectiles.add(new SerializableItemStack(is));
			}
		}
    }

    public SerializableCrossbowMeta(String base64String) {
		super(base64String);
    	try {
			byte[] data = Base64Coder.decode(base64String);
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
			Object o = ois.readObject();
			ois.close();
			if (!(o instanceof SerializableCrossbowMeta)) {return;}
			SerializableCrossbowMeta cm = (SerializableCrossbowMeta)o;
			this.chargedProjectiles = cm.getChargedProjectiles();
    	} catch (Exception e) {
    		
    	}
    }

    @Override
	public ItemMeta getItemMeta() {
		ItemStack s = new ItemStack(Material.CROSSBOW);
		CrossbowMeta cm = (CrossbowMeta)s.getItemMeta();
		cm.setDisplayName(displayName);
		cm.setLore(lore);
		for (SerializableItemStack im:chargedProjectiles) {
            cm.addChargedProjectile(new ItemStack(im.getMaterialEnum()));
        }
        return cm;
	}

    public List<SerializableItemStack> getChargedProjectiles() {
        return chargedProjectiles;
    }
    
}