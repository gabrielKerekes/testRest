package service.messages;

import java.math.BigInteger;
import ocrahotp.Ocra;

public class ConfirmTransactionServiceMessage extends ServiceMessage {
	private double amount;
	
	public double getAmount() {
		return amount;
	}
	
	public void setAmount(double amount) {
		this.amount = amount;
	}
	
	@Override
	public boolean checkOcra(String imei, String pin, String otp) {		
		try {			
			String messageAndAnswer = getAnswer() +":"+ getMessage();
			String byteMessage = String.format("%040x", new BigInteger(1, messageAndAnswer.getBytes()));
			
			String server_ocra = Ocra.generateOCRA(imei, pin, otp, byteMessage);

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
