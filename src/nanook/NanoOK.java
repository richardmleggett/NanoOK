package nanook;

import java.util.Set;

/**
 * Entry class for tool.
 * 
 * @author Richard Leggett
 */
public class NanoOK {
    public final static String VERSION_STRING = "v0.3.2";
    
    /**
     * Entry to tool.
     * @param args command line arguments
     */
    public static void main(String[] args) {
        System.out.println("\nNanoOK " + VERSION_STRING + "\n");
        NanoOKOptions options = new NanoOKOptions();
        OverallStats overallStats = new OverallStats(options);

        //SequenceLogo logo = new SequenceLogo();
        //logo.drawImage();
        //logo.saveImage("/Users/leggettr/Desktop/logo.png");
        //System.exit(0);
        
        // Parse command line
        options.parseArgs(args);
        options.checkDirectoryStructure();

        // Load references
        System.out.println("\nFinding references");
        References references = new References(options);
                
        //AlignmentFileParser p = new LastParser(0, options, overallStats.getStatsByType(0), references);
        //AlignmentsTableFile nonAlignedSummary = new AlignmentsTableFile("blob.txt");
        //p.parseFile("/Users/leggettr/Documents/Projects/Nanopore/N79681_EvenMC_R7_06082014/last/2D/N79681_EvenMC_R7_0608215_5314_1_ch319_file116_strand.fast5_BaseCalled_2D.fasta.maf", nonAlignedSummary);
        //System.exit(0);
        
        // Parse all reads sets       
        if (options.doParseAlignments()) {
            ReadLengthsSummaryFile summary = new ReadLengthsSummaryFile(options.getLengthSummaryFilename());
            summary.open(options.getSample());
            for (int type = 0; type<3; type++) {
                AlignmentFileParser parser = new LastParser(type, options, overallStats.getStatsByType(type), references);
                ReadSet readSet = new ReadSet(type, options, references, parser, overallStats.getStatsByType(type));
                readSet.gatherLengthStats();
                readSet.parseAlignmentFiles();
                summary.addReadSetStats(overallStats.getStatsByType(type));
                overallStats.getStatsByType(type).closeKmersFile();
            }
            summary.close();

            // Write files
            System.out.println("\nWriting analysis files");
            Set<String> ids = references.getAllIds();
            int allCount = ids.size() * 3;
            int counter = 1;
            
            for (String id : ids) {
                for (int type=0; type<3; type++) {
                    System.out.print("\r"+counter+"/"+allCount);
                    references.writeReferenceStatFiles(type);
                    references.writeReferenceSummary(type);
                    counter++;
                }
            }

            references.closeAlignmentFiles();
            System.out.println("");
        }
        
        // Plot graphs
        if (options.doPlotGraphs()) {
            System.out.println("\nPlotting graphs");
            RGraphPlotter plotter = new RGraphPlotter(options);
            plotter.plot(references);                
        }
        
        // Make report
        if (options.doMakeReport()) {
            System.out.println("\nMaking report");
            ReportWriter rw = new ReportWriter(options, references, overallStats);
            rw.writeReport();

            if (options.doMakePDF()) {
                System.out.println("\nMaking PDF");
                rw.makePDF();
            }
        }
                
        System.out.println("\nDone");
    }
}
