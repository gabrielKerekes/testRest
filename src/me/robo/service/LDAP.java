package me.robo.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.novell.ldap.LDAPAttribute;
import com.novell.ldap.LDAPAttributeSet;
import com.novell.ldap.LDAPConnection;
import com.novell.ldap.LDAPDeleteRequest;
import com.novell.ldap.LDAPEntry;
import com.novell.ldap.LDAPException;
import com.novell.ldap.LDAPModification;
import com.novell.ldap.LDAPSearchResults;

//trieda realizujuca pripojenie k databaze a nacitavanie dat 
public class LDAP {
	
	//potrebne pre LDAP Connection
	private int ldapPort;
	private int searchScope;
	private int ldapVersion;
	private boolean attributeOnly;
	private String attrs[] = {LDAPConnection.ALL_USER_ATTRS};    
	private String ldapHost;
    private String loginDN ;
    private String password;
    private String searchBase;
    private String searchFilter;
    //new user data
    private String new_user;
    private String new_pass;
    private String new_mail;
    private String new_grid_card[];
    private int new_counter;
    private String new_pin;
    private String pin;
    private String init_string;
    private String imei;
    private String username;
    
    private LDAPConnection lc;
    
    //nase lokalne
    private String jsp_code;
    private String database_code;
    private String jsp_otp;
	boolean is_correct;
	private Utils utils = new Utils();
	private String ldapIp;

	private String readLdapIpFromConfigFile() {
		try {
			BufferedReader br = new BufferedReader(
					new InputStreamReader(this.getClass().getResourceAsStream("/config/ldapIp.conf")));
			ldapIp = br.readLine();
			br.close();
		} catch (Exception e) {
			ldapIp = "147.175.98.17";
			e.printStackTrace();
			System.out.println("IP config file not found");
		}

		return ldapIp;
	}

	//konstruktor pre prihlasenie
	LDAP(String username, String passwrd, String grid, String number, String otp){
		this.username = username;
		
		ldapPort = LDAPConnection.DEFAULT_PORT;
        searchScope = LDAPConnection.SCOPE_ONE;
        ldapVersion  = LDAPConnection.LDAP_V3;
        attributeOnly = false;        
        
        ldapHost = readLdapIpFromConfigFile();//"147.175.98.17";//"192.168.0.3";
        loginDN = "uid="+username+",ou=Users,dc=test,dc=sk";
        password = Integer.toString(passwrd.hashCode());
        searchBase = "ou=Users,dc=test,dc=sk";
        searchFilter = "uid="+username;
        
        lc = new LDAPConnection();      
        jsp_code = grid;  
        jsp_otp = otp;
		is_correct = false;
		
		boolean connected = false;

		try {
			connected = connect();
		} 
		catch (UnsupportedEncodingException e) {
			System.out.println( "Nepodarilo sa pripojit" );
			disconnect();
		}
		
		if( connected ){
			
	//		is_correct = true; //testing
			
			database_code = get_grid(number);
		    is_correct = grid_is_correct();
		    
		    if(is_correct){
		    	is_correct = otp_is_correct();
		    }
			disconnect();

		}
			
	}	

	// konstruktor pre spravu uzivatela
	LDAP(String username){

		this.username = username;
		
		ldapPort = LDAPConnection.DEFAULT_PORT;
        searchScope = LDAPConnection.SCOPE_ONE;
        ldapVersion  = LDAPConnection.LDAP_V3;
        attributeOnly = false;
        
        ldapHost = readLdapIpFromConfigFile();//"147.175.98.17";//"192.168.0.3";
        loginDN = "cn=admin,dc=test,dc=sk";
        password = "gbld33";
        searchBase = "ou=Users,dc=test,dc=sk";
        searchFilter = "uid="+username;
        
        lc = new LDAPConnection();
        try {
			connect();
		} catch (UnsupportedEncodingException e) {
			System.out.println("Error appeared");
		}
	}
	
