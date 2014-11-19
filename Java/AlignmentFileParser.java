package nanotools;

public abstract class AlignmentFileParser {    
    abstract int parseFile(String filename, AlignmentsTableFile summaryFile);
}
