/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package odyssey;

import com.google.common.io.ByteStreams;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ProtocolException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javazoom.jl.decoder.JavaLayerException;
import javazoom.jl.player.advanced.AdvancedPlayer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tritonus.share.sampled.file.TAudioFileFormat;
import sun.misc.IOUtils;

/**
 *
 * @author jaam
 */
public class LibrariesComm {

    private Connection connection;

    public LibrariesComm() {
    }
    
    public void createUserTables() {
        getConnection();
        PreparedStatement myStmt = null;
        
        String sql = Constants.SQLCreateUserTableP1 + Constants.userID + Constants.SQLCreateUserTableP2;
        
        try {
            myStmt = connection.prepareStatement(sql);
            
            myStmt.executeUpdate();
            connection.commit();

            System.out.println("¡Completed successfully!");
        } catch (SQLException ex) {
            Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        closeConnection(connection, myStmt);
    }

    public void setLocalLibStatus(String pNewStatus, int pOption) {
        getConnection();
        PreparedStatement myStmt = null;
        
        String sql = "";
        
        if(pOption == 0) 
            sql = Constants.SQLUpdateLocalLibStatus;
        else
            sql = Constants.SQLUpdateLocalLibStatusToServer;

        try {
            myStmt = connection.prepareStatement(sql);
            
            myStmt.setString(1, pNewStatus);
            myStmt.setString(2, Constants.userName); //  *********************** DEBE SER Constants.userID

            myStmt.executeUpdate();
            connection.commit();

            System.out.println("¡Completed successfully!");
        } catch (SQLException ex) {
            Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
        }

        closeConnection(connection, myStmt);
    }

    public boolean getLibrariesStatus(int pOption) {
        boolean returnVal = false;

        getConnection();
        PreparedStatement myStmt;
        ResultSet myRs = null;
        
        String sql = "";
        
        if(pOption == 0) 
            sql = Constants.SQLSelectStatus;
        else
            sql = Constants.SQLUpdateLocalLibStatusToServer;

        try {
            myStmt = connection.prepareStatement(sql);
            myStmt.setString(1, Constants.userName);     //  *********************** DEBE SER Constants.userID

            myRs = myStmt.executeQuery();

            String status = "";
            while (myRs.next()) {
                status = myRs.getString("localUpdateAvaible");
            }
            if (status.equals("1")) {
                returnVal = true;
            }
        } catch (SQLException ex) {
            Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
        }

        return returnVal;
    }

    public void setShareLib(String pUser) {
        ArrayList tagNames = new ArrayList();
        ArrayList tagValues = new ArrayList();
        tagNames.add("userName");
        tagValues.add(pUser);

        HttpRequest request = new HttpRequest();
        request.postRequest(Constants.shareMyLibUrl, tagValues, tagNames);
    }

    public ArrayList getLibraryContent(int pOption, String pUserNameThatShare) {
        ArrayList retList = new ArrayList();

        if (pOption == 0) {
            getConnection();

            PreparedStatement myStmt;
            ResultSet myRs = null;

            String sql = Constants.SQLSelectP1 + Constants.userID + Constants.SQLSelectP2;

            try {
                myStmt = connection.prepareStatement(sql);
                myRs = myStmt.executeQuery();

                Mp3File tempFile;
                while (myRs.next()) {
                    tempFile = new Mp3File();
                    tempFile.setID(myRs.getString("mp3ID"));
                    tempFile.setName(myRs.getString("name"));
                    tempFile.setArtist(myRs.getString("artist"));
                    tempFile.setAlbum(myRs.getString("album"));
                    tempFile.setGenre(myRs.getString("genre"));
                    tempFile.setAnno(myRs.getString("anno"));
                    tempFile.setDuration(myRs.getInt("duration"));
                    tempFile.setLengthBytes(myRs.getInt("fileSize"));
                    retList.add(tempFile);
                }
            } catch (SQLException ex) {
                Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            HttpRequest request = new HttpRequest();
            String response = request.getRequest(Constants.sharedLibContentUrl + pUserNameThatShare);
            //System.out.println(response);
            try {
                JSONObject jsonInput = new JSONObject(response);
                JSONArray mainArray = jsonInput.getJSONArray("data");

                for (int i = 0; i < mainArray.length(); i++) {
                    JSONArray tempArray = mainArray.getJSONArray(i);
                    Mp3File tempFile = new Mp3File();
                    for (int j = 0; j < tempArray.length(); j++) {
                        if (j == 0) {
                            tempFile.setName(tempArray.getString(j));
                        }
                        if (j == 1) {
                            tempFile.setArtist(tempArray.getString(j));
                        }
                        if (j == 2) {
                            tempFile.setAlbum(tempArray.getString(j));
                        }
                        if (j == 3) {
                            tempFile.setGenre(tempArray.getString(j));
                        }
                        if (j == 4) {
                            tempFile.setAnno(tempArray.getString(j));
                        }
                    }
                    retList.add(tempFile);
                }
            } catch (JSONException ex) {
                Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return retList;
    }

    public ArrayList getSharedLibs() {
        ArrayList retList = new ArrayList();
        JSONObject jsonInput = null;

        HttpRequest request = new HttpRequest();
        String outputFromServer = request.getRequest(Constants.shareLibsUrl + Constants.userName);

        if (outputFromServer.equals("-1")) {
            System.out.println("OdysseyCloud Unavaible");
        } else {
            JSONArray userNamesArray;
            JSONArray userIDsArray;
            try {
                jsonInput = new JSONObject(outputFromServer);

                userNamesArray = jsonInput.getJSONArray("userNames");
                userIDsArray = jsonInput.getJSONArray("idUsers");

                for (int i = 0; i < userNamesArray.length(); i++) {
                    UserDetails tempUser = new UserDetails();
                    tempUser.setUserName(userNamesArray.getString(i));
                    tempUser.setUserID(userIDsArray.getString(i));
                    retList.add(tempUser);
                }

            } catch (JSONException ex) {
                Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return retList;
    }

    public void updateMp3File(Mp3File pFileToUpdate) {
        getConnection();
        PreparedStatement myStmt = null;

        String sql = Constants.SQLUpdateP1 + Constants.userID + Constants.SQLUpdateP2;

        try {
            myStmt = connection.prepareStatement(sql);

            myStmt.setString(1, pFileToUpdate.getName());
            myStmt.setString(2, pFileToUpdate.getArtist());
            myStmt.setString(3, pFileToUpdate.getAlbum());
            myStmt.setString(4, pFileToUpdate.getGenre());
            myStmt.setString(5, pFileToUpdate.getAnno());

            myStmt.setString(6, pFileToUpdate.getID());

            myStmt.executeUpdate();
            connection.commit();

            System.out.println("¡Completed successfully!");
        } catch (SQLException ex) {
            Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
        }

        closeConnection(connection, myStmt);
        
        // se setea que hay disponible actualizacion
        this.setLocalLibStatus("1",0);
        this.setLocalLibStatus("1",1);
    }

    public byte[] getMp3ToPlay(int pMp3ID) {
        getConnection();

        PreparedStatement myStmt;
        ResultSet myRs = null;
        InputStream input = null;
        byte[] bytes = null;

        String sql = Constants.SQLSelectMediaP1 + Constants.userID + Constants.SQLSelectMediaP2;

        try {
            myStmt = connection.prepareStatement(sql);
            myStmt.setInt(1, pMp3ID);
            myRs = myStmt.executeQuery();

            while (myRs.next()) {
                input = myRs.getBinaryStream("media");
                bytes = ByteStreams.toByteArray(input);
            }
        } catch (SQLException ex) {
            Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("\nCompleted successfully!");

        return bytes;
    }

    public void syncMp3FilesWithCloud() {
        getConnection();

        Statement myStmt;
        ResultSet myRs = null;
        InputStream input = null;

        String sql = "select * from mp3Files"; // ******************** falta cambiar
        try {
            myStmt = connection.createStatement();
            myRs = myStmt.executeQuery(sql);

            while (myRs.next()) {

                input = myRs.getBinaryStream("media");
                byte[] bytes = ByteStreams.toByteArray(input);
                String blobInString = Base64.encode(bytes);
                //System.out.println(myString);
                //exportFile(myString, "Prueba.mp3");

                ArrayList tagNames = new ArrayList();
                ArrayList values = new ArrayList();

                tagNames.add("userName");
                tagNames.add("name");
                tagNames.add("artist");
                tagNames.add("album");
                tagNames.add("genre");
                tagNames.add("anno");
                tagNames.add("media");
                tagNames.add("duration");
                tagNames.add("fileSize");

                values.add(Constants.userName);
                values.add(myRs.getString("name"));
                values.add(myRs.getString("artist"));
                values.add(myRs.getString("album"));
                values.add(myRs.getString("genre"));
                values.add(myRs.getString("anno"));
                values.add(blobInString);
                values.add(myRs.getInt("duration"));
                values.add(myRs.getInt("fileSize"));

                HttpRequest request = new HttpRequest();
                String rr = request.postRequest(Constants.uploadUrl, values, tagNames);
                System.out.println(rr);
            }
            System.out.println("\nCompleted successfully!");
        } catch (SQLException ex) {
            Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //Convert a Base64 string and create a file
    private static void exportFile(String file_string, String file_name) throws IOException {
        byte[] bytes = Base64.decode(file_string);
        File file = new File("/home/jaam/Escritorio/" + file_name);
        FileOutputStream fop = new FileOutputStream(file);
        fop.write(bytes);
        fop.flush();
        fop.close();
    }

    public void addMp3Blobs(ArrayList mp3Files, ArrayList mp3FilesNames) {

        getConnection();
        PreparedStatement myStmt = null;

        for (int i = 0; i < mp3Files.size(); i++) {

            String sql = Constants.SQLInsertIntoP1 + Constants.userID + Constants.SQLInsertIntoP2;

            File blobFile = new File((String) mp3Files.get(i));
            ArrayList metadataFile = obtainMetaDataFromMp3File(blobFile);

            InputStream in;
            try {
                myStmt = connection.prepareStatement(sql);

                in = new FileInputStream(blobFile);

                myStmt.setString(1, (String) mp3FilesNames.get(i));
                myStmt.setString(2, (String) metadataFile.get(0));
                myStmt.setString(3, (String) metadataFile.get(1));
                myStmt.setString(4, (String) metadataFile.get(2));
                myStmt.setString(5, (String) metadataFile.get(3));
                myStmt.setBinaryStream(6, in, (int) blobFile.length());
                myStmt.setInt(7, (int) metadataFile.get(4));
                myStmt.setInt(8, (int) metadataFile.get(5));

                myStmt.executeUpdate();
                connection.commit();

                System.out.println("¡Completed successfully!");
            } catch (FileNotFoundException | SQLException ex) {
                Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        closeConnection(connection, myStmt);
    }

    private ArrayList obtainMetaDataFromMp3File(File pFile) {
        ArrayList metadataList = new ArrayList();
        AudioFileFormat baseFileFormat = null;

        try {
            baseFileFormat = AudioSystem.getAudioFileFormat(pFile);
        } catch (UnsupportedAudioFileException | IOException ex) {
            Logger.getLogger(CustomPlayer.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (baseFileFormat instanceof TAudioFileFormat) {
            Map properties = ((TAudioFileFormat) baseFileFormat).properties();
            System.out.println(properties.toString());
            System.out.printf("FileSize: %d\n", pFile.length());
            if (properties.containsKey("author")) {
                metadataList.add(properties.get("author"));
            } else {
                metadataList.add("Unknow");
            }
            if (properties.containsKey("album")) {
                metadataList.add(properties.get("album"));
            } else {
                metadataList.add("Unknow");
            }
            if (properties.containsKey("mp3.id3tag.genre")) {
                metadataList.add(properties.get("mp3.id3tag.genre"));
            } else {
                metadataList.add("Unknow");
            }
            if (properties.containsKey("date")) {
                metadataList.add(properties.get("date"));
            } else {
                metadataList.add("Unknow");
            }
            if (properties.containsKey("duration")) {
                Long tmpDur = (long) properties.get("duration");
                metadataList.add(tmpDur.intValue() / 1000000);
            } else {
                metadataList.add(1);
            }
            Long tmpLenght = (long) pFile.length();
            metadataList.add(tmpLenght.intValue());
        }

        return metadataList;
    }

    private void getConnection() {
        String jdbcClassName = "com.ibm.db2.jcc.DB2Driver";
        String url = "jdbc:db2://localhost:50001/odylocal";
        String user = "db2inst1";
        String password = "program44";

        try {
            Class.forName(jdbcClassName);
            connection = DriverManager.getConnection(url, user, password);
        } catch (ClassNotFoundException | SQLException ex) {
            Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void closeConnection(Connection myConn, Statement myStmt) {

        if (myStmt != null) {
            try {
                myStmt.close();
            } catch (SQLException ex) {
                Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        if (myConn != null) {
            try {
                myConn.close();
            } catch (SQLException ex) {
                Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
