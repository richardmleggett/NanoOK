/*
 * Program: NanoOK
 * Author:  Richard M. Leggett
 * 
 * Copyright 2015 The Genome Analysis Centre (TGAC)
 */

package nanook;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Map;

/**
 * Represent statistics about a read set (for example Template read set).
 * 
 * @author Richard Leggett
 */
public class ReadSetStats implements Serializable {
    private static final long serialVersionUID = NanoOK.SERIAL_VERSION;
    NanoOKOptions options;
    private transient PrintWriter pwLengths = null;
    private transient PrintWriter pwKmers = null;
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
    private Hashtable<String,Integer> readLengths = new Hashtable();
    private Hashtable<String,Double> readGC = new Hashtable();
    private int nReads = 0;
    private int nReadFiles = 0;
    private int nPassFiles = 0;
    private int nFailFiles = 0;
    private int nReadsWithAlignments = 0;
    private int nReadsWithoutAlignments = 0;
    private int[] readBestPerfectKmer = new int[NanoOKOptions.MAX_KMER];
    private int[] readCumulativeBestPerfectKmer = new int[NanoOKOptions.MAX_KMER];
    private MotifStatistics motifStats = new MotifStatistics();
    private int substitutionErrors[][] = new int[4][4];
    private int nSubstitutions = 0;
    private int nInsertions = 0;
    private int nDeletions = 0;
    private int ignoredDuplicates = 0;
    private int type;
   
    /**
     * Constructor
     * @param o NanoOKOptions object
     * @param t Type integer (defined in NanoOKOptions)
     */
    public ReadSetStats(NanoOKOptions o, int t) {
        options=o;
        type = t;
        typeString = NanoOKOptions.getTypeFromInt(type);
        for (int i=0; i<NanoOKOptions.MAX_KMER; i++) {
            readBestPerfectKmer[i] = 0;
            readCumulativeBestPerfectKmer[i] = 0;
        }
    }

    /**
     * Open a text file to store read lengths.
     */
    public synchronized void openLengthsFile() {
        String lengthsFilename = options.getAnalysisDir() + File.separator + "all_" + typeString + "_lengths.txt";
        String kmersFilename = options.getAnalysisDir() + File.separator + "all_" + typeString + "_kmers.txt";
        
        try {
            pwLengths = new PrintWriter(new FileWriter(lengthsFilename)); 
            pwKmers = new PrintWriter(new FileWriter(kmersFilename));
            pwKmers.write("Id\tLength\tnk15\tnk17\tnk19\tnk21\tnk23\tnk25");
            pwKmers.println("");
        } catch (IOException e) {
            System.out.println("openLengthsFile exception:");
            e.printStackTrace();
            System.exit(1);
        }        
    }
    
    /**
     * Close the read lengths file.
     */
    public synchronized void closeLengthsFile() {
        pwLengths.close();
    }
    
    /**
     * Close the kmers file
     */
    public synchronized void closeKmersFile() {
        pwKmers.close();
    }

