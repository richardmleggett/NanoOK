/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nanook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parser for SAM format files.
 * 
 * @author Richard Leggett
 */
public abstract class SAMParser {
    private NanoOKOptions options;
    private References references;
    private ReportWriter report;
    private String programID = null;
    ArrayList<Alignment> alignments;
    private Hashtable<String,Integer> referenceSizes;
    String leafName;
    
    /**
     * Parse a SAM file.
     * @param filename filename to parse
     * @param nonAlignedSummaryFile an AlignmentTableFile to output details of anything that doesn't align to
     * @return number of alignments parsed
     */
    public SAMParser(NanoOKOptions o, References r) {
        options = o;
        references = r;
    }
    
    /**
     * Get file extension of alignment files
     * @return 
     */
    public String getAlignmentFileExtension() {
        return ".sam";
    }

    
    private void processReferenceTag(String s) {
        Pattern pattern = Pattern.compile("@SQ(\\s+)SN:(\\S+)(\\s+)LN:(\\S+)");
        Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            String refID = matcher.group(2);
            int size = Integer.parseInt(matcher.group(4));
            if (referenceSizes.containsKey(refID)) {
                System.out.println("Warning: Reference "+refID+" already seen.");
            } else {
                referenceSizes.put(refID, size);
            }
        } else {
            System.out.println("Warning: Badly formated tag: " + s);
        }
    }
    
    /**
     * Process @PG tag in SAM file
     * @param s 
     */
    private void processProgramTag(String s) {
        Pattern pattern = Pattern.compile("(\\s+)ID:(\\S+)(\\s+)");
        Matcher matcher = pattern.matcher(s);
        if (matcher.find()) {
            programID = matcher.group(2);
        }
    }
    
    /**
     * Process an alignment line from a SAM file
     * @param s the line
     * @param outputFilename .maf file to write
     * @return ]
     */
    private Alignment processAlignmentLine(String s, String outputFilename, ReadSetStats overallStats) {
        String[] cols = s.split("\t");
        String queryName = cols[0];
        int flags = Integer.parseInt(cols[1]);
        String hitName = cols[2];
        int hitStart = Integer.parseInt(cols[3]) - 1; // SAM is 1-based, Last and NanoOK 0-based
        int mapQuality = Integer.parseInt(cols[4]);
        String cigar = cols[5];
        String rNext = cols[6];
        int pNext = Integer.parseInt(cols[7]);
        int tLen = Integer.parseInt(cols[8]);
        String seq = cols[9];
        String qual = cols[10];
        boolean mapped = ((flags & 0x04) == 0x04) ? false:true;
        int queryStart;
        Alignment al = null;
        
        if (options.getAligner().equals("blasr")) {
            queryName = cols[0].substring(0, cols[0].lastIndexOf("/"));
        }
        
        if (mapped) {
            ReferenceSequence readReference = references.getReferenceById(hitName);
            if (readReference != null) {        
                int readLength = overallStats.getReadLength(queryName);
                if (readLength != -1) {
                    CIGARString cs = new CIGARString(cigar, seq, leafName, queryName, hitStart, options.getReferenceFile(), readReference);
                    if (cs.processString()) {
                        //System.out.println("hitName "+hitName);
                        al = new Alignment(mapQuality,
                                           queryName, 
                                           readLength,
                                           cs.getQueryStart(),
                                           cs.getQueryAlnSize(),
                                           cs.getQueryString(),
                                           hitName,
                                           readReference.getSize(),
                                           hitStart,
                                           cs.getHitAlnSize(),
                                           cs.getHitString(),
                                           false); 

                        // Check for reverse complement
                        if ((flags & 0x10) == 0x10) {
                            al.setQueryStrand("-");
                        }

                        al.writeMafFile(outputFilename);
                    }
                } else {
                    System.out.println("Error: can't find read length for ["+queryName+"]");
                    System.exit(1);
                }
            } else {
                System.out.println("Error: Couldn't find reference "+hitName);
            }
        }
        
        return al;
    }
    
    public int parseFile(String filename, AlignmentsTableFile nonAlignedSummaryFile, ReadSetStats overallStats) {
        alignments = new ArrayList();
        referenceSizes = new Hashtable();
        leafName = new File(filename).getName();
        
        // Read all alignmnets and put into an ArrayList
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            
            do {
                line = br.readLine();
                if (line != null) {
                    if (line.startsWith("@SQ")) {
                        processReferenceTag(line);
                    } else if (line.startsWith("@PG")) {
                        processProgramTag(line);
                    } else if (!line.startsWith("@")) {
                        Alignment al = processAlignmentLine(line, filename+".last", overallStats);
                        if (al != null) {
                            alignments.add(al);
                        }                         
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
