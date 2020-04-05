package regalowl.hyperconomy.bukkit;

import java.util.List;

import org.bukkit.Bukkit;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.milkbowl.vault.economy.EconomyResponse.ResponseType;
import regalowl.hyperconomy.api.HEconomyProvider;

public class BukkitEconomy implements HEconomyProvider {

	private Economy e;
	
	public BukkitEconomy(Economy e) {
		this.e = e;
	}

	@Override
	public void createAccount(String name) {
		if (name == null || name.equals("")) return;
		e.createPlayerAccount(Bukkit.getPlayer(name));
	}

	@Override
	public boolean hasAccount(String name) {
		if (name == null || name.equals("")) return false;
		return e.hasAccount(Bukkit.getPlayer(name));
	}

	@Override
	public double getAccountBalance(String accountName) {
		if (accountName == null || accountName.equals("")) return 0;
		return e.getBalance(Bukkit.getPlayer(accountName));
	}

	@Override
	public boolean accountHasBalance(String accountName, double amount) {
		return (e.getBalance(Bukkit.getPlayer(accountName)) >= amount) ? true:false;
	}

	@Override
	public void setAccountBalance(String accountName, double balance) {
		if (accountName == null || accountName.equals("")) return;
		e.withdrawPlayer(Bukkit.getPlayer(accountName), e.getBalance(Bukkit.getPlayer(accountName)));
		e.depositPlayer(Bukkit.getPlayer(accountName), balance);
	}

	@Override
	public void withdrawAccount(String accountName, double amount) {
		if (accountName == null || accountName.equals("")) return;
		e.withdrawPlayer(Bukkit.getPlayer(accountName), amount);
	}

	@Override
	public void depositAccount(String accountName, double amount) {
		if (accountName == null || accountName.equals("")) return;
		e.depositPlayer(Bukkit.getPlayer(accountName), amount);
	}

	@Override
	public void deleteAccount(String accountName) {
		//not possible
	}

	@Override
	public void createBank(String bankName, String ownerName) {
		if (bankName == null || bankName.equals("")) return;
		if (ownerName == null || ownerName.equals("")) return;
		e.createBank(bankName, Bukkit.getPlayer(ownerName));
	}

	@Override
	public boolean hasBank(String bankName) {
		if (bankName == null || bankName.equals("")) return false;
		EconomyResponse response = e.bankBalance(bankName);
		return (response.type.equals(ResponseType.SUCCESS)) ? true:false;
	}

	@Override
	public double getBankBalance(String bankName) {
		if (bankName == null || bankName.equals("")) return 0;
		EconomyResponse response = e.bankBalance(bankName);
		if (response.type.equals(ResponseType.SUCCESS)) {
			return response.balance;
		} else {
			return 0;
		}
	}

	@Override
	public boolean bankHasBalance(String bankName, double amount) {
		if (bankName == null || bankName.equals("")) return false;
		return (getBankBalance(bankName) >= amount) ? true:false;
	}

	@Override
	public void setBankBalance(String bankName, double balance) {
		if (!hasBank(bankName)) return;
		withdrawBank(bankName, getBankBalance(bankName));
		depositBank(bankName, balance);
	}

	@Override
	public void withdrawBank(String bankName, double amount) {
		if (!hasBank(bankName)) return;
		e.bankWithdraw(bankName, amount);
	}

	@Override
	public void depositBank(String bankName, double amount) {
		if (!hasBank(bankName)) return;
		e.bankDeposit(bankName, amount);
	}

	@Override
	public void deleteBank(String name) {
		if (name == null || name.equals("")) return;
		e.deleteBank(name);
	}

	@Override
	public boolean isBankOwner(String bankName, String playerName) {
		if (bankName == null || bankName.equals("")) return false;
		if (playerName == null || playerName.equals("")) return false;
		EconomyResponse response = e.isBankOwner(bankName, Bukkit.getPlayer(playerName));
		return (ResponseType.SUCCESS == response.type) ? true:false;
	}

	@Override
	public boolean isBankMember(String bankName, String playerName) {
		if (bankName == null || bankName.equals("")) return false;
		if (playerName == null || playerName.equals("")) return false;
		EconomyResponse response = e.isBankMember(bankName, Bukkit.getPlayer(playerName));
		return (ResponseType.SUCCESS == response.type) ? true:false;
	}

	@Override
	public List<String> getBanks() {
		return e.getBanks();
	}

	@Override
	public boolean hasBankSupport() {
		return e.hasBankSupport();
	}

	@Override
	public String getEconomyName() {
		return e.getName();
	}

	@Override
	public boolean isEnabled() {
		return e.isEnabled();
	}

	@Override
	public int fractionalDigits() {
		return e.fractionalDigits();
	}

	@Override
	public String getAmountAsString(double amount) {
		return e.format(amount);
	}

	@Override
	public String currencyNameSingular() {
		return e.currencyNameSingular();
	}

	@Override
	public String currencyNamePlural() {
		return e.currencyNamePlural();
	}

	
	
}
