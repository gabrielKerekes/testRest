package me.robo.service;

public class Item {

	private String transText;
	private String datetime;
	private boolean state;
	private String from;
	
	Item(String trans){
		String[] splitted = trans.split("#");
		
		transText = splitted[0];
		datetime = splitted[1];
		from = splitted[2];
		
		if (splitted[3].equals("1")) state = true;
		else state = false;
			
	}

	public String getTransText() {
		return transText;
	}

	public void setTransText(String transText) {
		this.transText = transText;
	}

	public String getDatetime() {
		return datetime;
	}

	public void setDatetime(String datetime) {
		this.datetime = datetime;
	}

	public boolean isState() {
		return state;
	}

	public void setState(boolean state) {
		this.state = state;
	}

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}
	
	
	
}
