package nanook;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

/**
 * Represents a sequence (contig) within a reference.
 * 
 * @author Richard leggett
 */
public class ReferenceSequence implements Comparable {
    private String id = null;
    private String name = null;
    private int size = 0;
    private int binSize = 500;
    private ReferenceSequenceStats referenceStats[] = new ReferenceSequenceStats[3];
    private KmerTable refKmerTable = new KmerTable(5);
    
    /**
     * Constructor
     * @param i sequence ID
     * @param s size (length) of sequence
     * @param n display name (may be difference to ID in file)
     */
    public ReferenceSequence(String i, int s, String n) {
        id = i;
        size = s;
        name = n;

        float b = size / 100;

        // Make a multiple of 10, 100 or 500...
        if (size < 50000) {
            binSize = 10 * (1 + Math.round(b / 10));   
        } else if (size < 500000) {
            binSize = 100 * (1 + Math.round(b / 100));   
        } else {
            binSize = 500 * (1 + Math.round(b / 500));   
        }
        
        for (int t=0; t<3; t++) {
            referenceStats[t] = new ReferenceSequenceStats(size, name);
        }
    }
    
    /**
     * Open alignment summary files for each reference for each type (Template, Complement, 2D).
     * 
     * @param analysisDir directory to write files to 
     */
    public void openAlignmentSummaryFiles(String analysisDir) {
        for (int t=0; t<3; t++) {
            referenceStats[t].openAlignmentsTableFile(analysisDir + File.separator + name + File.separator + name + "_" + NanoOKOptions.getTypeFromInt(t) + "_alignments.txt");
        }
    }
        
    /**
     * Get stats for a particular type (Template, Complement, 2D).
     * @param t integer type
     * @return ReferenceSequenceStats object
     */
    public ReferenceSequenceStats getStatsByType(int t) {
        return referenceStats[t];
    }
    
    /**
     * Get ID for this sequence.
     * @return ID String
     */
    public String getId() {
        return id;
    }
    
    /**
     * Get display name for this sequence.
     * @return name String
     */
    public String getName() {
        return name;
    }
    
    /**
     * Get size (length) of this sequence.
     * @return length
     */
    public int getSize() {
        return size;
    }
    
    /**
     * Get bin size for graph plotting
     * @return size (nt)
     */
    public int getBinSize() {
        return binSize;
    }

    public int compareTo(Object o) {
        ReferenceSequence r = (ReferenceSequence)o;
        return name.compareTo(r.getName());
    }

    /**
     * Get kmer table
     * @return 
     */
    public KmerTable getKmerTable() {
        return refKmerTable;
    }
    
    /**
     * 
     */
    public void writeKmerFile(int type, String filename) {
        KmerTable readKmerTable = referenceStats[type].getReadKmerTable();
        
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(filename)); 
            pw.println("Kmer\tRefCount\tReadCount\tRefPc\tReadPc");
            
            Set<String> refKeys = refKmerTable.getKeys();
            Set<String> readKeys = readKmerTable.getKeys();
            HashSet<String> allKeys = new HashSet();
            int refTotal = 0;
            int readTotal = 0;
        
            for (String kmer : refKeys) {    
                refTotal += refKmerTable.get(kmer);
                allKeys.add(kmer);
            }

            int count = 0;
            for (String kmer : readKeys) {
                readTotal += readKmerTable.get(kmer);
                if (! allKeys.contains(kmer)) {
                    allKeys.add(kmer);
                    count++;
                }
            }
                        
            for (String kmer : allKeys) {
                int refCount = refKmerTable.get(kmer);
                int readCount = readKmerTable.get(kmer);
                double refPc = 0; 
                double readPc = 0;
                
                if (refCount > 0) {
                    refPc = (100 * refCount) / (double)refTotal;
                }
                
                if (readCount > 0) {
                    readPc = (100 * readCount) / (double)readTotal;
                }
                
                referenceStats[type].addKmerAbundance(kmer, refPc, readPc);
                              
                pw.printf("%s\t%d\t%d\t%.4f\t%.4f", kmer, refCount, readCount, refPc, readPc);
                pw.println("");
            }
            
            pw.close();
        } catch (IOException e) {
            System.out.println("Exception:");
            e.printStackTrace();
            System.exit(1);
        }
    }
}
