package nanook;

import java.io.*;

/**
 * Representation of program options and some global constants.
 * 
 * @author Richard Leggett
 */
public class NanoOKOptions {
    public final static int MAX_KMER = 5000;
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
    private String baseDir="/Users/leggettr/Documents/Projects/Nanopore";
    private String referenceFile=null;
    private String sample=null;
    private String scriptsDir="/Users/leggettr/Documents/github/nanotools/scripts";
    private String aligner="last";
    private String alignerExtension=".maf";
    private int coverageBinSize = 100;
    private boolean processPassReads = true;
    private boolean processFailReads = true;
    private boolean parseAlignments = true;
    private boolean plotGraphs = true;
    private boolean makeReport = true;
    private boolean makePDF = true;
    private int maxReads = 0;
    private boolean process2DReads = true;
    private boolean processTemplateReads = true;
    private boolean processComplementReads = true;
    
    public NanoOKOptions() {
        String value = System.getenv("NANOOK_SCRIPT_DIR");
        
        if (value != null) {
            scriptsDir = value;
        } else {
            System.out.println("*** WARNING: You should set NANOOK_SCRIPT_DIR. Default value unlikely to work. ***\n");
        }
        
        value = System.getenv("NANOOK_BASE_DIR");
        if (value != null) {
            baseDir = value;
        }
        
        System.out.println("Scripts dir: "+scriptsDir);
    }
    
