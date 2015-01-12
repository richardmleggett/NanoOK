package nanook;

import java.io.*;

/**
 * Representation of program options and some global constants.
 * 
 * @author Richard Leggett
 */
public class NanoOKOptions {
    public final static int MAX_KMER = 1000;
    public final static int MAX_READ_LENGTH = 1000000;
    public final static int MAX_READS = 1000000;
    public final static int TYPE_TEMPLATE = 0;
    public final static int TYPE_COMPLEMENT = 1;
    public final static int TYPE_2D = 2;
    public final static int TYPE_INSERTION = 0;
    public final static int TYPE_DELETION = 1;
    public final static int TYPE_SUBSTITUTION = 2;
    public final static int READTYPE_COMBINED = 0;
    public final static int READTYPE_PASS = 1;
    public final static int READTYPE_FAIL = 2;
    private String program="";
    private String baseDir="/Users/leggettr/Documents/Projects/Nanopore";
    private String referenceFile=null;
    private String sample=null;
    private String scriptsDir="/Users/leggettr/Documents/github/nanotools/scripts";
    private int coverageBinSize = 100;
    private boolean processPassReads = true;
    private boolean processFailReads = true;
    
    /**
     * Parse command line arguments.
     * @param args array of command line arguments
     */
    public void parseArgs(String[] args) {
        int i=0;
        
        if (args.length <= 1) {
            System.out.println("Syntax Nanotools [program] [options]");
            System.out.println("");
            System.out.println("Programs:");            
            System.out.println("    parselast - parse LAST alignments");            
            System.out.println("    plot      - plot graphs");
            System.out.println("");
            System.out.println("Options:");
            System.out.println("    -basesdir <directory> specifies base directory");
            System.out.println("    -coveragebin <int> specifies coverage bin size (default 100)");
            System.out.println("    -reference <path> specifies path to reference database");
            System.out.println("    -sample <name> specifies name of sample");
            System.out.println("");
            System.exit(0);
        }
        
        if (args[i].equalsIgnoreCase("readstats")) {
            program="readstats";
        } else if (args[i].equalsIgnoreCase("parselast")) {
            program="parselast";
        } else if (args[i].equalsIgnoreCase("plot")) {
            program="plot";
        } else {
            System.out.println("Unknown program "+program);
            System.exit(1);
        }
        
        i++;
        
        while (i < (args.length-1)) {
            if (args[i].equalsIgnoreCase("-basedir")) {
                baseDir = args[i+1];
            } else if (args[i].equalsIgnoreCase("-coveragebin")) {
                coverageBinSize = Integer.parseInt(args[i+1]);
            } else if (args[i].equalsIgnoreCase("-reference")) {
                referenceFile = args[i+1];
            } else if (args[i].equalsIgnoreCase("-sample")) {
                sample = args[i+1];
            } else {
                System.out.println("Unknown paramter: " + args[i]);
                System.exit(0);
            }
            
            i+=2;
        }
        
        if (baseDir == null) {
            System.out.println("Error: You must specify a base directory");
            System.exit(1);
        }
        if (referenceFile == null) {
            System.out.println("Error: You must specify a reference");
            System.exit(1);
        }
        if (sample == null) {
            System.out.println("Error: You must specify a sample");
            System.exit(1);
        }
    }
    
    /**
     * Get 'program' name.
     * @return program name String
     */
    public String getProgram() {
        return program;
    }
    
    /**
     * Get base directory name.
     * @return directory name as String
     */
    public String getBaseDirectory() {
        return baseDir;
    }
    
    /**
     * Get sample name.
     * @return name String
     */
    public String getSample() {
        return sample;
    }
    
    /**
     * Get name of references file.
     * @return filename String
     */
    public String getReferenceFile() {
        return referenceFile;
    }
    
    /**
     * Get coverage graph bin size.
     * @return bin size
     */
    public int getCoverageBinSize() {
        return coverageBinSize;
    }
        
    /**
     * Get a type string (Template, Complement, 2D) from an integer.
     * @param n integer to convert
     * @return type String
     */
    public static String getTypeFromInt(int n) {
        String typeString;
        
        switch(n) {
            case TYPE_TEMPLATE: typeString = "Template"; break;
            case TYPE_COMPLEMENT: typeString = "Complement"; break;
            case TYPE_2D: typeString = "2D"; break;
            default: typeString = "Unknown"; break;
        }
        
        return typeString;
    }

