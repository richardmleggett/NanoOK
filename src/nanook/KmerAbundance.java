/*
 * Program: NanoOK
 * Author:  Richard M. Leggett
 * 
 * Copyright 2015 The Genome Analysis Centre (TGAC)
 */

package nanook;

import java.io.Serializable;

/**
 * Represent abundance of a kmer
 * 
 * @author Richard Leggett
 */
public class KmerAbundance implements Comparable, Serializable {
    private static final long serialVersionUID = NanoOK.SERIAL_VERSION;
    private String kmer;
    private double refAbundance;
    private double readAbundance;
    private double difference;
    
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
