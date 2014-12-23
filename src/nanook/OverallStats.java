package nanook;

/**
 * Represents overall (as opposed to per reference) stats for Template, Complement and 2D reads.
 * 
 * @author Richard Leggett
 */
public class OverallStats {
    private ReadSetStats[] readStats = new ReadSetStats[3];
    
    /**
     * Constructor.
     * @param o NanoOKOptions structure
     */
    public OverallStats(NanoOKOptions o) {
        for (int t=0; t<3; t++) {
            readStats[t] = new ReadSetStats(o, t);
        }
    }
    
    /**
     * Get a set of stats (for either Template, Complement or 2D reads)
     * @param t integer type - see defs in NanoOKOptions
     * @return ReadSetStats object
     */
    public ReadSetStats getStatsByType(int t) {
        return readStats[t];
    }
}
