package regalowl.hyperconomy.display;


import regalowl.hyperconomy.HyperConomy;
import regalowl.hyperconomy.account.HyperAccount;
import regalowl.hyperconomy.minecraft.HLocation;
import regalowl.hyperconomy.minecraft.HSign;
import regalowl.hyperconomy.util.LanguageFile;

public class AccountInfoSign extends InfoSign {
    private HyperAccount account;
    private LanguageFile L;

    private int timeValueHours;
	private int timeValue;
	private String timeIncrement;
    
    public AccountInfoSign(HyperConomy hc, HLocation loc, String economy, String type, String[] parameters) {
        super(hc,loc,economy,type,parameters);
        L = hc.getLanguageFile();
        account = hc.getDataManager().getAccount(parameters[0]+parameters[1]);
        HSign s = getSign();
        if(s == null || account == null) {
            valid = false;
            return;
        } else {
            valid = true;
        }
        parameters[0] = account.getName();
        if(type.equals("ACTCHANGE")) {
            try {
                timeIncrement = parameters[2].substring(parameters[2].length()-1).toUpperCase();
                timeValue = Integer.parseInt(parameters[2].substring(0,parameters[2].length()-1));
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
                    timeIncrement = "D";
                    timeValue = 1;
                    timeValueHours = 24;
                }
            } catch (Exception e) {
                timeIncrement = "D";
                timeValue = 1;
                timeValueHours = 24;
            }
            parameters[2] = timeValue+timeIncrement;
        }
        if(s.getLine(0).equalsIgnoreCase(type)) {
            lines[0] = hc.getMC().removeColor(s.getLine(1).trim());
            lines[1] = hc.getMC().removeColor(s.getLine(2).trim());
            if (lines[0].length() > 13) {
                lines[0] = "&1" + lines[0].substring(0, 13);
                lines[1] = "&1" + lines[0].substring(13, lines[0].length()) + lines[1];
            } else {
                lines[0] = "&1" + lines[0];
                lines[1] = "&1" + lines[1];
            }
        }
    }

    @Override
    public void update() {
        if(type.equalsIgnoreCase("ACTCHANGE")) {
            updateHistorySign();
        } else {
            lines[2] = "&f" + "Balance:";
            double balance = account.getBalance();
            if(balance < 0.0)
                lines[3] = "&c" + "" + L.formatMoney(balance);
            else
                lines[3] = "&a" + "" + L.formatMoney(balance);
            updateHSign(); 
        }
    }

    private void updateHistorySign() {
		try {
			new Thread(new Runnable() {
				public void run() {
					String percentchange = hc.getHistory().getAccountPercentChange(account, timeValueHours);
                    lines[2] = "&f" + "Change:";
                    if(percentchange.equalsIgnoreCase("?")) {
                        lines[3] = "&f" + "" + timeValue + timeIncrement.toLowerCase() + "&1(" + percentchange + "%)";
                    } else {
                        Double percentc = Double.parseDouble(percentchange);
                        if(percentc < 0.0)
                            lines[3] = "&f" + timeValue + timeIncrement.toLowerCase() + "&c(" + percentchange + "%)";
                        else if (percentc > 0)
                            lines[3] = "&f" + timeValue + timeIncrement.toLowerCase() + "&a(+" + percentchange + "%)";
                        else
                            lines[3] = "&f" + timeValue + timeIncrement.toLowerCase() + "&1(" + percentchange + "%)";
                    }
					if (lines[2].length() > 14) {
						lines[2] = lines[2].substring(0, 13) + ")";
					}
					hc.getMC().runTask(new Runnable() {
						public void run() {
							HSign s = getSign();
							if (s != null) {
								updateHSign();
							}
						}
					});
				}
			}).start();
		} catch (Exception e) {
			hc.gSDL().getErrorWriter().writeError(e);
		}
	}

    public HyperAccount getAccount() {
        return account;
    }
}