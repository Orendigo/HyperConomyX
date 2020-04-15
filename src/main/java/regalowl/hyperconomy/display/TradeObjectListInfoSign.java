package regalowl.hyperconomy.display;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import regalowl.hyperconomy.HyperConomy;
import regalowl.hyperconomy.minecraft.HLocation;
import regalowl.hyperconomy.minecraft.HSign;
import regalowl.hyperconomy.shop.PlayerShop;
import regalowl.hyperconomy.shop.Shop;
import regalowl.hyperconomy.tradeobject.BasicTradeObject;
import regalowl.hyperconomy.tradeobject.EnchantmentClass;
import regalowl.hyperconomy.tradeobject.TradeObject;
import regalowl.hyperconomy.tradeobject.TradeObjectType;
import regalowl.hyperconomy.util.LanguageFile;
import regalowl.simpledatalib.CommonFunctions;

public class TradeObjectListInfoSign extends InfoSign implements InteractiveInfoSign {
    ArrayList<TradeObject> displayObjects = new ArrayList<TradeObject>();
    HashMap<TradeObject, Double> historyDisplayObjects = new HashMap<TradeObject, Double>();
    boolean scrollable;
    int index;
    int multiplier;
    EnchantmentClass enchantClass = EnchantmentClass.DIAMOND;
    String timeIncrement;
    int timeValue;
    int timeValueHours;
    String filter;
    boolean categoryFilter;

    public TradeObjectListInfoSign(HyperConomy hc, HLocation loc, String economy, String type, String[] parameters) {
        super(hc,loc,economy,type,parameters);
        HSign s = getSign();
        if(s == null) {
            valid = false;
        } else {
            valid = true;
        }
        try {
            if(parameters[0].equals("") || parameters[0].equalsIgnoreCase("scroll")) {
                parameters[0] = "scroll";
                scrollable = true;
                index = 0;
            } else {
                scrollable = false;
                index = Integer.parseInt(parameters[0])-1;
                if(index < 0)
                    index = 0;
            }
        } catch(Exception e) {
            parameters[0] = "scroll";
            index = 0;
        }
        filter = parameters[1].toUpperCase();
        if(filter.equals("ALL"))
            filter = "ALL";
        else if(filter.equals("ITEMS"))
            filter = "ITEMS";
        else if(filter.equals("ENCHANTS"))
            filter = "ENCHANTS";
        else if(filter.startsWith("C:")) {
            if(hc.getDataManager().getCategories().contains(filter.substring(2).toLowerCase())) {
                categoryFilter = true;
                filter = filter.toLowerCase();
            }
        } else {
            categoryFilter = false;
            filter = "ALL";
        }
        parameters[1] = filter;
        if(type.equals("TOPOBJCHANGE")) {
            try {
                timeIncrement = parameters[2].substring(parameters[2].length()-1).toLowerCase();
                timeValue = Integer.parseInt(parameters[2].substring(0,parameters[2].length()-1));
                timeValueHours = timeValue;
                if (timeIncrement.equals("h")) {
                    timeValueHours *= 1;
                } else if (timeIncrement.equals("d")) {
                    timeValueHours *= 24;
                } else if (timeIncrement.equals("w")) {
                    timeValueHours *= 168;
                } else if (timeIncrement.equals("m")) {
                    timeValueHours *= 672;
                } else {
                    parameters[2] = "1d";
                    timeIncrement = "d";
                    timeValue = 1;
                    timeValueHours = 24;
                }
            } catch (Exception e) {
                parameters[2] = "1d";
                timeIncrement = "d";
                timeValue = 1;
                timeValueHours = 24;
            }
            parameters[2] = timeValue+timeIncrement.toLowerCase();
        } else if(type.toUpperCase().matches("TOPBUY|TOPSELL|TOPPRODUCTTAX")) {
            if(filter.equals("ENCHANTS")) {
                enchantClass = EnchantmentClass.fromString(parameters[2]);
                if(enchantClass == EnchantmentClass.NONE) {
                    if(hc.getConf().getBoolean("shop.enchant-books-only"))
                        enchantClass = EnchantmentClass.BOOK;
                    else
                        enchantClass = EnchantmentClass.DIAMOND;
                }
                parameters[2] = enchantClass.name();
            } else if(filter.equals("ITEMS")) {
                try {
                    multiplier = Integer.parseInt(parameters[2]);
                } catch(Exception e) {
                    multiplier = 1;
                }
                parameters[2] = multiplier+"";
            } else {
                if(hc.getConf().getBoolean("shop.enchant-books-only"))
                    enchantClass = EnchantmentClass.BOOK;
                else
                    enchantClass = EnchantmentClass.DIAMOND;
                multiplier = 1;
                parameters[2] = "";
            }
            
            parameters[1] = "ENCHANTS";
            parameters[2] = enchantClass.name();
        }
    }

