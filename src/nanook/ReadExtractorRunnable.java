/*
 * Program: NanoOK
 * Author:  Richard M. Leggett
 * 
 * Copyright 2015 The Genome Analysis Centre (TGAC)
 */

package nanook;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import ncsa.hdf.hdf5lib.H5;
import ncsa.hdf.hdf5lib.structs.H5O_info_t;
import ncsa.hdf.object.*;
import ncsa.hdf.object.h5.*;

/**
 * Enable multi-threading of read extraction
 * 
 * @author Richard Leggett
 */
public class ReadExtractorRunnable implements Runnable {
    public final static String TYPE_STRING_TEMPLATE = "/Analyses/Basecall_2D_000/BaseCalled_template/Fastq";
    public final static String TYPE_STRING_COMPLEMENT = "/Analyses/Basecall_2D_000/BaseCalled_complement/Fastq";
    public final static String TYPE_STRING_2D = "/Analyses/Basecall_2D_000/BaseCalled_2D/Fastq";
    private String[] typeStrings = {TYPE_STRING_TEMPLATE, TYPE_STRING_COMPLEMENT, TYPE_STRING_2D};
    public NanoOKOptions options;
    public String inDir;
    public String filename;
    public String outDir;
    
    public ReadExtractorRunnable(NanoOKOptions o, String in, String file, String out) {
        options = o;
        inDir = in;
        filename = file;
        outDir = out;
    }

    /**
     * Check which types - Template, Complement, 2D - are available for this read
     * @param filename filename of fast5 file
     * @return boolean area of flags for Template, Complement, 2D
     */
    private boolean[] checkTypesAvailable(String filename) {
        boolean[] typesAvailable = new boolean[3];
        ProcessLogger pl = new ProcessLogger();
        ArrayList<String> response;
        response = pl.getCommandOutput("h5dump -n "+filename, true, true);
        for (int i=0; i<response.size(); i++) {
            String s = response.get(i);
            if (s.contains(TYPE_STRING_TEMPLATE)) {
                typesAvailable[NanoOKOptions.TYPE_TEMPLATE] = true;
                //System.out.println("Got template");
            } else if (s.contains(TYPE_STRING_COMPLEMENT)) {
                typesAvailable[NanoOKOptions.TYPE_COMPLEMENT] = true;
                //System.out.println("Got complement");
            } else if (s.contains(TYPE_STRING_2D)) {
                typesAvailable[NanoOKOptions.TYPE_2D] = true;
                //System.out.println("Got 2D");
            }
        }
                
        return typesAvailable;
    }    
    
    /**
     * Write a FASTA file
     * @param id
     * @param seq
     * @param filename 
     */
    private void writeFastaFile(String id, String seq, String filename) {
        PrintWriter pw;
        //System.out.println("Writing " + filename);
        
        try {
            pw = new PrintWriter(new FileWriter(filename));
            pw.print(">");
            pw.println(id);
            pw.println(seq);
            pw.close();            
        } catch (IOException e) {
            System.out.println("writeFastaFile exception");
            e.printStackTrace();
        }
    }
    
    private void writeFastqFile(String id, String seq, String qual, String filename) {
        PrintWriter pw;
        //System.out.println("Writing " + filename);
        
        try {
            pw = new PrintWriter(new FileWriter(filename));
            pw.print("@");
            pw.println(id);
            pw.println(seq);
            pw.println("+");
            pw.println(qual);
            pw.close();            
        } catch (IOException e) {
            System.out.println("writeFastaFile exception");
            e.printStackTrace();
        }
    }
    
