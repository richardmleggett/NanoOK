/*
 * Program: NanoOK
 * Author:  Richard M. Leggett
 * 
 * Copyright 2015 The Genome Analysis Centre (TGAC)
 */

package nanook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Enable multi-threading of read extraction
 * 
 * @author Richard Leggett
 */
public class WatcherRunnable implements Runnable {
    public final static String TYPE_STRING_TEMPLATE = "/Analyses/Basecall_2D_000/BaseCalled_template/Fastq";
    public final static String TYPE_STRING_COMPLEMENT = "/Analyses/Basecall_2D_000/BaseCalled_complement/Fastq";
    public final static String TYPE_STRING_2D = "/Analyses/Basecall_2D_000/BaseCalled_2D/Fastq";
    private String[] typeStrings = {TYPE_STRING_TEMPLATE, TYPE_STRING_COMPLEMENT, TYPE_STRING_2D};
    private NanoOKOptions options;
    private AlignmentFileParser parser;
    private String inDir;
    private String filename;
    private  String fastaqDir;
    private String alignDir;
    private String passOrFail;
    private int readType;
    
    public WatcherRunnable(NanoOKOptions o, String in, String file, String pf, String out, String ad, AlignmentFileParser p) {
        options = o;
        inDir = in;
        filename = file;
        passOrFail = pf;
        fastaqDir = out;
        alignDir = ad;
        parser = p;
        
        if (passOrFail.equals("pass")) {
            readType = NanoOKOptions.READTYPE_PASS;
        } else if (passOrFail.equals("fail")) {
            readType = NanoOKOptions.READTYPE_FAIL;
        } else {
            System.out.println("Error in WatcherRunnable - not pass or fail!");
            System.exit(1);
        }
    }
    
    /**
     * Extract reads of each type from file
     * @param inDir input directory
     * @param filename filename
     * @param outDir output directory
     */
    public void run() {
        String inputPathname = inDir + File.separator + filename;
        Fast5File inputFile = new Fast5File(options, inputPathname);
        String outName = new File(inputPathname).getName();
        
        //for (int t=0; t<3; t++) {
        int t;
        if (options.isProcessing2DReads()) {
            t = NanoOKOptions.TYPE_2D;
        } else {
            t = NanoOKOptions.TYPE_TEMPLATE;
        }

        if (options.isProcessingReadType(t)) {
            FastAQFile ff = inputFile.getFastq(options.getBasecallIndex(), t);
            if (ff != null) {
                String readFilename = null;
                String readPathname = null;
                if (options.getReadFormat() == NanoOKOptions.FASTA) {
                    readFilename = outName + "_BaseCalled_" + NanoOKOptions.getTypeFromInt(t) + ".fasta";
                    readPathname = fastaqDir + File.separator + NanoOKOptions.getTypeFromInt(t) + File.separator + readFilename;
                    System.out.println("    Writing "+readPathname);
                    options.getWatcherReadLog().println(readPathname);
                    ff.writeFasta(readPathname, options.outputFast5Path() ? inputPathname:null);
                    options.getMergedFile(t, readType).addFile(readPathname, options.outputFast5Path() ? inputPathname:null);
                } else if (options.getReadFormat() == NanoOKOptions.FASTQ) {
                    readFilename = outName + "_BaseCalled_" + NanoOKOptions.getTypeFromInt(t) + ".fastq";
                    readPathname = fastaqDir + File.separator + NanoOKOptions.getTypeFromInt(t) + File.separator + readFilename;
                    System.out.println("    Writing "+readPathname);
                    options.getWatcherReadLog().println(readPathname);
                    ff.writeFastq(readPathname);
                    options.getMergedFile(t, readType).addFile(readPathname, options.outputFast5Path() ? inputPathname:null);
                }
            }
        }
        //}
    }    
}
