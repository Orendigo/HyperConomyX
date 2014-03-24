package regalowl.hyperconomy.command;


import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import regalowl.hyperconomy.DataManager;
import regalowl.hyperconomy.HyperConomy;
import regalowl.hyperconomy.HyperEconomy;
import regalowl.hyperconomy.account.HyperPlayer;
import regalowl.hyperconomy.util.LanguageFile;




public class Hcdelete implements CommandExecutor {
	
	
	
	public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
		HyperConomy hc = HyperConomy.hc;
		if (hc.getHyperLock().isLocked(sender)) {
			hc.getHyperLock().sendLockMessage(sender);;
			return true;
		}
		DataManager em = hc.getDataManager();
		LanguageFile L = hc.getLanguageFile();
		try {
			String economy = hc.getConsoleSettings().getEconomy(sender);
			HyperEconomy he = em.getEconomy(economy);
			if (args.length == 0) {
				sender.sendMessage(L.get("HCDELETE_INVALID"));
				return true;
			}
			if (args[0].equalsIgnoreCase("object")) {
				try {
					if (args.length == 2) {
						String name = args[1];
						if (he.objectTest(name)) {
							he.getHyperObject(name).delete();
							sender.sendMessage(L.get("HCDELETE_SUCCESS"));
						} else {
							sender.sendMessage(L.get("INVALID_NAME"));
						}
					} else {
						sender.sendMessage(L.get("HCDELETE_OBJECT_INVALID"));
					}
				} catch (Exception e) {
					sender.sendMessage(L.get("HCDELETE_OBJECT_INVALID"));
				}
			} else if (args[0].equalsIgnoreCase("account")) {
				try {
					if (args.length == 2) {
						String name = args[1];
						if (em.hyperPlayerExists(name)) {
							HyperPlayer hp = em.getHyperPlayer(name);
							hp.delete();
							sender.sendMessage(L.get("HCDELETE_SUCCESS"));
						} else {
							sender.sendMessage(L.get("ACCOUNT_NOT_FOUND"));
						}
					} else {
						sender.sendMessage(L.get("HCDELETE_ACCOUNT_INVALID"));
					}
				} catch (Exception e) {
					sender.sendMessage(L.get("HCDELETE_ACCOUNT_INVALID"));
				}
			} else {
				//TODO
			}
			
		} catch (Exception e) {
			sender.sendMessage(L.get("HCDELETE_INVALID"));
		}
		return true;
	}
	
	

}