package nanotools;

import java.io.*;
import java.util.*;

public class References {
    private NanotoolsOptions options;
    private File sizesFile;
    private Hashtable<String,ReferenceSequence> referenceSequences = new Hashtable();
    private int longestId = 0;
        
    public References(NanotoolsOptions o)
    {
        options = o;
        getSizesFile();
        
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(sizesFile));
            String line = br.readLine();
            while (line != null) {
                String[] values = line.split("\\t");
                ReferenceSequence refSeq = referenceSequences.get(values[0]);
                if (refSeq != null) {
                    System.out.println("Error: reference contig ID "+values[0]+" occurs more than once.");
                    System.exit(1);
                } else {
                    referenceSequences.put(values[0], new ReferenceSequence(values[0], Integer.parseInt(values[1]), values[2]));
                }
                
                if (values[0].length() > longestId) {
                    longestId = values[0].length();
                }
                
                line = br.readLine();
            }
        } catch (Exception e) {
            System.out.println("NanotoolsReferences Exception:");
            e.printStackTrace();
            System.exit(1);
        }
    }    

    public ReferenceSequence getReferenceById(String id) {
        ReferenceSequence r = referenceSequences.get(id);
        
        if (r == null) {
            System.out.println("Error: Couldn't find reference for "+id);
            System.exit(1);
        }
        
        return r;
    }
    
    public void clearReferenceStats() {
        Set<String> keys = referenceSequences.keySet();
        for(String id : keys) {
            referenceSequences.get(id).clearStats();
        }        
    }
    
    public Set<String> getAllIds() {
        return referenceSequences.keySet();
    }
    
    public void writeReferenceStatFiles(int type) {
        Set<String> keys = referenceSequences.keySet();
        String analysisDir = options.getBaseDirectory() + options.getSeparator() + options.getSample() + options.getSeparator() + "analysis";
        
        for(String id : keys) {
            ReferenceSequence ref = referenceSequences.get(id);
            ref.writeCoverageData(analysisDir + options.getSeparator() + ref.getName() + "_" + options.getTypeFromInt(type) + "_coverage.txt", options.getCoverageBinSize());
            ref.writePerfectKmerHist(analysisDir + options.getSeparator() + ref.getName() + "_" + options.getTypeFromInt(type) + "_all_perfect_kmers.txt");
            ref.writeBestPerfectKmerHist(analysisDir + options.getSeparator() + ref.getName()+ "_" + options.getTypeFromInt(type) + "_best_perfect_kmers.txt");
            ref.writeBestPerfectKmerHistCumulative(analysisDir + options.getSeparator() + ref.getName()+ "_" + options.getTypeFromInt(type) + "_cumulative_perfect_kmers.txt");
        }        
    }
    
    public int getLongestIdLength() {
        return longestId;
    }
    
    public int getNumberOfReferences() {
        return referenceSequences.size();
    }
    
    private void getSizesFile()
    {
        sizesFile = new File(options.getReference()+".sizes");
        if (! sizesFile.exists()) {
            sizesFile = new File(options.getReference()+".fasta.sizes");
            if (!sizesFile.exists()) {
                sizesFile = new File(options.getReference()+".fa.sizes");
            }
        }
        
        if (!sizesFile.exists()) {
            System.out.println("Error: can't read sizes file.");
            System.exit(1);
        }
    }
    
    public void writeReferenceSummary() {
       try {
            PrintWriter pw = new PrintWriter(new FileWriter(options.getAlignmentSummaryFilename(), true));
            String formatString = "%-"+longestId+"s %-12s %-10s %-10s\n";
            pw.println("");
            pw.printf(formatString, "Id", "Size", "ReadsAlign", "LongPerfKm");        
            Set<String> keys = referenceSequences.keySet();
            for(String id : keys) {
                referenceSequences.get(id).writeSummary(pw, "%-"+longestId+"s %-12d %-10d %-10d\n");
            }
            pw.close();
        } catch (IOException e) {
            System.out.println("writeReferenceSummary exception:");
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public void writeTexSummary(PrintWriter pw) {
        pw.println("\\begin{table}[H]");
        pw.println("{\\footnotesize");
        pw.println("\\fontsize{9pt}{11pt}\\selectfont");
        pw.println("\\begin{tabular}{l c c c}");
        pw.println("{\\bf Id} & {\\bf Size} & {\\bf Reads aligning} & {\\bf Longest Perfect Kmer} \\\\");
        Set<String> keys = referenceSequences.keySet();
        for(String id : keys) {
            ReferenceSequence r = referenceSequences.get(id);
            pw.println(r.getName().replaceAll("_", " ") + " & " + r.getSize() + " & " + r.getNumberOfReadsWithAlignments() + " & " + r.getLongestPerfectKmer() + " \\\\");
        }
        pw.println("\\end{tabular}");
        pw.println("}");
        pw.println("\\end{table}");
    }
}
