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
    private TradeObject to;
    private LanguageFile L;
    private EnchantmentClass enchantClass;
    private int multiplier;

    private int timeValueHours;
	private int timeValue;
	private String timeIncrement;
    
    public TradeObjectInfoSign(HyperConomy hc, HLocation loc, String economy, String type, String[] parameters) {
        super(hc,loc,economy,type,parameters);
        L = hc.getLanguageFile();
        to = hc.getDataManager().getEconomy(economy).getTradeObject(parameters[0]+parameters[1]);
        HSign s = getSign();
        if(s == null || to == null) {
            valid = false;
            return;
        } else {
            valid = true;
        }
        if(type.equals("CHANGE")) {
            try {
                timeIncrement = parameters[1].substring(parameters[1].length()-1).toUpperCase();
                timeValue = Integer.parseInt(parameters[1].substring(0,parameters[1].length()-1));
                timeValueHours = timeValue;
                if (timeIncrement.equals("H")) {
                    timeValueHours *= 1;
                } else if (timeIncrement.equals("D")) {
                    timeValueHours *= 24;
                } else if (timeIncrement.equals("W")) {
                    timeValueHours *= 168;
                } else if (timeIncrement.equals("M")) {
                    timeValueHours *= 672;
                } else {
                    timeIncrement = "D";
                    timeValue = 1;
                    timeValueHours = 24;
                }
            } catch (Exception e) {
                timeIncrement = "D";
                timeValue = 1;
                timeValueHours = 24;
            }
            parameters[1] = timeValue+timeIncrement;
        } else if(to.getType() == TradeObjectType.ENCHANTMENT) {
            enchantClass = EnchantmentClass.fromString(parameters[2]);
            if(enchantClass == EnchantmentClass.NONE) {
                if(hc.getConf().getBoolean("shop.enchant-books-only"))
                    enchantClass = EnchantmentClass.BOOK;
                else
                    enchantClass = EnchantmentClass.DIAMOND;
            }
            parameters[2] = enchantClass.name();
        } else {
            try {
                multiplier = Integer.parseInt(parameters[2]);
            } catch(Exception e) {
                multiplier = 1;
            }
            parameters[2] = multiplier+"";
        }
        parameters[0] = to.getName();
        if(to.getType() == TradeObjectType.ENCHANTMENT)
            parameters[2] = enchantClass.name();
        else
            parameters[2] = multiplier+"";
        lines[0] = hc.getMC().removeColor(s.getLine(1).trim());
		lines[1] = hc.getMC().removeColor(s.getLine(2).trim());
        if (lines[0].length() > 13) {
			lines[1] = "&1" + lines[0].substring(13, lines[0].length()) + lines[1];
			lines[0] = "&1" + lines[0].substring(0, 13);
		} else {
			lines[0] = "&1" + lines[0];
			lines[1] = "&1" + lines[1];
		}
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
                updateHSign();
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
                updateHSign();
                break;
            case "STOCK":
                lines[2] = "&f" + "Stock:";
                lines[3] = "&a" + "" + L.formatDouble(to.getStock());
                updateHSign();
                break;
            case "TOTALSTOCK":
                lines[2] = "&f" + "Total Stock:";
                lines[3] = "&a" + "" + L.formatDouble(to.getTotalStock());
                updateHSign();
                break;
            case "VALUE":
                lines[2] = "&f" + "Value:";
                lines[3] = "&a" + "" + to.getValue() * multiplier;
                updateHSign();
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
                updateHSign();
                break;
            case "STATICPRICE":
                lines[2] = "&f" + "Static Price:";
                lines[3] = "&a" + "" + L.formatMoney(to.getStaticPrice() * multiplier);
                updateHSign();
                break;
            case "STARTPRICE":
                lines[2] = "&f" + "Start Price:";
                lines[3] = "&a" + "" + L.formatMoney(to.getStartPrice() * multiplier);
                updateHSign();
                break;
            case "MEDIAN":
                lines[2] = "&f" + "Median:";
                lines[3] = "&a" + "" + L.formatDouble(to.getMedian());
                updateHSign();
                break;
            case "PRODUCTTAX":
                if (to.getType() == TradeObjectType.ENCHANTMENT) {
                    double price = to.getBuyPrice(enchantClass);
                    double taxpaid = CommonFunctions.twoDecimals(to.getPurchaseTax(price) * multiplier);
                    lines[2] = "&f" + "Tax:";
                    lines[3] = "&a" + L.formatMoney(taxpaid);
                } else if (to.getType() == TradeObjectType.ITEM) {
                    lines[2] = "&f" + "Tax:";
                    lines[3] = "&a" + L.formatMoney(CommonFunctions.twoDecimals(to.getPurchaseTax(to.getBuyPrice(1) * multiplier)));
                } else {
                    BasicTradeObject bo = (BasicTradeObject)to;
                    lines[2] = "&f" + "Tax:";
                    lines[3] = "&a" + L.formatMoney(CommonFunctions.twoDecimals(bo.getPurchaseTax(bo.getBuyPrice(1) * multiplier)));
                }
                updateHSign();
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
                updateHSign();
                break;
            case "CHANGE":
                if (timeIncrement.equals("H")) {
                    timeValueHours *= 1;
                } else if (timeIncrement.equals("D")) {
                    timeValueHours *= 24;
                } else if (timeIncrement.equals("W")) {
                    timeValueHours *= 168;
                } else if (timeIncrement.equals("M")) {
                    timeValueHours *= 672;
                }
                updateHistorySign();
                break;
        }
    }

    private void updateHistorySign() {
		try {
			new Thread(new Runnable() {
				public void run() {
					String percentchange = hc.getHistory().getPercentChange(to, timeValueHours);
                    lines[2] = "&f" + "Change:";
                    if(percentchange.equalsIgnoreCase("?")) {
                        lines[3] = "&f" + "" + timeValue + timeIncrement.toLowerCase() + "&1(" + percentchange + "%)";
                    } else {
                        Double percentc = Double.parseDouble(percentchange);
                        if(percentc < 0.0)
                            lines[3] = "&f" + timeValue + timeIncrement.toLowerCase() + "&c(" + percentchange + "%)";
                        else if (percentc > 0)
                            lines[3] = "&f" + timeValue + timeIncrement.toLowerCase() + "&a(+" + percentchange + "%)";
                        else
                            lines[3] = "&f" + timeValue + timeIncrement.toLowerCase() + "&1(" + percentchange + "%)";
                    }
					if (lines[2].length() > 14) {
						lines[2] = lines[2].substring(0, 13) + ")";
					}
					hc.getMC().runTask(new Runnable() {
						public void run() {
							HSign s = getSign();
							if (s != null) {
								s.setLine(0, lines[0]);
								s.setLine(1, lines[1]);
								s.setLine(2, lines[2]);
								s.setLine(3, lines[3]);
								s.update();
							}
						}
					});
				}
			}).start();
		} catch (Exception e) {
			hc.gSDL().getErrorWriter().writeError(e);
		}
	}

    public TradeObject getTradeObject() {
        return to;
    }
}