    /**
     * Get FASTQ section out of FAST5 file
     * @param pathname path to FAST5 file
     * @param type type of read
     * @return multi-line String
     */
    public String getFastq(String pathname, int type) {
        H5File file = null;
        String[] fastq = null;
        
		// Open a file using default properties.
		try {
            file = new H5File(pathname, FileFormat.READ);
            
            // Find basecall group
            H5Group grp;
            String groupPath = new String();
            String datasetPath = null;
            String indexString;
            int index = -1;
            int i = 0;

            // Default behaviour is to find latest
            if (options.getBasecallIndex() == -1) {
                do {
                    indexString = String.format("%03d", i);                
                    grp = (H5Group)file.get("/Analyses/Basecall_2D_" + indexString);
                    if (grp != null) {
                        index=i;
                        i++;
                    }
                } while (grp != null);
            } else {
                // User has specified index - check it exists
                indexString = String.format("%03d", options.getBasecallIndex());    
                grp = (H5Group)file.get("/Analyses/Basecall_2D_" + indexString);
                if (grp != null) {
                    index=i;
                }                
            }
            
            // index will = -1 if we didn't find any group
            if (index >=0) {
                // Make string for group
                indexString = String.format("%03d", index);                

                // Build path to dataset
                if (type == NanoOKOptions.TYPE_2D) {
                    datasetPath = "/Analyses/Basecall_2D_"+indexString+"/BaseCalled_2D/Fastq";
                } else { 
                    // Now look if we are new format (with Basecall_1D_XXX)
                    grp = (H5Group)file.get("/Analyses/Basecall_1D_"+indexString);
                    if (grp == null) {
                        // Old format
                        if (type == NanoOKOptions.TYPE_TEMPLATE) {
                            datasetPath = "/Analyses/Basecall_2D_"+indexString+"/BaseCalled_template/Fastq";
                        } else if (type == NanoOKOptions.TYPE_COMPLEMENT) {
                            datasetPath = "/Analyses/Basecall_2D_"+indexString+"/BaseCalled_complement/Fastq";
                        } else {
                            System.out.println("Error: bad type in getFastq");
                            System.exit(1);
                        }
                    } else {
                        // New format
                        if (type == NanoOKOptions.TYPE_TEMPLATE) {
                            datasetPath = "/Analyses/Basecall_1D_"+indexString+"/BaseCalled_template/Fastq";
                        } else if (type == NanoOKOptions.TYPE_COMPLEMENT) {
                            datasetPath = "/Analyses/Basecall_1D_"+indexString+"/BaseCalled_complement/Fastq";
                        } else {
                            System.out.println("Error: bad type in getFastq");
                            System.exit(1);
                        }
                    }
                }

                //System.out.println("Path: "+datasetPath);            
                Dataset ds = (Dataset)file.get(datasetPath);
                if (ds == null) {
                    System.out.println("No dataset at "+datasetPath);
                } else {
                    fastq = (String[])ds.getData();
                }
            }

            file.close();            
		} catch (Exception e) {
			e.printStackTrace();
		}
        
        if (fastq == null) {
            return null;
        } else {
            return fastq[0];
        }
    }    
    
