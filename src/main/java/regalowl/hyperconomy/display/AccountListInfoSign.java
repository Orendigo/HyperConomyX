package regalowl.hyperconomy.display;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import regalowl.hyperconomy.HyperConomy;
import regalowl.hyperconomy.account.HyperAccount;
import regalowl.hyperconomy.account.HyperBank;
import regalowl.hyperconomy.account.HyperPlayer;
import regalowl.hyperconomy.minecraft.HLocation;
import regalowl.hyperconomy.minecraft.HSign;
import regalowl.hyperconomy.util.LanguageFile;
import regalowl.simpledatalib.CommonFunctions;

public class AccountListInfoSign extends InfoSign implements InteractiveInfoSign {
    ArrayList<HyperAccount> displayObjects = new ArrayList<HyperAccount>();
    HashMap<HyperAccount, Double> historyDisplayObjects = new HashMap<HyperAccount, Double>();
    boolean scrollable;
    int index;
    String timeIncrement;
    int timeValue;
    int timeValueHours;
    String filter;

    public AccountListInfoSign(HyperConomy hc, HLocation loc, String economy, String type, String[] parameters) {
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
        else if(filter.equals("PLAYERS"))
            filter = "PLAYERS";
        else if(filter.equals("BANKS"))
            filter = "BANKS";
        else
            filter = "ALL";
        parameters[1] = filter;
        if(type.equals("TOPACTCHANGE")) {
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
        }
    }

	public void update() {
        if(type.equals("TOPACTCHANGE")) {
            HashMap<HyperAccount, Double> allObjects = hc.getHistory().getAccountPercentChange(economy, timeValueHours);
            historyDisplayObjects.clear();
            if(!filter.equals("ALL")) {
                List<HyperAccount> keys = new ArrayList<HyperAccount>(allObjects.keySet());
                for(HyperAccount account : keys) {
                    if(filter.equals("PLAYERS") && account instanceof HyperPlayer)
                        historyDisplayObjects.put(account, allObjects.get(account));
                    else if(filter.equals("BANKS") && account instanceof HyperBank)
                        historyDisplayObjects.put(account, allObjects.get(account));
                }
            } else {
                historyDisplayObjects = allObjects;
            }
            historyDisplayObjects = sortByValue(historyDisplayObjects);
        } else {
            ArrayList<HyperAccount> allObjects = hc.getDataManager().getAccounts();
            displayObjects.clear();	
            if(!filter.equals("ALL")) {
                for(HyperAccount account : allObjects) {
                    if(filter.equals("PLAYERS") && account instanceof HyperPlayer)
                        displayObjects.add(account);
                    else if(filter.equals("BANKS") && account instanceof HyperBank)
                        displayObjects.add(account);
                }
            } else {
                displayObjects = allObjects;
            }
            Collections.sort(displayObjects, new Comparator<HyperAccount>(){
                public int compare(HyperAccount a1, HyperAccount a2) {
                    Double s1 = a1.getBalance();
                    Double s2 = a2.getBalance();
                    if (s1 < s2) return 1;
                    if (s1 > s2) return -1;
                    return 0;
                }
            });
        }
        if(index >= historyDisplayObjects.size())
            index = historyDisplayObjects.size()-1;
        if(index < 0)
            index = 0;
        updateDisplay();
    }

    public void updateDisplay() {
        String name;
        if(type.equals("TOPACTCHANGE")) {
            lines[0] = "&a#"+(index+1)+" &f"+"Change"+":";
            if(historyDisplayObjects.isEmpty()) {
                lines[1] = " ";
                lines[2] = " ";
                lines[3] = "&cNo Changes";
                updateHSign();
                return;
            }
            List<HyperAccount> keys = new ArrayList<HyperAccount>(historyDisplayObjects.keySet());
            name = keys.get(index).getName();
            double percentChange = historyDisplayObjects.get(keys.get(index));
            if(percentChange < 0.0)
                lines[3] = "&f" + timeValue + timeIncrement + "&c(" + hc.getLanguageFile().formatDouble(percentChange) + "%)";
            else if (percentChange > 0)
                lines[3] = "&f" + timeValue + timeIncrement + "&a(+" + hc.getLanguageFile().formatDouble(percentChange) + "%)";
        } else {
            lines[0] = "&a#"+(index+1)+" &f"+"Change"+":";
            if(displayObjects.isEmpty()) {
                lines[1] = " ";
                lines[2] = " ";
                lines[3] = "&cNo Items";
                updateHSign();
                return;
            }
            name = displayObjects.get(index).getName();
            lines[3] = hc.getLanguageFile().formatMoney(displayObjects.get(index).getBalance());
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

    public static HashMap<HyperAccount, Double> sortByValue(HashMap<HyperAccount, Double> hm) {
        List<Map.Entry<HyperAccount, Double> > list = new LinkedList<Map.Entry<HyperAccount, Double> >(hm.entrySet()); 
        Collections.sort(list, new Comparator<Map.Entry<HyperAccount, Double> >() { 
            public int compare(Map.Entry<HyperAccount, Double> o1, Map.Entry<HyperAccount, Double> o2) { 
                return ((Double)Math.abs(o2.getValue())).compareTo((Double)Math.abs(o1.getValue())); 
            } 
        }); 
        HashMap<HyperAccount, Double> temp = new LinkedHashMap<HyperAccount, Double>(); 
        for (Map.Entry<HyperAccount, Double> aa : list) {
            double value = aa.getValue();
            if(CommonFunctions.twoDecimals(value) != 0.0)
                temp.put(aa.getKey(), value); 
        } 
        return temp; 
    } 

    public void incrementIndex(int amount) {
        if(scrollable) {
            for(int i = 0; i < amount; i++) {
                if(type.equals("TOPACTCHANGE")) {
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
                if(type.equals("TOPACTCHANGE")) {
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
}