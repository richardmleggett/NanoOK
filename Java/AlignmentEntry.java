package nanotools;

public class AlignmentEntry {
    private int hitSize;
    private int querySize;
    private int identicalBases;
    private int longest;
    private double meanPerfectKmer;
    private int total;
    private int count;
    private int alignmentSize;
    private double queryId;
    private double alignmentId;
    
    public AlignmentEntry(int hs, int qs, int ib, int l, int t, int c, int as) {
        hitSize = hs;
        querySize = qs;
        identicalBases = ib;
        longest = l;
        total = t;
        count = c;
        meanPerfectKmer = (double)t / (double)c;
        alignmentSize = as;            
        queryId = (100.0 * (double)identicalBases) / (double)querySize;
        alignmentId = (100.0 * (double)identicalBases) / (double)alignmentSize;        
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
        return queryId;
    }
    
    public double getAlignmentId() {
        return alignmentId;
    }
    
    public double getMeanPerfectKmer() {
        return meanPerfectKmer;
    }
}
