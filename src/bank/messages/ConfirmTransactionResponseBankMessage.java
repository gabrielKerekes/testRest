package bank.messages;

import java.sql.Timestamp;

public class ConfirmTransactionResponseBankMessage extends BankMessage {
	public static class StatusString {
		public static String EXPIRED = "EXPIRED";
		public static String CONFIRMED = "CONFIRMED";
		public static String REJECTED = "REJECTED";
		public static String ERROR = "ERROR";
	}
	
	private String paymentId;
	private String status;
	
	public ConfirmTransactionResponseBankMessage() {
		
	}
	
	public ConfirmTransactionResponseBankMessage(String paymentId, Timestamp timestamp, String status) {
		super("", timestamp);
		
		this.paymentId = paymentId;
		this.status = status;
	}

	public String getStatus() { return status; }
	public void setStatus(String status) { this.status = status; }
	public String getPaymentId() { return paymentId; }
	public void setPaymentId(String status) { this.paymentId = paymentId; }
}
