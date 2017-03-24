package service;

public class MyResponse {
	public static class ResponseString {
		public static String EXPIRED = "EXPIRED";
		public static String OCRA_ERROR = "OCRA_ERROR";
		public static String ERROR = "ERROR";
		public static String ACCOUNT_NUMBER_ERROR = "ACCOUNT_NUMBER_ERROR";
		public static String SUCCESS = "SUCCESS";
		public static String EXCEPTION = "EXCEPTION";
		public static String TOKEN_COMBINATION_ERROR = "TOKEN_COMBINATION_ERROR";
	}
	
	private boolean success;
	private String message;
	private String exceptionMessage;
	
	public MyResponse(boolean success, String message) {
		this.success = success;
		this.message = message;
		
		exceptionMessage = "";
	}
	
	public MyResponse(boolean success, String message, String exceptionMessaeg) {
		this.success = success;
		this.message = message;
		
		this.exceptionMessage = exceptionMessaeg;
	}
	
	public boolean isSuccess() { return success; }
	public void setSuccess(boolean success) { this.success = success; }
	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }
	public String getExceptionMessage() { return exceptionMessage; }
	public void setExceptionMessage(String exceptionMessage) { this.exceptionMessage = exceptionMessage; }
}
