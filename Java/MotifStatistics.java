package nanotools;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;
import static nanotools.NanoOKOptions.TYPE_2D;
import static nanotools.NanoOKOptions.TYPE_COMPLEMENT;
import static nanotools.NanoOKOptions.TYPE_TEMPLATE;

public class MotifStatistics {
    private KmerMotifStatistic[] insertionMotifs = new KmerMotifStatistic[3];
    private KmerMotifStatistic[] deletionMotifs = new KmerMotifStatistic[3];
    private KmerMotifStatistic[] substitutionMotifs = new KmerMotifStatistic[3];
    
    public MotifStatistics() {
        for (int k=0; k<3; k++) {
            insertionMotifs[k] = new KmerMotifStatistic(k+3);
            deletionMotifs[k] = new KmerMotifStatistic(k+3);
            substitutionMotifs[k] = new KmerMotifStatistic(k+3);
        }
    }
    
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
        
    public void addInsertionMotifs(String kmer) {
        addMotifs(insertionMotifs, kmer);
    }
    
    public void addDeletionMotifs(String kmer) {
        addMotifs(deletionMotifs, kmer);
    }

    public void addSubstitutionMotifs(String kmer) {
        addMotifs(substitutionMotifs, kmer);
    }

    private void outputMotifCounts(KmerMotifStatistic[] motif) {
        for (int k=3; k<=5; k++) {
            System.out.println("k="+k);
            motif[k-3].outputMotifCounts();
        }        
    }
    
    public void outputAllMotifCounts() {
        System.out.println("Outputtng motif data");
        System.out.println("Insertions");
        outputMotifCounts(insertionMotifs);
        System.out.println("Deletions");
        outputMotifCounts(deletionMotifs);
        System.out.println("Substitutions");
        outputMotifCounts(substitutionMotifs);
    }
    
    public ArrayList<Map.Entry<String, Integer>> getSortedInsertionMotifCounts(int k) {
        return insertionMotifs[k-3].getSortedMotifCounts();
    }

    public ArrayList<Map.Entry<String, Integer>> getSortedDeletionMotifCounts(int k) {
        return deletionMotifs[k-3].getSortedMotifCounts();
    }

    public ArrayList<Map.Entry<String, Integer>> getSortedSubstitutionMotifCounts(int k) {
        return substitutionMotifs[k-3].getSortedMotifCounts();
    }
    
    public ArrayList<Map.Entry<String, Double>> getSortedInsertionMotifPercentages(int k) {
        return insertionMotifs[k-3].getSortedMotifPercentages();
    }

    public ArrayList<Map.Entry<String, Double>> getSortedDeletionMotifPercentages(int k) {
        return deletionMotifs[k-3].getSortedMotifPercentages();
    }

    public ArrayList<Map.Entry<String, Double>> getSortedSubstitutionMotifPercentages(int k) {
        return substitutionMotifs[k-3].getSortedMotifPercentages();
    }    
    
    public void writeInsertionLogoImage(int type, String filename, int k) {
        insertionMotifs[k-3].writeLogoImage(type, filename);
    }

    public void writeDeletionLogoImage(int type, String filename, int k) {
        deletionMotifs[k-3].writeLogoImage(type, filename);
    }

    public void writeSubstitutionLogoImage(int type, String filename, int k) {
        substitutionMotifs[k-3].writeLogoImage(type, filename);
    }

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
