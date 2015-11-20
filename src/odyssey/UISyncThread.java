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
public class UISyncThread extends Observable implements Runnable {

    private Thread newThread;
    private String threadName;
    boolean suspended = false;
    boolean stopThread = false;
    
    public UISyncThread(String pName) {
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

                Thread.sleep(5000);
                if (Constants.selectedLib.equals("MyOdyssey-Lib")) {
                    LibrariesComm communication = new LibrariesComm();
                    boolean updateAvaible = communication.getLibrariesStatus(0, 0,Constants.actualUser);

                    if (updateAvaible) {
                        communication.setLocalLibStatus("0", 0);
                        setChanged();
                        notifyObservers("local");
                    }
                } else {
                    // cuando la biblioteca seleccionada es compartida y no la local
                    LibrariesComm communication = new LibrariesComm();
                    boolean updateAvaible = communication.getLibrariesStatus(0, 1,Constants.actualUser);

                    if (updateAvaible) {
                        setChanged();
                        notifyObservers("share");
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
        System.out.println("!UISync paused!");
        suspended = true;
    }

    public synchronized void resume() {
        System.out.println("!UISync resumed!");
        suspended = false;
        notify();
    }

    public void stop() {
        stopThread = true;
    }
}
