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
			
	public boolean checkTrans(LDAP database){

		int counter = 0;
		DateFormat dateformat = new SimpleDateFormat("yyyy:MM:dd_HH:mm:ss");
		Calendar cal = new GregorianCalendar();
		cal.add(Calendar.DAY_OF_MONTH, -7);
    	Date date = cal.getTime();
    	String acctualdatetime = dateformat.format(date);
		
		String[] trans = database.get_trans();
		
		ArrayList<Item> items = new ArrayList<Item>();
		ArrayList<Item> newitems = new ArrayList<Item>();
    	
    	for (String string : trans) {
			items.add(new Item(string));
		}
    	
    	Collections.sort(items,new ComparatorTrans());
    	
    	for (Item item : items) {
			if(counter<10){
				if(dateTimeCheck(item.getDatetime(),acctualdatetime)){
					newitems.add(item);
					counter++;
				}
				else break;
			}
			else break;
		}
    	
    	return rewriteDatabase(buildDatabaseData(newitems),database);
    	
	}
	
	private String[] buildDatabaseData(ArrayList<Item> items){
	
		String[] data = new String[items.size()];
		int i = 0;
		
		for (Item item : items) {
			data[i] = item.getTransText()+"#"+item.getDatetime()+"#"+item.getFrom()+"#";
			if(item.isState()) data[i] += "1";
			else data[i] += "0";
			i++;
		}
		
		return data;		
	}
	
	private boolean rewriteDatabase(String[] items, LDAP database){
		return database.modify_array_attribute("carLicense", items);
	}
	
	private boolean dateTimeCheck(String itemDT, String acctualDT){
		
		boolean result = false;		

		DateFormat dateformat = new SimpleDateFormat("yyyy:MM:dd_HH:mm:ss");
		
		try {
			Date itemDate = dateformat.parse(itemDT);
			Date acctualDate = dateformat.parse(acctualDT);
			
			if(itemDate.compareTo(acctualDate)>0) result = true;
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		return result;
	}
	
	public static String formatTransactionDate(Date date) {
		DateFormat dateformat = new SimpleDateFormat("yyyy:MM:dd_HH:mm:ss");
		return dateformat.format(date);
	}
	
	public static String buildTransactionDatabaseString(String username, String dateString, double amount, boolean isTransactionAccepted) {
		return username + "#" + dateString + "#" + amount + "#" + (isTransactionAccepted ? "1" : "0");
	}
	
	private class ComparatorTrans implements Comparator<Item>{

		@Override
		public int compare(Item o1, Item o2) {
			return o2.getDatetime().compareTo(o1.getDatetime());
		}

	}
}