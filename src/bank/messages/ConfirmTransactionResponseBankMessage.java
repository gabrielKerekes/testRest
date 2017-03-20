package bank.messages;

import java.sql.Timestamp;

public class ConfirmTransactionResponseBankMessage extends BankMessage {
	// todo: GABO - rename
	private String answer;
	
	public ConfirmTransactionResponseBankMessage(String accountNumber, Timestamp timestamp, String answer) {
		super(accountNumber, timestamp);
		
		this.answer = answer;
	}

	public String getAnswer() { return answer; }
	public void setAnswer(String answer) { this.answer = answer; }
}
