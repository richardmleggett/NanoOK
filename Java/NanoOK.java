package nanotools;

import java.util.Set;

public class NanoOK {
    public static void main(String[] args) {
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
        References references = new References(options);
                
        // Parse all reads sets       
        ReadLengthsSummaryFile summary = new ReadLengthsSummaryFile(options.getLengthSummaryFilename());
        summary.open(options.getSample());
        for (int type = 0; type<3; type++) {
            AlignmentFileParser parser = new LastParser(type, options, overallStats.getStatsByType(type), references);
            ReadSet readSet = new ReadSet(type, options, references, parser, overallStats.getStatsByType(type));
            readSet.parseFiles();
            readSet.gatherLengthStats();
            summary.addReadSetStats(overallStats.getStatsByType(type));
        }
        summary.close();

        // Write files
        Set<String> ids = references.getAllIds();
        for (String id : ids) {
            for (int type=0; type<3; type++) {
                references.writeReferenceStatFiles(type);
                references.writeReferenceSummary(type);
            }
        }
        
        references.closeAlignmentFiles();
        
        // Plot graphs
        RGraphPlotter plotter = new RGraphPlotter(options);
        plotter.plot(references);                
        options.initialiseAlignmentSummaryFile();

        ReportWriter rw = new ReportWriter(options, references, overallStats);
        rw.writeReport();
        
        
//        reportWriter.addReferencePlots(references);      
//        reportWriter.close();
        /*
        if (options.getProgram().equals("readstats")) {
            ReadLengthsSummaryFile summary = new ReadLengthsSummaryFile(options.getLengthSummaryFilename());
            summary.open();
            for (int type = 0; type<3; type++) {
                ReadSetAnalysis set = new ReadSetAnalysis(options);
                set.gatherLengthStats(type);
                summary.addReadSet(set);
            }
            summary.close();
        } else if (options.getProgram().equals("parselast")) {
            options.initialiseAlignmentSummaryFile();
            References references = new References(options);
            OverallAlignmentStats stats = new OverallAlignmentStats();
            LastParser parser = new LastParser(options, stats, references);
            parser.parseAll();
        } else if (options.getProgram().equals("plot")) {
            References references = new References(options);
            RGraphPlotter plotter = new RGraphPlotter(options);
            plotter.plot(references);
        }
        */
        
//        stats.outputMotifStats();
    }
}
