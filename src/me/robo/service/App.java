package me.robo.service;


public class App 
{
    public App(String title, String cont, String username)
    {
        String apiKey = "AIzaSyAZBdN__jxzicFfzSmqtgL-fKVDoAqiaCg";//"AIzaSyAJG2hGrTAdAfguxDkDZbEKLbBATdQZRZg";
        Content content = createContent(title, cont, username);

        POST2GCM.post(apiKey, content);
    }

    public static Content createContent(String title, String cont, String username){

        Content c = new Content();
        LDAP database = new LDAP(username);
        String reg_id = database.get_reg_id();
        c.addRegId(reg_id);
        c.createData(title, cont);        
        database.disconnect();
        
        return c;
    }
}