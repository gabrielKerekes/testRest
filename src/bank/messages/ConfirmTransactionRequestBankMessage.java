package bank.messages;

import java.sql.Timestamp;

public class ConfirmTransactionRequestBankMessage extends BankMessage {
	private double amount;
	
	public ConfirmTransactionRequestBankMessage(String accountNumber, Timestamp timestamp) {
		super(accountNumber, timestamp);

	}
	
	public double getAmount() {	return amount;	}
	public void setAmount(double amount) { this.amount = amount; }	
}
