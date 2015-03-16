package nanook;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Parser for LAST aligner files.
 * 
 * @author Richard Leggett
 */
public class LastParser extends AlignmentFileParser {
    private NanoOKOptions options;
    private ReadSetStats overallStats;
    private References references;
    private ReportWriter report;
    private int deletionSize = 0;
    private int insertionSize = 0;
    private int type = 0;    
    
    /**
     * Constructor.
     * @param t type of alignment (TYPE_TEMPLATE etc.)
     * @param o NanoOKOptions object
     * @param s ReadSetStats object to store stats in
     * @param r References object
     */
    public LastParser(int t, NanoOKOptions o, ReadSetStats s, References r) {
        options = o;
        overallStats = s;
        references = r;
        type = t;
    }
    
    /**
     * Parse a LAST file.
     * @param filename filename to parse
     * @param nonAlignedSummaryFile an AlignmentTableFile to output details of anything that doesn't align to
     * @return number of alignments parsed
     */
    public int parseFile(String filename, AlignmentsTableFile nonAlignedSummaryFile) {
        String leafName = new File(filename).getName();
        ArrayList<LastAlignment> alignments = new ArrayList();
            
        // Read all alignmnets and put into an ArrayList
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            
            do {
                line = br.readLine();
                if (line != null) {
                    if (line.startsWith("a score=")) {
                        int score = Integer.parseInt(line.substring(8));
                        LastAlignmentLine hitLine = new LastAlignmentLine(br.readLine());
                        LastAlignmentLine queryLine = new LastAlignmentLine(br.readLine());
                        LastAlignment al = new LastAlignment(score, hitLine, queryLine);
                        alignments.add(al);                        
                        //System.out.println("Score "+score);                       
                    }
                }
            } while (line != null);
            
            br.close();
        } catch (Exception e) {
            System.out.println("parseFile Exception:");
            e.printStackTrace();
            System.exit(1);
        }
        
        // Now, if we found alignments, sort in score order
        // Then merge any alignments matching to the top hit
        if (alignments.size() > 0) {
            Collections.sort(alignments);
            String readReferenceName = alignments.get(0).getHitLine().getName();
            ReferenceSequence readReference = references.getReferenceById(readReferenceName);
            LastAlignmentMerger merger = new LastAlignmentMerger(readReference, alignments.get(0).getQueryLine().getSeqSize(), overallStats, type);
            for (int i=0; i<alignments.size(); i++) {
                LastAlignment a = alignments.get(i);
                //System.out.println("Size="+a.getScore());
                if (a.getHitLine().getName().equals(readReferenceName)) {
                    merger.addAlignment(a);
                    //System.out.println(
                    //        a.getHitLine().getStart() + "\t" +
                    //        a.getHitLine().getAlnSize() + "\t" + 
                    //        a.getQueryLine().getStart() + "\t" +
                    //        a.getQueryLine().getAlnSize());
                }
            }
            AlignmentInfo stat = merger.endMergeAndStoreStats();
            readReference.getStatsByType(type).getAlignmentsTableFile().writeMergedAlignment(leafName, merger, stat);           
        } else {
            nonAlignedSummaryFile.writeNoAlignmentMessage(leafName);
            overallStats.addReadWithoutAlignment();
        } 
        
        return alignments.size();
    }
    
    /**
     * Helper method to check if to store insertion or deletion (and store it).
     * @param reference Reference object this alignment relates to
     * @param errorKmer The perfect sequence before this error
     */
    private void checkStoreInsertionsOrDeletions(ReferenceSequence reference, String errorKmer) {
        if (deletionSize > 0) {
            reference.getStatsByType(type).addDeletionError(deletionSize, errorKmer, overallStats);
            deletionSize = 0;
        }
                
        if (insertionSize > 0) {
            reference.getStatsByType(type).addInsertionError(insertionSize, errorKmer, overallStats);
            insertionSize = 0;
        }
    }
    
