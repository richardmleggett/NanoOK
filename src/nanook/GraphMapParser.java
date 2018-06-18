/*
 * Program: NanoOK
 * Author:  Richard M. Leggett
 * 
 * Copyright 2015 The Genome Analysis Centre (TGAC)
 */

package nanook;

import java.io.File;

/**
 * Parser for GraphMap files
 * @author Richard Leggett
 */
public class GraphMapParser extends SAMParser implements AlignmentFileParser {
    private String alignmentParams = "";
    private NanoOKOptions options;
    
    public GraphMapParser(NanoOKOptions o, References r) {
        super(o, r);
        options = o;
    }
    
    public String getProgramID() {
        return "graphmap";
    }

    public int getReadFormat() {
        int or = options.getReadFormat();    
        return or;
        
        //return NanoOKOptions.FASTA;
    }
    
    public void setAlignmentParams(String p) {
        alignmentParams = p;
    }
    
    public boolean outputsToStdout() {
        return false;
    }
        
    public String getRunCommand(String query, String output, String reference) {
        //reference = reference.replaceAll("\\.fasta$", "");
        //reference = reference.replaceAll("\\.fa$", "");
        String command = "graphmap align -v 0 -r " + reference + " -d " + query + " -o " + output;
        if (alignmentParams.length() > 0 ) {
            command = command + " " + alignmentParams;
        }
        
        return command;
    }
    
    public void checkForIndex(String referenceFile) {
        /*String[] files = {referenceFile + ".fasta.bwt",
                          referenceFile + ".fasta.pac"};

        for (int i=0; i<files.length; i++) {
            File f = new File(files[i]);

            if (!f.exists()) {
                System.out.println("");
                System.out.println("Error:");
                System.out.println("Can't find file " + f.getPath());
                System.out.println("Have you indexed the reference with bwa index?");
                System.out.println("Will continue but anticipate failure at analyse stage.");
                System.out.println("");
                return;
            }
        }*/
        
        return;
    }    
}