    /**
     * Calculate various statistics, e.g. N50 etc.
     */
    public synchronized void calculateStats() {
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
     * @param type 
     */
    public synchronized void addReadFile(int type) {
       nReadFiles++;
       
       if (type == NanoOKOptions.READTYPE_PASS) {
           nPassFiles++;
       } else if (type == NanoOKOptions.READTYPE_FAIL) {
           nFailFiles++;
       }
    }
    
    /**
     * Get number of read files in pass directory
     * @return Number of files in pass directory
     */
    public synchronized int getNumberOfPassFiles() {
        return nPassFiles;        
    }

    /**
     * Get number of read files in fail directory
     * @return Number of files in fail directory
     */
    public synchronized int getNumberOfFailFiles() {
        return nFailFiles;        
    }    
    
    /**
     * Get type
     * @return type
     */
    public synchronized int getType() {
        return type;
    }
    
    /**
     * Get type as a string.
     * @return type String
     */
    public synchronized String getTypeString() {
        return typeString;
    }
    
    /**
     * Get mean length of reads in this read set.
     * @return length
     */
    public synchronized double getMeanLength() {
        return meanLength;
    }
    
    /**
     * Get longest read in this read set.
     * @return length
     */
    public synchronized int getLongest() {
        return longest;
    }
    
    /**
     * Get shortest read in this read set.
     * @return length
     */
    public synchronized int getShortest() {
        return shortest;
    }
    
    /**
     * Get N50 for this read set.
     * @return N50 length
     */
    public synchronized int getN50() {
        return n50;
    }
    
    /**
     * Get N50 count - number of reads of length N50 or greater.
     * @return count
     */
    public synchronized int getN50Count() {
        return n50Count;
    }
    
    /**
     * Get N90 for this read set.
     * @return N90 length
     */
    public synchronized int getN90() {
        return n90;
    }
    
    /**
     * Get N90 count - number of reads of length N90 or greater.
     * @return count
     */
    public synchronized int getN90Count() {
        return n90Count;
    }
    
    /**
     * Get number of reads.
     * @return number of reads
     */
    public synchronized int getNumReads() {
        return nReads;
    }
        
    /**
     * Get total bases represented by read set.
     * @return number of bases
     */
    public synchronized int getTotalBases() {
        return basesSum;
    }    
    
    /**
     * Get number of read files.
     * @return number of files
     */
    public synchronized int getNumReadFiles() {
        return nReadFiles;
    }    
    
    private String getPrefix(String path) {
        String leafname = new File(path).getName();
        leafname.replaceAll(":", "_");
        return leafname.substring(0, leafname.indexOf(".fa"));
    }
    
    /**
     * Store a read length in the array of read lengths.
     * @param id ID of read
     * @param l length
     */
    public synchronized void addLength(String readPath, String id, int l, double gc) {
        
        lengths[l]++;
        
        pwLengths.println(id + "\t" + l);
        
        id = getPrefix(readPath) + ":"+id;
        
        if (l > longest) {
            longest = l;
        }
        
        if (l < shortest) {
            shortest = l;
        }
        
        basesSum += l;
        nReads++;
        
        if (readLengths.containsKey(id)) {
            System.out.println("Error: Read ID "+id+"  . This occurrance ignored.");
            ignoredDuplicates++;
        } else {
            readLengths.put(id, l);
            readGC.put(id, gc);
        }
    }    
        
    /**
     * Get length of read
     * @param id of read
     * @return length, in bases
     */
    public synchronized int getReadLength(String alignmentFile, String id) {
        int length = -1;

        id = getPrefix(alignmentFile) + ":"+id;

        Integer l = readLengths.get(id);
        
        if (l != null) {
            length = l.intValue();
        }
        
        return length;
    }

    /**
     * Get GC of read
     * @param id of read
     * @return GC percent
     */
    public synchronized double getGC(String alignmentFile, String id) {
        double gc = -1;

        id = getPrefix(alignmentFile) + ":"+id;
        
        Double g = readGC.get(id);
        
        if (g != null) {
            gc = g.intValue();
        }
        
        return gc;
    }    
    
    /**
     * Store a read with an alignment.
     */
    public synchronized void addReadWithAlignment() {
        nReadsWithAlignments++;
    }

    /**
     * Store a read without an alignment.
     */
    public synchronized void addReadWithoutAlignment() {
        nReadsWithoutAlignments++;
    }
        
    /**
     * Store best perfect kmers for each read.
     * @param bestKmer length of best perfect kmer
     */
    public synchronized void addReadBestKmer(int bestKmer) {
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
    public synchronized int getNumberOfReads() {
        return nReads;
    }
    
    /**
     * Get number of reads with alignments in this read set.
     * @return number of reads
     */
    public synchronized int getNumberOfReadsWithAlignments() {
        return nReadsWithAlignments;
    }
    
    /**
     * Get number of reads without alignments in this read set.
     * @return number of reads
     */
    public synchronized int getNumberOfReadsWithoutAlignments() {
        return nReadsWithoutAlignments;
    }
    
    /**
     * Get percentage of reads with alignments
     * @return percentage of reads
     */
    public synchronized double getPercentOfReadsWithAlignments() {
        return (100.0 * (double)nReadsWithAlignments) / (double)nReads;
    }
    
    /**
     * Get percentage of reads without alignments
     * @return percentage of reads
     */
    public synchronized double getPercentOfReadsWithoutAlignments() {
        return (100.0 * (double)nReadsWithoutAlignments) / (double)nReads;
    }    
    
    /**
     * Print statistics to screen.
     */
    public synchronized void printStats() {
        System.out.println("Parse " + typeString + " alignments");
        System.out.println(typeString + " reads: " + nReads);
        System.out.println(typeString + " reads with alignments: " + nReadsWithAlignments);
        System.out.println(typeString + " reads without alignments: " + nReadsWithoutAlignments);
    }
    
    /**
     * Write a short summary file for this read set.
     * @param filename output filename
     */
    public synchronized void writeSummaryFile() {
        String filename = options.getAlignmentSummaryFilename();
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(filename, true));
            pw.println("");
            pw.printf("%s alignments", typeString);
            pw.println("");
            pw.println("");
            pw.printf("Num reads: %d", nReads);
            pw.println("");
            pw.printf("Num reads with alignments: %d", nReadsWithAlignments);
            pw.println("");
            pw.printf("Num reads without alignments: %d", nReadsWithoutAlignments);
            pw.println("");
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
    public synchronized void addDeletionError(int size, String kmer) {
        motifStats.addDeletionMotifs(kmer);
        nDeletions++;
    }
    
    /**
     * Store an insertion error.
     * @param size size of insertion
     * @param kmer kmer prior to error
     */
    public synchronized void addInsertionError(int size, String kmer) {
        motifStats.addInsertionMotifs(kmer);
        nInsertions++;
    } 
    
    /** 
     * Store a substitution error.
     * @param kmer kmer prior to error
     * @param refChar reference base
     * @param subChar substituted base
     */
    public synchronized void addSubstitutionError(String kmer, char refChar, char subChar) {
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
    public synchronized int[][] getSubstitutionErrors() {
        return substitutionErrors;
    }
    
    /**
     * Get number of substitutions.
     * @return number
     */
    public synchronized int getNumberOfSubstitutions() {
        return nSubstitutions;
    }
    
    /**
     * Write motif stats to screen.
     */
    public synchronized void outputMotifStats() {
        motifStats.outputAllMotifCounts();
    }
    
    /**
     * Get motif statistics.
     * @return MotifStatistics object
     */
    public synchronized MotifStatistics getMotifStatistics() {
        return motifStats;
    }
    
    public synchronized void writekCounts(String id, int length, int nk, int[] s, int[] kCounts) {            
        pwKmers.print(id+"\t"+Integer.toString(length));
        for (int i=0; i<nk; i++) {
            pwKmers.print("\t"+Integer.toString(kCounts[i]));
        }
        pwKmers.println("");
    }
    
    /**
     * Get options
     */
    public NanoOKOptions getOptions() {
        return options;
    }
    
    /**
     * Write substitution stats to a file
     */
    public void writeSubstitutionStats() {
        String filenamePc = options.getAnalysisDir() + File.separator + "all_" + NanoOKOptions.getTypeFromInt(type) + "_substitutions_percent.txt";
        String bases[] = {"A","C","G","T"};
        try {
            PrintWriter pwPc = new PrintWriter(new FileWriter(filenamePc)); 
            pwPc.println("\tSubA\tSubC\tSubG\tSubT");            
            for (int r=0; r<4; r++) {
                pwPc.print("Ref"+bases[r]);
                for (int s=0; s<4; s++) {
                    double pc = 0;

                    if (substitutionErrors[r][s] > 0) {
                        pc = (100.0 * (double)substitutionErrors[r][s]) / nSubstitutions;
                    }                    
                    pwPc.printf("\t%.2f", pc);
                }
                pwPc.println("");
            }
            pwPc.close();
        } catch (IOException e) {
            System.out.println("writeSubstitutionStats exception:");
            e.printStackTrace();
            System.exit(1);
        }                
    }
    
    /**
     * Write error motif stats to a file
     */
    public void writeErrorMotifStats() {
        try {
            for (int t=0; t<3; t++) {
                for (int n=3; n<=5; n++) {
                    ArrayList<Map.Entry<String, Double>> motifs = null;
                    String typeString = "";
                    String filename = "";
                    
                    if (t == 0) {
                        typeString = "insertion";
                        motifs = motifStats.getSortedInsertionMotifPercentages(n);
                    } else if (t == 1) {
                        typeString = "deletion";
                        motifs = motifStats.getSortedDeletionMotifPercentages(n);
                    } else {
                        typeString = "substitution";
                        motifs = motifStats.getSortedSubstitutionMotifPercentages(n);
                    }
                    
                    filename = options.getAnalysisDir() + File.separator + "all_" + NanoOKOptions.getTypeFromInt(type) + "_"+typeString+"_"+n+"mer_motifs.txt";
                    PrintWriter pw = new PrintWriter(new FileWriter(filename)); 
                    pw.println("Kmer\tPercentage");
                    
                    for (int i=0; i<motifs.size(); i++) {
                        pw.printf("%s\t%.4f", motifs.get(i).getKey(), motifs.get(i).getValue());
                        pw.println("");
                    }
                    pw.close();
                }
            }
        } catch (IOException e) {
            System.out.println("writeSubstitutionStats exception:");
            e.printStackTrace();
            System.exit(1);
        }   
    }
    
    public int getIgnoredDuplicates() {
        return ignoredDuplicates;
    }
}