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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Enable multi-threading of read extraction
 * 
 * @author Richard Leggett
 */
public class ReadProcessorRunnable implements Runnable {
    public final static String TYPE_STRING_TEMPLATE = "/Analyses/Basecall_2D_000/BaseCalled_template/Fastq";
    public final static String TYPE_STRING_COMPLEMENT = "/Analyses/Basecall_2D_000/BaseCalled_complement/Fastq";
    public final static String TYPE_STRING_2D = "/Analyses/Basecall_2D_000/BaseCalled_2D/Fastq";
    private String[] typeStrings = {TYPE_STRING_TEMPLATE, TYPE_STRING_COMPLEMENT, TYPE_STRING_2D};
    public NanoOKOptions options;
    public FileWatcher fileWatcher;
    public boolean isNewStyleDir;
    
    public ReadProcessorRunnable(NanoOKOptions o, FileWatcher f) {
        options = o;
        fileWatcher = f;
    }   
    
    private String getFastaqDirFromFast5Name(String fast5Pathname) {
        File f = new File(fast5Pathname);        
        String inDir = f.getParent();
        String outDir = options.getReadDir();
        
        if (!inDir.startsWith(options.getFast5Dir())) {
            System.out.println("Something wrong with fast5 filename - shouldn't get to this code. Please contact richard.leggett@earlham.ac.uk");
            System.exit(1);
        }
        
        outDir = outDir + inDir.substring(options.getFast5Dir().length());
        
        //options.getLog().println("     In: "+fast5Pathname);
        //options.getLog().println(" OutDir: "+outDir);
        
        return outDir;
    }    

    private String getAlignmentPathnameFromFastaqName(String fastaqPathname) {
        File f = new File(fastaqPathname);        
        String inDir = f.getPath();
        String outPathname;
        
        if (!fastaqPathname.startsWith(options.getReadDir())) {
            System.out.println("Something wrong with read filename - shouldn't get to this code. Please contact richard.leggett@earlham.ac.uk");
            System.out.println("FastaPathname: "+fastaqPathname);
            System.out.println("ReadDir: "+options.getReadDir());
            System.exit(1);
        }
        
        outPathname = options.getAlignerDir() + inDir.substring(options.getReadDir().length());
        
        //options.getLog().println("     In: "+fastaqPathname);
        //options.getLog().println(" OutDir: "+outPathname);
        
        return outPathname;
    }      

    private String getAlignmentLogPathnameFromFastaqName(String fastaqPathname) {
        File f = new File(fastaqPathname);        
        String inDir = f.getPath();
        String outPathname;
        
        if (!fastaqPathname.startsWith(options.getReadDir())) {
            System.out.println("Something wrong with read filename - shouldn't get to this code. Please contact richard.leggett@earlham.ac.uk");
            System.out.println("FastaPathname: "+fastaqPathname);
            System.out.println("ReadDir: "+options.getReadDir());
            System.exit(1);
        }
        
        outPathname = options.getLogsDir() + File.separator + options.getAligner() + inDir.substring(options.getReadDir().length());
        
        //options.getLog().println("     In: "+fastaqPathname);
        //options.getLog().println(" OutDir: "+outPathname);
        
        return outPathname;
    }      
    
    private String getParserPathnameFromAlignmentName(String alignmentPathname) {
        File f = new File(alignmentPathname);        
        String inDir = f.getPath();
        String outPathname;
        
        if (!alignmentPathname.startsWith(options.getAlignerDir())) {
            System.out.println("Something wrong with read filename - shouldn't get to this code. Please contact richard.leggett@earlham.ac.uk");
            System.exit(1);
        }
        
        outPathname = options.getParserDir() + inDir.substring(options.getAlignerDir().length());
        
        //options.getLog().println("     In: " + alignmentPathname);
        //options.getLog().println(" OutDir: " + outPathname);
        
        return outPathname;
    }          

    private String getFastaqPathnameFromAlignmentName(String alignmentPathname) {
        File f = new File(alignmentPathname);        
        String inDir = f.getPath();
        String outPathname;
        
        if (!alignmentPathname.startsWith(options.getAlignerDir())) {
            System.out.println("Something wrong with read filename - shouldn't get to this code. Please contact richard.leggett@earlham.ac.uk");
            System.exit(1);
        }
        
        outPathname = options.getReadDir() + inDir.substring(options.getAlignerDir().length(),inDir.lastIndexOf('.'));
        
        //options.getLog().println("     In: " + alignmentPathname);
        //options.getLog().println(" OutDir: " + outPathname);
        
        return outPathname;
    }      
    
    public static String getFilePrefixFromPathname(String pathname) {
        File f = new File(pathname);
        String inName = f.getName();
        int suffixPos = inName.lastIndexOf(".");
        String outName;
        
        if (suffixPos > 0) {
            outName = inName.substring(0, suffixPos);
        } else {
            outName = inName;
        }

        //options.getLog().println("OutName: "+outName);

        return outName;
    }
    
    private void runCommandLocal(String command, String outPath) {
        ProcessLogger pl = new ProcessLogger();
        
        // outPath only non-null if aligner will only write to screen (yes, BWA, I'm talking about you)
        if (outPath != null) {
            pl.setWriteFormat(false, true, false);
            pl.runAndLogCommand(command, outPath, false);
        } else {
            pl.runCommand(command);
        }
    }    

