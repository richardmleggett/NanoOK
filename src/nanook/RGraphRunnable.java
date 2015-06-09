/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nanook;

/**
 *
 * @author leggettr
 */
public class RGraphRunnable implements Runnable {
    private String command;
    private String logFilename;
    
    public RGraphRunnable(String cmd, String log) {
        command = cmd;
        logFilename = log;
    }

    public void run() {  
        ProcessLogger pl = new ProcessLogger();
        pl.runAndLogCommand(command, logFilename, true);
    }
}
