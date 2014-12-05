package nanotools;

import java.io.*;

public class LastParser extends AlignmentFileParser {
    private NanoOKOptions options;
    private ReadSetStats overallStats;
    private References references;
    private ReportWriter report;
    private int deletionSize = 0;
    private int insertionSize = 0;
    private int type = 0;    
    
    public LastParser(int t, NanoOKOptions o, ReadSetStats s, References r) {
        options = o;
        overallStats = s;
        references = r;
        type = t;
    }
    
    public int parseFile(String filename, AlignmentsTableFile nonAlignedSummaryFile) {
        int bestPerfectKmer = 0;
        int nAlignments = 0;
        ReferenceSequence bestKmerReference = null;
        String leafName = new File(filename).getName();

        try
        {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            
            do {
                line = br.readLine();
                if (line != null) {
                    if (line.startsWith("a score=")) {
                        LastAlignmentLine hitLine = new LastAlignmentLine(br.readLine());
                        LastAlignmentLine queryLine = new LastAlignmentLine(br.readLine());
                        ReferenceSequence reference = references.getReferenceById(hitLine.getName());
                        AlignmentInfo stat = processMatches(hitLine, queryLine, reference);
                        reference.getStatsByType(type).addAlignmentStats(stat.getQuerySize(), stat.getAlignmentSize(), stat.getIdenticalBases());

                        if (stat.getLongest() > bestPerfectKmer) {
                            bestPerfectKmer = stat.getLongest();
                            bestKmerReference = reference;
                        }
                        
                        reference.getStatsByType(type).addCoverage(hitLine.getStart(), hitLine.getAlnSize());
                        reference.getStatsByType(type).getAlignmentsTableFile().writeAlignment(leafName, hitLine, queryLine, stat);
                        nAlignments++;
                    }
                }
            } while (line != null);
        } catch (Exception e) {
            System.out.println("parseFile Exception:");
            e.printStackTrace();
            System.exit(1);
        }
        
        if (nAlignments == 0) {
            nonAlignedSummaryFile.writeNoAlignmentMessage(leafName);
            overallStats.addReadWithoutAlignment();
        } else {
            bestKmerReference.getStatsByType(type).addReadBestKmer(bestPerfectKmer);
            overallStats.addReadWithAlignment();
            overallStats.addReadBestKmer(bestPerfectKmer);
        }
        
        return nAlignments;
    }
    
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
    
    public AlignmentInfo processMatches(LastAlignmentLine hit, LastAlignmentLine query, ReferenceSequence reference) {
        String hitSeq = hit.getAlignment();
        String querySeq = query.getAlignment();
        int hitSize = hitSeq.length();
        int querySize = querySeq.length();
        int loopTo = hitSize <= querySize ? hitSize:querySize;
        int identicalBases = 0;
        int currentSize = 0;
        int total = 0;
        int count = 0;
        int longest = 0;
        String currentKmer = "";
        String errorKmer = "";
        
        if (hitSize != querySize) {
            System.out.println("hitSize not equal to querySize");
        }
        
        insertionSize = 0;
        deletionSize = 0;

        for (int i=0; i<loopTo; i++) {
            boolean fStoreKmer = false;
            
            if (hitSeq.charAt(i) == querySeq.charAt(i)) {
                checkStoreInsertionsOrDeletions(reference, errorKmer);
                errorKmer = "";
                identicalBases++;
                currentSize++;
                currentKmer += querySeq.charAt(i);
                                                
                if (i == (loopTo-1)) {
                    fStoreKmer = true;
                }
            } else {
                fStoreKmer = true;
                if (hitSeq.charAt(i) == '-') {
                    if (deletionSize > 0) {
                        checkStoreInsertionsOrDeletions(reference, errorKmer);
                        errorKmer = "";
                    }
                    
                    insertionSize++;
                    
                    // If this is a new insertion and we have a previous kmer, store it
                    if ((insertionSize == 1) && (currentKmer != "")) {
                        errorKmer = currentKmer;
                    }
                } else if (querySeq.charAt(i) == '-') {
                    if (insertionSize > 0) {
                        checkStoreInsertionsOrDeletions(reference, errorKmer);
                        errorKmer = "";
                    }

                    deletionSize++;

                    // If this is a new deletion and we have a previous kmer, store it
                    if ((deletionSize == 1) && (currentKmer != "")) {
                        errorKmer = currentKmer;
                    }
                } else {
                    checkStoreInsertionsOrDeletions(reference, errorKmer);
                    errorKmer = "";
                    
                    if (currentKmer != "") {
                        errorKmer = currentKmer;
                    }
                    
                    //System.out.println(query.getName());
                    reference.getStatsByType(type).addSubstitutionError(errorKmer, hitSeq.charAt(i), querySeq.charAt(i), overallStats);
                }
                currentKmer = "";
            }
                        
            if (fStoreKmer) {
                if (currentSize > 0) {
                    if (reference == null) {
                        System.out.println("Oops: null reference");
                        System.exit(1);
                    }
                    reference.getStatsByType(type).addPerfectKmer(currentSize);
                    total+=currentSize;
                    count++;
                   
                    if (currentSize > longest) {
                       longest=currentSize;
                    }
                   
                    currentSize = 0;
                }
            }            
        }
        
        return new AlignmentInfo(hit.getName(), hit.getSeqSize(), query.getName(), query.getSeqSize(), identicalBases, longest, total, count, loopTo);
    }
}
