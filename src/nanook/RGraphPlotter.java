package nanook;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
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
    private long lastCompleted = -1;
    private String logDirectory;

    /**
     * Constructor.
     * @param o NanoOKOptions object
     */
    public RGraphPlotter(NanoOKOptions o) {
        options = o;
        executor = new ThreadPoolExecutor(options.getNumberOfThreads(), options.getNumberOfThreads(), 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
        logDirectory = options.getLogsDir() + File.separator + "R";
        File f = new File(logDirectory);
        if (!f.exists()) {
            f.mkdir();
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
            lastCompleted = completed;
        }
    } 
    
    public void runScript(String scriptName, String logPrefix, String refName) {
        ArrayList<String> args = new ArrayList<String>();
        //String command = options.getScriptsDir() + File.separator + scriptName + " " + options.getBaseDirectory() + " " + options.getSample();
        String logFilename = logDirectory + File.separator + logPrefix;
        
        args.add("Rscript");
        args.add(options.getScriptsDir() + File.separator + scriptName);
        args.add(options.getBaseDirectory());
        args.add(options.getSample());
        
        if (refName != null) {
            args.add(refName);
            //command = command + " " + refName;
            logFilename = logFilename + "_"+refName;
        }
                
        executor.execute(new RGraphRunnable("Rscript", args, logFilename + ".txt"));
        writeProgress();
    }
    
    /**
     * Execute plot commands.
     * @param references References object containing all references
     */
    public void plot() throws InterruptedException {
        String s = null;
        
        runScript("nanook_plot_lengths.R", "plot_lengths", null);
       
        Set<String> ids = options.getReferences().getAllIds();
        for (String id : ids) {
            String name = options.getReferences().getReferenceById(id).getName();
            runScript("nanook_plot_alignments.R", "plot_alignments", name);
            runScript("nanook_plot_indels.R", "plot_indels", name);
            runScript("nanook_plot_read_identity.R", "plot_identity", name);
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
