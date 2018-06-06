/*
 * Program: NanoOK
 * Author:  Richard M. Leggett
 * 
 * Copyright 2015 The Genome Analysis Centre (TGAC)
 */

package nanook;

import java.io.*;
import java.util.*;

/**
 * Represents the set of references (sequences) used for the analysis.
 * 
 * @author Richard Leggett
 */
public class References implements Serializable {
    private static final long serialVersionUID = NanoOK.SERIAL_VERSION;
    private NanoOKOptions options;
    private File sizesFile;
    private Hashtable<String,ReferenceSequence> referenceSeqIds = new Hashtable();
    private Hashtable<String,ReferenceSequence> referenceSeqNames = new Hashtable();
    private int longestId = 0;
    private OverallStats overallStats = null;
        
    /**
     * Constructor
     * @param o a NanoOKOptions object
     */
    public References(NanoOKOptions o)
    {
        options = o;
    }
    
    public void setOverallStats(OverallStats s) {
        overallStats = s;
    }
    
    public void readSizesFile() {
        sizesFile = new File(options.getReferenceFile()+".sizes");
        
        if (sizesFile.exists()) {
            System.out.println("Using .sizes file "+sizesFile.getName());
            System.out.println("Note: if you have changed the reference file, you need to delete the .sizes file and re-run.\n");
        } else {
            int extensionIndex = options.getReferenceFile().lastIndexOf('.');
            if (extensionIndex > 0) {
                String minusExtension = options.getReferenceFile().substring(0, extensionIndex);
                sizesFile = new File(minusExtension + ".sizes");
            }
        }
        
        if (!sizesFile.exists()) {
            System.out.println("Error: can't read sizes file.");
            System.out.println("Generating .sizes file for reference. You may want to edit the display names.");
            SequenceReader sr = new SequenceReader(false);
            sr.indexFASTAFile(options.getReferenceFile(), options.getReferenceFile()+".sizes" , false);
            sizesFile = new File(options.getReferenceFile()+".sizes");
        }        
        
        System.out.println("Reading reference sizes and making directories");
        
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(sizesFile));
            String line = br.readLine();
            while (line != null) {
                if (!line.startsWith("#") && (!line.startsWith("SequenceID"))) {
                    String[] values = line.split("\\t");
                    int size = Integer.parseInt(values[1]);

                    ReferenceSequence refSeqById = referenceSeqIds.get(values[0]);
                    if (refSeqById != null) {
                        System.out.println("Error: reference contig ID "+values[0]+" occurs more than once.");
                        System.exit(1);
                    }

                    ReferenceSequence refSeqByName = referenceSeqNames.get(values[2]);
                    if (refSeqByName != null) {
                        System.out.println("Error: reference contig name "+values[2]+" occurs more than once.");
                        System.exit(1);
                    }

                    System.out.println("\t" + values[2] + "\t" + size);

                    refSeqById = new ReferenceSequence(values[0], size, values[2]);
                    options.checkAndMakeReferenceAnalysisDir(refSeqById.getName());
                    referenceSeqIds.put(values[0], refSeqById);
                    referenceSeqNames.put(values[2], refSeqById);
                    refSeqById.openAlignmentSummaryFiles(options);

                    if (values[0].length() > longestId) {
                        longestId = values[0].length();
                    }
                }

                line = br.readLine();
            }
            br.close();
        } catch (Exception e) {
            System.out.println("NanotoolsReferences Exception:");
            e.printStackTrace();
            System.exit(1);
        }
    }
       
    /**
     * Read reference FASTA file
     */
    private void readReferenceFile() {
        ReferenceSequence currentRef = null;
        KmerTable refKmerTable = null;
        GCCounter gcc = null;

        try
        {
            BufferedReader br = new BufferedReader(new FileReader(options.getReferenceFile()));             
            String line;
            String id = null;
            String name = null;
            String seq = "";
            String previousKmerString = "";
 
            System.out.println("");
            System.out.println("Calculating reference GC");
            
            do {
                line = br.readLine();
                if (line != null) {
                    line = line.trim();
                }

                // New ID
                if ((line == null) || (line.startsWith(">"))) {                    
                    if (id != null) {
                        if (gcc != null) {
                            gcc.closeFile();
                        }                        
                    }
                    
                    if (line != null) {
                        String[] parts = line.substring(1).split("(\\s+)");
                        id = parts[0];
                        currentRef = getReferenceById(id);
                        System.out.println("\t" + currentRef.getName());
                        refKmerTable = currentRef.getKmerTable();
                        gcc = new GCCounter(currentRef.getBinSize(), options.getAnalysisDir() + File.separator + currentRef.getName() + File.separator + currentRef.getName() + "_gc.txt");
                    }                                        
                }
                // Continuing sequence read 
                else if ((line != null) && (currentRef != null)) {
                    if (!line.equals("")) {
                        String kmerSeq = previousKmerString + line;                    
                        int k = refKmerTable.getKmerSize();

                        // Store kmers
                        for (int o=0; o<kmerSeq.length() - k; o++) {
                            refKmerTable.countKmer(kmerSeq.substring(o, o+5));
                        }

                        // Store end k-1 bases for start of next kmer
                        if (line.length() > k) {
                            previousKmerString = line.substring(line.length() - k + 1);
                        } else {
                            previousKmerString = "";
                        }

                        // Now for GC graph
                        gcc.addString(line);
                    }           
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
     * Load references
     */
    public void loadReferences() {
        readSizesFile(); 
        readReferenceFile();
    }    
    
    /**
     * Get a ReferenceSequence object from sequence ID.
     */
    public ReferenceSequence getReferenceById(String id) {
        ReferenceSequence r = referenceSeqIds.get(id);
        
        if (r == null) {
            System.out.println("");
            System.out.println("Error: Couldn't find reference for "+id + ". This can occur if you have changed the refernce file, but not deleted the .sizes file associated with it. Try deleting reference.fasta.sizes and re-running.");
            System.exit(1);
        }
        
        return r;
    }
            
    /**
     * Return set of all reference sequence IDs.
     * @return a String set
     */
    public Set<String> getAllIds() {
        return referenceSeqIds.keySet();
    }
    
    /**
     * Return sorted set of all reference sequence IDs.
     * @return a String set
     */
    public ArrayList getSortedReferences() {
        ArrayList sortedReferences = new ArrayList();
        Set<String> keys = referenceSeqIds.keySet();
        
        for(String id : keys) {
            sortedReferences.add(referenceSeqIds.get(id));
        }    
        Collections.sort(sortedReferences);
        
        return sortedReferences;
    }
    
    
    /**
     * Initiate writing of all statistics data files used to generate graphs.
     * @param type a type, as defined in NanoOKOptions (for example TYPE_TEMPLATE)
     */
    public void writeReferenceStatFiles(int type) {
        Set<String> keys = referenceSeqIds.keySet();
        
        for(String id : keys) {
            ReferenceSequence ref = referenceSeqIds.get(id);
            ref.getStatsByType(type).writeCoverageData(options.getAnalysisDir() + File.separator + ref.getName() + File.separator + ref.getName() + "_" + options.getTypeFromInt(type) + "_coverage.txt", ref.getBinSize());
            ref.getStatsByType(type).writePerfectKmerHist(options.getAnalysisDir() + File.separator + ref.getName() + File.separator + ref.getName() + "_" + options.getTypeFromInt(type) + "_all_perfect_kmers.txt");
            ref.getStatsByType(type).writeBestPerfectKmerHist(options.getAnalysisDir() + File.separator + ref.getName() + File.separator + ref.getName() + "_" + options.getTypeFromInt(type) + "_best_perfect_kmers.txt");
            ref.getStatsByType(type).writeBestPerfectKmerHistCumulative(options.getAnalysisDir() + File.separator + ref.getName() + File.separator + ref.getName() + "_" + options.getTypeFromInt(type) + "_cumulative_perfect_kmers.txt");
            ref.getStatsByType(type).writeInsertionStats(options.getAnalysisDir() + File.separator + ref.getName() + File.separator + ref.getName() + "_" + options.getTypeFromInt(type) + "_insertions.txt");
            ref.getStatsByType(type).writeDeletionStats(options.getAnalysisDir() + File.separator + ref.getName() + File.separator + ref.getName() + "_" + options.getTypeFromInt(type) + "_deletions.txt");
            ref.writeKmerFile(type, options.getAnalysisDir() + File.separator + ref.getName() + File.separator + ref.getName() + "_" + options.getTypeFromInt(type) + "_kmers.txt");
        }        
    }
    
    /**
     * Get the length of the longest ID - used for formatting output.
     * @return length of longest sequence ID
     */
    public int getLongestIdLength() {
        return longestId;
    }
    
    /**
     * Get number of references.
     * @return number of references
     */
    public int getNumberOfReferences() {
        return referenceSeqIds.size();
    }
    
    /**
     * Write reference summary text file.
     * @param type type from NanoOKOptions
     */
    public void writeReferenceSummary(int type) {
       try {
            String filename = options.getAnalysisDir() + File.separator + "all_" + NanoOKOptions.getTypeFromInt(type) + "_alignment_summary.txt";
            PrintWriter pw = new PrintWriter(new FileWriter(filename));
            String formatString = "%-"+longestId+"s %-12s %-10s %-10s %-10s %-12s %-10s %-10s";
            //pw.printf(formatString, "ID", "Size", "ReadsAlign", "PcReads", "MeanLen", "TotalBases", "MeanCov", "LongPerfKm");    
            pw.print("ID\tSize\tReadsAlign\tPcReads\tMeanLen\tTotalBases\tMeanCov\tLongPerfKm\tLongestAlignment");
            pw.println("");
            
            //List<String> keys = new ArrayList<String>(referenceSeqIds.keySet());
            //Collections.sort(keys);
            //for(String id : keys) {
            //    referenceSeqIds.get(id).getStatsByType(type).writeSummary(pw, "%-"+longestId+"s %-12d %-10d %-10.2f %-10d");
            //}

            formatString = "%s\t%d\t%d\t%.2f\t%.2f\t%d\t%.2f\t%d\t%d";
            ArrayList<ReferenceSequence> sortedRefs = getSortedReferences();
            for (int i=0; i<sortedRefs.size(); i++) {
                ReferenceSequence r = sortedRefs.get(i);
                ReferenceSequenceStats refStats = r.getStatsByType(type);
                pw.printf(formatString,
                           r.getName(),
                           r.getSize(),
                           refStats.getNumberOfReadsWithAlignments(),
                           100.0 * (double)refStats.getNumberOfReadsWithAlignments() / (double)overallStats.getStatsByType(type).getNumberOfReads(),
                           refStats.getMeanReadLength(),
                           refStats.getTotalAlignedBases(),
                           (double)refStats.getTotalAlignedBases() / r.getSize(),
                           refStats.getLongestPerfectKmer(),
                           refStats.getLongestAlignmentSize());
                
                pw.println("");
            }
            
            
            pw.close();
        } catch (IOException e) {
            System.out.println("writeReferenceSummary exception:");
            e.printStackTrace();
            System.exit(1);
        }
    }
    
//    /**
//     * Write reference summary to LaTeX report.
//     * @param type type from NanoOKOptions
//     * @param pw handle to LaTeX file
//     */
//    public void writeTexSummary(int type, PrintWriter pw) {
//        pw.println("\\begin{table}[H]");
//        pw.println("{\\footnotesize");
//        pw.println("\\fontsize{9pt}{11pt}\\selectfont");
//        pw.println("\\begin{tabular}{l c c c c c c c}");
//        pw.println("          &             & {\\bf Number of} & {\\bf \\% of} & {\\bf Mean read} & {\\bf Aligned} & {\\bf Mean} & {\\bf Longest} \\\\");
//        pw.println("{\\bf ID} & {\\bf Size} & {\\bf Reads}     & {\\bf Reads}  & {\\bf length}    & {\\bf bases}   & {\\bf coverage} & {\\bf Perf Kmer} \\\\");
//        ArrayList<ReferenceSequence> sortedRefs = getSortedReferences();
//        for (int i=0; i<sortedRefs.size(); i++) {
//            ReferenceSequence r = sortedRefs.get(i);
//            ReferenceSequenceStats refStats = r.getStatsByType(type);
//            if ((sortedRefs.size() < 100) || (refStats.getNumberOfReadsWithAlignments() > 0)) {
//                pw.printf("%s & %d & %d & %.2f & %.2f & %d & %.2f & %d \\\\",
//                           r.getName().replaceAll("_", " "),
//                           r.getSize(),
//                           refStats.getNumberOfReadsWithAlignments(),
//                           100.0 * (double)refStats.getNumberOfReadsWithAlignments() / (double)overallStats.getStatsByType(type).getNumberOfReads(),
//                           refStats.getMeanReadLength(),
//                           refStats.getTotalAlignedBases(),
//                           (double)refStats.getTotalAlignedBases() / r.getSize(),
//                           refStats.getLongestPerfectKmer());
//                pw.println("");
//            }
//        }
//        pw.println("\\end{tabular}");
//        pw.println("}");
//        pw.println("\\end{table}");
//    }
}
