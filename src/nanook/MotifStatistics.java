/*
 * Program: NanoOK
 * Author:  Richard M. Leggett
 * 
 * Copyright 2015 The Genome Analysis Centre (TGAC)
 */

package nanook;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import static nanook.NanoOKOptions.TYPE_2D;
import static nanook.NanoOKOptions.TYPE_COMPLEMENT;
import static nanook.NanoOKOptions.TYPE_TEMPLATE;

/**
 * Store all motif statistics (ie, insertion, deletion, substitution) at a range
 * of sizes (3, 4, 5) for a single read type (Template, Complement or 2D).
 * 
 * @author Richard Leggett
 */
public class MotifStatistics {
    private KmerMotifStatistic[] insertionMotifs = new KmerMotifStatistic[3];
    private KmerMotifStatistic[] deletionMotifs = new KmerMotifStatistic[3];
    private KmerMotifStatistic[] substitutionMotifs = new KmerMotifStatistic[3];
    
    /**
     * Constructor
     */
    public MotifStatistics() {
        for (int k=0; k<3; k++) {
            insertionMotifs[k] = new KmerMotifStatistic(k+3);
            deletionMotifs[k] = new KmerMotifStatistic(k+3);
            substitutionMotifs[k] = new KmerMotifStatistic(k+3);
        }
    }
    
    /**
     * Given a stretch of perfect sequence, store motifs at all k size.
     * @param motif KmerMotifStatistic object to add to
     * @param kmer perfect sequence to get motifs from
     */
    public void addMotifs(KmerMotifStatistic[] motif, String kmer) {
        if (kmer.length() < 3) {
            return;
        }
        
        for (int k=3; k<=5; k++) {
            if (kmer.length() > k) {
                motif[k-3].addMotif(kmer.substring(kmer.length() - k));
            }
        }
    }
    
    /**
     * Add a insertion motif.
     * @param kmer motif to add
     */
    public void addInsertionMotifs(String kmer) {
        addMotifs(insertionMotifs, kmer);
    }
    
    /**
     * Add a deletion motif.
     * @param kmer motif to add 
     */
    public void addDeletionMotifs(String kmer) {
        addMotifs(deletionMotifs, kmer);
    }

    /**
     * Add a substitution motif
     * @param kmer motif to add
     */
    public void addSubstitutionMotifs(String kmer) {
        addMotifs(substitutionMotifs, kmer);
    }

    /**
     * Output motif counts to screen (debugging).
     * @param motif KmerMotifStatistic object to get counts from
     */
    private void outputMotifCounts(KmerMotifStatistic[] motif) {
        for (int k=3; k<=5; k++) {
            System.out.println("k="+k);
            motif[k-3].outputMotifCounts();
        }        
    }
    
    /**
     * Output motif counts for all types (debugging).
     */
    public void outputAllMotifCounts() {
        System.out.println("Outputtng motif data");
        System.out.println("Insertions");
        outputMotifCounts(insertionMotifs);
        System.out.println("Deletions");
        outputMotifCounts(deletionMotifs);
        System.out.println("Substitutions");
        outputMotifCounts(substitutionMotifs);
    }
    
    /**
     * Get a sorted list of insertion motif counts at given kmer size.
     * @param k kmer size required
     * @return ArrayList of counts.
     */
    public ArrayList<Map.Entry<String, Integer>> getSortedInsertionMotifCounts(int k) {
        return insertionMotifs[k-3].getSortedMotifCounts();
    }

    /**
     * Get a sorted list of deletion motif counts at given kmer size.
     * @param k kmer size required
     * @return ArrayList of counts.
     */
    public ArrayList<Map.Entry<String, Integer>> getSortedDeletionMotifCounts(int k) {
        return deletionMotifs[k-3].getSortedMotifCounts();
    }

    /**
     * Get a sorted list of substitution motif counts at given kmer size.
     * @param k kmer size required
     * @return ArrayList of counts.
     */
    public ArrayList<Map.Entry<String, Integer>> getSortedSubstitutionMotifCounts(int k) {
        return substitutionMotifs[k-3].getSortedMotifCounts();
    }
    
    /**
     * Get a sorted list of insertion motif percentages at given kmer size.
     * @param k kmer size required
     * @return ArrayList of counts.
     */
    public ArrayList<Map.Entry<String, Double>> getSortedInsertionMotifPercentages(int k) {
        return insertionMotifs[k-3].getSortedMotifPercentages();
    }

    /**
     * Get a sorted list of deletion motif percentages at given kmer size.
     * @param k kmer size required
     * @return ArrayList of counts.
     */
    public ArrayList<Map.Entry<String, Double>> getSortedDeletionMotifPercentages(int k) {
        return deletionMotifs[k-3].getSortedMotifPercentages();
    }

    /**
     * Get a sorted list of substitution motif percentages at given kmer size.
     * @param k kmer size required
     * @return ArrayList of counts.
     */
    public ArrayList<Map.Entry<String, Double>> getSortedSubstitutionMotifPercentages(int k) {
        return substitutionMotifs[k-3].getSortedMotifPercentages();
    }    
    
    /**
     * Write insertion logo image (via KmerMotifStatistic object)
     * @param type either TYPE_TOP or TYPE_BOTTOM (Top 10 or bottom 10)
     * @param filename image filename
     * @param k kmer size
     */
    public void writeInsertionLogoImage(int type, String filename, int k) {
        insertionMotifs[k-3].writeLogoImage(type, filename);
    }

    /**
     * Write deletion logo image (via KmerMotifStatistic object)
     * @param type either TYPE_TOP or TYPE_BOTTOM (Top 10 or bottom 10)
     * @param filename image filename
     * @param k kmer size
     */
    public void writeDeletionLogoImage(int type, String filename, int k) {
        deletionMotifs[k-3].writeLogoImage(type, filename);
    }

    /**
     * Write substitution logo image (via KmerMotifStatistic object)
     * @param type either TYPE_TOP or TYPE_BOTTOM (Top 10 or bottom 10)
     * @param filename image filename
     * @param k kmer size
     */
    public void writeSubstitutionLogoImage(int type, String filename, int k) {
        substitutionMotifs[k-3].writeLogoImage(type, filename);
    }

    /**
     * Get total count of motifs seen
     * @param errorType type of error - TYPE_INSERTION etc.
     * @param k kmer size
     * @return count
     */
    public int getTotalMotifCounts(int errorType, int k) {
        int count = 0;
        
        switch(errorType) {
            case NanoOKOptions.TYPE_INSERTION:
                count = insertionMotifs[k-3].getTotalMotifCount();
                break;
            case NanoOKOptions.TYPE_DELETION:
                count = deletionMotifs[k-3].getTotalMotifCount();
                break;
            case NanoOKOptions.TYPE_SUBSTITUTION:
                count = substitutionMotifs[k-3].getTotalMotifCount();
                break;
            default:
                System.out.println("Error: bad error type in getTotalMotifCounts");
                System.exit(1);
                break;
        }
        
        return count;
    }
}
