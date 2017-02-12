package me.robo.service;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.Gson;


public class POST2GCM {
    public static void post(String apiKey, Content content) {
        try {
	        // 1. URL
	        URL url = new URL("https://android.googleapis.com/gcm/send");
	
	        // 2. Open connection
	        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
	
	        // 3. Specify POST method
	        conn.setRequestMethod("POST");
	
	        // 4. Set the headers
	        conn.setRequestProperty("Content-Type", "application/json");
	        conn.setRequestProperty("Authorization", "key="+apiKey);
	
	        conn.setDoOutput(true);
	
	        // 5. Add JSON data into POST request body
	
	        //`5.1 Use Jackson object mapper to convert Contnet object into JSON
	        //ObjectMapper mapper = new ObjectMapper();
	        Gson gson = new Gson();
	        	
	        // 5.2 Get connection output stream
	        DataOutputStream wr = new DataOutputStream(conn.getOutputStream());
	
	        // 5.3 Copy Content "JSON" into
	        //mapper.writeValue(wr, content);
	    
	        String json = gson.toJson(content);
	        wr.writeBytes(json);
	        // 5.4 Send the request
	        wr.flush();
	
	        // 5.5 close
	        wr.close();
	
	        // 6. Get the response
	        int responseCode = conn.getResponseCode();
	
	        BufferedReader in = new BufferedReader(
	                new InputStreamReader(conn.getInputStream()));
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