package nanotools;

import java.io.*;
import java.util.Set;

public class ReportWriter {
    private NanotoolsOptions options;
    private References references;
    private PrintWriter pw;
    private String sample;
    
    public ReportWriter(NanotoolsOptions o, References r) {
        options = o;
        references = r;
        sample = o.getSample().replaceAll("_", "\\\\_");
    }
    
    public void open() {
        try {
            pw = new PrintWriter(new FileWriter(options.getTexFilename())); 
            writeLaTeXHeader();
        } catch (IOException e) {
            System.out.println("ReportWriter exception:");
            e.printStackTrace();
            System.exit(1);
        }        
    }
    
    public void close() {
        writeLaTeXFooter();
        pw.close();
    }
    
    private void writeLaTeXHeader() {
        pw.println("\\documentclass[a4paper,11pt,oneside]{article}");
        pw.println("\\usepackage{graphicx}");
        pw.println("\\usepackage{url}");
        pw.println("\\usepackage{subcaption}");
        pw.println("\\usepackage{rotating}");
        pw.println("\\usepackage{color}");
        pw.println("\\usepackage[compact]{titlesec}");
        pw.println("\\usepackage[portrait,top=1cm, bottom=2cm, left=1cm, right=1cm]{geometry}");
        pw.println("\\usepackage{float}");
        pw.println("\\restylefloat{table}");
        pw.println("\\begin{document}");
        pw.println("\\renewcommand*{\\familydefault}{\\sfdefault}");
        pw.println("\\section*{\\large{Nanotools report for " + sample + "}}");
    }
    
    public void beginLengthsSection() {
        pw.println("\\subsection*{Read lengths}");
        pw.println("\\vspace{-3mm}");
        pw.println("\\begin{table}[H]");
        pw.println("{\\footnotesize");
        pw.println("\\fontsize{9pt}{11pt}\\selectfont");
        pw.println("\\begin{tabular}{l c c c c c c c c c}");
        pw.println("{\\bf Type} & {\\bf NumReads} & {\\bf TotalBases} & {\\bf Mean} & {\\bf Longest} & {\\bf Shortest} & {\\bf N50} & {\\bf N50Count} & {\\bf N90} & {\\bf N90Count} \\\\");
    }
    
    public void addReadSet(ReadSet r) {
        pw.printf("%s & %d & %d & %.2f & %d & %d & %d & %d & %d & %d \\\\\n", r.getType(), r.getNumReads(), r.getTotalBases(), r.getMeanLength(), r.getLongest(), r.getShortest(), r.getN50(), r.getN50Count(), r.getN90(), r.getN90Count());
    }
    
    public void endLengthsSection() {       
        pw.println("\\end{tabular}");
        pw.println("}");
        pw.println("\\end{table}");

        pw.println("\\begin{figure}[H]");
        pw.println("\\centering");
        pw.println("\\includegraphics[width=.45\\linewidth]{" + options.getGraphsDir() + options.getSeparator() + "all_Template_lengths.pdf}");
        pw.println("\\includegraphics[width=.45\\linewidth]{" + options.getGraphsDir() + options.getSeparator() + "all_Complement_lengths.pdf}");
        pw.println("\\includegraphics[width=.45\\linewidth]{" + options.getGraphsDir() + options.getSeparator() + "all_2D_lengths.pdf}");
        pw.println("\\end{figure}");
    }
    
    public void beginAlignmentsSection(OverallAlignmentStats stats) {
        if ((stats.getType() == "Template") || (references.getNumberOfReferences() > 8)) {
            pw.println("\\clearpage");
        }
        pw.println("\\subsection*{" + stats.getType() + " alignments}");
        pw.println("\\vspace{-3mm}");
        pw.println("\\begin{table}[H]");
        pw.println("{\\footnotesize");
        pw.println("\\fontsize{9pt}{11pt}\\selectfont");
        pw.println("\\begin{tabular}{l c c}");
        pw.println("Number of reads & " + stats.getNumberOfReads() + " & \\\\");
        pw.printf("Number of reads with alignments & %d & (%.2f\\%%) \\\\", stats.getNumberOfReadsWithAlignments(), stats.getPercentOfReadsWithAlignments());
        pw.printf("Number of reads without alignments & %d & (%.2f\\%%) \\\\", stats.getNumberOfReadsWithoutAlignments(), stats.getPercentOfReadsWithoutAlignments());
        pw.println("\\end{tabular}");
        pw.println("}");
        pw.println("\\end{table}");
    }
    
    public void beginReferenceSection(ReferenceSequence refSeq) {
        String id = refSeq.getName().replaceAll("_", " ");
        
        pw.println("\\clearpage");
        pw.println("\\subsection*{" + id + " coverage}");
        pw.println("\\vspace{-3mm}");
        pw.println("\\begin{figure}[H]");
        pw.println("\\centering");
        pw.println("\\includegraphics[width=.8\\linewidth]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName()+ "_Template_coverage.pdf}");
        pw.println("\\includegraphics[width=.8\\linewidth]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_Complement_coverage.pdf}");
        pw.println("\\includegraphics[width=.8\\linewidth]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_2D_coverage.pdf}");
        pw.println("\\end{figure}");

        pw.println("\\clearpage");
        pw.println("\\subsection*{" + id + " perfect kmers}");
        pw.println("\\vspace{-3mm}");
        pw.println("\\begin{figure}[H]");
        pw.println("\\centering");
        pw.println("\\includegraphics[width=.45\\linewidth]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_Template_cumulative_perfect_kmers.pdf}");
        pw.println("\\includegraphics[width=.45\\linewidth]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_Template_best_perfect_kmers.pdf}");
        pw.println("\\includegraphics[width=.45\\linewidth]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_Complement_cumulative_perfect_kmers.pdf}");
        pw.println("\\includegraphics[width=.45\\linewidth]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_Complement_best_perfect_kmers.pdf}");
        pw.println("\\includegraphics[width=.45\\linewidth]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_2D_cumulative_perfect_kmers.pdf}");        
        pw.println("\\includegraphics[width=.45\\linewidth]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_2D_best_perfect_kmers.pdf}");
        pw.println("\\end{figure}");
    }
    
    public void addReferencePlots(References refs) {
        Set<String> ids = refs.getAllIds();
        for (String id : ids) {
            beginReferenceSection(refs.getReferenceById(id));
        }
    }
    
    private void writeLaTeXFooter() {
        pw.println("\\end{document}");
    }
    
    public PrintWriter getPrintWriter() {
        return pw;
    }
}
