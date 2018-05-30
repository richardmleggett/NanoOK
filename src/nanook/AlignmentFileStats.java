package nanook;

import java.io.File;

public class AlignmentFileStats {
    private String alignmentPathname;
    private int nAlignments = 0;
    
    public AlignmentFileStats(String p) {
        alignmentPathname = p;
    }
    
    public void markNoAlignments() {
        nAlignments = 0;
    }
       
    public void legacyActions(AlignmentsTableFile nonAlignedSummaryFile, ReadSetStats overallStats) {
        if (nAlignments == 0) {
            String leafName = new File(alignmentPathname).getName();
            nonAlignedSummaryFile.writeNoAlignmentMessage(leafName);
            overallStats.addReadWithoutAlignment();
        }
    }
}
