/*
 * Program: NanoOK
 * Author:  Richard M. Leggett
 * 
 * Copyright 2015 The Genome Analysis Centre (TGAC)
 */

package nanook;

import java.io.*;

/**
 * Representation of program options and some global constants.
 * 
 * @author Richard Leggett
 */
public class NanoOKOptions implements Serializable {
    private static final long serialVersionUID = NanoOK.SERIAL_VERSION;
    public final static int MAX_KMER = 5000;
    public final static int MAX_READ_LENGTH = 1000000;
    public final static int MAX_READS = 1000000;
    public final static int MODE_EXTRACT = 1;
    public final static int MODE_ALIGN = 2;
    public final static int MODE_ANALYSE = 3;
    public final static int MODE_COMPARE = 4;
    public final static int FASTA = 1;
    public final static int FASTQ = 2;
    public final static int TYPE_TEMPLATE = 0;
    public final static int TYPE_COMPLEMENT = 1;
    public final static int TYPE_2D = 2;
    public final static int TYPE_ALL = -1;
    public final static int TYPE_INSERTION = 0;
    public final static int TYPE_DELETION = 1;
    public final static int TYPE_SUBSTITUTION = 2;
    public final static int READTYPE_COMBINED = 0;
    public final static int READTYPE_PASS = 1;
    public final static int READTYPE_FAIL = 2;
    public final static int MIN_ALIGNMENTS = 10;
    public final static int PROGRESS_WIDTH = 50;
    private References references = new References(this);
    private String referenceFile=null;
    private String sampleDirectory = null;
    private String sampleName = null;
    private String scriptsDir="/Users/leggettr/Documents/github/nanotools/scripts";
    private String aligner="last";
    private String alignerParams="";
    private String scheduler="system";
    private String sampleList = null;
    private String comparisonDir = null;
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
    private boolean fixIDs = false;
    private boolean fixRandom = false;
    private boolean doKmerCounting = true;
    private boolean showAlignerCommand = false;
    private int runMode = 0;
    private int readFormat = FASTA;
    private int numThreads = 1;
    private String jobQueue = "";
    private NanoOKLog logFile = new NanoOKLog();
    private String imageFormat = "pdf";
    private int specifiedType = TYPE_2D;
    private String readsDir = "fast5";
    
    public NanoOKOptions() {
        String value = System.getenv("NANOOK_SCRIPT_DIR");
        
        if (value != null) {
            scriptsDir = value;
        } else {
            System.out.println("*** WARNING: You should set NANOOK_SCRIPT_DIR. Default value unlikely to work. ***");
            System.out.println("");
        }
                
        System.out.println("Scripts dir: "+scriptsDir);
    }
    
    public References getReferences() {
        return references;
    }
    
    public void setReferences(References r) {
        references = r;
    }
    
