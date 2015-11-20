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
    public static String actualUser = "LocalUser";
    
    public static boolean syncWorking = false;
    
    public static String getCloudLibCountUrl = "http://lastodyssey.mybluemix.net/myLibLength/";
    
    public static String modifMetaUrl = "http://lastodyssey.mybluemix.net/updateMetadata/";
    
    public static String loginUrl = "http://lastodyssey.mybluemix.net/login";
    public static String logoutUrl = "http://lastodyssey.mybluemix.net/logout";
    
    public static String uploadUrl = "http://lastodyssey.mybluemix.net/upload";
    
    public static String shareLibsUrl = "http://lastodyssey.mybluemix.net/sharedlibs/";
    public static String sharedLibContentUrl = "http://lastodyssey.mybluemix.net/metadata/";
    public static String shareMyLibUrl = "http://lastodyssey.mybluemix.net/shareMyLib";
    
    public static String streamingUrl = "http://lastodyssey.mybluemix.net/stream/";
    
    public static String getShareLibStatusUrl = "http://lastodyssey.mybluemix.net/shareLibStatus/";
    public static String getMyLibStatusUrl = "http://lastodyssey.mybluemix.net/myLibStatus/";
    
    public static String userName = "1";
    public static String userID = "XXX";
    
    
    // Sentencias SQL
    
    public static String SQLInsertLocalUser = "insert into localUsers(userID,userName,localUpdateAvaible,localUpdateAvaibletoServer) values(?,?,'0','0')";
    
    public static String SQLInsertIntoP1 = "insert into user_";
    public static String SQLInsertIntoP2 = "_Lib(name,artist,album,genre,anno,media,duration,fileSize) values (?,?,?,?,?,?,?,?)";
    
    public static String SQLSelectAllP1 = "select mp3ID,name,artist,album,genre,anno from user_";
    public static String SQLSelectAllP2 = "_Lib order by mp3ID";
    
    public static String SQLSelectAllInFileP1 = "select mp3ID,name,artist,album,genre,anno,media,fileSize,Duration from user_";
    public static String SQLSelectAllInFileP2 = "_Lib order by mp3ID";
    
    public static String SQLSelectP1 = "select mp3ID,name,artist,album,genre,anno,duration,fileSize from user_";
    public static String SQLSelectP2 = "_Lib order by mp3ID";
    
    public static String SQLSelectBackupP1 = "select name,artist,album,genre,anno from user_";
    public static String SQLSelectBackupP2 = "_MetaBackup order by mp3ID";
    
    public static String SQLUpdateP1 = "update user_";
    public static String SQLUpdateP2 = "_Lib set(name,artist,album,genre,anno) = (?,?,?,?,?) where mp3ID=?";
    
    public static String SQLSelectMediaP1 = "select media from user_";
    public static String SQLSelectMediaP2 = "_Lib where mp3ID=?";
    
    public static String SQLSelectStatus = "select localUpdateAvaible from localUsers where userID=?";
    
    public static String SQLSelectStatusToServer = "select localUpdateAvaibletoServer from localUsers where userID=?";
    
    public static String SQLUpdateLocalLibStatus = "update localUsers set localUpdateAvaible=? where userID=?";
    
    public static String SQLUpdateLocalLibStatusToServer = "update localUsers set localUpdateAvaibletoServer=? where userID=?";
    
    public static String SQLCreateUserTableP1 = 
            "create table user_";
    public static String SQLCreateUserTableP2 = 
            "_Lib(mp3ID int not null GENERATED ALWAYS AS IDENTITY (START WITH 1 INCREMENT BY 1 MINVALUE 1 NO MAXVALUE NO CYCLE NO CACHE ORDER), name varchar(250) not null, artist varchar(100), album varchar(100), genre varchar(100), anno varchar(100), letter blob(500K), media blob(2M), duration int,fileSize int,primary key(mp3ID))";
 
    public static String SQLCreateMetaBackupP1 = 
            "create table user_";
    public static String SQLCreateMetaBackupP2 = 
            "_MetaBackup(mp3ID int not null, name varchar(250) not null, artist varchar(100), album varchar(100), genre varchar(100), anno varchar(100), letter blob(500K), primary key(mp3ID))";
    
    public static String SQLCreateTrigger1P1 =
            "CREATE TRIGGER insertBackupUser_"; 
    public static String SQLCreateTrigger1P2 = 
            " AFTER INSERT ON user_"; 
    public static String SQLCreateTrigger1P3 = 
            "_Lib REFERENCING NEW AS NEWROW OLD AS OLDROW FOR EACH ROW BEGIN INSERT INTO user_";
    public static String SQLCreateTrigger1P4 = 
            "_MetaBackup(mp3ID, name,artist,album,genre,anno,letter) VALUES(NEWROW.mp3ID,NEWROW.name,NEWROW.artist,NEWROW.album,NEWROW.genre,NEWROW.anno,NEWROW.letter); END";
    
    public static String SQLCreateTrigger2P1 =
            "CREATE TRIGGER doBackupUser_"; 
    public static String SQLCreateTrigger2P2 = 
            " NO CASCADE BEFORE UPDATE ON user_"; 
    public static String SQLCreateTrigger2P3 = 
            "_Lib REFERENCING NEW AS NEWROW OLD AS OLDROW FOR EACH ROW BEGIN UPDATE user_";
    public static String SQLCreateTrigger2P4 = 
            "_MetaBackup SET(name,artist,album,genre,anno,letter) = (OLDROW.name,OLDROW.artist,OLDROW.album,OLDROW.genre,OLDROW.anno,OLDROW.letter) WHERE mp3ID=OLDROW.mp3ID; END";
    
    public static String SQLGetLocalLibCountP1 = "select mp3ID from user_";
    public static String SQLGetLocalLibCountP2 = "_Lib order by mp3ID";
    
    
    public static synchronized String getSelectedLib() {
        return selectedLib;
    }
    
}
