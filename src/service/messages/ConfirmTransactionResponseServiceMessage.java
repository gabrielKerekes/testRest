package service.messages;

import java.math.BigInteger;
import ocrahotp.Ocra;

public class ConfirmTransactionResponseServiceMessage extends ServiceMessage {
	private double amount;
	private String accountNumber;
	private String paymentId;
	
	public double getAmount() {	return amount;	}	
	public void setAmount(double amount) { this.amount = amount; }
	public String getAccountNumber() { return accountNumber; }
	public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
	public String getPaymentId() { return paymentId; }
	public void setPaymentId(String paymentId) { this.paymentId = paymentId; }
	
	@Override
	public boolean checkOcra(String imei, String pin, String otp) {		
		try {			
			String stringForOcra = getPaymentId() + getTimestamp();
			String hexStringForOcra = String.format("%040x", new BigInteger(1, stringForOcra.getBytes()));
			
			String serverOcra = Ocra.generateOCRA(imei, pin, otp, hexStringForOcra);

			if(getOcra().equals(serverOcra)) {
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
