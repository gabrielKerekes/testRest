package service;


import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import ocrahotp.OtpGenerator;
import service.LDAP;
import service.messages.AddAccountNumberServiceMessage;
import service.messages.ConfirmIdentityResponseServiceMessage;
import service.messages.ConfirmTransactionResponseServiceMessage;
import service.messages.ServiceMessage;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.DELETE;
import javax.ws.rs.Produces;
import javax.ws.rs.FormParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import bank.messages.AddAccountNumberTokenBankMessage;
import bank.messages.BankMessage;
import bank.messages.ConfirmTransactionRequestBankMessage;
import bank.messages.ConfirmIdentityResponseBankMessage;
import bank.messages.ConfirmIdentityRequestBankMessage;
import bank.messages.ConfirmTransactionResponseBankMessage;
import bank.messages.ConfirmTransactionResponseBankMessage.StatusString;
import client.BankClient;
import db.MysqlDb;
import gcm.Gcm;
import gcm.GcmContent;
import gcm.GcmMessageType;
import gcm.messages.ConfirmIdentityGcmMessage;
import gcm.messages.ConfirmTransactionGcmMessage;


@Path("/service")
public class SimpleRestService {
	private long accountNumberAuthentificationPeriod = 300000; // 300 000 ms = 5 minutes
	private long confirmIdentityExpirationPeriod = 300000; // 300 000 ms = 5 minutes
	private long confirmTransactionExpirationPeriod = 300000; // 300 000 ms = 5 minutes
	
	private String loginStat = new String();
	private String regStat = new String();
	// todo: GABO - prerobit na databazu..
//	private static Map messages = new HashMap<String, ConfirmTransactionResponseServiceMessage>();
//	private static Map messages_reg = new HashMap<String, String>();
//	private static Map blocked_messages = new HashMap<String, String>();
//	private static Map req_message = new HashMap<String, String>();
//	private static Map pendingTransactionsFromBank = new HashMap<String, String>();
	// todo: GABO - implement blocked
//	private static Map blockedPendingTransactionsFromBank = new HashMap<String, String>();
//	private static Map pendingIdentityConfirmations = new HashMap<String, String>();
	
	private static List<String> pendingTransactions = new ArrayList<String>();
	
	@GET
	@Path("/getBankTest")
	@Produces(MediaType.TEXT_PLAIN)
	public String getBankTest() {
		BankMessage bankMessage = new ConfirmIdentityResponseBankMessage();
		int responseCode = BankClient.executePost("getTest", bankMessage);
		
		return responseCode +"";
	}
	
	@GET
	@Path("/getTest")
	@Produces(MediaType.TEXT_PLAIN)
	public String getTest() {		
		return "Tadadaaa!!";
	}
	
	@GET
	@Path("getLdapTest")
	@Produces(MediaType.TEXT_PLAIN)
	public String getLdapTest(@QueryParam("accountNumber") String accountNumber) {
		String username = "nestel84";
		
		LDAP ldap = new LDAP(username);		
		
		//return ldap.addAccountNumber(accountNumber) ? "success" : "failure";
		
		return ldap.getAccountNumberUsername(accountNumber);
	}
		
	@GET
	@Path("getDatabaseTest")
	@Produces(MediaType.TEXT_PLAIN)
	public String getDatabaseTest() {
		String accountNumber = "8888";

		String response = "";
		
		MysqlDb database = new MysqlDb();
		if (database.setAccountNumberAuthenticated(accountNumber, new Timestamp(new Date().getTime()))) {
			response += "set success ";
		}
		else {
			response += "set failure ";
		}
		
		response += " | ";
		
		if (database.isAccountNumberAuthenticated(accountNumber, new Timestamp(new Date().getTime()), 300000)) {
			response += " is authenticated";
		}
		else {
			response += " is not authenticated";
		}
		
		response += " | ";
		
		if (database.isAccountNumberAuthenticated(accountNumber, new Timestamp(new Date().getTime() + 800000), 300000)) {
			response += " is authenticated";
		}
		else {
			response += " is not authenticated";
		}		
		
		return response;
	}

