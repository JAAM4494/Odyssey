package odyssey;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author jaam
 */
public class Constants {
    
    public static String selectedLib = "MyOdyssey-Lib";
    
    public static String loginUrl = "http://lastodyssey.mybluemix.net/login";
    public static String logoutUrl = "http://lastodyssey.mybluemix.net/logout";
    public static String uploadUrl = "http://lastodyssey.mybluemix.net/upload";
    public static String shareLibsUrl = "http://lastodyssey.mybluemix.net/sharedlibs/";
    public static String sharedLibContentUrl = "http://lastodyssey.mybluemix.net/metadata/";
    public static String streamingUrl = "http://lastodyssey.mybluemix.net/stream/";
    public static String shareMyLibUrl = "http://lastodyssey.mybluemix.net/shareLib/";
    
    public static String userName = "1";
    public static String userID = "XXX";
    
    
    // Sentencias SQL
    
    public static String SQLInsertIntoP1 = "insert into user_";
    public static String SQLInsertIntoP2 = "_Lib(name,artist,album,genre,anno,media,duration,fileSize) values (?,?,?,?,?,?,?,?)";
    
    public static String SQLSelectP1 = "select mp3ID,name,artist,album,genre,anno,duration,fileSize from user_";
    public static String SQLSelectP2 = "_Lib order by mp3ID";
    
    public static String SQLUpdateP1 = "update user_";
    public static String SQLUpdateP2 = "_Lib set(name,artist,album,genre,anno) = (?,?,?,?,?) where mp3ID=?";
    
    public static String SQLSelectMediaP1 = "select media from user_";
    public static String SQLSelectMediaP2 = "_Lib where mp3ID=?";
    
    public static String SQLSelectStatus = "select localUpdateAvaible from localUsers where userID=?";
    
    public static String SQLSelectStatusToServer = "select localUpdateAvaibletoServer from localUsers where userID=?";
    
    public static String SQLUpdateLocalLibStatus = "update localUsers set localUpdateAvaible=? where userID=?";
    
    public static String SQLUpdateLocalLibStatusToServer = "update localUsers set localUpdateAvaibletoServer=? where userID=?";
    
    
}
