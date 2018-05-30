/*
 * Program: NanoOK
 * Author:  Richard M. Leggett
 * 
 * Copyright 2015 The Genome Analysis Centre (TGAC)
 */

package nanook;

/**
 * Class to hold information about an alignment.
 * 
 * @author Richard Leggett
 */
public class AlignmentInfo {
    private String hitName;
    private int hitSize;
    private String queryName;
    private int querySize;
    private int identicalBases;
    private int longest;
    private double meanPerfectKmer;
    private int total;
    private int count;
    private int alignmentSize;
    private int alignmentSizeMinusIndels;
    private double queryIdentity;
    private double alignmentIdentity;
    private double alignmentIdentityMinusIndels;
    private double percentQueryAligned;
    private int queryAlignmentSize;
    int kSizes[];
    int kCounts[];
    int nk;

    
    /**
     * Constructor.
     * 
     * @param hn hit name
     * @param hs hit size
     * @param qn query name
     * @param qs query size
     * @param ib number of identical bases
     * @param l longest perfect kmer
     * @param t sum of perfect kmers
     * @param c count of perfect kmers
     * @param as alignment size
     * @param ad alignment size minus indels
     * @param qas query alignment size
     */
    public AlignmentInfo(String hn, int hs, String qn, int qs, int ib, int l, int t, int c, int as, int ami, int qas) {
        hitName = hn;
        hitSize = hs;
        querySize = qs;
        queryName = qn;
        identicalBases = ib;
        longest = l;
        total = t;
        count = c;
        meanPerfectKmer = (double)t / (double)c;
        alignmentSize = as;        
        alignmentSizeMinusIndels = ami;
        queryAlignmentSize = qas;
        queryIdentity = (100.0 * (double)identicalBases) / (double)querySize;
        alignmentIdentity = (100.0 * (double)identicalBases) / (double)alignmentSize;        
        alignmentIdentityMinusIndels = (100.0 * (double)identicalBases) / (double)alignmentSizeMinusIndels;        
        //percentQueryAligned = (100.0 * (double)alignmentSize) / (double)querySize;
        percentQueryAligned = (100.0 * (double)queryAlignmentSize) / (double)querySize;
    }
    
    /**
     * Get identical bases count.
     * @return number of identical bases
     */
    public int getIdenticalBases() {
        return identicalBases;
    }
    
    /**
     * Get longest perfect kmer.
     * @return longest perfect kmer
     */
    public int getLongestPerfectKmer() {
        return longest;
    }
    
    /**
     * Get alignment size.
     * @return alignment size, in bases
     */
    public int getAlignmentSize() {
        return alignmentSize;
    }
    
    /**
     * Get query identity.
     * @return query identity percent
     */
    public double getQueryId() {
        return queryIdentity;
    }
    
    public String getQueryName() {
        return queryName;
    }
    
    public String getHitName() {
        return hitName;
    }
    
    /**
     * Get alignment identity.
     * @return alignment identity percent
     */
    public double getAlignmentId() {
        return alignmentIdentity;
    }

    /**
     * Get alignment identity.
     * @return alignment identity percent
     */
    public double getAlignmentIdMinusIndels() {
        return alignmentIdentityMinusIndels;
    }    
    
    /**
     * Get mean perfect kmer size.
     * @return mean perfect kmer size, in bases
     */
    public double getMeanPerfectKmer() {
        return meanPerfectKmer;
    }
    
    /**
     * Get query size.
     * @return size of query, in bases.
     */
    public int getQuerySize() {
        return querySize;
    }
    
    /**
     * Get hit size.
     * @return size of hit, in bases
     */
    public int getHitSize() {
        return hitSize;
    }
    
    /**
     * Get percentage of query aligned.
     * @return percentage of hit sequence aligned
     */
    public double getPercentQueryAligned() {
        return percentQueryAligned;
    }
    
    public void addkCounts(int n, int[] s, int[] c) {
        nk = n;
        kSizes = s;
        kCounts = c;
    }
    
    public String getkCounts() {
        String s="";
        
        for (int i=0; i<nk; i++) {
            s = s + Integer.toString(kCounts[i]);
            if (i != (nk-1)) {
                s = s + "\t";
            }
        }
        
        return s;
    }
}
