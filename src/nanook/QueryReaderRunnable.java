package nanook;

/**
 *
 * @author leggettr
 */
public class QueryReaderRunnable implements Runnable
{
    NanoOKOptions options;
    String pathname;
    ReadSetStats stats;
    int type;

    public QueryReaderRunnable(NanoOKOptions o, ReadSetStats s, String pn, int t) {
        options = o;
        pathname = pn;
        stats = s;
        type = t;
    }

    /**
     * Parse a FASTA or FASTQ file, noting length of reads etc.
     * @param filename filename of FASTA file
     */
    private void readQueryFile(String filename) {
        SequenceReader sr = new SequenceReader(false);
        int nReadsInFile;
        
        if (options.getReadFormat() == NanoOKOptions.FASTQ) {
            nReadsInFile = sr.indexFASTQFile(filename);
        } else {
            nReadsInFile = sr.indexFASTAFile(filename, null, true);
        }

        if (nReadsInFile > 1) {
            System.out.println("Warning: File "+filename+" has more than 1 read.");
        }

        for (int i=0; i<sr.getSequenceCount(); i++) {
            String id = sr.getID(i);
            
            if (id.startsWith("00000000-0000-0000-0000-000000000000")) {
                System.out.println("Error:");
                System.out.println(filename);
                System.out.println("The reads in this file do not have unique IDs because they were generated when MinKNOW was producing UUIDs, but Metrichor was not using them. To fix, run nanook_extract_reads with the -fixids option.");
                System.exit(1);
            }
            
            stats.addLength(id, sr.getLength(i));
        }
    }

    public void run() {        
        readQueryFile(pathname);
        stats.addReadFile(type);
    }
}
