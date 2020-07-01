/*
 * Program: NanoOK
 * Author:  Richard M. Leggett (richard.leggett@earlham.ac.uk)
 * 
 * Copyright 2015-17 Earlham Institute
 */

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
    private String defaultFormatString = "6 qseqid sseqid pident length mismatch gapopen qstart qend sstart send evalue bitscore stitle staxids";
    private ArrayList<String> inputFilenames = new ArrayList<String>();
    private ArrayList<String> blastFilenames = new ArrayList<String>();
    private boolean runningFromMultiFastQ = false;
    
    public BlastHandler(NanoOKOptions o, int t, int pf) {
        options = o;
        type = t;
        passfail = pf;
        if (options.getFileCounterOffset() > 0) {
            fileCounter = options.getFileCounterOffset();
            System.out.println("File offset "+fileCounter);
        }        
    }
    
    private void writeMeganFile() {
        ArrayList<String> blastProcesses = options.getBlastProcesses();
        String meganDir = options.getSampleDirectory() + File.separator + "megan";
        File f = new File(meganDir);
        
        if (!f.exists()) {
            f.mkdir();
        }
        
        for (int i=0; i<blastProcesses.size(); i++) {
            String[] params = blastProcesses.get(i).split(",");
            if ((params.length == 5) || (params.length == 6)) {
                String blastName = params[0];
                String blastTool = params[1];
                String blastDb = params[2];
                String memory = params[3];
                String queue = params[4];
                // A is min support 1
                String cmdPathnameA = meganDir + File.separator + "all_" + NanoOKOptions.getTypeFromInt(type) +
                                    "_" + NanoOKOptions.getPassFailFromInt(passfail) + "_" + Integer.toString(fileCounter) + "_ms1.cmds";
                String meganPathnameA = meganDir + File.separator + "all_" + NanoOKOptions.getTypeFromInt(type) +
                                    "_" + NanoOKOptions.getPassFailFromInt(passfail) + "_" + Integer.toString(fileCounter) + "_ms1.rma";
                String slurmPathnameA = meganDir + File.separator + "all_" + NanoOKOptions.getTypeFromInt(type) +
                                    "_" + NanoOKOptions.getPassFailFromInt(passfail) + "_" + Integer.toString(fileCounter) + "_ms1.slurm.sh";
                String slurmLognameA = meganDir + File.separator + "all_" + NanoOKOptions.getTypeFromInt(type) +
                                    "_" + NanoOKOptions.getPassFailFromInt(passfail) + "_" + Integer.toString(fileCounter) + "_ms1.slurm.log";
                // B is min support 0.1%
                String cmdPathnameB = meganDir + File.separator + "all_" + NanoOKOptions.getTypeFromInt(type) +
                                    "_" + NanoOKOptions.getPassFailFromInt(passfail) + "_" + Integer.toString(fileCounter) + "_ms0.1pc.cmds";
                String meganPathnameB = meganDir + File.separator + "all_" + NanoOKOptions.getTypeFromInt(type) +
                                    "_" + NanoOKOptions.getPassFailFromInt(passfail) + "_" + Integer.toString(fileCounter) + "_ms0.1pc.rma";
                String slurmPathnameB = meganDir + File.separator + "all_" + NanoOKOptions.getTypeFromInt(type) +
                                    "_" + NanoOKOptions.getPassFailFromInt(passfail) + "_" + Integer.toString(fileCounter) + "_ms0.1pc.slurm.sh";
                String slurmLognameB = meganDir + File.separator + "all_" + NanoOKOptions.getTypeFromInt(type) +
                                    "_" + NanoOKOptions.getPassFailFromInt(passfail) + "_" + Integer.toString(fileCounter) + "_ms0.1pc.slurm.log";
               
                
                try {
                    String blastFileString="";
                    String fastaFileString="";
                    
                    for (int fc=0; fc<=fileCounter; fc++) {
                        String fileName = "all_" + NanoOKOptions.getTypeFromInt(type) + "_" + NanoOKOptions.getPassFailFromInt(passfail) + "_" +  Integer.toString(fc);
                        String fastaPathname;
                        String blastPathname;
                        
                        if (runningFromMultiFastQ) {
                            fastaPathname = inputFilenames.get(fc);
                            blastPathname = blastFilenames.get(fc);
                        } else {
                            fastaPathname = options.getReadDir() + "_chunks" + File.separator + fileName + (options.getReadFormat() == NanoOKOptions.FASTA ? ".fasta":".fastq");
                            blastPathname = options.getSampleDirectory() + File.separator +
                                                 blastTool + "_" + blastName + File.separator + 
                                                 fileName + "_" + blastTool + "_" + blastName + ".txt";
                        }
                        
                        if (blastFileString != "") {
                            blastFileString += ",";
                            fastaFileString += ",";
                        }
                        fastaFileString = fastaFileString + "'" + fastaPathname + "'";
                        blastFileString = blastFileString + "'" + blastPathname + "'";
                    }
                    
                    options.getLog().println("Writing MEGAN command file " + cmdPathnameA);
                    PrintWriter pw = new PrintWriter(new FileWriter(cmdPathnameA));
                    pw.println("setprop MaxNumberCores=4;");
                    pw.print("import blastFile="+blastFileString+" fastaFile="+fastaFileString +" meganFile="+meganPathnameA);
                    pw.println(" maxMatches=100 maxExpected=0.001 minSupport=1 minComplexity=0 blastFormat=BlastTAB;");
                    pw.println("quit;");
                    pw.close();

                    options.getLog().println("Writing MEGAN command file " + cmdPathnameB);
                    pw = new PrintWriter(new FileWriter(cmdPathnameB));
                    pw.println("setprop MaxNumberCores=4;");
                    pw.print("import blastFile="+blastFileString+" fastaFile="+fastaFileString +" meganFile="+meganPathnameB);
                    pw.println(" maxMatches=100 maxExpected=0.001 minSupportPercent=0.1 minComplexity=0 blastFormat=BlastTAB;");
                    pw.println("quit;");
                    pw.close();

                    pw = new PrintWriter(new FileWriter(slurmPathnameA));
                    if (!options.isMac()) {
                        pw.print("slurmit -p ei-long -c 4 -o " + slurmLognameA + " -m \"16G\" \"");
                    }
                    pw.print(options.getMeganCmdLine());
                    pw.println(" -g -c " + cmdPathnameA + " -L " + options.getMeganLicense());
                    if (!options.isMac()) {
                        pw.print("\"");
                    }                    
                    pw.close();
                    
                    pw = new PrintWriter(new FileWriter(slurmPathnameB));
                    if (!options.isMac()) {
                        pw.print("slurmit -p ei-long -c 4 -o " + slurmLognameB + " -m \"16G\" \"");
                    }
                    pw.print(options.getMeganCmdLine());
                    pw.println(" -g -c " + cmdPathnameB + " -L " + options.getMeganLicense());
                    if (!options.isMac()) {
                        pw.print("\"");
                    }                    
                    pw.close();
                    
                    
                } catch (Exception e) {
                    System.out.println("writeMeganFile exception");
                    e.printStackTrace();
                }
            }
        }
    }
    
    void createSizesFile(String fastqFilename, String sizesFilename) {
        System.out.println("Writing "+sizesFilename);
        
        try {
            BufferedReader br = new BufferedReader(new FileReader(fastqFilename));
            PrintWriter pwSizes = new PrintWriter(new FileWriter(sizesFilename));
            String line;
            
            while ((line = br.readLine()) != null) {
                String header = line;
                String seq = br.readLine();
                String otherheader = br.readLine();
                String quals = br.readLine();
                String[] tokens = header.substring(1).split("\\s+");
                int readSize = seq.length();
                String id = tokens[0];
                pwSizes.println(id + "\t" + readSize);            
            }
            pwSizes.close();
            br.close();
        } catch (IOException e) {
            System.out.println("createSizesFile exception");
            e.printStackTrace();
        }
                    
    }
    
    private void runBlasts(String inputPathname) {
        String formatString = "'" + defaultFormatString + "'";
        ArrayList<String> blastProcesses = options.getBlastProcesses();
        File iff = new File(inputPathname);
        String fileName = iff.getName();
        String filePrefix = fileName;
        
        if (filePrefix.contains(".")) {
            filePrefix = fileName.substring(0, fileName.lastIndexOf('.'));
        }
        
        for (int i=0; i<blastProcesses.size(); i++) {
            String[] params = blastProcesses.get(i).split(",");
            if ((params.length == 5) || (params.length == 6)) {
                String blastName = params[0];
                String blastTool = params[1];
                String blastDb = params[2];
                String memory = params[3];
                String queue = params[4];
                String taxfilter = "";
                
                if (params.length == 6) {
                    taxfilter = params[5];
                }
                
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
                
                inputFilenames.add(inputPathname);
                blastFilenames.add(outputBlast);
                
                try {
                    options.getLog().println("Writing blast command file "+commandFile);
                    PrintWriter pw = new PrintWriter(new FileWriter(commandFile));
                    // TODO: -task option shouldn't be hardcoded
                    String command = "";
                    SimpleJobScheduler jobScheduler = options.getJobScheduler();
                    
                    if (options.isNedome()) {
                        command = blastTool + " -db " + blastDb + " -query " + inputPathname + " -evalue " + Double.toString(options.getBlastMaxE()) + " -max_target_seqs 1 -max_hsps 1 -task "+options.getBlastTask()+" -out " + outputBlast + " -outfmt "+formatString;
                    } else {
                        command = blastTool + " -db " + blastDb + " -query " + inputPathname + " -evalue " + Double.toString(options.getBlastMaxE()) + " -max_target_seqs " + Integer.toString(options.getBlastMaxTargetSeqs()) + " -show_gis -task "+options.getBlastTask()+" -out " + outputBlast + " -outfmt "+formatString;
                    }
                    if (taxfilter.length() > 1) {
                        command = command + " -taxidlist " + taxfilter;
                    }
                    
                    pw.write(command);
                    pw.close();

                    if (jobScheduler == null) {
                        options.getLog().println("Submitting blast command file to SLURM "+commandFile);
                        ProcessLogger pl = new ProcessLogger();
                        String[] commands = {"slurmit",
                                             "-o", logFile,
                                             "-p", queue,
                                             "-m", memory,
                                             "sh "+commandFile};
                        pl.runCommandToLog(commands, options.getLog());            
                    } else if (options.isNedome()) {
//                        String[] commands = {blastTool,
//                                             "-db", blastDb,
//                                             "-query", inputPathname,
//                                             "-evalue", Double.toString(options.getBlastMaxE()),
//                                             "-max_target_seqs", "1",
//                                             "-max_hsps", "1",
//                                             "-task", options.getBlastTask(),
//                                             "-out", outputBlast,
//                                             "-outfmt", defaultFormatString};

                        String outputMinimap = options.getSampleDirectory() + File.separator +
                                     blastTool + "_" + blastName + File.separator + 
                                     filePrefix + "_" + blastTool + "_" + blastName + ".paf";
                        
                        createSizesFile(inputPathname, inputPathname+".sizes");

                        String[] commands = {"minimap2",
                                             "-x", "map-ont",
                                             blastDb,
                                             inputPathname};

                        File f = new File(outputMinimap);
                        if (f.exists()) {
                            f.delete();
                            System.out.println("Deleted existing file "+f.getPath());
                        }
                        
                        f = new File(outputMinimap + ".completed");
                        if (f.exists()) {
                            f.delete();
                            System.out.println("Deleted existing file "+f.getPath());
                        }
                        
                        jobScheduler.submitJob(commands, outputMinimap, logFile);
                        System.out.println("Submitting from BlastHandler");                        
                    } else {
                        String[] commands;
                        
                        if (taxfilter.length() > 1) {
                            commands = new String[]{blastTool,
                                                 "-db", blastDb,
                                                 "-query", inputPathname,
                                                 "-evalue", Double.toString(options.getBlastMaxE()),
                                                 "-max_target_seqs", Integer.toString(options.getBlastMaxTargetSeqs()),
                                                 "-show_gis",
                                                 "-task", options.getBlastTask(),
                                                 "-out", outputBlast,
                                                 "-outfmt", defaultFormatString,
                                                 "-taxidlist", taxfilter};
                        } else {
                            commands = new String[]{blastTool,
                                                 "-db", blastDb,
                                                 "-query", inputPathname,
                                                 "-evalue", Double.toString(options.getBlastMaxE()),
                                                 "-max_target_seqs", Integer.toString(options.getBlastMaxTargetSeqs()),
                                                 "-show_gis",
                                                 "-task", options.getBlastTask(),
                                                 "-out", outputBlast,
                                                 "-outfmt", defaultFormatString};
                        }
                                                
                        jobScheduler.submitJob(commands, logFile);
                        System.out.println("Submitting from BlastHandler");
                    }
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
        String mergedPathname = options.getReadDir() + 
                                "_chunks" + File.separator + 
                                "all_" + NanoOKOptions.getTypeFromInt(type) + "_" + NanoOKOptions.getPassFailFromInt(passfail) + "_" + 
                                Integer.toString(fileCounter) + 
                                (options.getReadFormat() == NanoOKOptions.FASTA ? ".fasta":".fastq");

        options.getLog().println("Writing merged file "+mergedPathname);
               
        try {
            PrintWriter pw = new PrintWriter(new FileWriter(mergedPathname));
            PrintWriter pwSizes = null;

            if (options.isNedome()) {
                pwSizes = new PrintWriter(new FileWriter(mergedPathname + ".sizes"));
            }
            
            for (int i=0; i<mergeList.size(); i++) {                
                BufferedReader br = new BufferedReader(new FileReader(mergeList.get(i)));
                String line;
                String id = "Unknown";
                int lineCount = 0;
                int readSize = 0;

                while ((line = br.readLine()) != null) {
                    pw.println(line);
                    
                    if (options.isNedome()) {
                        lineCount++;
                        if (lineCount == 1) {
                            id = line.substring(1, line.indexOf(' '));
                        } else {
                            readSize += line.length();
                        }
                    }

                }
                br.close();

                if (options.isNedome()) {
                    pwSizes.println(id + "\t" + readSize);
                }
            }
            pw.close();
            if (options.isNedome()) {
                pwSizes.close();
            }
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
            options.getLog().println("Merging files (nSeqs = "+nSeqs+")");
            String mergedPathname = mergeInputFiles();
            runBlasts(mergedPathname);
            writeMeganFile();
            
            //options.getThreadExecutor().execute(new FastAQMerger(options, mergedFilename, mergeList, fileCounter));
            mergeList = new ArrayList(); 
            fileCounter++;
            nSeqs = 0;
        }
    }    
    
    public synchronized void addReadChunk(String readFilename) {
        runningFromMultiFastQ = true;
        runBlasts(readFilename);
        writeMeganFile();
        fileCounter++;
    }
}
