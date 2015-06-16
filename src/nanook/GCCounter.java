package nanook;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Count GC content in references
 * 
 * @author Richard Leggett
 */
public class GCCounter {
    PrintWriter pw = null;
    int binSize = 0;
    int currentGCPosition = 0;
    int currentGCCounter = 0;
    int currentGC = 0;
    int counts[];
    
    public GCCounter(int bs, String outputFilename) {
        binSize = bs;
        counts = new int[binSize*2];
        currentGCPosition = binSize;

        try {
            pw = new PrintWriter(new FileWriter(outputFilename));
        } catch (IOException e) {
            System.out.println("GCCounter exception:");
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Store GC
     */
    private void storeGC() {
        int gc = 0;
        double pc;
                
        for (int i=0; i<binSize; i++) {
            gc += counts[i];
        }
        
        pc = (100.0 * (double)gc) / (double)binSize;        
        if (pw != null) {
            pw.println(currentGCPosition + "\t" + pc);
        }
    }
    
    /**
     * Close file
     */
    public void closeFile() {
        pw.close();
    }
    
    /**
     * Process sequence string
     * @param line 
     */
    public void addString(String line) {
        for (int i=0; i<line.length(); i++) {
            if ((line.charAt(i) == 'G') || (line.charAt(i) == 'C') || (line.charAt(i) == 'g') || (line.charAt(i) == 'c')) {
                counts[currentGCCounter] = 1;
                currentGC++;
            } else {
                counts[currentGCCounter] = 0;
            }
            currentGCCounter++;

            if (currentGCCounter == (binSize*2)) {
                storeGC();
                
                currentGCCounter = 0;
                for (int j=binSize; j<(binSize*2); j++) {
                    counts[currentGCCounter++] = counts[j];
                }
                currentGCPosition += binSize;
                currentGC = 0;
            }
        }                    
    }        
}
