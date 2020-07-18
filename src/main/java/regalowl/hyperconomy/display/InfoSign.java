package regalowl.hyperconomy.display;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;







import regalowl.simpledatalib.CommonFunctions;
import regalowl.hyperconomy.HyperConomy;
import regalowl.hyperconomy.HyperEconomy;
import regalowl.hyperconomy.minecraft.HBlock;
import regalowl.hyperconomy.minecraft.HLocation;
import regalowl.hyperconomy.minecraft.HSign;
import regalowl.hyperconomy.tradeobject.BasicTradeObject;
import regalowl.hyperconomy.tradeobject.EnchantmentClass;
import regalowl.hyperconomy.tradeobject.TradeObject;
import regalowl.hyperconomy.tradeobject.TradeObjectType;
import regalowl.hyperconomy.util.LanguageFile;


public abstract class InfoSign {
	HyperConomy hc;
	HLocation loc;
	String economy;
	String type;
	String[] parameters;
	String[] lines = {"","","",""};
	boolean valid;

	public InfoSign(HyperConomy hc, HLocation loc, String economy, String type, String[] parameters) {
		this.hc = hc;
		this.loc = loc;
		this.economy = economy;
		this.type = type;
		this.parameters = parameters;
	}

	public void update() {}

	public HLocation getLocation() {
		return loc;
	}

	public String getType() {
		return type;
	}

	public String getEconomy() {
		return economy;
	}

	public String getParameter(int index) {
		return parameters[index];
	}

	public boolean isValid() {
		return valid;
	}

	public void disableSign() {
		HSign sign = getSign();
		if(sign != null) {
			sign.setLine(0, "");
			sign.setLine(1, "");
			sign.setLine(2, "");
			sign.setLine(3, "");
			sign.update();
		}
	}

	public void updateHSign() {
		HSign sign = getSign();
		if(lines[0] != "")
			sign.setLine(0, lines[0]);
		if(lines[1] != "")
			sign.setLine(1, lines[1]);
		if(lines[2] != "")
			sign.setLine(2, lines[2]);
		if(lines[3] != "")
			sign.setLine(3, lines[3]);
        sign.update();
	}
	
	protected HSign getSign() {
		if (loc == null) return null;
		HBlock sb = new HBlock(hc, loc);
		if (!sb.isLoaded()) sb.load();
		return hc.getMC().getSign(loc);
	}
}