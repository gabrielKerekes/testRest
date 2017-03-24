package service.messages;

import java.sql.Timestamp;

import org.codehaus.jackson.map.annotate.JsonDeserialize;

// todo: GABO - mozno premenovat na PhoneMessage, alebo daco take, aby to davalo zmysel
// 		s tym, ze banka ma svoju message a tak .. 
public abstract class ServiceMessage {
	private String username;
	private String message;
	private String answer;
	private String ocra;
	
	@JsonDeserialize(using = DateDeserializer.class)
    private Timestamp timestamp;
		
	public String getUsername() { return username; }	
	public String getMessage() { return message; }
	public String getAnswer() { return answer; }	
	public String getOcra() { return ocra; }	
	public void setUsername(String username) { this.username = username; }
	public void setMessage(String message) { this.message = message; }
	public void setAnswer(String answer) { this.answer = answer; }
	public void setOcra(String ocra) { this.ocra = ocra; }	
	public Timestamp getTimestamp() { return timestamp; }
	public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
	
	public abstract boolean checkOcra(String imei, String pin, String otp);
}
