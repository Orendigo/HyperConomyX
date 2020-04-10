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
	private String increment;
    
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
                    lines[3] = "&a" + "" + L.formatMoney(taxpaid);
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
            case "HISTORY":
                String timeIncrement = parameters[2];
                if (timeIncrement.contains("(")) timeIncrement = timeIncrement.substring(0, timeIncrement.indexOf("("));
					timeIncrement = timeIncrement.toUpperCase().replaceAll("[^A-Z]", "");
                String timeValueString = parameters[2];
                if (timeValueString.contains("(")) timeValueString = timeValueString.substring(0, timeValueString.indexOf("("));
					timeValueString = timeValueString.toUpperCase().replaceAll("[^0-9]", "");
                int timeValue = Integer.parseInt(timeValueString);
                int timeValueHours = timeValue;
                if (timeIncrement.equals("H")) {
                    timeValueHours *= 1;
                } else if (timeIncrement.equals("D")) {
                    timeValueHours *= 24;
                } else if (timeIncrement.equals("W")) {
                    timeValueHours *= 168;
                } else if (timeIncrement.equals("M")) {
                    timeValueHours *= 672;
                }
                updateHistorySign(timeValueHours, timeValue, timeIncrement);
                break;
        }
    }

    private void updateHistorySign(int timevalueHours, int timevalue, String inc) {
		try {
			this.timeValueHours = timevalueHours;
			this.timeValue = timevalue;
			this.increment = inc;
			new Thread(new Runnable() {
				public void run() {
					String percentchange = hc.getHistory().getPercentChange(to, timeValueHours);
					String colorcode = getcolorCode(percentchange);
					lines[2] = "&f" + "History:";
					lines[3] = "&f" + "" + timeValue + increment.toLowerCase() + colorcode + "(" + percentchange + ")";
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
	

	private String getcolorCode(String percentchange) {
		String colorcode = "&1";
		if (percentchange.equalsIgnoreCase("?")) {
			colorcode = "&1";
		} else {
			Double percentc = Double.parseDouble(percentchange);
			if (percentc > 0) {
				colorcode = "&a";
			} else if (percentc < 0) {
				colorcode = "&4";
			}
		}
		return colorcode;
	}

    public TradeObject getTradeObject() {
        return to;
    }
}