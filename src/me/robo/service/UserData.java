package me.robo.service;

public class UserData {
	
	private String uname;
	private String pwd;
	private String grid;
	private String grid_num;
	private String otp;
	
	public String getUname() {
		return uname;
	}
	public String getPwd() {
		return pwd;
	}
	public String getGrid() {
		return grid;
	}
	public String getGrid_num() {
		return grid_num;
	}
	public String getOtp() {
		return otp;
	}
	public void setUname(String uname) {
		this.uname = uname;
	}
	public void setPwd(String pwd) {
		this.pwd = pwd;
	}
	public void setGrid(String grid) {
		this.grid = grid;
	}
	public void setGrid_num(String grid_num) {
		this.grid_num = grid_num;
	}
	public void setOtp(String otp) {
		this.otp = otp;
	}
	
	/*public UserData(String data){
		parseAndSetData(data);		
	}
	
	private void parseAndSetData(String data){
		
		String[] splittedData;
		
		splittedData = data.split("-");
		
		uname = splittedData[0];
		pwd = splittedData[1];
		grid = splittedData[2];
		grid_num = splittedData[3];
		otp = splittedData[4];
	}*/

	
}
