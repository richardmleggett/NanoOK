package nanook;

/**
 * Abstract class used for parsers of alignment files.
 * 
 * @author Richard Leggett
 */

public abstract class AlignmentFileParser {    
    /**
     * Abstract method to parse an alignment file.
     * @param filename the filename of the alignments file
     * @param summaryFile the name of an alignments table summary file to write
     * @return 
     */
    abstract int parseFile(String filename, AlignmentsTableFile summaryFile);
}
