/*
 * Program: NanoOK
 * Author:  Richard M. Leggett
 * 
 * Copyright 2015 The Genome Analysis Centre (TGAC)
 */

package nanook;

import java.util.ArrayList;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Entry class for tool.
 * 
 * @author Richard Leggett
 */
public class NanoOK {
    public final static String VERSION_STRING = "v0.24";
    
    /**
     * Check for program dependencies - R, pdflatex
     */
    public static void checkDependencies() {
        ProcessLogger pl = new ProcessLogger();
        ArrayList<String> response;
        String rVersion = null;
        String pdflatexVersion = null;
        String hVersion = null;
                
        response = pl.checkCommand("Rscript --version");
        if (response != null) {
            for (int i=0; i<response.size(); i++) {
                String s = response.get(i);
                if (s.startsWith("R scripting front-end")) {
                    rVersion = s;
                }
            }
        }
        
        if (rVersion == null) {
            System.out.println("*** ERROR: Couldn't find Rscript - is R installed? ***");
        } else {
            System.out.println(rVersion);
        }
        
        response = pl.checkCommand("pdflatex --version");
        if (response != null) {
            for (int i=0; i<response.size(); i++) {
                String s = response.get(i);
                if (s.contains("pdfTeX")) {
                    pdflatexVersion = s;
                    break;
                }
            }
        }
        
        if (pdflatexVersion == null) {
            System.out.println("*** ERROR: Couldn't find pdflatex - is TeX installed? ***");
        } else {
            System.out.println(pdflatexVersion);
        }

        response = pl.checkCommand("h5dump --version");
        if (response != null) {
            for (int i=0; i<response.size(); i++) {
                String s = response.get(i);
                if (s.startsWith("h5dump")) {
                    hVersion = s;
                }
            }
        }
        
        if (hVersion == null) {
            System.out.println("*** ERROR: Couldn't find h5dump - is H5 Tools installed? ***");
        } else {
            System.out.println(hVersion);
        }
        
        System.out.println("");
    }

    /**
     * Test logo plotting
     */
    public static void testLogo() {
        SequenceLogo logo = new SequenceLogo();
        logo.drawImage();
        logo.saveImage("/Users/leggettr/Desktop/logo.png");
    }
    
    /**
     * Test SequenceReader class
     */
    public static void testSequenceReader() {
        SequenceReader r = new SequenceReader(true);
        r.indexFASTAFile("/Users/leggettr/Documents/Projects/Nanopore/test.fasta", null, true);
        String s = r.getSubSequence("gi|223667766|ref|NZ_DS264586.1|", 0, 499);
        System.out.println("String (0,499) = ["+s+"]");
        s = r.getSubSequence("gi|223667766|ref|NZ_DS264586.1|", 0, 9);
        System.out.println("String (0,9) = ["+s+"]");
        s = r.getSubSequence("gi|223667766|ref|NZ_DS264586.1|", 200, 209);
        System.out.println("String (200,209) = ["+s+"]");
        s = r.getSubSequence("gi|223667766|ref|NZ_DS264586.1|", 200, 214);
        System.out.println("String (200,214) = ["+s+"]");
    }
    
    /**
     * Test parser
     * @param options
     * @param overallStats
     * @param references 
     */
    public static void testParser(NanoOKOptions options, OverallStats overallStats, References references) {
        AlignmentFileParser p = new LastParser(options, references);
        AlignmentsTableFile nonAlignedSummary = new AlignmentsTableFile("blob.txt");
        //p.parseFile("/Users/leggettr/Documents/Projects/Nanopore/N79681_EvenMC_R7_06082014/last/2D/N79681_EvenMC_R7_0608215_5314_1_ch319_file116_strand.fast5_BaseCalled_2D.fasta.maf", nonAlignedSummary, overallStats);
        //System.exit(0);
    }
    
