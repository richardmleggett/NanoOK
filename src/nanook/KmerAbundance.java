package nanook;

/**
 *
 * @author leggettr
 */
public class KmerAbundance implements Comparable {
    String kmer;
    double refAbundance;
    double readAbundance;
    double difference;
    
    public KmerAbundance(String k, double ref, double read) {
        kmer = k;
        refAbundance = ref;
        readAbundance = read;
        difference = read - ref;
    }
    
    public double getDifference() {
        return difference;
    }
    
    public int compareTo(Object o) {
        double d = ((KmerAbundance)o).getDifference() - difference;
        int r = 0;
        
        if (d < 0) {
            r = -1;
        } else if (d > 0) {
            r = 1;
        }
        
        return r;
    }
    
    public String getKmer() {
        return kmer;
    }
    
    public double getRefAbundance() {
        return refAbundance;
    }
    
    public double getReadAbundance() {
        return readAbundance;
    }    
}
