package regalowl.hyperconomy.util;

import java.util.ArrayList;
import java.util.HashMap;

import regalowl.simpledatalib.CommonFunctions;
import regalowl.simpledatalib.sql.QueryResult;
import regalowl.simpledatalib.sql.SQLRead;
import regalowl.simpledatalib.sql.SQLWrite;
import regalowl.hyperconomy.DataManager;
import regalowl.hyperconomy.HyperConomy;
import regalowl.hyperconomy.account.HyperAccount;
import regalowl.hyperconomy.display.InfoSignHandler;
import regalowl.hyperconomy.tradeobject.EnchantmentClass;
import regalowl.hyperconomy.tradeobject.TradeObject;
import regalowl.hyperconomy.tradeobject.TradeObjectType;


public class History {
	
	private HyperConomy hc;
	private DataManager em;
	private InfoSignHandler isign;
	private SQLWrite sw;
	private SQLRead sr;

	private long historylogtaskid;

	private int daysToSaveHistory;
	
	private long lastTime;
	private long timeCounter;
	private boolean useHistory;
	private boolean timeCounterAdded;
	
	private final int millisecondsInHour = 3600000;
	
	public History(HyperConomy hc) {
		this.hc = hc;
		useHistory = hc.getConf().getBoolean("enable-feature.price-history-storage");
		if (!useHistory) {return;}
		em = hc.getDataManager();
		isign = hc.getInfoSignHandler();
		sw = hc.getSQLWrite();
		sr = hc.getSQLRead();
		daysToSaveHistory = hc.getConf().getInt("history.days-to-save");
		lastTime = System.currentTimeMillis();
		timeCounter = getTimeCounter();
		startTimer();
	}
	
	public boolean useHistory() {
		return useHistory;
	}
	

	public Long getTimeCounter() {
		Long value = 0L;
		QueryResult result = sr.select("SELECT VALUE FROM hyperconomy_settings WHERE SETTING = 'history_time_counter'");
		if (result.next()) {
			try {
				value = Long.parseLong(result.getString("VALUE"));
			} catch (Exception e) {
				value = 0L;
			}
		} else {
			if (!timeCounterAdded) {
				timeCounterAdded = true;
				addSetting("history_time_counter", "0");
			}
		}
		result.close();
		return value;
	}

	public void addSetting(String setting, String value) {
		sw.addToQueue("INSERT INTO hyperconomy_settings (SETTING, VALUE, TIME) VALUES ('" + setting + "', '" + value + "', NOW() )");
	}

	public void updateSetting(String setting, String value) {
		sw.addToQueue("UPDATE hyperconomy_settings SET VALUE='" + value + "' WHERE SETTING = '" + setting + "'");
	}

	
	private void startTimer() {
		historylogtaskid = hc.getMC().runRepeatingTask(new Runnable() {
			public void run() {
				long currentTime = System.currentTimeMillis();
				timeCounter += (currentTime - lastTime);
				lastTime = currentTime;
				if (timeCounter >= millisecondsInHour) {
					timeCounter = 0;
					writeHistoryValues();
					hc.getMC().runTaskLater(new Runnable() {
						public void run() {
							if (isign != null) isign.updateSigns();
						}
					}, 1200L);
				}
				updateSetting("history_time_counter", timeCounter + "");
			}
		}, 600L, 600L);
	}
	

	
	private void writeHistoryValues() {
		ArrayList<TradeObject> objects = em.getTradeObjects();
		ArrayList<HyperAccount> accounts = em.getAccounts();
		ArrayList<String> statements = new ArrayList<String>();
		
		//Trade Objects
		for (TradeObject object : objects) {
			statements.add("INSERT INTO hyperconomy_history (OBJECT, ECONOMY, TIME, PRICE) "
					+ "VALUES ('"+object.getName()+"','"+object.getEconomy()+"', NOW() ,'"+object.getBuyPrice(1)+"')");
		}
		if (hc.getSQLManager().useMySQL()) {
			statements.add("DELETE FROM hyperconomy_history WHERE TIME < DATE_SUB(NOW(), INTERVAL " + daysToSaveHistory + " DAY)");
		} else {
			statements.add("DELETE FROM hyperconomy_history WHERE TIME < date('now','" + formatSQLiteTime(daysToSaveHistory * -1) + " day')");
		}

		//Accounts
		for(HyperAccount account : accounts) {
			statements.add("INSERT INTO hyperconomy_history_accounts (ACCOUNT, TIME, BALANCE) "
					+ "VALUES ('"+account.getName()+"', NOW() ,'"+account.getBalance()+"')");
		
		}
		if (hc.getSQLManager().useMySQL()) {
			statements.add("DELETE FROM hyperconomy_history_accounts WHERE TIME < DATE_SUB(NOW(), INTERVAL " + daysToSaveHistory + " DAY)");
		} else {
			statements.add("DELETE FROM hyperconomy_history_accounts WHERE TIME < date('now','" + formatSQLiteTime(daysToSaveHistory * -1) + " day')");
		}
		sw.addToQueue(statements);
	}

    
    public void stopHistoryLog() {
    	hc.getMC().cancelTask(historylogtaskid);
    }