	public void update() {
        if(type.equals("TOPOBJCHANGE")) {
            HashMap<TradeObject, Double> allObjects = hc.getHistory().getObjectPercentChange(economy, timeValueHours);
            historyDisplayObjects.clear();
            if(!filter.equals("ALL")) {
                List<TradeObject> keys = new ArrayList<TradeObject>(allObjects.keySet());
                for(TradeObject to : keys) {
                    if(categoryFilter) {
                        if(to.getCategories().contains(filter.substring(2).toLowerCase()))
                            historyDisplayObjects.put(to, allObjects.get(to));
                    } else if(to.getType() == TradeObjectType.ENCHANTMENT && filter.equals("ENCHANTS")) {
                        historyDisplayObjects.put(to, allObjects.get(to));
                    } else if(to.getType() == TradeObjectType.ITEM && filter.equals("ITEMS")) {
                        historyDisplayObjects.put(to, allObjects.get(to));
                    }
                }
            } else {
                historyDisplayObjects = allObjects;
            }
            historyDisplayObjects = sortByValue(historyDisplayObjects);
        } else {
            Shop s = hc.getDataManager().getHyperShopManager().getShop(loc);
            ArrayList<TradeObject> allObjects = hc.getDataManager().getEconomy(economy).getTradeObjects(s);
            displayObjects.clear();
            if(!filter.equals("ALL")) {
                for(TradeObject to : allObjects) {
                    if(categoryFilter) {
                        if(to.getCategories().contains(filter.substring(2).toLowerCase()))
                        displayObjects.add(to);
                    } else if(to.getType() == TradeObjectType.ENCHANTMENT && filter.equals("ENCHANTS")) {
                        displayObjects.add(to);
                    } else if(to.getType() == TradeObjectType.ITEM && filter.equals("ITEMS")) {
                        displayObjects.add(to);
                    }
                }
            } else {
                displayObjects = allObjects;
            }
            Collections.sort(displayObjects, new Comparator<TradeObject>(){
                public int compare(TradeObject to1, TradeObject to2) {
                    Double s1 = getStat(to1).value;
                    Double s2 = getStat(to2).value;
                    if (s1 < s2) return 1;
                    if (s1 > s2) return -1;
                    return 0;
                }
            });
            if(index >= displayObjects.size())
                index = displayObjects.size()-1; 
        }
        if(index >= historyDisplayObjects.size())
            index = historyDisplayObjects.size()-1;
        if(index < 0)
            index = 0;
        updateDisplay();
    }

    public void updateDisplay() {
        lines[0] = "&a#"+(index+1)+" &f"+getTitle()+":";
        String name;
        if(type.equals("TOPOBJCHANGE")) {
            if(historyDisplayObjects.isEmpty()) {
                lines[1] = " ";
                lines[2] = " ";
                lines[3] = "&cNo Changes";
                updateHSign();
                return;
            }
            List<TradeObject> keys = new ArrayList<TradeObject>(historyDisplayObjects.keySet());
            name = keys.get(index).getDisplayName();
            double percentChange = historyDisplayObjects.get(keys.get(index));
            if(percentChange < 0.0)
                lines[3] = "&f" + timeValue + timeIncrement + "&c(" + hc.getLanguageFile().formatDouble(percentChange) + "%)";
            else if (percentChange > 0)
                lines[3] = "&f" + timeValue + timeIncrement + "&a(+" + hc.getLanguageFile().formatDouble(percentChange) + "%)";
        } else {
            if(displayObjects.isEmpty()) {
                lines[1] = " ";
                lines[2] = " ";
                lines[3] = "&cNo Items";
                updateHSign();
                return;
            }
            name = displayObjects.get(index).getDisplayName();
            lines[3] = getStat(displayObjects.get(index)).display;
        }
        int spaceIndex = name.indexOf(" ");
        if(spaceIndex == -1)
            spaceIndex = name.indexOf("_");
        if(spaceIndex != -1 && spaceIndex < 13) {
            lines[1] = "&1"+name.substring(0, spaceIndex);
            lines[2] = "&1"+name.substring(spaceIndex+1);
        } else if(name.length() < 13) {
            lines[1] = "&1"+name;
            lines[2] = " ";
        } else {
            lines[1] = "&1"+name.substring(0,13);
            lines[2] = "&1"+name.substring(13);
        }
        updateHSign();
    }

    public static HashMap<TradeObject, Double> sortByValue(HashMap<TradeObject, Double> hm) {
        List<Map.Entry<TradeObject, Double> > list = new LinkedList<Map.Entry<TradeObject, Double> >(hm.entrySet()); 
        Collections.sort(list, new Comparator<Map.Entry<TradeObject, Double> >() { 
            public int compare(Map.Entry<TradeObject, Double> o1, Map.Entry<TradeObject, Double> o2) { 
                return ((Double)Math.abs(o2.getValue())).compareTo((Double)Math.abs(o1.getValue())); 
            } 
        }); 
        HashMap<TradeObject, Double> temp = new LinkedHashMap<TradeObject, Double>(); 
        for (Map.Entry<TradeObject, Double> aa : list) {
            double value = aa.getValue();
            if(CommonFunctions.twoDecimals(value) != 0.0)
                temp.put(aa.getKey(), value); 
        } 
        return temp; 
    } 

