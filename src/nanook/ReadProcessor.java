/*
 * Program: NanoOK
 * Author:  Richard M. Leggett
 * 
 * Copyright 2015 The Genome Analysis Centre (TGAC)
 */

package nanook;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Read extractor
 * 
 * @author Richard Leggett
 */
public class ReadProcessor {
    private NanoOKOptions options;
    private ThreadPoolExecutor executor;
    private long lastCompleted = -1;
    FileWatcher fw = null;

    /**
     * Constructor
     * @param o program options
     */
    public ReadProcessor(NanoOKOptions o) {    
        options = o;
        fw = new FileWatcher(options);

        //executor = Executors.newFixedThreadPool(options.getNumberOfThreads());
        executor = new ThreadPoolExecutor(options.getNumberOfThreads(), options.getNumberOfThreads(), 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }
    
    /**
     * Write progress of extraction
     */
    private void writeProgress() {
        long completed = executor.getCompletedTaskCount();
        long total = executor.getTaskCount();
        long e = 0;
        long s = NanoOKOptions.PROGRESS_WIDTH;
        
        if (total > 0) {
            e = NanoOKOptions.PROGRESS_WIDTH * completed / total;
            s = NanoOKOptions.PROGRESS_WIDTH - e;
        }
        
        if (completed != lastCompleted) {              
            System.out.print("\rExtraction [");
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
    
    /**
     * Process a directory and extract reads
     * @param inputDirName input directory name
     * @param outputDirName output directory name
     */
    private void processDirectory(String inputDirName, String outputDirName, boolean allowSubdir, boolean processThisDir) {
        File f = new File(outputDirName);
        
        options.getLog().println("Processing directory");
        options.getLog().println("Input dir name: "+inputDirName);
        options.getLog().println("Output dir name: "+outputDirName);
        options.getLog().println("allowSubdir: "+allowSubdir);        
        options.getLog().println("processThisDir: "+processThisDir);
                
        // Make directory
        if (! f.exists()) {
            f.mkdir();
        }
        
        if (processThisDir) {
            // Make output Template, Complement and 2D directories
            for (int t=0; t<3; t++) {
                if (options.isProcessingReadType(t)) {
                    f = new File(outputDirName + File.separator + NanoOKOptions.getTypeFromInt(t));
                    if (! f.exists()) {
                        f.mkdir();
                    }
                }
            }
            
            fw.addWatchDir(inputDirName);
        } else {
            File inputDir = new File(inputDirName);
            File[] listOfFiles = inputDir.listFiles();

            if (listOfFiles == null) {
                System.out.println("");
                System.out.println("Directory "+inputDirName+" doesn't exist");
            } else if (listOfFiles.length <= 0) {
                System.out.println("");
                System.out.println("Directory "+inputDirName+" empty");
            } else {
                for (File file : listOfFiles) {
                    if (file.isDirectory() && allowSubdir) {
                        processDirectory(inputDirName + File.separator + file.getName(),
                                         outputDirName + File.separator + file.getName(),
                                         false,
                                         true);
                    }
                }           
            }
        }    
    }

    /**
     * Extract reads
     */
    public void process() throws InterruptedException {        
        if (options.isNewStyleDir()) {
            if (options.isProcessingPassReads()) {
                processDirectory(options.getFast5Dir() + File.separator + "pass",
                                 options.getReadDir() + File.separator + "pass",
                                 options.processSubdirs(),
                                 options.processSubdirs() ? false:true);
            }
            
            if (options.isProcessingFailReads()) {
                processDirectory(options.getFast5Dir() + File.separator + "fail",
                                 options.getReadDir() + File.separator + "fail",
                                 options.processSubdirs(),
                                 true);
            }
        } else {
            processDirectory(options.getFast5Dir(), options.getReadDir(), false, true);
        }
        
        for (int i=0; i<options.getNumberOfThreads(); i++) {
            executor.execute(new ReadProcessorRunnable(options, fw));
        }
        
        // Now keep scanning
        while (!fw.timedOut()) {
            fw.scan();
            fw.writeProgress();
            Thread.sleep(500);            
            fw.writeProgress();
            Thread.sleep(500);            
        }
        fw.writeProgress();
                
        // That's all - wait for all threads to finish
        executor.shutdown();

        if (options.mergeFastaFiles()) {        
            System.out.println("");
            options.getReadFileMerger().writeMergedFiles();
        }        
        
        //writeProgress();
        System.out.println("");
        System.out.println("");
        System.out.println("DONE");
    }  
}

