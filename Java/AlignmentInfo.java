package nanotools;

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
    
    public int getIdenticalBases() {
        return identicalBases;
    }
    
    public int getLongest() {
        return longest;
    }
    
    public int getAlignmentSize() {
        return alignmentSize;
    }
    
    public double getQueryId() {
        return queryIdentity;
    }
    
    public double getAlignmentId() {
        return alignmentIdentity;
    }
    
    public double getMeanPerfectKmer() {
        return meanPerfectKmer;
    }
    
    public int getQuerySize() {
        return querySize;
    }
    
    public int getHitSize() {
        return hitSize;
    }
    
    public double getPercentQueryAligned() {
        return percentQueryAligned;
    }
}
