package regalowl.hyperconomy.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import regalowl.hyperconomy.HyperConomy;
import regalowl.hyperconomy.HyperEconomy;
import regalowl.hyperconomy.account.HyperAccount;
import regalowl.hyperconomy.account.HyperBank;
import regalowl.hyperconomy.account.HyperPlayer;
import regalowl.hyperconomy.shop.PlayerShop;
import regalowl.hyperconomy.shop.Shop;
import regalowl.hyperconomy.tradeobject.BasicTradeObject;
import regalowl.hyperconomy.tradeobject.EnchantmentClass;
import regalowl.hyperconomy.tradeobject.TradeObject;
import regalowl.hyperconomy.tradeobject.TradeObjectStatus;
import regalowl.hyperconomy.tradeobject.TradeObjectType;
import regalowl.hyperconomy.util.LanguageFile;
import regalowl.simpledatalib.CommonFunctions;

public class Hctop extends BaseCommand implements HyperCommand {
	private final int numberPerPage = 10;

	public Hctop(HyperConomy hc) {
		super(hc, false);
	}

	@Override
	public CommandData onCommand(CommandData data) {
		if (!validate(data)) return data;
		boolean requireShop = hc.getConf().getBoolean("shop.limit-info-commands-to-shops");
		Shop s = dm.getHyperShopManager().getShop(hp);
		boolean hasPlayer = (hp != null) ? true:false;
		boolean hasShop = (s != null) ? true:false;
		if (hasPlayer && requireShop && !hasShop && !hp.hasPermission("hyperconomy.admin")) {
			data.addResponse(L.get("REQUIRE_SHOP_FOR_INFO"));
			return data;
		}
		try {
			if(args.length == 0) {
				data.addResponse(L.get("HCTOP_INVALID"));
				return data;
			}

			//BALANCE
			if(args[0].toUpperCase().matches("BALANCE|ACCOUNTS|BAL|ACT")) {
				try {
					if (hc.getMC().useExternalEconomy()) {
						data.addResponse(L.get("ONLY_AVAILABLE_INTERNAL"));
						return data;
					}
					int pe = 1;
					String filter = "ALL";
					if (args.length == 2 || args.length == 3) {
						pe = Integer.parseInt(args[1]);
						if(args.length == 3) {
							if(args[2].toUpperCase().matches("ALL|BANKS|PLAYERS")) {
								filter = args[2].toUpperCase();
							} else {
								data.addResponse("HCTOP_INVALID");
								return data;
							}
						}
					} else if (args.length == 1) {

					} else {
						data.addResponse(L.get("HCTOP_INVALID"));
						return data;
					}
					ArrayList<HyperAccount> accounts = new ArrayList<HyperAccount>();
					if(filter.equals("ALL") || filter.equals("PLAYERS")) {
						for (HyperPlayer hp : dm.getHyperPlayerManager().getHyperPlayers()) {
							accounts.add(hp);
						}
					}
					if(filter.equals("ALL") || filter.equals("BANKS")) {
						for (HyperBank hb : dm.getHyperBankManager().getHyperBanks()) {
							accounts.add(hb);
						}
					}
					Collections.sort(accounts, new Comparator<HyperAccount>() { 
						public int compare(HyperAccount o1, HyperAccount o2) { 
							return ((Double)o2.getBalance()).compareTo((Double)o1.getBalance()); 
						} 
					});
					double serverTotal = 0.0;
					for (int i = 0; i < accounts.size(); i++) {
						serverTotal += accounts.get(i).getBalance();
					}
					data.addResponse(L.get("TOP_BALANCE"));
					data.addResponse(L.f(L.get("TOP_BALANCE_PAGE"), pe, (int)Math.ceil(accounts.size()/10.0)));
					data.addResponse(L.f(L.get("TOP_BALANCE_TOTAL"), L.formatMoney(serverTotal)));
					int ps = pe - 1;
					ps *= 10;
					pe *= 10;
					for (int i = ps; i < pe; i++) {
						if (i > (accounts.size() - 1)) {
							data.addResponse(L.get("REACHED_END"));
							return data;
						}
						HyperAccount account = accounts.get(i);
						data.addResponse(L.f(L.get("TOP_BALANCE_BALANCE"), account.getName(), L.formatMoney(account.getBalance()), (i + 1)));
					}
				} catch (Exception e) {
					data.addResponse(L.get("HCTOP_INVALID"));
				}

			//OBJECT CHANGE	
			} else if(args[0].toUpperCase().matches("OBJCHANGE|OC|OBJECTCHANGE")) {
				int timeValueHours = 1;
				int timeValue;
				String timeIncrement;
				int selectedPage = 1;
				String filter = "ALL";
				boolean categoryFilter = false;
				if(args.length > 1) {
					timeIncrement = args[1].substring(args[1].length()-1).toUpperCase();
					timeValue = Integer.parseInt(args[1].substring(0,args[1].length()-1));
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
						data.addResponse(L.get("HCTOP_INVALID"));
						return data;
					}
				} else {
					data.addResponse(L.get("HCTOP_INVALID"));
					return data;
				}
				if(args.length > 2) {
					selectedPage = Integer.parseInt(args[2]);
				}
				if(args.length > 3) {
					if(args[2].toUpperCase().matches("ALL|ITEMS|ENCHANTS")) {
						filter = args[3];
					} else if(args[3].toUpperCase().startsWith("C:")) {
						if(hc.getDataManager().getCategories().contains(args[3].substring(2).toLowerCase())) {
							categoryFilter = true;
							filter = args[3].toLowerCase();
						}
					} else {
						data.addResponse(L.get("HCTOP_INVALID"));
						return data;
					}
				}
				HashMap<TradeObject, Double> history = hc.getHistory().getObjectPercentChange(data.getHyperPlayer().getEconomy(), timeValueHours);
				history = sortByValue(history);
				HashMap<TradeObject, Double> displayObjects = new HashMap<TradeObject, Double>();
				ArrayList<TradeObject> historyKeys = new ArrayList<TradeObject>(history.keySet());
				if(filter.equals("ALL")) {
					displayObjects = history;
				} else {
					for(TradeObject to : historyKeys) {
						if(history.get(to) == 0) continue;
						if(categoryFilter) {
							if(to.getCategories().contains(filter.substring(2).toLowerCase()))
								displayObjects.put(to, history.get(to));
						} else if(to.getType() == TradeObjectType.ENCHANTMENT && filter.equals("ENCHANTS")) {
							displayObjects.put(to, history.get(to));
						} else if(to.getType() == TradeObjectType.ITEM && filter.equals("ITEMS")) {
							displayObjects.put(to, history.get(to));
						}
					} 
				}      
				if (selectedPage < 1) selectedPage = 1;
				int startIndex = (selectedPage - 1) * numberPerPage;
				int endIndex = startIndex + numberPerPage;
				int numberOfPages = displayObjects.size()/numberPerPage + 1;
				data.addResponse("&bTop Price Changes in last "+args[1]);
				data.addResponse(L.f(L.get("PAGE_NUMBER"), selectedPage, numberOfPages));
				ArrayList<TradeObject> keys = new ArrayList<TradeObject>(displayObjects.keySet());
				for (int i = startIndex; i < endIndex; i++) {
					if (i >= displayObjects.size()) {
						data.addResponse(L.get("YOU_HAVE_REACHED_THE_END"));
						break;
					}
					TradeObject to = keys.get(i);
					double percentChange = displayObjects.get(to);
					if(percentChange > 0.0) {
						data.addResponse("&f"+to.getDisplayName() + ": &a" + L.formatDouble(percentChange)+"%");
					} else {
						data.addResponse("&f"+to.getDisplayName() + ": &c" + L.formatDouble(percentChange)+"%");
					}
				}

			//ACCOUNT CHANGE
			} else if(args[0].toUpperCase().matches("ACTCHANGE|AC|ACCOUNTCHANGE")) {
				int timeValueHours = 1;
				int timeValue;
				String timeIncrement;
				int selectedPage = 1;
				String filter = "ALL";
				if(args.length > 1) {
					timeIncrement = args[1].substring(args[1].length()-1).toUpperCase();
					timeValue = Integer.parseInt(args[1].substring(0,args[1].length()-1));
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
						data.addResponse(L.get("HCTOP_INVALID"));
						return data;
					}
				} else {
					data.addResponse(L.get("HCTOP_INVALID"));
					return data;
				}
				if(args.length > 2) {
					selectedPage = Integer.parseInt(args[2]);
				}
				if(args.length > 3) {
					if(args[2].toUpperCase().matches("ALL|PLAYERS|BANKS")) {
						filter = args[3];
					} else {
						data.addResponse(L.get("HCTOP_INVALID"));
						return data;
					}
				}
				HashMap<HyperAccount, Double> history = hc.getHistory().getAccountPercentChange(data.getHyperPlayer().getEconomy(), timeValueHours);
				history = sortByValue(history);
				HashMap<HyperAccount, Double> displayObjects = new HashMap<HyperAccount,Double>();
				ArrayList<HyperAccount> historyKeys = new ArrayList<HyperAccount>(history.keySet());
				for(HyperAccount account : historyKeys) {
					if(account instanceof HyperPlayer && filter.equals("PLAYERS")) {
                        displayObjects.put(account, history.get(account));
                    } else if(account instanceof HyperBank && filter.equals("BANKS")) {
                        displayObjects.put(account, history.get(account));
                    }
				}     
				if (selectedPage < 1) selectedPage = 1;
				int startIndex = (selectedPage - 1) * numberPerPage;
				int endIndex = startIndex + numberPerPage;
				int numberOfPages = displayObjects.size()/numberPerPage + 1;
				data.addResponse("&bTop Account Changes in last "+args[1]);
				data.addResponse(L.f(L.get("PAGE_NUMBER"), selectedPage, numberOfPages));
				ArrayList<HyperAccount> keys = new ArrayList<HyperAccount>(displayObjects.keySet());
				for (int i = startIndex; i < endIndex; i++) {
					if (i >= displayObjects.size()) {
						data.addResponse(L.get("YOU_HAVE_REACHED_THE_END"));
						break;
					}
					HyperAccount account = keys.get(i);
					double percentChange = displayObjects.get(account);
					if(percentChange > 0.0) {
						data.addResponse("&f"+account.getName() + ": &a" + L.formatDouble(percentChange)+"%");
					} else {
						data.addResponse("&f"+account.getName() + ": &c" + L.formatDouble(percentChange)+"%");
					}
				}

			//OBJECTSTATS
			} else if(args[0].toUpperCase().matches("STOCK|TOTALSTOCK|VALUE|BUY|SELL|PRODUCTTAX|MEDIAN|STATICPRICE|STARTPRICE")) {
				EnchantmentClass enchantClass;
				if(hc.getConf().getBoolean("shop.enchant-books-only"))
					enchantClass = EnchantmentClass.BOOK;
				else
					enchantClass = EnchantmentClass.DIAMOND;
				int multiplier = 1;
				String filter = "ALL";
				int selectedPage = 1;
				boolean categoryFilter = false;
				if (args.length > 1) {
					selectedPage = Integer.parseInt(args[1]);
				}
				if(args.length > 2) {
					if(args[2].toUpperCase().matches("ALL|ITEMS|ENCHANTS")) {
						filter = args[2].toUpperCase();
					} else if(args[2].toUpperCase().startsWith("C:")) {
						if(hc.getDataManager().getCategories().contains(args[2].substring(2).toLowerCase())) {
							categoryFilter = true;
							filter = args[2].toLowerCase();
						}
					} else {
						data.addResponse(L.get("HCTOP_INVALID"));
						return data;
					}
				}
				if(args.length > 3 && args[0].toUpperCase().matches("BUY|SELL|PRODUCTTAX")) {
					if(filter.equals("ENCHANTS")) {
						if(EnchantmentClass.fromString(args[3]) != EnchantmentClass.NONE)
							enchantClass = EnchantmentClass.fromString(args[3]);
						else {
							data.addResponse(L.get("HCTOP_INVALID"));
							return data;
						}
					} else if(filter.equals("ITEMS")) {
						multiplier = Integer.parseInt(args[3]);
					} else {
						data.addResponse(L.get("HCTOP_INVALID"));
						return data;
					}
				} else if(args.length > 3) {
					data.addResponse(L.get("HCTOP_INVALID"));
					return data;
				}
				HyperEconomy he = super.getEconomy();
				ArrayList<TradeObject> allObjects = he.getTradeObjects(s);
				final EnchantmentClass compareEnchantClass = enchantClass;
				final int compareMultiplier = multiplier;
				Collections.sort(allObjects, new Comparator<TradeObject>(){
					public int compare(TradeObject to1, TradeObject to2) {
						Double s1 = getStat(to1, args[0], compareEnchantClass, compareMultiplier).value;
						Double s2 = getStat(to2, args[0], compareEnchantClass, compareMultiplier).value;
						if (s1 < s2) return 1;
						if (s1 > s2) return -1;
						return 0;
					}
				});
				ArrayList<TradeObject> displayObjects = new ArrayList<TradeObject>();
				for (TradeObject to:allObjects) {
					if(getStat(to, args[0], compareEnchantClass, compareMultiplier).value == 0) continue;
					if(hasShop && s.isBanned(to.getName())) continue;
					if(hasShop && to.isShopObject()) {
						PlayerShop ps = (PlayerShop)s;
						if(!ps.isAllowed(hp) && !hp.hasPermission("hyperconomy.admin")) {
							if(to.getShopObjectStatus() == TradeObjectStatus.NONE) continue;
						}
					}
					if(filter.equals("ALL")) {
						displayObjects.add(to);
					}
					if(categoryFilter) {
                        if(to.getCategories().contains(filter.substring(2).toLowerCase()))
                        	displayObjects.add(to);
                    } else if(to.getType() == TradeObjectType.ENCHANTMENT && filter.equals("ENCHANTS")) {
                        displayObjects.add(to);
                    } else if(to.getType() == TradeObjectType.ITEM && filter.equals("ITEMS")) {
                        displayObjects.add(to);
                    }
				}
				if (selectedPage < 1) selectedPage = 1;
				int startIndex = (selectedPage - 1) * numberPerPage;
				int endIndex = startIndex + numberPerPage;
				int numberOfPages = displayObjects.size()/numberPerPage + 1;
				data.addResponse(L.f(L.get("PAGE_NUMBER"), selectedPage, numberOfPages));
				for (int i = startIndex; i < endIndex; i++) {
					if (i >= displayObjects.size()) {
						data.addResponse(L.get("YOU_HAVE_REACHED_THE_END"));
						break;
					}
					TradeObject to = displayObjects.get(i);
					if (to.isShopObject()) {
						data.addResponse("&f" + to.getDisplayName() + ": &a" + getStat(to, args[0], enchantClass, multiplier).display + " &f(&e" + to.getShopObjectStatus().toString() + "&f)");
					} else {
						data.addResponse("&f" + to.getDisplayName() + ": &a" + getStat(to, args[0], enchantClass, multiplier).display);
					}
				}
			}
			
		} catch (Exception e) {
			data.addResponse(L.get("HCTOP_INVALID"));
		}
		return data;
	}

	public static <T> HashMap<T, Double> sortByValue(HashMap<T, Double> hm) {
        List<Map.Entry<T, Double> > list = new LinkedList<Map.Entry<T, Double> >(hm.entrySet()); 
        Collections.sort(list, new Comparator<Map.Entry<T, Double> >() { 
            public int compare(Map.Entry<T, Double> o1, Map.Entry<T, Double> o2) { 
                return ((Double)Math.abs(o2.getValue())).compareTo((Double)Math.abs(o1.getValue())); 
            } 
        }); 
        HashMap<T, Double> temp = new LinkedHashMap<T, Double>(); 
        for (Map.Entry<T, Double> aa : list) {
            double value = aa.getValue();
			temp.put(aa.getKey(), value); 
        } 
        return temp;
	} 

	private Stat getStat(TradeObject to, String type, EnchantmentClass enchantClass, int multiplier) {
        LanguageFile L = hc.getLanguageFile();
        switch(type.toUpperCase()) {
            case "STOCK":
                return new Stat(to.getStock(), "&a" + L.formatDouble(to.getStock()));
            case "TOTALSTOCK":
                return new Stat(to.getTotalStock(), "&a" + L.formatDouble(to.getTotalStock()));
            case "VALUE":
                return new Stat(to.getValue(), "&a" + L.formatDouble(to.getValue()));
            case "BUY":
                if (to.getType() == TradeObjectType.ENCHANTMENT) {
                    double cost = to.getBuyPrice(enchantClass);
                    cost = cost + to.getPurchaseTax(cost);
                    return new Stat(cost, "&a" + L.formatMoney(cost));
                } else {
                    double pcost = to.getBuyPrice(1);
                    double cost = CommonFunctions.twoDecimals((pcost + to.getPurchaseTax(pcost)) * multiplier);
                    return new Stat(cost, "&a" + L.formatMoney(cost));
                }
            case "SELL":
                if (to.getType() == TradeObjectType.ENCHANTMENT) {
                    double value = to.getSellPrice(enchantClass);
                    value = CommonFunctions.twoDecimals((value - to.getSalesTaxEstimate(value)) * multiplier);
                    return new Stat(value, "&a" + L.formatMoney(value));
                } else {
                    double value = to.getSellPrice(1);
                    value = CommonFunctions.twoDecimals((value - to.getSalesTaxEstimate(value)) * multiplier);
                    return new Stat(value, "&a" + L.formatMoney(value));
                }
            case "PRODUCTTAX":
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
            case "MEDIAN":
                return new Stat(to.getMedian(), "&a" + L.formatDouble(to.getMedian()));
            case "STATICPRICE":
                return new Stat(to.getStaticPrice(), "&a" + L.formatDouble(to.getStaticPrice()));
            case "STARTPRICE":
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
