package bank.messages;

import java.sql.Timestamp;

public class ConfirmTransactionRequestBankMessage extends BankMessage {
	private String paymentId;
	private double amount;
	
	public ConfirmTransactionRequestBankMessage() {
		
	}
	
	public ConfirmTransactionRequestBankMessage(String accountNumber, Timestamp timestamp) {
		super(accountNumber, timestamp);
	}

	public String getPaymentId() { return paymentId; }
	public void setPaymentId(String paymentId) { this.paymentId = paymentId; }	
	public double getAmount() {	return amount;	}
	public void setAmount(double amount) { this.amount = amount; }
}
