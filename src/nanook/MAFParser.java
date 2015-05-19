package nanook;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Parser for LAST aligner files.
 * 
 * @author Richard Leggett
 */
public abstract class MAFParser {
    private NanoOKOptions options;
    private References references;
    private ReportWriter report;
    ArrayList<Alignment> alignments;
    String leafName;
    
    /**
     * Constructor.
     * @param o NanoOKOptions object
     * @param s ReadSetStats object to store stats in
     * @param r References object
     */
    public MAFParser(NanoOKOptions o, References r) {
        options = o;
        references = r;
    }
    
    /**
     * Get file extension of alignment files
     * @return 
     */
    public String getAlignmentFileExtension() {
        return ".maf";
    }
    
    /**
     * Parse a LAST file.
     * @param filename filename to parse
     * @param nonAlignedSummaryFile an AlignmentTableFile to output details of anything that doesn't align to
     * @return number of alignments parsed
     */
    public int parseFile(String filename, AlignmentsTableFile nonAlignedSummaryFile, ReadSetStats overallStats) {            
        alignments = new ArrayList();
        leafName = new File(filename).getName();
        
         // Read all alignmnets and put into an ArrayList
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            
            do {
                line = br.readLine();
                if (line != null) {
                    if (line.startsWith("a score=")) {
                        String[] fields = line.substring(8).split(" ");
                        int score = Integer.parseInt(fields[0]);
                        MAFAlignmentLine hitLine = new MAFAlignmentLine(br.readLine());
                        MAFAlignmentLine queryLine = new MAFAlignmentLine(br.readLine());
                        Alignment al = new Alignment(score,
                                                     queryLine.getName(), 
                                                     queryLine.getSeqSize(),
                                                     queryLine.getStart(),
                                                     queryLine.getAlnSize(),
                                                     queryLine.getAlignment(),
                                                     hitLine.getName(),
                                                     hitLine.getSeqSize(),
                                                     hitLine.getStart(),
                                                     hitLine.getAlnSize(),
                                                     hitLine.getAlignment(),
                                                     false);
                        alignments.add(al);                        
                    }
                }
            } while (line != null);            
            br.close();
            
            if (alignments.size() == 0) {
                nonAlignedSummaryFile.writeNoAlignmentMessage(leafName);
                overallStats.addReadWithoutAlignment();
            }
            
        } catch (Exception e) {
            System.out.println("parseFile Exception:");
            e.printStackTrace();
            System.exit(1);
        }
        
        return alignments.size();
    }
    
    /**
     * Sort alignments in order of score
     */
    public void sortAlignments() {       
        if (alignments.size() > 0) {
            Collections.sort(alignments);
        }        
    }
    
    /**
     * Get the set of alignments that match the highest scoring reference
     */
    public ArrayList getHighestScoringSet() {
        ArrayList hss = new ArrayList();
        
        if (alignments.size() > 0) {
            String readReferenceName = alignments.get(0).getHitName();
            ReferenceSequence readReference = references.getReferenceById(readReferenceName);
            for (int i=0; i<alignments.size(); i++) {
                Alignment a = alignments.get(i);
                if (a.getHitName().equals(readReferenceName)) {
                    hss.add(a);
                }
            }
        } 
        
        return hss;
    }
 }