	// Novy konstruktor pre Registration.java z aplikacie 
	LDAP(String username, String pass, String mail, String grid_card[], String init,String imei,String pin){
		
		ldapPort = LDAPConnection.DEFAULT_PORT;
        searchScope = LDAPConnection.SCOPE_ONE;
        ldapVersion  = LDAPConnection.LDAP_V3;
        attributeOnly = false;   
        
        new_user = username;
        new_pass = Integer.toString(pass.hashCode());
        new_mail = mail;
        new_grid_card = grid_card;
        new_counter = 0;
        init_string = init;
        this.imei = imei;
        this.pin = pin;        
        
        ldapHost = readLdapIpFromConfigFile();//"147.175.98.17";//"192.168.0.3";
        loginDN = "cn=admin,dc=test,dc=sk";
        password = "gbld33";
        searchBase = "ou=Users,dc=test,dc=sk";
        searchFilter = "uid="+username;
        
        lc = new LDAPConnection();
        try {
			connect();
		} catch (UnsupportedEncodingException e) {
			System.out.println("Error appeared");
		}
	}
	
	//konstruktor pre prihlasenie pomocou aplikacie
	LDAP(String username, String passwrd, String otp){

		ldapPort = LDAPConnection.DEFAULT_PORT;
        searchScope = LDAPConnection.SCOPE_ONE;
        ldapVersion  = LDAPConnection.LDAP_V3;
        attributeOnly = false;        
        
        ldapHost = readLdapIpFromConfigFile();//"147.175.98.17";//"192.168.0.3";
        loginDN = "uid="+username+",ou=Users,dc=test,dc=sk";
        password = new_pass = Integer.toString(passwrd.hashCode());
        searchBase = "ou=Users,dc=test,dc=sk";
        searchFilter = "uid="+username;
        
        lc = new LDAPConnection(); 
        jsp_otp = otp;
		is_correct = false;
		
		boolean connected = false;

		try {
			connected = connect();
		} 
		catch (UnsupportedEncodingException e) {
			System.out.println( "Nepodarilo sa pripojit" );
			disconnect();
		}
		
		if( connected ){
		    is_correct = otp_is_correct();
			disconnect();
		}
			
	}
	
	//overenie uzivatela v databaze pri synchronizacii aplikacie
	LDAP(String username, String passwrd){

		ldapPort = LDAPConnection.DEFAULT_PORT;
        searchScope = LDAPConnection.SCOPE_ONE;
        ldapVersion  = LDAPConnection.LDAP_V3;
        attributeOnly = false;        
        
        ldapHost = readLdapIpFromConfigFile();//"147.175.98.17";//"192.168.0.3";
        loginDN = "uid="+username+",ou=Users,dc=test,dc=sk";
        password = Integer.toString(passwrd.hashCode());
        searchBase = "ou=Users,dc=test,dc=sk";
        searchFilter = "uid="+username;
        
        lc = new LDAPConnection(); 
		is_correct = false;

		try {
			is_correct = connect();
		} 
		catch (UnsupportedEncodingException e) {
			System.out.println( "Nepodarilo sa pripojit" );
			disconnect();
		}
			
	}

	// Novy konstruktor pre Registration.java 
	LDAP(String username, String pass, String mail, String grid_card[], String pin){
		
		ldapPort = LDAPConnection.DEFAULT_PORT;
        searchScope = LDAPConnection.SCOPE_ONE;
        ldapVersion  = LDAPConnection.LDAP_V3;
        attributeOnly = false;   
        
        new_user = username;
        new_pass = Integer.toString(pass.hashCode());
        new_mail = mail;
        new_grid_card = grid_card;
        new_counter = 0;
        this.pin = pin;        
        
        ldapHost = readLdapIpFromConfigFile();//"147.175.98.17";//"192.168.0.3";
        loginDN = "cn=admin,dc=test,dc=sk";
        password = "gbld33";
        searchBase = "ou=Users,dc=test,dc=sk";
        searchFilter = "uid="+username;
        
        lc = new LDAPConnection();
        try {
			connect();
		} catch (UnsupportedEncodingException e) {
			System.out.println("Error appeared");
		}
	}
	
