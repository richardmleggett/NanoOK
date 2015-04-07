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
    private int nFastaFiles=0;
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
        SequenceReader sr = new SequenceReader(false);
        int nReadsInFile = sr.indexFASTAFile(filename);

        if (nReadsInFile > 1) {
            System.out.println("Warning: File "+filename+" has more than 1 read.");
        }

        for (int i=0; i<sr.getSequenceCount(); i++) {
            stats.addLength(sr.getID(i), sr.getLength(i));
        }
    }
    
    /**
     * Gather length statistics on all files in this read set.
     */
    public void processReads() {
        String dirs[] = new String[2];
        int readTypes[] = new int[2];
        int maxReads = options.getMaxReads();
        int nDirs = 0;
        
        nFastaFiles=0;

        typeString = options.getTypeFromInt(type);
                
        stats.openLengthsFile();

        if (options.isNewStyleDir()) {
            if (options.doProcessPassReads()) {
                dirs[nDirs] = options.getFastaDir() + File.separator + "pass";
                readTypes[nDirs] = NanoOKOptions.READTYPE_PASS;
                nDirs++;
            }
            
            if (options.doProcessFailReads()) {
                dirs[nDirs] = options.getFastaDir() + File.separator + "fail";
                readTypes[nDirs] = NanoOKOptions.READTYPE_FAIL;
                nDirs++;
            }
        } else {
            dirs[nDirs] = options.getFastaDir();
            readTypes[nDirs] = NanoOKOptions.READTYPE_COMBINED;
            nDirs++;
        }
                
        for (int dirIndex=0; dirIndex<nDirs; dirIndex++) {        
            String inputDir = dirs[dirIndex] + File.separator + options.getTypeFromInt(type);
            File folder = new File(inputDir);
            File[] listOfFiles = folder.listFiles();

            if (listOfFiles == null) {
                System.out.println("Directory "+inputDir+" doesn't exist");
            } else if (listOfFiles.length <= 0) {
                System.out.println("Directory "+inputDir+" empty");
            } else {
                System.out.println("\nGathering stats from "+inputDir);
            
                for (File file : listOfFiles) {
                    if (file.isFile()) {
                        if (file.getName().endsWith(".fasta")) {                            
                            readFasta(file.getPath());
                            stats.addReadFile(dirIndex, readTypes[dirIndex]);
                            nFastaFiles++;

                            if ((nFastaFiles % 100) == 0) {
                                System.out.print("\r"+nFastaFiles);
                            }


                            if ((maxReads > 0) && (nFastaFiles >= maxReads)) {
                                 break;
                            }
                        }
                    }
                }

                System.out.println("\r"+nFastaFiles);
            }
        }
        
        stats.closeLengthsFile();
             
        //System.out.println("Calculating...");
        stats.calculateStats();        
    }
    
    /**
     * Parse all alignment files for this read set.
     * Code in common with gatherLengthStats - combine?
     */
    public void processAlignments() {
        int nReads = 0;
        int nReadsWithAlignments = 0;
        int nReadsWithoutAlignments = 0;
        String dirs[] = new String[2];
        int readTypes[] = new int[2];
        int nDirs = 0;
        int maxReads = options.getMaxReads();
        String outputFilename = options.getAnalysisDir() + File.separator + "Unaligned" + File.separator + options.getTypeFromInt(type) + "_nonaligned.txt";
        AlignmentsTableFile nonAlignedSummary = new AlignmentsTableFile(outputFilename);
        
        if (options.isNewStyleDir()) {
            if (options.doProcessPassReads()) {
                dirs[nDirs] = options.getAlignerDir() + File.separator + "pass";
                readTypes[nDirs] = NanoOKOptions.READTYPE_PASS;
                nDirs++;
            }
            
            if (options.doProcessFailReads()) {
                dirs[nDirs] = options.getAlignerDir() + File.separator + "fail";
                readTypes[nDirs] = NanoOKOptions.READTYPE_FAIL;
                nDirs++;
            }
        } else {
            dirs[nDirs] = options.getAlignerDir();
            readTypes[nDirs] = NanoOKOptions.READTYPE_COMBINED;
            nDirs++;
        }
        
        for (int dirIndex=0; dirIndex<nDirs; dirIndex++) {        
            String inputDir = dirs[dirIndex] + File.separator + options.getTypeFromInt(type);
            File folder = new File(inputDir);
            File[] listOfFiles = folder.listFiles();
            
            if (listOfFiles == null) {
                System.out.println("Directory "+inputDir+" doesn't exist");
            } else if (listOfFiles.length <= 0) {
                System.out.println("Directory "+inputDir+" empty");
            } else {            
                System.out.println("Parsing from " + inputDir);            
                for (File file : listOfFiles) {
                    if (file.isFile()) {
                        if (file.getName().endsWith(options.getAlignerExtension())) {
                            String pathname = inputDir + File.separator + file.getName();
                            int nAlignments = parser.parseFile(pathname, nonAlignedSummary);

                            if (nAlignments > 0) {
                                nReadsWithAlignments++;
                                parser.sortAlignments();
                                ArrayList<Alignment> al = parser.getHighestScoringSet();
                                
                                for (int i=0; i<al.size(); i++) {
                                    Alignment a = al.get(i);
                                }
                                
                                String readReferenceName = al.get(0).getHitName();
                                ReferenceSequence readReference = references.getReferenceById(readReferenceName);
                                AlignmentMerger merger = new AlignmentMerger(readReference, al.get(0).getQuerySequenceSize(), stats, type);
                                for (int i=0; i<al.size(); i++) {
                                    Alignment a = al.get(i);
                                    merger.addAlignment(a);
                                }
                                AlignmentInfo stat = merger.endMergeAndStoreStats();
                                readReference.getStatsByType(type).getAlignmentsTableFile().writeMergedAlignment(file.getName(), merger, stat);  
                            } else {
                                nReadsWithoutAlignments++;
                            }

                            nReads++;
                            if ((nReads % 100) == 0) {
                                System.out.print("\r"+nReads+"/"+nFastaFiles);
                            }

                            if ((maxReads > 0) && (nReads >= maxReads)) {
                                break;
                            }
                        }
                    }
                }
                System.out.println("\r" + nFastaFiles + "/" + nFastaFiles + " ("+(nFastaFiles - nReads)+")");
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
