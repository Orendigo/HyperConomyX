package regalowl.hyperconomy.display;

public enum SignObject {
	TRADEOBJECT, ACCOUNT, TRADEOBJECTLIST, ACCOUNTLIST, TAXRATE, NONE;

	public static SignObject fromString(String type) {
		if (type == null) {
			return SignObject.NONE;
		}
		type = type.toUpperCase();
		if (type == null) {
			return null;
		} else if (type.matches("BUY|SELL|STOCK|TOTALSTOCK|VALUE|STATUS|STATICPRICE|STARTPRICE|MEDIAN|OBJCHANGE|PRODUCTTAX|SB")) {
			return SignObject.TRADEOBJECT;
		} else if (type.matches("BALANCE|ACTCHANGE")) {
			return SignObject.ACCOUNT;
		} else if (type.matches("TOPSTOCK|TOPTOTALSTOCK|TOPVALUE|TOPBUY|TOPSELL|TOPPRODUCTTAX|TOPMEDIAN|TOPSTATICPRICE|TOPSTARTPRICE|TOPOBJCHANGE")) {
			return SignObject.TRADEOBJECTLIST;
		} else if (type.matches("TOPBALANCE|TOPACTCHANGE")) {
			return SignObject.ACCOUNTLIST;
		} else if (type.matches("TAXRATE")) {
			return SignObject.TAXRATE;
		} else {
			return SignObject.NONE;
		}
	}
}