	@POST
	@Path("/confirmIdentityRequest")
    @Produces(MediaType.APPLICATION_JSON)
	public MyResponse incomingTransactions(ConfirmIdentityRequestBankMessage message) {
		MyResponse response = new MyResponse(true, MyResponse.ResponseString.SUCCESS);
		
		try {
			System.out.println("CONFIRM IDENTITY REQUEST - " + message.getAccountNumber() + " " + message.getTimestamp() + " " + message.getGuid());

			MysqlDb database = new MysqlDb();

			if (database.isAccountNumberAuthenticated(message.getAccountNumber(), message.getTimestamp(), accountNumberAuthentificationPeriod)) {
				System.out.println("AccountNumber " + message.getAccountNumber() + " already authenticated.");
				sendIdentityConfirmedMessageToBank(message.getAccountNumber(), message.getTimestamp(), "ano", message.getGuid(), message.getAction());
				return response;
			}
			
			LDAP ldap = new LDAP();
			
			String username = ldap.getAccountNumberUsername(message.getAccountNumber());
			
			// todo: GABO - test
			//pendingIdentityConfirmations.put(username + message.getTimestamp() + message.getGuid(), message.getAccountNumber());
			if (!database.addPendingIdentityConfirmation(username + message.getTimestamp() + message.getGuid(), message.getAccountNumber())) {
				System.out.println("DBERROR: addPendingIdentityConfirmation returned false");
				return new MyResponse(false, MyResponse.ResponseString.ERROR);
			}
			
			ConfirmIdentityGcmMessage confirmIdentityMessage = new ConfirmIdentityGcmMessage(message.getAccountNumber(), message.getTimestamp(), message.getGuid(), message.getAction());
			new Gcm(username, confirmIdentityMessage);	
		} catch (Exception e) {
			response = new MyResponse(false, MyResponse.ResponseString.EXCEPTION, e.getMessage());		
		} 
		
		return response;
	}
	
	@POST
	@Path("/confirmIdentityResponse")
    @Produces(MediaType.APPLICATION_JSON)
	public MyResponse confirmIdentityResponse(ConfirmIdentityResponseServiceMessage message) {		
		MyResponse response = new MyResponse(true, MyResponse.ResponseString.SUCCESS);
		try {			
			LDAP ldap = new LDAP(message.getUsername());
			
			System.out.println("CONFIRM IDENTITY RESPONSE - " + message.getUsername() + " " + message.getTimestamp() + " " + message.getGuid());

			String messageId = message.getUsername() + message.getTimestamp() + message.getGuid();

			// todo: GABO - test
			//Object accountNumberObject = pendingIdentityConfirmations.get(messageId);

			MysqlDb database = new MysqlDb();
			String accountNumber = database.getPendingIdentityConfirmation(messageId);
			
			// if there was a pending identity confirmation and it isn't expired yet
			if (accountNumber != null && (new Date().getTime() - message.getTimestamp().getTime()) < confirmIdentityExpirationPeriod) {			
				if (checkTransactionOcra(ldap, message)) {	
					if (!database.setAccountNumberAuthenticated(message.getAccountNumber(), message.getTimestamp())) {
						System.out.println("DBERROR: setAccountNumberAuthenticated");
						return new MyResponse(false, MyResponse.ResponseString.ERROR);
					}
					sendIdentityConfirmedMessageToBank(accountNumber, message.getTimestamp(), message.getAnswer(), message.getGuid(), message.getAction());
				}
				else {
					response = new MyResponse(false, MyResponse.ResponseString.OCRA_ERROR);
				}
			}
			else {
				response = new MyResponse(false, MyResponse.ResponseString.EXPIRED);
			}
		} catch (Exception e) {
			response = new MyResponse(false, MyResponse.ResponseString.EXCEPTION, e.getMessage());
		}
		
		return response;
	}
	
