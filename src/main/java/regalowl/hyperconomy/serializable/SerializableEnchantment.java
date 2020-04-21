package regalowl.hyperconomy.serializable;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;

import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;


public class SerializableEnchantment extends SerializableObject implements Serializable {
	private static final long serialVersionUID = 4510326523024526205L;
	private String namespace;
	private String key;
    private int lvl;
 
	public SerializableEnchantment(Enchantment e, int lvl) {
		this.namespace = e.getKey().getNamespace();
		this.key = e.getKey().getKey();
        this.lvl = lvl;
    }

	public SerializableEnchantment(String base64String) {
    	try {
			byte[] data = Base64Coder.decode(base64String);
			ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(data));
			Object o = ois.readObject();
			ois.close();
			if (!(o instanceof SerializableEnchantment)) {return;}
			SerializableEnchantment se = (SerializableEnchantment)o;
			this.namespace = se.namespace;
			this.key = se.key;
	        this.lvl = se.lvl;
    	} catch (Exception e) {
    		
    	}
    }

	public Enchantment getEnchantment() {
		return Enchantment.getByKey(NamespacedKey.minecraft(key));
    }

	public String getEnchantmentNamespace() {
		return namespace;
	}

	public String getEnchantmentKey() {
		return key;
	}

	public int getLvl() {
		return lvl;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((namespace == null) ? 0 : namespace.hashCode());
		result = prime * result + ((key == null) ? 0 : key.hashCode());
		result = prime * result + lvl;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		SerializableEnchantment other = (SerializableEnchantment) obj;
		if (namespace == null) {
			if (other.namespace != null)
				return false;
		} else if (!namespace.equals(other.namespace))
			return false;
		if (key == null) {
			if (other.key != null)
				return false;
		} else if (!key.equals(other.key))
			return false;
		if (lvl != other.lvl)
			return false;
		return true;
	}

}