	//pripojenie na LDAP server
	private boolean connect() throws UnsupportedEncodingException{
		
        try {
           // connect to the server
            lc.connect( ldapHost, ldapPort );
            // bind to the server
            lc.bind( ldapVersion, loginDN, password.getBytes("UTF8") );
        }
        catch(LDAPException e){
        	disconnect();
			System.out.println("Could not connect to the database");
        	
        	return false;
        }
            return true;
	}
	
	
	public void disconnect(){
		
		try {
			lc.disconnect();
			finalize();
		} 
		catch (LDAPException e) {
			System.out.println( "Could not disconnect" );
		} 
		catch (Throwable e) {
			System.out.println( "Could not finalize" );
			e.printStackTrace();
		}
	}
	
	
	//returne sa kontrolna premenna 
    boolean is_Correct(){    	     
        return is_correct;
    }

    //overenie ci je zadany grid spravny		
    private boolean grid_is_correct(){
    	
       //overenie ci je zadany grid spravny
       boolean correct = false;
       
       if(database_code == null)
    	   return correct;
       
       if( database_code.equals(jsp_code) ){
    	   correct = true;
    	   database_code = null;
       }
       else {
           correct = false;
       }
       return correct;        
    }
    
	
    //prevod kodu grid cisla (A2,A3,...) do poradia (1,2,3,......)
	private int get_code_num(String grid_num){
		
		int num = 0;
		String temp = grid_num.substring(0, 1);
		if(temp.equals("A"))
			num = Integer.parseInt(grid_num.substring(1, 2)) - 1;
		if(temp.equals("B"))
			num = 5 + Integer.parseInt(grid_num.substring(1, 2)) - 1;
		
		return num;
	}
	
	private String get_grid(String grid_num){
		
	   String grid = get_attribute("mail").getStringValueArray()[get_code_num(grid_num)];	
            
       return grid;            	
	}
	
	public String[] get_gridCard(String pwd){
	   
	   RegistrationUtils utils = new RegistrationUtils();
	   utils.make_grid_card(pwd);
	   String [] temp_grid_card = utils.getGrid_card();
	   
	   modify_array_attribute("mail", temp_grid_card);
	   
	   String grid[] = get_attribute("mail").getStringValueArray();	
	            
	   return grid;            	
	}
	
	public String[] get_trans(){
		
		String[] trans = get_attribute("carLicense").getStringValueArray();
		
		String pass = get_attribute("userpassword").getStringValue();
            
       return trans;            	
	}
	
	public String get_mail(){
		
		String mail = get_attribute("givenName").getStringValue();
            
       return mail;            	
	}
	
	public String get_pin(){
		
	   String pin = get_attribute("roomNumber").getStringValue();
            
       return pin;            	
	}
	
	public boolean set_device_data(String imei, String pin, String regid){
		set_hotp_data(pin,imei);
		
		/*String myEntryDN = searchFilter+ "," + searchBase;
		LDAPAttribute newAttr = new LDAPAttribute("roomNumber"); 
		LDAPModification singleChange = new LDAPModification( LDAPModification.DELETE, newAttr );

		try {
			lc.modify( myEntryDN, singleChange );
		} catch (LDAPException e) {
			e.printStackTrace();
			System.out.println("Could not delete");
			return false;
		}*/
		
		return modify_attribute("initials", regid, null);
	}
	
	public String get_reg_id(){
		return get_attribute("initials").getStringValue();
	}
	public String get_imei() {
		return get_attribute("employeeType").getStringValue();
	}
	public String get_ts() {
		return get_attribute("preferredLanguage").getStringValue();
	}
	