	// todo: GABO - tu asi vobec netreba answer ....
	private void sendIdentityConfirmedMessageToBank(String accountNumber, Timestamp timestamp, String answer, String guid, String action) {
		BankMessage bankMessage = new ConfirmIdentityResponseBankMessage(accountNumber, timestamp, answer, guid, action);
		BankClient.executePost("identity", bankMessage);
	}

	@POST
	@Path("/confirmTransactionRequest")
    @Produces(MediaType.APPLICATION_JSON)
	public MyResponse confirmTransactionRequest(ConfirmTransactionRequestBankMessage message) {
		MyResponse response = new MyResponse(true, MyResponse.ResponseString.SUCCESS);
		
		try {		
			System.out.println("CONFIRM TRANSACTION REQUEST - " + message.getAccountNumber() + " " + message.getTimestamp() + " " + message.getPaymentId());
	
			LDAP ldap = new LDAP("");		
			String username = ldap.getAccountNumberUsername(message.getAccountNumber());

			// todo: GABO - test
			//pendingTransactionsFromBank.put(username + message.getTimestamp() + message.getPaymentId(), message.getPaymentId());
			MysqlDb database = new MysqlDb();
			if (!database.addPendingTransaction(username + message.getTimestamp() + message.getPaymentId(), message.getPaymentId())) {
				System.out.println("DBERROR: addPendingTransaction returned false");
				return new MyResponse(false, MyResponse.ResponseString.ERROR);
			}
			
			ConfirmTransactionGcmMessage confirmTransactionMessage = new ConfirmTransactionGcmMessage(message.getAccountNumber(), message.getPaymentId(), message.getTimestamp(), message.getAmount());
			new Gcm(username, confirmTransactionMessage);	
		} catch (Exception e) {
			response = new MyResponse(false, MyResponse.ResponseString.EXCEPTION, e.getMessage());
		}

		return new MyResponse(true, MyResponse.ResponseString.SUCCESS);
	}

	@POST
	@Path("/confirmTransactionResponse")
    @Produces(MediaType.APPLICATION_JSON)
	public MyResponse confirmTransactionResponse(ConfirmTransactionResponseServiceMessage message) {
		System.out.println("CONFIRM TRANSACTION - " + message.getUsername() + " " + message.getTimestamp() + " " + message.getPaymentId());
		
		MyResponse response = new MyResponse(true, MyResponse.ResponseString.SUCCESS);
		
		LDAP ldap = null;
		try {			
			ldap = new LDAP(message.getUsername());
			
			boolean ocraMatched = checkTransactionOcra(ldap, message);
			if (ocraMatched) {		
				// todo: GABO - test		
				//Object paymentIdObject = pendingTransactionsFromBank.get(message.getUsername() + message.getTimestamp() + message.getPaymentId());
				MysqlDb database = new MysqlDb();
				String paymentId = database.getPendingTransaction(message.getUsername() + message.getTimestamp() + message.getPaymentId());
				
				if (paymentId == null || (new Date().getTime() - message.getTimestamp().getTime()) > confirmTransactionExpirationPeriod) {
					ConfirmTransactionResponseBankMessage bankMessage = new ConfirmTransactionResponseBankMessage(message.getPaymentId(), message.getTimestamp(), StatusString.EXPIRED);
					BankClient.executePost("transaction", bankMessage);
					
					return new MyResponse(false, MyResponse.ResponseString.EXPIRED);
				}

				ConfirmTransactionResponseBankMessage bankMessage = new ConfirmTransactionResponseBankMessage(message.getPaymentId(), message.getTimestamp(), message.getAnswer());
				BankClient.executePost("transaction", bankMessage);
			} 
			else {
				if(message.getAnswer().equals("err")) 
					response = new MyResponse(false, MyResponse.ResponseString.OCRA_ERROR);
				else
					response = new MyResponse(false, MyResponse.ResponseString.ERROR);
			}			
		} catch(Exception e) {
			response = new MyResponse(false, MyResponse.ResponseString.EXCEPTION, e.getMessage());
		}
		
		return response;
	}
	
