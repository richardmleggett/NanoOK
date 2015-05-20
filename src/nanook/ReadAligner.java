/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nanook;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 *
 * @author leggettr
 */
public class ReadAligner {
    NanoOKOptions options;
    AlignmentFileParser parser;
    
    /**
     * Constructor
     * @param o program options
     */
    public ReadAligner(NanoOKOptions o, AlignmentFileParser afp) {    
        options = o;
        parser = afp;
    }
    
    private void checkAndMakeDir(String dir) {
        File f = new File(dir);
        if (f.exists()) {
            if (!f.isDirectory()) {
                System.out.println("Error: " + dir + " is a file, not a directory!");
                System.exit(1);
            }
        } else {
            System.out.println("Making directory " + dir);
            f.mkdir();
        }
    }
    
    /**
     * Create directories for output
     */
    public void createDirectories() {
        checkAndMakeDir(options.getAlignerDir());
        checkAndMakeDir(options.getLogsDir());
        checkAndMakeDir(options.getLogsDir() + File.separator + options.getAligner());
    }
    
    public boolean isValidReadFile(String filename) {
        boolean isValid = false;
        
        System.out.println(filename);
        
        if (parser.getReadFormat() == NanoOKOptions.FASTA) {
            if (filename.endsWith(".fa") || filename.endsWith(".fasta")) {
                isValid = true;
            }
        } else if (parser.getReadFormat() == NanoOKOptions.FASTQ) {
            if (filename.endsWith(".fq") || filename.endsWith(".fastq")) {
                isValid = true;
            }
        }            
        
        return isValid;
    }
    
    private void runCommandLSF(String command, String outPath, String log) {
        // outPath only non-null if aligner will only write to screen (yes, BWA, I'm talking about you)
        if (outPath != null) {
             command = command + " > " + outPath;    
        }        
        
        // Make the LSF command
        String lsfCommand = "bsub -n " + options.getNumberOfThreads() + " -q " + options.getQueue() + " -oo " + log + " -R \"rusage[mem=8000] span[hosts=1]\" \"" + command + "\"";
        System.out.println(command);
        //pl = new ProcessLogger();
        //response = pl.getCommandOutput(lsfCommand, true, true);                
    }
    
    private void runCommandLocal(String command, String outPath) {
        ProcessLogger pl = new ProcessLogger();
        
        // outPath only non-null if aligner will only write to screen (yes, BWA, I'm talking about you)
        if (outPath != null) {
            pl.setWriteFormat(false, true, false);
            pl.runAndLogCommand(command, outPath, false);
        } else {
            pl.runCommand(command);
        }
    }
    
    /**
     * Run the alignment command
     * @param command
     * @param outPath
     * @param log 
     */
    private void runCommand(String command, String outPath, String log) {        
        switch(options.getScheduler()) {
            case "screen":
                System.out.println(command);
                break;
            case "lsf":
                runCommandLSF(command, outPath, log);
                break;
            case "system":
                runCommandLocal(command, outPath);
                break;
            default:
                System.out.println("Error: scheduler " + options.getScheduler() + " not recognised.");
                System.exit(1);
                break;
        }
    }
    
    private void checkReferenceSizesFile(String referenceFile) {
        String sizesFilename = referenceFile + ".sizes";
        File sizesFile = new File(sizesFilename);
        if (!sizesFile.exists()) {
            System.out.println("\nGenerating .sizes file for reference");
            SequenceReader sr = new SequenceReader(false);
            sr.indexFASTAFile(referenceFile, sizesFilename , false);
        }
    }
    
    private void processDirectory(String readsDir, String alignDir, String logDirName) {
        String reference = options.getReferenceFile();
        
        checkReferenceSizesFile(reference);
        checkAndMakeDir(alignDir);
        checkAndMakeDir(logDirName);
        
        for (int t=0; t<3; t++) {
            String inputDirName = readsDir + File.separator + NanoOKOptions.getTypeFromInt(t);
            String outputDirName = alignDir + File.separator + NanoOKOptions.getTypeFromInt(t);
            
            checkAndMakeDir(outputDirName);
            
            File inputDir = new File(inputDirName);
            File[] listOfFiles = inputDir.listFiles();

            if (listOfFiles == null) {
                System.out.println("Directory "+inputDirName+" doesn't exist");
            } else if (listOfFiles.length <= 0) {
                System.out.println("Directory "+inputDirName+" empty");
            } else {
                System.out.println("\nProcessing from "+inputDirName);

                int readCount = 0;
                for (File file : listOfFiles) {
                    if (file.isFile()) {
                        if (isValidReadFile(file.getName())) {
                            String inPath = inputDirName + File.separator + file.getName();
                            String outPath = outputDirName + File.separator + file.getName() + parser.getAlignmentFileExtension();
                            String logFile = logDirName + File.separator + file.getName() + ".log";
                            String command = parser.getRunCommand(inPath, outPath, reference);
                            
                            System.out.println("Aligning "+inPath);
                            System.out.println("      to "+outPath);
                            
                            runCommand(command, parser.outputsToStdout() ? outPath:null, logFile);
                            readCount++;
                        }
                    }
                }
                
                if (readCount == 0) {
                    System.out.print("Error: unable to find any ");
                    if (parser.getReadFormat() == NanoOKOptions.FASTA) {
                        System.out.print("FASTA");
                    } else if (parser.getReadFormat() == NanoOKOptions.FASTQ) {
                        System.out.print("FASTQ");
                    }
                    System.out.println(" files to align\n");
                    System.exit(1);
                }
            }
        }
    }
    
    public void align() {
        if (options.isNewStyleDir()) {
            if (options.isProcessingPassReads()) {
                processDirectory(options.getReadDir() + File.separator + "pass",
                                 options.getAlignerDir() + File.separator + "pass",
                                 options.getLogsDir() + File.separator + options.getAligner() + File.separator + "pass");
            }
            
            if (options.isProcessingFailReads()) {
                processDirectory(options.getReadDir() + File.separator + "fail",
                                 options.getAlignerDir() + File.separator + "fail",
                                 options.getLogsDir() + File.separator + options.getAligner() + File.separator + "fail");
            }
        } else {
            processDirectory(options.getReadDir(), options.getAlignerDir(), options.getLogsDir() + File.separator + options.getAligner());
        }        

        System.out.println("DONE");
    }
}
