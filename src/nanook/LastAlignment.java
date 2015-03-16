/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nanook;

import java.util.ArrayList;

public class LastAlignment implements Comparable {
    int score = 0;
    LastAlignmentLine hitLine = null;
    LastAlignmentLine queryLine = null;

    public LastAlignment(int s, LastAlignmentLine hit, LastAlignmentLine query) {
        score = s;
        hitLine = hit;
        queryLine = query;
    }

    public int getScore() {
        return score;
    }
    
    public LastAlignmentLine getHitLine() {
        return hitLine;
    }
    
    public LastAlignmentLine getQueryLine() {
        return queryLine;
    }

    @Override
    public int compareTo(Object o) {
        return ((LastAlignment)o).getScore() - score;
    }    
}