    public void runParse(String alignmentPathname) {
        String parsedPathname = getParserPathnameFromAlignmentName(alignmentPathname) + ".txt";
        String fastaqPathname = getFastaqPathnameFromAlignmentName(alignmentPathname);
        ReadParser rp = new ReadParser(options);
        
        options.getLog().println("Parsing file "+ alignmentPathname);
        options.getLog().println("          to "+ parsedPathname);
        
        rp.parse(fastaqPathname, alignmentPathname, parsedPathname);
    }
    
    public void runAlign(String fastaqPathname) {
        String reference = options.getReferenceFile();
        AlignmentFileParser parser = options.getParser();
        
        String filePrefix = getFilePrefixFromPathname(fastaqPathname);
        String alignmentPathname = getAlignmentPathnameFromFastaqName(fastaqPathname) + parser.getAlignmentFileExtension();
        String alignmentLogPathname = getAlignmentLogPathnameFromFastaqName(fastaqPathname);

        options.getLog().println("Aligning file "+fastaqPathname);        
        options.getLog().println("           to "+alignmentPathname);        
        options.getLog().println("     with log "+alignmentLogPathname);        
        
        
        String command = parser.getRunCommand(fastaqPathname, alignmentPathname, reference);                            
        if (options.showAlignerCommand()) {
            System.out.println("Running: " + command);
        }
        runCommandLocal(command, parser.outputsToStdout() ? alignmentPathname:null);
        if (options.isParsingRead()) {
            runParse(alignmentPathname);
        }
    }
    
    public void addToBlast(String fastaqPathname, int type) {
        int pf = NanoOKOptions.READTYPE_PASS;
        
        if (fastaqPathname.contains("/fail/")) {
            pf = NanoOKOptions.READTYPE_FAIL;
        }
        
        options.getBlastHandler(type, pf).addRead(fastaqPathname);
    }
    
    public void runExtract(String fast5Pathname) {
        Fast5File inputFile = new Fast5File(options, fast5Pathname);
        String fastaqDir = getFastaqDirFromFast5Name(fast5Pathname);                    
        String filePrefix = getFilePrefixFromPathname(fast5Pathname);
        String fastaqPathname;
        
        options.getLog().println("Extracting file "+fast5Pathname);

        for (int t=0; t<3; t++) {
            if (options.isProcessingReadType(t)) {
                FastAQFile ff = inputFile.getFastq(options.getBasecallIndex(), t);
                if (ff != null) {
                    if (options.getReadFormat() == NanoOKOptions.FASTA) {
                        fastaqPathname = fastaqDir + File.separator + NanoOKOptions.getTypeFromInt(t) + File.separator + filePrefix + "_BaseCalled_" + NanoOKOptions.getTypeFromInt(t) + ".fasta";
                        options.getLog().println("        Writing "+fastaqPathname);
                        if (options.mergeFastaFiles()) {
                            options.getReadFileMerger().addReadFile(fastaqPathname, t);
                        }                        
                        ff.writeFasta(fastaqPathname, options.outputFast5Path() ? fast5Pathname:null);
                        if (options.isBlastingRead()) {
                            addToBlast(fastaqPathname, t);
                        }
                        
                        if (options.isAligningRead()) {
                            runAlign(fastaqPathname);
                        }
                    } else if (options.getReadFormat() == NanoOKOptions.FASTQ) {
                        fastaqPathname = fastaqDir + File.separator + NanoOKOptions.getTypeFromInt(t) + File.separator + filePrefix + "_BaseCalled_" + NanoOKOptions.getTypeFromInt(t) + ".fastq";
                        options.getLog().println("        Writing "+fastaqPathname);
                        if (options.mergeFastaFiles()) {
                            options.getReadFileMerger().addReadFile(fastaqPathname, t);
                        }
                        ff.writeFastq(fastaqPathname);
                        if (options.isBlastingRead()) {
                            addToBlast(fastaqPathname, t);
                        }
                        
                        if (options.isAligningRead()) {
                            runAlign(fastaqPathname);
                        }
                    }
                }
            }
        }
    }

    public void run() {
        while (!fileWatcher.timedOut()) {
            String nextPathname = null;
            String fastaqPathname = null;
            String alignmentPathname = null;
            String parsedPathname = null;
            String alignmentLogPathname = null;
            
            // Get next file to process
            while ((nextPathname == null) && !fileWatcher.timedOut()) {
                nextPathname = fileWatcher.getPendingFile();
                if (nextPathname == null) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ReadProcessorRunnable.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
            
            if (nextPathname != null) {
                // Check valid filename
                if (options.isExtractingReads()) {
                    if (nextPathname.toLowerCase().endsWith(".fast5")) {
                        runExtract(nextPathname);
                    } else {
                        options.getLog().println("Invalid "+nextPathname);
                    }
                } else if (options.isAligningRead()) {
                    if (nextPathname.toLowerCase().endsWith(".fasta") || 
                        nextPathname.toLowerCase().endsWith(".fastq")) {
                        runAlign(nextPathname);
                    }                
                } else if (options.isParsingRead()) {
                    if (nextPathname.toLowerCase().endsWith(options.getParser().getAlignmentFileExtension())) {
                        alignmentPathname = nextPathname;
                        runParse(nextPathname);
                    }
                }                
            }
        }
        
        options.getLog().println("Thread exiting");
    }
}