	@POST
	@Path("/addAccountNumberToken")
	@Produces(MediaType.APPLICATION_JSON)
	public MyResponse addAccountNumberToken(AddAccountNumberTokenBankMessage message) {
		MyResponse response = new MyResponse(true, MyResponse.ResponseString.SUCCESS);
		
		try {
			String accountNumber = message.getAccountNumber();
			String token = message.getToken();
			
			System.out.println("ADDING ACCOUNT NUMBER TOKEN " + accountNumber + " " + token);
			
			LDAP ldap = new LDAP("");
			if (!ldap.getAccountNumberUsername(accountNumber).equals("")) {
				return new MyResponse(false, MyResponse.ResponseString.ACCOUNT_NUMBER_ERROR);
			}
	
			MysqlDb database = new MysqlDb();
			if (!database.addAccountNumberToken(accountNumber, token)) {
				return new MyResponse(false, MyResponse.ResponseString.ACCOUNT_NUMBER_ERROR);
			}
		} catch (Exception e) {
			return new MyResponse(false, MyResponse.ResponseString.EXCEPTION, e.getMessage());
		}
		
		return response;
	}
	
	@POST
	@Path("/addAccountNumber")
	@Produces(MediaType.APPLICATION_JSON)
	public MyResponse addAccountNumber(AddAccountNumberServiceMessage message) {
		System.out.println("ADDING ACCOUNT NUMBER " + message.getAccountNumber() + " " + message.getUsername());
		
		MyResponse response = new MyResponse(true, MyResponse.ResponseString.SUCCESS);
		
		try {
			MysqlDb database = new MysqlDb();
			if (!database.isAccountNumberTokenValid(message.getAccountNumber(), message.getToken())) {
				return new MyResponse(false, MyResponse.ResponseString.TOKEN_COMBINATION_ERROR);
			}
			
			LDAP ldap = new LDAP(message.getUsername());
			
			if (!ldap.addAccountNumber(message.getAccountNumber())) {
				return new MyResponse(false, MyResponse.ResponseString.ACCOUNT_NUMBER_ERROR);
			}
			
			database.removeAccountNumberToken(message.getAccountNumber(), message.getToken());
		} catch (Exception e) {
			
		}
		
		return response;
	}
	
	private boolean checkTransactionOcra(LDAP ldap, ServiceMessage message) {
		try {
			String imei = ldap.get_imei();
			String pin = ldap.get_pin();
			String otpSeed = ldap.get_attribute("description").getStringValue();
			String otpCounterString = ldap.get_attribute("employeenumber").getStringValue();
			
			int otpCounter = Integer.parseInt(otpCounterString);
			List<String> otps = OtpGenerator.generateOTP(otpSeed, otpCounter);
			
			int i = 0;
			for (String otp : otps)	{
				if(message.checkOcra(imei, pin, otp)) {	
					ldap.syncOtpCounter(otpCounter + i + 1);
					return true;
				}
	
				i++;
			}	
		} 
		catch (Exception e) {
			e.printStackTrace();
		}
		
		return false;
	}
	
	@POST
	@Path("/postLogin")
    @Produces(MediaType.APPLICATION_JSON)
	public Response getLoginStatus(UserData data) {
		
		LDAP database = null;
		Utils utils = new Utils();
		Response resp = Response.ok().build();
				
		database = new LDAP(data.getUname(),data.getPwd(),
				data.getGrid(),data.getGrid_num(),data.getOtp());
		               
		//nastavenie msg podla uspesneho/neuspesneho prihlasenia
		if(database.is_Correct()){     
			resp = Response.status(201).build();
		}else{
		 	resp = Response.status(417).build();
		}
			
        return resp;	
	}

