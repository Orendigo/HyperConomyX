package regalowl.hyperconomy.command;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import regalowl.hyperconomy.HyperConomy;
import regalowl.hyperconomy.account.HyperAccount;
import regalowl.hyperconomy.account.HyperBank;
import regalowl.hyperconomy.account.HyperPlayer;

public class Hctop extends BaseCommand implements HyperCommand {
	
	public Hctop(HyperConomy hc) {
		super(hc, false);
	}

	@Override
	public CommandData onCommand(CommandData data) {
		if (!validate(data)) return data;
		try {
			if (hc.getMC().useExternalEconomy()) {
				data.addResponse(L.get("ONLY_AVAILABLE_INTERNAL"));
				return data;
			}
			int pe;
			if (args.length == 1) {
				pe = Integer.parseInt(args[0]);
			} else if (args.length == 0) {
				pe = 1;
			} else {
				data.addResponse(L.get("HCTOP_INVALID"));
				return data;
			}                   
			ArrayList<HyperAccount> accounts = dm.getAccounts();
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
		return data;
	}
}
