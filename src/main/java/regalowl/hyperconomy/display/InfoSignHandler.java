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
import regalowl.hyperconomy.event.minecraft.HPlayerItemHeldEvent;
import regalowl.hyperconomy.event.minecraft.HSignChangeEvent;
import regalowl.hyperconomy.minecraft.HLocation;
import regalowl.hyperconomy.minecraft.HSign;

public class InfoSignHandler implements HyperEventListener {

	private HyperConomy hc;
	private CopyOnWriteArrayList<InfoSign> infoSigns = new CopyOnWriteArrayList<InfoSign>();
	private ArrayList<HLocation> signLocations = new ArrayList<HLocation>();
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
							String parameters[] = {dbData.getString("PARAMETER1"), dbData.getString("PARAMETER2"), dbData.getString("PARAMETER3")};
							InfoSign is = getInfoSignFromData(hc, l, dbData.getString("ECONOMY"), dbData.getString("TYPE"), parameters);
							if(is.isValid()) {
								infoSigns.add(is);
								signLocations.add(is.getLocation());
							} else {
								removeSign(is);
							}
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
			if(!hevent.getPlayer().hasPermission("hyperconomy.createsign"))
				hevent.cancel();
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
		} else if (event instanceof HPlayerItemHeldEvent) {
			HPlayerItemHeldEvent hevent = (HPlayerItemHeldEvent)event;
			try {
				HyperPlayer hp = hevent.getHyperPlayer();
				if (hc.getHyperLock().loadLock()) return;
				if (!hp.hasPermission("hyperconomy.usesign")) return;
				HLocation target = hp.getTargetLocation();
				InteractiveInfoSign iis = null;
				if(isInfoSign(target)) {
					for(InfoSign is : infoSigns) {
						if(is instanceof InteractiveInfoSign &&  is.getLocation().equals(target)) {
							iis = (InteractiveInfoSign)is;
							break;
						}
					}
					if(iis != null) {
						int ps = hevent.getPreviousSlot();
						int ns = hevent.getNewSlot();
						int change = 0;
						if(ns == 0 && ps == 8)
							change = 1;
						else if(ns == 8 && ps == 0)
							change = -1;
						else if(ns > ps)
							change = 1;
						else if(ns < ps)
							change = -1;
						if(change > 0)
							iis.incrementIndex(change);
						else if(change < 0)
							iis.decrementIndex(change*-1);
					}
				}
			} catch (Exception e) {
				hc.gSDL().getErrorWriter().writeError(e);
			}
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
					String parameters[] = {s.getLine(1), s.getLine(2), s.getLine(3)};
					InfoSign is = getInfoSignFromData(hc, s.getLocation(), economy, s.getLine(0), parameters);
					if(is != null && is.isValid()) {
						infoSigns.add(is);
						signLocations.add(is.getLocation());
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
			} catch (Exception e) {
				hc.gSDL().getErrorWriter().writeError(e);
			}
		}
	}

	private InfoSign getInfoSignFromData(HyperConomy hc, HLocation loc, String economy, String type, String[] parameters) {
		SignObject object = SignObject.fromString(type);
		switch(object) {
			case TRADEOBJECT:
				return new TradeObjectInfoSign(hc, loc, economy, type, parameters);
			case ACCOUNT:
				return new AccountInfoSign(hc, loc, economy, type, parameters);
			case TRADEOBJECTLIST:
				return new TradeObjectListInfoSign(hc, loc, economy, type, parameters);
			case ACCOUNTLIST:
				return new AccountListInfoSign(hc, loc, economy, type, parameters);
			default:
				return null;
		}
		
	}
	
	public void removeSign(InfoSign is) {
		is.disableSign();
		infoSigns.remove(is);
		HLocation loc = is.getLocation();
		signLocations.remove(loc);
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

	public boolean isInfoSign(HLocation hloc) {
		return signLocations.contains(hloc);
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
