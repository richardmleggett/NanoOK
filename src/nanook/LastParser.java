/*
 * Program: NanoOK
 * Author:  Richard M. Leggett
 * 
 * Copyright 2015 The Genome Analysis Centre (TGAC)
 */

package nanook;

import java.io.File;

/**
 * Parser for LAST alignments
 * 
 * @author Richard Leggett
 */
public class LastParser extends MAFParser implements AlignmentFileParser {
    private String alignmentParams = "-s 2 -T 0 -Q 0 -a 1";
    
    public LastParser(NanoOKOptions o, References r) {
        super(o, r);
    }
    
    public String getProgramID() {
        return "last";
    }
    
    public int getReadFormat() {
        return NanoOKOptions.FASTA;
    }
    
    public void setAlignmentParams(String p) {
        alignmentParams = p;
    }
        
    public String getRunCommand(String query, String output, String reference) {
        reference = reference.replaceAll("\\.fasta$", "");
        reference = reference.replaceAll("\\.fa$", "");
        
        return "lastal " + alignmentParams + " " + reference + " " + query;
        //return "lastal -o "+ output + " " + alignmentParams + " " + reference + " " + query;
    }
    
    public boolean outputsToStdout() {
        return true;
    }
    
    public void checkForIndex(String referenceFile) {
        String[] files = {referenceFile + ".bck",
                          referenceFile + ".suf"};

        for (int i=0; i<files.length; i++) {
            File f = new File(files[i]);

            if (!f.exists()) {
                System.out.println("");
                System.out.println("Error:");
                System.out.println("Can't find file " + f.getPath());
                System.out.println("1. Have you indexed the reference with lastdb?");
                System.out.println("2. Have you made sure that the output prefix is the same name as the reference file, apart from the .fasta or .fa extension?");
                System.out.println("   e.g. lastdb -Q 0 referencename referencename.fasta");
                System.out.println("Will continue but anticipate failure at analyse stage.");
                System.out.println("");
                return;
            }
        }
        
        return;
    }
}
