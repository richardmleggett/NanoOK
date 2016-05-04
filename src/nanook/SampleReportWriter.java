/*
 * Program: NanoOK
 * Author:  Richard M. Leggett
 * 
 * Copyright 2015 The Genome Analysis Centre (TGAC)
 */

package nanook;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Writes a LaTeX report file.
 * 
 * @author Richard Leggett
 */
public class SampleReportWriter {
    private static final int LONGTABLE_THRESHOLD = 25;
    private NanoOKOptions options;
    private References references;
    private OverallStats overallStats;
    private PrintWriter pw;
    private String sample;
   
    /**
     * Constructor.
     * @param o a NanoOKOptions object
     * @param r the references
     * @param s overall statistics
     */
    public SampleReportWriter(NanoOKOptions o, OverallStats s) {
        options = o;
        references = options.getReferences();
        overallStats = s;
        sample = o.getSample().replaceAll("_", "\\\\_");
    }
    
    /**
     * Open the .tex file.
     */
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
        if (references.getNumberOfReferences() >= LONGTABLE_THRESHOLD) {
            pw.println("\\usepackage{longtable}");
        }
        pw.println("\\restylefloat{table}");
        pw.println("\\begin{document}");
        pw.println("\\renewcommand*{\\familydefault}{\\sfdefault}");
        pw.println("\\normalfont");
        pw.println("\\section*{\\large{NanoOK report for " + sample + "}}");
    }
    
    /**
     * Add the pass/fail section
     */
    public void addPassFailSection() {
        if (options.isNewStyleDir()) {
            pw.println("\\subsection*{Pass and fail counts}");
            pw.println("\\vspace{-3mm}");
            pw.println("\\begin{table}[H]");
            pw.println("{\\footnotesize");
            pw.println("\\fontsize{9pt}{11pt}\\selectfont");
            pw.println("\\begin{tabular}{l c c}");
            pw.println("{\\bf Type} & {\\bf Pass} & {\\bf Fail} \\\\");
    
            for (int type = 0; type<3; type++) {
                if (options.isProcessingReadType(type)) {
                    ReadSetStats r = overallStats.getStatsByType(type);           
                    pw.printf("%s & %d & %d  \\\\", r.getTypeString(), r.getNumberOfPassFiles(), r.getNumberOfFailFiles());
                    pw.println("");
                }
            }
            
            pw.println("\\end{tabular}");
            pw.println("}");
            pw.println("\\end{table}");
        }
    }
    
    /**
     * Add the read lengths section.
     */
    public void addLengthsSection() {
        String graphWidth = "width=.3\\linewidth";
        
        if (options.getNumberOfTypes() == 1) {
            graphWidth = "width=.4\\linewidth";
        }
        
        pw.println("\\subsection*{Read lengths}");
        pw.println("\\vspace{-3mm}");
                        
        pw.println("\\begin{table}[H]");
        pw.println("{\\footnotesize");
        pw.println("\\fontsize{9pt}{11pt}\\selectfont");
        pw.println("\\begin{tabular}{l c c c c c c c c c}");
        pw.println("{\\bf Type} & {\\bf NumReads} & {\\bf TotalBases} & {\\bf Mean} & {\\bf Longest} & {\\bf Shortest} & {\\bf N50} & {\\bf N50Count} & {\\bf N90} & {\\bf N90Count} \\\\");

        for (int type = 0; type<3; type++) {
            if (options.isProcessingReadType(type)) {
                ReadSetStats r = overallStats.getStatsByType(type);           
                pw.printf("%s & %d & %d & %.2f & %d & %d & %d & %d & %d & %d \\\\", r.getTypeString(), r.getNumReads(), r.getTotalBases(), r.getMeanLength(), r.getLongest(), r.getShortest(), r.getN50(), r.getN50Count(), r.getN90(), r.getN90Count());
                pw.println("");
            }
        }

        pw.println("\\end{tabular}");
        pw.println("}");
        pw.println("\\end{table}");
        pw.println("\\vspace{-10mm}");
        pw.println("\\begin{figure}[H]");
        pw.println("\\centering");
        
        
        
        includeGraphicsIfExists(NanoOKOptions.TYPE_TEMPLATE, "\\includegraphics["+graphWidth+"]{", options.getGraphsDir() + File.separator + "all_Template_lengths", "}");
        includeGraphicsIfExists(NanoOKOptions.TYPE_COMPLEMENT, "\\includegraphics["+graphWidth+"]{", options.getGraphsDir() + File.separator + "all_Complement_lengths", "}");
        includeGraphicsIfExists(NanoOKOptions.TYPE_2D, "\\includegraphics["+graphWidth+"]{", options.getGraphsDir() + File.separator + "all_2D_lengths", "}");
        
        
        //pw.println("\\includegraphics[width=.3\\linewidth]{" + options.getGraphsDir() + File.separator + "all_Template_lengths.pdf}");
        //pw.println("\\includegraphics[width=.3\\linewidth]{" + options.getGraphsDir() + File.separator + "all_Complement_lengths.pdf}");
        //pw.println("\\includegraphics[width=.3\\linewidth]{" + options.getGraphsDir() + File.separator + "all_2D_lengths.pdf}");
        pw.println("\\end{figure}");
    
    }
    
    /**
     * Write the alignments section to the report.
     * @param stats a ReadSetStats object
     */
    public void writeAlignmentsSection(ReadSetStats stats) {
        //if ((stats.getTypeString() == "Template") || (references.getNumberOfReferences() > 8)) {
        //    pw.println("\\clearpage");
        //}
        pw.println("\\subsection*{" + stats.getTypeString() + " alignments}");
        pw.println("\\vspace{-3mm}");
        pw.println("\\begin{table}[H]");
        pw.println("{\\footnotesize");
        pw.println("\\fontsize{9pt}{11pt}\\selectfont");
        pw.println("\\begin{tabular}{l c c}");
        pw.println("Number of reads & " + stats.getNumberOfReads() + " & \\\\");
        pw.printf("Number of reads with alignments & %d & (%.2f\\%%) \\\\", stats.getNumberOfReadsWithAlignments(), stats.getPercentOfReadsWithAlignments());
        pw.println("");
        pw.printf("Number of reads without alignments & %d & (%.2f\\%%) \\\\", stats.getNumberOfReadsWithoutAlignments(), stats.getPercentOfReadsWithoutAlignments());
        pw.println("");
        pw.println("\\end{tabular}");
        pw.println("}");
        pw.println("\\end{table}");
        pw.println("\\vspace{-10mm}");
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
                System.out.println("Can't find " + fullFilename);
                pw.print(" ");
            }
        }
    }
    
    /**
     * Write a section for a reference sequence.
     * @param refSeq reference to write
     */
    public void writeReferenceSection(ReferenceSequence refSeq) {
        String id = refSeq.getName().replaceAll("_", " ");
        String[] lines = new String[10];
        String newLineTag=" \\\\";
        String graphSize;        
        
        if (options.getNumberOfTypes() == 1) {
            newLineTag = "";
        }
                
        pw.println("\\subsection*{" + id + " error analysis}");
        pw.println("\\vspace{-3mm}");
        pw.println("\\begin{table}[H]");
        pw.println("{\\footnotesize");
        pw.println("\\fontsize{9pt}{11pt}\\selectfont");
        pw.println("\\begin{tabular}{l c c c}");       
        
        
        lines[0] = "";
        lines[1] = "Overall base identity (excluding indels)";
        lines[2] = "Aligned base identity (excluding indels)";
        lines[3] = "Identical bases per 100 aligned bases (including indels)";
        lines[4] = "Inserted bases per 100 aligned bases (including indels)";
        lines[5] = "Deleted bases per 100 aligned bases (including indels)";
        lines[6] = "Substitutions per 100 aligned bases (including indels)";
        lines[7] = "Mean insertion size";
        lines[8] = "Mean deletion size";
        for (int type=0; type<3; type++) {
            if (options.isProcessingReadType(type)) {
                lines[0] += " & " + NanoOKOptions.getTypeFromInt(type);
                lines[1] += String.format(" & %.2f\\%%", refSeq.getStatsByType(type).getReadPercentIdentical());
                lines[2] += String.format(" & %.2f\\%%", refSeq.getStatsByType(type).getAlignedPercentIdenticalWithoutIndels());
                lines[3] += String.format(" & %.2f\\%%", refSeq.getStatsByType(type).getAlignedPercentIdentical());
                lines[4] += String.format(" & %.2f\\%%", refSeq.getStatsByType(type).getPercentInsertionErrors());
                lines[5] += String.format(" & %.2f\\%%", refSeq.getStatsByType(type).getPercentDeletionErrors());
                lines[6] += String.format(" & %.2f\\%%", refSeq.getStatsByType(type).getPercentSubstitutionErrors());
                lines[7] += String.format(" & %.2f", refSeq.getStatsByType(type).getMeanInsertionSize());
                lines[8] += String.format(" & %.2f", refSeq.getStatsByType(type).getMeanDeletionSize());
            }
        }
        
       for (int i=0; i<=8; i++) {
           lines[i] += " \\\\";
           pw.println(lines[i]);
       }
        
        pw.println("\\end{tabular}");
        pw.println("}");
        pw.println("\\end{table}");     

        if (options.getNumberOfTypes() == 1) {
            graphSize = "width=.4\\linewidth";
        } else {
            graphSize = "height=3.5cm";
        }        
        
        pw.println("\\vspace{-5mm}");
        pw.println("\\begin{figure}[H]");
        pw.println("\\centering");
        includeGraphicsIfExists(NanoOKOptions.TYPE_TEMPLATE, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Template_insertions", "}");
        includeGraphicsIfExists(NanoOKOptions.TYPE_COMPLEMENT, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Complement_insertions", "}");
        includeGraphicsIfExists(NanoOKOptions.TYPE_2D, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_2D_insertions", "}"+newLineTag);
        includeGraphicsIfExists(NanoOKOptions.TYPE_TEMPLATE, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Template_deletions", "}");
        includeGraphicsIfExists(NanoOKOptions.TYPE_COMPLEMENT, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Complement_deletions", "}");
        includeGraphicsIfExists(NanoOKOptions.TYPE_2D, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_2D_deletions", "}");
        pw.println("\\end{figure}");
        
        pw.println("\\subsection*{" + id + " read identity}");
        pw.println("\\vspace{-3mm}");
        pw.println("\\begin{figure}[H]");
        pw.println("\\centering");
        includeGraphicsIfExists(NanoOKOptions.TYPE_TEMPLATE, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Template_length_vs_identity_hist", "}");
        includeGraphicsIfExists(NanoOKOptions.TYPE_COMPLEMENT, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Complement_length_vs_identity_hist", "}");
        includeGraphicsIfExists(NanoOKOptions.TYPE_2D, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_2D_length_vs_identity_hist", "}"+newLineTag);        
        includeGraphicsIfExists(NanoOKOptions.TYPE_TEMPLATE, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Template_length_vs_identity_scatter", "}");
        includeGraphicsIfExists(NanoOKOptions.TYPE_COMPLEMENT, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Complement_length_vs_identity_scatter", "}");
        includeGraphicsIfExists(NanoOKOptions.TYPE_2D, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_2D_length_vs_identity_scatter", "}"+newLineTag);        
        includeGraphicsIfExists(NanoOKOptions.TYPE_TEMPLATE, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Template_read_fraction_vs_alignment_identity_scatter", "}");
        includeGraphicsIfExists(NanoOKOptions.TYPE_COMPLEMENT, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Complement_read_fraction_vs_alignment_identity_scatter", "}");
        includeGraphicsIfExists(NanoOKOptions.TYPE_2D, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_2D_read_fraction_vs_alignment_identity_scatter", "}");        
        if (options.getNumberOfTypes() > 1) {
            pw.println("\\end{figure}");
            pw.println("\\begin{figure}[H]");
            pw.println("\\centering");
        }
        includeGraphicsIfExists(NanoOKOptions.TYPE_TEMPLATE, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Template_percent_aligned_vs_length_scatter", "}");
        includeGraphicsIfExists(NanoOKOptions.TYPE_COMPLEMENT, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Complement_percent_aligned_vs_length_scatter", "}");
        includeGraphicsIfExists(NanoOKOptions.TYPE_2D, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_2D_percent_aligned_vs_length_scatter", "}");        
        pw.println("\\end{figure}");
        
        if (options.getNumberOfTypes() == 1) {
            graphSize = "width=.4\\linewidth";
        } else {
            graphSize = "height=3.5cm";
        }

        pw.println("\\subsection*{" + id + " perfect kmers}");
        pw.println("\\vspace{-3mm}");
        pw.println("\\begin{figure}[H]");
        pw.println("\\centering");
        includeGraphicsIfExists(NanoOKOptions.TYPE_TEMPLATE, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Template_cumulative_perfect_kmers", "}");
        includeGraphicsIfExists(NanoOKOptions.TYPE_COMPLEMENT, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Complement_cumulative_perfect_kmers", "}");
        includeGraphicsIfExists(NanoOKOptions.TYPE_2D, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_2D_cumulative_perfect_kmers", "}");        
        includeGraphicsIfExists(NanoOKOptions.TYPE_TEMPLATE, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Template_best_perfect_kmers", "}");
        includeGraphicsIfExists(NanoOKOptions.TYPE_COMPLEMENT, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Complement_best_perfect_kmers", "}");
        includeGraphicsIfExists(NanoOKOptions.TYPE_2D, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_2D_best_perfect_kmers", "}");
        includeGraphicsIfExists(NanoOKOptions.TYPE_TEMPLATE, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Template_longest_perfect_vs_length_scatter", "}");
        includeGraphicsIfExists(NanoOKOptions.TYPE_COMPLEMENT, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Complement_longest_perfect_vs_length_scatter", "}");
        includeGraphicsIfExists(NanoOKOptions.TYPE_2D, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_2D_longest_perfect_vs_length_scatter", "}");
        pw.println("\\end{figure}");

        if (options.getNumberOfTypes() == 1) {
            graphSize = "width=.7\\linewidth";
        } else {
            graphSize = "height=2cm";
        }
                
        pw.println("\\subsection*{" + id + " coverage}");
        pw.println("\\vspace{-3mm}");
        pw.println("\\begin{figure}[H]");
        pw.println("\\centering");
        includeGraphicsIfExists(NanoOKOptions.TYPE_TEMPLATE, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Template_coverage", "} \\\\");
        includeGraphicsIfExists(NanoOKOptions.TYPE_COMPLEMENT, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Complement_coverage", "} \\\\");
        includeGraphicsIfExists(NanoOKOptions.TYPE_2D, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_2D_coverage", "} \\\\");
        includeGraphicsIfExists(NanoOKOptions.TYPE_ALL, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_gc", "}");
        pw.println("\\end{figure}"); 

        if (options.getNumberOfTypes() == 1) {
            pw.println("\\clearpage");
        }                
        
        if (options.getNumberOfTypes() == 1) {
            graphSize = "width=.7\\linewidth";
        } else {
            graphSize = "height=8cm";
        }

        pw.println("\\subsection*{" + id + " 5-mer analysis}");

        String[] overRepLines = new String[10];
        String[] underRepLines = new String[10];
        for (int i=0; i<10; i++) {
            overRepLines[i] = Integer.toString(i+1);
            underRepLines[i] = Integer.toString(i+1);
            for (int type=0; type<3; type++) {
                if (options.isProcessingReadType(type)) {
                    if (i == 0) {
                        refSeq.getStatsByType(type).sortKmerAbundance();
                    }
                    
                    ArrayList<KmerAbundance> ka = refSeq.getStatsByType(type).getKmerAbundance();
                    KmerAbundance ko = ka.get(i);
                    KmerAbundance ku = ka.get(ka.size() - 1 - i);
                    overRepLines[i] += String.format(" & %s & %.3f & %.3f & %.3f", ko.getKmer(), ko.getRefAbundance(), ko.getReadAbundance(), ko.getDifference());
                    underRepLines[i] += String.format(" & %s & %.3f & %.3f & %.3f", ku.getKmer(), ku.getRefAbundance(), ku.getReadAbundance(), ku.getDifference());
                }
            }
            overRepLines[i] += " \\\\";
            underRepLines[i] += " \\\\";
        }        
        
        pw.println("\\subsection*{Under-represented 5-mers}");
        pw.println("\\vspace{-3mm}");
        writeKmerTable(underRepLines);
        pw.println("\\vspace{-3mm}");
        pw.println("\\subsection*{Over-represented 5-mers}");
        pw.println("\\vspace{-3mm}");
        writeKmerTable(overRepLines);
        pw.println("\\vspace{-8mm}");
        
        pw.println("\\begin{figure}[H]");
        pw.println("\\centering");
        includeGraphicsIfExists(NanoOKOptions.TYPE_TEMPLATE, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Template_kmer_scatter", "} ");
        includeGraphicsIfExists(NanoOKOptions.TYPE_COMPLEMENT, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Complement_kmer_scatter", "} \\\\");
        includeGraphicsIfExists(NanoOKOptions.TYPE_2D, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_2D_kmer_scatter", "} \\\\");
        pw.println("\\end{figure}");         

        if (options.getNumberOfTypes() == 1) {
            graphSize = "width=.4\\linewidth";
        } else {
            graphSize = "height=3.5cm";
        }
        pw.println("\\subsection*{" + id + " GC content}");
        includeGraphicsIfExists(NanoOKOptions.TYPE_TEMPLATE, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Template_GC_hist", "}");
        includeGraphicsIfExists(NanoOKOptions.TYPE_COMPLEMENT, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Complement_GC_hist", "}");
        includeGraphicsIfExists(NanoOKOptions.TYPE_2D, "\\includegraphics["+graphSize+"]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_2D_GC_hist", "}");        
    }

    private void writeKmerTable(String[] lines) {
        pw.println("\\begin{table}[H]");
        pw.println("{\\footnotesize");
        pw.println("\\fontsize{7pt}{9pt}\\selectfont");
        pw.print("\\begin{tabular}{|c");
        int colCount = 1;
        for (int type=0; type<3; type++) {                
            if (options.isProcessingReadType(type)) {
                pw.print("|c c c c");
                colCount+=4;
            }
        }
        pw.println("|}");
        pw.println("\\cline{1-"+colCount+"}");
        for (int type=0; type<3; type++) {                
            if (options.isProcessingReadType(type)) {
                pw.print(" & \\multicolumn{4}{c|}{" + NanoOKOptions.getTypeFromInt(type) + "}");
            }
        }            
        pw.println(" \\\\");
        pw.print("Rank");
        for (int type=0; type<3; type++) {
            if (options.isProcessingReadType(type)) {
                pw.print(" & kmer & Ref \\% & Read \\% & Diff \\%");
            }
        }
        pw.println(" \\\\");
        pw.println("\\cline{1-"+colCount+"}");
                
        for (int i=0; i<10; i++) {
            pw.println(lines[i]);
        }
        pw.println("\\cline{1-"+colCount+"}");
        pw.println("\\end{tabular}");
        pw.println("}");
        pw.println("\\end{table}");     
    }
    
     /**
     * Write Top 10 or Bottom 10 moitf section.
     * @param listType either TYPE_TOP or TYPE_BOTTOM
     * @param k kmer size
     */
    public void writeMotifRange(int listType, int k, int colCount) {
        ArrayList<Map.Entry<String, Double>>[] insertionMotifs = new ArrayList[3];
        ArrayList<Map.Entry<String, Double>>[] deletionMotifs = new ArrayList[3];
        ArrayList<Map.Entry<String, Double>>[] substitutionMotifs = new ArrayList[3];
        String logoTypeString = new String("Unknown");
        
        for (int type=0; type<3; type++) {
            if (options.isProcessingReadType(type)) {
                String typeString;
                if (listType == KmerMotifStatistic.TYPE_TOP) {
                    typeString = overallStats.getStatsByType(type).getTypeString() + "_top";
                } else if (listType == KmerMotifStatistic.TYPE_BOTTOM) {
                    typeString = overallStats.getStatsByType(type).getTypeString() + "_bottom";
                } else {
                    typeString = overallStats.getStatsByType(type).getTypeString() + "_unknown";
                }

                insertionMotifs[type] = overallStats.getStatsByType(type).getMotifStatistics().getSortedInsertionMotifPercentages(k);                
                deletionMotifs[type] = overallStats.getStatsByType(type).getMotifStatistics().getSortedDeletionMotifPercentages(k);
                substitutionMotifs[type] = overallStats.getStatsByType(type).getMotifStatistics().getSortedSubstitutionMotifPercentages(k);

                overallStats.getStatsByType(type).getMotifStatistics().writeInsertionLogoImage(listType, options.getGraphsDir() + File.separator + "motifs" + File.separator + "logo_insertion_" + typeString + "_k" + k + ".png", k);
                overallStats.getStatsByType(type).getMotifStatistics().writeDeletionLogoImage(listType, options.getGraphsDir() + File.separator + "motifs" + File.separator + "logo_deletion_" + typeString + "_k" + k + ".png", k);
                overallStats.getStatsByType(type).getMotifStatistics().writeSubstitutionLogoImage(listType, options.getGraphsDir() + File.separator + "motifs" + File.separator + "logo_substitution_" + typeString + "_k" + k + ".png", k);
            }
        }

        for (int i=0; i<10; i++) {
            if (listType == KmerMotifStatistic.TYPE_TOP) {
                pw.print(i+1);
            } else {
                pw.print("-"+(10-i));
            }
            for (int type=0; type<3; type++) {
                if (options.isProcessingReadType(type)) {
                    int insertionPos = i;
                    int deletionPos = i;
                    int substitutionPos = i;
                    
                    if (listType == KmerMotifStatistic.TYPE_BOTTOM) {
                        insertionPos = insertionMotifs[type].size() - 10 + i;
                        deletionPos = deletionMotifs[type].size() - 10 + i;
                        substitutionPos = substitutionMotifs[type].size() - 10 + i;
                    }               

                    if ((insertionMotifs[type].size() > insertionPos) && (insertionPos >=0)) {
                        pw.printf(" & %s (%.2f\\%%)", insertionMotifs[type].get(insertionPos).getKey(), insertionMotifs[type].get(insertionPos).getValue());
                    } else {
                        pw.print(" &");
                    }

                    if ((deletionMotifs[type].size() > deletionPos) && (deletionPos >=0)) {
                        pw.printf(" & %s (%.2f\\%%)", deletionMotifs[type].get(deletionPos).getKey(), deletionMotifs[type].get(deletionPos).getValue());
                    } else {
                        pw.print(" &");
                    }

                    if ((substitutionMotifs[type].size() > substitutionPos) && (substitutionPos >=0)) {
                        pw.printf(" & %s (%.2f\\%%)", substitutionMotifs[type].get(substitutionPos).getKey(), substitutionMotifs[type].get(substitutionPos).getValue());
                    } else {
                        pw.print(" &");
                    }           
                }
            }
            
            if (i == 0) {
                if (listType == KmerMotifStatistic.TYPE_TOP) {
                    pw.print(" & \\multirow{10}{*}{\\rotatebox[origin=c]{90}{Most common}}");
                } else if (listType == KmerMotifStatistic.TYPE_BOTTOM) {
                    pw.print(" & \\multirow{10}{*}{\\rotatebox[origin=c]{90}{Least common}}");                
                } else {
                    pw.print(" & \\multirow{10}{*}{\\rotatebox[origin=c]{90}{Unknown}}");
                }                    
            }
            pw.println("\\\\");
        }

        pw.println("\\cline{1-"+colCount+"}");
        pw.println("\\rule{0pt}{0.6cm}");
        pw.print(" ");

        for (int type=0; type<3; type++) {
            if (options.isProcessingReadType(type)) {
                String typeString;
                if (listType == KmerMotifStatistic.TYPE_TOP) {
                    typeString = overallStats.getStatsByType(type).getTypeString() + "_top";
                } else if (listType == KmerMotifStatistic.TYPE_BOTTOM) {
                    typeString = overallStats.getStatsByType(type).getTypeString() + "_bottom";
                } else {
                    typeString = overallStats.getStatsByType(type).getTypeString() + "_unknown";
                }

                pw.print(" & \\includegraphics[height=0.5cm]{" + options.getGraphsDir()+File.separator + "motifs" + File.separator + "logo_insertion_" + typeString + "_k" + k + ".png}");
                pw.print(" & \\includegraphics[height=0.5cm]{" + options.getGraphsDir()+File.separator + "motifs" + File.separator + "logo_deletion_" + typeString + "_k" + k + ".png}");
                pw.print(" & \\includegraphics[height=0.5cm]{" + options.getGraphsDir()+File.separator + "motifs" + File.separator + "logo_substitution_" + typeString + "_k" + k + ".png}");
            }
        }

        pw.println(" \\\\");
    }
    
    /**
     * Write motif section of report.
     */
    public void writeMotifSection() {        
        pw.println("\\subsection*{Kmer motifs before errors}");
        
        for (int k=3; k<=5; k++) {   
            int colCount = 1;

            pw.println("\\subsection*{"+k+"-mer error motif analysis}");
            pw.println("\\vspace{-3mm}");
            pw.println("\\begin{table}[H]");
            pw.println("{\\footnotesize");
            pw.println("\\fontsize{6pt}{8pt}\\selectfont");
            pw.println("\\tabcolsep=0.15cm");
            pw.print("\\begin{tabular}{|c");
            for (int type=0; type<3; type++) {                
                if (options.isProcessingReadType(type)) {
                    pw.print("|c c c");
                    colCount+=3;
                }
            }
            pw.println("|c}");
            pw.println("\\cline{1-"+colCount+"}");
            //pw.println("& \\multicolumn{3}{c|}{Template} & \\multicolumn{3}{c|}{Complement} & \\multicolumn{3}{c|}{2D} & \\\\");
            for (int type=0; type<3; type++) {                
                if (options.isProcessingReadType(type)) {
                    pw.print(" & \\multicolumn{3}{c|}{" + NanoOKOptions.getTypeFromInt(type) + "}");
                }
            }            
            pw.println(" & \\\\");
            //pw.println("Rank & Insertion & Deletion & Substitution & Insertion & Deletion & Substitution & Insertion & Deletion & Substitution & \\\\");
            pw.print("Rank");
            for (int type=0; type<3; type++) {
                if (options.isProcessingReadType(type)) {
                    pw.print(" & Insertion & Deletion & Substitution");
                }
            }
            pw.println(" & \\\\");
            pw.println("\\cline{1-"+colCount+"}");
            writeMotifRange(KmerMotifStatistic.TYPE_TOP, k, colCount);
            pw.println("\\cline{1-"+colCount+"}");
            writeMotifRange(KmerMotifStatistic.TYPE_BOTTOM, k, colCount);
            pw.println("\\cline{1-"+colCount+"}");
            pw.println("\\end{tabular}");
            pw.println("}");
            pw.println("\\end{table}");  
            pw.println("\\vspace{-9mm}");
            pw.printf("{\\fontsize{8}{8}\\textsf{Kmer space for %d-mers: %d \\hspace{5mm} Random chance for any given %d-mer: %.2f\\%%}}", k, (int)Math.pow(4, k), k, 100.0/Math.pow(4, k));
            pw.println("");
            pw.println("\\vspace{5mm}");
        }
    }
    
    /**
     * Convert integer (0, 1, 2, 3) to base (A, C, G, T)
     * @param i number to convert
     * @return base character
     */
    private char intToBase(int i) {
        char c;
        
        switch(i) {
            case 0: c = 'A'; break;
            case 1: c = 'C'; break;
            case 2: c = 'G'; break;
            case 3: c = 'T'; break;
            default: c = 'N'; break;
        }
        
        return c;
    }
    
    /**
     * Write section to report on substitution errors.
     */
    public void writeSubstitutionErrorsSection()
    {
        pw.println("\\subsection*{All reference substitutions}");
        pw.println("\\vspace{-3mm}");

        pw.println("\\begin{table}[H]");
        pw.println("{\\footnotesize");
        pw.println("\\fontsize{8pt}{10pt}\\selectfont");
        //pw.println("\\begin{tabular}{|c c|c c c c|c c c c|c c c c|}");
        pw.print("\\begin{tabular}{|c c");
        for (int type=0; type<3; type++) {                
            if (options.isProcessingReadType(type)) {
                pw.print("|c c c c");
            }
        }
        pw.println("|}");
        pw.println("\\hline");
        //pw.println(" & & \\multicolumn{4}{c|}{Template substituted \\%} & \\multicolumn{4}{c|}{Complement substituted \\%} & \\multicolumn{4}{c|}{2D substituted \\%} \\\\");
        pw.print(" &");
        for (int type=0; type<3; type++) {                
            if (options.isProcessingReadType(type)) {
                pw.print(" & \\multicolumn{4}{c|}{" + NanoOKOptions.getTypeFromInt(type) + " substituted \\%}");
            }
        }       
        pw.println(" \\\\");
        //pw.println(" & & a & c & g & t & a & c & g & t & a & c & g & t \\\\");
        pw.print(" &");
        for (int type=0; type<3; type++) {                
            if (options.isProcessingReadType(type)) {
                pw.print(" & a & c & g & t");
            }
        }
        pw.println(" \\\\");
        
        pw.println("\\hline");

        for (int r=0; r<4; r++) {
            if (r == 0) {
                pw.print("\\multirow{4}{*}{\\rotatebox[origin=c]{90}{Reference}} & ");
            } else {
                pw.print(" & ");
            }
            pw.print(intToBase(r));
            for (int type=0; type<3; type++) {
                if (options.isProcessingReadType(type)) {
                    int subs[][] = overallStats.getStatsByType(type).getSubstitutionErrors();
                    double nSubstitutions = (double)overallStats.getStatsByType(type).getNumberOfSubstitutions();
                    for (int s=0; s<4; s++) {
                        double pc = (100.0 * (double)subs[r][s]) / nSubstitutions;
                        pw.printf(" & %.2f", pc);
                    }
                }
            }
        pw.println("\\\\");
        }
        pw.println("\\hline");
        pw.println("\\end{tabular}");
        pw.println("}");
        pw.println("\\end{table}");  
    }
    
    private void writeOverallKmerSection() {
        String graphWidth = "width=.3\\linewidth";

        if (options.getNumberOfTypes() == 1) {
            graphWidth = "width=.5\\linewidth";
        }
 
        pw.println("\\subsection*{All reference 21mer analysis}");
        pw.println("\\vspace{-3mm}");
        pw.println("\\begin{figure}[H]");
        pw.println("\\centering");
        includeGraphicsIfExists(NanoOKOptions.TYPE_TEMPLATE, "\\includegraphics["+graphWidth+"]{", options.getGraphsDir() + File.separator + "all_Template_21mers", "}");
        includeGraphicsIfExists(NanoOKOptions.TYPE_COMPLEMENT, "\\includegraphics["+graphWidth+"]{", options.getGraphsDir() + File.separator + "all_Complement_21mers", "}");
        includeGraphicsIfExists(NanoOKOptions.TYPE_2D, "\\includegraphics["+graphWidth+"]{", options.getGraphsDir() + File.separator + "all_2D_21mers", "}");                
        pw.println("\\end{figure}");        
   }
    
    /**
     * Add sections for each reference sequence.
     * @param refs reference sequences
     */
    public void addAllReferenceSections() {
        ArrayList<ReferenceSequence> sortedRefs = references.getSortedReferences();
        for (int i=0; i<sortedRefs.size(); i++) {
            ReferenceSequence rs = sortedRefs.get(i);

            if (rs.getTotalNumberOfAlignments() > NanoOKOptions.MIN_ALIGNMENTS) {
                if ((options.getNumberOfTypes() > 1) || (references.getNumberOfReferences() > 1)) {
                    pw.println("\\clearpage");
                }        
            
                writeReferenceSection(rs);
            }
        }
    }
    
    /**
     * Write end of LaTeX file.
     */
    private void writeLaTeXFooter() {
        pw.println("\\end{document}");
    }
    
    /**
     * Get handle to PrintWriter.
     * @return a PrintWriter object
     */
    public PrintWriter getPrintWriter() {
        return pw;
    }    
    
    /**
     * Write the LaTeX report.
     */
    public void writeReport() {
        open();
        addPassFailSection();
        addLengthsSection();
        
        for (int type=0; type<3; type++) {
            if (options.isProcessingReadType(type)) {
                writeAlignmentsSection(overallStats.getStatsByType(type));            
        //        references.writeReferenceStatFiles(type);
        //        references.writeReferenceSummary(type);
                writeAlignmentSummary(type, pw);
            }
        }
        
        addAllReferenceSections();        
        //Set<String> ids = references.getAllIds();
        //for (String id : ids) {
        //    writeReferenceSection(references.getReferenceById(id));
        //}
        
        if ((options.getNumberOfTypes() > 1) || (references.getNumberOfReferences() > 1)) {
            pw.println("\\clearpage");
        }
        writeOverallKmerSection();
        writeSubstitutionErrorsSection();           
        writeMotifSection();
        
        writeLaTeXFooter();
        close();
    }
    
        /**
     * Write reference summary to LaTeX report.
     * @param type type from NanoOKOptions
     * @param pw handle to LaTeX file
     */
    public void writeAlignmentSummary(int type, PrintWriter pw) {
        if (references.getNumberOfReferences() < LONGTABLE_THRESHOLD) {
            pw.println("\\begin{table}[H]");
        }
        pw.println("{\\footnotesize");
        if (references.getNumberOfReferences() < LONGTABLE_THRESHOLD) {
            pw.println("\\fontsize{9pt}{11pt}\\selectfont");
            pw.println("\\begin{tabular}{l c c c c c c c}");
        } else {
            pw.println("\\begin{longtable}[l]{l c c c c c c c}");
        }
        pw.println("          &             & {\\bf Number of} & {\\bf \\% of} & {\\bf Mean read} & {\\bf Aligned} & {\\bf Mean} & {\\bf Longest} \\\\");
        pw.println("{\\bf ID} & {\\bf Size} & {\\bf Reads}     & {\\bf Reads}  & {\\bf length}    & {\\bf bases}   & {\\bf coverage} & {\\bf Perf Kmer} \\\\");
        ArrayList<ReferenceSequence> sortedRefs = references.getSortedReferences();
        for (int i=0; i<sortedRefs.size(); i++) {
            ReferenceSequence r = sortedRefs.get(i);
            ReferenceSequenceStats refStats = r.getStatsByType(type);
            if ((sortedRefs.size() < 100) || (refStats.getNumberOfReadsWithAlignments() > 0)) {
                pw.printf("%s & %d & %d & %.2f & %.2f & %d & %.2f & %d \\\\",
                           r.getName().replaceAll("_", " "),
                           r.getSize(),
                           refStats.getNumberOfReadsWithAlignments(),
                           100.0 * (double)refStats.getNumberOfReadsWithAlignments() / (double)overallStats.getStatsByType(type).getNumberOfReads(),
                           refStats.getMeanReadLength(),
                           refStats.getTotalAlignedBases(),
                           (double)refStats.getTotalAlignedBases() / r.getSize(),
                           refStats.getLongestPerfectKmer());
                pw.println("");
            }
        }
        if (references.getNumberOfReferences() < LONGTABLE_THRESHOLD) {
            pw.println("\\end{tabular}");
        } else {
            pw.println("\\end{longtable}");
        }
        pw.println("}");
        if (references.getNumberOfReferences() < LONGTABLE_THRESHOLD) {
            pw.println("\\end{table}");        
        }
    }

    
    public void makePDF() {
        ProcessLogger pl = new ProcessLogger();
        String command = "pdflatex -interaction=nonstopmode -output-directory " +options.getLatexDir() + " " + options.getLatexDir() + File.separator + options.getSample() + ".tex";
        String logFilename = options.getLogsDir() + File.separator + "pdflatex_output_log" + options.getAnalysisSuffix() + ".txt";
        System.out.println("pdflatex output " + logFilename);
        pl.runAndLogCommand(command, logFilename, false);
    }
}
