package nanook;

/**
 * Class to merge alignments
 * 
 * @author leggettr
 */
public class LastAlignmentMerger {
    private ReferenceSequence reference;
    private ReadSetStats overallStats;    
    private int readLength;
    private int[] covered;
    private int deletionSize = 0;
    private int insertionSize = 0;
    private String errorKmer = "";    
    private int type;
    private int kmerTotal = 0;
    private int kmerCount = 0;
    private int currentPerfectKmerSize = 0;
    private int longestPerfectKmer = 0;
    private int overallQueryStart = -1;
    private int overallQueryEnd = -1;
    private int overallHitStart = -1;
    private int overallHitEnd = -1;
    private int hitSeqSize = 0;
    private int querySeqSize = 0;
    private String queryName =null;
    private String hitName = null;
    private int identicalBases = 0;
    private int alignmentSize = 0;
    private int alignmentSizeWithoutIndels = 0;

    // Bodge for speed - need to change way AlignmentInfo works
    int kSizes[] = {15, 17, 19, 21, 23, 25};
    int kCounts[] = {0, 0, 0, 0, 0, 0};
    int nk = 6;
    
    /**
     * Constructor
     * @param r the reference, as a ReferenceSequence object
     * @param l the read length
     * @param s the read set stats for this read set
     * @param t the type number of read (defined in NanoOKOptions)
     */
    public LastAlignmentMerger(ReferenceSequence r, int l, ReadSetStats s, int t) {
        reference = r;
        readLength = l;
        overallStats = s;
        type = t;
        
        covered = new int[readLength];
    }

    /**
     * Helper method to check if to store insertion or deletion (and store it).
     * @param reference Reference object this alignment relates to
     * @param errorKmer The perfect sequence before this error
     */
    private void checkStoreInsertionsOrDeletions() {
        if (deletionSize > 0) {
            reference.getStatsByType(type).addDeletionError(deletionSize, errorKmer, overallStats);
            deletionSize = 0;
        }
                
        if (insertionSize > 0) {
            reference.getStatsByType(type).addInsertionError(insertionSize, errorKmer, overallStats);
            insertionSize = 0;
        }
        
        errorKmer = "";
    }    
    
    private void storePerfectKmerLength() {
        // Store perfect kmers
        if (currentPerfectKmerSize > 0) {
            reference.getStatsByType(type).addPerfectKmer(currentPerfectKmerSize);

            // Bodge - need to change
            for (int l=0; l<nk; l++) {
                if (currentPerfectKmerSize >= kSizes[l]) {
                    kCounts[l]++;
                }
            }

            kmerTotal+=currentPerfectKmerSize;
            kmerCount++;

            if (currentPerfectKmerSize > longestPerfectKmer) {
               longestPerfectKmer = currentPerfectKmerSize;
            }

            currentPerfectKmerSize = 0;
        }
    }
    
    /**
     * Merge in a new alignment
     * @param a a LastAlignment
     */
    public void addAlignment(LastAlignment a) {
        addAlignment(a.getHitLine(), a.getQueryLine());
    }
    
