package bank.messages;

import java.sql.Timestamp;

public class ConfirmIdentityResponseBankMessage extends BankMessage {
	private String answer;
	private String guid;
	private String action;
	
	public ConfirmIdentityResponseBankMessage() {
		
	}
	
	public ConfirmIdentityResponseBankMessage(String accountNumber, Timestamp timestamp, String answer, String guid, String action) {
		super(accountNumber, timestamp);
	}

	public String getAnswer() { return answer; }
	public void setAnswer(String answer) { this.answer = answer; }
	public String getGuid() { return guid; }
	public void setGuid() { this.guid = guid; }
	public String getAction() { return action; }
	public void setAction(String action) { this.action = action; }
}
