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
        String graphSize = "height=5.2cm";
        int type = options.getSpecifiedType();
        
        pw.println("\\subsection*{Read lengths}");
        includeGraphicsIfExists(type, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + NanoOKOptions.getTypeFromInt(type)+"_lengths", "} \\\\");

        pw.println("\\vspace{-3mm}");
        pw.println("\\subsection*{Number of reads}");                 
        includeGraphicsIfExists(type, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + NanoOKOptions.getTypeFromInt(type)+"_number_of_reads", "} \\\\");

        pw.println("\\vspace{-3mm}");
        pw.println("\\subsection*{Total bases}");                 
        includeGraphicsIfExists(type, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + NanoOKOptions.getTypeFromInt(type)+"_total_bases", "} \\\\");        

        pw.println("\\vspace{-3mm}");
        pw.println("\\subsection*{Alignment summary}");
        includeGraphicsIfExists(type, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + NanoOKOptions.getTypeFromInt(type)+"_maps", "} \\\\");
    }
    
    public void writeReferenceSection(ReferenceSequence refSeq) {
        String id = refSeq.getName().replaceAll("_", " ");
        String graphSize = "height=6cm";
        int type = options.getSpecifiedType();

        pw.println("\\clearpage");
        pw.println("\\subsection*{" + id + " identity}");
        includeGraphicsIfExists(type, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + "_" + NanoOKOptions.getTypeFromInt(type)+"_query_identity", "} \\\\");
        includeGraphicsIfExists(type, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + "_" + NanoOKOptions.getTypeFromInt(type)+"_query_identity_zoom", "} \\\\");
        includeGraphicsIfExists(type, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + "_" + NanoOKOptions.getTypeFromInt(type)+"_percent_query_aligned", "} \\\\");
        includeGraphicsIfExists(type, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + "_" + NanoOKOptions.getTypeFromInt(type)+"_percent_query_aligned_zoom", "} \\\\");

        pw.println("\\subsection*{" + id + " best perfect kmer}");
        includeGraphicsIfExists(type, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + "_" + NanoOKOptions.getTypeFromInt(type)+"_best_perfect_kmer", "} \\\\");

        pw.println("\\subsection*{" + id + " GC}");
        includeGraphicsIfExists(type, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + "_" + NanoOKOptions.getTypeFromInt(type)+"_query_gc", "} \\\\");

        for (int ou=0; ou<2; ou++) { 
            if (ou == 0) {
                pw.println("\\subsection*{" + id + " " + NanoOKOptions.getTypeFromInt(type) + " Over-represented 5-mers}");
            } else {
                pw.println("\\subsection*{" + id + " " + NanoOKOptions.getTypeFromInt(type) + " Under-represented 5-mers}");
            }
            
            pw.println("\\vspace{-3mm}");
            pw.println("\\begin{table}[H]");
            pw.println("{\\footnotesize");
            pw.println("\\fontsize{8pt}{10pt}\\selectfont");
            pw.println("\\tabcolsep=0.15cm");
            pw.println("\\begin{tabular}{|c|c c c c c c c c c c|}");
            pw.println("\\cline{1-11}");
            pw.println("Sample & 1 & 2 & 3 & 4 & 5 & 6 & 7 & 8 & 9 & 10 \\\\");
            pw.println("\\cline{1-11}");
            for (int i=0; i<sampleComparer.getNumberOfSamples(); i++) {
                OverallStats os = sampleComparer.getSample(i);
                ReferenceSequence rs = os.getStatsByType(type).getOptions().getReferences().getReferenceById(refSeq.getId());
                rs.getStatsByType(type).sortKmerAbundance();
                ArrayList<KmerAbundance> ka = rs.getStatsByType(type).getKmerAbundance();
                pw.print(sampleComparer.getSampleName(i).replaceAll("_", "\\\\_"));
                for (int j=0; j<10; j++) {
                    KmerAbundance ko;
                    
                    if (ou == 0) {
                        ko = ka.get(j);
                    } else {
                        ko = ka.get(ka.size() - 1 - j);
                    }
                    pw.print(" & "+ko.getKmer());
                }
                pw.println(" \\\\");            
            }
            pw.println("\\cline{1-11}");
            pw.println("\\end{tabular}");
            pw.println("}");
            pw.println("\\end{table}");                          
        }
    }
    
    private void writeReferenceSection() {
        ArrayList<ReferenceSequence> sortedRefs = options.getReferences().getSortedReferences();
        for (int i=0; i<sortedRefs.size(); i++) {
            ReferenceSequence rs = sortedRefs.get(i);
            
            if ((options.debugMode() && (!rs.getName().equalsIgnoreCase("DNA_CS")))) {
                writeReferenceSection(rs);
            }
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
        String logFilename = options.getLogsDir() + File.separator + "pdflatex_output_log_comparison.txt";
        System.out.println("pdflatex output " + logFilename);
        pl.runAndLogCommand(command, logFilename, false);
    }
}
