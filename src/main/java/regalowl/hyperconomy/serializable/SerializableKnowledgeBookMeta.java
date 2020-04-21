package regalowl.hyperconomy.serializable;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.xml.stream.events.Namespace;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.KnowledgeBookMeta;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

public class SerializableKnowledgeBookMeta extends SerializableItemMeta {
    
    private static final long serialVersionUID = 4594188791475874853L;
    
    private Map<String, String> recipes = new HashMap<String, String>();

    public SerializableKnowledgeBookMeta(ItemMeta im) {
        super(im);
        if (im instanceof KnowledgeBookMeta) {
            KnowledgeBookMeta kbm = (KnowledgeBookMeta)im;
            for(NamespacedKey ns:kbm.getRecipes()) {
                this.recipes.put(ns.getNamespace(), ns.getKey());
            }
        }
    }

    public SerializableKnowledgeBookMeta(String base64String) {
		super(base64String);
    	try {
			byte[] data = Base64Coder.decode(base64String);
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
			Object o = ois.readObject();
			ois.close();
			if (!(o instanceof SerializableCrossbowMeta)) {return;}
			SerializableKnowledgeBookMeta kbm = (SerializableKnowledgeBookMeta)o;
            this.recipes = kbm.getRecipes();
    	} catch (Exception e) {
    		
    	}
    }

    @Override
	public ItemMeta getItemMeta() {
		ItemStack s = new ItemStack(Material.KNOWLEDGE_BOOK);
		KnowledgeBookMeta kbm = (KnowledgeBookMeta)s.getItemMeta();
		kbm.setDisplayName(displayName);
        kbm.setLore(lore);
        Set<String> namespaces = recipes.keySet();
		for (String namespace:namespaces) {
            kbm.addRecipe(NamespacedKey.minecraft(recipes.get(namespace)));
        }
        return kbm;
	}

    public Map<String,String> getRecipes() {
        return recipes;
    }

}