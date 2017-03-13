package service;

import java.util.Properties;
import java.util.UUID;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

public class Recovery {	
 	private static String USER_NAME = "dpauth2016";  
    private static String PASSWORD = "MailService2016";
    private String code = null;
    private String user;
    
    Recovery(String usr){
    	user = usr;
    	code = random_string();
    }
   
    public void sendFromGMail( String[] to) {
        Properties props = System.getProperties();
        String host = "smtp.gmail.com";
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", host);
        props.put("mail.smtp.user", USER_NAME);
        props.put("mail.smtp.password", PASSWORD);
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
 
        Session session = Session.getDefaultInstance(props);
        MimeMessage message = new MimeMessage(session);
 
        try {
            message.setFrom(new InternetAddress(USER_NAME));
            InternetAddress[] toAddress = new InternetAddress[to.length];
 
            // To get the array of addresses
            for( int i = 0; i < to.length; i++ ) {
                toAddress[i] = new InternetAddress(to[i]);
            }
 
            for( int i = 0; i < toAddress.length; i++) {
                message.addRecipient(Message.RecipientType.TO, toAddress[i]);
            }
 
            message.setSubject("Recovery code for user "+user);
            message.setText("Code: "+code);
            Transport transport = session.getTransport("smtp");
            transport.connect(host, USER_NAME, PASSWORD);
            transport.sendMessage(message, message.getAllRecipients());
            transport.close();
        }
        catch (AddressException ae) {
            ae.printStackTrace();
        }
        catch (MessagingException me) {
            me.printStackTrace();
        }
    }

	public String getCode() {
		return code;
	}
    
	private String random_string(){
		
		String code = " ";
		String str = UUID.randomUUID().toString();		
		code = str.substring(str.length()-9, str.length()-1);
		
		return code;		
	}
}
