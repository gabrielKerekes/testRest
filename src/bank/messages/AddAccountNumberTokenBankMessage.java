package bank.messages;

import java.sql.Timestamp;

public class AddAccountNumberTokenBankMessage extends BankMessage {
	private String token;
	
	public AddAccountNumberTokenBankMessage() { 
		
	}
	
	public AddAccountNumberTokenBankMessage(String accountNumber, Timestamp timestamp) {
		super(accountNumber, timestamp);
	}
	
	public String getToken() { return token; }
	public void setToken(String token) { this.token = token; }
}
