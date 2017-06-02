package service;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

public class Utils {		
	public static String formatTransactionDate(Date date) {
		DateFormat dateformat = new SimpleDateFormat("yyyy:MM:dd_HH:mm:ss");
		return dateformat.format(date);
	}
	
	public static String buildTransactionDatabaseString(String username, String dateString, double amount, boolean isTransactionAccepted) {
		return username + "#" + dateString + "#" + amount + "#" + (isTransactionAccepted ? "1" : "0");
	}
}