    /**
     * Get an error type string (Insertion, Deletion, Substitution) from an integer.
     * @param n error type integer
     * @return type String
     */
    public static String getErrorTypeFromInt(int n) {
        String typeString;
        
        switch(n) {
            case TYPE_INSERTION: typeString = "Insertion"; break;
            case TYPE_DELETION: typeString = "Deletion"; break;
            case TYPE_SUBSTITUTION: typeString = "Substitution"; break;
            default: typeString = "Unknown"; break;
        }
        
        return typeString;
    }
    
    /**
     * Get the filename separator string for this platform.
     * @return separator string.
     */
    public String getSeparator() {
        return "/";   
    }
    
    /**
     * Check if various required directories exist and create if not.
     */
    public void checkDirectoryStructure() {
        File analysisDir = new File(baseDir + getSeparator() + sample + getSeparator() + "analysis");
        File graphsDir = new File(baseDir + getSeparator() + sample + getSeparator() + "graphs");
        File latexDir = new File(baseDir + getSeparator() + sample + getSeparator() + "latex");
        
        if (!analysisDir.exists()) {
            analysisDir.mkdir();
        }

        if (!graphsDir.exists()) {
            graphsDir.mkdir();
        }    

        if (!latexDir.exists()) {
            latexDir.mkdir();
        }    
    }
    
    /**
     * Create a new alignment summary file.
     */
    public void initialiseAlignmentSummaryFile() {
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(this.getAlignmentSummaryFilename())); 
            pw.close();
        } catch (IOException e) {
            System.out.println("initialiseAlignmentSummaryFile exception:");
            e.printStackTrace();
            System.exit(1);
        }        
    }
    
    /**
     * Get filename of alignment summary file.
     * @return filename String
     */
    public String getAlignmentSummaryFilename() {
        return baseDir + getSeparator() + sample + getSeparator() + "analysis" + getSeparator() + "alignment_summary.txt";
    }

    /**
     * Get filename of length summary file.
     * @return filename String
     */
    public String getLengthSummaryFilename() {
        return baseDir + getSeparator() + sample + getSeparator() + "analysis" + getSeparator() + "length_summary.txt";
    }    
    
    /**
     * Get scripts directory.
     * @return directory name as String
     */
    public String getScriptsDir() {
        return scriptsDir;
    }
    
    /**
     * Get graphs directory.
     * @return directory name as String
     */
    public String getGraphsDir() {
        return baseDir + getSeparator() + sample + getSeparator() + "graphs";
    } 

    /**
     * Get FASTA directory.
     * @return directory name as String
     */
    public String getFastaDir() {
        return baseDir + getSeparator() + sample + getSeparator() + "fasta";
    } 

    /**
     * Get LAST directory.
     * @return directory name as String
     */
    public String getLastDir() {
        return baseDir + getSeparator() + sample + getSeparator() + "last";
    } 
    
    public boolean isNewStyleDir() {
        File passDir = new File(getFastaDir() + getSeparator() + "pass");
        File failDir = new File(getFastaDir() + getSeparator() + "pass");
        boolean rc = false;
        
        if (passDir.exists() && passDir.isDirectory() && failDir.exists() && failDir.isDirectory()) {
            rc = true;
        }
        
        return rc;
    }
    
    /**
     * Get analysis directory.
     * @return directory name as String
     */
    public String getAnalysisDir() {
        return baseDir + getSeparator() + sample + getSeparator() + "analysis";
    } 
    
    /**
     * Get LaTeX filename.
     * @return filename as String
     */
    public String getTexFilename() {
        return baseDir + getSeparator() + sample + getSeparator() + "latex" + getSeparator() + sample + ".tex";
    }
    
    /**
     * Check if processing "pass" reads.
     * @return true to process
     */
    public boolean processPassReads() {
        return processPassReads;
    }

    /**
     * Check if processing "fail" reads.
     * @return true to process
     */
    public boolean processFailReads() {
        return processFailReads;
    }
}
