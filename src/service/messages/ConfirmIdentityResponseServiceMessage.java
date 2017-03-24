package service.messages;

import java.math.BigInteger;

import ocrahotp.Ocra;

public class ConfirmIdentityResponseServiceMessage extends ServiceMessage {
	private String accountNumber;

	public String getAccountNumber() { return accountNumber; }
	public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
	
	@Override
	public boolean checkOcra(String imei, String pin, String otp) {		
		try {
			String messageBytes = String.format("%040x", new BigInteger(1, (getTimestamp() + getAccountNumber()).getBytes()));
			
			String server_ocra = Ocra.generateOCRA(imei, pin, otp, messageBytes);

			if(getOcra().equals(server_ocra)) {
				return true;
			}

			return false;
		}
		catch(Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
