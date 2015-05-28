/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nanook;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.*;

/**
 *
 * @author leggettr
 */
public class ReadAligner {
    private NanoOKOptions options;
    private AlignmentFileParser parser;
    private ExecutorService executor;
    
    /**
     * Constructor
     * @param o program options
     */
    public ReadAligner(NanoOKOptions o, AlignmentFileParser afp) {    
        options = o;
        parser = afp;
        
        executor = Executors.newFixedThreadPool(options.getNumberOfThreads());
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
        
        //System.out.println(filename);
        
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
                //System.out.println("\nProcessing from "+inputDirName);

                int readCount = 0;
                for (File file : listOfFiles) {
                    if (file.isFile()) {
                        if (isValidReadFile(file.getName())) {
                            String inPath = inputDirName + File.separator + file.getName();
                            String outPath = outputDirName + File.separator + file.getName() + parser.getAlignmentFileExtension();
                            String logFile = logDirName + File.separator + file.getName() + ".log";
                            String command = parser.getRunCommand(inPath, outPath, reference);                            
                            executor.execute(new SystemCommandRunnable(options, "Aligning "+inPath+"\n      to "+outPath, command, parser.outputsToStdout() ? outPath:null, logFile));
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
        
        // That's all - wait for all threads to finish
        executor.shutdown();
        while (!executor.isTerminated()) {
        }        

        System.out.println("DONE");
    }
}
