package gcm.messages;

import java.sql.Timestamp;

import org.codehaus.jackson.annotate.JsonAutoDetect;

import gcm.GcmMessageType;

@JsonAutoDetect()
public class ConfirmIdentityGcmMessage extends GcmMessage {
	private String accountNumber;
	private String guid;
	private String action;
	
	public ConfirmIdentityGcmMessage(String accountNumber, Timestamp timestamp, String guid, String action) {
		super(timestamp);
		
		this.accountNumber = accountNumber;
		this.guid = guid;
		this.action = action;
	}

	@Override
	public void createData() {	
		super.createData();
		
		putData("messageType", Integer.toString(GcmMessageType.CONFIRM_IDENTITY.ordinal()));
		putData("accountNumber", accountNumber);
		putData("guid", guid);
	}

	public String getAccountNumber() { return accountNumber; }
	public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
	public String getGuid() { return guid; }
	public void setGuid(String guid) { this.guid = guid; }
	public String getAction() { return action; }
	public void setAction(String action) { this.action = action; }
}
