/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nanook;

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
}
