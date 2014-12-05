package nanotools;

import java.io.*;
import java.util.*;

public class References {
    private NanoOKOptions options;
    private File sizesFile;
    private Hashtable<String,ReferenceSequence> referenceSequences = new Hashtable();
    private int longestId = 0;
        
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
                float b = size / 1000;
                int binSize = 500 * (1 + Math.round(b / 500));
                
                System.out.println("size = " + size + " Binsize = " + binSize);
                
                ReferenceSequence refSeq = referenceSequences.get(values[0]);
                if (refSeq != null) {
                    System.out.println("Error: reference contig ID "+values[0]+" occurs more than once.");
                    System.exit(1);
                } else {
                    refSeq = new ReferenceSequence(values[0], size, values[2]);                    
                    referenceSequences.put(values[0], refSeq);
                    refSeq.openAlignmentSummaryFiles(options.getAnalysisDir() + options.getSeparator());
                }
                
                if (values[0].length() > longestId) {
                    longestId = values[0].length();
                }
                
                gcp.parseSequence(o.getReferenceFile() + ".fasta", values[0], options.getAnalysisDir()+options.getSeparator()+values[2]+"_gc.txt", binSize);               
                line = br.readLine();
            }
        } catch (Exception e) {
            System.out.println("NanotoolsReferences Exception:");
            e.printStackTrace();
            System.exit(1);
        }
    }    

    public void closeAlignmentFiles() {
        Set<String> keys = referenceSequences.keySet();
        for(String id : keys) {
            ReferenceSequence ref = referenceSequences.get(id);
            ref.closeAlignmentSummaryFiles();
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
        
    public Set<String> getAllIds() {
        return referenceSequences.keySet();
    }
    
    public void writeReferenceStatFiles(int type) {
        Set<String> keys = referenceSequences.keySet();
        String analysisDir = options.getBaseDirectory() + options.getSeparator() + options.getSample() + options.getSeparator() + "analysis";
        
        for(String id : keys) {
            ReferenceSequence ref = referenceSequences.get(id);
            ref.getStatsByType(type).writeCoverageData(analysisDir + options.getSeparator() + ref.getName() + "_" + options.getTypeFromInt(type) + "_coverage.txt", options.getCoverageBinSize());
            ref.getStatsByType(type).writePerfectKmerHist(analysisDir + options.getSeparator() + ref.getName() + "_" + options.getTypeFromInt(type) + "_all_perfect_kmers.txt");
            ref.getStatsByType(type).writeBestPerfectKmerHist(analysisDir + options.getSeparator() + ref.getName()+ "_" + options.getTypeFromInt(type) + "_best_perfect_kmers.txt");
            ref.getStatsByType(type).writeBestPerfectKmerHistCumulative(analysisDir + options.getSeparator() + ref.getName()+ "_" + options.getTypeFromInt(type) + "_cumulative_perfect_kmers.txt");
            ref.getStatsByType(type).writeInsertionStats(analysisDir + options.getSeparator() + ref.getName()+ "_" + options.getTypeFromInt(type) + "_insertions.txt");
            ref.getStatsByType(type).writeDeletionStats(analysisDir + options.getSeparator() + ref.getName()+ "_" + options.getTypeFromInt(type) + "_deletions.txt");
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
    
    public void writeTexSummary(int type, PrintWriter pw) {
        pw.println("\\begin{table}[H]");
        pw.println("{\\footnotesize");
        pw.println("\\fontsize{9pt}{11pt}\\selectfont");
        pw.println("\\begin{tabular}{l c c c}");
        pw.println("{\\bf Id} & {\\bf Size} & {\\bf Reads aligning} & {\\bf Longest Perfect Kmer} \\\\");
        List<String> keys = new ArrayList<String>(referenceSequences.keySet());
        Collections.sort(keys);
        for(String id : keys) {
            ReferenceSequence r = referenceSequences.get(id);
            pw.println(r.getName().replaceAll("_", " ") + " & " + r.getSize() + " & " + r.getStatsByType(type).getNumberOfReadsWithAlignments() + " & " + r.getStatsByType(type).getLongestPerfectKmer() + " \\\\");
        }
        pw.println("\\end{tabular}");
        pw.println("}");
        pw.println("\\end{table}");
    }
}
