/*
 * Program: NanoOK
 * Author:  Richard M. Leggett
 * 
 * Copyright 2015 The Genome Analysis Centre (TGAC)
 */

package nanook;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Represent and parse a CIGAR string
 * 
 * @author Richard Leggett
 */
public class CIGARString {
    private StringBuilder queryString = new StringBuilder("");
    private StringBuilder hitString = new StringBuilder("");
    private String hitFilename;
    private String cigarString;
    private String querySeq;
    private String queryFilename;
    private String queryID;
    private ReferenceSequence hitReference;
    private int queryStart;
    private int hitStart;
    private int queryAlnSize;
    private int hitAlnSize;
    
    /**
     * Constructor
     * @param cs
     * @param qseq
     * @param qf
     * @param hs hit start position (0-based)
     * @param hf
     * @param hr 
     */
    public CIGARString(String cs, String qseq, String qf, String qi, int hs, String hf, ReferenceSequence hr) {
        cigarString = cs;
        querySeq = qseq;
        queryFilename = qf;
        queryID = qi;
        hitStart = hs;
        hitFilename = hf;
        hitReference = hr;
        queryStart = 0;
        
        //trimCIGAR(cs, qseq);
    }
    
    /**
     * Attempt at handling marginAlign CIGAR strings
     * Needs work!
     * @param cs
     * @param qseq
     * @return 
     */
    private String trimCIGAR(String cs, String qseq) {
        //System.out.println("Old cigar: "+cs);

        boolean foundStart = false;
        int trimQueryStart = 0;
        int trimCigarStart = 0;
        int trimCigarEnd = 0;
        int trimQueryEnd = 0;        
        Pattern outPattern = Pattern.compile("(\\d+)\\S");
        Matcher outMatcher = outPattern.matcher(cs);
        ArrayList<String> tags = new ArrayList();
        while (outMatcher.find()) {
            tags.add(outMatcher.group(0));
        }

        
        for (int i=0; i<tags.size(); i++) {
            String tag = tags.get(i);
            int n = Integer.parseInt(tag.substring(0, tag.length()-1));
            String c = tag.substring(tag.length()-1);

            if (c.equals("I")) { 
                queryStart += n;
                trimQueryStart += n;
                trimCigarStart += tag.length();
            } else if (c.equals("D")) {
                hitStart += n;
                trimCigarStart += tag.length();
            } else {
                break;
            }
        }        

        for (int i=tags.size()-1; i>0; i--) {
            String tag = tags.get(i);
            int n = Integer.parseInt(tag.substring(0, tag.length()-1));
            String c = tag.substring(tag.length()-1);

            if (c.equals("I")) { 
                trimQueryEnd += n;
                trimCigarEnd += tag.length();
            } else if (c.equals("D")) {
                trimCigarEnd += tag.length();
            } else {
                break;
            }
        }        
        
        cigarString = cs.substring(trimCigarStart, cs.length()-trimCigarEnd);
        querySeq = qseq.substring(trimQueryStart, qseq.length()-trimQueryEnd);
        
        //System.out.println("New cigar: "+cigarString);
        return cigarString;
    }
    
