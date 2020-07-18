package regalowl.hyperconomy.display;

import regalowl.hyperconomy.HyperConomy;
import regalowl.hyperconomy.minecraft.HLocation;
import regalowl.hyperconomy.minecraft.HSign;
import regalowl.simpledatalib.file.FileConfiguration;

public class TaxInfoSign extends InfoSign {
    String tax;

    public TaxInfoSign(HyperConomy hc, HLocation loc, String economy, String type, String[] parameters) {
        super(hc,loc,economy,type,parameters);
        HSign s = getSign();
        if(s == null) {
            valid = false;
            return;
        } else {
            valid = true;
        }
        if(parameters[0].toUpperCase().matches("PURCHASE|SALES|ENCHANT|INITIAL|STATIC")) {
            tax = parameters[0].toUpperCase();
        } else {
            tax = "PURCHASE";
        }
        parameters[0] = tax;
        if(s.getLine(0).equalsIgnoreCase(type)) {
            lines[0] = "&l" + tax;
            lines[1] = "&lTAX";
            lines[2] = "&fPercent:";
        }
    }

    public void update() {
        lines[3] = "&a" + getPercent() + "%";
    }

    private double getPercent() {
        FileConfiguration conf = hc.getConf();
        switch(tax) {
            case "PURCHASE":
                return conf.getDouble("tax.purchase");
            case "SALES":
                return conf.getDouble("tax.sales");
            case "ENCHANT":
                return conf.getDouble("tax.enchant");
            case "INITIAL":
                return conf.getDouble("tax.initial");
            case "STATIC":
                return conf.getDouble("tax.static");
            default:
                return 0.0;
        }
    }

}