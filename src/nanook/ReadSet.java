/*
 * Program: NanoOK
 * Author:  Richard M. Leggett
 * 
 * Copyright 2015 The Genome Analysis Centre (TGAC)
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
    private ThreadPoolExecutor parserExecutor;
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
        
        parserExecutor = new ThreadPoolExecutor(options.getNumberOfThreads(), options.getNumberOfThreads(), 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
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
    public int processReads() throws InterruptedException {
        AlignmentFileParser parser = options.getParser();
        String[] readDirs = new String[2];
        String[] alignerDirs = new String[2];
        int readTypes[] = new int[2];
        int nDirs = 0;
        int maxReads = options.getMaxReads();
        String outputFilename = options.getAnalysisDir() + File.separator + "Unaligned" + File.separator + options.getTypeFromInt(type) + "_nonaligned.txt";
        AlignmentsTableFile nonAlignedSummary = new AlignmentsTableFile(outputFilename);
        
        nFastaFiles=0;

        typeString = options.getTypeFromInt(type);
                
        stats.openLengthsFile();

        if (options.isNewStyleDir()) {
            if (options.isProcessingPassReads()) {
                readDirs[nDirs] = options.getReadDir() + File.separator + "pass";
                alignerDirs[nDirs] = options.getAlignerDir() + File.separator + "pass";
                readTypes[nDirs] = NanoOKOptions.READTYPE_PASS;
                nDirs++;
            }
            
            if (options.isProcessingFailReads()) {
                readDirs[nDirs] = options.getReadDir() + File.separator + "fail";
                alignerDirs[nDirs] = options.getAlignerDir() + File.separator + "fail";
                readTypes[nDirs] = NanoOKOptions.READTYPE_FAIL;
                nDirs++;
            }
        } else {
            readDirs[nDirs] = options.getReadDir();
            alignerDirs[nDirs] = options.getAlignerDir();
            readTypes[nDirs] = NanoOKOptions.READTYPE_COMBINED;
            nDirs++;
        }
                
        for (int dirIndex=0; dirIndex<nDirs; dirIndex++) {        
            String inputDir = readDirs[dirIndex] + File.separator + options.getTypeFromInt(type);
            String alignDir = alignerDirs[dirIndex] + File.separator + options.getTypeFromInt(type);
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
