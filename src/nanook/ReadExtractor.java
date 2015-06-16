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
public class ReadExtractor {
    private NanoOKOptions options;
    private ThreadPoolExecutor executor;
    private long lastCompleted = -1;

    /**
     * Constructor
     * @param o program options
     */
    public ReadExtractor(NanoOKOptions o) {    
        options = o;

        //executor = Executors.newFixedThreadPool(options.getNumberOfThreads());
        executor = new ThreadPoolExecutor(options.getNumberOfThreads(), options.getNumberOfThreads(), 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }
    
    /**
     * Write progress of extraction
     */
    private void writeProgress() {
        long completed = executor.getCompletedTaskCount();
        long total = executor.getTaskCount();
        long e = 50 * completed / total;
        long s = 50 - e;
        
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
     * Create directories for output
     */
    public void createDirectories() {
        File f = new File(options.getReadDir());
        if (f.exists()) {
            if (!f.isDirectory()) {
                System.out.println("Error: "+options.getReadDir()+" is a file, not a directory!");
                System.exit(1);
            }
        } else {
            //System.out.println("Making directory "+options.getReadDir());
            f.mkdir();
        }
        
    }

    /**
     * Process a directory and extract reads
     * @param inputDirName input directory name
     * @param outputDirName output directory name
     */
    private void processDirectory(String inputDirName, String outputDirName) {
        File f = new File(outputDirName);
        
        // Make directory
        if (! f.exists()) {
            f.mkdir();
        }
        
        // Make output Template, Complement and 2D directories
        for (int t=0; t<3; t++) {
            if (options.isProcessingReadType(t)) {
                f = new File(outputDirName + File.separator + NanoOKOptions.getTypeFromInt(t));
                if (! f.exists()) {
                    f.mkdir();
                }
            }
        }
        
        File inputDir = new File(inputDirName);
        File[] listOfFiles = inputDir.listFiles();
        
        if (listOfFiles == null) {
            System.out.println("Directory "+inputDirName+" doesn't exist");
        } else if (listOfFiles.length <= 0) {
            System.out.println("Directory "+inputDirName+" empty");
        } else {
            for (File file : listOfFiles) {
                if (file.isFile()) {
                    if (file.getName().endsWith(".fast5")) {
                        executor.execute(new ReadExtractorRunnable(options, inputDirName, file.getName(), outputDirName));
                        writeProgress();
                    }
                }
            }            
        }    
    }

    /**
     * Extract reads
     */
    public void extract() throws InterruptedException {
        if (options.isNewStyleDir()) {
            if (options.isProcessingPassReads()) {
                processDirectory(options.getFast5Dir() + File.separator + "pass",
                                 options.getReadDir() + File.separator + "pass");
            }
            
            if (options.isProcessingFailReads()) {
                processDirectory(options.getFast5Dir() + File.separator + "fail",
                                 options.getReadDir() + File.separator + "fail");
            }
        } else {
            processDirectory(options.getFast5Dir(), options.getReadDir());
        }
        
        // That's all - wait for all threads to finish
        executor.shutdown();
        while (!executor.isTerminated()) {
            writeProgress();
            Thread.sleep(100);
        }        

        writeProgress();
        System.out.println("");
        System.out.println("");
        System.out.println("DONE");
    }    
}


