package nanotools;

import java.io.BufferedReader;
import java.io.*;
import java.util.*;

public class ReadSet {
    private NanotoolsOptions options;
    private PrintWriter pw;
    private int[] lengths = new int[NanotoolsOptions.MAX_READ_LENGTH];
    private int nReads = 0;
    private int nReadFiles = 0;
    private String type;
    private int longest = 0;
    private int shortest = NanotoolsOptions.MAX_READ_LENGTH;
    private int basesSum = 0;
    private double meanLength = 0;
    private int n50 = 0;
    private int n50Count = 0;
    private int n90 = 0;
    private int n90Count = 0;
   
    public ReadSet(NanotoolsOptions o) {
        options = o;
    }
    
    private void openLengthsFile() {
        String filename = options.getAnalysisDir() + options.getSeparator() + "all_" + type + "_lengths.txt";
        try {
            pw = new PrintWriter(new FileWriter(filename)); 
        } catch (IOException e) {
            System.out.println("openLengthsFile exception:");
            e.printStackTrace();
            System.exit(1);
        }        
    }
    
    private void closeLengthsFile() {
        pw.close();
    }
    
    private void addLength(String id, int l) {
        lengths[l]++;
        
        pw.println(id + "\t" + l);
        
        if (l > longest) {
            longest = l;
        }
        
        if (l < shortest) {
            shortest = l;
        }
        
        basesSum += l;
        nReads++;
    }
    
    private void readFasta(String filename) {
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            String id = null;
            int contigLength = 0;
            int readsInThisFile = 0;
                        
            do {
                line = br.readLine();

                if ((line == null) || (line.startsWith(">"))) {                    
                    if (id != null) {
                        addLength(id, contigLength);
                        readsInThisFile++;
                        
                        if (readsInThisFile > 1) {
                            System.out.println("Warning: File "+filename+" has more than 1 read.");
                        }
                    }
                    
                    if (line != null) {
                        String[] parts = line.substring(1).split("(\\s+)");
                        id = parts[0];
                    }                   
                    
                    contigLength = 0;
                } else if (line != null) {
                    contigLength += line.length();
                }                
            } while (line != null);

            br.close();
        } catch (Exception e) {
            System.out.println("readFasta Exception:");
            e.printStackTrace();
            System.exit(1);
        }

    }
    
    private void calculateN() {
        int total = 0;
        int c = 0;
                
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

    public void gatherLengthStats(int nType) {
        String inputDir = options.getBaseDirectory() + options.getSeparator() + options.getSample() + options.getSeparator() + "fasta" + options.getSeparator() + options.getTypeFromInt(nType);
        File folder = new File(inputDir);
        File[] listOfFiles = folder.listFiles();

        type = options.getTypeFromInt(nType);
        
        System.out.println("Gathering stats on "+type+" reads");
        
        openLengthsFile();
        
        for (File file : listOfFiles) {
            if (file.isFile()) {
                if (file.getName().endsWith(".fasta")) {
                    readFasta(file.getPath());
                    nReadFiles++;
                }
            }
        }
        
        closeLengthsFile();
             
        meanLength = (double)basesSum / (double)nReads;        
        calculateN();        
    }
        
    public String getType() {
        return type;
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
    
    public int getNumReadFiles() {
        return nReadFiles;
    }
    
    public int getTotalBases() {
        return basesSum;
    }
}
