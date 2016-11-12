package me.robo.service;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.apache.tomcat.util.codec.binary.Base64;


//import org.apache.commons.codec.binary.Base64;

public class RegistrationUtils {
	
	private List<String> messages;
	private String[] grid_card;
	
	//vytvori random string dlzky 5
	private String random_string(){
		
		String code = " ";
		String str = UUID.randomUUID().toString();		
		code = str.substring(str.length()-6, str.length()-1);
		
		return code;		
	}
	
	public String enc_grid_card(String pass, String[] grid){
		String content = " ";
		String temp = null;
		
		content = grid[0];
		
		for(int i = 1; i<10; i++){
			content = content + " " + grid[i];
		}
		
		try {
			content = encrypt(content,pass);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		
		return content;
	}
	
	//vytvorenie obsahu suboru
	public String make_grid_card(String pass){
		grid_card = new String[10];
		String content = " ";
		String temp = null;
		
		content = random_string();
		grid_card[0] = content;
		
		for(int i = 0; i<9; i++){
			temp = random_string();
			grid_card[i+1] = temp;
			content = content + " " + temp;
		}
		
		try {
			content = encrypt(content,pass);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		
		return content;
	}
	
	//zasifrovanie obsahu 
	private String encrypt(String plain_text,String pass) throws NoSuchAlgorithmException, NoSuchPaddingException{
		
		String cipher_text = " ";
		
		//zahashovanie kluca "pass" na 256bit dlzku (potrebne pre AES)
		MessageDigest md = MessageDigest.getInstance("SHA-256");
		md.update(pass.getBytes());
		byte[] key = md.digest();
		
		//specifikacia kluca
		SecretKeySpec keySpec = new SecretKeySpec(key, "AES");
		IvParameterSpec ivSpec = new IvParameterSpec("1234567891012345".getBytes());
		
		//nastavenie sifry
		Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");		
		
		try {
			//inicializacia sifry
			cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
		} catch (InvalidKeyException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		} catch (InvalidAlgorithmParameterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
		try {
			//sifrovanie
			byte[] encrypted = cipher.doFinal(plain_text.getBytes("UTF-8"));

			//konverzia sifrovaneho textu z byte array na String
			byte[]   bytesEncoded = Base64.encodeBase64(encrypted); //TODO: encoding -- neviem ci dobre !!!
			System.out.println("ecncoded value is " + new String(bytesEncoded ));
			
			cipher_text = new String(bytesEncoded);
			
		} catch (IllegalBlockSizeException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		} catch (BadPaddingException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			System.out.println(e.getMessage());
		}
		
		return cipher_text;		
	}
	
	//kontrola spravnosti zadanych dat 
		public boolean check_data(String pw, String rep_pw, String un, String mail, String mail_repeat, String pin){
			
			boolean is_OK = true;
			
			messages = new ArrayList<String>();
			
			if( pw.trim().isEmpty() ){
				messages.add("Password is required!");
				is_OK = false;
			}
			else if(pw.length()<6){
				messages.add("Password length<6!");
				is_OK = false;
			}
			else if(pw.matches(".*[^\\s\\w].*")){
				messages.add("Password contains special character!");
				is_OK = false;
			}
			else if(!(pw.matches(".*\\d.*") && pw.matches(".*[A-Z].*"))){
				messages.add("Password does not contain capital or number!");
				is_OK = false;
			}
			else if(!pw.equals(rep_pw)){
				messages.add("Passwords do not match!");
				is_OK = false;
			}
			
			if (un.trim().isEmpty()){
				messages.add("User name is required!");
				is_OK = false;
			}
			
			if (mail.trim().isEmpty()){
				messages.add("Mail is required!");
				is_OK = false;
			}	
			else if (!mail.contains("@")){
				messages.add("Wrong mail format");
				is_OK = false;
			}
			else if (!mail.contains(".")){
				messages.add("Wrong mail format");
				is_OK = false;
			}
			else if (!mail.equals(mail_repeat)){
				messages.add("Mails do not match!");
				is_OK = false;
			}
			
			if (pin.trim().isEmpty()){
				messages.add("PIN is required!");
				is_OK = false;
			}

			return is_OK;
		}

		public String getMessages() {
			
			String msgs="";
			
			for(int i=0;i<messages.size();i++){
				if(i == 0) msgs=messages.get(i);
				else msgs+="-"+messages.get(i);
			}
			
			return msgs;
		}
		
		public String[] getGrid_card(){
			return grid_card;
		}
		
}
