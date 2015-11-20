/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package odyssey;

import com.google.common.io.ByteStreams;
import com.sun.org.apache.xerces.internal.impl.dv.util.Base64;
import java.io.IOException;
import java.io.InputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author jaam
 */
public class CloudSyncThread extends Observable implements Runnable {

    public Thread newThread;
    private String threadName;
    private boolean suspended = false;
    private boolean stopThread = false;

    public CloudSyncThread(String pName) {
        threadName = pName;
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (stopThread) {
                    break;
                }
                /////// Thread action
                Thread.sleep(10000);
                LibrariesComm communication = new LibrariesComm();
                boolean updateAvaible = communication.getLibrariesStatus(1, 0, "LocalUser");

                runAux(updateAvaible);

            }
        } catch (InterruptedException e) {
            System.out.println("Thread " + threadName + " interrupted.");
        }
    }

    private void runAux(boolean localUpdateAvaible) {
        LibrariesComm communication = new LibrariesComm();
        //communication.setLocalLibStatus("0", 0);
        //setChanged();
        //notifyObservers();

        boolean cloudLibUpdateAvaible = communication.getLibrariesStatus(1, 1, Constants.userName);
        // tomar acciones según las actualizaciones disponibles

        /////////////////////////
        if (localUpdateAvaible & !cloudLibUpdateAvaible) {
            //synchronizing = true;
            ArrayList localIDs = communication.getUsersIDLib(0);
            ArrayList cloudIDs = communication.getUsersIDLib(1);

            if (localIDs.size() == cloudIDs.size()) {
                this.uploadAllFiles(communication.getAllFilesResultSet());
                System.out.println("!Actualizacion contra cloud hecha!");
            }
            if (localIDs.size() > cloudIDs.size()) {
                this.uploadFiles(communication.getAllInFilesResultSet(), cloudIDs);
                System.out.println("!Actualizacion contra cloud hecha!");
            }
        }
        ///////////////////////////
        if (!localUpdateAvaible & cloudLibUpdateAvaible) {
 

        }
        //////////////////////////
        if (localUpdateAvaible & cloudLibUpdateAvaible) {


        }
    }
    
    private void uploadFiles(ResultSet pRs,ArrayList pCloudIDs) {
        LibrariesComm communication = new LibrariesComm();

        try {
            while (pRs.next()) {
                
                ArrayList tagNames = new ArrayList();
                ArrayList values = new ArrayList();

                tagNames.add("userName");
                tagNames.add("mp3ID");
                tagNames.add("name");
                tagNames.add("artist");
                tagNames.add("album");
                tagNames.add("genre");
                tagNames.add("anno");
                
                values.add(Constants.userName);
                values.add(pRs.getInt("mp3ID"));
                values.add(pRs.getString("name"));
                values.add(pRs.getString("artist"));
                values.add(pRs.getString("album"));
                values.add(pRs.getString("genre"));
                values.add(pRs.getString("anno"));
                
                if(pCloudIDs.contains(pRs.getInt("mp3ID"))) {
                    tagNames.add("operation");
                    values.add("update");
                } else {
                    tagNames.add("operation");
                    tagNames.add("media");
                    tagNames.add("fileSize");
                    tagNames.add("duration");
    
                    InputStream input = pRs.getBinaryStream("media");
                    byte[] bytes = ByteStreams.toByteArray(input);
                    String blobInString = Base64.encode(bytes);
                    
                    values.add("insert");
                    values.add(blobInString);
                    values.add(pRs.getInt("fileSize"));
                    values.add(pRs.getInt("duration"));
                }
                
                HttpRequest request = new HttpRequest();
                request.postRequest(Constants.uploadUrl, values, tagNames);
                
                Thread.sleep(300);
                synchronized (this) {
                    while (suspended) {
                        wait();
                    }
                }
            }
            
            communication.setLocalLibStatus("0", 1);
            
        } catch (SQLException ex) {
            Logger.getLogger(CloudSyncThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(CloudSyncThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(CloudSyncThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private boolean contains(ArrayList pList, int pSearch) {
        boolean retVal = false;
        
        for (int i = 0; i < pList.size(); i++) {
            if(pSearch == (int)pList.get(i)) {
                retVal = true;
                break;
            }
        }
        
        return retVal;
    }

    private void uploadAllFiles(ResultSet pRs) {
        LibrariesComm communication = new LibrariesComm();
        try {
            while (pRs.next()) {
                
                ArrayList tagNames = new ArrayList();
                ArrayList values = new ArrayList();

                tagNames.add("userName");
                tagNames.add("mp3ID");
                tagNames.add("name");
                tagNames.add("artist");
                tagNames.add("album");
                tagNames.add("genre");
                tagNames.add("anno");
                tagNames.add("operation");

                values.add(Constants.userName);
                values.add(pRs.getInt("mp3ID"));
                values.add(pRs.getString("name"));
                values.add(pRs.getString("artist"));
                values.add(pRs.getString("album"));
                values.add(pRs.getString("genre"));
                values.add(pRs.getString("anno"));
                values.add("update");

                HttpRequest request = new HttpRequest();
                request.postRequest(Constants.uploadUrl, values, tagNames);
                
                Thread.sleep(300);
                synchronized (this) {
                    while (suspended) {
                        wait();
                    }
                }
            }
            
            communication.setLocalLibStatus("0", 1);
            
        } catch (SQLException ex) {
            Logger.getLogger(CloudSyncThread.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(CloudSyncThread.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void start() {
        if (newThread == null) {
            newThread = new Thread(this, threadName);
            newThread.start();
        }
    }

    public void suspend() {
        System.out.println("!CloudSync Paused!");
        suspended = true;
    }

    public synchronized void resume() {
        System.out.println("!CloudSync Resumed!");
        suspended = false;
        notify();
    }

    public void stop() {
        stopThread = true;
    }
}
