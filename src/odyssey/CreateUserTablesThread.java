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
public class CreateUserTablesThread extends Observable implements Runnable {

    public Thread newThread;
    private String threadName;
    private boolean suspended = false;
    private boolean stopThread = false;

    private String ResultCode;

    public CreateUserTablesThread(String pName) {
        threadName = pName;
        ResultCode = "-1";
    }

    @Override
    public void run() {
        try {
            /////// Thread action

            if (ResultCode.equals("1")) {
                // Verificar si el usuario ya esta en la base local y sino esperar
                // que el usuario decida descargar la biblioteca
                System.out.println("Goooddd");
            } else {
                // crear tablas relacionadas al usuario
                LibrariesComm comm2 = new LibrariesComm();
                comm2.createUserTables();
            }

            //communication.setLocalLibStatus("0", 0);
            //setChanged();
            //notifyObservers();
            ////////////////////
            Thread.sleep(300);
            synchronized (this) {
                while (suspended) {
                    wait();
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

    public String getResultCode() {
        return ResultCode;
    }

    public void setResultCode(String ResultCode) {
        this.ResultCode = ResultCode;
    }
}
