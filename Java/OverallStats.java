package nanotools;

public class OverallStats {
    private ReadSetStats[] readStats = new ReadSetStats[3];
    
    public OverallStats(NanoOKOptions o) {
        for (int t=0; t<3; t++) {
            readStats[t] = new ReadSetStats(o, t);
        }
    }
    
    public ReadSetStats getStatsByType(int t) {
        return readStats[t];
    }
}
