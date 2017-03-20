package bank.messages;

import java.sql.Timestamp;

public class ConfirmIdentityResponseBankMessage extends BankMessage {
	// todo: GABO - rename
	private String answer;
	
	public ConfirmIdentityResponseBankMessage(String accountNumber, Timestamp timestamp, String answer) {
		super(accountNumber, timestamp);
	}

	public String getAnswer() { return answer; }
	public void setAnswer(String answer) { this.answer = answer; }
}
