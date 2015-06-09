package nanook;

import java.io.*;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Executes command to plot graphs with R.
 * 
 * @author Richard Leggett
 */
public class RGraphPlotter {
    private ThreadPoolExecutor executor;
    private NanoOKOptions options;
    private String logFilename;
    private long lastCompleted = -1;

    /**
     * Constructor.
     * @param o NanoOKOptions object
     */
    public RGraphPlotter(NanoOKOptions o) {
        options = o;
        logFilename = options.getLogsDir() + File.separator + "R_output_log.txt";
        executor = new ThreadPoolExecutor(options.getNumberOfThreads(), options.getNumberOfThreads(), 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }
    
    public void createLog() {
        try {         
            PrintWriter pw = new PrintWriter(new FileWriter(logFilename, false)); 
            pw.close();
        } catch (Exception e) {
            System.out.println("createLog exception:");
            e.printStackTrace();
            System.exit(1);
        }       
    }
    
    /**
     * Write progress
     */
    private void writeProgress() {
        long completed = executor.getCompletedTaskCount();
        long total = executor.getTaskCount();
        long e = 50 * completed / total;
        long s = 50 - e;
        
        if (completed != lastCompleted) {              
            System.out.print("\rGraph plotting [");
            for (int i=0; i<e; i++) {
                System.out.print("=");
            }
            for (int i=0; i<s; i++) {
                System.out.print(" ");
            }
            System.out.print("] " + completed +"/" +  total);
            lastCompleted = e;
        }
    } 
    
    public void runScript(String scriptName, String refName) {
        String command = "Rscript " + options.getScriptsDir() + File.separator + scriptName + " " + options.getBaseDirectory() + " " + options.getSample();
        
        if (refName != null) {
            command = command + " " + refName;
        }
        
        executor.execute(new RGraphRunnable(command, logFilename));
        writeProgress();
    }
    
    /**
     * Execute plot commands.
     * @param references References object containing all references
     */
    public void plot() throws InterruptedException {
        String s = null;

        System.out.println("R log " + logFilename);
        System.out.println("");
        createLog();
        
        runScript("nanook_plot_lengths.R ", null);
       
        Set<String> ids = options.getReferences().getAllIds();
        for (String id : ids) {
            String name = options.getReferences().getReferenceById(id).getName();
            runScript("nanook_plot_alignments.R", name);
            runScript("nanook_plot_indels.R", name);
            runScript("nanook_plot_read_identity.R", name);
            writeProgress();
        }          
        
        // That's all - wait for all threads to finish
        executor.shutdown();
        while (!executor.isTerminated()) {
            writeProgress();
            Thread.sleep(100);
        }        

        writeProgress();
        System.out.println("");
    }
}