	private void set_hotp_data(String pin, String imei){
        byte[] new_pin_byte = null;
        try {
        	new_pin_byte = hmac_sha2(imei,pin);
		} catch (InvalidKeyException e1) {
			e1.printStackTrace();
			System.out.println( "InvalidKey" );
			System.exit(0);
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
			System.out.println( "NoSuchAlgorithm!" );
			System.exit(0);
		}        
        new_pin = bytesToHex(new_pin_byte);
        
        String snew_pin = new String(new_pin);
        
        modify_attribute("employeeType", new String(imei),null);
        modify_attribute("description", snew_pin,null);
        modify_attribute("employeenumber", Integer.toString(0),null);
	}
	//nacitanie pozadovaneho gridu z LDAPu
	public LDAPAttribute get_attribute(String attName){
		//nacita userove grids a do premennych "code_num" a "code" da vybrane hodnoty
		LDAPAttribute ldap_att = null;		
		LDAPSearchResults searchResults = null, tempResults= null;
		
		try {
			searchResults = lc.search(  searchBase,     // container to search
			            				searchScope,    // search scope
			            				searchFilter,   // search filter
			            				attrs,          // * should return all "1.1" returns entry name only
			            				attributeOnly);
			
			tempResults = lc.search(  	searchBase,     // container to search
					    				searchScope,    // search scope
					    				searchFilter,   // search filter
					    				attrs,          // * should return all "1.1" returns entry name only
					    				attributeOnly);
			
			LDAPEntry entry = tempResults.next();
		} 
		catch (LDAPException e1) {
			disconnect();
			System.out.println( "Nevyhladal" );
		}     
		
		
        if(searchResults.getCount() == 0){
        	System.out.println( "No entry has been found" );
        	disconnect();
        }
        else{
        	LDAPEntry nextEntry = null;     
        	
            	try {                	
                    nextEntry = searchResults.next();
                    ldap_att = nextEntry.getAttributeSet().getAttribute(attName); 
                }
                catch(LDAPException e) {
                	disconnect();
                	System.out.println( "Nepodarilo sa priradit");
                }                  	
        }
    	return ldap_att;   
	}
	
	//pridanie atributu k uzivatelovi - transakcia
	public boolean add_attribute(String field, String value){
		System.out.println(value);
		String myEntryDN = searchFilter+ "," + searchBase;
		LDAPAttribute newAttr = new LDAPAttribute(field, value); 
		LDAPModification singleChange = new LDAPModification( LDAPModification.ADD, newAttr );

		try {
			lc.modify( myEntryDN, singleChange );
		} catch (LDAPException e) {
			e.printStackTrace();
			System.out.println("Could not add field: "+field);
			return false;
		}
		
		return true;
	}
	
	//Sluzi aj na ulozenie REG_KEY uzivatelovi, atribut "initials"
	public boolean modify_attribute( String field, String value, String pass){
		String myEntryDN = searchFilter+ "," + searchBase;
		
		try {
			
			if(field.equals("pin")){
				field = "description";
				value = create_seed(value, get_attribute("employeeType").getStringValue());
			}else if(field.equals("mail")){
				field = "givenname";		
			}else if(field.equals("password")){
				field = "userpassword";
				if(!bind(username, pass)) throw new LDAPException();
			}
			
			LDAPAttribute newAttr = new LDAPAttribute(field, value); 
			LDAPModification singleChange = new LDAPModification( LDAPModification.REPLACE, newAttr );

			lc.modify( myEntryDN, singleChange );
		} catch (LDAPException e) {
			e.printStackTrace();
			System.out.println("Could not change field: "+field);
			return false;
		}
		
		return true;
	}
	
	public boolean modify_array_attribute( String field, String[] value){
		String myEntryDN = searchFilter+ "," + searchBase; 
		LDAPAttribute newAttr = new LDAPAttribute(field, value); 
		LDAPModification singleChange = new LDAPModification( LDAPModification.REPLACE, newAttr );

		try {
			lc.modify( myEntryDN, singleChange );
		} catch (LDAPException e) {
			e.printStackTrace();
			System.out.println("Could not change field: "+field);
			return false;
		}
		
		return true;
	}
	
