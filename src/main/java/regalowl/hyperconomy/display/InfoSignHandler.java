package regalowl.hyperconomy.display;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;


import regalowl.simpledatalib.sql.QueryResult;
import regalowl.simpledatalib.sql.SQLRead;
import regalowl.hyperconomy.HyperConomy;
import regalowl.hyperconomy.account.HyperPlayer;
import regalowl.hyperconomy.event.HyperBankModificationEvent;
import regalowl.hyperconomy.event.HyperEvent;
import regalowl.hyperconomy.event.HyperEventListener;
import regalowl.hyperconomy.event.HyperPlayerModificationEvent;
import regalowl.hyperconomy.event.TradeObjectModificationEvent;
import regalowl.hyperconomy.event.TradeObjectModificationType;
import regalowl.hyperconomy.event.minecraft.HBlockBreakEvent;
import regalowl.hyperconomy.event.minecraft.HSignChangeEvent;
import regalowl.hyperconomy.minecraft.HLocation;
import regalowl.hyperconomy.minecraft.HSign;

public class InfoSignHandler implements HyperEventListener {

	private HyperConomy hc;
	private CopyOnWriteArrayList<InfoSign> infoSigns = new CopyOnWriteArrayList<InfoSign>();
	private AtomicInteger signCounter = new AtomicInteger();
	private AtomicBoolean updateActive = new AtomicBoolean();
	private AtomicBoolean repeatUpdate = new AtomicBoolean();

	private QueryResult dbData;
	
	private final long signUpdateInterval = 1L;

	public InfoSignHandler(HyperConomy hc) {
		this.hc = hc;
		updateActive.set(false);
		repeatUpdate.set(false);
		if (hc.getConf().getBoolean("enable-feature.info-signs")) {
			hc.getHyperEventHandler().registerListener(this);
			loadSigns();
		}
	}

	private void loadSigns() {
		signCounter.set(0);
		infoSigns.clear();
		new Thread(new Runnable() {
			public void run() {
				SQLRead sr = hc.getSQLRead();
				dbData = sr.select("SELECT * FROM hyperconomy_info_signs");
				hc.getMC().runTask(new Runnable() {
					public void run() {
						while (dbData.next()) {
							HLocation l = new HLocation(dbData.getString("WORLD"), dbData.getInt("X"),dbData.getInt("Y"),dbData.getInt("Z"));
							InfoSign is = null;
							String parameters[] = {dbData.getString("PARAMETER1"), dbData.getString("PARAMETER2"), dbData.getString("PARAMETER3")};
							SignObject object = SignObject.fromString(dbData.getString("TYPE"));
							switch(object) {
								case TRADEOBJECT:
									is = new TradeObjectInfoSign(hc, l, dbData.getString("ECONOMY"), dbData.getString("TYPE"), parameters);
									break;
								case ACCOUNT:
									is = new AccountInfoSign(hc, l, dbData.getString("ECONOMY"), dbData.getString("TYPE"), parameters);
									break;
								default:
									is = null;
							}
							if(is.isValid()) 
								infoSigns.add(is);
							else
								removeSign(is);
						}
						dbData.close();
						dbData = null;
						updateSigns();
					}
				});
			}
		}).start();
	}

	
	@Override
	public void handleHyperEvent(HyperEvent event) {
		if (event instanceof HBlockBreakEvent) {
			HBlockBreakEvent hevent = (HBlockBreakEvent)event;
			HLocation l = hevent.getBlock().getLocation();
			InfoSign is = getInfoSign(l);
			if (is == null) {return;}
			removeSign(is);
		} else if (event instanceof TradeObjectModificationEvent) {
			TradeObjectModificationEvent hevent = (TradeObjectModificationEvent)event;
			if(hevent.getTradeObjectModificationType() == TradeObjectModificationType.DELETED) {
				for(InfoSign is : infoSigns) {
					if(is instanceof TradeObjectInfoSign)
						if(((TradeObjectInfoSign)is).getTradeObject().getName().equals(hevent.getTradeObject().getName()))
							removeSign(is);			
				}
			}
			updateSigns();
		} else if (event instanceof HyperBankModificationEvent) {
			HyperBankModificationEvent hevent = (HyperBankModificationEvent)event;
			if(hevent.getHyperBank().deleted())
				for(InfoSign is : infoSigns)
					if(is instanceof AccountInfoSign)
						if(((AccountInfoSign)is).getAccount().getName().equals(hevent.getHyperBank().getName()))
							removeSign(is);
			updateSigns();
		} else if (event instanceof HyperPlayerModificationEvent) {
			HyperPlayerModificationEvent hevent = (HyperPlayerModificationEvent)event;
			if(hevent.getHyperPlayer().deleted())
				for(InfoSign is : infoSigns)
					if(is instanceof AccountInfoSign)
						if(((AccountInfoSign)is).getAccount().getName().equals(hevent.getHyperPlayer().getName()))
							removeSign(is);			
			updateSigns();
		} else if (event instanceof HSignChangeEvent) {
			HSignChangeEvent hevent = (HSignChangeEvent)event;
			try {
				HSign s = hevent.getSign();
				HyperPlayer hp = hevent.getHyperPlayer();
				if (hp.hasPermission("hyperconomy.createsign")) {
					String economy = "default";
					if (hp != null && hp.getEconomy() != null) {
						economy = hp.getEconomy();
					}
					SignObject object = SignObject.fromString(s.getLine(2));
					InfoSign is;
					if (object != null) {
						switch(object) {
							case TRADEOBJECT:
								is = TradeObjectInfoSign.fromHSign(hc, s.getLocation(), economy, s);
								break;
							case ACCOUNT:
								is = AccountInfoSign.fromHSign(hc, s.getLocation(), economy, s);
								break;
							case NONE:
								is = null;
							default:
								is = null;
						}
						if(is.isValid()) {
							infoSigns.add(is);
							HashMap<String,String> values = new HashMap<String,String>();
							values.put("WORLD", is.getLocation().getWorld());
							values.put("X", is.getLocation().getBlockX()+"");
							values.put("Y", is.getLocation().getBlockY()+"");
							values.put("Z", is.getLocation().getBlockZ()+"");
							values.put("TYPE", is.getType());
							values.put("PARAMETER1", (is.getParameter(0) == null) ? "" : is.getParameter(0));
							values.put("PARAMETER2", (is.getParameter(1) == null) ? "" : is.getParameter(1));
							values.put("PARAMETER3", (is.getParameter(2) == null) ? "" : is.getParameter(2));
							values.put("ECONOMY", is.getEconomy());
							hc.getSQLWrite().performInsert("hyperconomy_info_signs", values);
							updateSigns();
						}
					}
				}
			} catch (Exception e) {
				hc.gSDL().getErrorWriter().writeError(e);
			}
		}
	}
	
