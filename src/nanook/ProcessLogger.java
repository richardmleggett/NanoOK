package nanook;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Execute a system process and log result to a file
 * 
 * @author leggettr
 */
public class ProcessLogger {        
    public void runCommand(String command, String logFilename, boolean fAppend) {
        try {         
            PrintWriter pw = new PrintWriter(new FileWriter(logFilename, fAppend)); 
            Process p = Runtime.getRuntime().exec(command);            
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            if (fAppend) {
                pw.println("\n---\n");
            }

            pw.println("Running "+command);

            // read the output from the command
            pw.println("\nStdout:");
            String s = null;
            while ((s = stdInput.readLine()) != null) {
                pw.println(s);
            }

            // read any errors from the attempted command
            pw.println("\nStderr:");
            while ((s = stdError.readLine()) != null) {
                pw.println(s);
            }       
            
            pw.close();
            
            p.waitFor(); 

        } catch (Exception e) {
            System.out.println("ProcessLogger exception:");
            e.printStackTrace();
            System.exit(1);
        }        
    }        
}
