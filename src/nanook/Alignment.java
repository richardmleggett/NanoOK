package nanook;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Generic class to represent alignment
 * @author Richard Leggett
 */
public class Alignment implements Comparable {
    private int score;
    private String queryName;
    private int querySequenceSize;
    private int queryStart;
    private int queryAlignmentSize;
    private int queryEnd;
    private String queryStrand;
    private String hitName;
    private int hitSequenceSize;
    private int hitStart;
    private int hitAlignmentSize;
    private int hitEnd;
    private String hitStrand;
    private String queryString;
    private String hitString;
    boolean fIsCIGAR;
    
    public Alignment(int s, String qName, int qSize, int qStart, int qAlnSize, String qs, String hName, int hSize, int hStart, int hAlnSize, String hs, boolean cigar) {
        score = s;
        queryName = qName;
        querySequenceSize = qSize;
        queryStart = qStart;
        queryAlignmentSize = qAlnSize;
        queryEnd = qStart + qAlnSize - 1;
        queryString = qs;
        hitName = hName;
        hitSequenceSize = hSize;
        hitStart = hStart;
        hitAlignmentSize = hAlnSize;
        hitEnd = hStart + hAlnSize - 1;
        hitString = hs;
        fIsCIGAR = cigar;
        queryStrand = "+";
        hitStrand = "+";
    }
    
    public void setQueryStrand(String s) {
        queryStrand = s;
    }
    
    public void setHitStrand(String s) {
        hitStrand = s;
    }

    public String getQueryStrand() {
       return queryStrand;
    }
    
    public String getHitStrand() {
        return hitStrand;
    }
    
    public int getScore() {
        return score;
    }
    
    public String getQueryName() {
        return queryName;
    }
    
    public int getQuerySequenceSize() {
        return querySequenceSize;
    }
    
    public int getQueryStart() {
        return queryStart;
    }
    
    public int getQueryAlignmentSize() {
        return queryAlignmentSize;
    }
    
    public int getQuertEnd() {
        return queryEnd;
    }
    
    public String getQueryString() {
        return queryString;
    }

    public String getHitName() {
        return hitName;
    }
    
    public int getHitSequenceSize() {
        return hitSequenceSize;
    }
    
    public int getHitStart() {
        return hitStart;
    }
    
    public int getHitAlignmentSize() {
        return hitAlignmentSize;
    }
    
    public int getHitEnd() {
        return hitEnd;
    }
    
    public String getHitString() {
        return hitString;
    }    
    
    public boolean isCIGAR() {
        return fIsCIGAR;
    }
    
    public void writeMafFile(String filename) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(filename)); 
            pw.printf("s %24s %5d %5d %s %5d %s", hitName, hitStart, hitAlignmentSize, hitStrand, hitSequenceSize, hitString);
            pw.println("");
            pw.printf("s %24s %5d %5d %s %5d %s", queryName, queryStart, queryAlignmentSize, queryStrand, querySequenceSize, queryString);
            pw.println("");
            pw.close();
        } catch (IOException e) {
            System.out.println("ReportWriter exception:");
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    @Override
    public int compareTo(Object o) {
        return ((Alignment)o).getScore() - score;
    } 
}
