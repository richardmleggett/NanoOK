package nanook;

import java.io.*;

/**
 * Represents a summary file containing basic information on read lengths, N50 etc. for the three different read types.
 * 
 * @author Richard Leggett
 */
public class ReadLengthsSummaryFile {
    private PrintWriter pw;
    private String filename;

    /**
     * Constructor.
     * @param f filename of output file
     */
    public ReadLengthsSummaryFile(String f) {
        filename = f;
    }
    
    /**
     * Open output file.
     * @param sample sample name
     */
    public void open(String sample) {
        try {
            pw = new PrintWriter(new FileWriter(filename)); 
            pw.println("Nanotools report - "+sample+"\n");
            pw.println("Length summary");
            pw.println("");
            pw.printf("%-10s %-8s %-10s %-10s %-8s %-8s %-8s %-8s %-8s %-8s\n", "Type", "NumReads", "TotalBases", "Mean", "Long", "Short", "N50", "N50Count", "N90", "N90Count"); 
        } catch (IOException e) {
            System.out.println("ReadLengthsSummaryFile exception:");
            e.printStackTrace();
            System.exit(1);
        }        
    }
    
    /**
     * Output read stats for a particular type (Template, Complement, 2D).
     * @param r ReadSetStats object for the type
     */
    public void addReadSetStats(ReadSetStats r) {
        pw.printf("%-10s %-8d %-10d %-10.2f %-8d %-8d %-8d %-8d %-8d %-8d\n", r.getTypeString(), r.getNumReads(), r.getTotalBases(), r.getMeanLength(), r.getLongest(), r.getShortest(), r.getN50(), r.getN50Count(), r.getN90(), r.getN90Count());
    }
    
    /**
     * Close output file.
     */
    public void close() {
        pw.close();
    }
}