    public void incrementIndex(int amount) {
        if(scrollable) {
            for(int i = 0; i < amount; i++) {
                if(type.equals("TOPOBJCHANGE")) {
                    if(index+1 == historyDisplayObjects.size())
                        index = 0;
                    else
                        index += 1;
                } else {
                    if(index+1 == displayObjects.size())
                        index = 0;
                    else
                        index += 1;
                }
            }
            updateDisplay();
        }
    }

    public void decrementIndex(int amount) {
        if(scrollable) {
            for(int i = 0; i < amount; i++) {
                if(type.equals("TOPOBJCHANGE")) {
                    if(index-1 < 0)
                        index = historyDisplayObjects.size()-1;
                    else
                        index -= 1;
                } else {
                    if(index-1 < 0)
                        index = displayObjects.size()-1;
                    else
                        index -= 1;
                }
            }
            updateDisplay();
        }
    }

    private String getTitle() {
        switch(type) {
            case "TOPSTOCK":
                return "Stock";
            case "TOPTOTALSTOCK":
                return "Total Stock";
            case "TOPVALUE":
                return "Value";
            case "TOPBUY":
                return "Buy";
            case "TOPSELL":
                return "Sell";
            case "TOPPRODUCTTAX":
                return "Product Tax";
            case "TOPMEDIAN":
                return "Median";
            case "TOPSTATICPRICE":
                return "Static Price";
            case "TOPSTARTPRICE":
                return "Start Price";
            case "TOPOBJCHANGE":
                return "Change";
            default:
                return "";
        }
    }

    private Stat getStat(TradeObject to) {
        LanguageFile L = hc.getLanguageFile();
        switch(type) {
            case "TOPSTOCK":
                return new Stat(to.getStock(), "&a" + L.formatDouble(to.getStock()));
            case "TOPTOTALSTOCK":
                return new Stat(to.getTotalStock(), "&a" + L.formatDouble(to.getTotalStock()));
            case "TOPVALUE":
                return new Stat(to.getValue(), "&a" + L.formatDouble(to.getValue()));
            case "TOPBUY":
                if (to.getType() == TradeObjectType.ENCHANTMENT) {
                    double cost = to.getBuyPrice(enchantClass);
                    cost = cost + to.getPurchaseTax(cost);
                    return new Stat(cost, "&a" + L.formatMoney(cost));
                } else {
                    double pcost = to.getBuyPrice(1);
                    double cost = CommonFunctions.twoDecimals((pcost + to.getPurchaseTax(pcost)) * multiplier);
                    return new Stat(cost, "&a" + L.formatMoney(cost));
                }
            case "TOPSELL":
                if (to.getType() == TradeObjectType.ENCHANTMENT) {
                    double value = to.getSellPrice(enchantClass);
                    value = CommonFunctions.twoDecimals((value - to.getSalesTaxEstimate(value)) * multiplier);
                    return new Stat(value, "&a" + L.formatMoney(value));
                } else {
                    double value = to.getSellPrice(1);
                    value = CommonFunctions.twoDecimals((value - to.getSalesTaxEstimate(value)) * multiplier);
                    return new Stat(value, "&a" + L.formatMoney(value));
                }
            case "TOPPRODUCTTAX":
                if (to.getType() == TradeObjectType.ENCHANTMENT) {
                    double price = to.getBuyPrice(enchantClass);
                    double taxpaid = CommonFunctions.twoDecimals(to.getPurchaseTax(price) * multiplier);
                    return new Stat(taxpaid, "&a" + L.formatMoney(taxpaid));
                } else if (to.getType() == TradeObjectType.ITEM) {
                    double taxpaid = CommonFunctions.twoDecimals(to.getPurchaseTax(to.getBuyPrice(1) * multiplier));
                    return new Stat(taxpaid, "&a" + L.formatMoney(taxpaid));
                } else {
                    BasicTradeObject bo = (BasicTradeObject)to;
                    double taxpaid = CommonFunctions.twoDecimals(bo.getPurchaseTax(bo.getBuyPrice(1) * multiplier));
                    return new Stat(taxpaid, "&a" + L.formatMoney(taxpaid));
                }
            case "TOPMEDIAN":
                return new Stat(to.getMedian(), "&a" + L.formatDouble(to.getMedian()));
            case "TOPSTATICPRICE":
                return new Stat(to.getStaticPrice(), "&a" + L.formatDouble(to.getStaticPrice()));
            case "TOPSTARTPRICE":
                return new Stat(to.getStartPrice(), "&a" + L.formatDouble(to.getStartPrice()));
            default:
                return new Stat(0,"");
        }
    }
}

class Stat {
    double value;
    String display;
    Stat(double value, String display) {
        this.value = value;
        this.display = display;
    }
}