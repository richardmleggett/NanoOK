/*
 * Program: NanoOK
 * Author:  Richard M. Leggett
 * 
 * Copyright 2015 The Genome Analysis Centre (TGAC)
 */

package nanook;

/**
 * Parser for BLASR files
 * @author Richard Leggett
 */
public class BLASRParser extends SAMParser implements AlignmentFileParser {
    private String alignmentParams = "";
    
    public BLASRParser(NanoOKOptions o, References r) {
        super(o, r);
    }
    
    public String getProgramID() {
        return "blasr";
    }
    
    public int getReadFormat() {
        return NanoOKOptions.FASTA;
    }    
    
    public void setAlignmentParams(String p) {
        alignmentParams = p;
    }
        
    public String getRunCommand(String query, String output, String reference) {
        String command = "blasr " + query + " " + reference + " -sam -out " + output;
    
        if (alignmentParams.length() > 0) {
            command = command + alignmentParams;
        }
        
        return command;
    }
    
    public boolean outputsToStdout() {
        return false;
    }
}
