package nanook;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;

/**
 * Represent reference coverage
 * 
 * @author Richard Leggett
 */
public class SequenceCoverage implements Serializable {
    private static final long serialVersionUID = NanoOK.SERIAL_VERSION;
    private int[] coverage;
    private int numBins = 1000;
    private int genomeSize = 0;
    private int binSize = 1;
    private boolean binEarly = false;
    
    public SequenceCoverage(int s) {
        genomeSize = s;

        // Approx hundred bins for coverage
        float b = genomeSize / 100;
        
        // Make a multiple of 10, 100 or 500...
        if (genomeSize < 50000) {
            binSize = 10 * (1 + Math.round(b / 10));   
        } else if (genomeSize < 500000) {
            binSize = 100 * (1 + Math.round(b / 100));   
        } else {
            binSize = 500 * (1 + Math.round(b / 500));   
        }        

        //binSize=50;
        
        numBins = (int) Math.ceil(genomeSize / (double)binSize);
        
        
        // Bin early for large genomes
        if (genomeSize < 10000000) {
            binEarly = false;
        } else {
            binEarly = true;
        }
        
        // Force this for now
        binEarly = true;
        
        if (binEarly) {
            coverage = new int[numBins];
        } else {
            coverage = new int[genomeSize];
        }
    }
    
    /**
     * Increment coverage between two points.
     * @param start start position
     * @param size size
     */
    public synchronized void addCoverage(int start, int size) {
        for (int i=start; i<(start+size); i++) {
            if (binEarly) {
                coverage[i/binSize]++;
            } else {
                coverage[i]++;
            }
        }
    }
    
    /**
     * Write coverage file for later graph plotting.
     * @param filename output filename
     * @param binSize bin size
     */
    private synchronized void binAndWriteCoverageData(String filename, int pbinSize) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(filename));            
            for (int i=0; i<(genomeSize-pbinSize); i+=pbinSize) {
                int count = 0;
                for (int j=0; j<pbinSize; j++) {
                    count += coverage[i+j];
                }
                pw.printf("%d\t%.2f", i, ((double)count / (double)pbinSize));
                pw.println("");
            }            
            pw.close();
        } catch (IOException e) {
            System.out.println("writeCoverageData exception:");
            e.printStackTrace();
            System.exit(1);
        }
    }
        
    /**
     * Write coverage file for later graph plotting.
     * @param filename output filename
     * @param binSize bin size
     */
    private synchronized void writeBinnedCoverageData(String filename) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(filename));            
            for (int i=0; i<numBins-1; i++) {
                double c = (double)coverage[i] / (double)binSize;
                if (i == (numBins - 1)) {
                    c = (double)coverage[i] / (double)(genomeSize - (i*binSize));
                }
                pw.printf("%d\t%.2f", i*binSize, c);
                pw.println("");
            }            
            pw.close();
        } catch (IOException e) {
            System.out.println("writeCoverageData exception:");
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Write coverage file for later graph plotting.
     * @param filename output filename
     * @param binSize bin size
     */
    public synchronized void writeCoverageData(String filename, int binSize) {
        if (binEarly) {
            writeBinnedCoverageData(filename);
        } else {
            binAndWriteCoverageData(filename, binSize);
        }
    }
    
}
