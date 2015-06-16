/*
 * Program: NanoOK
 * Author:  Richard M. Leggett
 * 
 * Copyright 2015 The Genome Analysis Centre (TGAC)
 */

package nanook;

/**
 * Class representing an alignment line in a LAST file.
 * 
 * @author Richard Leggett
 */
public class MAFAlignmentLine {
    private String name;
    private int start;
    private int alnSize;
    private String strand;
    private int seqSize;
    private String alignment;
    
    /**
     * Constructor.
     * @param s - alignment line string
     */
    public MAFAlignmentLine(String s) {
        String[] parts = s.split("\\s+");

        if (parts.length == 7) {
            name = parts[1];
            start = Integer.parseInt(parts[2]);
            alnSize = Integer.parseInt(parts[3]);
            strand = parts[4];
            seqSize = Integer.parseInt(parts[5]);
            alignment = parts[6];
        } else {                
            System.out.println("Error: can't understand alignment file format.");
            System.exit(1);
        }
    }
    
    /**
     * Get name (ID) of sequence.
     * @return name, as String
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get start position of alignment.
     * @return start position
     */
    public int getStart() {
        return start;
    }
    
    /**
     * Get end position of alignment
     * @return end position
     */
    public int getEnd() {
        return start + alnSize - 1;
    }
    
    /**
     * Get alignment size.
     * @return alignment size, in bases
     */
    public int getAlnSize() {
        return alnSize;
    }
    
    /**
     * Get strand.
     * @return strand, "+" or "-"
     */
    public String getStrand() {
        return strand;
    }
    
    /**
     * Get sequence size.
     * @return sequence size, in bases.
     */
    public int getSeqSize() {
        return seqSize;
    }
    
    /**
     * Get alignment string.
     * @return alignment string
     */
    public String getAlignment() {
        return alignment;
    }
}
