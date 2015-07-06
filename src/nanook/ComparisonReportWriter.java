package nanook;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 *
 * @author Richard Leggett
 */
public class ComparisonReportWriter {
    private NanoOKOptions options;
    private PrintWriter pw = null;
    private SampleComparer sampleComparer = null;
    
    public ComparisonReportWriter(NanoOKOptions o, SampleComparer sc) {
        options = o;
        sampleComparer = sc;
    }

    /**
     * Check if graphic file exists and only insert if it does
     * @param preTex LaTeX before filename
     * @param filename the file
     * @param postTex LaTeX after filename
     */
    private void includeGraphicsIfExists(int type, String preTex, String filename, String postTex) {
        if (options.isProcessingReadType(type)) {
            String fullFilename = filename + "." + options.getImageFormat();
            File f = new File(fullFilename);
            
            if (f.exists()) {
                pw.print(preTex);
                pw.print(fullFilename);
                pw.println(postTex);            
            } else {
                pw.print(" ");
            }
        }
    }    
    
    /**
     * Open the .tex file.
     */
    public void open() {
        try {
            pw = new PrintWriter(new FileWriter(options.getLatexDir() + File.separator + "comparison.tex")); 
            writeLaTeXHeader();
        } catch (IOException e) {
            System.out.println("ReportWriter exception:");
            e.printStackTrace();
            System.exit(1);
        }        
    }
    
    /**
     * Write end of LaTeX file.
     */
    private void writeLaTeXFooter() {
        pw.println("\\end{document}");
    }
    
    /**
     * Close the .tex file.
     */
    public void close() {
        writeLaTeXFooter();
        pw.close();
    }
    
    /**
     * Write the top of the LaTeX document.
     */
    private void writeLaTeXHeader() {
        pw.println("\\documentclass[a4paper,11pt,oneside]{article}");
        pw.println("\\usepackage{graphicx}");
        pw.println("\\usepackage{url}");
        pw.println("\\usepackage{multirow}");
        pw.println("\\usepackage{rotating}");
        pw.println("\\usepackage{color}");
        pw.println("\\usepackage[compact]{titlesec}");
        pw.println("\\usepackage[portrait,top=1cm, bottom=2cm, left=1cm, right=1cm]{geometry}");
        pw.println("\\usepackage{float}");
        pw.println("\\restylefloat{table}");
        pw.println("\\begin{document}");
        pw.println("\\renewcommand*{\\familydefault}{\\sfdefault}");
        pw.println("\\section*{\\large{NanoOK comparison report}}");
    }
    
    private void writeLengthSection() {
        String graphSize = "height=6cm";
        
        //if (options.getNumberOfTypes() == 1) {
        //    graphSize = "width=.4\\linewidth";
        //}

        pw.println("\\subsection*{Read lengths}");
        //pw.println("\\vspace{-3mm}");
                        
        for (int type=0; type<3; type++) {
            if (options.isProcessingReadType(type)) {
                includeGraphicsIfExists(type, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + NanoOKOptions.getTypeFromInt(type)+"_lengths", "} \\\\");
            }
        }        
    }
    
    public void writeReferenceSection(ReferenceSequence refSeq) {
        String id = refSeq.getName().replaceAll("_", " ");
        String graphSize = "height=6cm";

        pw.println("\\clearpage");
        pw.println("\\subsection*{" + id + " identity}");
        for (int type=0; type<3; type++) {
            if (options.isProcessingReadType(type)) {
                includeGraphicsIfExists(type, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + "_" + NanoOKOptions.getTypeFromInt(type)+"_query_identity", "} \\\\");
            }
        }        

        for (int type=0; type<3; type++) {
            if (options.isProcessingReadType(type)) {
                includeGraphicsIfExists(type, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + "_" + NanoOKOptions.getTypeFromInt(type)+"_percent_query_aligned", "} \\\\");
            }
        }        

        pw.println("\\subsection*{" + id + " best perfect kmer}");
        for (int type=0; type<3; type++) {
            if (options.isProcessingReadType(type)) {
                includeGraphicsIfExists(type, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + "_" + NanoOKOptions.getTypeFromInt(type)+"_best_perfect_kmer", "} \\\\");
            }
        }            
    }
    
    private void writeReferenceSection() {
        ArrayList<ReferenceSequence> sortedRefs = options.getReferences().getSortedReferences();
        for (int i=0; i<sortedRefs.size(); i++) {
            ReferenceSequence rs = sortedRefs.get(i);
            writeReferenceSection(rs);
        }
    }
    
    public void writeReport() {
        open();
        writeLengthSection();
        writeReferenceSection();
        close();
    }

    public void makePDF() {
        ProcessLogger pl = new ProcessLogger();
        String command = "pdflatex -interaction=nonstopmode -output-directory " + options.getLatexDir() + " " + options.getLatexDir() + File.separator + "comparison.tex";
        String logFilename = options.getLogsDir() + File.separator + "pdflatex_output_log.txt";
        System.out.println("pdflatex output " + logFilename);
        pl.runAndLogCommand(command, logFilename, false);
    }
}
