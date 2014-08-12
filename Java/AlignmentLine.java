package nanotools;

public class AlignmentLine {
    private String name;
    private int start;
    private int alnSize;
    private String strand;
    private int seqSize;
    private String alignment;
    
    public AlignmentLine(String s) {
        String[] parts = s.split("\\s+");

        if (parts.length == 7) {
            name = parts[1];
            start = Integer.parseInt(parts[2]);
            alnSize = Integer.parseInt(parts[3]);
            strand = parts[4];
            seqSize = Integer.parseInt(parts[5]);
            alignment = parts[6];            
        } else {                
            System.out.println("Error: can't understand alignment file format.");
            System.exit(1);
        }
    }
    
    public String getName() {
        return name;
    }
    
    public int getStart() {
        return start;
    }
    
    public int getAlnSize() {
        return alnSize;
    }
    
    public String getStrand() {
        return strand;
    }
    
    public int getSeqSize() {
        return seqSize;
    }
    
    public String getAlignment() {
        return alignment;
    }
}
