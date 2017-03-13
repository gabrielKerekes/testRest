package service;

import gcm.Gcm;
import gcm.GcmContent;

public class App {
	// todo: GABO - fakt ze refaktorovat .... message by mala osbahovat typ spravy, elbo uz ich bude viac
//    public App(String username, int messageType, String title, String cont) {
//        String apiKey = "AIzaSyA9Me7U6x9mhne7t7aUmmVLCzdcujmtx-M";//"AIzaSyAZBdN__jxzicFfzSmqtgL-fKVDoAqiaCg";//"AIzaSyAJG2hGrTAdAfguxDkDZbEKLbBATdQZRZg";
//        GcmContent content = createContent(username, messageType, title, cont);
//
//        Gcm.post(apiKey, content);
//    }
//    // todo: GABO - nahradit GcmContent GcmMessagou
//    public static GcmContent createContent(String username, int messageType, String title, String cont) {
//
//        GcmContent c = new GcmContent();
//        LDAP database = new LDAP(username);
//        String reg_id = database.get_reg_id();
//        c.addRegId(reg_id);
//        c.createData(messageType, title, cont);        
//        database.disconnect();
//        
//        return c;
//    }
}