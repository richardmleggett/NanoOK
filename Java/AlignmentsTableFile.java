package nanotools;

import java.io.*;

public class AlignmentsTableFile {
    private String filename;
    private PrintWriter pw;

    public AlignmentsTableFile(String f) {
        filename = f;
        
        try {
            pw = new PrintWriter(new FileWriter(filename));
            writeHeader();
        } catch (IOException e) {
            System.out.println(e);
        }
    }
    
    private void writeHeader() {
        pw.print("Filename\t");
        pw.print("Query Name\t");
        pw.print("Query Start\t");
        pw.print("Query Bases Covered\t");
        pw.print("Query Strand\t");
        pw.print("Query Length\t");
        pw.print("Hit Name\t");
        pw.print("Hit Start\t");
        pw.print("Hit Bases Covered\t");
        pw.print("Hit Strand\t");
        pw.print("Hit Length\t");
        pw.print("Alignment Size\t");
        pw.print("Identical Bases\t");
        pw.print("Alignment Percent Identity\t");
        pw.print("Query Percent Identity\t");
        pw.print("Longest Perfect Kmer\t");
        pw.println("Mean Perfect Kmer");
    }
    
    public void writeAlignment(String filename, AlignmentLine hitLine, AlignmentLine queryLine, AlignmentEntry stat) {
        pw.print(filename+"\t");
        pw.print(queryLine.getName()+"\t");
        pw.print(queryLine.getStart()+"\t");
        pw.print(queryLine.getAlnSize()+"\t");
        pw.print(queryLine.getStrand()+"\t");
        pw.print(queryLine.getSeqSize()+"\t");
        pw.print(hitLine.getName()+"\t");
        pw.print(hitLine.getStart()+"\t");
        pw.print(hitLine.getAlnSize()+"\t");
        pw.print(hitLine.getStrand()+"\t");
        pw.print(hitLine.getSeqSize()+"\t");
        pw.print(stat.getAlignmentSize() + "\t");
        pw.print(stat.getIdenticalBases() + "\t");
        pw.printf("%.2f\t", stat.getAlignmentId());
        pw.printf("%.2f\t", stat.getQueryId());
        pw.print(stat.getLongest() + "\t");
        pw.printf("%.2f\n", stat.getMeanPerfectKmer());            
    }
    
    public void writeNoAlignmentMessage(String filename) {
        pw.println(filename+"\tNO ALIGNMENTS");
    }

    public void closeFile() {
        pw.flush();
        pw.close();
    }
}
