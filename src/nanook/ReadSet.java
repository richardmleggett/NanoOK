/*
 * Program: NanoOK
 * Author:  Richard M. Leggett (richard.leggett@earlham.ac.uk)
 * 
 * Copyright 2015-17 Earlham Institute
 */

package nanook;

import java.io.BufferedReader;
import java.io.*;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Represents a read set (Template reads, Complement reads, or 2D reads).
 * 
 * @author Richard Leggett
 */
public class ReadSet {
    public final static int MAX_READ_DIRS = 1000;
    private ThreadPoolExecutor queryExecutor;
    private NanoOKOptions options;
    private ReadSetStats stats;
    private int type;
    private int nFastaFiles=0;
    private String typeString;
    private long lastCompleted = -1;

   
    /**
     * Constructor
     * @param t type (defined in NanoOKOprions)
     * @param o NanoOKOptions object
     * @param s set of stats to associate with this read set
     */
    public ReadSet(int t, NanoOKOptions o, ReadSetStats s) {
        options = o;
        type = t;
        stats = s;
        
        queryExecutor = new ThreadPoolExecutor(options.getNumberOfThreads(), options.getNumberOfThreads(), 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    }
        
    /**
     * Write progress
     */
    private void writeProgress(ThreadPoolExecutor tpe) {
        long completed = tpe.getCompletedTaskCount();
        long total = tpe.getTaskCount();
        long e = 0;
        long s = NanoOKOptions.PROGRESS_WIDTH;
        
        if (total > 0) {
            e = NanoOKOptions.PROGRESS_WIDTH * completed / total;
            s = NanoOKOptions.PROGRESS_WIDTH - e;
        }
        
        if (completed != lastCompleted) {              
            System.out.print("\r[");
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
     * Check if filename has valid read extension 
     * @param f flename
     * @return true if valid for chosen aligner
     */
    private boolean isValidReadExtension(String f) {
        boolean r = false;
        
        if (options.getReadFormat() == NanoOKOptions.FASTQ) {
            if ((f.endsWith(".fastq")) || (f.endsWith(".fq"))) {
                r = true;
            }
        } else {
            if ((f.endsWith(".fasta")) || (f.endsWith(".fa"))) {
                r = true;
            }
        }
        
        return r;
    }
    
    /**
     * Gather length statistics on reads and parse alignments
     */
    public int processReadsOld() throws InterruptedException {
        AlignmentFileParser parser = options.getParser();
        String[] readDirs = new String[MAX_READ_DIRS];
        String[] alignerDirs = new String[MAX_READ_DIRS];
        int readTypes[] = new int[MAX_READ_DIRS];
        int nDirs = 0;
        int maxReads = options.getMaxReads();
        String outputFilename = options.getAnalysisDir() + File.separator + "Unaligned" + File.separator + options.getTypeFromInt(type) + "_nonaligned.txt";
        AlignmentsTableFile nonAlignedSummary = new AlignmentsTableFile(outputFilename);
        
        nFastaFiles=0;

        stats.openLengthsFile();
        
        if (options.usingPassFailDirs()) {
            for (int pf=NanoOKOptions.READTYPE_PASS; pf<=NanoOKOptions.READTYPE_FAIL; pf++) {
                String passOrFail="";

                if ((pf == NanoOKOptions.READTYPE_PASS) && (options.isProcessingPassReads())) {
                    passOrFail="pass";
                } else if ((pf == NanoOKOptions.READTYPE_FAIL) && (options.isProcessingFailReads())) {
                    passOrFail="fail";
                }

                if (passOrFail != "") {
                    if (options.isBarcoded()) {
                            File inputDir = new File(options.getReadDir() + File.separator + passOrFail + File.separator + options.getTypeFromInt(type));
                            File[] listOfFiles = inputDir.listFiles();
                            for (File file : listOfFiles) {
                                if (file.isDirectory()) {
                                    if (nDirs == MAX_READ_DIRS) {
                                        System.out.println("Error: too many directories.\n");
                                        System.exit(1);
                                    }
                                    readDirs[nDirs] = inputDir.getPath() + File.separator + file.getName();
                                    alignerDirs[nDirs] = options.getAlignerDir() + File.separator + passOrFail + File.separator + options.getTypeFromInt(type) + File.separator + file.getName();
                                    readTypes[nDirs++] = pf;
                                }
                            }
                    } else {                
                        readDirs[nDirs] = options.getReadDir() + File.separator + passOrFail + File.separator + options.getTypeFromInt(type);
                        alignerDirs[nDirs] = options.getAlignerDir() + File.separator + passOrFail + File.separator + options.getTypeFromInt(type);
                        readTypes[nDirs++] = pf;
                    }
                    
                }
            }
        } else {
            readDirs[nDirs] = options.getReadDir();
            alignerDirs[nDirs] = options.getAlignerDir();
            readTypes[nDirs] = NanoOKOptions.READTYPE_COMBINED;
            nDirs++;
        }
                
        for (int dirIndex=0; dirIndex<nDirs; dirIndex++) {        
            String inputDir = readDirs[dirIndex];
            String alignDir = alignerDirs[dirIndex];
            File folder = new File(inputDir);
            File[] listOfFiles = folder.listFiles();
                        
            if (listOfFiles == null) {
                System.out.println("");
                System.out.println("Directory "+inputDir+" doesn't exist");
            } else if (listOfFiles.length <= 0) {
                System.out.println("");
                System.out.println("Directory "+inputDir+" empty");
            } else {
                for (File file : listOfFiles) {
                    if (file.isFile()) {
                        if (isValidReadExtension(file.getName())) {
                            String alignmentFilename = alignDir + File.separator + file.getName() + parser.getAlignmentFileExtension();
                            //System.out.println(alignmentFilename);
                            //options.getLog().println("File: " + alignmentFilename);
                            if (new File(alignmentFilename).exists()) {
                                queryExecutor.execute(new ParserRunnable(options, stats, file.getAbsolutePath(), alignmentFilename, type, readTypes[dirIndex], nonAlignedSummary));
                                writeProgress(queryExecutor);
                                                                
                                nFastaFiles++;
                                if ((maxReads > 0) && (nFastaFiles >= maxReads)) {
                                     break;
                                }
                                
                            } else {
                                System.out.println("Error: Read ignored, can't find alignment "+alignmentFilename);
                            } 
                        }
                    }
                }
            }
        }


        
        // That's all - wait for all threads to finish
        queryExecutor.shutdown();
        while (!queryExecutor.isTerminated()) {
            writeProgress(queryExecutor);
            Thread.sleep(100);
        }        

        writeProgress(queryExecutor);
        System.out.println("");
        
        stats.closeLengthsFile();
        stats.writeSummaryFile();        
        stats.calculateStats();    
        
        return nFastaFiles;
    }
    
    /**
     * Gather length statistics on reads and parse alignments
     */
    public int processReadsBatch() throws InterruptedException {
        AlignmentFileParser parser = options.getParser();
        String[] readDirs = new String[MAX_READ_DIRS];
        String[] alignerDirs = new String[MAX_READ_DIRS];
        int readTypes[] = new int[MAX_READ_DIRS];
        int nDirs = 0;
        int maxReads = options.getMaxReads();
        String outputFilename = options.getAnalysisDir() + File.separator + "Unaligned" + File.separator + options.getTypeFromInt(type) + "_nonaligned.txt";
        AlignmentsTableFile nonAlignedSummary = new AlignmentsTableFile(outputFilename);
        
        nFastaFiles=0;

        typeString = options.getTypeFromInt(type);
                
        stats.openLengthsFile();

        for (int pf=NanoOKOptions.READTYPE_PASS; pf<=NanoOKOptions.READTYPE_FAIL; pf++) {
            String passOrFail="";

            if ((pf == NanoOKOptions.READTYPE_PASS) && (options.isProcessingPassReads())) {
                passOrFail="pass";
            } else if ((pf == NanoOKOptions.READTYPE_FAIL) && (options.isProcessingFailReads())) {
                passOrFail="fail";
            }

            if (passOrFail != "") {
                if (options.isBarcoded()) {
                        File inputDir = new File(options.getReadDir() + File.separator + passOrFail + File.separator + typeString);
                        File[] listOfFiles = inputDir.listFiles();
                        for (File file : listOfFiles) {
                            if (file.isDirectory()) {
                                if (nDirs == MAX_READ_DIRS) {
                                    System.out.println("Error: too many directories.\n");
                                    System.exit(1);
                                }
                                readDirs[nDirs] = options.getReadDir() + File.separator + passOrFail + File.separator + typeString + File.separator + file.getName();
                                alignerDirs[nDirs] = options.getAlignerDir() + File.separator + passOrFail + File.separator + typeString + File.separator + file.getName();
                                readTypes[nDirs++] = pf;
                            }
                        }
                } else {                
                    readDirs[nDirs] = options.getReadDir() + File.separator + passOrFail + File.separator + typeString;
                    alignerDirs[nDirs] = options.getAlignerDir() + File.separator + passOrFail + File.separator + typeString;
                    readTypes[nDirs++] = pf;
                }

            }
        }
                
        // Dirs should be e.g.
        // inputDir = sample/fasta/Template/pass
        // alignDir = sample/last/Template/pass
        for (int dirIndex=0; dirIndex<nDirs; dirIndex++) {        
            String inputDir = readDirs[dirIndex];
            String alignDir = alignerDirs[dirIndex];
            File folder = new File(inputDir);
            File[] listOfFilesTop = folder.listFiles();
            
            options.getLog().println("Input: "+inputDir);
            options.getLog().println("Align: "+alignDir);
            
            // Now list of files should contain batch_XXX directories
            if (listOfFilesTop == null) {
                System.out.println("");
                System.out.println("Directory "+inputDir+" doesn't exist");
            } else if (listOfFilesTop.length <= 0) {
                System.out.println("");
                System.out.println("Directory "+inputDir+" empty");
            } else {                
                for (File topLevelFile : listOfFilesTop) {
                    options.getLog().println("  Got dir "+ topLevelFile.getName());
                    if (topLevelFile.isDirectory()) {
                        // Now go through reads in directory
                        File[] listOfFiles = topLevelFile.listFiles();
                        for (File file : listOfFiles) {
                            if (file.isFile()) {
                                if (isValidReadExtension(file.getName())) {
                                    String alignmentFilename = alignDir + File.separator + topLevelFile.getName() + File.separator + file.getName() + parser.getAlignmentFileExtension();
                                    //System.out.println(alignmentFilename);
                                    //options.getLog().println("File: " + alignmentFilename);
                                    if (new File(alignmentFilename).exists()) {
                                        queryExecutor.execute(new ParserRunnable(options, stats, file.getAbsolutePath(), alignmentFilename, type, readTypes[dirIndex], nonAlignedSummary));
                                        writeProgress(queryExecutor);

                                        nFastaFiles++;
                                        if ((maxReads > 0) && (nFastaFiles >= maxReads)) {
                                             break;
                                        }

                                    } else {
                                        System.out.println("Error: Read ignored, can't find alignment "+alignmentFilename);
                                    } 
                                }
                            }
                        }
                    }
                }
            }
        }


        
        // That's all - wait for all threads to finish
        queryExecutor.shutdown();
        while (!queryExecutor.isTerminated()) {
            writeProgress(queryExecutor);
            Thread.sleep(100);
        }        

        writeProgress(queryExecutor);
        System.out.println("");
        
        stats.closeLengthsFile();
        stats.writeSummaryFile();        
        stats.calculateStats();    
        
        return nFastaFiles;
    }    
    
    public int processReads() throws InterruptedException {
        if (options.usingBatchDirs()) {
            return processReadsBatch();
        } else {
            return processReadsOld();
        }
    }
    
    /**
     * Get type of this read set.
     * @return a String (e.g. "Template")
     */
    public String getTypeString() {
        return typeString;
    }
    
    /**
     * Get stats object.
     * @return a ReadSetStats object
     */
    public ReadSetStats getStats() {
        return stats;
    }
}
