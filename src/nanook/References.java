package nanook;

import java.io.*;
import java.util.*;

/**
 * Represents the set of references (sequences) used for the analysis.
 * @author Richard Leggett
 */
public class References {
    private NanoOKOptions options;
    private File sizesFile;
    private Hashtable<String,ReferenceSequence> referenceSequences = new Hashtable();
    private int longestId = 0;
        
    /**
     * Constructor
     * @param o a NanoOKOptions object
     */
    public References(NanoOKOptions o)
    {
        options = o;
        getSizesFile();
        GCParser gcp = new GCParser();
                
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(sizesFile));
            String line = br.readLine();
            while (line != null) {
                String[] values = line.split("\\t");
                int size = Integer.parseInt(values[1]);
                
                System.out.println("- Reference " + values[2] + "\t" + size);
                
                ReferenceSequence refSeq = referenceSequences.get(values[0]);
                if (refSeq != null) {
                    System.out.println("Error: reference contig ID "+values[0]+" occurs more than once.");
                    System.exit(1);
                } else {
                    refSeq = new ReferenceSequence(values[0], size, values[2]);
                    options.checkAndMakeReferenceAnalysisDir(refSeq.getName());
                    referenceSequences.put(values[0], refSeq);
                    refSeq.openAlignmentSummaryFiles(options.getAnalysisDir());
                }
                
                if (values[0].length() > longestId) {
                    longestId = values[0].length();
                }
                
                gcp.parseSequence(o.getReferenceFile() + ".fasta", values[0], options.getAnalysisDir() + File.separator + values[2] + File.separator + values[2] + "_gc.txt", refSeq.getBinSize());            
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
     * Close alignment summary files for all references.
     */
    public void closeAlignmentFiles() {
        Set<String> keys = referenceSequences.keySet();
        for(String id : keys) {
            ReferenceSequence ref = referenceSequences.get(id);
            ref.closeAlignmentSummaryFiles();
        }
    }
    
    /**
     * Get a ReferenceSequence object from sequence ID.
     */
    public ReferenceSequence getReferenceById(String id) {
        ReferenceSequence r = referenceSequences.get(id);
        
        if (r == null) {
            System.out.println("Error: Couldn't find reference for "+id);
            System.exit(1);
        }
        
        return r;
    }
        
    /**
     * Return set of all reference sequence IDs.
     * @return a String set
     */
    public Set<String> getAllIds() {
        return referenceSequences.keySet();
    }
    
    /**
     * Initiate writing of all statistics data files used to generate graphs.
     * @param type a type, as defined in NanoOKOptions (for example TYPE_TEMPLATE)
     */
    public void writeReferenceStatFiles(int type) {
        Set<String> keys = referenceSequences.keySet();
        
        for(String id : keys) {
            ReferenceSequence ref = referenceSequences.get(id);
            ref.getStatsByType(type).writeCoverageData(options.getAnalysisDir() + File.separator + ref.getName() + File.separator + ref.getName() + "_" + options.getTypeFromInt(type) + "_coverage.txt", ref.getBinSize());
            ref.getStatsByType(type).writePerfectKmerHist(options.getAnalysisDir() + File.separator + ref.getName() + File.separator + ref.getName() + "_" + options.getTypeFromInt(type) + "_all_perfect_kmers.txt");
            ref.getStatsByType(type).writeBestPerfectKmerHist(options.getAnalysisDir() + File.separator + ref.getName() + File.separator + ref.getName() + "_" + options.getTypeFromInt(type) + "_best_perfect_kmers.txt");
            ref.getStatsByType(type).writeBestPerfectKmerHistCumulative(options.getAnalysisDir() + File.separator + ref.getName() + File.separator + ref.getName() + "_" + options.getTypeFromInt(type) + "_cumulative_perfect_kmers.txt");
            ref.getStatsByType(type).writeInsertionStats(options.getAnalysisDir() + File.separator + ref.getName() + File.separator + ref.getName() + "_" + options.getTypeFromInt(type) + "_insertions.txt");
            ref.getStatsByType(type).writeDeletionStats(options.getAnalysisDir() + File.separator + ref.getName() + File.separator + ref.getName() + "_" + options.getTypeFromInt(type) + "_deletions.txt");
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
        return referenceSequences.size();
    }
    
    /**
     * Work out the sizes filename for this set of references.
     */
    private void getSizesFile()
    {
        sizesFile = new File(options.getReferenceFile()+".sizes");
        if (! sizesFile.exists()) {
            sizesFile = new File(options.getReferenceFile()+".fasta.sizes");
            if (!sizesFile.exists()) {
                sizesFile = new File(options.getReferenceFile()+".fa.sizes");
            }
        }
        
        if (!sizesFile.exists()) {
            System.out.println("Error: can't read sizes file.");
            System.exit(1);
        }
    }
    
    /**
     * Write reference summary text file.
     * @param type type from NanoOKOptions
     */
    public void writeReferenceSummary(int type) {
       try {
            PrintWriter pw = new PrintWriter(new FileWriter(options.getAlignmentSummaryFilename(), true));
            String formatString = "%-"+longestId+"s %-12s %-10s %-10s\n";
            pw.println("");
            pw.printf(formatString, "Id", "Size", "ReadsAlign", "LongPerfKm");        
            List<String> keys = new ArrayList<String>(referenceSequences.keySet());
            Collections.sort(keys);
            for(String id : keys) {
                referenceSequences.get(id).getStatsByType(type).writeSummary(pw, "%-"+longestId+"s %-12d %-10d %-10d\n");
            }
            pw.close();
        } catch (IOException e) {
            System.out.println("writeReferenceSummary exception:");
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    /**
     * Write reference summary to LaTeX report.
     * @param type type from NanoOKOptions
     * @param pw handle to LaTeX file
     */
    public void writeTexSummary(int type, PrintWriter pw) {
        pw.println("\\begin{table}[H]");
        pw.println("{\\footnotesize");
        pw.println("\\fontsize{9pt}{11pt}\\selectfont");
        pw.println("\\begin{tabular}{l c c c c c c c}");
        pw.println("          &             & {\\bf Number of} & {\\bf Mean read} & {\\bf Aligned} & {\\bf Mean} & {\\bf Longest}   & {\\bf +ve/-ve} \\\\");
        pw.println("{\\bf Id} & {\\bf Size} & {\\bf Reads}     & {\\bf length}    & {\\bf bases}   & {\\bf coverage} & {\\bf Perf Kmer} & {\\bf strand \\%} \\\\");
        List<String> keys = new ArrayList<String>(referenceSequences.keySet());
        Collections.sort(keys);
        for(String id : keys) {
            ReferenceSequence r = referenceSequences.get(id);
            ReferenceSequenceStats refStats = r.getStatsByType(type);
            pw.printf("%s & %d & %d & %.2f & %d & %.2f & %d & %.2f / %.2f \\\\\n",
                       r.getName().replaceAll("_", " "),
                       r.getSize(),
                       refStats.getNumberOfReadsWithAlignments(),
                       refStats.getMeanReadLength(),
                       refStats.getTotalAlignedBases(),
                       (double)refStats.getTotalAlignedBases() / r.getSize(),
                       refStats.getLongestPerfectKmer(),
                       refStats.getAlignedPositiveStrandPercent(),
                       refStats.getAlignedNegativeStrandPercent());
        }
        pw.println("\\end{tabular}");
        pw.println("}");
        pw.println("\\end{table}");
    }
}
