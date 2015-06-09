/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nanook;

import java.io.File;
import java.util.LinkedList;

/**
 *
 * @author leggettr
 */
public class SystemCommandRunnable implements Runnable {
    NanoOKOptions options;
    private String message;
    private String command;
    private String logFile;
    private String outFile;
    
    /**
     * Constructor
     * @param o program options
     */
    public SystemCommandRunnable(NanoOKOptions ops, String msg, String com, String out, String log) {    
        options = ops;
        message = msg;
        command = com;
        logFile = log;
        outFile = out;
    }
            
    private void runCommandLSF(String command, String outPath, String log) {
        // outPath only non-null if aligner will only write to screen (yes, BWA, I'm talking about you)
        if (outPath != null) {
             command = command + " > " + outPath;    
        }        
        
        // Make the LSF command
        String lsfCommand = "bsub -n " + options.getNumberOfThreads() + " -q " + options.getQueue() + " -oo " + log + " -R \"rusage[mem=8000] span[hosts=1]\" \"" + command + "\"";
        System.out.println(command);
        //pl = new ProcessLogger();
        //response = pl.getCommandOutput(lsfCommand, true, true);                
    }
    
    private void runCommandLocal(String command, String outPath) {
        ProcessLogger pl = new ProcessLogger();
        
        // outPath only non-null if aligner will only write to screen (yes, BWA, I'm talking about you)
        if (outPath != null) {
            pl.setWriteFormat(false, true, false);
            pl.runAndLogCommand(command, outPath, false);
        } else {
            pl.runCommand(command);
        }
    }
    
    /**
     * Run the alignment command
     * @param command
     * @param outPath
     * @param log 
     */
    private void runCommand(String command, String outPath, String log) {        
        switch(options.getScheduler()) {
            case "screen":
                System.out.println(command);
                break;
            case "lsf":
                runCommandLSF(command, outPath, log);
                break;
            case "system":
                runCommandLocal(command, outPath);
                break;
            default:
                System.out.println("Error: scheduler " + options.getScheduler() + " not recognised.");
                System.exit(1);
                break;
        }
    }
        
    public void run() {
        if (message != null) {
            System.out.println(message);
        }
        
        runCommand(command, outFile, logFile);
    }         
}
