package nanotools;

import java.io.*;

public class ReadLengthsSummaryFile {
    private PrintWriter pw;
    private String filename;

    public ReadLengthsSummaryFile(String f) {
        filename = f;
    }
    
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
    
    public void addReadSet(ReadSet r) {
        pw.printf("%-10s %-8d %-10d %-10.2f %-8d %-8d %-8d %-8d %-8d %-8d\n", r.getType(), r.getNumReads(), r.getTotalBases(), r.getMeanLength(), r.getLongest(), r.getShortest(), r.getN50(), r.getN50Count(), r.getN90(), r.getN90Count());
    }
    
    public void close() {
        pw.close();
    }
}
