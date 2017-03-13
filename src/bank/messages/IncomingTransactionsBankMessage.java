package bank.messages;

import java.sql.Timestamp;

public class IncomingTransactionsBankMessage extends BankMessage {
	public IncomingTransactionsBankMessage(String accountNumber, Timestamp timestamp) {
		super(accountNumber, timestamp);
	}
}
