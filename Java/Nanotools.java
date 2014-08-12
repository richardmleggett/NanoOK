package nanotools;

public class Nanotools {
    public static void main(String[] args) {
        NanotoolsOptions options = new NanotoolsOptions();        
        options.parseArgs(args);
        options.checkDirectoryStructure();

        ReadLengthsSummaryFile summary = new ReadLengthsSummaryFile(options.getLengthSummaryFilename());
        summary.open(options.getSample());
        for (int type = 0; type<3; type++) {
            ReadSetAnalysis set = new ReadSetAnalysis(options);
            set.gatherLengthStats(type);
            summary.addReadSet(set);
        }
        summary.close();

        options.initialiseAlignmentSummaryFile();
        References references = new References(options);
        OverallAlignmentStats stats = new OverallAlignmentStats();
        LastParser parser = new LastParser(options, stats, references);
        parser.parseAll();

        RGraphPlotter plotter = new RGraphPlotter(options);
        plotter.plot(references);
        
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
