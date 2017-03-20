package bank.messages;

import java.sql.Timestamp;

public class ConfirmIdentityRequestBankMessage extends BankMessage {
	public ConfirmIdentityRequestBankMessage() {
		
	}
	
	public ConfirmIdentityRequestBankMessage(String accountNumber, Timestamp timestamp) {
		super(accountNumber, timestamp);
	}
}
