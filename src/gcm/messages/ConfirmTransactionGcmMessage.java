package gcm.messages;

import java.sql.Timestamp;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;

import gcm.GcmMessageType;

@JsonAutoDetect()
public class ConfirmTransactionGcmMessage extends GcmMessage {
	// transient so it doesn't serialize
	private transient double amount;
	private transient String accountNumber;
	private transient String paymentId;

	public ConfirmTransactionGcmMessage(String accountNumber, String paymentId, Timestamp timestamp, double amount) {
		super(timestamp);
		
		this.setPaymentId(paymentId);
		this.setAmount(amount);
		this.setAccountNumber(accountNumber);
	}

	@Override
	public void createData() {	
		super.createData();
		
		putData("messageType", Integer.toString(GcmMessageType.CONFIRM_TRANSACTION.ordinal()));
		putData("amount", Double.toString(getAmount()));
		putData("accountNumber", getAccountNumber());
		putData("paymentId", getPaymentId());
	}

	public double getAmount() {	return amount; }
	public void setAmount(double amount) { this.amount = amount; }
	public String getAccountNumber() { return accountNumber; }
	public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
	public String getPaymentId() { return paymentId; }
	public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
}
