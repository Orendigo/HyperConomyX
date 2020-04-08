package regalowl.hyperconomy.display;

import java.util.ArrayList;
import java.util.Arrays;

import regalowl.hyperconomy.HyperConomy;
import regalowl.hyperconomy.minecraft.HLocation;
import regalowl.hyperconomy.minecraft.HSign;
import regalowl.hyperconomy.tradeobject.BasicTradeObject;
import regalowl.hyperconomy.tradeobject.EnchantmentClass;
import regalowl.hyperconomy.tradeobject.TradeObject;
import regalowl.hyperconomy.tradeobject.TradeObjectType;
import regalowl.hyperconomy.util.LanguageFile;
import regalowl.simpledatalib.CommonFunctions;

public class TradeObjectInfoSign extends InfoSign {
    TradeObject to;
    LanguageFile L;
    EnchantmentClass enchantClass;
    int multiplier;
    
    public TradeObjectInfoSign(HyperConomy hc, HLocation loc, String economy, String type, String[] parameters) {
        super(hc,loc,economy,type,parameters);
        L = hc.getLanguageFile();
        to = hc.getDataManager().getEconomy(economy).getTradeObject(parameters[0]);
        try {
            multiplier = Integer.parseInt(parameters[2]);
        } catch(Exception e) {
            multiplier = 1;
        }
        if (EnchantmentClass.fromString(parameters[2]) != null) {
            enchantClass = EnchantmentClass.fromString(parameters[2]);
        } else {
            enchantClass = EnchantmentClass.DIAMOND;
        }
        HSign s = getSign();
        if(s == null || to == null) {
            valid = false;
            return;
        } else {
            valid = true;
        }
        lines[0] = hc.getMC().removeColor(s.getLine(0).trim());
		lines[1] = hc.getMC().removeColor(s.getLine(1).trim());
        if (lines[0].length() > 13) {
			lines[1] = "&1" + lines[0].substring(13, lines[0].length()) + lines[1];
			lines[0] = "&1" + lines[0].substring(0, 13);
		} else {
			lines[0] = "&1" + lines[0];
			lines[1] = "&1" + lines[1];
		}
    }

    public static TradeObjectInfoSign fromHSign(HyperConomy hc, HLocation loc, String economy, HSign sign) {
        String[] parameters = new String[3];
        parameters[0] = sign.getLine(0)+sign.getLine(1);
        parameters[2] = sign.getLine(3);
        return new TradeObjectInfoSign(hc, loc, economy, sign.getLine(2), parameters);
    }
    
    @Override
    public void update() {
        switch(type.toUpperCase()) {
            case "BUY":
                if (to.getType() == TradeObjectType.ENCHANTMENT) {
                    double cost = to.getBuyPrice(enchantClass);
                    cost = cost + to.getPurchaseTax(cost);
                    lines[2] = "&f" + "Buy:";
                    lines[3] = "&a" + L.formatMoney(cost);
                } else {
                    double pcost = to.getBuyPrice(1);
                    lines[2] = "&f" + "Buy:";
                    lines[3] = "&a" + L.formatMoney(CommonFunctions.twoDecimals((pcost + to.getPurchaseTax(pcost)) * multiplier));
                }
                break;
            case "SELL":
                if (to.getType() == TradeObjectType.ENCHANTMENT) {
                    double value = to.getSellPrice(enchantClass);
                    value = CommonFunctions.twoDecimals((value - to.getSalesTaxEstimate(value)) * multiplier);
                    lines[2] = "&f" + "Sell:";
                    lines[3] = "&a" + L.formatMoney(value);
                } else {
                    double value = to.getSellPrice(1);
                    value = CommonFunctions.twoDecimals((value - to.getSalesTaxEstimate(value)) * multiplier);
                    lines[2] = "&f" + "Sell:";
                    lines[3] = "&a" + L.formatMoney(value);
                }
                break;
            case "STOCK":
                lines[2] = "&f" + "Stock:";
                lines[3] = "&a" + "" + L.formatDouble(to.getStock());
                break;
            case "TOTALSTOCK":
                lines[2] = "&f" + "Total Stock:";
                lines[3] = "&a" + "" + L.formatDouble(to.getTotalStock());
                break;
            case "VALUE":
                lines[2] = "&f" + "Value:";
                lines[3] = "&a" + "" + to.getValue() * multiplier;
                break;
            case "STATUS":
                boolean staticstatus;
                staticstatus = to.isStatic();
                lines[2] = "&f" + "Status:";
                if (staticstatus) {
                    lines[3] = "&a" + "Static";
                } else {
                    boolean initialstatus;
                    initialstatus = to.useInitialPricing();
                    if (initialstatus) {
                        lines[3] = "&a" + "Initial";
                    } else {
                        lines[3] = "&a" + "Dynamic";
                    }
                }
                break;
            case "STATICPRICE":
                lines[2] = "&f" + "Static Price:";
                lines[3] = "&a" + "" + L.formatMoney(to.getStaticPrice() * multiplier);
                break;
            case "STARTPRICE":
                lines[2] = "&f" + "Start Price:";
                lines[3] = "&a" + "" + L.formatMoney(to.getStartPrice() * multiplier);
                break;
            case "MEDIAN":
                lines[2] = "&f" + "Median:";
                lines[3] = "&a" + "" + L.formatDouble(to.getMedian());
                break;
            case "PRODUCTTAX":
                if (to.getType() == TradeObjectType.ENCHANTMENT) {
                    double price = to.getBuyPrice(enchantClass);
                    double taxpaid = CommonFunctions.twoDecimals(to.getPurchaseTax(price) * multiplier);
                    lines[2] = "&f" + "Tax:";
                    lines[3] = "&a" + "" + L.formatMoney(taxpaid);
                } else if (to.getType() == TradeObjectType.ITEM) {
                    lines[2] = "&f" + "Tax:";
                    lines[3] = "&a" + L.formatMoney(CommonFunctions.twoDecimals(to.getPurchaseTax(to.getBuyPrice(1) * multiplier)));
                } else {
                    BasicTradeObject bo = (BasicTradeObject)to;
                    lines[2] = "&f" + "Tax:";
                    lines[3] = "&a" + L.formatMoney(CommonFunctions.twoDecimals(bo.getPurchaseTax(bo.getBuyPrice(1) * multiplier)));
                }
                break;
            case "SB":
                if (to.getType() == TradeObjectType.ENCHANTMENT) {
                    double cost = to.getBuyPrice(enchantClass);
                    cost = CommonFunctions.twoDecimals((cost + to.getPurchaseTax(cost)) * multiplier);
                    lines[3] = "&f" + "B:" + "&a" + L.formatMoney(cost);
                    double value = to.getSellPrice(enchantClass);
                    value = CommonFunctions.twoDecimals((value - to.getSalesTaxEstimate(value)) * multiplier);
                    lines[2] = "&f" + "S:" + "&a" + L.formatMoney(value);
                }                                                                                                                                            else {
                    double pcost = to.getBuyPrice(1);
                    lines[3] = "&f" + "B:" + "&a" + L.formatMoney(CommonFunctions.twoDecimals((pcost + to.getPurchaseTax(pcost)) * multiplier));
                    double value = to.getSellPrice(1);
                    value = CommonFunctions.twoDecimals((value - to.getSalesTaxEstimate(value)) * multiplier);
                    lines[2] = "&f" + "S:" + "&a" + L.formatMoney(value);
                }
                break;  
        }
        updateHSign();
    }

    public TradeObject getTradeObject() {
        return to;
    }
}