    private static void analyse(NanoOKOptions options) throws InterruptedException {
        OverallStats overallStats = new OverallStats(options);

        // Load references
        System.out.println("Reading references");
        
        // Load reference data
        options.getReferences().loadReferences();
        options.setReadFormat(options.getParser().getReadFormat());
        
        System.out.println("");
        
        // Parse all reads sets       
        if (options.doParseAlignments()) {
            ReadLengthsSummaryFile summary = new ReadLengthsSummaryFile(options.getLengthSummaryFilename());
            summary.open(options.getSample());
            
            for (int type = 0; type<3; type++) {
                if (options.isProcessingReadType(type)) {
                    System.out.println("Parsing " + NanoOKOptions.getTypeFromInt(type));
                    ReadSet readSet = new ReadSet(type, options, overallStats.getStatsByType(type));
                    int nReads = readSet.processReads();

                    if (nReads < 1) {
                        System.out.println("Error: unable to find any reads to process.");
                        System.out.println("");
                        System.exit(1);
                    }

                    int nReadsWithAlignments = readSet.getStats().getNumberOfReadsWithAlignments();
                    if (nReadsWithAlignments < 1) {
                        System.out.println("Error: unable to find any alignments to process.");
                        System.out.println("");
                        System.exit(1);
                    } else if (nReadsWithAlignments < 400) {
                        System.out.println("Warning: few alignments ("+nReadsWithAlignments+") found to process.");
                    }

                    summary.addReadSetStats(overallStats.getStatsByType(type));
                    overallStats.getStatsByType(type).closeKmersFile();
                    System.out.println("");
                }
            }
            summary.close();            
            
            // Write files
            System.out.println("Writing analysis files");
            Set<String> ids = options.getReferences().getAllIds();
            int allCount = 3; //ids.size() * 3;
            int counter = 1;            
            for (int type=0; type<3; type++) {
                long completed = counter;
                long total = allCount;
                long e = 50 * completed / total;
                long s = 50 - e;    
                System.out.print("\r[");
                for (int i=0; i<e; i++) {
                    System.out.print("=");
                }
                for (int i=0; i<s; i++) {
                    System.out.print(" ");
                }
                System.out.print("] " + completed +"/" +  total);
                options.getReferences().writeReferenceStatFiles(type);
                options.getReferences().writeReferenceSummary(type);
                counter++;
            }
            System.out.println("");
        }
        
        // Plot graphs
        if (options.doPlotGraphs()) {
            System.out.println("");
            System.out.println("Plotting graphs");
            RGraphPlotter plotter = new RGraphPlotter(options);
            plotter.plot();                
        }
        
        // Make report
        if (options.doMakeReport()) {
            System.out.println("");
            System.out.println("Making report");
            ReportWriter rw = new ReportWriter(options, overallStats);
            rw.writeReport();

            if (options.doMakePDF()) {
                System.out.println("");
                System.out.println("Making PDF");
                rw.makePDF();
            }
        }
                
        System.out.println("");
        System.out.println("Done");
    }
    
    private static void extract(NanoOKOptions options) throws InterruptedException {
        ReadExtractor re = new ReadExtractor(options);
        re.createDirectories();
        re.extract();
    }
    
    private static void align(NanoOKOptions options) throws InterruptedException {
        AlignmentFileParser parser = options.getParser();
        ReadAligner aligner = new ReadAligner(options, parser);
        options.setReadFormat(parser.getReadFormat());
        aligner.createDirectories();
        aligner.align();
    }
    
    /**
     * Entry to tool.
     * @param args command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        System.out.println("");
        System.out.println("NanoOK " + VERSION_STRING);
        System.out.println("");

        NanoOKOptions options = new NanoOKOptions();
               
        Locale.setDefault(new Locale("en", "US"));
        
        // Parse command line
        options.parseArgs(args);
        options.checkDirectoryStructure();

        // Check dependencies
        System.out.println("");
        System.out.println("Checking dependencies");
        checkDependencies();
        
        if (options.getRunMode() == NanoOKOptions.MODE_EXTRACT) {
            extract(options);
        } else if (options.getRunMode() == NanoOKOptions.MODE_ALIGN) {
            align(options);
        } else if (options.getRunMode() == NanoOKOptions.MODE_ANALYSE) {
            analyse(options);
        }
        
        options.getLog().close();
    }
}
