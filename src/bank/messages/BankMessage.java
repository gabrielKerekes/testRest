package bank.messages;

import java.sql.Timestamp;

public class BankMessage {
	private String accountNumber;
	private Timestamp timestamp;
	
	public BankMessage(String accountNumber, Timestamp timestamp) {
		this.accountNumber = accountNumber;
		this.timestamp = timestamp;
	}
	
	public String getAccountNumber() { return accountNumber; }	
	public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }	
	public Timestamp getTimestamp() { return timestamp; }
	public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp;	}
}
