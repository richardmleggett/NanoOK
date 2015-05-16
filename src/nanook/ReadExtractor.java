package nanook;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author leggettr
 */
public class ReadExtractor {
    public final static String TYPE_STRING_TEMPLATE = "/Analyses/Basecall_2D_000/BaseCalled_template/Fastq";
    public final static String TYPE_STRING_COMPLEMENT = "/Analyses/Basecall_2D_000/BaseCalled_complement/Fastq";
    public final static String TYPE_STRING_2D = "/Analyses/Basecall_2D_000/BaseCalled_2D/Fastq";
    private String[] typeStrings = {TYPE_STRING_TEMPLATE, TYPE_STRING_COMPLEMENT, TYPE_STRING_2D};
    private NanoOKOptions options;
    
    /**
     * Constructor
     * @param o program options
     */
    public ReadExtractor(NanoOKOptions o) {    
        options = o;
    }
    
    /**
     * Create directories for output
     */
    public void createDirectories() {
        File f = new File(options.getReadDir());
        if (f.exists()) {
            if (!f.isDirectory()) {
                System.out.println("Error: "+options.getReadDir()+" is a file, not a directory!");
                System.exit(1);
            }
        } else {
            System.out.println("Making directory "+options.getReadDir());
            f.mkdir();
        }
        
    }
            
    /**
     * Check which types - Template, Complement, 2D - are available for this read
     * @param filename filename of fast5 file
     * @return boolean area of flags for Template, Complement, 2D
     */
    private boolean[] checkTypesAvailable(String filename) {
        boolean[] typesAvailable = new boolean[3];
        ProcessLogger pl = new ProcessLogger();
        ArrayList<String> response;
        response = pl.getCommandOutput("h5dump -n "+filename, true, true);
        for (int i=0; i<response.size(); i++) {
            String s = response.get(i);
            if (s.contains(TYPE_STRING_TEMPLATE)) {
                typesAvailable[NanoOKOptions.TYPE_TEMPLATE] = true;
                //System.out.println("Got template");
            } else if (s.contains(TYPE_STRING_COMPLEMENT)) {
                typesAvailable[NanoOKOptions.TYPE_COMPLEMENT] = true;
                //System.out.println("Got complement");
            } else if (s.contains(TYPE_STRING_2D)) {
                typesAvailable[NanoOKOptions.TYPE_2D] = true;
                //System.out.println("Got 2D");
            }
        }
                
        return typesAvailable;
    }
    
    /**
     * Write a FASTA file
     * @param id
     * @param seq
     * @param filename 
     */
    private void writeFastaFile(String id, String seq, String filename) {
        PrintWriter pw;
        //System.out.println("Writing " + filename);
        
        try {
            pw = new PrintWriter(new FileWriter(filename));
            pw.printf(">%s\n", id);
            pw.printf("%s\n", seq);
            pw.close();            
        } catch (IOException e) {
            System.out.println("writeFastaFile exception");
            e.printStackTrace();
        }
    }
    
