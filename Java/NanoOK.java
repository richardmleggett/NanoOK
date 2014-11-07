package nanotools;

public class NanoOK {
    public static void main(String[] args) {
        NanoOKOptions options = new NanoOKOptions();
        options.parseArgs(args);
        options.checkDirectoryStructure();

        References references = new References(options);
                
        ReportWriter rw = new ReportWriter(options, references);
        rw.open();
        
        ReadLengthsSummaryFile summary = new ReadLengthsSummaryFile(options.getLengthSummaryFilename());
        rw.beginLengthsSection();
        summary.open(options.getSample());
        for (int type = 0; type<3; type++) {
            ReadSet set = new ReadSet(options);
            set.gatherLengthStats(type);
            summary.addReadSet(set);
            rw.addReadSet(set);
        }
        summary.close();
        rw.endLengthsSection();

        options.initialiseAlignmentSummaryFile();
        OverallAlignmentStats stats = new OverallAlignmentStats();
        AlignerParser parser = new LastParser(options, stats, references, rw);
        parser.parseAll();

        RGraphPlotter plotter = new RGraphPlotter(options);
        plotter.plot(references);
        
        rw.addReferencePlots(references);
      
        rw.close();
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
    }
}
