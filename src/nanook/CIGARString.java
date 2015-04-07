/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nanook;

/**
 * Represent and parse a CIGAR string
 * @author leggettr
 */
public class CIGARString {
    private StringBuilder queryString = new StringBuilder("");
    private StringBuilder hitString = new StringBuilder("");
    private String hitFilename;
    private String cigarString;
    private String querySeq;
    private String queryFilename;
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
    public CIGARString(String cs, String qseq, String qf, int hs, String hf, ReferenceSequence hr) {
        cigarString = cs;
        querySeq = qseq;
        queryFilename = qf;
        hitStart = hs;
        hitFilename = hf;
        hitReference = hr;
    }
    
    public void processString() {
        String value = "";        
        SequenceReader r = new SequenceReader(true);
        r.indexFASTAFile(hitFilename);
        String hitSeq = r.getSubSequence(hitReference.getId(), hitStart, hitStart+(3*querySeq.length()/2));
        int hitPtr = 0;
        int queryPtr = 0;
        boolean displayResult = false;
        boolean donePreClipping = false;
        
        //System.out.println("  Hit: "+hitSeq.length()+" "+hitSeq);
        //System.out.println("Query: "+querySeq.length()+" "+querySeq);

        hitAlnSize = 0;
        queryAlnSize = 0;
        hitAlnSize = 0;
        for (int i=0; i<cigarString.length(); i++) {
            //System.out.println("hitPtr="+hitPtr+" queryPtr="+queryPtr);
            //System.out.println("Query: " + queryString.toString());
            //System.out.println("  Hit: " + hitString.toString());
            char c = cigarString.charAt(i);
            if (Character.isDigit(c)) {
                value = value + c;
            } else {
                int n = Integer.parseInt(value);
                //System.out.println(n + " " + c);
                switch(c) {
                    case 'M':
                    case '=':
                    case 'X':
                        queryString.append(querySeq.substring(queryPtr, queryPtr + n));
                        hitString.append(hitSeq.substring(hitPtr, hitPtr + n));
                        queryPtr += n;
                        hitPtr += n;
                        queryAlnSize += n;
                        hitAlnSize += n;
                        donePreClipping = true;
                        break;
                    case 'I':
                        queryString.append(querySeq.substring(queryPtr, queryPtr + n));
                        for (int j=0; j<n; j++) {
                            hitString.append('-'); 
                        }
                        queryPtr += n;
                        queryAlnSize += n;
                        donePreClipping = true;
                        break;
                    case 'D':
                        hitString.append(hitSeq.substring(hitPtr, hitPtr + n));
                        for (int j=0; j<n; j++) {
                            queryString.append('-'); 
                        }
                        hitPtr += n;
                        hitAlnSize += n;
                        donePreClipping = true;
                        break;
                    case 'N':
                        System.out.println("Warning: encountered N in CIGAR format!\n");
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
                        //System.out.println("Warnning: encountered S in CIGAR format!\n");
                        queryPtr += n;
                        if (!donePreClipping) {
                            queryStart += n;
                        }
                        displayResult = true;                        
                        break;
                    case 'H':
                        //System.out.println("Warning: encountered H in CIGAR format!\n");
                        if (!donePreClipping) {
                            queryStart += n;
                        } else {
                            //System.out.println("Warning: hard clipping at end\n");
                        }
                        displayResult = true;
                        break;
                    case 'P':
                        System.out.println("Warning: encountered P in CIGAR format!\n");
                        displayResult = true;
                        donePreClipping = true;
                        break;
                    default:
                        System.out.println("Unrecognised character in CIGAR string: "+c);
                        break;
                }
                value="";
            }
        }
        
        //if (displayResult) {
            //System.out.println(queryFilename);
            //System.out.println("Query: " + queryString.toString());
            //System.out.println("  Hit: " + hitString.toString());
            //System.exit(1);
        //}        
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
