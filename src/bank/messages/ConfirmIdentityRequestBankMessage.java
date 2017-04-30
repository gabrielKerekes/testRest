package bank.messages;

import java.sql.Timestamp;

public class ConfirmIdentityRequestBankMessage extends BankMessage {
	private String guid;
	private String action;
	
	public ConfirmIdentityRequestBankMessage() {
		
	}
	
	public ConfirmIdentityRequestBankMessage(String accountNumber, Timestamp timestamp, String guid) {
		super(accountNumber, timestamp);
		
		this.guid = guid;
	}
	
	public String getGuid() { return guid; }
	public void setGuid(String guid) { this.guid = guid; }
	
	public String getAction() { return action; }
	public void setAction(String action) { this.action = action; }
}
