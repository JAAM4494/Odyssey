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
public class LocalSyncThread extends Observable implements Runnable {

    public Thread newThread;
    private String threadName;
    boolean suspended = false;
    boolean stopThread = false;

    public LocalSyncThread(String pName) {
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
                if (Constants.selectedLib.equals("MyOdyssey-Lib")) {
                    LibrariesComm communication = new LibrariesComm();
                    boolean updateAvaible = communication.getLibrariesStatus(0);

                    if (updateAvaible) {
                        communication.setLocalLibStatus("0",0);
                        setChanged();
                        notifyObservers();
                    }
                }

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

    public void start() {
        if (newThread == null) {
            newThread = new Thread(this, threadName);
            newThread.start();
        }
    }

    public void suspend() {
        System.out.println("Pause...");
        suspended = true;
    }

    public synchronized void resume() {
        System.out.println("Resuming...");
        suspended = false;
        notify();
    }

    public void stop() {
        stopThread = true;
    }
    
    
    /*
    public static void main(String args[]) {

        SyncThread R1 = new SyncThread("Hilo1");
        R1.start();

        try {
            Thread.sleep(5000);
            R1.suspend();
            Thread.sleep(5000);
            R1.resume();
            Thread.sleep(5000);
            R1.stop();
        } catch (InterruptedException e) {
            System.out.println("Main thread Interrupted");
        }

        /*
         try {
         System.out.println("Waiting for threads to finish.");
         R1.newThread.join();
         } catch (InterruptedException e) {
         System.out.println("Main thread Interrupted");
         }
    }
    */

}
