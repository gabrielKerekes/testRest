package gcm;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Timestamp;

import com.google.gson.Gson;

import gcm.messages.ConfirmIdentityGcmMessage;
import gcm.messages.ConfirmTransactionGcmMessage;
import gcm.messages.GcmMessage;
import service.LDAP;

public class Gcm {
    public static String apiKey = "AIzaSyA9Me7U6x9mhne7t7aUmmVLCzdcujmtx-M";//"AIzaSyAZBdN__jxzicFfzSmqtgL-fKVDoAqiaCg";//"AIzaSyAJG2hGrTAdAfguxDkDZbEKLbBATdQZRZg";
    
    public static void post(String username, GcmMessage gcmMessage) {        
        String gcmRegistartionId = getUserGcmRegistrationId(username);
        gcmMessage.addGcmRegistrationId(gcmRegistartionId);
        
        gcmMessage.createData();
        
        Gcm.post(gcmMessage);
    }
    
    private static String getUserGcmRegistrationId(String username) {
        LDAP database = new LDAP(username);
        String gcmRegistrationId = database.get_reg_id();      
        database.disconnect();
        
        return gcmRegistrationId;
    }
    
    private static void post(GcmMessage message) {
        try {
	        URL url = new URL("https://android.googleapis.com/gcm/send");
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Content-Type", "application/json");
	        conn.setRequestProperty("Authorization", "key="+apiKey);
	        conn.setDoOutput(true);
		        	
	        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());

	        Gson gson = new Gson();
	        String json = gson.toJson(message);
	        
	        wr.writeBytes(json);
	        wr.flush();
	        wr.close();
	
	        int responseCode = conn.getResponseCode();
	
	        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
	        String inputLine;
	        StringBuffer response = new StringBuffer();
	
	        while ((inputLine = in.readLine()) != null) {
	            response.append(inputLine);
	        }
	        in.close();
	        
        } catch (MalformedURLException e) {
        	System.out.println("GcmPost - Malformed URL exception");
            e.printStackTrace();
        } catch (IOException e) {
        	System.out.println("GcmPost - IOException");
            e.printStackTrace();
        }
    }
}
