package nanook;

import java.io.BufferedReader;
import java.io.*;
import java.util.*;

/**
 * Represents a read set (Template reads, Complement reads, or 2D reads).
 * 
 * @author leggettr
 */
public class ReadSet {
    private NanoOKOptions options;
    private AlignmentFileParser parser;
    private ReadSetStats stats;
    private References references;
    private int type;
    private String typeString;
   
    /**
     * Constructor
     * @param t type (defined in NanoOKOprions)
     * @param o NanoOKOptions object
     * @param r the References
     * @param p an alignment parser object
     * @param s set of stats to associate with this read set
     */
    public ReadSet(int t, NanoOKOptions o, References r, AlignmentFileParser p, ReadSetStats s) {
        options = o;
        parser = p;
        references = r;
        type = t;
        stats = s;
    }
        
    /**
     * Parse a FASTA file, noting length of reads etc.
     * @param filename filename of FASTA file
     */
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
    
    /**
     * Gather length statistics on all files in this read set.
     */
    public void gatherLengthStats() {
        String dirs[] = new String[2];
        int readTypes[] = new int[2];
        int nDirs = 0;

        typeString = options.getTypeFromInt(type);
        
        System.out.println("Gathering stats on "+typeString+" reads");
        
        stats.openLengthsFile();

        if (options.isNewStyleDir()) {
            if (options.processPassReads()) {
                dirs[nDirs] = options.getFastaDir() + options.getSeparator() + "pass";
                readTypes[nDirs] = NanoOKOptions.READTYPE_PASS;
                nDirs++;
            }
            
            if (options.processFailReads()) {
                dirs[nDirs] = options.getFastaDir() + options.getSeparator() + "fail";
                readTypes[nDirs] = NanoOKOptions.READTYPE_FAIL;
                nDirs++;
            }
        } else {
            dirs[nDirs] = options.getFastaDir();
            readTypes[nDirs] = NanoOKOptions.READTYPE_COMBINED;
            nDirs++;
        }
                
        for (int dirIndex=0; dirIndex<nDirs; dirIndex++) {        
            String inputDir = dirs[dirIndex] + options.getSeparator() + options.getTypeFromInt(type);
            File folder = new File(inputDir);
            File[] listOfFiles = folder.listFiles();

            for (File file : listOfFiles) {
                if (file.isFile()) {
                    if (file.getName().endsWith(".fasta")) {
                        readFasta(file.getPath());
                        stats.addReadFile(dirIndex, readTypes[dirIndex]);
                    }
                }
            }
        }
        
        stats.closeLengthsFile();
             
        stats.calculateStats();        
    }
    
    /**
     * Parse all alignment files for this read set.
     * Code in common with gatherLengthStats - combine?
     */
    public void parseAlignmentFiles() {
        int nReads = 0;
        int nReadsWithAlignments = 0;
        int nReadsWithoutAlignments = 0;
        String dirs[] = new String[2];
        int readTypes[] = new int[2];
        int nDirs = 0;
        String outputFilename = options.getAnalysisDir() + options.getSeparator() + options.getTypeFromInt(type) + "_nonaligned.txt";
        AlignmentsTableFile nonAlignedSummary = new AlignmentsTableFile(outputFilename);
        
        if (options.isNewStyleDir()) {
            if (options.processPassReads()) {
                dirs[nDirs] = options.getLastDir() + options.getSeparator() + "pass";
                readTypes[nDirs] = NanoOKOptions.READTYPE_PASS;
                nDirs++;
            }
            
            if (options.processFailReads()) {
                dirs[nDirs] = options.getLastDir() + options.getSeparator() + "fail";
                readTypes[nDirs] = NanoOKOptions.READTYPE_FAIL;
                nDirs++;
            }
        } else {
            dirs[nDirs] = options.getLastDir();
            readTypes[nDirs] = NanoOKOptions.READTYPE_COMBINED;
            nDirs++;
        }
        
        for (int dirIndex=0; dirIndex<nDirs; dirIndex++) {        
            String inputDir = dirs[dirIndex] + options.getSeparator() + options.getTypeFromInt(type);

            System.out.println("Parsing " + options.getTypeFromInt(type));            

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
        }

        nonAlignedSummary.closeFile();
        stats.writeSummaryFile(options.getAlignmentSummaryFilename());
    }
    
    /**
     * Get type of this read set.
     * @return a String (e.g. "Template")
     */
    public String getTypeString() {
        return typeString;
    }
    
    /**
     * Get stats object.
     * @return a ReadSetStats object
     */
    public ReadSetStats getStats() {
        return stats;
    }
}
