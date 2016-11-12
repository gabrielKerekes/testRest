package me.robo.service;

public class RegData {

	private String uname;
	private String pwd;
	private String mail;
	private String pin;	
	private String[] grid_card;
	
	public void makeGrid(){
		RegistrationUtils utils = new RegistrationUtils();
	    utils.make_grid_card(pwd);
		grid_card = utils.getGrid_card();
	}
	
	
	public String getUname() {
		return uname;
	}

	public String getPwd() {
		return pwd;
	}
	
	public String getMail() {
		return mail;
	}

	public String getPin() {
		return pin;
	}

	public String[] getGrid_card() {
		return grid_card;
	}

	public void setUname(String uname) {
		this.uname = uname;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public void setPin(String pin) {
		this.pin = pin;
	}

	public void setGrid_card(String[] grid_card) {
		this.grid_card = grid_card;
	}	
}
