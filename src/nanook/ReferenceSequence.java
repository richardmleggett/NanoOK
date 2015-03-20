package nanook;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Represents a sequence (contig) within a reference.
 * 
 * @author Richard leggett
 */
public class ReferenceSequence implements Comparable {
    private String id = null;
    private String name = null;
    private int size = 0;
    private int binSize = 500;
    private ReferenceSequenceStats referenceStats[] = new ReferenceSequenceStats[3];
    
    /**
     * Constructor
     * @param i sequence ID
     * @param s size (length) of sequence
     * @param n display name (may be difference to ID in file)
     */
    public ReferenceSequence(String i, int s, String n) {
        id = i;
        size = s;
        name = n;

        float b = size / 100;

        // Make a multiple of 10, 100 or 500...
        if (size < 50000) {
            binSize = 10 * (1 + Math.round(b / 10));   
        } else if (size < 500000) {
            binSize = 100 * (1 + Math.round(b / 100));   
        } else {
            binSize = 500 * (1 + Math.round(b / 500));   
        }
        
        for (int t=0; t<3; t++) {
            referenceStats[t] = new ReferenceSequenceStats(size, name);
        }
    }
    
    /**
     * Open alignment summary files for each reference for each type (Template, Complement, 2D).
     * 
     * @param analysisDir directory to write files to 
     */
    public void openAlignmentSummaryFiles(String analysisDir) {
        for (int t=0; t<3; t++) {
            referenceStats[t].openAlignmentsTableFile(analysisDir + File.separator + name + File.separator + name + "_" + NanoOKOptions.getTypeFromInt(t) + "_alignments.txt");
        }
    }
    
    /**
     * Close all alignment summary files.
     */
    public void closeAlignmentSummaryFiles() {
        for (int t=0; t<3; t++) {
            referenceStats[t].getAlignmentsTableFile().closeFile();
        }
    }
    
    /**
     * Get stats for a particular type (Template, Complement, 2D).
     * @param t integer type
     * @return ReferenceSequenceStats object
     */
    public ReferenceSequenceStats getStatsByType(int t) {
        return referenceStats[t];
    }
    
    /**
     * Get ID for this sequence.
     * @return ID String
     */
    public String getId() {
        return id;
    }
    
    /**
     * Get display name for this sequence.
     * @return name String
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get size (length) of this sequence.
     * @return length
     */
    public int getSize() {
        return size;
    }
    
    /**
     * Get bin size for graph plotting
     * @return size (nt)
     */
    public int getBinSize() {
        return binSize;
    }

    public int compareTo(Object o) {
        ReferenceSequence r = (ReferenceSequence)o;
        return name.compareTo(r.getName());
    }
}
