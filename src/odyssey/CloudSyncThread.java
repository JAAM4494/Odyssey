/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package odyssey;

import java.util.Observable;

/**
 *
 * @author jaam
 */
public class CloudSyncThread extends Observable implements Runnable {

    public Thread newThread;
    private String threadName;
    private boolean suspended = false;
    private boolean stopThread = false;
    private boolean synchronizing = false;
    private boolean trySuspend = false;

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

                //System.out.println("Thread: " + threadName + "running");
                Thread.sleep(5000);
                LibrariesComm communication = new LibrariesComm();
                boolean updateAvaible = communication.getLibrariesStatus(1);

                if (updateAvaible) {
                    runAux();
                    //communication.setLocalLibStatus("0", 0);
                    //setChanged();
                    //notifyObservers();
                }
                
                if(!synchronizing)
                    suspended = true;

                ////////////////////
                Thread.sleep(300);
                synchronized (this) {
                    while (suspended) {
                        wait();
                    }
                }
            }
        } catch (InterruptedException e) {
            System.out.println("Thread " + threadName + " interrupted.");
        }
    }
    
    private void runAux() {
        synchronizing = true;
        
        LibrariesComm communication = new LibrariesComm();
        communication.syncMp3FilesWithCloud();
        
        communication.setLocalLibStatus("0", 1);
        synchronizing = false;
    }

    public void start() {
        if (newThread == null) {
            newThread = new Thread(this, threadName);
            newThread.start();
        }
    }

    public void suspend() {
        System.out.println("Pause...");
        //suspended = true;
        trySuspend = true;
    }

    public synchronized void resume() {
        System.out.println("Resuming...");
        suspended = false;
        trySuspend = false;
        notify();
    }

    public void stop() {
        stopThread = true;
    }
}
