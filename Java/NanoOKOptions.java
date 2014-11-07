package nanotools;

import java.io.*;

public class NanoOKOptions {
    public final static int MAX_KMER = 1000;
    public final static int MAX_READ_LENGTH = 1000000;
    public final static int MAX_READS = 1000000;
    private String program="";
    private String baseDir="/Users/leggettr/Documents/Projects/Nanopore";
    private String reference=null;
    private String sample=null;
    private String scriptsDir="/Users/leggettr/Documents/github/nanotools/scripts";
    private int coverageBinSize = 100;
    
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
                reference = args[i+1];
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
        if (reference == null) {
            System.out.println("Error: You must specify a reference");
            System.exit(1);
        }
        if (sample == null) {
            System.out.println("Error: You must specify a sample");
            System.exit(1);
        }
    }
    
    public String getProgram() {
        return program;
    }
    
    public String getBaseDirectory() {
        return baseDir;
    }
    
    public String getSample() {
        return sample;
    }
    
    public String getReference() {
        return reference;
    }
    
    public int getCoverageBinSize() {
        return coverageBinSize;
    }
        
    public String getTypeFromInt(int n) {
        String typeString;
        
        switch(n) {
            case 0: typeString = "Template"; break;
            case 1: typeString = "Complement"; break;
            case 2: typeString = "2D"; break;
            default: typeString = "Unknown"; break;
        }
        
        return typeString;
    }
    
    public String getSeparator() {
        return "/";   
    }
    
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


    
    public String getAlignmentSummaryFilename() {
        return baseDir + getSeparator() + sample + getSeparator() + "analysis" + getSeparator() + "alignment_summary.txt";
    }

    public String getLengthSummaryFilename() {
        return baseDir + getSeparator() + sample + getSeparator() + "analysis" + getSeparator() + "length_summary.txt";
    }    
    
    public String getScriptsDir() {
        return scriptsDir;
    }
    
    public String getGraphsDir() {
        return baseDir + getSeparator() + sample + getSeparator() + "graphs";
    } 
    
    public String getAnalysisDir() {
        return baseDir + getSeparator() + sample + getSeparator() + "analysis";
    } 
    
    public String getTexFilename() {
        return baseDir + getSeparator() + sample + getSeparator() + "latex" + getSeparator() + sample + ".tex";
    }
}
