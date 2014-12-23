package nanook;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Class to parse GC content of a FASTA file for creating a GC graph.
 * 
 * @author Richard Leggett
 */
public class GCParser {
    /**
     * Constructor
     */
    public GCParser() {    
    }
    
    /**
     * Parse a file looking for a particular ID and write GC content histogram data.
     * @param fastaFilename name of FASTA file
     * @param sequenceId ID of sequence in file
     * @param outputFilename output tab separated file containing GC content per position
     * @param binSize bin size to use
     */
    public void parseSequence(String fastaFilename, String sequenceId, String outputFilename, int binSize) {
        int currentGCPosition = binSize / 2;
        int currentGCCounter = 0;
        int currentGC = 0;
        int counts[] = new int[binSize];
        boolean foundID = false;
        boolean continueReading = true;

        //System.out.println("Writing to "+outputFilename);
        
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(fastaFilename));
            PrintWriter pw = new PrintWriter(new FileWriter(outputFilename));

            String line = br.readLine();
            while ((line != null) && (continueReading)) {
                if (line.startsWith(">")) {
                    String[] s = line.substring(1).split(" ");
                    String id = s[0];
                    if (foundID) {
                        continueReading = false;
                    } else if (id.equals(sequenceId)) {
                        foundID = true;
                    } //else {                  
                        //System.out.println("["+id+"] not equal to ["+sequenceId+"]");
                    //}
                } else if (foundID) {
                    for (int i=0; i<line.length(); i++) {
                        if ((line.charAt(i) == 'G') || (line.charAt(i) == 'C') || (line.charAt(i) == 'g') || (line.charAt(i) == 'c')) {
                            counts[currentGCCounter] = 1;
                            currentGC++;
                        } else {
                            counts[currentGCCounter] = 0;
                        }
                        currentGCCounter++;
                        
                        if (currentGCCounter == binSize) {
                            //pw.println(currentGCPosition + "\t" + (double)((double)currentGC / (double)currentGCLength));
                            pw.println(currentGCPosition + "\t" + getGC(counts, binSize));
                            currentGCCounter = 0;
                            for (int j=(binSize / 2); j<binSize; j++) {
                                counts[currentGCCounter++] = counts[j];
                            }
                            currentGCPosition += (binSize / 2);
                            //currentGCPosition += binSize;
                            //currentGCLength = 0;
                            currentGC = 0;
                        }
                    }
                }
                line = br.readLine();
            }
            br.close();
            pw.close();
        } catch (Exception e) {
            System.out.println("NanotoolsReferences Exception:");
            e.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Get overall GC content of file.
     * @param counts - GC counts array
     * @param binSize - bin size
     * @return 
     */
    private double getGC(int[] counts, int binSize) {
        int gc = 0;
                
        for (int i=0; i<binSize; i++) {
            gc += counts[i];
        }
        
        return (100.0 * (double)gc) / (double)binSize;
    }
}
