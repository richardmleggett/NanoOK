package nanook;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

/**
 * Represent statistics about a read set (for example Template read set).
 * 
 * @author Richard Leggett
 */
public class ReadSetStats {
    NanoOKOptions options;
    private PrintWriter pw;
    private String typeString = "";
    private int longest = 0;
    private int shortest = NanoOKOptions.MAX_READ_LENGTH;
    private int basesSum = 0;
    private double meanLength = 0;
    private int n50 = 0;
    private int n50Count = 0;
    private int n90 = 0;
    private int n90Count = 0;
    private int[] lengths = new int[NanoOKOptions.MAX_READ_LENGTH];
    private int nReads = 0;
    private int nReadFiles = 0;
    private int nReadsWithAlignments = 0;
    private int nReadsWithoutAlignments = 0;
    private int[] readBestPerfectKmer = new int[NanoOKOptions.MAX_KMER];
    private int[] readCumulativeBestPerfectKmer = new int[NanoOKOptions.MAX_KMER];
    private MotifStatistics motifStats = new MotifStatistics();
    private int substitutionErrors[][] = new int[4][4];
    private int nSubstitutions = 0;
    private int nInsertions = 0;
    private int nDeletions = 0;
   
    /**
     * Constructor
     * @param o NanoOKOptions object
     * @param t Type integer (defined in NanoOKOptions)
     */
    public ReadSetStats(NanoOKOptions o, int t) {
        options=o;
        typeString = NanoOKOptions.getTypeFromInt(t);
        for (int i=0; i<NanoOKOptions.MAX_KMER; i++) {
            readBestPerfectKmer[i] = 0;
            readCumulativeBestPerfectKmer[i] = 0;
        }
    }

    /**
     * Open a text file to store read lengths.
     */
    public void openLengthsFile() {
        String filename = options.getAnalysisDir() + options.getSeparator() + "all_" + typeString + "_lengths.txt";
        try {
            pw = new PrintWriter(new FileWriter(filename)); 
        } catch (IOException e) {
            System.out.println("openLengthsFile exception:");
            e.printStackTrace();
            System.exit(1);
        }        
    }
    
    /**
     * Close the read lengths file.
     */
    public void closeLengthsFile() {
        pw.close();
    }

    /**
     * Calculate various statistics, e.g. N50 etc.
     */
    public void calculateStats() {
        int total = 0;
        int c = 0;
                
        meanLength = (double)basesSum / (double)nReads;        
        
        for (int i=longest; i>0; i--) {
            for (int j=0; j<lengths[i]; j++) {
                total += i;
                c++;
                
                if ((n50 == 0) && ((double)total >= ((double)basesSum * 0.5))) {
                    n50 = i;
                    n50Count = c;
                }

                if ((n90 == 0) && ((double)total >= ((double)basesSum * 0.9))) {
                    n90 = i;
                    n90Count = c;
                }        
            }
        }
        
    }
    
    /**
     * Update count of read files.
     */
    public void addReadFile() {
       nReadFiles++;
    }
   
    /**
     * Get type as a string.
     * @return type String
     */
    public String getTypeString() {
        return typeString;
    }
    
    /**
     * Get mean length of reads in this read set.
     * @return length
     */
    public double getMeanLength() {
        return meanLength;
    }
    
    /**
     * Get longest read in this read set.
     * @return length
     */
    public int getLongest() {
        return longest;
    }
    
    /**
     * Get shortest read in this read set.
     * @return length
     */
    public int getShortest() {
        return shortest;
    }
    
    /**
     * Get N50 for this read set.
     * @return N50 length
     */
    public int getN50() {
        return n50;
    }
    
    /**
     * Get N50 count - number of reads of length N50 or greater.
     * @return count
     */
    public int getN50Count() {
        return n50Count;
    }
    
    /**
     * Get N90 for this read set.
     * @return N90 length
     */
    public int getN90() {
        return n90;
    }
    
    /**
     * Get N90 count - number of reads of length N90 or greater.
     * @return count
     */
    public int getN90Count() {
        return n90Count;
    }
    
    /**
     * Get number of reads.
     * @return number of reads
     */
    public int getNumReads() {
        return nReads;
    }
        
    /**
     * Get total bases represented by read set.
     * @return number of bases
     */
    public int getTotalBases() {
        return basesSum;
    }    
    
    /**
     * Get number of read files.
     * @return number of files
     */
    public int getNumReadFiles() {
        return nReadFiles;
    }    
    
    /**
     * Store a read length in the array of read lengths.
     * @param id ID of read
     * @param l length
     */
    public void addLength(String id, int l) {
        lengths[l]++;
        
        pw.println(id + "\t" + l);
        
        if (l > longest) {
            longest = l;
        }
        
        if (l < shortest) {
            shortest = l;
        }
        
        basesSum += l;
        //nReads++;
    }    
        
