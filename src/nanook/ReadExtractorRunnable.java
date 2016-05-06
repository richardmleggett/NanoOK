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

/**
 * Enable multi-threading of read extraction
 * 
 * @author Richard Leggett
 */
public class ReadExtractorRunnable implements Runnable {
    public final static String TYPE_STRING_TEMPLATE = "/Analyses/Basecall_2D_000/BaseCalled_template/Fastq";
    public final static String TYPE_STRING_COMPLEMENT = "/Analyses/Basecall_2D_000/BaseCalled_complement/Fastq";
    public final static String TYPE_STRING_2D = "/Analyses/Basecall_2D_000/BaseCalled_2D/Fastq";
    private String[] typeStrings = {TYPE_STRING_TEMPLATE, TYPE_STRING_COMPLEMENT, TYPE_STRING_2D};
    public NanoOKOptions options;
    public String inDir;
    public String filename;
    public String outDir;
    
    public ReadExtractorRunnable(NanoOKOptions o, String in, String file, String out) {
        options = o;
        inDir = in;
        filename = file;
        outDir = out;
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
        
        for (int t=0; t<3; t++) {
            if (options.isProcessingReadType(t)) {
                FastAQFile ff = inputFile.getFastq(options.getBasecallIndex(), t);
                if (ff != null) {
                    if (options.getReadFormat() == NanoOKOptions.FASTA) {
                        ff.writeFasta(outDir + File.separator + NanoOKOptions.getTypeFromInt(t) + File.separator + outName + "_BaseCalled_" + NanoOKOptions.getTypeFromInt(t) + ".fasta", options.outputFast5Path() ? inputPathname:null);
                    } else if (options.getReadFormat() == NanoOKOptions.FASTQ) {
                        ff.writeFastq(outDir + File.separator + NanoOKOptions.getTypeFromInt(t) + File.separator + outName + "_BaseCalled_" + NanoOKOptions.getTypeFromInt(t) + ".fastq");
                    }
                }
            }
        }
    }    
}