    /**
     * Parse command line arguments.
     * @param args array of command line arguments
     */
    public void parseArgs(String[] args) {
        int i=0;
        
        if (args.length <= 1) {
            System.out.println("\nSyntax nanook [options]");
            System.out.println("");
            System.out.println("Main options:");
            System.out.println("    -basesdir <directory> specifies base directory");
            System.out.println("    -reference <path> specifies path to reference database");
            System.out.println("    -sample <name> specifies name of sample");
            System.out.println("Other options:");
            System.out.println("    -aligner <name> specifies the aligner (default last)");            
            System.out.println("    -coveragebin <int> specifies coverage bin size (default 100)");            
            System.out.println("    -nofail to exclude analysis of reads in 'fail' folder");
            System.out.println("    -nopass to exclude analysus of reads in 'pass' folder");            
            System.out.println("");
            System.exit(0);
        }
        
        parseAlignments = true;
        plotGraphs = true;
        makeReport = true;
                        
        while (i < (args.length)) {
            if (args[i].equalsIgnoreCase("-basedir")) {
                baseDir = args[i+1];
                i+=2;
            } else if (args[i].equalsIgnoreCase("-coveragebin")) {
                coverageBinSize = Integer.parseInt(args[i+1]);
                i+=2;
            } else if (args[i].equalsIgnoreCase("-reference")) {
                referenceFile = args[i+1];
                i+=2;
            } else if (args[i].equalsIgnoreCase("-sample")) {
                sample = args[i+1];
                i+=2;
            } else if (args[i].equalsIgnoreCase("-maxreads")) {
                maxReads = Integer.parseInt(args[i+1]);
                i+=2;
            } else if (args[i].equalsIgnoreCase("-nofail")) {
                processFailReads = false;  
                i++;
            } else if (args[i].equalsIgnoreCase("-nopass")) {
                processPassReads = false;
                i++;
            } else if (args[i].equalsIgnoreCase("-aligner")) {
                aligner = args[i+1];
                i+=2;
            } else {                
                System.out.println("Unknown paramter: " + args[i]);
                System.exit(0);
            }            
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
        
        if (aligner.equals("bwa")) {
            alignerExtension = ".sam";
        } else if (aligner.equals("last")) {
            alignerExtension = ".maf";
        } else if (aligner.equals("marginalign")) {
            alignerExtension = ".sam";
        } else if (aligner.equals("blasr")) {
            alignerExtension = ".sam";
        } else {
            System.out.println("Error: aligner not known\n");
            System.exit(1);
        }
    }
        
    public String getAligner() {
        return aligner;
    }
    
    public String getAlignerExtension() {
        return alignerExtension;
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
     * Check if various required directories exist and create if not.
     */
    public void checkDirectoryStructure() {
        File analysisDir = new File(getAnalysisDir());
        File unalignedAnalysisDir = new File(getAnalysisDir()+File.separator+"Unaligned");
        File graphsDir = new File(getGraphsDir());
        File motifsDir = new File(getGraphsDir() + File.separator + "motifs");
        File latexDir = new File(getLatexDir());
        File logsDir = new File(getLogsDir());
        
        if (!analysisDir.exists()) {
            analysisDir.mkdir();
        }
        
        if (!unalignedAnalysisDir.exists()) {
            unalignedAnalysisDir.mkdir();
        }

        if (!graphsDir.exists()) {
            graphsDir.mkdir();
        }    

        if (!motifsDir.exists()) {
            motifsDir.mkdir();
        }    
        
        if (!latexDir.exists()) {
            latexDir.mkdir();
        }    
        
        if (!logsDir.exists()) {
            logsDir.mkdir();
        }
    }
    
    /**
     * Check if an analysis reference directory exists and make if not.
     * @param reference name of reference
     */
    public void checkAndMakeReferenceAnalysisDir(String reference) {
        File analysisDir = new File(getAnalysisDir() + File.separator + reference);
        File graphsDir = new File(getGraphsDir() + File.separator + reference);
        
        if (!analysisDir.exists()) {
            analysisDir.mkdir();
        }
        if (!graphsDir.exists()) {
            graphsDir.mkdir();
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
        return getAnalysisDir() + File.separator + "alignment_summary.txt";
    }

    /**
     * Get filename of length summary file.
     * @return filename String
     */
    public String getLengthSummaryFilename() {
        return getAnalysisDir() + File.separator + "length_summary.txt";
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
        return baseDir + File.separator + sample + File.separator + "graphs";
    } 

    /**
     * Get FASTA directory.
     * @return directory name as String
     */
    public String getFastaDir() {
        return baseDir + File.separator + sample + File.separator + "fasta";
    } 

    /**
     * Get LAST directory.
     * @return directory name as String
     */
    public String getAlignerDir() {
        return baseDir + File.separator + sample + File.separator + aligner;
    } 

    /**
     * Get LaTeX directory.
     * @return directory name as String
     */
    public String getLatexDir() {
        return baseDir + File.separator + sample + File.separator + "latex";
    } 

    /**
     * Get logs directory.
     * @return directory name as String
     */
    public String getLogsDir() {
        return baseDir + File.separator + sample + File.separator + "logs";
    } 
    
    public boolean isNewStyleDir() {
        File passDir = new File(getFastaDir() + File.separator + "pass");
        File failDir = new File(getFastaDir() + File.separator + "pass");
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
        return baseDir + File.separator + sample + File.separator + "analysis";
    } 
    
    /**
     * Get LaTeX filename.
     * @return filename as String
     */
    public String getTexFilename() {
        return baseDir + File.separator + sample + File.separator + "latex" + File.separator + sample + ".tex";
    }
    
    /**
     * Check if processing "pass" reads.
     * @return true to process
     */
    public boolean doProcessPassReads() {
        return processPassReads;
    }

    /**
     * Check if processing "fail" reads.
     * @return true to process
     */
    public boolean doProcessFailReads() {
        return processFailReads;
    }

    /**
     * Check if to parse alignments or not
     * @return true to parse
     */
    public boolean doParseAlignments() {
        return parseAlignments;
    }
    
    /**
     * Check if to plot graphs or not
     * @return true to plot
     */
    public boolean doPlotGraphs() {
        return plotGraphs;
    }
    
    /**
     * Check if to make report or not
     * @return true to make report
     */
    public boolean doMakeReport() {
        return makeReport;
    }

    /**
     * Check if to make report or not
     * @return true to make report
     */
    public boolean doMakePDF() {
        return makePDF;
    }
        
    /**
     * Get maximum number of reads (used for debugging)
     * @return maximum number of reads
     */
    public int getMaxReads() {
        return maxReads;
    }    
}
