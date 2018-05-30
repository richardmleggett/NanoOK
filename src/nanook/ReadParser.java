
/*
 TO DO:
 - Use the AlignmentFileStats structure to store ALL alignment stats and write this to the separate alignment files.
 - This requires rewriting the current parsers and methods.
*/
package nanook;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 *
 * @author leggettr
 */
public class ReadParser {
    private NanoOKOptions options;
    private SequenceReader sr;

    
    public ReadParser(NanoOKOptions o) {
        options = o;
    }
    
    /**
     * Parse a FASTA or FASTQ file, noting length of reads etc.
     */
    private void readQueryFile(String readPath, PrintWriter pw) {
        int nReadsInFile;

        sr = new SequenceReader(true);
        
        if (options.getReadFormat() == NanoOKOptions.FASTQ) {
            nReadsInFile = sr.indexFASTQFile(readPath);
        } else {
            nReadsInFile = sr.indexFASTAFile(readPath, null, true);
        }

        if (nReadsInFile > 1) {
            System.out.println("Warning: File "+readPath+" has more than 1 read. NanoOK can't currently handle this.");
        }

        for (int i=0; i<sr.getSequenceCount(); i++) {
            String id = sr.getID(i);
            
            if (id.startsWith("00000000-0000-0000-0000-000000000000")) {
                System.out.println("Error:");
                System.out.println(readPath);
                System.out.println("The reads in this file do not have unique IDs because they were generated when MinKNOW was producing UUIDs, but Metrichor was not using them. To fix, run nanook_extract_reads with the -fixids option.");
                System.exit(1);
            }
            
            //stats.addLength(readPath, id, sr.getLength(i), sr.getGC(i));
            pw.printf("Read:%s\t%d\t%.2f\n", id, sr.getLength(i), sr.getGC(i));
        }
    }    
    
   /**
     * Parse alignment
     */
    private void parseAlignment(String alignmentPath)
    {
//        try {
//            File file = new File(alignmentPath);
//            AlignmentFileParser parser = options.getParser();
//
//            options.getLog().println("");
//            options.getLog().println("> New file " + file.getName());
//            options.getLog().println("");
//
//            int nAlignments = parser.parseFile(alignmentPath, nonAlignedSummary, stats);
//
//            if (nAlignments > 0) {
//                parser.sortAlignments();
//                List<Alignment> al = parser.getHighestScoringSet();
//                int topAlignment = pickTopAlignment(al);
//                String readReferenceName = al.get(topAlignment).getHitName();
//
//                options.getLog().println("Query size = " + al.get(topAlignment).getQuerySequenceSize());
//                options.getLog().println("  Hit size = " + al.get(topAlignment).getHitSequenceSize());
//
//                readReference = options.getReferences().getReferenceById(readReferenceName);
//                AlignmentMerger merger = new AlignmentMerger(options, readReference, al.get(topAlignment).getQuerySequenceSize(), stats, stats.getType());
//                for (int i=topAlignment; i<al.size(); i++) {
//                    Alignment a = al.get(i);
//                    merger.addAlignment(a);
//                }
//                AlignmentInfo ais = merger.endMergeAndStoreStats();
//                readReference.getStatsByType(stats.getType()).getAlignmentsTableFile().writeMergedAlignment(stats, file.getName(), merger, ais);
//            }
//        } catch (Exception e) {
//            System.out.println("Error parsing alignment "+ alignmentPath);
//            options.setReturnValue(1);
//            options.getLog().println("Error parsing alignment " + alignmentPath);
//            e.printStackTrace();
//        }
    }
    
    public void parse(String fastaqPathname, String alignmentPathname, String parserPathname) {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(parserPathname, false));
            pw.println("NanoOKVersion:"+NanoOK.VERSION_STRING);
            pw.println("FastAQPath:"+fastaqPathname);
            pw.println("AlignmentPath:"+alignmentPathname);
            pw.println("Aligner:"+options.getAligner());
            
            readQueryFile(fastaqPathname, pw);
            //stats.addReadFile(passfail);
            //parseAlignment();
            //if ((readReference != null) && (options.doKmerCounting())) {
            //    sr.storeKmers(0, readReference.getStatsByType(type).getReadKmerTable());
            //}
                        
            pw.close();
        } catch (IOException e) {
            System.out.println("parseAlignment exception");
            e.printStackTrace();
        }        
    }
}
