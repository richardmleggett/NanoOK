/*
 * Program: NanoOK
 * Author:  Richard M. Leggett
 * 
 * Copyright 2015 The Genome Analysis Centre (TGAC)
 */

package nanook;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Read FASTA files
 * 
 * @author Richard Leggett
 */
public class SequenceReader {
    private ArrayList<String> seqIDs = new ArrayList();
    private ArrayList<Integer> seqLengths = new ArrayList();
    private ArrayList<String> sequence = new ArrayList();
    private ArrayList<Double> gcPc = new ArrayList();
    private int nSeqs = 0;
    private boolean cacheSequence = false;
    private String currentFilename;
    
    public SequenceReader(boolean cache) {
        cacheSequence = cache;
    }
    
    public int countGC(String s) {
        int g = s.length() - s.replace("G", "").length();
        int c = s.length() - s.replace("C", "").length();
        
        return g + c;
    }
    
    public int indexFASTQFile(String filename) {
        currentFilename = filename;
        
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(filename));
            String line;
            String id = null;
            int contigLength = 0;
            int readsInThisFile = 0;
            boolean gotRead;
            int gc = 0;
                    
            do {
                String sh = br.readLine();
                String s = br.readLine();
                String qh = br.readLine();
                String q = br.readLine();
                gotRead = false;
                if ((sh != null) && (s != null) && (qh != null) & (q != null)) {
                    if (sh.startsWith("@")) {
                        if (qh.startsWith("+")) {
                            String sequenceHeader = sh.trim();
                            String seq = s.trim();
                            String[] parts = sequenceHeader.substring(1).split("(\\s+)");
                            id = parts[0];
                            
                            if (id != null) {
                                seqIDs.add(id);
                                seqLengths.add(seq.length());
                                gcPc.add(new Double(100.0 * (double)countGC(seq) / (double)seq.length()));
                                if (cacheSequence) {
                                    sequence.add(seq);
                                }
                                nSeqs++;                        
                                gotRead = true;
                            }

                        }
                    }
                }
            } while (gotRead);

            br.close();
        } catch (Exception e) {
            System.out.println("readFasta Exception:");
            e.printStackTrace();
            System.exit(1);
        }
                                 
        return nSeqs;
    }
    
    private String makeName(String line, String id) {
        String name = id;
        Pattern p = Pattern.compile(">gi\\|(\\S+)\\|(\\S+)\\|(\\S+)\\| (\\S+) (\\S+)");
        Matcher m = p.matcher(line);
        
        if (m.find()) {
            name = m.group(4) + "_" + m.group(5);
        }
        
        name=name.replaceAll("\\.", "_");
        name=name.replaceAll(" ", "_");
        name=name.replaceAll("\\|", "_");
        
        return name;
    }
    
    /**
     * Parse a FASTA file
     * @param filename filename of FASTA file
     */
    public int indexFASTAFile(String filename, String indexFilename, boolean storeIds) {
        currentFilename = filename;
                
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(filename)); 
            PrintWriter pw = null;
            String line;
            String id = null;
            String name = null;
            int contigLength = 0;
            int readsInThisFile = 0;
            //String seq = "";
            StringBuilder seq = new StringBuilder(100000);
            int gc = 0;
            
            if (indexFilename != null) {
                pw = new PrintWriter(new FileWriter(indexFilename, false)); 
            }
                        
            do {
                line = br.readLine();
                if (line != null) {
                    line = line.trim();
                }

                if ((line == null) || (line.startsWith(">"))) {                    
                    if (id != null) {
                        if (storeIds) {
                            seqIDs.add(id);
                            seqLengths.add(contigLength);
                            gcPc.add(new Double(100.0*(double)gc / (double)contigLength));
                        }

                        if (pw != null) {
                            pw.printf("%s\t%d\t%s", id, contigLength, name);
                            pw.println("");
                        }
                        
                        if (cacheSequence) {
                            sequence.add(seq.toString());
                        }
                        nSeqs++;  
                    }
                    
                    if (line != null) {
                        String[] parts = line.substring(1).split("(\\s+)");
                        id = parts[0];
                        name = makeName(line, id);
                    }                   
                    
                    contigLength = 0;
                    gc = 0;
                } else if (line != null) {
                    contigLength += line.length();
                    gc += countGC(line);
                    
                    if (cacheSequence) {
                        seq.append(line);
                        //seq = seq + line;
                    }
                }  
            } while (line != null);
            
            br.close();
            if (pw != null) {
                pw.close();
            }                
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
    
    public double getGC(int i) {
        return gcPc.get(i);
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
        
    public void storeKmers(int index, KmerTable t) {
        String seq = sequence.get(index);
        if (seq != null) {
            int k = t.getKmerSize();

            for (int o=0; o<seq.length() - k; o++) {
                t.countKmer(seq.substring(o, o+5));
            }        
        } else {
            System.out.println("Need to handle the non-cached case");
        }
    }
    
}