	//Object Functions

	public double getObjectHistoricValue(String name, String economy, int count) {
		try {
			count -= 1;
			QueryResult result = sr.select("SELECT PRICE FROM hyperconomy_history WHERE OBJECT = '"+name+"' AND ECONOMY = '"+economy+"' ORDER BY TIME DESC LIMIT "+count+",1");
			if (result.next()) {
				return Double.parseDouble(result.getString("PRICE"));
			}
			result.close();
			return -1.0;
		} catch (Exception e) {
			hc.gSDL().getErrorWriter().writeError(e, "getObjectHistoricValue() passed arguments: name = '" + name + "', economy = '" + economy + "', count = '" + count + "'");
			return -1.0;
		}
	}

	/**
	 * This function must be called from an asynchronous thread!
	 * @param ho
	 * @param timevalue
	 * @return The percentage change in theoretical price for the given object and timevalue in hours
	 */
    
	public synchronized String getObjectPercentChange(TradeObject ho, int timevalue) {
		if (ho == null || sr == null) {
			hc.gSDL().getErrorWriter().writeError("getObjectPercentChange passed null HyperObject or SQLRead");
			return "?";
		}
		double percentChange = 0.0;
		double historicvalue = getObjectHistoricValue(ho.getName(), ho.getEconomy(), timevalue);
		if (historicvalue == -1.0 || historicvalue == 0.0) return "?";
		double currentvalue = ho.getBuyPrice(1);
		percentChange = ((currentvalue - historicvalue) / historicvalue) * 100.0;
		percentChange = CommonFunctions.round(percentChange, 3);
		return percentChange + "";
	}
	
	/**
	 * This function must be called from an asynchronous thread!
	 * @param timevalue
	 * @param economy
	 * @return The percentage change in theoretical price for the given object and timevalue in hours
	 */
	
	public synchronized HashMap<TradeObject, String> getObjectPercentChangeAsString(String economy, int timevalue) {
		if (sr == null) return null;
		HashMap<TradeObject, ArrayList<Double>> allValues = new HashMap<TradeObject, ArrayList<Double>>();
		QueryResult result = sr.select("SELECT OBJECT, PRICE FROM hyperconomy_history WHERE ECONOMY = '" + economy + "' ORDER BY TIME DESC");
		while (result.next()) {
			TradeObject ho = em.getEconomy(economy).getTradeObject(result.getString("OBJECT"));
			double price = result.getDouble("PRICE");
			if (!allValues.containsKey(ho)) {
				ArrayList<Double> values = new ArrayList<Double>();
				values.add(price);
				allValues.put(ho, values);
			} else {
				ArrayList<Double> values = allValues.get(ho);
				values.add(price);
				allValues.put(ho, values);
			}
		}
		result.close();
		
		ArrayList<TradeObject> hobjects =  em.getEconomy(economy).getTradeObjects();
		HashMap<TradeObject, String> relevantValues = new HashMap<TradeObject, String>();
		for (TradeObject ho:hobjects) {
			if (allValues.containsKey(ho)) {
				ArrayList<Double> historicValues = allValues.get(ho);
				if (historicValues.size() >= timevalue) {
					double historicValue = historicValues.get(timevalue - 1);
					double currentvalue = 0.0;
					currentvalue = ho.getBuyPrice(1);
					if (historicValue == 0.0) {
						relevantValues.put(ho, "?");
						continue;
					}
					double percentChange = ((currentvalue - historicValue) / historicValue) * 100.0;
					percentChange = CommonFunctions.round(percentChange, 3);
					String stringValue = percentChange + "";
					relevantValues.put(ho, stringValue);
				} else {
					relevantValues.put(ho, "?");
				}
			} else {
				relevantValues.put(ho, "?");
			}
		}
		return relevantValues;
	}

