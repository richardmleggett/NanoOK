package nanotools;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class OverallAlignmentStats {
    private String type = "";
    private int nReads = 0;
    private int nReadsWithAlignments = 0;
    private int nReadsWithoutAlignments = 0;
    private int[] readBestPerfectKmer = new int[NanotoolsOptions.MAX_KMER];
    private int[] readCumulativeBestPerfectKmer = new int[NanotoolsOptions.MAX_KMER];
    
    public OverallAlignmentStats() {
    }

    public void clearStats(String s) {
        type = s;
        nReads = 0;
        nReadsWithAlignments = 0;
        nReadsWithoutAlignments = 0;
        for (int i=0; i<NanotoolsOptions.MAX_KMER; i++) {
            readBestPerfectKmer[i] = 0;
            readCumulativeBestPerfectKmer[i] = 0;
        }
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
        if (bestKmer >= NanotoolsOptions.MAX_KMER) {
            System.out.println("Error: the unlikely event of a best kmer size of "+bestKmer+" has happened!");
            System.exit(1);
        }
        
        readBestPerfectKmer[bestKmer]++;
        
        for (int i=0; i<bestKmer; i++) {
            readCumulativeBestPerfectKmer[i]++;
        }
    }
    
    public void printStats() {
        System.out.println("Parse " + type + " alignments");
        System.out.println(type + " reads: " + nReads);
        System.out.println(type + " reads with alignments: " + nReadsWithAlignments);
        System.out.println(type + " reads without alignments: " + nReadsWithoutAlignments);
    }
    
    public void writeSummaryFile(String filename) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(filename, true));
            pw.println("");
            pw.printf("%s alignments\n\n", type);
            pw.printf("Num reads: %d\n", nReads);
            pw.printf("Num reads with alignments: %d\n", nReadsWithAlignments);
            pw.printf("Num reads without alignments: %d\n", nReadsWithoutAlignments);
            pw.close();
        } catch (IOException e) {
            System.out.println("writeSummaryFile exception:");
            System.out.println(e);
        }
    }
}
