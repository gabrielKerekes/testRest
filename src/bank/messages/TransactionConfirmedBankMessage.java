package bank.messages;

import java.sql.Timestamp;

public class TransactionConfirmedBankMessage extends BankMessage {
	public TransactionConfirmedBankMessage(String accountNumber, Timestamp timestamp) {
		super(accountNumber, timestamp);
	}
}
