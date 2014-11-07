package nanotools;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ReportWriter {
    private NanoOKOptions options;
    private References references;
    private PrintWriter pw;
    private String sample;
    
    public ReportWriter(NanoOKOptions o, References r) {
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
        pw.println("\\subsection*{" + id + " error analysis}");
        pw.println("\\vspace{-3mm}");
        pw.println("\\begin{table}[H]");
        pw.println("{\\footnotesize");
        pw.println("\\fontsize{9pt}{11pt}\\selectfont");
        pw.println("\\begin{tabular}{l c c}");       
        
        pw.printf("Overall (all read) identity & %.2f\\%% \\\\", refSeq.getReadPercentIdentical());                
        pw.printf("Identical bases per 100 aligned bases & %.2f\\%% \\\\", refSeq.getAlignedPercentIdentical());
        pw.printf("Inserted bases per 100 aligned bases & %.2f\\%% \\\\", refSeq.getPercentInsertionErrors());
        pw.printf("Deleted bases per 100 aligned bases & %.2f\\%% \\\\", refSeq.getPercentDeletionErrors());
        pw.printf("Substitutions per 100 aligned bases & %.2f\\%% \\\\", refSeq.getPercentSubstitutionErrors());
        pw.println("\\end{tabular}");
        pw.println("}");
        pw.println("\\end{table}");        
        
        pw.println("\\subsection*{" + id + " coverage}");
        pw.println("\\vspace{-3mm}");
        pw.println("\\begin{figure}[H]");
        pw.println("\\centering");
        pw.println("\\includegraphics[width=.5\\linewidth]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName()+ "_Template_coverage.pdf}");
        pw.println("\\includegraphics[width=.5\\linewidth]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_Complement_coverage.pdf}");
        pw.println("\\includegraphics[width=.5\\linewidth]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_2D_coverage.pdf}");
        pw.println("\\end{figure}");

        pw.println("\\subsection*{" + id + " perfect kmers}");
        pw.println("\\vspace{-3mm}");
        pw.println("\\begin{figure}[H]");
        pw.println("\\centering");
        pw.println("\\includegraphics[width=.3\\linewidth]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_Template_cumulative_perfect_kmers.pdf}");
        pw.println("\\includegraphics[width=.3\\linewidth]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_Complement_cumulative_perfect_kmers.pdf}");
        pw.println("\\includegraphics[width=.3\\linewidth]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_2D_cumulative_perfect_kmers.pdf}");        
        pw.println("\\includegraphics[width=.3\\linewidth]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_Template_best_perfect_kmers.pdf}");
        pw.println("\\includegraphics[width=.3\\linewidth]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_Complement_best_perfect_kmers.pdf}");
        pw.println("\\includegraphics[width=.3\\linewidth]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_2D_best_perfect_kmers.pdf}");
        pw.println("\\end{figure}");
    }
    
    public void addReferencePlots(References refs) {
        List<String> keys = new ArrayList<String>(refs.getAllIds());
        Collections.sort(keys);
        for (String id : keys) {
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
