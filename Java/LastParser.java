package nanotools;

import java.io.*;

public class LastParser {
    private NanotoolsOptions options;
    private OverallAlignmentStats overallStats;
    private References references;
    
    public LastParser(NanotoolsOptions o, OverallAlignmentStats s, References r) {
        options = o;
        overallStats = s;
        references = r;
    }
    
    public int parseFile(String filename, AlignmentsTableFile summaryFile) {
        int bestPerfectKmer = 0;
        int nAlignments = 0;
        ReferenceContig bestKmerReference = null;
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
                        ReferenceContig reference = references.getReferenceById(hitLine.getName());
                        AlignmentEntry stat = processMatches(hitLine, queryLine, reference);
                        
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
        } catch (Exception ioe) {
            System.out.println("parseFile Exception:");
            System.out.println(ioe);
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
            references.writeReferenceStatFiles(type);
            references.writeReferenceSummary();
        }
    }
    
    public AlignmentEntry processMatches(AlignmentLine hit, AlignmentLine query, ReferenceContig reference) {
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
        
        for (int i=0; i<loopTo; i++) {
            boolean storeThis = false;
            
            if (hitSeq.charAt(i) == querySeq.charAt(i)) {
                identicalBases++;
                currentSize++;
                
                if (i == (loopTo-1)) {
                    storeThis = true;
                }
            } else {
                storeThis = true;
            }
                        
            if (storeThis) {
                if (currentSize > 0) {
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
        
        return new AlignmentEntry(hit.getAlnSize(), query.getAlnSize(), identicalBases, longest, total, count, loopTo);
    }
}