    private void writeFastqFile(String id, String seq, String qual, String filename) {
        PrintWriter pw;
        //System.out.println("Writing " + filename);
        
        try {
            pw = new PrintWriter(new FileWriter(filename));
            pw.printf("@%s\n", id);
            pw.printf("%s\n", seq);
            pw.printf("+\n");
            pw.printf("%s\n", qual);
            pw.close();            
        } catch (IOException e) {
            System.out.println("writeFastaFile exception");
            e.printStackTrace();
        }
    }

    
    /**
     * Dump an individual read
     * @param inputFilename filename of FAST5 file
     * @param type type of read
     */
    private void dumpRead(String inputFilename, int type, String outputDir) {
        ProcessLogger pl = new ProcessLogger();
        ArrayList<String> response = pl.getCommandOutput("h5dump -d "+typeStrings[type]+" "+inputFilename, true, true);
        String outName = new File(inputFilename).getName();
                
        // Look for start of FASTQ section
        int l;
        for (l=0; l<response.size(); l++) {
            if (response.get(l).contains("\"@")) {
                break;
            }
        }
        
        // Parse FASTQ portion with regex
        if (l < response.size()) {
            String id = null;
            String seq = null;
            String qual = null;
            
            // Header row
            Pattern outPattern = Pattern.compile("@(.+)");
            Matcher outMatcher = outPattern.matcher(response.get(l));
            if (outMatcher.find()) {
                id = outMatcher.group(1);
            }
            
            // Sequence
            outPattern = Pattern.compile("(\\s*)(\\S+)");
            outMatcher = outPattern.matcher(response.get(l+1));
            if (outMatcher.find()) {
                seq = outMatcher.group(2);
            }
            
            // Qualities
            outPattern = Pattern.compile("(\\s*)(\\S+)");
            outMatcher = outPattern.matcher(response.get(l+3));
            if (outMatcher.find()) {
                qual = outMatcher.group(2);
            }
            
            // Fix IDs
            outPattern = Pattern.compile("00000000-0000-0000-0000-000000000000(.+)");
            outMatcher = outPattern.matcher(id);
            if (outMatcher.find()) {
                if (options.fixIDs()) {
                    id = id.replaceAll("^00000000-0000-0000-0000-000000000000_", "");
                    id = id.replaceAll(" ", "");
                } else {
                    System.out.println("Warning: " + id + " is non-unqiue. Recommend re-running with -fixids option.\\n");
                }
            }
            
            if (id != null) {
                if (options.getReadFormat() == NanoOKOptions.FASTA) {
                    writeFastaFile(id, seq, outputDir + File.separator + NanoOKOptions.getTypeFromInt(type) + File.separator + outName + "_BaseCalled_" + NanoOKOptions.getTypeFromInt(type) + ".fasta");
                } else if (options.getReadFormat() == NanoOKOptions.FASTQ) {
                    writeFastqFile(id, seq, qual, outputDir + File.separator + NanoOKOptions.getTypeFromInt(type) + File.separator + outName + "_BaseCalled_" + NanoOKOptions.getTypeFromInt(type) + ".fastq");
                }
            }
            
        } else {
            System.out.println("Error: couldn't find payload in " + inputFilename);
        }
    }

    /**
     * Extract reads of each type from file
     * @param inDir input directory
     * @param filename filename
     * @param outDir output directory
     */
    private void extractFile(String inDir, String filename, String outDir) {
        String inputPathname = inDir + File.separator + filename;
        
        System.out.println("Extracting " + inputPathname);

        boolean[] typesAvailable = checkTypesAvailable(inputPathname);        
        
        for (int t=0; t<3; t++) {
            if (typesAvailable[t]) {
                dumpRead(inputPathname, t, outDir);
            }
        }
    }

    /**
     * Process a directory and extract reads
     * @param inputDirName input directory name
     * @param outputDirName output directory name
     */
    private void processDirectory(String inputDirName, String outputDirName) {
        File f = new File(outputDirName);
        
        // Make directory
        if (! f.exists()) {
            f.mkdir();
        }
        
        // Make output Template, Complement and 2D directories
        for (int t=0; t<3; t++) {
            if (options.isProcessingReadType(t)) {
                f = new File(outputDirName + File.separator + NanoOKOptions.getTypeFromInt(t));
                if (! f.exists()) {
                    f.mkdir();
                }
            }
        }
        
        File inputDir = new File(inputDirName);
        File[] listOfFiles = inputDir.listFiles();
        
        if (listOfFiles == null) {
            System.out.println("Directory "+inputDirName+" doesn't exist");
        } else if (listOfFiles.length <= 0) {
            System.out.println("Directory "+inputDirName+" empty");
        } else {
            System.out.println("\nProcessing from "+inputDirName);

            for (File file : listOfFiles) {
                if (file.isFile()) {
                    if (file.getName().endsWith(".fast5")) {
                        extractFile(inputDirName, file.getName(), outputDirName);    
                        //System.exit(1);
                    }
                }
            }            
        }    
    }

    /**
     * Extract reads
     */
    public void extract() {
        if (options.isNewStyleDir()) {
            if (options.isProcessingPassReads()) {
                processDirectory(options.getFast5Dir() + File.separator + "pass",
                                 options.getReadDir() + File.separator + "pass");
            }
            
            if (options.isProcessingFailReads()) {
                processDirectory(options.getFast5Dir() + File.separator + "fail",
                                 options.getReadDir() + File.separator + "fail");
            }
        } else {
            processDirectory(options.getFast5Dir(), options.getReadDir());
        }
        
        System.out.println("DONE");
    }    
}


