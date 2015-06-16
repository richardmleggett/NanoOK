/*
 * Program: NanoOK
 * Author:  Richard M. Leggett
 * 
 * Copyright 2015 The Genome Analysis Centre (TGAC)
 */

package nanook;

import java.io.File;
import java.util.List;

/**
 * Enable multi-threading of R plotting
 * 
 * @author Richard Leggett
 */
public class RGraphRunnable implements Runnable {
    private String command;
    private String logFilename;
    private List<String> args;
    
    public RGraphRunnable(String cmd, List<String> a, String log) {
        command = cmd;
        args = a;
        logFilename = log;
    }

    public void run() {  
        try {         
            ProcessBuilder pb = new ProcessBuilder(args);
            pb.redirectErrorStream(true);
            pb.redirectOutput(ProcessBuilder.Redirect.appendTo(new File(logFilename)));
            Process p = pb.start();
            p.waitFor();
        } catch (Exception e) {
            System.out.println("RGraphRunnable exception:");
            e.printStackTrace();
            System.exit(1);
        }        
    }
}