    /**
     * Merge in a new alignment
     * @param hit hit object
     * @param query query object
     * @param reference matching reference (ie. hit)
     * @return 
     */
    public void addAlignment(LastAlignmentLine hit, LastAlignmentLine query) {
        String hitSeq = hit.getAlignment();
        String querySeq = query.getAlignment();
        int hitSize = hitSeq.length();
        int querySize = querySeq.length();
        int loopFrom = 0;
        int loopTo = hitSize <= querySize ? hitSize:querySize;
        int queryPos = query.getStart();
        int hitPos = hit.getStart();
        String currentKmer = "";
        AlignmentInfo ai;

        // Deal with hit and query names
        if (queryName == null) {
            queryName = query.getName();
            hitName = hit.getName();
            querySeqSize = query.getSeqSize();
            hitSeqSize = hit.getSeqSize();
        }
        
        if (! hitName.equals(hit.getName())) {
            System.out.println("Error: hit name doesn't match!");
            System.exit(1);
        }

        if (! queryName.equals(query.getName())) {
            System.out.println("Error: hit name doesn't match!");
            System.exit(1);
        }
        
        // Store alignment size
        if ((overallQueryStart == -1) || (queryPos < overallQueryStart)) {
            overallQueryStart = queryPos;
        }        
        if ((overallHitStart == -1) || (hitPos < overallHitStart)) {
            overallHitStart = hitPos;
        }        
        
        // Expect these to be equal
        if (hitSize != querySize) {
            System.out.println("hitSize not equal to querySize");
        }
        
        currentPerfectKmerSize = 0;
        insertionSize = 0;
        deletionSize = 0;
        errorKmer = "";
        
        // If alignment starts in middle of area already covered, move to end
        if (covered[queryPos] == 1) {
            while((loopFrom < loopTo) && (covered[queryPos] == 1)) {
                if (hitSeq.charAt(loopFrom)== '-') {
                    queryPos++;
                } else if (querySeq.charAt(loopFrom) == '-') {
                    hitPos++;
                } else {
                    queryPos++;
                    hitPos++;
                }
                loopFrom++;
            }
        }
        
        for (int i=loopFrom; i<loopTo; i++) {
            // If we've ventured into previously covered territory, break
            if (covered[queryPos] == 1) {
                break;
            }
            
            // Identical bases
            if (hitSeq.charAt(i) == querySeq.charAt(i)) {
                // Check if there are any insertions or deletions to store
                checkStoreInsertionsOrDeletions();
                
                currentPerfectKmerSize++;
                currentKmer += querySeq.charAt(i);
                    
                // If reached end, store perfect sequence length
                if (i == (loopTo-1)) {
                    storePerfectKmerLength();
                }

                // Mark this position and move on
                identicalBases++;
                covered[queryPos]= 1;
                queryPos++;
                hitPos++;
                alignmentSizeWithoutIndels++;
            } else {
                // An insertion or deletion or substitution, so store perfect sequence length, if we have some
                if (currentPerfectKmerSize > 0) {
                    storePerfectKmerLength();
                }
                
                // Insertion
                if (hitSeq.charAt(i) == '-') {
                    // If new insertion, check if we have a previous deletion we were tracking
                    // And store the current perfect kmer as the one associated with this insertion
                    if (insertionSize == 0) {
                        checkStoreInsertionsOrDeletions();
                        errorKmer = currentKmer;
                    }
                                            
                    // Keep track of insertion size
                    insertionSize++;      
                    
                    // Keep track of position
                    queryPos++;
                }
                
                // Deletion
                else if (querySeq.charAt(i) == '-') {
                    // If new deletion, check if we have a previous insertion we were tracking
                    // And store the current perfect kmer as the one associated with this deletion
                    if (deletionSize == 0) {
                        checkStoreInsertionsOrDeletions();
                        errorKmer = currentKmer;
                    }

                    // Keep track of size
                    deletionSize++;
                    
                    // Keep track of position
                    hitPos++;
                }
                
                // Substitution
                else {
                    // Check if previous insertion or deletion we were tracking
                    checkStoreInsertionsOrDeletions();
                    
                    // Store current perfect kmer associated with this substitution
                    errorKmer = currentKmer;
                    
                    // Store substitution
                    reference.getStatsByType(type).addSubstitutionError(errorKmer, hitSeq.charAt(i), querySeq.charAt(i), overallStats);
                    
                    // Mark this position and move on
                    covered[queryPos] = 1;
                    queryPos++;
                    hitPos++;
                    alignmentSizeWithoutIndels++;
            }
                
                // Reset current kmer
                currentKmer = "";
            }     
            
            alignmentSize++;
        }

        if ((overallQueryEnd == -1) || (queryPos > overallQueryEnd)) {
            overallQueryEnd = queryPos;
        }
        if ((overallHitEnd == -1) || (hitPos > overallHitEnd)) {
            overallHitEnd = hitPos;
        }

        reference.getStatsByType(type).addCoverage(hit.getStart(), hit.getAlnSize());    
    }  
    
    /**
     * Declare end of alignment merge
     * @return an AlignmentInfo object
     */
    public AlignmentInfo endMergeAndStoreStats() {
        AlignmentInfo ai = new AlignmentInfo(hitName,
                                             hitSeqSize,
                                             queryName,
                                             querySeqSize,
                                             identicalBases,
                                             longestPerfectKmer,
                                             kmerTotal,
                                             kmerCount,
                                             alignmentSize,
                                             alignmentSizeWithoutIndels,
                                             overallQueryEnd - overallQueryStart);

        ai.addkCounts(nk, kSizes, kCounts);        
        
        overallStats.writekCounts(queryName, querySeqSize, nk, kSizes, kCounts);
        overallStats.addReadWithAlignment();
        overallStats.addReadBestKmer(longestPerfectKmer);
        
        reference.getStatsByType(type).addAlignmentStats(querySeqSize, alignmentSize, alignmentSizeWithoutIndels, identicalBases, "?", "?");
        reference.getStatsByType(type).addReadBestKmer(longestPerfectKmer);       
        
        return ai;
    }
    
    /**
     * Get query start position of merged alignment
     * @return start position
     */
    public int getOverallQueryStart() {
        return overallQueryStart;
    }

    /**
     * Get query end position of merged alignment
     * @return end position
     */
    public int getOverallQueryEnd() {
        return overallQueryEnd;
    }

    /**
     * Get hit start position of merged alignment
     * @return start position
     */
    public int getOverallHitStart() {
        return overallHitStart;
    }

    /**
     * Get hit end position of merged alignment
     * @return end position
     */
    public int getOverallHitEnd() {
        return overallHitEnd;
    }

    /**
     * Get size of query covered by merged alignment
     * @return size of alignment
     */
    public int getOverallQuerySize() {
        return overallQueryEnd - overallQueryStart;
    }

    /**
     * Get size of hit covered by merged alignment
     * @return size of hit alignment
     */
    public int getOverallHitSize() {
        return overallHitEnd - overallHitStart;
    }
    
    /**
     * Get size of alignment without indels
     * @return size
     */
    public int getAlignmentSize() {
        return alignmentSizeWithoutIndels;
    }
}
