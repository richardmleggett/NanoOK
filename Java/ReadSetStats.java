package nanotools;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Hashtable;

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
        
    public ReadSetStats(NanoOKOptions o, int t) {
        options=o;
        typeString = NanoOKOptions.getTypeFromInt(t);
        for (int i=0; i<NanoOKOptions.MAX_KMER; i++) {
            readBestPerfectKmer[i] = 0;
            readCumulativeBestPerfectKmer[i] = 0;
        }
    }

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
    
    public void closeLengthsFile() {
        pw.close();
    }
    
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
        
   public void addReadFile() {
       nReadFiles++;
   }
   
    public String getTypeString() {
        return typeString;
    }
    
    public double getMeanLength() {
        return meanLength;
    }
    
    public int getLongest() {
        return longest;
    }
    
    public int getShortest() {
        return shortest;
    }
    
    public int getN50() {
        return n50;
    }
    
    public int getN50Count() {
        return n50Count;
    }
    
    public int getN90() {
        return n90;
    }
    
    public int getN90Count() {
        return n90Count;
    }
    
    public int getNumReads() {
        return nReads;
    }
        
    public int getTotalBases() {
        return basesSum;
    }    
    
    public int getNumReadFiles() {
        return nReadFiles;
    }    
    
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
        
    public void addReadWithAlignment() {
        nReads++;
        nReadsWithAlignments++;
    }

    public void addReadWithoutAlignment() {
        nReads++;
        nReadsWithoutAlignments++;
    }
        
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
        
    public int getNumberOfReads() {
        return nReads;
    }
    
    public int getNumberOfReadsWithAlignments() {
        return nReadsWithAlignments;
    }
    
    public int getNumberOfReadsWithoutAlignments() {
        return nReadsWithoutAlignments;
    }
    
    public double getPercentOfReadsWithAlignments() {
        return (100.0 * (double)nReadsWithAlignments) / (double)nReads;
    }
    
    public double getPercentOfReadsWithoutAlignments() {
        return (100.0 * (double)nReadsWithoutAlignments) / (double)nReads;
    }    
    
    public void printStats() {
        System.out.println("Parse " + typeString + " alignments");
        System.out.println(typeString + " reads: " + nReads);
        System.out.println(typeString + " reads with alignments: " + nReadsWithAlignments);
        System.out.println(typeString + " reads without alignments: " + nReadsWithoutAlignments);
    }
    
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
    
    public void addDeletionError(int size, String kmer) {
        motifStats.addDeletionMotifs(kmer);
        nDeletions++;
    }
    
    public void addInsertionError(int size, String kmer) {
        motifStats.addInsertionMotifs(kmer);
        nInsertions++;
    }
    
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
    
    public int[][] getSubstitutionErrors() {
        return substitutionErrors;
    }
    
    public int getNumberOfSubstitutions() {
        return nSubstitutions;
    }
    
    public void outputMotifStats() {
        motifStats.outputAllMotifCounts();
    }
    
    public MotifStatistics getMotifStatistics() {
        return motifStats;
    }
}