	public void removeSign(InfoSign is) {
		is.disableSign();
		infoSigns.remove(is);
		HLocation loc = is.getLocation();
		HashMap<String,String> conditions = new HashMap<String,String>();
		conditions.put("WORLD", loc.getWorld());
		conditions.put("X", loc.getBlockX()+"");
		conditions.put("Y", loc.getBlockY()+"");
		conditions.put("Z", loc.getBlockZ()+"");
		hc.getSQLWrite().performDelete("hyperconomy_info_signs", conditions);
	}
	

	public void updateSigns() {
		if (hc.getHyperLock().fullLock() || !hc.loaded()) {return;}
		if (updateActive.get()) {
			repeatUpdate.set(true);
			return;
		}
		updateActive.set(true);
		new SignUpdater();
	}
	
	private class SignUpdater {
		private long updateTaskId;
		private int currentSign;
		SignUpdater() {
			currentSign = 0;
			updateTaskId = hc.getMC().runRepeatingTask(new Runnable() {
				public void run() {
					if (currentSign >= infoSigns.size()) {
						if (repeatUpdate.get()) {
							currentSign = 0;
							repeatUpdate.set(false);
							if (infoSigns.isEmpty()) {
								hc.getMC().cancelTask(updateTaskId);
								updateActive.set(false);
								return;
							}
						} else {
							hc.getMC().cancelTask(updateTaskId);
							updateActive.set(false);
							return;
						}
					}
					InfoSign is = infoSigns.get(currentSign);
					if (is.getSign() != null) {
						is.update();
					} else {
						removeSign(is);
					}
					currentSign++;
				}
			}, signUpdateInterval, signUpdateInterval);
		}
	}
	
	
	public void reloadSigns() {
		loadSigns();
	}


	public ArrayList<InfoSign> getInfoSigns() {
		ArrayList<InfoSign> iSigns = new ArrayList<InfoSign>();
		for (InfoSign is : infoSigns) {
			iSigns.add(is);
		}
		return iSigns;
	}

	public InfoSign getInfoSign(HLocation l) {
		for (InfoSign isign : infoSigns) {
			if (isign == null) {continue;}
			if (l.equals(isign.getLocation())) return isign;
		}
		return null;
	}





}
