/*
 * Program: NanoOK
 * Author:  Richard M. Leggett
 * 
 * Copyright 2015 The Genome Analysis Centre (TGAC)
 */

package nanook;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.*;

/**
 * Align reads
 * 
 * @author Richard Leggett
 */
public class ReadAligner {
    private NanoOKOptions options;
    private AlignmentFileParser parser;
    private ThreadPoolExecutor executor;
    private long lastCompleted = -1;
    
    /**
     * Constructor
     * @param o program options
     */
    public ReadAligner(NanoOKOptions o, AlignmentFileParser afp) {    
        options = o;
        parser = afp;
        
        executor = new ThreadPoolExecutor(options.getNumberOfThreads(), options.getNumberOfThreads(), 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }
    
    /**
     * Write progress
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
            System.out.print("\rAlignment [");
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
    
    private void checkAndMakeDir(String dir) {
        File f = new File(dir);
        if (f.exists()) {
            if (!f.isDirectory()) {
                System.out.println("Error: " + dir + " is a file, not a directory!");
                System.exit(1);
            }
        } else {
            //System.out.println("Making directory " + dir);
            f.mkdir();
        }
    }
    
    /**
     * Create directories for output
     */
    public void createDirectories() {
        checkAndMakeDir(options.getAlignerDir());
        checkAndMakeDir(options.getLogsDir());
        checkAndMakeDir(options.getLogsDir() + File.separator + options.getAligner());
    }
    
    public boolean isValidReadFile(String filename) {
        boolean isValid = false;
        
        //System.out.println(filename);
        
        if (parser.getReadFormat() == NanoOKOptions.FASTA) {
            if (filename.endsWith(".fa") || filename.endsWith(".fasta")) {
                isValid = true;
            }
        } else if (parser.getReadFormat() == NanoOKOptions.FASTQ) {
            if (filename.endsWith(".fq") || filename.endsWith(".fastq")) {
                isValid = true;
            }
        }            
        
        return isValid;
    }
    
    private void checkReferenceSizesFile(String referenceFile) {
        String sizesFilename = referenceFile + ".sizes";
        File sizesFile = new File(sizesFilename);
        if (!sizesFile.exists()) {
            System.out.println("");
            System.out.println("Generating .sizes file for reference. You may want to edit the display names.");
            SequenceReader sr = new SequenceReader(false);
            sr.indexFASTAFile(referenceFile, sizesFilename , false);
        }
    }
    
    private void processDirectory(String readsDir, String alignDir, String logDirName) {
        String reference = options.getReferenceFile();
        
        checkReferenceSizesFile(reference);
        checkAndMakeDir(alignDir);
        checkAndMakeDir(logDirName);
        
        for (int t=0; t<3; t++) {
            String inputDirName = readsDir + File.separator + NanoOKOptions.getTypeFromInt(t);
            String outputDirName = alignDir + File.separator + NanoOKOptions.getTypeFromInt(t);
            
            checkAndMakeDir(outputDirName);
            
            File inputDir = new File(inputDirName);
            File[] listOfFiles = inputDir.listFiles();

            if (listOfFiles == null) {
                System.out.println("");
                System.out.println("Directory "+inputDirName+" doesn't exist");
            } else if (listOfFiles.length <= 0) {
                System.out.println("");
                System.out.println("Directory "+inputDirName+" empty");
            } else {
                int readCount = 0;
                for (File file : listOfFiles) {
                    if (file.isFile()) {
                        if (isValidReadFile(file.getName())) {
                            String inPath = inputDirName + File.separator + file.getName();
                            String outPath = outputDirName + File.separator + file.getName() + parser.getAlignmentFileExtension();
                            String logFile = logDirName + File.separator + file.getName() + ".log";
                            String command = parser.getRunCommand(inPath, outPath, reference);                            
                            if (options.showAlignerCommand()) {
                                System.out.println("Running: " + command);
                            }
                            executor.execute(new SystemCommandRunnable(options, null, command, parser.outputsToStdout() ? outPath:null, logFile));
                            writeProgress();
                            readCount++;
                        }
                    }
                }
                
                if (readCount == 0) {
                    System.out.print("Error: unable to find any ");
                    if (parser.getReadFormat() == NanoOKOptions.FASTA) {
                        System.out.print("FASTA");
                    } else if (parser.getReadFormat() == NanoOKOptions.FASTQ) {
                        System.out.print("FASTQ");
                    }
                    System.out.println(" files to align");
                    System.out.println("");
                    System.exit(1);
                }
            }
        }
    }
    
    public void align() throws InterruptedException {
        if (options.isNewStyleReadDir()) {
            if (options.isProcessingPassReads()) {
                processDirectory(options.getReadDir() + File.separator + "pass",
                                 options.getAlignerDir() + File.separator + "pass",
                                 options.getLogsDir() + File.separator + options.getAligner() + File.separator + "pass");
            }
            
            if (options.isProcessingFailReads()) {
                processDirectory(options.getReadDir() + File.separator + "fail",
                                 options.getAlignerDir() + File.separator + "fail",
                                 options.getLogsDir() + File.separator + options.getAligner() + File.separator + "fail");
            }
        } else {
            processDirectory(options.getReadDir(), options.getAlignerDir(), options.getLogsDir() + File.separator + options.getAligner());
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
