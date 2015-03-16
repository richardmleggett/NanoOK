package nanook;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Stores stats for each reference sequence, one object per read type (Template, Complement, 2D).
 * 
 * @author Richard Leggett
 */
public class ReferenceSequenceStats {
    private static final int MAX_INDEL = 100;
    private int size;
    private String name;
    private int[] coverage;
    private int[] perfectKmerCounts = new int[NanoOKOptions.MAX_KMER];
    private int[] readBestPerfectKmer = new int[NanoOKOptions.MAX_KMER];
    private int[] readCumulativeBestPerfectKmer = new int[NanoOKOptions.MAX_KMER];
    private int longestPerfectKmer = 0;
    private int nReadsWithAlignments = 0;
    private int totalReadBases = 0;
    private int totalAlignedBases = 0;
    private int totalAlignedBasesWithoutIndels = 0;
    private int totalIdentical = 0;
    private int nDeletionErrors = 0;
    private int nInsertionErrors = 0;
    private int nSubstitutionErrors = 0;
    private int nInsertedBases = 0;
    private int nDeletedBases = 0;
    private int largestInsertion = 0;
    private int largestDeletion = 0;
    private int insertionSizes[] = new int[MAX_INDEL];
    private int deletionSizes[] = new int[MAX_INDEL];
    private int alignedPositiveStrand = 0;
    private int alignedNegativeStrand = 0;
    private long totalBases = 0;
    private long totalReads = 0;
    private AlignmentsTableFile atf;

    /** 
     * Constructor.
     * @param size size (length) of reference
     * @param n name of reference
     */
    public ReferenceSequenceStats(int s, String n) {
        size = s;
        name = n;
        coverage = new int[size];
    }
    
    /**
     * Create an alignments table file.
     * @param filename flename
     */
    public void openAlignmentsTableFile(String filename) {
        atf = new AlignmentsTableFile(filename);
    }
    
    /**
     * Close the alignments table file.
     */
    public void closeAlignmentsTableFile() {
        atf.closeFile();
    }
    
    /**
     * Get the associated AlignmentsTableFile object
     * @return an AlignmentsTableFile
     */
    public AlignmentsTableFile getAlignmentsTableFile() {
            return  atf;
    }
    
    /**
     * Get number of reads with alignments.
     * @return number of reads
     */
    public int getNumberOfReadsWithAlignments() {
        return nReadsWithAlignments;
    }
    
    /**
     * Get longest perfect kmer length.
     * @return length longest perfect kmer, in bases
     */
    public int getLongestPerfectKmer() {
        return longestPerfectKmer;
    }

    /**
     * Clear all stats.
     */
    public void clearStats() {
        for (int i=0; i<NanoOKOptions.MAX_KMER; i++) {
            perfectKmerCounts[i] = 0;
            readBestPerfectKmer[i] = 0;
            readCumulativeBestPerfectKmer[i] = 0;
        }
        
        for (int i=0; i<size; i++) {
            coverage[i] = 0;
        }
        
        longestPerfectKmer = 0;
        nReadsWithAlignments = 0;
    }    
    
    /**
     * Store all perfect kmer sizes for later analysis.
     * @param size size of kmer
     */
    public void addPerfectKmer(int size) {
        if (size >= NanoOKOptions.MAX_KMER) {
            System.out.println("Error: very unlikely situation with perfect kmer of size " + size);
            System.exit(1);
        }
        
        perfectKmerCounts[size]++;
        
        if (size > longestPerfectKmer) {
            longestPerfectKmer = size;
        }
    }
    
    /**
     * Increment coverage between two points.
     * @param start start position
     * @param size size
     */
    public void addCoverage(int start, int size) {
        for (int i=start; i<(start+size); i++) {
            coverage[i]++;
        }
    }
    
    /**
     * Store best perfect kmer length for each read.
     * @param bestKmer length of best perfect kmer
     */
    public void addReadBestKmer(int bestKmer) {
        readBestPerfectKmer[bestKmer]++;
        
        for (int i=1; i<=bestKmer; i++) {
            readCumulativeBestPerfectKmer[i]++;
        }
        
        nReadsWithAlignments++;
    }
    
