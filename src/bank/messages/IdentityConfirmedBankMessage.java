package bank.messages;

import java.sql.Timestamp;

public class IdentityConfirmedBankMessage extends BankMessage {

	public IdentityConfirmedBankMessage(String accountNumber, Timestamp timestamp) {
		super(accountNumber, timestamp);
	}

}