	// todo: GABO - check if really not needed .. maybe delete after
//	@POST
//	@Path("/postRegister")
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response getRegisterStatus(RegData reg) {
//		
//		LDAP database = null;
//		Response resp = Response.ok().build();
//		
//		reg.makeGrid();
//		
//		req_message.put(reg.getUname(), "");
//		
//		database = new LDAP(reg.getUname(),reg.getPwd(),
//				reg.getMail(),reg.getGrid_card(),reg.getPin());
//		               
//		//nastavenie msg podla uspesneho/neuspesneho registracie
//		if(database.create()){  
//			resp = Response.status(201).build();
//		}
//	 	else{
//	 		resp = Response.status(417).build();
//		}
//		
//        return resp;	
//	}
	
	@POST
	@Path("/changeAtt")
    @Consumes(MediaType.TEXT_PLAIN)
	public String changeAtt(String str) {
		
		LDAP database = null;
		String[] splittedData;
		String msg = "";
		Response resp = Response.ok().build();
		boolean result = false;
		Utils utils = new Utils();
		
		try{
		splittedData = str.split("=");
		
		String uname = splittedData[0];
		String field = splittedData[1];
		String value = splittedData[2];
		String pass = splittedData[3];
		
		database = new LDAP(uname);
		if(database.modify_attribute(field, value, pass)) msg = "suc";
		else msg = "fail";
		
		}catch(Exception e){
			msg = "exc";
		}
		
		switch (msg) {
		case "suc":
			resp = Response.status(201).build(); 
			break;
		case "fail":
			resp = Response.status(417).build(); 
			break;
		case "exc":
			resp = Response.status(500).build(); 
			break;
		}
		
		return Integer.toString(resp.getStatus());
	}
	
	@POST
	@Path("/confTrans")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response postMessage(ConfirmTransactionResponseServiceMessage message) {		
		Response resp = Response.ok().build();
//		LDAP database = null;
//		
//		if(blocked_messages.get(message.getUsername() + message.getTimestamp()) == null) {		
//			try {			
//				database = new LDAP(message.getUsername());
//				
//				int i = 0;
//				boolean ocraMatched = checkTransactionOcra(database, message);
//				if (ocraMatched) {
//					resp = Response.status(201).build(); 
//					messages.put(message.getUsername() + message.getTimestamp(), message);
//					//pendingTransactionsFromBank.put(message.getUsername() + message.getTimestamp(), message);
//					pendingTransactions.remove(message.getMessageId());
//				} 
//				else {
//					if(message.getAnswer().equals("err")) 
//						resp = Response.status(417).build(); 
//					else
//						resp = Response.status(500).build(); 
//				}			
//			}catch(Exception e) {
//				e.printStackTrace();
//			}
//		} 
//		else {
//			blocked_messages.remove(message.getUsername() + message.getTimestamp());
//			//new Gcm(message.getUsername(), GcmMessageType.CONFIRM_TRANSACTION.ordinal(), "Confirmation failed", "Time Limit= ");
//			// todo: GABO - fix somehow
//			resp = Response.status(417).build();
//		}

 		return resp;
	}
	
	// todo: GABO - check if really not needed .. maybe delete after
//	@GET
//	@Path("/getRegStatus")
//	@Produces(MediaType.TEXT_PLAIN)
//	public String isRegFinished(@QueryParam("user") String user){
//		
//		String ret = "";
//		String msg = null;
//
//		long startTime = System.currentTimeMillis();
//		long currentTime = 0;
//		
//		try{			
//			do{
//				currentTime = System.currentTimeMillis();
//				
//				if((currentTime-startTime)>= 600000){
//					ret = "err";
//					messages_reg.remove(user);
//					req_message.remove(user);
//					break;
//				}
//				
//				msg = (String)messages_reg.get(user);
//				
//				if(msg!=null){				
//					messages_reg.remove(user);
//				}
//				
//				ret = "suc";
//				
//			}while(msg == null);
//			
//		}catch(Exception e){
//			messages_reg.remove(user);
//			ret = "err";
//		}
//		
//		return ret;
//	}

	// todo: GABO - check if really not needed .. maybe delete after	
//	@GET
//	@Path("/getAnswer")
//	@Produces(MediaType.TEXT_PLAIN)
//	public String getAnswer(@QueryParam("user") String user, @QueryParam("counter") String counter){
//		
//		String ret = "";
//		ConfirmTransactionResponseServiceMessage msg = null;
//		
//		long startTime = System.currentTimeMillis();
//		long currentTime = 0;
//		
//		try{			
//			do{
//				currentTime = System.currentTimeMillis();
//				
//				if((currentTime-startTime)>= 300000){
//					ret = "err";
//					messages.remove(user+counter);
//					blocked_messages.put(user+counter, "");
//					break;
//				}
//				
//				
//				msg = (ConfirmTransactionResponseServiceMessage)messages.get(user+counter);
//				
//				if(msg!=null){
//					if(msg.getAnswer().equals("ano")) ret = "1";
//					else ret = "0";
//					
//					messages.remove(user+counter);
//				}
//				
//			}while(msg == null);
//			
//		}catch(Exception e){
//			messages.remove(user+counter);
//			blocked_messages.put(user+counter, "");
//			ret = "err";
//		}
//		
//		return ret;
//	}
	
	@POST
	@Path("/deleteReg")
    @Consumes(MediaType.TEXT_PLAIN)
	public Response delUsr(String uname) {
		Response resp = Response.ok().build();
		
		LDAP database = new LDAP(uname);
		
		if(database.delete(uname)){
			resp = Response.status(201).build();
		}else resp = Response.status(417).build();
		
		return resp;
	}

	// todo: GABO - check if really not needed .. maybe delete after
//	@POST
//	@Path("/reqMess")
//    @Consumes(MediaType.TEXT_PLAIN)
//	public Response reqMess(String uname) {
//		Response resp = Response.ok().build();
//		
//		req_message.put(uname, "");
//		
//		return resp;
//	}
	
	@POST
	@Path("/writePIN")
    @Consumes(MediaType.TEXT_PLAIN)
	public Response writePIN(String str) {
		
		LDAP database = null;
		String[] splittedData;
		String msg = "";
		Response resp = Response.ok().build();
		boolean result = false;
		
		try{
			splittedData = str.split("=");
			
			String uname = splittedData[0];
			String pin = splittedData[1];		
	
			database = new LDAP(uname);
			
			result = database.add_attribute("roomNumber", pin);
			
			msg = "suc";		
		}catch(Exception e){
			msg = "exc";
		}
		
		switch (msg) {
			case "suc":
				resp = Response.status(201).build(); 
				break;
			case "exc":
				resp = Response.status(500).build(); 
				break;
		}
		
		return resp;
	}
	

	// todo: GABO - check if really not needed .. maybe delete after
	@POST
	@Path("/writeTransDev")
    @Consumes(MediaType.TEXT_PLAIN)
	public Response writeTansDev(String str) {
		// zakomentovane gabom - lebo sak asi nam to nebude treba
//		LDAP database = null;
//		String[] splittedData;
//		String msg = "";
//		Response resp = Response.ok().build();
//		boolean result = false;
//		Utils utils = new Utils();
//		
//		try{
//		splittedData = str.split("=");
//		
//		String uname = splittedData[0];
//		String transdata = splittedData[1];		
//
//		database = new LDAP(uname);
//		
//		if(splittedData.length > 2){
//			String ocra = splittedData[2];
//			String imei = database.get_imei();
//			
//			if(utils.checkOcra(imei, uname+"="+transdata, ocra)){
//				result = database.add_attribute("carLicense", transdata);
//				result = utils.checkTrans(database);
//				msg = "suc";
//			}else msg = "fail";
//		}else{
//	               
//			result = database.add_attribute("carLicense", transdata);
//		
//			result = utils.checkTrans(database);
//		
//			if(result == true) msg = "suc";
//			else msg = "fail";
//		
//		}
//		
//		}catch(Exception e){
//			msg = "exc";
//		}
//		
//		switch (msg) {
//		case "suc":
//			resp = Response.status(201).build(); 
//			break;
//		case "fail":
//			resp = Response.status(417).build(); 
//			break;
//		case "exc":
//			resp = Response.status(500).build(); 
//			break;
//		}
		
		return Response.status(500).build();
	}
	
	@POST
	@Path("/postLoginDev")
    @Consumes(MediaType.TEXT_PLAIN)
	public Response postLoginDev(String str) {
		
		LDAP database = null;
		String[] splittedData;
		String msg = "";
		Response resp = Response.ok().build();
		
		try{
		splittedData = str.split("-");
		
		String uname = splittedData[0];
		String pwd = splittedData[1];
		String otp = splittedData[2];
		
		database = new LDAP(uname,pwd,otp);
	               
		//nastavenie msg podla uspesneho/neuspesneho prihlasenia
		if(database.is_Correct()){     
			msg = "suc";
		}
	 	else{
	 		msg = "fail";
		}
		}catch(Exception e){
			msg = "exc";
		}
		
		switch (msg) {
		case "suc":
			resp = Response.status(201).build(); 
			break;
		case "fail":
			resp = Response.status(417).build(); 
			break;
		case "exc":
			resp = Response.status(500).build(); 
			break;
		}
		
		return resp;
	}

	// todo: GABO - check if really not needed .. maybe delete after
//	@GET
//	@Path("/synchronizeDev")
//	@Produces(MediaType.TEXT_PLAIN)
//	public String synchronizeDevice(@QueryParam("data") String data){
//		
//		LDAP database = null;
//		RegistrationUtils utils = new RegistrationUtils();
//		String uname="";
//		String pwd="";
//		String regid="";
//		String imei="";		
//		String[] splittedData;
//		String ret = "";
//		
//		try{
//			
//		splittedData = data.split(":");
//		
//		uname = splittedData[0];
//		pwd = splittedData[1];
//		regid = splittedData[2];
//		imei = splittedData[3];
//		
//		if(!req_message.containsKey(uname)){
//			throw new Exception("Not Requested");
//		}else req_message.remove(uname);
//			
//		database = new LDAP(uname,pwd);
//		
//		if(database.is_Correct()){	
//			// ked sa overi pouzivatel, tak sa pripoj ako admin s pravami na edit
//			// urobene kvoli tomu ze som nevedel nakonfigurovat LDAP ...
//			database = new LDAP(uname);
//			
//			String pin = database.get_pin();
//			
//			database.set_device_data(imei, pin, regid);
//			
//			String[] grid = database.get_gridCard(pwd);
//			
//			ret = utils.enc_grid_card(pwd, grid);
//			
//			messages_reg.put(uname, "Done");
//		}else{
//			ret = "err";
//		}
//			
//		}catch(Exception e){
//			ret = "err";
//		}
//		
//		return ret;
//	}
	
	
	@GET
	@Path("/regDevice")
    @Produces(MediaType.APPLICATION_JSON)
	public String postRegisterDevice(@QueryParam("data") String str) {
		
		LDAP database = null;
		RegistrationUtils utils = new RegistrationUtils();
		String[] splittedData;
		String msg;
		String resp = "";
		String grid_enc="";
		
		
		JSONObject json = new JSONObject();
		
		try{
			
		str = str.replace("*", " ");
		
		splittedData = str.split(":");
		
		String uname = splittedData[0];
		String pwd = splittedData[1];
		String rep_pwd = splittedData[2];
		String mail = splittedData[3];
		String rep_mail = splittedData[4];
		String pin = splittedData[5];
		String imei = splittedData[6];
		String appID = splittedData[7];
		
		if(utils.check_data(pwd, rep_pwd, uname, mail, rep_mail, pin)){
			
			grid_enc = utils.make_grid_card(pwd);
			
			String[] grid_card = utils.getGrid_card(); 

			database = new LDAP(uname,pwd,mail,grid_card,appID,imei,pin);
		               
			//nastavenie msg podla uspesneho/neuspesneho registracie
			if(database.create_dev()){  
				msg = "suc";
			}
		 	else{
		 		msg = "fail";
			}
		} 
		else{
			msg = "err";
		}
		}catch(Exception e){
			msg = "exc";
		}
		
		switch (msg) {
		case "suc":
			json.put("response", "suc");
			json.put("data", grid_enc);
			break;
		case "fail":
			json.put("response", "fail");
			json.put("data", "err"); 
			break;
		case "err":
			json.put("response", "err");
			json.put("data", utils.getMessages());
			break;
		case "exc":
			json.put("response", "exc");
			json.put("data", "err");
			break;
		}
		
		return json.toJSONString(); 
	}
	
	@GET
	@Path("/getTrans")
    @Produces(MediaType.TEXT_PLAIN)
	public String getTrans(@QueryParam("user") String user) {
		
		LDAP database = null;
		RegistrationUtils utils = new RegistrationUtils();
		String[] trans;
		String msg="";
		
		try{
			database = new LDAP(user);
		
			trans = database.get_trans();
		
			for (String string : trans) {
				msg+=string+"=";
			}
		
			msg = msg.substring(0, msg.length()-1);
		}catch(Exception e){
			msg = "err";
		}
			return msg;	
        
	}
	
	@GET
	@Path("/getRecovery")
    @Produces(MediaType.TEXT_PLAIN)
	public String getRecovery(@QueryParam("user") String user) {
		
		LDAP database = null;
		String msg="";
		
		try{
			database = new LDAP(user);
			String mail = database.get_mail();
			
			Recovery rec = new Recovery(user);
			
			String[] to = {mail};
			
			rec.sendFromGMail(to);
			
			msg = rec.getCode();
		}catch(Exception e){
			msg = "Error";
		}
		
        return msg;	
        
	}
	
	@GET
	@Path("/sendNot")
    @Produces(MediaType.TEXT_PLAIN)
	public String sendNot(@QueryParam("user") String user, @QueryParam("cont") String cont, @QueryParam("counter") String counter) {
		
		LDAP database = null;
		String answ= "";
		
		try{
			System.out.println("Sendin to "+user);
			// todo: GABO - asi tento request vobec netreba ... keby hej, tka fixnut
			//new Gcm(user, GcmMessageType.CONFIRM_TRANSACTION.ordinal(), "Transaction confirmation", cont+"="+counter);
			System.out.println("Sended to "+user);
			
			answ = "suc";
		}catch(Exception e){
			answ = "err";
		}
		
        return answ;	
        
	}
	
	@GET
	@Path("/getTs")
    @Produces(MediaType.TEXT_PLAIN)
	public String getTs(@QueryParam("user") String user, @QueryParam("ts") String ts) {
		
		LDAP database = null;
		RegistrationUtils utils = new RegistrationUtils();
		String[] trans;
		String msg="";
		
		try{
			database = new LDAP(user);
		
			Long reg_ts = Long.parseLong(database.get_ts());
			
			long reg = reg_ts/Long.parseLong("2629746000");
			long act = Long.parseLong(ts)/Long.parseLong("2629746000");
			
			if((act-reg)<6){
				msg = "ok";
			}else msg = "out";
			
		}catch(Exception e){
			msg = "err";
		}
			return msg;	
        
	}

	}