    /**
     * Store a read with an alignment.
     */
    public void addReadWithAlignment() {
        nReads++;
        nReadsWithAlignments++;
    }

    /**
     * Store a read without an alignment.
     */
    public void addReadWithoutAlignment() {
        nReads++;
        nReadsWithoutAlignments++;
    }
        
    /**
     * Store best perfect kmers for each read.
     * @param bestKmer length of best perfect kmer
     */
    public void addReadBestKmer(int bestKmer) {
        if (bestKmer >= NanoOKOptions.MAX_KMER) {
            System.out.println("Error: the unlikely event of a best kmer size of "+bestKmer+" has happened!");
            System.exit(1);
        }
        
        readBestPerfectKmer[bestKmer]++;
        
        for (int i=0; i<bestKmer; i++) {
            readCumulativeBestPerfectKmer[i]++;
        }
    }
        
    /**
     * Get number of reads in this read set.
     * @return number of reads.
     */
    public int getNumberOfReads() {
        return nReads;
    }
    
    /**
     * Get number of reads with alignments in this read set.
     * @return number of reads
     */
    public int getNumberOfReadsWithAlignments() {
        return nReadsWithAlignments;
    }
    
    /**
     * Get number of reads without alignments in this read set.
     * @return number of reads
     */
    public int getNumberOfReadsWithoutAlignments() {
        return nReadsWithoutAlignments;
    }
    
    /**
     * Get percentage of reads with alignments
     * @return percentage of reads
     */
    public double getPercentOfReadsWithAlignments() {
        return (100.0 * (double)nReadsWithAlignments) / (double)nReads;
    }
    
    /**
     * Get percentage of reads without alignments
     * @return percentage of reads
     */
    public double getPercentOfReadsWithoutAlignments() {
        return (100.0 * (double)nReadsWithoutAlignments) / (double)nReads;
    }    
    
    /**
     * Print statistics to screen.
     */
    public void printStats() {
        System.out.println("Parse " + typeString + " alignments");
        System.out.println(typeString + " reads: " + nReads);
        System.out.println(typeString + " reads with alignments: " + nReadsWithAlignments);
        System.out.println(typeString + " reads without alignments: " + nReadsWithoutAlignments);
    }
    
    /**
     * Write a short summary file for this read set.
     * @param filename output filename
     */
    public void writeSummaryFile(String filename) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(filename, true));
            pw.println("");
            pw.printf("%s alignments\n\n", typeString);
            pw.printf("Num reads: %d\n", nReads);
            pw.printf("Num reads with alignments: %d\n", nReadsWithAlignments);
            pw.printf("Num reads without alignments: %d\n", nReadsWithoutAlignments);
            pw.close();
        } catch (IOException e) {
            System.out.println("writeSummaryFile exception:");
            e.printStackTrace();
            System.exit(1);
        }        
    }
    
    /**
     * Store a deletion error.
     * @param size size of deletion
     * @param kmer kmer prior to error
     */
    public void addDeletionError(int size, String kmer) {
        motifStats.addDeletionMotifs(kmer);
        nDeletions++;
    }
    
    /**
     * Store an insertion error.
     * @param size size of insertion
     * @param kmer kmer prior to error
     */
    public void addInsertionError(int size, String kmer) {
        motifStats.addInsertionMotifs(kmer);
        nInsertions++;
    }
    
    /** 
     * Store a substitution error.
     * @param kmer kmer prior to error
     * @param refChar reference base
     * @param subChar substituted base
     */
    public void addSubstitutionError(String kmer, char refChar, char subChar) {
        int r = -1;
        int s = -1;
        
        motifStats.addSubstitutionMotifs(kmer);
        
        switch(refChar) {
            case 'A': r=0; break;
            case 'C': r=1; break;
            case 'G': r=2; break;
            case 'T': r=3; break;
            default: break; //System.out.println("Warning: Unknown base ("+refChar+") in reference"); break;
        }

        switch(subChar) {
            case 'A': s=0; break;
            case 'C': s=1; break;
            case 'G': s=2; break;
            case 'T': s=3; break;
            default: System.out.println("Warning: Unknown base ("+refChar+") in read"); break;
        }
           
        if ((r >= 0) && (s >= 0)) {
            nSubstitutions++;
            substitutionErrors[r][s]++;
        }
    }
    
    /**
     * Get substitution error matrix (A, C, G, T vs A, C, G, T).
     * @return Substitution error matrix
     */
    public int[][] getSubstitutionErrors() {
        return substitutionErrors;
    }
    
    /**
     * Get number of substitutions.
     * @return number
     */
    public int getNumberOfSubstitutions() {
        return nSubstitutions;
    }
    
    /**
     * Write motif stats to screen.
     */
    public void outputMotifStats() {
        motifStats.outputAllMotifCounts();
    }
    
    /**
     * Get motif statistics.
     * @return MotifStatistics object
     */
    public MotifStatistics getMotifStatistics() {
        return motifStats;
    }
}
