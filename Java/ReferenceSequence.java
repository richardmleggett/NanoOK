package nanotools;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ReferenceSequence {
    private String id = null;
    private String name = null;
    private int size = 0;
    private ReferenceSequenceStats referenceStats[] = new ReferenceSequenceStats[3];
    
    public ReferenceSequence(String i, int s, String n) {
        id = i;
        size = s;
        name = n;
        
        System.out.println("Got reference "+n);
        for (int t=0; t<3; t++) {
            referenceStats[t] = new ReferenceSequenceStats(size, name);
        }
    }
    
    public ReferenceSequenceStats getStatsByType(int t) {
        return referenceStats[t];
    }
    
    public String getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
    
    public int getSize() {
        return size;
    }
}