	public boolean bind(String name, String pass){
		name = "uid="+name+",ou=Users,dc=test,dc=sk";
		
		try {
			lc.bind( ldapVersion, name, pass.getBytes("UTF8") );
		} catch (UnsupportedEncodingException | LDAPException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	
	//vytvorenie zaznamu - aplikacia 
	public boolean create_dev() {
		
		Date today = new Date();
		String timestamp = Long.toString(today.getTime());
		
		String containerName  = searchBase;
        LDAPAttributeSet attributeSet = new LDAPAttributeSet();

        attributeSet.add( new LDAPAttribute( "preferredLanguage", new String(timestamp)));              
        attributeSet.add( new LDAPAttribute( "objectclass", new String("inetOrgPerson")));                
        attributeSet.add( new LDAPAttribute("uid", new String(new_user)) );               
        attributeSet.add( new LDAPAttribute("cn", new String(new_user)) );               
        attributeSet.add( new LDAPAttribute("givenname", new String(new_mail)));        
        attributeSet.add( new LDAPAttribute("sn", new String(new_user)));
        attributeSet.add( new LDAPAttribute("userpassword", new String(new_pass)));    
        attributeSet.add( new LDAPAttribute("employeenumber", Integer.toString(new_counter) ));  
        attributeSet.add( new LDAPAttribute("initials", new String(init_string)) );  
        attributeSet.add( new LDAPAttribute("roomNumber", new String(pin)) );
        // Pridavanie vygenerovanych grid hodnot
        attributeSet.add( new LDAPAttribute("mail", new_grid_card));
        attributeSet.add( new LDAPAttribute("employeeType", imei));
        
        String seed = create_seed(pin, imei);
        attributeSet.add( new LDAPAttribute("description", seed));
                                                                    
        String  dn  = "uid=" + new_user + "," + containerName;
        LDAPEntry newEntry = new LDAPEntry( dn, attributeSet );
        
        try {
			lc.add( newEntry );
			return true;
		} catch (LDAPException e) {
			// ak sa nepodarilo pridat znamena to aj ze user uz existuje
			System.out.println("Could not add user");
			return false;
		}
		
	}
	
	//vytvorenie seedu pre OTP na ulozenie do LDAP
	private String create_seed(String pin, String imei){
        byte[] new_pin_byte = null;
        try {
        	new_pin_byte = hmac_sha2(imei,pin);
		} catch (InvalidKeyException e1) {
			e1.printStackTrace();
			System.out.println( "InvalidKey" );
			System.exit(0);
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
			System.out.println( "NoSuchAlgorithm!" );
			System.exit(0);
		}        
        new_pin = bytesToHex(new_pin_byte);
        
        String snew_pin = new String(new_pin);
        
        return snew_pin;
	}
	
	public boolean delete(String uname){
		boolean result = false;
		String dn = null;
		
		dn = searchFilter+","+searchBase;
		
		try{
			lc.delete(dn);
			result = true;
		}catch(Exception e){
			result = false;
		}		
		
		return result;
	}
	
	//vytvorenie zaznamu - web 
	public boolean create() {
		
		Date today = new Date();
		String timestamp = Long.toString(today.getTime());
		
		String containerName  = searchBase;
        LDAPAttributeSet attributeSet = new LDAPAttributeSet();
        
        attributeSet.add( new LDAPAttribute( "preferredLanguage", new String(timestamp)));
        attributeSet.add( new LDAPAttribute( "objectclass", new String("inetOrgPerson")));                
        attributeSet.add( new LDAPAttribute("uid", new String(new_user)) );               
        attributeSet.add( new LDAPAttribute("cn", new String(new_user)) );               
        attributeSet.add( new LDAPAttribute("givenname", new String(new_mail)));        
        attributeSet.add( new LDAPAttribute("sn", new String(new_user)));
        attributeSet.add( new LDAPAttribute("userpassword", new String(new_pass)));    
        attributeSet.add( new LDAPAttribute("employeenumber", Integer.toString(new_counter) ));  
        attributeSet.add( new LDAPAttribute("roomNumber", new String(pin)) );
        attributeSet.add( new LDAPAttribute("initials", new String("X")));
        // Pridavanie vygenerovanych grid hodnot
        attributeSet.add( new LDAPAttribute("mail", new_grid_card));
                                                                    
        String  dn  = "uid=" + new_user + "," + containerName;
        LDAPEntry newEntry = new LDAPEntry( dn, attributeSet );
        
        try {
			lc.add( newEntry );
			return true;
		} catch (LDAPException e) {
			// ak sa nepodarilo pridat znamena to aj ze user uz existuje
			System.out.println("Could not add user");
			return false;
		}
		
	}
	
	public boolean check() {
		// Skontroluje ci dany user uz existuje
		try {
			LDAPSearchResults user = lc.search(searchBase, LDAPConnection.SCOPE_ONE, "uid="+new_user, attrs, true);
			return false;
		} catch (LDAPException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return true;
		}
	}

	private boolean otp_is_correct(){
		List<String> OTP_window;

		//najskor ziskaj aktualny counter
		String otp_seed = get_attribute("description").getStringValue();
		String otp_counter = get_attribute("employeenumber").getStringValue();
		int otp_counter_i = Integer.parseInt(otp_counter);
		try {
			HOTPGenerator hotp_gen = new HOTPGenerator();
			OTP_window = hotp_gen.get_OTP(otp_seed, otp_counter_i,50);
	//		OTP_window = get_OTP(otp_seed, otp_counter_i,50);
		} catch (IOException e) {
			System.out.println("Problem generating otp from bash.");
			e.printStackTrace();
			return false;
		}
		
		int resync = 0;
		
		for(Iterator<String> otp_it = OTP_window.iterator(); otp_it.hasNext(); ) {
			resync++;
		    String item = otp_it.next();
		    if( item.equals(jsp_otp) ){
		    	LDAP adminDatabase = new LDAP(this.username);
		    	adminDatabase.modify_attribute("employeenumber", Integer.toString(otp_counter_i+resync),null);
		    	return true;
		    }
		}
		
       return false;  
	}
	
    private static List<String> get_OTP( String seed, int counter, int window ) throws IOException {  	
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
    
    
    public String toHex(String arg) throws UnsupportedEncodingException {
        return String.format("%040x", new BigInteger(1, arg.getBytes("UTF-8")));
    }
    
    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for ( int j = 0; j < bytes.length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    
    public byte[] hmac_sha2( String imei, String heslo) throws NoSuchAlgorithmException, InvalidKeyException
    {
    	String imei_hex = null;
    	String pin_hex = null;
    	
        try {
			imei_hex = toHex(imei);
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
			System.out.println( "Nepodoradilo sa prekodovat string na HEX. Zle kodovanie (UTF-8)" );
			System.exit(0);
		}
        try {
			pin_hex = toHex(heslo);
		} catch (UnsupportedEncodingException e2) {
			e2.printStackTrace();
			System.out.println( "Nepodoradilo sa prekodovat string na HEX. Zle kodovanie (UTF-8)" );
			System.exit(0);
		}
        
    	byte[] keyBytes = imei_hex.getBytes();
    	byte[] text = pin_hex.getBytes();
        
    	
        //        try {
        Mac hmacSha2;
        try {
            hmacSha2 = Mac.getInstance("HmacSHA256");
        } catch (NoSuchAlgorithmException nsae) {
            hmacSha2 = Mac.getInstance("HMAC-SHA-256");
        }
        SecretKeySpec macKey =
                new SecretKeySpec(keyBytes, "RAW");
        hmacSha2.init(macKey);
        return hmacSha2.doFinal(text);
    }

    
}
