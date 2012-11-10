package regalowl.hyperconomy;

import org.bukkit.command.CommandSender;

public class Setstock {
	Setstock(String args[], CommandSender sender, String playerecon) {
		HyperConomy hc = HyperConomy.hc;
		SQLFunctions sf = hc.getSQLFunctions();
		InfoSign isign = hc.getInfoSign();
		LanguageFile L = hc.getLanguageFile();
		String name = "";
		try {
			if (args.length == 2) {
				name = args[0];
				int stock = Integer.parseInt(args[1]);
				String teststring = hc.testiString(name);
				if (teststring != null) {
					sf.setStock(name, playerecon, stock);
					//sender.sendMessage(ChatColor.GOLD + "" + name + " stock set!");
					sender.sendMessage(L.f(L.get("STOCK_SET"), name));
					isign.setrequestsignUpdate(true);
					isign.checksignUpdate();
				} else {
					sender.sendMessage(L.get("INVALID_ITEM_NAME"));
				}
			} else if (args.length == 3) {
				String ench = args[2];
				if (ench.equalsIgnoreCase("e")) {
					name = args[0];
					int stock = Integer.parseInt(args[1]);
					String teststring = hc.testeString(name);
					if (teststring != null) {
						sf.setStock(name, playerecon, stock);
						//sender.sendMessage(ChatColor.GOLD + "" + name + " stock set!");
						sender.sendMessage(L.f(L.get("STOCK_SET"), name));
						isign.setrequestsignUpdate(true);
						isign.checksignUpdate();
					} else {
						sender.sendMessage(L.get("INVALID_ENCHANTMENT_NAME"));
					}
				} else {
					sender.sendMessage(L.get("SETSTOCK_INVALID"));
				}
			} else {
				sender.sendMessage(L.get("SETSTOCK_INVALID"));
			}
		} catch (Exception e) {
			sender.sendMessage(L.get("SETSTOCK_INVALID"));
		}
	}
}