    public boolean processString() {
        String value = "";        
        SequenceReader r = new SequenceReader(true);
        r.indexFASTAFile(hitFilename, null, true);
        int l = 3*querySeq.length();
        String hitSeq = r.getSubSequence(hitReference.getId(), hitStart, hitStart+l);
        int hitPtr = 0;
        int queryPtr = 0;
        boolean displayResult = false;
        boolean donePreClipping = false;
        int tagCtr = 0;
        int i = 0;
        boolean continueParsing = true;
        int totalCount = 0;
        int delCount = 0;
        int insCount = 0;
        int matchCount = 0;
        boolean processed = true;
        
        //System.out.println("Query filename: "+queryFilename);
        //System.out.println("CIGAR: "+cigarString);
        //System.out.println("  Hit: "+hitSeq.length()+" "+hitSeq);
        //System.out.println("Query: "+querySeq.length()+" "+querySeq);

        hitAlnSize = 0;
        queryAlnSize = 0;
        hitAlnSize = 0;
        while ((i<cigarString.length()) && (continueParsing)) {
        //for (int i=0; i<cigarString.length(); i++) {
            //System.out.println("hitPtr="+hitPtr+" queryPtr="+queryPtr);
            //System.out.println("Query: " + queryString.toString());
            //System.out.println("  Hit: " + hitString.toString());
            char c = cigarString.charAt(i);
            
            if (Character.isDigit(c)) {
                value = value + c;
            } else {
                int n = Integer.parseInt(value);
                totalCount += n;
                //System.out.println(n + " " + c);
                switch(c) {
                    case 'M':
                    case '=':
                    case 'X':
                        //System.out.println(hitString.length() + " " + hitPtr);
                        //System.out.println("Hit up: " + hitSeq.substring(hitPtr));
                        queryString.append(querySeq.substring(queryPtr, queryPtr + n));
                        hitString.append(hitSeq.substring(hitPtr, hitPtr + n));
                        queryPtr += n;
                        hitPtr += n;
                        queryAlnSize += n;
                        hitAlnSize += n;
                        donePreClipping = true;
                        matchCount+=n;
                        break;
                    case 'I':
                        if (n > 100) {
                            // DEBUG MODE TURNS OFF THIS
                            System.out.println("");
                            System.out.println("Error: large I ("+n+") - read "+queryID+" ignored");
                            processed = false;
                            continueParsing = false;
                        } else {
                            queryString.append(querySeq.substring(queryPtr, queryPtr + n));
                            for (int j=0; j<n; j++) {
                                hitString.append('-'); 
                            }
                            queryPtr += n;
                            queryAlnSize += n;
                        }
                        donePreClipping = true;
                        insCount+=n;
                        break;
                    case 'D':
                        if (n > 100) {
                            System.out.println("Error: large D ("+n+") - read "+queryID+" ignored");
                            processed = false;
                            continueParsing = false;
                        } else {
                            hitString.append(hitSeq.substring(hitPtr, hitPtr + n));
                            for (int j=0; j<n; j++) {
                                queryString.append('-'); 
                            }
                            hitPtr += n;
                            hitAlnSize += n;
                        }
                        donePreClipping = true;
                        delCount+=n;
                        break;
                    case 'N':
                        System.out.println("Warning: encountered N in CIGAR format!");
                        System.out.println("");
                        displayResult = true;
                        hitString.append(hitSeq.substring(hitPtr, hitPtr + n));
                        for (int j=0; j<n; j++) {
                            queryString.append('-'); 
                        }
                        queryPtr += n;
                        hitPtr += n;
                        donePreClipping = true;
                        break;
                    case 'S':
                        //System.out.println("Warnning: encountered S in CIGAR format!");
                        queryPtr += n;
                        if (!donePreClipping) {
                            queryStart += n;
                        }
                        displayResult = true;                        
                        break;
                    case 'H':
                        //System.out.println("Warning: encountered H in CIGAR format!");
                        if (!donePreClipping) {
                            queryStart += n;
                        } else {
                            //System.out.println("Warning: hard clipping at end");
                        }
                        displayResult = true;
                        break;
                    case 'P':
                        System.out.println("Warning: encountered P in CIGAR format!");
                        System.out.println("");
                        displayResult = true;
                        donePreClipping = true;
                        break;
                    default:
                        System.out.println("Unrecognised character in CIGAR string: "+c);
                        processed = false;
                        break;
                }
                value="";
                tagCtr++;
                //System.out.println("qseq="+querySeq.length()+" matchCount="+matchCount+" insCount="+insCount+" delCount="+delCount+" totalCount="+totalCount);
                //System.out.println("Query: "+queryString.toString());
                //System.out.println("  Hit: "+hitString.toString());
            }
            
            i++;
            //System.out.println("i="+i+" and length="+cigarString.length());
        }
        
        //if (displayResult) {
            //System.out.println(queryFilename);
            //System.out.println("Query: " + queryString.toString());
            //System.out.println("  Hit: " + hitString.toString());
            //System.exit(1);
        //}        
        return processed;
    }
    
    public int getQueryStart() {
        return queryStart;
    }
    
    public int getQueryAlnSize() {
        return queryAlnSize;
    }

    public int getHitAlnSize() {
        return hitAlnSize;
    }
    
    public String getQueryString() {
        return queryString.toString();
    }

    public String getHitString() {
        return hitString.toString();
    }
}
