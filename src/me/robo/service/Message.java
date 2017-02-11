package me.robo.service;

import java.math.BigInteger;

import hotp.OcraGenerator;

public class Message {

	private String uname;
	private String msg;
	private String answer;
	private String ocra;
	private String counter;	
	private double amount;
	
	public String getCounter() {
		return counter;
	}
	public void setCounter(String counter) {
		this.counter = counter;
	}
	public String getUname() {
		return uname;
	}
	public String getMsg() {
		return msg;
	}
	public String getAnswer() {
		return answer;
	}
	public String getOcra() {
		return ocra;
	}
	public double getAmount() {
		return amount;
	}
	public void setUname(String uname) {
		this.uname = uname;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	public void setAnswer(String answer) {
		this.answer = answer;
	}
	public void setOcra(String ocra) {
		this.ocra = ocra;
	}
	public void setAmount(double amount) {
		this.amount = amount;
	}
	
	public boolean checkOcra(String imei, String pin){
		
		try{
			OCRAhotpGenerator ocra_gen = new OCRAhotpGenerator();
			msg = answer +":"+ msg;
			msg = String.format("%040x", new BigInteger(1, msg.getBytes(/*YOUR_CHARSET?*/)));

			String server_ocra = OcraGenerator.generateOCRA(imei + pin, msg);
		
			if(!ocra.equals(server_ocra)){
				answer = "err";
				return false;
			}else return true;
		}catch(Exception e){
			answer = "exc";
			return false;
		}
	}
	
}
