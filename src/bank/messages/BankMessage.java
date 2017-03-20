package bank.messages;

import java.sql.Timestamp;

import org.codehaus.jackson.map.annotate.JsonDeserialize;

import service.messages.DateDeserializer;

public class BankMessage {
	private String accountNumber;
	@JsonDeserialize(using = DateDeserializer.class)
	private Timestamp timestamp;
	
	public BankMessage() { }
	
	public BankMessage(String accountNumber, Timestamp timestamp) {
		this.accountNumber = accountNumber;
		this.timestamp = timestamp;
	}
	
	public String getAccountNumber() { return accountNumber; }	
	public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }	
	public Timestamp getTimestamp() { return timestamp; }
	public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp;	}
}
