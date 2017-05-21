package gcm;

import java.io.Serializable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.annotate.JsonAutoDetect;

@JsonAutoDetect()
public class GcmContent implements Serializable {
    private List<String> registration_ids;
    private Map<String,String> data;

    public void addRegId(String regId) {
        if(registration_ids == null)
            registration_ids = new LinkedList<String>();
        registration_ids.add(regId);
    }

    // todo: GABO - mozno prerobit na to, aby vsetko nebolo v message, ale aby to boly normal parametre
    public void createData(int messageType, String title, String message) {
        if(data == null)
            data = new HashMap<String,String>();

        data.put("messageType", Integer.toString(messageType));
        data.put("title", title);
        data.put("message", message);
    }
}