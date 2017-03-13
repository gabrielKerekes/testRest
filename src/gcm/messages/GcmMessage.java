package gcm.messages;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;


@JsonAutoDetect()
public abstract class GcmMessage {
	// registration_ids and data must have the given name because of GCM
    private List<String> registration_ids;
    private Map<String,String> data;
    
    // transient keyword -> don't serialize to json
    private transient String username;
    private transient String title;
    private transient String message;
    private transient Timestamp timestamp;
    
    public GcmMessage(String title, String message, Timestamp timestamp) {
    	this.title = title;
    	this.message = message;
    	this.timestamp = timestamp;
    	
    	registration_ids = new ArrayList<String>();
    	data = new HashMap<String, String>();
    }
	
	public String getUsername() { return username; }
	public void setUsername(String username) { this.username = username; }
	
	public Timestamp getTimestamp() { return timestamp; }
	public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
	
	public String getTitle() { return title; }
	public void setTitle(String title) { this.title = title; }
	
	public String getMessage() { return message; }
	public void setMessage(String message) { this.message = message; }
	
	protected void putData(String key, String value) {
		data.put(key, value);
	}
	
	public void createData() {
		if (data == null) {
			data = new HashMap<String, String>();
		}
		
		putData("title", title);
		putData("message", message);
		putData("timestamp", timestamp.toString());
	}

	public void addGcmRegistrationId(String gcmRegistrationId) {
		registration_ids.add(gcmRegistrationId);
	}
}
