package nanook;

import java.util.ArrayList;

/**
 * Represent a LastAlignment involving a query and a hit.
 * @author leggettr
 */
public class LastAlignment implements Comparable {
    int score = 0;
    LastAlignmentLine hitLine = null;
    LastAlignmentLine queryLine = null;

    public LastAlignment(int s, LastAlignmentLine hit, LastAlignmentLine query) {
        score = s;
        hitLine = hit;
        queryLine = query;
    }

    /**
     * Return the score for this alignment.
     * @return score
     */
    public int getScore() {
        return score;
    }
    
    /**
     * Get the hit line
     * @return hit line, as LastAlignmentLine
     */
    public LastAlignmentLine getHitLine() {
        return hitLine;
    }
    
    /**
     * Get the query line
     * @return query line, as LastAlignmentLine
     */
    public LastAlignmentLine getQueryLine() {
        return queryLine;
    }

    @Override
    public int compareTo(Object o) {
        return ((LastAlignment)o).getScore() - score;
    }    
}

