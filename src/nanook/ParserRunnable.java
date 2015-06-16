package nanook;

import java.io.File;
import java.util.List;
import java.util.Random;

/**
 *
 * @author leggettr
 */
public class ParserRunnable implements Runnable
{
    private NanoOKOptions options;
    private ReadSetStats stats;
    private String readPath;
    private String alignmentPath;
    private AlignmentsTableFile nonAlignedSummary;
    private ReferenceSequence readReference = null;
    private SequenceReader sr;
    private int type;
    private int passfail;

    public ParserRunnable(NanoOKOptions o, ReadSetStats s, String rp, String ap, int t, int pf, AlignmentsTableFile nas) {
        options = o;
        readPath = rp;
        alignmentPath = ap;
        stats = s;
        type = t;
        passfail = pf;
        nonAlignedSummary = nas;
    }

    /**
     * Pick top alignment from sorted list. List is sorted in order of score, but if there are
     * matching scores, we pick one at random.
     * @param al list of alignments
     * @return index
     */
    private int pickTopAlignment(List<Alignment> al) {
        int index = 0;
        int topScore = al.get(0).getScore();
        int countSame = 0;
        
        if (!options.fixRandom()) {
            //for (int i=0; i<al.size(); i++) {
            //    System.out.println(i+" = "+al.get(i).getScore());
            //}
            
            // Find out how many have the same score
            while ((countSame < al.size()) && (al.get(countSame).getScore() == topScore)) {
                countSame++;
            }
            
            if (countSame > 1) {
                Random rn = new Random();
                index = rn.nextInt(countSame);
            }
            
            //System.out.println("Index chosen ("+countSame+") "+index);
        }
        
        return index;
    }
    
    /**
     * Parse alignment
     */
    private void parseAlignment()
    {
        File file = new File(alignmentPath);
        AlignmentFileParser parser = options.getParser();
        
        options.getLog().println("");
        options.getLog().println("> New file " + file.getName());
        options.getLog().println("");
        
        int nAlignments = parser.parseFile(alignmentPath, nonAlignedSummary, stats);
        
        if (nAlignments > 0) {
            parser.sortAlignments();
            List<Alignment> al = parser.getHighestScoringSet();
            int topAlignment = pickTopAlignment(al);
            String readReferenceName = al.get(topAlignment).getHitName();
            
            options.getLog().println("Query size = " + al.get(topAlignment).getQuerySequenceSize());
            options.getLog().println("  Hit size = " + al.get(topAlignment).getHitSequenceSize());
            
            readReference = options.getReferences().getReferenceById(readReferenceName);
            AlignmentMerger merger = new AlignmentMerger(options, readReference, al.get(topAlignment).getQuerySequenceSize(), stats, stats.getType());
            for (int i=topAlignment; i<al.size(); i++) {
                Alignment a = al.get(i);
                merger.addAlignment(a);
            }
            AlignmentInfo stat = merger.endMergeAndStoreStats();
            readReference.getStatsByType(stats.getType()).getAlignmentsTableFile().writeMergedAlignment(file.getName(), merger, stat);
        }
    }
    
    /**
     * Parse a FASTA or FASTQ file, noting length of reads etc.
     */
    private void readQueryFile() {
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
            
            stats.addLength(id, sr.getLength(i));
        }
    }

    /**
     * Entry point to thread
     */
    public void run() {        
        readQueryFile();
        stats.addReadFile(passfail);
        parseAlignment();
        if ((readReference != null) && (options.doKmerCounting())) {
            sr.storeKmers(0, readReference.getStatsByType(type).getReadKmerTable());
        }
    }
}