    /**
     * Parse command line arguments.
     * @param args array of command line arguments
     */
    public void parseArgs(String[] args) {
        int i=0;
        
        if (args.length <= 1) {
            System.out.println("");
            System.out.println("Syntax nanook <extract|align|analyse> -s <sample> [options]");
            System.out.println("");
            System.out.println("Standard options:");
            System.out.println("    -s|-sample <dir> specifies sample directory");
            System.out.println("    -t|-numthreads <number> specifies the number of threads to use (default 1)");
            System.out.println("");
            System.out.println("'extract' options:");
            System.out.println("    -f|-reads specifies subdirectory name for FAST5 files within sample directory (default fast5)");
            System.out.println("              e.g. -f reads/downloads if replicating Metrichor file structure");
            System.out.println("    -a|-fasta specifies FASTA file extraction (default)");
            System.out.println("    -q|-fastq specifies FASTQ file extraction");
            System.out.println("");
            System.out.println("'align' options:");
            System.out.println("    -r|-reference <path> specifies path to reference database");
            System.out.println("    -aligner <name> specifies the aligner (default last)"); 
            System.out.println("    -alignerparams <params> specifies paramters to the aligner");
            System.out.println("    -showaligns echoes aligner commands to screen");
            System.out.println("");
            System.out.println("'analyse' options:");
            System.out.println("    -r|-reference <path> specifies path to reference database");
            System.out.println("    -aligner <name> specifies the aligner (default last)");            
            System.out.println("    -coveragebin <int> specifies coverage bin size (default 100)");            
            System.out.println("    -passonly to analyse only pass reads");
            System.out.println("    -failonly to analyse only fail reads");            
            System.out.println("    -2donly to analyse only 2D reads"); 
            System.out.println("    -bitmaps to output bitmap PNG graphs instead of PDF");
            System.out.println("");
            System.exit(0);
        }
        
        parseAlignments = true;
        plotGraphs = true;
        makeReport = true;
                        
        if (args[i].equals("extract")) {
            runMode = MODE_EXTRACT;
        } else if (args[i].equals("align")) {
            runMode = MODE_ALIGN;
        } else if (args[i].equals("analyse") || args[i].equals("analyze")) {
            runMode = MODE_ANALYSE;
        } else if (args[i].equals("compare")) {
            runMode = MODE_COMPARE;
        } else {
            System.out.println("Unknonwn mode " + args[i] + " - must be extract, align or analyse");
            System.exit(1);
        }
        i++;
        
        while (i < (args.length)) {
            if (args[i].equalsIgnoreCase("-coveragebin")) {
                coverageBinSize = Integer.parseInt(args[i+1]);
                i+=2;
            } else if (args[i].equalsIgnoreCase("-reference") || args[i].equalsIgnoreCase("-r")) {
                referenceFile = args[i+1];
                i+=2;
            } else if (args[i].equalsIgnoreCase("-sample") |  args[i].equalsIgnoreCase("-s")) {
                sampleDirectory = args[i+1];
                i+=2;
            } else if (args[i].equalsIgnoreCase("-samplelist") |  args[i].equalsIgnoreCase("-l")) {
                sampleList = args[i+1];
                i+=2;
            } else if (args[i].equalsIgnoreCase("-outputdir") |  args[i].equalsIgnoreCase("-o")) {
                comparisonDir = args[i+1];
                i+=2;
            } else if (args[i].equalsIgnoreCase("-reads") |  args[i].equalsIgnoreCase("-f")) {
                readsDir = args[i+1];
                i+=2;
            } else if (args[i].equalsIgnoreCase("-maxreads")) {
                maxReads = Integer.parseInt(args[i+1]);
                i+=2;
            } else if (args[i].equalsIgnoreCase("-log")) {
                logFile.open(args[i+1]);
                i+=2;
            } else if (args[i].equalsIgnoreCase("-type")) {
                if (args[i+1].equalsIgnoreCase("template")) {
                    specifiedType = TYPE_TEMPLATE;
                } else if (args[i+1].equalsIgnoreCase("complement")) {
                    specifiedType = TYPE_COMPLEMENT;
                } else if (args[i+1].equalsIgnoreCase("2d")) {
                    specifiedType = TYPE_2D;
                }
                i+=2;
            } else if (args[i].equalsIgnoreCase("-nofail") || args[i].equalsIgnoreCase("-passonly")) {
                processPassReads = true;
                processFailReads = false;  
                i++;
            } else if (args[i].equalsIgnoreCase("-nopass") || args[i].equalsIgnoreCase("-failonly")) {
                processPassReads = false;
                processFailReads = true;
                i++;
            } else if (args[i].equalsIgnoreCase("-fasta") || args[i].equalsIgnoreCase("-a")) {
                if (runMode == MODE_EXTRACT) { 
                    readFormat = FASTA;
                }
                i++;
            } else if (args[i].equalsIgnoreCase("-fastq") || args[i].equalsIgnoreCase("-q")) {
                if (runMode == MODE_EXTRACT) { 
                    readFormat = FASTQ;
                }
                i++;
            } else if (args[i].equalsIgnoreCase("-2donly")) {
                processTemplateReads = false;
                processComplementReads = false;
                i++;
            } else if (args[i].equalsIgnoreCase("-bitmaps")) {
                imageFormat = "png";
                i++;
            } else if (args[i].equalsIgnoreCase("-fixids")) {
                fixIDs = true;
                i++;
            } else if (args[i].equalsIgnoreCase("-showaligns")) {
                showAlignerCommand = true;
                i++;
            } else if (args[i].equalsIgnoreCase("-deterministic")) {
                fixRandom = true;
                i++;                
            } else if (args[i].equalsIgnoreCase("-aligner")) {
                aligner = args[i+1];
                i+=2;
            } else if (args[i].equalsIgnoreCase("-alignerparams")) {
                alignerParams = args[i+1];
                System.out.println("Alignment parameters: "+alignerParams);
                i+=2;
            } else if (args[i].equalsIgnoreCase("-scheduler")) {
                scheduler = args[i+1];
                i+=2;
            } else if (args[i].equalsIgnoreCase("-queue")) {
                jobQueue = args[i+1];
                i+=2;
            } else if (args[i].equalsIgnoreCase("-numthreads") || args[i].equalsIgnoreCase("-t")) {
                numThreads = Integer.parseInt(args[i+1]);
                i+=2;
            } else {                
                System.out.println("Unknown parameter: " + args[i]);
                System.exit(0);
            }            
        }
                
        if ((runMode == MODE_ALIGN) || (runMode == MODE_ANALYSE)) {
            if (referenceFile == null) {
                System.out.println("Error: You must specify a reference");
                System.exit(1);
            }
            if (!referenceFile.endsWith(".fa") && !referenceFile.endsWith(".fasta")) {
                System.out.println("Error: reference must specify a .fa or .fasta file");
                System.exit(1);
            }
        }
        
        if (runMode == MODE_COMPARE) {
            if (comparisonDir == null) {
                System.out.println("Error: you must specify an output dir for the comparison");
                System.exit(1);
            } else {
                checkAndMakeComparisonDirs();
            }
        } else {
            if (sampleDirectory == null) {
                System.out.println("Error: You must specify a sample");
                System.exit(1);
            } else {
                File s = new File(sampleDirectory);
                if (!s.exists()) {
                    System.out.println("Error: sample directory doesn't exist");
                    System.exit(1);
                }

                if (!s.isDirectory()) {
                    System.out.println("Error: sample doesn't point to a directory");
                    System.exit(1);
                }

                sampleDirectory = s.getAbsolutePath();

                sampleName = s.getName();
            }
        }
    }
        