	public synchronized HashMap<TradeObject, Double> getObjectPercentChange(String economy, int timevalue) {
		if (sr == null) return null;
		HashMap<TradeObject, ArrayList<Double>> allValues = new HashMap<TradeObject, ArrayList<Double>>();
		QueryResult result = sr.select("SELECT OBJECT, PRICE FROM hyperconomy_history WHERE ECONOMY = '" + economy + "' ORDER BY TIME DESC");
		while (result.next()) {
			TradeObject ho = em.getEconomy(economy).getTradeObject(result.getString("OBJECT"));
			double price = result.getDouble("PRICE");
			if (!allValues.containsKey(ho)) {
				ArrayList<Double> values = new ArrayList<Double>();
				values.add(price);
				allValues.put(ho, values);
			} else {
				ArrayList<Double> values = allValues.get(ho);
				values.add(price);
				allValues.put(ho, values);
			}
		}
		result.close();
		
		ArrayList<TradeObject> hobjects =  em.getEconomy(economy).getTradeObjects();
		HashMap<TradeObject, Double> relevantValues = new HashMap<TradeObject, Double>();
		for (TradeObject ho:hobjects) {
			if (allValues.containsKey(ho)) {
				ArrayList<Double> historicValues = allValues.get(ho);
				if (historicValues.size() >= timevalue) {
					double historicValue = historicValues.get(timevalue - 1);
					double currentvalue = 0.0;
					currentvalue = ho.getBuyPrice(1);
					if (historicValue == 0.0) {
						relevantValues.put(ho, 0.0);
						continue;
					}
					double percentChange = ((currentvalue - historicValue) / historicValue) * 100.0;
					percentChange = CommonFunctions.round(percentChange, 3);
					relevantValues.put(ho, percentChange);
				} else {
					relevantValues.put(ho, 0.0);
				}
			} else {
				relevantValues.put(ho, 0.0);
			}
		}
		return relevantValues;
	}




	//Account Functions

	public double getAccountHistoricValue(String name, int count) {
		try {
			count -= 1;
			QueryResult result = sr.select("SELECT BALANCE FROM hyperconomy_history_accounts WHERE ACCOUNT = '"+name+"' ORDER BY TIME DESC LIMIT "+count+",1");
			if (result.next()) {
				return Double.parseDouble(result.getString("BALANCE"));
			}
			result.close();
			return -1.0;
		} catch (Exception e) {
			hc.gSDL().getErrorWriter().writeError(e, "getAccountHistoricValue() passed arguments: name = '" + name + "', count = '" + count + "'");
			return -1.0;
		}
	}

	/**
	 * This function must be called from an asynchronous thread!
	 * @param ho
	 * @param timevalue
	 * @return The percentage change in theoretical price for the given account and timevalue in hours
	 */
    
	public synchronized String getAccountPercentChange(HyperAccount account, int timevalue) {
		if (account == null || sr == null) {
			hc.gSDL().getErrorWriter().writeError("getAccountPercentChange passed null HyperObject or SQLRead");
			return "?";
		}
		double percentChange = 0.0;
		double historicvalue = getAccountHistoricValue(account.getName(), timevalue);
		if (historicvalue == -1.0 || historicvalue == 0.0) return "?";
		double currentvalue = account.getBalance();
		percentChange = ((currentvalue - historicvalue) / historicvalue) * 100.0;
		percentChange = CommonFunctions.round(percentChange, 3);
		return percentChange + "";
	}
	
