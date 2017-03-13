package gcm.messages;

import java.sql.Timestamp;

import org.codehaus.jackson.annotate.JsonAutoDetect;

import gcm.GcmMessageType;

@JsonAutoDetect()
public class ConfirmIdentityGcmMessage extends GcmMessage {
	private String accountNumber;
	
	public ConfirmIdentityGcmMessage(String accountNumber, String title, String message, Timestamp timestamp) {
		super(title, message, timestamp);
		
		this.accountNumber = accountNumber;
	}

	@Override
	public void createData() {	
		super.createData();
		
		putData("messageType", Integer.toString(GcmMessageType.CONFIRM_IDENTITY.ordinal()));
		putData("accountNumber", accountNumber);
	}

	public String getAccountNumber() { return accountNumber; }
	public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
}
