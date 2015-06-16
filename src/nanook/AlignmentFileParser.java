/*
 * Program: NanoOK
 * Author:  Richard M. Leggett
 * 
 * Copyright 2015 The Genome Analysis Centre (TGAC)
 */

package nanook;

import java.util.ArrayList;
import java.util.List;

/**
 * Interface for parsers of alignment files.
 * 
 * @author Richard Leggett
 */

public interface AlignmentFileParser {    
    /**
     * Get identifier for the alignment program
     * @return ID in lower case e.g. "last"
     */
    public String getProgramID();
    
    /**
     * Get file extension of alignments
     * @return 
     */
    public String getAlignmentFileExtension();
    
    /**
     * Get format of input reads expected
     * @return NanoOKOptions.FASTA or NanoOKOptions.FASTQ
     */
    public int getReadFormat();

    /**
     * Set alignment parameters to run executable
     * @return 
     */
    public void setAlignmentParams(String p);
    
    /**
     * Get command to run aligner
     * @param query query file
     * @param output output file
     * @param reference reference file
     * @return 
     */
    public String getRunCommand(String query, String output, String reference);
        
    /**
     * Parse an alignment file.
     * @param filename the filename of the alignments file
     * @param summaryFile the name of an alignments table summary file to write
     * @return 
     */
    int parseFile(String filename, AlignmentsTableFile summaryFile, ReadSetStats overallStats);
    
    /**
     * Sort alignments by score
     */
    void sortAlignments();
    
    /**
     * Get highest scoring set of alignments (ie. highest scoring reference)
     * @return an List of Alignment objects
     */
    List<Alignment> getHighestScoringSet();
    
    /**
     * Return true if this aligner outputs to Stdout and not a file
     * @return true or false
     */
    public boolean outputsToStdout();
}
