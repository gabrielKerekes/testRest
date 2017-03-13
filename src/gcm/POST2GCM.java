package gcm;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.Gson;

public class POST2GCM {
    public static void post(String apiKey, GcmContent content) {
        try {
	        URL url = new URL("https://android.googleapis.com/gcm/send");
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	
	        conn.setRequestMethod("POST");
	        conn.setRequestProperty("Content-Type", "application/json");
	        conn.setRequestProperty("Authorization", "key="+apiKey);
	        conn.setDoOutput(true);
		        	
	        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());

	        Gson gson = new Gson();
	        String json = gson.toJson(content);
	        
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
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}