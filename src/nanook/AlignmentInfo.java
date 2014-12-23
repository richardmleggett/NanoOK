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
    private double queryIdentity;
    private double alignmentIdentity;
    private double percentQueryAligned;
    
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
     */
    public AlignmentInfo(String hn, int hs, String qn, int qs, int ib, int l, int t, int c, int as) {
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
        queryIdentity = (100.0 * (double)identicalBases) / (double)querySize;
        alignmentIdentity = (100.0 * (double)identicalBases) / (double)alignmentSize;        
        percentQueryAligned = (100.0 * (double)alignmentSize) / (double)querySize;
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
    public int getLongest() {
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
    
    /**
     * Get alignment identity.
     * @return alignment identity percent
     */
    public double getAlignmentId() {
        return alignmentIdentity;
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
}
