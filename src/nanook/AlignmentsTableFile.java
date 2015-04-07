package nanook;

import java.io.*;

/**
 * Represents alignment summary file written by tool and used for graph plotting.
 * 
 * @author Richard Leggett
 */
public class AlignmentsTableFile {
    private String filename;
    private PrintWriter pw;
    private int count = 0;

    /**
     * Constructor.
     * @param f filename of output file
     */
    public AlignmentsTableFile(String f) {
        filename = f;
        
        try {
            pw = new PrintWriter(new FileWriter(filename));
            writeHeader();
        } catch (IOException e) {
            System.out.println("AlignmentsTableFile exception");
            e.printStackTrace();
        }
    }
    
    /**
     * Write header row to file.
     */
    private void writeHeader() {
        pw.print("Filename\t");
        pw.print("QueryName\t");
        pw.print("QueryStart\t");
        pw.print("QueryBasesCovered\t");
        pw.print("QueryStrand\t");
        pw.print("QueryLength\t");
        pw.print("HitName\t");
        pw.print("HitStart\t");
        pw.print("HitBasesCovered\t");
        pw.print("HitStrand\t");
        pw.print("HitLength\t");
        pw.print("AlignmentSize\t");
        pw.print("IdenticalBases\t");
        pw.print("AlignmentPercentIdentity\t");
        pw.print("QueryPercentIdentity\t");
        pw.print("LongestPerfectKmer\t");
        pw.print("MeanPerfectKmer\t");
        pw.print("PercentQueryAligned\t");
        pw.print("nk15\tnk17\tnk19\tnk21\tnk23\tnk25");
        pw.println("");
    }
    
    /**
     * Write an alignment line.
     * @param alignmentFilename filename of alignment
     * @param hitLine hit object
     * @param queryLine query object
     * @param stat AlignmentInfo statistics
     */
    public void writeAlignment(String alignmentFilename, LastAlignmentLine hitLine, LastAlignmentLine queryLine, AlignmentInfo stat) {
        String outputLine = String.format("%s\t%s\t%d\t%d\t%s\t%d\t%s\t%d\t%d\t%s\t%d\t%d\t%d\t%.2f\t%.2f\t%d\t%.2f\t%.2f\t%s",
                alignmentFilename,
                queryLine.getName(),
                queryLine.getStart(),
                queryLine.getAlnSize(),
                queryLine.getStrand(),
                queryLine.getSeqSize(),
                hitLine.getName(),
                hitLine.getStart(),
                hitLine.getAlnSize(),
                hitLine.getStrand(),
                hitLine.getSeqSize(),
                stat.getAlignmentSize(),
                stat.getIdenticalBases(),
                stat.getAlignmentId(),
                stat.getQueryId(),
                stat.getLongestPerfectKmer(),
                stat.getMeanPerfectKmer(),
                stat.getPercentQueryAligned(),
                stat.getkCounts());
        
        pw.println(outputLine);
        pw.flush();
        
        count++;
    }
    
    public void writeMergedAlignment(String alignmentFilename, AlignmentMerger merger, AlignmentInfo stat) {
        String outputLine = String.format("%s\t%s\t%d\t%d\t%s\t%d\t%s\t%d\t%d\t%s\t%d\t%d\t%d\t%.2f\t%.2f\t%d\t%.2f\t%.2f\t%s",
                alignmentFilename,
                stat.getQueryName(),
                merger.getOverallQueryStart(),
                merger.getOverallQuerySize(),
                "+",
                stat.getQuerySize(),
                stat.getHitName(),
                merger.getOverallHitStart(),
                merger.getOverallHitSize(),
                "+",
                stat.getHitSize(),
                stat.getAlignmentSize(),
                stat.getIdenticalBases(),
                stat.getAlignmentId(),
                stat.getQueryId(),
                stat.getLongestPerfectKmer(),
                stat.getMeanPerfectKmer(),
                stat.getPercentQueryAligned(),
                stat.getkCounts());
        
        pw.println(outputLine);
        pw.flush();
        
        count++;
    }    
    
    /**
     * Used when no alignment found for this query.
     * @param alignmentFilename - alignment filename
     */
    public void writeNoAlignmentMessage(String alignmentFilename) {
        pw.println(alignmentFilename+"\tNO ALIGNMENTS");
    }

    /**
     * Close file.
     */
    public void closeFile() {
        pw.flush();
        pw.close();
        //System.out.println("File closed");
    }
}
