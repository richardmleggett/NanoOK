package nanook;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

/**
 * Read FASTA files
 * 
 * @author Richard Leggett
 */
public class SequenceReader {
    private ArrayList<String> seqIDs = new ArrayList();
    private ArrayList<Integer> seqLengths = new ArrayList();
    private ArrayList<String> sequence = new ArrayList();
    private int nSeqs = 0;
    private boolean cacheSequence = false;
    private String currentFilename;
    
    public SequenceReader(boolean cache) {
        cacheSequence = cache;
    }
    
    /**
     * Parse a FASTA file
     * @param filename filename of FASTA file
     */
    public int indexFASTAFile(String filename) {
        currentFilename = filename;
        
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            String id = null;
            int contigLength = 0;
            int readsInThisFile = 0;
            String seq = "";
                        
            do {
                line = br.readLine();
                if (line != null) {
                    line = line.trim();
                }

                if ((line == null) || (line.startsWith(">"))) {                    
                    if (id != null) {
                        seqIDs.add(id);
                        seqLengths.add(contigLength);
                        sequence.add(seq);
                        nSeqs++;                        
                    }
                    
                    if (line != null) {
                        String[] parts = line.substring(1).split("(\\s+)");
                        id = parts[0];
                    }                   
                    
                    contigLength = 0;
                } else if (line != null) {
                    contigLength += line.length();
                    
                    if (cacheSequence) {
                        seq = seq + line;
                    }
                }                
            } while (line != null);

            br.close();
        } catch (Exception e) {
            System.out.println("readFasta Exception:");
            e.printStackTrace();
            System.exit(1);
        }
        
        return nSeqs;
    }
    
    public int getSequenceCount() {
        return nSeqs;
    }
    
    public String getID(int i) {
        return seqIDs.get(i);
    }
    
    public int getLength(int i) {
        return seqLengths.get(i);
    }
    
    public String getSubSequence(String id, int start, int end) {
        int index = -1;
        String seq = "";
        
        for (int i=0; i<nSeqs; i++) {
            if (seqIDs.get(i).equals(id)) {
                index = i;
                break;
            }
        }
        
        if (index == -1) {
            System.out.println("Error: can't find ID " + id);
            System.exit(1);
        }        
        
        if (cacheSequence) {
            if (start < 0) {
                System.out.println("Warning: invalid index ("+start+") in SequenceReader");
                start = 0;
            }
            if (end >= sequence.get(index).length()) {
                //System.out.println("Warning: invalid index ("+end+") in SequenceReader");
                end = sequence.get(index).length() - 1;
            }
            seq = sequence.get(index).substring(start, end+1);
        } else {
            try
            {
                BufferedReader br = new BufferedReader(new FileReader(currentFilename));
                StringBuilder ssb = new StringBuilder("");
                String line;
                boolean foundId = false;
                int position = 0;
                
                do {
                    line = br.readLine();
                    if (line != null) {
                        line = line.trim();
                    }


                    if (line != null) {
                        if (line.startsWith(">")) {
                            if (foundId) {
                                // If we've found the ID we were after, then this new one means we can stop
                                break;
                            } else {
                                String[] parts = line.substring(1).split("(\\s+)");
                                String thisid = parts[0];
                                
                                // Check for ID we're after
                                if (thisid.equals(id)) {
                                    foundId = true;
                                }
                            }
                        } else {
                            if (foundId) {
                                int fStart = position;
                                int fEnd = position + line.length() - 1;
                                
                                //System.out.println("fStart = "+fStart+" fEnd = "+fEnd);
                                
                                if (fEnd >= start) {
                                    int cutStart = (fStart >= start) ? 0:start-position;
                                    int cutEnd = (fEnd <= end) ? (line.length() - 1):end-position;
                                    
                                    //System.out.println(cutStart + " " +cutEnd+"["+line+"]");
                                    ssb.append(line.substring(cutStart, cutEnd+1));
                                    
                                    // Got all we wanted?
                                    if (fEnd >= end) {
                                        break;
                                    }
                                }
                                // Keep track of position
                                position = position + line.length();
                            }
                        }
                    }
                } while (line != null);

                br.close();
                
                seq = ssb.toString();
            } catch (Exception e) {
                System.out.println("readFasta Exception:");
                e.printStackTrace();
                System.exit(1);
            }
        }
        
        return seq;
    }
}
