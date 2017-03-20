package client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.Gson;

import bank.messages.BankMessage;

public class BankClient {
	private static final String BankIpAddress = "192.168.0.102";
	
	public static int executePost(BankMessage message) {
		System.out.println("BANK POST - " + message.getAccountNumber() + " " + message.getTimestamp());
		// todo: GABO - uncomment when bank ready
//		try {
//	        URL url = new URL(BankIpAddress);
//	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
//	
//	        conn.setRequestMethod("POST");
//	        conn.setRequestProperty("Content-Type", "application/json");
//	        conn.setDoOutput(true);
//		        	
//	        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
//
//	        Gson gson = new Gson();
//	        String json = gson.toJson(message);
//	        
//	        wr.writeBytes(json);
//	        wr.flush();
//	        wr.close();
//	
//	        return conn.getResponseCode();	        
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//		
//		return 500;
		return 200;
	}
}
