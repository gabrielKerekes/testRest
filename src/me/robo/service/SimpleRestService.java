package me.robo.service;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import me.robo.service.LDAP;

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


@Path("/service")
public class SimpleRestService {

	private String loginStat = new String();
	private String regStat = new String();
	private static Map messages = new HashMap<String, Message>();
	private static Map messages_reg = new HashMap<String, String>();
	private static Map blocked_messages = new HashMap<String, String>();
	private static Map req_message = new HashMap<String, String>();
	private static Map pendingTransactionsFromBank = new HashMap<String, String>();
	
	@GET
	@Path("/getTest")
	@Produces(MediaType.TEXT_PLAIN)
	public String getTest(){
		
		return "Tadadaaa2!!";
	}
	
	@GET
	@Path("/getTransactionConfirmation")
	@Produces(MediaType.TEXT_PLAIN)
	public String getTransactionConfirmation(@QueryParam("username") String username, @QueryParam("amount") String amount)
	{		
		String response = "";
		String pendingTransaction = null;
		
		try
		{
			// todo: GABO - fix params
			//new App("Transaction confirmation", cont+"="+counter, user);
			new App("Transaction confirmation", "test" + "=" + 0 + "=" + amount, username);
			
			long startTime = System.currentTimeMillis();
			long currentTime = 0;
			
			do
			{				
				// 300000 milis = 5 minutes  
				if ((currentTime - startTime) >= 300000)
				{
					response = "error";
					pendingTransactionsFromBank.remove(username);
					break;
				}
				
				pendingTransaction = (String) pendingTransactionsFromBank.get(username);
				
				if (pendingTransaction != null)
				{				
					pendingTransactionsFromBank.remove(username);
				}
				
			} while (pendingTransaction == null);
			
			response = "success";
		}
		catch(Exception e)
		{
			response = "error";
			pendingTransactionsFromBank.remove(username);
		}
		
        return response;
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
	
	@POST
	@Path("/postRegister")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getRegisterStatus(RegData reg) {
		
		LDAP database = null;
		Response resp = Response.ok().build();
		
		reg.makeGrid();
		
		req_message.put(reg.getUname(), "");
		
		database = new LDAP(reg.getUname(),reg.getPwd(),
				reg.getMail(),reg.getGrid_card(),reg.getPin());
		               
		//nastavenie msg podla uspesneho/neuspesneho registracie
		if(database.create()){  
			resp = Response.status(201).build();
		}
	 	else{
	 		resp = Response.status(417).build();
		}
		
        return resp;	
	}
	
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
	public Response postMessage(Message msg){
		
		Response resp = Response.ok().build();
		LDAP database = null;
		
		if(blocked_messages.get(msg.getUname()+msg.getCounter())==null){
		
			try{
			
				database = new LDAP(msg.getUname());
				String imei = database.get_imei();
			
				if(msg.checkOcra(imei)){
					resp = Response.status(201).build(); 
					messages.put(msg.getUname()+msg.getCounter(), msg);
					pendingTransactionsFromBank.put(msg.getUname(), "");
				}else{
					if(msg.getAnswer().equals("err")) resp = Response.status(417).build(); 
					else resp = Response.status(500).build(); 
			}
			
			}catch(Exception e){
			
			}
		} else{
			blocked_messages.remove(msg.getUname()+msg.getCounter());
			new App("Confirmation failed", "Time Limit= ", msg.getUname());
		}
		
		return resp;
	}
	
	@GET
	@Path("/getRegStatus")
	@Produces(MediaType.TEXT_PLAIN)
	public String isRegFinished(@QueryParam("user") String user){
		
		String ret = "";
		String msg = null;

		long startTime = System.currentTimeMillis();
		long currentTime = 0;
		
		try{
			
		do{
			currentTime = System.currentTimeMillis();
			
			if((currentTime-startTime)>= 600000){
				ret = "err";
				messages_reg.remove(user);
				req_message.remove(user);
				break;
			}
			
			msg = (String)messages_reg.get(user);
			
			if(msg!=null){				
				messages_reg.remove(user);
			}
			
			ret = "suc";
			
		}while(msg == null);
			
		}catch(Exception e){
			messages_reg.remove(user);
			ret = "err";
		}
		
		return ret;
	}
	
	
	@GET
	@Path("/getAnswer")
	@Produces(MediaType.TEXT_PLAIN)
	public String getAnswer(@QueryParam("user") String user, @QueryParam("counter") String counter){
		
		String ret = "";
		Message msg = null;
		
		long startTime = System.currentTimeMillis();
		long currentTime = 0;
		
		try{
			
		do{
			currentTime = System.currentTimeMillis();
			
			if((currentTime-startTime)>= 300000){
				ret = "err";
				messages.remove(user+counter);
				blocked_messages.put(user+counter, "");
				break;
			}
			
			
			msg = (Message)messages.get(user+counter);
			
			if(msg!=null){
				if(msg.getAnswer().equals("ano")) ret = "1";
				else ret = "0";
				
				messages.remove(user+counter);
			}
			
		}while(msg == null);
			
		}catch(Exception e){
			messages.remove(user+counter);
			blocked_messages.put(user+counter, "");
			ret = "err";
		}
		
		return ret;
	}
	
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
	
	@POST
	@Path("/reqMess")
    @Consumes(MediaType.TEXT_PLAIN)
	public Response reqMess(String uname) {
		Response resp = Response.ok().build();
		
		req_message.put(uname, "");
		
		return resp;
	}
	
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
	
	
	@POST
	@Path("/writeTransDev")
    @Consumes(MediaType.TEXT_PLAIN)
	public Response writeTansDev(String str) {
		
		LDAP database = null;
		String[] splittedData;
		String msg = "";
		Response resp = Response.ok().build();
		boolean result = false;
		Utils utils = new Utils();
		
		try{
		splittedData = str.split("=");
		
		String uname = splittedData[0];
		String transdata = splittedData[1];		

		database = new LDAP(uname);
		
		if(splittedData.length > 2){
			String ocra = splittedData[2];
			String imei = database.get_imei();
			
			if(utils.checkOcra(imei, uname+"="+transdata, ocra)){
				result = database.add_attribute("carLicense", transdata);
				result = utils.checkTrans(database);
				msg = "suc";
			}else msg = "fail";
		}else{
	               
			result = database.add_attribute("carLicense", transdata);
		
			result = utils.checkTrans(database);
		
			if(result == true) msg = "suc";
			else msg = "fail";
		
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
	
	@GET
	@Path("/synchronizeDev")
	@Produces(MediaType.TEXT_PLAIN)
	public String synchronizeDevice(@QueryParam("data") String data){
		
		LDAP database = null;
		RegistrationUtils utils = new RegistrationUtils();
		String uname="";
		String pwd="";
		String regid="";
		String imei="";		
		String[] splittedData;
		String ret = "";
		
		try{
			
		splittedData = data.split(":");
		
		uname = splittedData[0];
		pwd = splittedData[1];
		regid = splittedData[2];
		imei = splittedData[3];
		
		if(!req_message.containsKey(uname)){
			throw new Exception("Not Requested");
		}else req_message.remove(uname);
			
		database = new LDAP(uname,pwd);
		
		if(database.is_Correct()){		
			String pin = database.get_pin();
			
			database.set_device_data(imei, pin, regid);
			
			String[] grid = database.get_gridCard(pwd);
			
			ret = utils.enc_grid_card(pwd, grid);
			
			messages_reg.put(uname, "Done");
		}else{
			ret = "err";
		}
			
		}catch(Exception e){
			ret = "err";
		}
		
		return ret;
	}
	
	
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
			new App("Transaction confirmation", cont+"="+counter, user);
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
