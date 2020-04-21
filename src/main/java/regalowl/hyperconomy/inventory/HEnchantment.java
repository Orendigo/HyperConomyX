package regalowl.hyperconomy.inventory;


import java.util.HashMap;

import regalowl.simpledatalib.CommonFunctions;

 

public class HEnchantment {
	private String namespace;
	private String key;
    private int lvl;
 
	public HEnchantment(String namespace, String key, int lvl) {
		this.namespace = namespace;
		this.key = key;
        this.lvl = lvl;
    }
	
	public HEnchantment(HEnchantment he) {
        this.namespace = he.namespace;
		this.key = he.key;
        this.lvl = he.lvl;
    }
	
	public String serialize() {
		HashMap<String,String> data = new HashMap<String,String>();
		data.put("namespace", namespace);
		data.put("key", key);
		data.put("lvl", lvl+"");
		return CommonFunctions.implodeMap(data);
	}
	
	public HEnchantment(String serialized) {
		HashMap<String,String> data = CommonFunctions.explodeMap(serialized);
		this.namespace = data.get("namespace");
		this.key = data.get("key");
		this.lvl = Integer.parseInt(data.get("lvl"));
    }


	public String getEnchantment() {
		return namespace;
	}

	public String getKey() {
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
		HEnchantment other = (HEnchantment) obj;
		if (namespace == null) {
			if (other.namespace != null)
				return false;
		} else if (!namespace.equals(other.key))
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