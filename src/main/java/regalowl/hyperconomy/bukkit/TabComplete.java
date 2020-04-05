package regalowl.hyperconomy.bukkit;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.util.StringUtil;

import regalowl.hyperconomy.HyperConomy;
import regalowl.hyperconomy.account.HyperAccount;
import regalowl.hyperconomy.tradeobject.TradeObject;

class ObjectTabComplete implements TabCompleter {

    private HyperConomy hc;

    ObjectTabComplete(HyperConomy hc) {
        this.hc = hc;
    }


    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String alias, String[] args) {
        ArrayList<TradeObject> tradeObjects = hc.getDataManager().getTradeObjects();
        ArrayList<String> objects = new ArrayList<String>();
        for(TradeObject to : tradeObjects) {
            objects.add(to.getName());
        }  
        return (args.length == 1) ? StringUtil.copyPartialMatches(args[0], objects, new ArrayList<String>()) : null;
    }
}

class AccountTabComplete implements TabCompleter {

    private HyperConomy hc;

    AccountTabComplete(HyperConomy hc) {
        this.hc = hc;
    }


    @Override
    public List<String> onTabComplete(CommandSender commandSender, Command command, String alias, String[] args) {
        ArrayList<HyperAccount> accounts = hc.getDataManager().getAccounts();
        ArrayList<String> objects = new ArrayList<String>();
        for(HyperAccount ac : accounts) {
            objects.add(ac.getName());
        }  
        return (args.length == 1) ? StringUtil.copyPartialMatches(args[0], objects, new ArrayList<String>()) : null;
    }
}