    public String getAligner() {
        return aligner;
    }
    
    public String getAlignerParams() {
        return alignerParams;
    }
    
    public void setReadFormat(int f) {
        readFormat = f;
    }
        
    /**
     * Get sample name.
     * @return name String
     */
    public String getSample() {
        return sampleName;
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

    public void checkAndMakeComparisonDirs() {
        File f = new File(comparisonDir);
        if (!f.exists()) {
            f.mkdir();
        }
        
        f = new File(comparisonDir+File.separator+"graphs");
        if (!f.exists()) {
            f.mkdir();
        }

        f = new File(comparisonDir+File.separator+"latex");
        if (!f.exists()) {
            f.mkdir();
        }
        
        f = new File(comparisonDir+File.separator+"logs");
        if (!f.exists()) {
            f.mkdir();
        }        

        f = new File(comparisonDir+File.separator+"logs"+File.separator+"R");
        if (!f.exists()) {
            f.mkdir();
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
        return getAnalysisDir() + File.separator + "all_summary.txt";
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
    
    public String getSampleDirectory() {
        return sampleDirectory;
    }
    
    /**
     * Get graphs directory.
     * @return directory name as String
     */
    public String getGraphsDir() {
        if (runMode == MODE_COMPARE) {
            return comparisonDir + File.separator + "graphs";
        } else {
            return sampleDirectory + File.separator + "graphs";
        }
    } 

    public String getFastaDir() {
        return sampleDirectory + File.separator + "fasta";
    }

    public String getFastqDir() {
        return sampleDirectory + File.separator + "fastq";
    }    
    
    public String getFast5Dir() {
        return sampleDirectory + File.separator + readsDir;
    }
    
    /**
     * Get FASTA directory.
     * @return directory name as String
     */
    public String getReadDir() {
        String dir;
        
        if (readFormat == FASTQ) {
            dir = getFastqDir();
        } else {
            dir = getFastaDir(); 
        }
        
        return dir;
    } 

    /**
     * Get LAST directory.
     * @return directory name as String
     */
    public String getAlignerDir() {
        return sampleDirectory + File.separator + aligner;
    } 

    /**
     * Get LaTeX directory.
     * @return directory name as String
     */
    public String getLatexDir() {
        if (runMode == MODE_COMPARE) {
            return comparisonDir + File.separator + "latex";
        } else {
            return sampleDirectory + File.separator + "latex";
        }
    } 

    /**
     * Get logs directory.
     * @return directory name as String
     */
    public String getLogsDir() {
        if (runMode == MODE_COMPARE) {
            return comparisonDir + File.separator + "logs";
        } else {
            return sampleDirectory + File.separator + "logs";
        }
    } 
    
    public boolean isNewStyleDir() {
        File passDir = new File(getFast5Dir() + File.separator + "pass");
        File failDir = new File(getFast5Dir() + File.separator + "pass");
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
        return sampleDirectory + File.separator + "analysis";
    } 
    
    /**
     * Get LaTeX filename.
     * @return filename as String
     */
    public String getTexFilename() {
        return sampleDirectory + File.separator + "latex" + File.separator + sampleName + ".tex";
    }
    
    /**
     * Check if processing "pass" reads.
     * @return true to process
     */
    public boolean isProcessingPassReads() {
        return processPassReads;
    }

    /**
     * Check if processing "fail" reads.
     * @return true to process
     */
    public boolean isProcessingFailReads() {
        return processFailReads;
    }
    
    public boolean isProcessingComplementReads() {
        return processComplementReads;
    }
    
    public boolean isProcessingTemplateReads() {
        return processTemplateReads;
    }

    public boolean isProcessing2DReads() {
        return process2DReads;
    }
    
    public boolean isProcessingReadType(int type) {
        boolean r = false;
        
        switch(type) {
            case TYPE_ALL:
                r = true;
                break;
            case TYPE_TEMPLATE:
                r = processTemplateReads;
                break;
            case TYPE_COMPLEMENT:
                r = processComplementReads;
                break;
            case TYPE_2D:
                r = process2DReads;
                break;
        }         
        
        return r;
    }
    
    public int getNumberOfTypes() {
        int t = 0;
        if (processTemplateReads) t++;
        if (processComplementReads) t++;
        if (process2DReads) t++;
        return t;
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
    
    public int getReadFormat() {
        return readFormat;
    }
        
    public int getRunMode() {
        return runMode;
    }
    
    public boolean fixIDs() {
        return fixIDs;
    }
    
    public boolean fixRandom() {
        return fixRandom;
    }
    
    public String getScheduler() {
        return scheduler;
    }
    
    public int getNumberOfThreads() {
        return numThreads;
    }
    
    public String getQueue() {
        return jobQueue;
    }
    
    public NanoOKLog getLog() {
        return logFile;
    }
    
    /**
     * Get the right parser
     * @param options
     * @return 
     */
    public AlignmentFileParser getParser() {
        AlignmentFileParser parser = null;
        
        switch(aligner) {
            case "last":
                parser = new LastParser(this, references);
                break;
            case "bwa":
                parser = new BWAParser(this, references);                
                break;
            case "blasr":
                parser = new BLASRParser(this, references);                                    
                break;
            case "marginalign":
                parser = new MarginAlignParser(this, references);                                    
                break;
            default:
                System.out.println("Aligner unknown!");
                System.out.println("");
                System.exit(1);
                break;                      
        }
        
        return parser;
    }    
    
    public boolean doKmerCounting() {
        return doKmerCounting;
    }
    
    public String getImageFormat() {
        return imageFormat;
    }
    
    public String getSampleList() {
        return sampleList;
    }
    
    public String getComparisonDir() {
        return comparisonDir;
    }
    
    public int getSpecifiedType() {
        return specifiedType;
    }
    
    public boolean showAlignerCommand() {
        return showAlignerCommand;
    }
 }
