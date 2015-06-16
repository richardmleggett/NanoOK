package nanook;

import java.util.Hashtable;
import java.util.Set;

/**
 *
 * @author leggettr
 */
public class KmerTable {
    private int kmerSize = 5;
    private Hashtable<String, Integer> counts = new Hashtable();
   
    public KmerTable(int k) {
        kmerSize = k;
    }
    
    public synchronized void countKmer(String kmer) {
        int count = 0;
        
        if (counts.containsKey(kmer)) {
            count = counts.get(kmer);
        }
        
        count++;
        
        counts.put(kmer, count);
    }
    
    public void writeKmerTable() {
        Set<String> keys = counts.keySet();
        
        System.out.println("");
        System.out.println("Writing kmer table...");
        
        for(String kmer : keys) {
            int count = counts.get(kmer);
            System.out.println(kmer + "\t" + count);
        }

        System.out.println("");    
    }
    
    public int getKmerSize() {
        return kmerSize;
    }
    
    public Set<String> getKeys() {
        return counts.keySet();
    }
    
    public int get(String kmer) {
        int value = 0;
        
        if (counts.containsKey(kmer)) {
            value = counts.get(kmer);
        }
        
        return value;
    }
    
    public Hashtable getTable() {
        return counts;
    }
}
