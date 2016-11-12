package me.robo.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class HOTPGenerator {
	
    public List<String> get_OTP( String seed, int counter, int window ) throws IOException {  	
    	String command = "oathtool -w "+window+" -c "+counter+" "+seed;
        Process proc = Runtime.getRuntime().exec(command);
        
        BufferedReader stdInput = new BufferedReader(new 
        	     InputStreamReader(proc.getInputStream()));

		BufferedReader stdError = new BufferedReader(new 
		     InputStreamReader(proc.getErrorStream()));
        	
		List<String> otp_passwords = new ArrayList<String>();
		String line = null;
		
		// read the output from the command
        while ((line = stdInput.readLine()) != null) {
        	otp_passwords.add(line);
        }

        // read any errors from the attempted command
        while ((line = stdError.readLine()) != null) {
            System.out.println(line);
        }
        
        return otp_passwords;
    }

}
