package nanotools;

import java.io.BufferedReader;
import java.io.*;
import java.util.*;

public class ReadSet {
    private NanoOKOptions options;
    private AlignmentFileParser parser;
    private ReadSetStats stats;
    private References references;
    private int type;
    private String typeString;
   
    public ReadSet(int t, NanoOKOptions o, References r, AlignmentFileParser p, ReadSetStats s) {
        options = o;
        parser = p;
        references = r;
        type = t;
        stats = s;
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
                        stats.addLength(id, contigLength);
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
    public void gatherLengthStats() {
        String inputDir = options.getBaseDirectory() + options.getSeparator() + options.getSample() + options.getSeparator() + "fasta" + options.getSeparator() + options.getTypeFromInt(type);
        File folder = new File(inputDir);
        File[] listOfFiles = folder.listFiles();

        typeString = options.getTypeFromInt(type);
        
        System.out.println("Gathering stats on "+typeString+" reads");
        
        stats.openLengthsFile();
        
        for (File file : listOfFiles) {
            if (file.isFile()) {
                if (file.getName().endsWith(".fasta")) {
                    readFasta(file.getPath());
                    stats.addReadFile();
                }
            }
        }
        
        stats.closeLengthsFile();
             
        stats.calculateStats();        
    }
    
    public void parseFiles() {
        int nReads = 0;
        int nReadsWithAlignments = 0;
        int nReadsWithoutAlignments = 0;
        
        String inputDir = options.getBaseDirectory() + options.getSeparator() + options.getSample() + options.getSeparator() + "last" + options.getSeparator() + options.getTypeFromInt(type);
        String outputFilename = options.getBaseDirectory() + options.getSeparator() + options.getSample() + options.getSeparator() + "analysis" + options.getSeparator() + options.getTypeFromInt(type) + "_nonaligned.txt";
        AlignmentsTableFile nonAlignedSummary = new AlignmentsTableFile(outputFilename);

        System.out.println("\nParsing " + options.getTypeFromInt(type));            

        File folder = new File(inputDir);
        File[] listOfFiles = folder.listFiles();

        for (File file : listOfFiles) {
            if (file.isFile()) {
                if (file.getName().endsWith(".maf")) {
                    String pathname = inputDir + options.getSeparator() + file.getName();
                    int nAlignments = parser.parseFile(pathname, nonAlignedSummary);

                    if (nAlignments > 0) {
                        nReadsWithAlignments++;
                    } else {
                        nReadsWithoutAlignments++;
                    }

                    nReads++;
                }
            }
        }

        nonAlignedSummary.closeFile();
        stats.writeSummaryFile(options.getAlignmentSummaryFilename());
    }
    
    public String getTypeString() {
        return typeString;
    }
    
    public ReadSetStats getStats() {
        return stats;
    }
}
