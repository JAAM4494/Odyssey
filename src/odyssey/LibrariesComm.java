/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package odyssey;

import com.google.common.io.ByteStreams;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.tritonus.share.sampled.file.TAudioFileFormat;

/**
 *
 * @author jaam
 */
public class LibrariesComm {

    private Connection connection;

    public LibrariesComm() {
    }

    public ResultSet getAllInFilesResultSet() {
        getConnection();

        Statement myStmt;
        ResultSet myRs = null;

        String sql = Constants.SQLSelectAllInFileP1 + Constants.userID + Constants.SQLSelectAllInFileP2;
        try {
            myStmt = connection.createStatement();
            myRs = myStmt.executeQuery(sql);
        } catch (SQLException ex) {
            Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
        }

        return myRs;
    }

    public ResultSet getAllFilesResultSet() {
        getConnection();

        Statement myStmt;
        ResultSet myRs = null;

        String sql = Constants.SQLSelectAllP1 + Constants.userID + Constants.SQLSelectAllP2;
        try {
            myStmt = connection.createStatement();
            myRs = myStmt.executeQuery(sql);
        } catch (SQLException ex) {
            Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
        }

        return myRs;
    }

    public ArrayList getUsersIDLib(int pOption) {
        ArrayList retList = new ArrayList();

        if (pOption == 0) {
            getConnection();

            ResultSet myRs = null;
            PreparedStatement myStmt = null;

            String sql = Constants.SQLGetLocalLibCountP1 + Constants.userID + Constants.SQLGetLocalLibCountP2;

            try {
                myStmt = connection.prepareStatement(sql);
                myRs = myStmt.executeQuery();

                while (myRs.next()) {
                    retList.add(myRs.getInt("mp3ID"));
                }
            } catch (SQLException ex) {
                Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
            }
            closeConnection(connection, myStmt);
        } else {
            HttpRequest request = new HttpRequest();
            String result = request.getRequest(Constants.getCloudLibCountUrl + Constants.userName);

            try {
                JSONObject json = new JSONObject(result);
                JSONArray tempArray = null;

                if (!json.getString("result").equals("0")) {
                    tempArray = json.getJSONArray("data");
                    for (int i = 0; i < tempArray.length(); i++) {
                        retList.add(tempArray.getInt(i));
                    }
                }
            } catch (JSONException ex) {
                Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return retList;
    }

    public void restoreMetaData(Mp3File pFileToUpdate) {
        getConnection();
        PreparedStatement myStmt = null;

        // Los datos se obtienen del Mp3File
        String sql = Constants.SQLUpdateP1 + Constants.userID + Constants.SQLUpdateP2;

        try {
            myStmt = connection.prepareStatement(sql);

            myStmt.setString(1, pFileToUpdate.getNameBackup());
            myStmt.setString(2, pFileToUpdate.getArtistBackup());
            myStmt.setString(3, pFileToUpdate.getAlbumBackup());
            myStmt.setString(4, pFileToUpdate.getGenreBackup());
            myStmt.setString(5, pFileToUpdate.getAnnoBackup());

            myStmt.setString(6, pFileToUpdate.getID());

            myStmt.executeUpdate();
            connection.commit();

            System.out.println("¡Metadata restaturada!");
        } catch (SQLException ex) {
            Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
        }

        closeConnection(connection, myStmt);

        // se setea que hay disponible actualizacion
        this.setLocalLibStatus("1", 0);
        this.setLocalLibStatus("1", 1);
    }

    public void createUserTables() {
        getConnection();
        PreparedStatement myStmt = null;

        String sql = "";
        try {
            // inserta al usuario en la tabla de usuarios local
            sql = Constants.SQLInsertLocalUser;
            myStmt = connection.prepareStatement(sql);
            myStmt.setString(1, Constants.userID);
            myStmt.setString(2, Constants.userName);
            myStmt.executeUpdate();
            connection.commit();

            // crea la tabla de biblioteca
            sql = Constants.SQLCreateUserTableP1 + Constants.userID + Constants.SQLCreateUserTableP2;
            myStmt = connection.prepareStatement(sql);
            myStmt.executeUpdate();
            connection.commit();

            // crea la tabla metadata
            sql = Constants.SQLCreateMetaBackupP1 + Constants.userID + Constants.SQLCreateMetaBackupP2;
            myStmt = connection.prepareStatement(sql);
            myStmt.executeUpdate();
            connection.commit();

            // crea el trigger doInsert
            sql = Constants.SQLCreateTrigger1P1 + Constants.userID + Constants.SQLCreateTrigger1P2
                    + Constants.userID + Constants.SQLCreateTrigger1P3 + Constants.userID + Constants.SQLCreateTrigger1P4;
            myStmt = connection.prepareStatement(sql);
            myStmt.executeUpdate();
            connection.commit();

            // crea el trigger doBackup
            sql = Constants.SQLCreateTrigger2P1 + Constants.userID + Constants.SQLCreateTrigger2P2
                    + Constants.userID + Constants.SQLCreateTrigger2P3 + Constants.userID + Constants.SQLCreateTrigger2P4;
            myStmt = connection.prepareStatement(sql);
            myStmt.executeUpdate();
            connection.commit();

            System.out.println("¡Tablas de usuario creadas!");
        } catch (SQLException ex) {
            Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
        }

        closeConnection(connection, myStmt);
    }

    public void setLocalLibStatus(String pNewStatus, int pOption) {
        getConnection();
        PreparedStatement myStmt = null;

        String sql = "";

        if (pOption == 0) {
            sql = Constants.SQLUpdateLocalLibStatus;
        } else {
            sql = Constants.SQLUpdateLocalLibStatusToServer;
        }

        try {
            myStmt = connection.prepareStatement(sql);

            myStmt.setString(1, pNewStatus);
            myStmt.setString(2, Constants.userID);

            myStmt.executeUpdate();
            connection.commit();

            System.out.println("¡Status biblioteca local seteado!");
        } catch (SQLException ex) {
            Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
        }

        closeConnection(connection, myStmt);
    }

    public boolean getLibrariesStatus(int pOption, int pLibrary, String pUserBoss) {
        boolean returnVal = false;

        if (pLibrary == 0) {
            getConnection();
            PreparedStatement myStmt;
            ResultSet myRs = null;

            String sql = "";

            if (pOption == 0) {
                sql = Constants.SQLSelectStatus;
            } else {
                sql = Constants.SQLSelectStatusToServer;
            }

            try {
                myStmt = connection.prepareStatement(sql);
                myStmt.setString(1, Constants.userID);     //  *********************** DEBE SER Constants.userID

                myRs = myStmt.executeQuery();

                String status = "";
                while (myRs.next()) {
                    if (pOption == 0) {
                        status = myRs.getString("localUpdateAvaible");
                    } else {
                        status = myRs.getString("localUpdateAvaibletoServer");
                    };
                }
                if (status.equals("1")) {
                    returnVal = true;
                }

            } catch (SQLException ex) {
                Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            HttpRequest request = new HttpRequest();
            String result = "";

            if (pOption == 0) {
                result = request.getRequest(Constants.getShareLibStatusUrl + pUserBoss + "/" + Constants.userName);
            } else {
                result = request.getRequest(Constants.getMyLibStatusUrl + Constants.userName);
            }

            try {
                JSONObject json = new JSONObject(result);
                String libStatus = json.getString("result");

                if (libStatus.equals("1")) {
                    returnVal = true;
                }

            } catch (JSONException ex) {
                Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return returnVal;
    }

    public String setShareLib(String pUser) {
        ArrayList tagNames = new ArrayList();
        ArrayList tagValues = new ArrayList();
        tagNames.add("userId");
        tagNames.add("userName");
        tagNames.add("usertoShare");
        tagValues.add(Constants.userID);
        tagValues.add(Constants.userName);
        tagValues.add(pUser);

        HttpRequest request = new HttpRequest();
        String result = request.postRequest(Constants.shareMyLibUrl, tagValues, tagNames);

        String retValue = "";
        try {
            JSONObject json = new JSONObject(result);
            retValue = json.getString("result");
        } catch (JSONException ex) {
            Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
        }
        return retValue;
    }

    public ArrayList getLibraryContent(int pOption, String pUserNameThatShare) {
        ArrayList retList = new ArrayList();
        ArrayList retListTemp = new ArrayList();

        if (pOption == 0) {
            getConnection();

            PreparedStatement myStmt;
            ResultSet myRs = null;

            String sql = Constants.SQLSelectP1 + Constants.userID + Constants.SQLSelectP2;

            // Consultar la tabla de Backup
            //getConnection();
            PreparedStatement myStmt2;
            ResultSet myRs2 = null;

            String sql2 = Constants.SQLSelectBackupP1 + Constants.userID + Constants.SQLSelectBackupP2;

            try {
                myStmt = connection.prepareStatement(sql);
                myRs = myStmt.executeQuery();

                myStmt2 = connection.prepareStatement(sql2);
                myRs2 = myStmt2.executeQuery();

                Mp3File tempFile = null;
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

                    retListTemp.add(tempFile);
                }
                Mp3File tempFileAux = null;
                int pos = 0;
                while (myRs2.next()) {
                    tempFileAux = (Mp3File) retListTemp.get(pos);
                    tempFileAux.setNameBackup(myRs2.getString("name"));
                    tempFileAux.setArtistBackup(myRs2.getString("artist"));
                    tempFileAux.setAlbumBackup(myRs2.getString("album"));
                    tempFileAux.setGenreBackup(myRs2.getString("genre"));
                    tempFileAux.setAnnoBackup(myRs2.getString("anno"));

                    retList.add(tempFileAux);
                    pos++;
                }
            } catch (SQLException ex) {
                Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            HttpRequest request = new HttpRequest();
            String response = request.getRequest(Constants.sharedLibContentUrl + pUserNameThatShare);
 
            try {
                JSONObject jsonInput = new JSONObject(response);
                JSONArray mainArray = jsonInput.getJSONArray("data");

                for (int i = 0; i < mainArray.length(); i++) {
                    JSONArray tempArray = mainArray.getJSONArray(i);
                    Mp3File tempFile = new Mp3File();
                    for (int j = 0; j < tempArray.length(); j++) {
                        if (j == 0) {
                            tempFile.setID(tempArray.getString(j));
                        }
                        if (j == 1) {
                            tempFile.setName(tempArray.getString(j));
                        }
                        if (j == 2) {
                            tempFile.setArtist(tempArray.getString(j));
                        }
                        if (j == 3) {
                            tempFile.setAlbum(tempArray.getString(j));
                        }
                        if (j == 4) {
                            tempFile.setGenre(tempArray.getString(j));
                        }
                        if (j == 5) {
                            tempFile.setAnno(tempArray.getString(j));
                        }
                        if (j == 6) {
                            tempFile.setDuration(tempArray.getInt(j));
                        }
                        if (j == 7) {
                            tempFile.setLengthBytes(tempArray.getInt(j));
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
            System.out.println("!OdysseyCloud Unavaible!");
        } else {
            JSONArray userNamesArray;
            JSONArray userIDsArray;
            try {
                jsonInput = new JSONObject(outputFromServer);

                if (!jsonInput.getString("result").equals("0")) {
                    userNamesArray = jsonInput.getJSONArray("userNames");
                    userIDsArray = jsonInput.getJSONArray("idUsers");

                    for (int i = 0; i < userNamesArray.length(); i++) {
                        UserDetails tempUser = new UserDetails();
                        tempUser.setUserName(userNamesArray.getString(i));
                        tempUser.setUserID(userIDsArray.getString(i));
                        retList.add(tempUser);
                    }
                }
            } catch (JSONException ex) {
                Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        return retList;
    }

    public void updateMp3File(Mp3File pFileToUpdate, int pOption, String pUserToModify) {

        if (pOption == 0) {
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

                System.out.println("¡Registro actualizado!");
            } catch (SQLException ex) {
                Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
            }

            closeConnection(connection, myStmt);

            // se setea que hay disponible actualizacion
            this.setLocalLibStatus("1", 0);
            this.setLocalLibStatus("1", 1);
        } else {
            // actualizar una biblioteca compartida
            ArrayList tagNames = new ArrayList();
            ArrayList tagValues = new ArrayList();

            tagNames.add("name");
            tagNames.add("artist");
            tagNames.add("album");
            tagNames.add("genre");
            tagNames.add("anno");

            tagValues.add(pFileToUpdate.getName());
            tagValues.add(pFileToUpdate.getArtist());
            tagValues.add(pFileToUpdate.getAlbum());
            tagValues.add(pFileToUpdate.getGenre());
            tagValues.add(pFileToUpdate.getAnno());

            HttpRequest request = new HttpRequest();
            request.postRequest(Constants.modifMetaUrl + pUserToModify + "/" + pFileToUpdate.getID(), tagValues, tagNames);
        }
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
        System.out.println("!Archivo obtenido para reproducir!");

        return bytes;
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

                System.out.println("!Mp3 agregados!");
            } catch (FileNotFoundException | SQLException ex) {
                Logger.getLogger(LibrariesComm.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        // se setea que hay disponible actualizacion
        this.setLocalLibStatus("1", 0);
        this.setLocalLibStatus("1", 1);

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
