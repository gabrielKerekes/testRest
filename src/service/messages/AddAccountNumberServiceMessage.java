package service.messages;

public class AddAccountNumberServiceMessage extends ServiceMessage {
	private String accountNumber;
	private String token;

	public String getAccountNumber() { return accountNumber; }
	public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
	public String getToken() { return token; }
	public void setToken(String token) { this.token = token; }
	
	@Override
	public boolean checkOcra(String imei, String pin, String otp) {
		// TODO Auto-generated method stub
		return false;
	}
}
