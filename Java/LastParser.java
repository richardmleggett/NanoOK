package nanotools;

import java.io.*;

public class LastParser extends AlignerParser {
    private NanoOKOptions options;
    private OverallAlignmentStats overallStats;
    private References references;
    private ReportWriter report;
    private int deletionSize = 0;
    private int insertionSize = 0;
        
    
    public LastParser(NanoOKOptions o, OverallAlignmentStats s, References r, ReportWriter rw) {
        options = o;
        overallStats = s;
        references = r;
        report = rw;
    }
    
    public int parseFile(String filename, AlignmentsTableFile summaryFile) {
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
                        AlignmentLine hitLine = new AlignmentLine(br.readLine());
                        AlignmentLine queryLine = new AlignmentLine(br.readLine());
                        ReferenceSequence reference = references.getReferenceById(hitLine.getName());
                        AlignmentEntry stat = processMatches(hitLine, queryLine, reference);
                        reference.addAlignmentStats(stat.getQuerySize(), stat.getAlignmentSize(), stat.getIdenticalBases());

                        if (stat.getLongest() > bestPerfectKmer) {
                            bestPerfectKmer = stat.getLongest();
                            bestKmerReference = reference;
                        }
                        
                        reference.addCoverage(hitLine.getStart(), hitLine.getAlnSize());
                        summaryFile.writeAlignment(leafName, hitLine, queryLine, stat);
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
            summaryFile.writeNoAlignmentMessage(leafName);
            overallStats.addReadWithoutAlignment();
        } else {
            bestKmerReference.addReadBestKmer(bestPerfectKmer);
            overallStats.addReadWithAlignment();
            overallStats.addReadBestKmer(bestPerfectKmer);
        }
        
        return nAlignments;
    }
    
    public void parseAll() {
        int nReads = 0;
        int nReadsWithAlignments = 0;
        int nReadsWithoutAlignments = 0;
        
        for (int type=0; type<3; type++) {
            String inputDir = options.getBaseDirectory() + options.getSeparator() + options.getSample() + options.getSeparator() + "last" + options.getSeparator() + options.getTypeFromInt(type);
            String outputFilename = options.getBaseDirectory() + options.getSeparator() + options.getSample() + options.getSeparator() + "analysis" + options.getSeparator() + options.getTypeFromInt(type) + "_alignment_summary.txt";
            AlignmentsTableFile perFileSummary = new AlignmentsTableFile(outputFilename);
        
            System.out.println("Parsing " + options.getTypeFromInt(type));            
            
            overallStats.clearStats(options.getTypeFromInt(type));
            references.clearReferenceStats();

            File folder = new File(inputDir);
            File[] listOfFiles = folder.listFiles();

            for (File file : listOfFiles) {
                if (file.isFile()) {
                    if (file.getName().endsWith(".maf")) {
                        String pathname = inputDir + options.getSeparator() + file.getName();
                        int nAlignments = parseFile(pathname, perFileSummary);
                        
                        if (nAlignments > 0) {
                            nReadsWithAlignments++;
                        } else {
                            nReadsWithoutAlignments++;
                        }
                        
                        nReads++;
                    }
                }
            }
                        
            perFileSummary.closeFile();
            overallStats.writeSummaryFile(options.getAlignmentSummaryFilename());
            report.beginAlignmentsSection(overallStats);
            references.writeReferenceStatFiles(type);
            references.writeReferenceSummary();
            references.writeTexSummary(report.getPrintWriter());
        }
    }
    
    private void checkStoreInsertionsOrDeletions(ReferenceSequence reference, String errorKmer) {
        if (deletionSize > 0) {
            reference.addDeletionError(deletionSize, errorKmer);
            deletionSize = 0;
        }
                
        if (insertionSize > 0) {
            reference.addInsertionError(insertionSize, errorKmer);
            insertionSize = 0;
        }
    }
    
    public AlignmentEntry processMatches(AlignmentLine hit, AlignmentLine query, ReferenceSequence reference) {
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
                    
                    reference.addSubstitutionError(errorKmer);
                }
                currentKmer = "";
            }
                        
            if (fStoreKmer) {
                if (currentSize > 0) {
                    if (reference == null) {
                        System.out.println("Oops: null reference");
                        System.exit(1);
                    }
                    reference.addPerfectKmer(currentSize);
                    total+=currentSize;
                    count++;
                   
                    if (currentSize > longest) {
                       longest=currentSize;
                    }
                   
                    currentSize = 0;
                }
            }            
        }
        
        return new AlignmentEntry(hit.getSeqSize(), query.getSeqSize(), identicalBases, longest, total, count, loopTo);
    }
}
