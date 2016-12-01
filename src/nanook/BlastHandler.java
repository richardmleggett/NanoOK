package nanook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class BlastHandler {
    private NanoOKOptions options = null;
    private int type;
    private int passfail;
    private int nSeqs = 0;
    private int fileCounter = 0;
    private ArrayList<String> mergeList = new ArrayList<String>();
    
    public BlastHandler(NanoOKOptions o, int t, int pf) {
        options = o;
        type = t;
        passfail = pf;
        if (options.getFileCounterOffset() > 0) {
            if (type == NanoOKOptions.TYPE_TEMPLATE) {
                if (pf == NanoOKOptions.READTYPE_PASS) {
                    fileCounter = 98; 
                } else if (pf == NanoOKOptions.READTYPE_FAIL) {
                    fileCounter = 138;
                } else {
                    System.out.println("Error!");
                    System.exit(1);
                }
            }
            System.out.println("File offset "+fileCounter);
        }        
    }
    
    private void runBlasts(String inputPathname) {
        String formatString = "'6 qseqid sseqid pident length mismatch gapopen qstart qend sstart send evalue bitscore stitle'";
        ArrayList<String> blastProcesses = options.getBlastProcesses();
        File iff = new File(inputPathname);
        String fileName = iff.getName();
        String filePrefix = fileName;
        
        if (filePrefix.contains(".")) {
            filePrefix = fileName.substring(0, fileName.lastIndexOf('.'));
        }
        
        for (int i=0; i<blastProcesses.size(); i++) {
            String[] params = blastProcesses.get(i).split(",");
            if (params.length == 5) {
                String blastName = params[0];
                String blastTool = params[1];
                String blastDb = params[2];
                String memory = params[3];
                String queue = params[4];
                String outputBlast = options.getSampleDirectory() + File.separator +
                                     blastTool + "_" + blastName + File.separator + 
                                     filePrefix + "_" + blastTool + "_" + blastName + ".txt";
                String commandFile = options.getSampleDirectory() + File.separator +
                                     blastTool + "_" + blastName + File.separator +
                                     filePrefix + "_" + blastTool + "_" + blastName + ".sh";
                String logFile = options.getLogsDir() + File.separator +
                                 blastTool + "_" + blastName + File.separator +
                                 filePrefix + "_" + blastTool + "_" + blastName + ".log";

                options.getLog().println("  BLAST input: " + inputPathname);
                options.getLog().println(" BLAST output: " + outputBlast);
                options.getLog().println("BLAST command: " + commandFile);
                options.getLog().println("    BLAST log: " + logFile);
                
                try {
                    options.getLog().println("Writing blast command file "+commandFile);
                    PrintWriter pw = new PrintWriter(new FileWriter(commandFile));
                    pw.write(blastTool + " -db " + blastDb + " -query " + inputPathname + " -evalue 0.001 -show_gis -out " + outputBlast + " -outfmt "+formatString);
                    pw.close();

                    System.out.println("Submitting blast command file to SLURM "+commandFile);
                    ProcessLogger pl = new ProcessLogger();
                    String[] commands = {"slurmit",
                                         "-o", logFile,
                                         "-p", queue,
                                         "-m", memory,
                                         "sh "+commandFile};
                    pl.runCommand(commands);            
                } catch (IOException e) {
                    System.out.println("runBlast exception");
                    e.printStackTrace();
                }
            } else {
                System.out.println("Badly formatted BLAST process: "+blastProcesses.get(i));
            }
        }
    }    
    
    private String mergeInputFiles() {
        String mergedPathname = options.getReadDir() + File.separator + 
                                "merged" + File.separator + 
                                "all_" + NanoOKOptions.getTypeFromInt(type) + "_" + NanoOKOptions.getPassFailFromInt(passfail) + "_" + 
                                Integer.toString(fileCounter) + 
                                (options.getReadFormat() == NanoOKOptions.FASTA ? ".fasta":".fastq");

        System.out.println("Writing merged file "+mergedPathname);
        
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(mergedPathname));
            
            for (int i=0; i<mergeList.size(); i++) {                
                BufferedReader br = new BufferedReader(new FileReader(mergeList.get(i)));
                String line;                
                while ((line = br.readLine()) != null) {
                    pw.println(line);
                }
                br.close();
            }            
            pw.close();            
        } catch (IOException e) {
            System.out.println("mergeFiles exception");
            e.printStackTrace();
        }
        return mergedPathname;
    }     
    
    public synchronized void addRead(String readFilename) {
        mergeList.add(readFilename);
        nSeqs++;
        if (nSeqs == options.getReadsPerBlast()) {
            System.out.println("Merging files (nSeqs = "+nSeqs+")");
            String mergedPathname = mergeInputFiles();
            runBlasts(mergedPathname);
            
            //options.getThreadExecutor().execute(new FastAQMerger(options, mergedFilename, mergeList, fileCounter));
            mergeList = new ArrayList(); 
            fileCounter++;
            nSeqs = 0;
        }
    }    
}
