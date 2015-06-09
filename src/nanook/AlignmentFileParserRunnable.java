package nanook;

import java.io.File;
import java.util.List;
import java.util.Random;

/**
 *
 * @author leggettr
 */
public class AlignmentFileParserRunnable implements Runnable
{    
    NanoOKOptions options;
    String pathname;    
    ReadSetStats stats;
    AlignmentsTableFile nonAlignedSummary;
    
    public AlignmentFileParserRunnable(NanoOKOptions o, ReadSetStats s, String pn, AlignmentsTableFile nas) {
        options = o;
        stats = s;
        pathname = pn;
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
    
    public void run() {
        File file = new File(pathname);
        AlignmentFileParser parser = options.getParser();

        options.getLog().println("");
        options.getLog().println("> New file " + file.getName());
        options.getLog().println("");

        int nAlignments = parser.parseFile(pathname, nonAlignedSummary, stats);

        if (nAlignments > 0) {
            parser.sortAlignments();
            List<Alignment> al = parser.getHighestScoringSet();
            int topAlignment = pickTopAlignment(al);
            String readReferenceName = al.get(topAlignment).getHitName();

            options.getLog().println("Query size = " + al.get(topAlignment).getQuerySequenceSize());
            options.getLog().println("  Hit size = " + al.get(topAlignment).getHitSequenceSize());

            ReferenceSequence readReference = options.getReferences().getReferenceById(readReferenceName);
            AlignmentMerger merger = new AlignmentMerger(options, readReference, al.get(topAlignment).getQuerySequenceSize(), stats, stats.getType());
            for (int i=topAlignment; i<al.size(); i++) {
                Alignment a = al.get(i);
                merger.addAlignment(a);
            }
            AlignmentInfo stat = merger.endMergeAndStoreStats();
            readReference.getStatsByType(stats.getType()).getAlignmentsTableFile().writeMergedAlignment(file.getName(), merger, stat);  
        }
    }
}
