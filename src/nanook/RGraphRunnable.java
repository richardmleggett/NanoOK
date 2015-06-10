/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nanook;

import java.io.File;
import java.util.List;

/**
 *
 * @author leggettr
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
