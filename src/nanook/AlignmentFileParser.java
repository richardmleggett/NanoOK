package nanook;

import java.util.ArrayList;

/**
 * Interface for parsers of alignment files.
 * 
 * @author Richard Leggett
 */

public abstract interface AlignmentFileParser {    
    /**
     * Parse an alignment file.
     * @param filename the filename of the alignments file
     * @param summaryFile the name of an alignments table summary file to write
     * @return 
     */
    abstract int parseFile(String filename, AlignmentsTableFile summaryFile);
    
    /**
     * Sort alignments by score
     */
    abstract void sortAlignments();
    
    /**
     * Get highest scoring set of alignments (ie. highest scoring reference)
     * @return an ArrayList of Alignment objects
     */
    abstract ArrayList getHighestScoringSet();
}
