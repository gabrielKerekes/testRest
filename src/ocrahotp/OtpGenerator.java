package ocrahotp;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class OtpGenerator {
	private static int WINDOW_SIZE = 50;
	
	public static List<String> generateOTP( String seed, int counter) throws IOException {  	
    	String command = "oathtool -w "+ WINDOW_SIZE + " -c " + counter + " " + seed;
        Process proc = Runtime.getRuntime().exec(command);
        
        BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));
		BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));
        	
		List<String> otps = new ArrayList<String>();		
		String line = null;		
		// read the output from the command
        while ((line = stdInput.readLine()) != null) {
        	otps.add(line);
        }

        // read any errors from the attempted command
        while ((line = stdError.readLine()) != null) {
            System.out.println(line);
        }
        
        return otps;
    }
}