    /**
     * Write coverage file for later graph plotting.
     * @param filename output filename
     * @param binSize bin size
     */
    public void writeCoverageData(String filename, int binSize) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(filename));            
            for (int i=0; i<(size-binSize); i+=binSize) {
                int count = 0;
                for (int j=0; j<binSize; j++) {
                    count += coverage[i+j];
                }
                pw.printf("%d\t%.2f\n", i, ((double)count / (double)binSize));
            }            
            pw.close();
        } catch (IOException e) {
            System.out.println("writeCoverageData exception:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Write data for perfect kmer histogram.
     * @param filename output filename
     */
    public void writePerfectKmerHist(String filename) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(filename));
            for (int i=1; i<=longestPerfectKmer; i++) {
                pw.printf("%d\t%d\n", i, perfectKmerCounts[i]);
            }            
            pw.close();
        } catch (IOException e) {
            System.out.println("writePerfectKmerHist exception:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Write data for best perfect kmer histogram.
     * @param filename output filename
     */
    public void writeBestPerfectKmerHist(String filename) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(filename));
            for (int i=1; i<=longestPerfectKmer; i++) {
                double pc = 0;

                if ((readBestPerfectKmer[i] > 0) && (nReadsWithAlignments > 0)) {
                    pc = ((double)100.0 * readBestPerfectKmer[i]) / (double)nReadsWithAlignments;
                } 

                pw.printf("%d\t%d\t%.2f\n", i, readBestPerfectKmer[i], pc);
            }            
            pw.close();
        } catch (IOException e) {
            System.out.println("writeBestPerfectKmerHist exception:");
            e.printStackTrace();
            System.exit(1);
        }
    }    

    /**
     * Write data for best perfect kmer cumulative histogram.
     * @param filename output filename
     */
    public void writeBestPerfectKmerHistCumulative(String filename) {
        int nr = 0;
        
        for (int i=1; i<=longestPerfectKmer; i++) {
            nr += readBestPerfectKmer[i];
        }
        
        if (nReadsWithAlignments != nr) {
            System.out.println("Discrepancy: "+nr+" not equal to "+nReadsWithAlignments);
        }
        
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(filename));
            for (int i=1; i<=longestPerfectKmer; i++) {
                double pc = 0;
                
                if ((readCumulativeBestPerfectKmer[i]> 0) && (nReadsWithAlignments > 0)){
                    pc = ((double)100.0 * readCumulativeBestPerfectKmer[i]) / (double)nr; //(double)nReadsWithAlignments;
                }
                
                pw.printf("%d\t%d\t%.2f\n", i, readCumulativeBestPerfectKmer[i], pc);
            }            
            pw.close();
        } catch (IOException e) {
            System.out.println("writeBestPerfectKmerHistCumulative exception:");
            e.printStackTrace();
            System.exit(1);
        }
    }    
    /**
     * Write a line to the reference sequence summary file.
     * @param pw PrintWriter object to write with
     * @param format format string for output
     */
    public void writeSummary(PrintWriter pw, String format) {
        pw.printf(format, name, size, nReadsWithAlignments, longestPerfectKmer);
    }
        
    /**
     * Get mean read length
     * @return mean read length
     */
    public double getMeanReadLength() {
        if (nReadsWithAlignments > 0) {
            return (double)totalReadBases / (double)nReadsWithAlignments;
        } else {
            return 0.0;
        }
    }
    
    /**
     * Store alignment stats.
     * @param querySize query size
     * @param alignedSize number of aligned bases
     * @param identicalBases number of identical bases
     */
    public void addAlignmentStats(int querySize, int alignedSize, int alignedSizeMinusIndels, int identicalBases, String hitStrand, String queryStrand) {
        totalAlignedBases += alignedSize;
        totalAlignedBasesWithoutIndels += alignedSizeMinusIndels;
        totalReadBases += querySize;
        totalIdentical += identicalBases;
        
        if (hitStrand.equals("+")) {
            if (queryStrand.equals("+")) {
                alignedPositiveStrand++;
            } else if (queryStrand.equals("-")) {
                alignedNegativeStrand++;
            }
        }
    }
    
    /** 
     * Store a deletion error.
     * @param size - size of deletion
     * @param kmer - kmer before error
     * @param stats - ReadSetStats associated with the error
     */
    public void addDeletionError(int size, String kmer, ReadSetStats stats) {
        nDeletionErrors++;
        nDeletedBases += size;
        deletionSizes[size]++;
        if (size > largestDeletion) {
            largestDeletion = size;
        }
        stats.addDeletionError(size, kmer);
    }
    
    /** 
     * Store an insertion error.
     * @param size - size of insertion
     * @param kmer - kmer before error
     * @param stats - ReadSetStats associated with the error
     */
    public void addInsertionError(int size, String kmer, ReadSetStats stats) {
        nInsertionErrors++;
        nInsertedBases += size;
        insertionSizes[size]++;
        if (size > largestInsertion) {
            largestInsertion = size;
        }
        stats.addInsertionError(size, kmer);
    }
    
    /** 
     * Store a substitution error.
     * @param kmer - kmer before error
     * @param refChar - reference base
     * @param subChar - substituted base
     * @param stats - ReadSetStats associated with the error
     */
    public void addSubstitutionError(String kmer, char refChar, char subChar, ReadSetStats stats) {
        nSubstitutionErrors++;
        //System.out.println("Kmer before substitution "+kmer);
        stats.addSubstitutionError(kmer, refChar, subChar);
    }
    
    /**
     * Get percent identity of aligned bases.
     * @return identity
     */
    public double getAlignedPercentIdentical() {
        if ((totalIdentical == 0) || (totalAlignedBases == 0)) {
            return 0;
        } else {           
            return (100.0 * totalIdentical) / totalAlignedBases;
        }
    }

    /**
     * Get percent identity of aligned bases.
     * @return identity
     */
    public double getAlignedPercentIdenticalWithoutIndels() {
        if ((totalIdentical == 0) || (totalAlignedBasesWithoutIndels == 0)) {
            return 0;
        } else {           
            return (100.0 * totalIdentical) / totalAlignedBasesWithoutIndels;
        }
    }    
    
    /**
     * Get percent identity of read.
     * @return identity
     */
    public double getReadPercentIdentical() {
        if ((totalIdentical == 0) || (totalReadBases == 0)) {
            return 0;
        } else {
            return (100.0 * totalIdentical) / totalReadBases;
        }
    }

    /**
     * Getnumber of insertion errors.
     * @return number
     */
    public int getNumberOfInsertionErrors() {
        return nInsertionErrors;
    }

    /**
     * Get number of deletion errors.
     * @return number
     */
    public int getNumberOfDeletionErrors() {
        return nDeletionErrors;
    }
    
    /**
     * Get number of substitution errors.
     * @return number
     */
    public int getNumberOfSubstitutionErrors() {
        return nSubstitutionErrors;
    }
    
    /**
     * Get percentage of insertion errors
     * @return percentage
     */
    public double getPercentInsertionErrors() {
        if ((nInsertedBases == 0) || (totalAlignedBases == 0)) {
            return 0;
        } else {
            return (100.0 * nInsertedBases) / (totalAlignedBases);
        }
    }

    /** 
     * Get percentage of deletion errors
     * @return percentage
     */
    public double getPercentDeletionErrors() {
        if ((nDeletedBases == 0) || (totalAlignedBases == 0)) {
            return 0;
        } else {
            return (100.0 * nDeletedBases) / (totalAlignedBases);
        }
    }
    
    /**
     * Get percentage of substitution errors
     * @return percentage
     */
    public double getPercentSubstitutionErrors() {
        if ((nSubstitutionErrors == 0) || (totalAlignedBases == 0)) {
            return 0;
        } else {
           return (100.0 * nSubstitutionErrors) / (totalAlignedBases);
        }
    }  
    
    /**
     * Get the number of aligned bases
     * @return number of bases
     */
    public int getTotalAlignedBases() {
        return totalAlignedBases;
    }
    
    /**
     * Write a file of insertion stats for plotting.
     * @param filename output filename
     */
    public void writeInsertionStats(String filename) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(filename)); 
            for (int i=1; i<=largestInsertion; i++) {
                //pw.println(i + "\t" + insertionSizes[i]);
                pw.printf("%d\t%.4f\n", i, (100.0 * (double)insertionSizes[i]/(double)nInsertionErrors));
            }
            pw.close();
        } catch (IOException e) {
            System.out.println("writeInsertionStats exception:");
            e.printStackTrace();
            System.exit(1);
        }                
    }

    /**
     * Write a file of deletion stats for plotting.
     * @param filename output filename
     */
    public void writeDeletionStats(String filename) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(filename));
            for (int i=1; i<=largestDeletion; i++) {
                //pw.println(i + "\t" + deletionSizes[i]);
                pw.printf("%d\t%.4f\n", i, (100.0 * (double)deletionSizes[i]/(double)nDeletionErrors));            }
            pw.close();
        } catch (IOException e) {
            System.out.println("writeInsertionStats exception:");
            e.printStackTrace();
            System.exit(1);
        }                
    }    
    
    /**
     * Get percent of reads aligned on +ve strand
     * @return count
     */
    public double getAlignedPositiveStrandPercent() {
        if (alignedPositiveStrand > 0) {
            return (100.0 * (double)alignedPositiveStrand)/(double)(alignedPositiveStrand + alignedNegativeStrand);
        } else {
            return 0;
        }
    }
    
    /**
     * Get percent of reads aligned on -ve strand
     * @return count
     */
    public double getAlignedNegativeStrandPercent() {
        if (alignedNegativeStrand > 0) {
            return (100.0 * (double)alignedNegativeStrand)/(double)(alignedPositiveStrand + alignedNegativeStrand);
        } else {
            return 0;
        }
    }
}
