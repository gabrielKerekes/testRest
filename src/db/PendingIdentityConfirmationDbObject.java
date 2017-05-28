package db;

import java.sql.Timestamp;

public class PendingIdentityConfirmationDbObject {
	private String token;
	private String accountNumber;
	private Timestamp timestamp;
	private String guid;
	private String action;
	
	public PendingIdentityConfirmationDbObject(String token, String accountNumber, Timestamp timestamp, String guid, String action) {
		this.token = token;
		this.accountNumber = accountNumber;
		this.timestamp = timestamp;
		this.guid = guid;
		this.action = action;
	}
	
	public String getToken() { return token; }
	public void setToken(String token) { this.token = token; }
	
	public String getAccountNumber() { return accountNumber; }
	public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
	
	public Timestamp getTimestamp() { return timestamp; }
	public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
	
	public String getGuid() { return guid; }
	public void setGuid(String guid) { this.guid = guid; }
	
	public String getAction() { return action; }
	public void setAction(String action) { this.action = action; }
}