	/**
	 * This function must be called from an asynchronous thread!
	 * @param timevalue
	 * @param economy
	 * @return The percentage change in theoretical balance for the given account and timevalue in hours
	 */
	
	public synchronized HashMap<HyperAccount, String> getAccountPercentChangeAsString(String economy, int timevalue) {
		if (sr == null) return null;
		HashMap<HyperAccount, ArrayList<Double>> allValues = new HashMap<HyperAccount, ArrayList<Double>>();
		QueryResult result = sr.select("SELECT ACCOUNT, BALANCE FROM hyperconomy_history_accounts ORDER BY TIME DESC");
		while (result.next()) {
			HyperAccount account = em.getAccount(result.getString("ACCOUNT"));
			double balance = result.getDouble("BALANCE");
			if (!allValues.containsKey(account)) {
				ArrayList<Double> values = new ArrayList<Double>();
				values.add(balance);
				allValues.put(account, values);
			} else {
				ArrayList<Double> values = allValues.get(account);
				values.add(balance);
				allValues.put(account, values);
			}
		}
		result.close();
		
		ArrayList<HyperAccount> accounts =  em.getAccounts();
		HashMap<HyperAccount, String> relevantValues = new HashMap<HyperAccount, String>();
		for (HyperAccount account:accounts) {
			if (allValues.containsKey(account)) {
				ArrayList<Double> historicValues = allValues.get(account);
				if (historicValues.size() >= timevalue) {
					double historicValue = historicValues.get(timevalue - 1);
					double currentvalue = 0.0;
					currentvalue = account.getBalance();
					if (historicValue == 0.0) {
						relevantValues.put(account, "?");
						continue;
					}
					double percentChange = ((currentvalue - historicValue) / historicValue) * 100.0;
					percentChange = CommonFunctions.round(percentChange, 3);
					String stringValue = percentChange + "";
					relevantValues.put(account, stringValue);
				} else {
					relevantValues.put(account, "?");
				}
			} else {
				relevantValues.put(account, "?");
			}
		}
		return relevantValues;
	}

	public synchronized HashMap<HyperAccount, Double> getAccountPercentChange(String economy, int timevalue) {
		if (sr == null) return null;
		HashMap<HyperAccount, ArrayList<Double>> allValues = new HashMap<HyperAccount, ArrayList<Double>>();
		QueryResult result = sr.select("SELECT ACCOUNT, BALANCE FROM hyperconomy_history_accounts ORDER BY TIME DESC");
		while (result.next()) {
			HyperAccount account = em.getAccount(result.getString("ACCOUNT"));
			double balance = result.getDouble("BALANCE");
			if (!allValues.containsKey(account)) {
				ArrayList<Double> values = new ArrayList<Double>();
				values.add(balance);
				allValues.put(account, values);
			} else {
				ArrayList<Double> values = allValues.get(account);
				values.add(balance);
				allValues.put(account, values);
			}
		}
		result.close();
		
		ArrayList<HyperAccount> accounts =  em.getAccounts();
		HashMap<HyperAccount, Double> relevantValues = new HashMap<HyperAccount, Double>();
		for (HyperAccount account:accounts) {
			if (allValues.containsKey(account)) {
				ArrayList<Double> historicValues = allValues.get(account);
				if (historicValues.size() >= timevalue) {
					double historicValue = historicValues.get(timevalue - 1);
					double currentvalue = 0.0;
					currentvalue = account.getBalance();
					if (historicValue == 0.0) {
						relevantValues.put(account, 0.0);
						continue;
					}
					double percentChange = ((currentvalue - historicValue) / historicValue) * 100.0;
					percentChange = CommonFunctions.round(percentChange, 3);
					relevantValues.put(account, percentChange);
				} else {
					relevantValues.put(account, 0.0);
				}
			} else {
				relevantValues.put(account, 0.0);
			}
		}
		return relevantValues;
	}
	 
	
	
	public String formatSQLiteTime(int time) {
		if (time < 0) {
			return "-" + Math.abs(time);
		} else if (time > 0) {
			return "+" + time;
		} else {
			return "0";
		}
	}
  	
}