//    /**
//     * Compare hit string and query string base-by-base looking for matches.
//     * @param hit hit object
//     * @param query query object
//     * @param reference matching reference (ie. hit)
//     * @return 
//     */
//    public AlignmentInfo processMatches(LastAlignmentLine hit, LastAlignmentLine query, ReferenceSequence reference) {
//        String hitSeq = hit.getAlignment();
//        String querySeq = query.getAlignment();
//        int hitSize = hitSeq.length();
//        int querySize = querySeq.length();
//        int loopTo = hitSize <= querySize ? hitSize:querySize;
//        int identicalBases = 0;
//        int currentSize = 0;
//        int total = 0;
//        int count = 0;
//        int longest = 0;
//        String currentKmer = "";
//        String errorKmer = "";
//        AlignmentInfo ai;
//        
//        // Bodge for speed - need to change way AlignmentInfo works
//        int kSizes[] = {15, 17, 19, 21, 23, 25};
//        int kCounts[] = {0, 0, 0, 0, 0, 0};
//        int nk = 6;
//        
//        System.out.println("Deprecated");
//        System.exit(1);
//        
//        if (hitSize != querySize) {
//            System.out.println("hitSize not equal to querySize");
//        }
//        
//        insertionSize = 0;
//        deletionSize = 0;
//
//        for (int i=0; i<loopTo; i++) {
//            boolean fStoreKmer = false;
//            
//            if (hitSeq.charAt(i) == querySeq.charAt(i)) {
//                checkStoreInsertionsOrDeletions(reference, errorKmer);
//                errorKmer = "";
//                identicalBases++;
//                currentSize++;
//                currentKmer += querySeq.charAt(i);
//                                                
//                if (i == (loopTo-1)) {
//                    fStoreKmer = true;
//                }
//            } else {
//                fStoreKmer = true;
//                if (hitSeq.charAt(i) == '-') {
//                    if (deletionSize > 0) {
//                        checkStoreInsertionsOrDeletions(reference, errorKmer);
//                        errorKmer = "";
//                    }
//                    
//                    insertionSize++;
//                    
//                    // If this is a new insertion and we have a previous kmer, store it
//                    if ((insertionSize == 1) && (currentKmer != "")) {
//                        errorKmer = currentKmer;
//                    }
//                } else if (querySeq.charAt(i) == '-') {
//                    if (insertionSize > 0) {
//                        checkStoreInsertionsOrDeletions(reference, errorKmer);
//                        errorKmer = "";
//                    }
//
//                    deletionSize++;
//
//                    // If this is a new deletion and we have a previous kmer, store it
//                    if ((deletionSize == 1) && (currentKmer != "")) {
//                        errorKmer = currentKmer;
//                    }
//                } else {
//                    checkStoreInsertionsOrDeletions(reference, errorKmer);
//                    errorKmer = "";
//                    
//                    if (currentKmer != "") {
//                        errorKmer = currentKmer;
//                    }
//                    
//                    //System.out.println(query.getName());
//                    reference.getStatsByType(type).addSubstitutionError(errorKmer, hitSeq.charAt(i), querySeq.charAt(i), overallStats);
//                }
//                currentKmer = "";
//            }
//                        
//            if (fStoreKmer) {
//                if (currentSize > 0) {
//                    if (reference == null) {
//                        System.out.println("Oops: null reference");
//                        System.exit(1);
//                    }
//                    reference.getStatsByType(type).addPerfectKmer(currentSize);
//                    
//                    // Bodge - need to change
//                    for (int l=0; l<nk; l++) {
//                        if (currentSize >= kSizes[l]) {
//                            kCounts[l]++;
//                        }
//                    }
//                    
//                    total+=currentSize;
//                    count++;
//                   
//                    if (currentSize > longest) {
//                       longest=currentSize;
//                    }
//                   
//                    currentSize = 0;
//                }
//            }            
//        }
//        
//        ai = new AlignmentInfo(hit.getName(), hit.getSeqSize(), query.getName(), query.getSeqSize(), identicalBases, longest, total, count, loopTo, 0);
//        ai.addkCounts(nk, kSizes, kCounts);
//        overallStats.writekCounts(query.getName(), query.getSeqSize(), nk, kSizes, kCounts);
//        return ai;
//    }
}
