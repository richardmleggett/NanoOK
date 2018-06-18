/*
 * Program: NanoOK
 * Author:  Richard M. Leggett
 * 
 * Copyright 2015 The Genome Analysis Centre (TGAC)
 */

package nanook;

import java.io.File;

/**
 * Parser for Minimap2 files
 * @author Richard Leggett
 */
public class Minimap2Parser extends SAMParser implements AlignmentFileParser {
    private String alignmentParams = "-x map-ont";
    private NanoOKOptions options;
    
    public Minimap2Parser(NanoOKOptions o, References r) {
        super(o, r);
        options = o;
    }
    
    public String getProgramID() {
        return "minimap2";
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
        return true;
    }
        
    public String getRunCommand(String query, String output, String reference) {
        //reference = reference.replaceAll("\\.fasta$", "");
        //reference = reference.replaceAll("\\.fa$", "");
        
        if (alignmentParams == "") {            
            return "minimap2 -a " + reference + ".mmi " + query;
        } else {
            return "minimap2 " + alignmentParams + " -a " + reference + ".mmi " + query;
        }
    }
    
    public void checkForIndex(String referenceFile) {
        File f = new File(referenceFile + ".fasta.mmi");

        System.out.println("Checking!!!");
        
        if (!f.exists()) {
            System.out.println("");
            System.out.println("Error:");
            System.out.println("Can't find file " + f.getPath());
            System.out.println("Have you indexed the reference with minimap2 -d ref.fasta.mmi ref.fasta?");
            System.exit(1);
        }

        return;
    }    
}