    /**
     * Dump an individual read
     * @param inputFilename filename of FAST5 file
     * @param type type of read
     */
    private void dumpReadPrevious(String inputFilename, int type, String outputDir) {
        ProcessLogger pl = new ProcessLogger();
        ArrayList<String> response = pl.getCommandOutput("h5dump -d "+typeStrings[type]+" "+inputFilename, true, true);
        String outName = new File(inputFilename).getName();
                
        // Look for start of FASTQ section
        int l;
        for (l=0; l<response.size(); l++) {
            if (response.get(l).contains("\"@")) {
                break;
            }
        }
        
        // Parse FASTQ portion with regex
        if (l < response.size()) {
            String id = null;
            String seq = null;
            String qual = null;
            
            // Header row
            Pattern outPattern = Pattern.compile("@(.+)");
            Matcher outMatcher = outPattern.matcher(response.get(l));
            if (outMatcher.find()) {
                id = outMatcher.group(1);
            }
            
            // Sequence
            outPattern = Pattern.compile("(\\s*)(\\S+)");
            outMatcher = outPattern.matcher(response.get(l+1));
            if (outMatcher.find()) {
                seq = outMatcher.group(2);
            }
            
            // Qualities
            outPattern = Pattern.compile("(\\s*)(\\S+)");
            outMatcher = outPattern.matcher(response.get(l+3));
            if (outMatcher.find()) {
                qual = outMatcher.group(2);
            }
            
            // Fix IDs
            outPattern = Pattern.compile("00000000-0000-0000-0000-000000000000(.+)");
            outMatcher = outPattern.matcher(id);
            if (outMatcher.find()) {
                if (options.fixIDs()) {
                    id = id.replaceAll("^00000000-0000-0000-0000-000000000000_", "");
                    id = id.replaceAll(" ", "");
                } else {
                    System.out.println("Warning: " + id + " is non-unqiue. Recommend re-running with -fixids option.");
                    System.out.println("");
                }
            }
            
            if (id != null) {
                if (options.getReadFormat() == NanoOKOptions.FASTA) {
                    writeFastaFile(id, seq, outputDir + File.separator + NanoOKOptions.getTypeFromInt(type) + File.separator + outName + "_BaseCalled_" + NanoOKOptions.getTypeFromInt(type) + ".fasta");
                } else if (options.getReadFormat() == NanoOKOptions.FASTQ) {
                    writeFastqFile(id, seq, qual, outputDir + File.separator + NanoOKOptions.getTypeFromInt(type) + File.separator + outName + "_BaseCalled_" + NanoOKOptions.getTypeFromInt(type) + ".fastq");
                }
            }
            
        } else {
            System.out.println("Error: couldn't find payload in " + inputFilename);
        }
    }  

    /**
     * Dump an individual read
     * @param inputFilename filename of FAST5 file
     * @param type type of read
     */
    private void dumpRead(String inputFilename, int type, String outputDir) {
        String outName = new File(inputFilename).getName();

        String fastqDatafield = getFastq(inputFilename, type);
        if (fastqDatafield != null) {
            String [] lines = fastqDatafield.split("\n");
        
            String id = null;
            String seq = lines[1];
            String qual = lines[3];
            
            if (lines[0].startsWith("@")) {
                id = lines[0].substring(1);

                // Fix IDs
                Pattern outPattern = Pattern.compile("00000000-0000-0000-0000-000000000000(.+)");
                Matcher outMatcher = outPattern.matcher(id);
                if (outMatcher.find()) {
                    if (options.fixIDs()) {
                        id = id.replaceAll("^00000000-0000-0000-0000-000000000000_", "");
                        id = id.replaceAll(" ", "");
                    } else {
                        System.out.println("Warning: " + id + " is non-unqiue. Recommend re-running with -fixids option.");
                        System.out.println("");
                    }
                }
            } else {
                System.out.println("Couldn't parse "+inputFilename);
            }                      
            
            if (id != null) {
                if (options.getReadFormat() == NanoOKOptions.FASTA) {
                    writeFastaFile(id, seq, outputDir + File.separator + NanoOKOptions.getTypeFromInt(type) + File.separator + outName + "_BaseCalled_" + NanoOKOptions.getTypeFromInt(type) + ".fasta");
                } else if (options.getReadFormat() == NanoOKOptions.FASTQ) {
                    writeFastqFile(id, seq, qual, outputDir + File.separator + NanoOKOptions.getTypeFromInt(type) + File.separator + outName + "_BaseCalled_" + NanoOKOptions.getTypeFromInt(type) + ".fastq");
                }
            }            
        } else {
            System.out.println("Error: couldn't find payload in " + inputFilename);
        }
    }  
    
    
    /**
     * Extract reads of each type from file
     * @param inDir input directory
     * @param filename filename
     * @param outDir output directory
     */
    public void run() {
        String inputPathname = inDir + File.separator + filename;
        
        //System.out.println("Extracting " + inputPathname);

        boolean[] typesAvailable = checkTypesAvailable(inputPathname);        
        
        for (int t=0; t<3; t++) {
            if (typesAvailable[t]) {
                dumpRead(inputPathname, t, outDir);
            }
        }
    }    
}
