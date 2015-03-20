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
public class ReportWriter {
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
    public ReportWriter(NanoOKOptions o, References r, OverallStats s) {
        options = o;
        references = r;
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
        pw.println("\\restylefloat{table}");
        pw.println("\\begin{document}");
        pw.println("\\renewcommand*{\\familydefault}{\\sfdefault}");
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
                ReadSetStats r = overallStats.getStatsByType(type);           
                pw.printf("%s & %d & %d  \\\\\n", r.getTypeString(), r.getNumberOfPassFiles(), r.getNumberOfFailFiles());
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
        pw.println("\\subsection*{Read lengths}");
        pw.println("\\vspace{-3mm}");
                        
        pw.println("\\begin{table}[H]");
        pw.println("{\\footnotesize");
        pw.println("\\fontsize{9pt}{11pt}\\selectfont");
        pw.println("\\begin{tabular}{l c c c c c c c c c}");
        pw.println("{\\bf Type} & {\\bf NumReads} & {\\bf TotalBases} & {\\bf Mean} & {\\bf Longest} & {\\bf Shortest} & {\\bf N50} & {\\bf N50Count} & {\\bf N90} & {\\bf N90Count} \\\\");

        for (int type = 0; type<3; type++) {
            ReadSetStats r = overallStats.getStatsByType(type);           
            pw.printf("%s & %d & %d & %.2f & %d & %d & %d & %d & %d & %d \\\\\n", r.getTypeString(), r.getNumReads(), r.getTotalBases(), r.getMeanLength(), r.getLongest(), r.getShortest(), r.getN50(), r.getN50Count(), r.getN90(), r.getN90Count());
        }

        pw.println("\\end{tabular}");
        pw.println("}");
        pw.println("\\end{table}");
        pw.println("\\vspace{-10mm}");
        pw.println("\\begin{figure}[H]");
        pw.println("\\centering");
        pw.println("\\includegraphics[width=.3\\linewidth]{" + options.getGraphsDir() + File.separator + "all_Template_lengths.pdf}");
        pw.println("\\includegraphics[width=.3\\linewidth]{" + options.getGraphsDir() + File.separator + "all_Complement_lengths.pdf}");
        pw.println("\\includegraphics[width=.3\\linewidth]{" + options.getGraphsDir() + File.separator + "all_2D_lengths.pdf}");
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
        pw.printf("Number of reads with alignments & %d & (%.2f\\%%) \\\\\n", stats.getNumberOfReadsWithAlignments(), stats.getPercentOfReadsWithAlignments());
        pw.printf("Number of reads without alignments & %d & (%.2f\\%%) \\\\\n", stats.getNumberOfReadsWithoutAlignments(), stats.getPercentOfReadsWithoutAlignments());
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
    private void includeGraphicsIfExists(String preTex, String filename, String postTex) {
        File f = new File(filename);
        
        if (f.exists()) {
            pw.print(preTex);
            pw.print(filename);
            pw.println(postTex);            
        } else {
            pw.print(" ");
        }
    }
    
    /**
     * Write a section for a reference sequence.
     * @param refSeq reference to write
     */
    public void writeReferenceSection(ReferenceSequence refSeq) {
        String id = refSeq.getName().replaceAll("_", " ");

        pw.println("\\clearpage");
        pw.println("\\subsection*{" + id + " error analysis}");
        pw.println("\\vspace{-3mm}");
        pw.println("\\begin{table}[H]");
        pw.println("{\\footnotesize");
        pw.println("\\fontsize{9pt}{11pt}\\selectfont");
        pw.println("\\begin{tabular}{l c c c}");       
        
        pw.println(" & Template & Complement & 2D \\\\");
        pw.printf("Overall identity (minus indels) & %.2f\\%% & %.2f\\%% & %.2f\\%% \\\\\n",
                refSeq.getStatsByType(0).getReadPercentIdentical(),
                refSeq.getStatsByType(1).getReadPercentIdentical(),
                refSeq.getStatsByType(2).getReadPercentIdentical());                
        pw.printf("Aligned identity (minus indels) & %.2f\\%% & %.2f\\%% & %.2f\\%% \\\\\n",
                refSeq.getStatsByType(0).getAlignedPercentIdenticalWithoutIndels(),
                refSeq.getStatsByType(1).getAlignedPercentIdenticalWithoutIndels(),
                refSeq.getStatsByType(2).getAlignedPercentIdenticalWithoutIndels());                
        pw.printf("Identical bases per 100 aligned bases & %.2f\\%% & %.2f\\%% & %.2f\\%% \\\\\n",
                refSeq.getStatsByType(0).getAlignedPercentIdentical(),
                refSeq.getStatsByType(1).getAlignedPercentIdentical(),
                refSeq.getStatsByType(2).getAlignedPercentIdentical());
        pw.printf("Inserted bases per 100 aligned bases & %.2f\\%% & %.2f\\%% & %.2f\\%% \\\\\n",
                refSeq.getStatsByType(0).getPercentInsertionErrors(),
                refSeq.getStatsByType(1).getPercentInsertionErrors(),
                refSeq.getStatsByType(2).getPercentInsertionErrors());
        pw.printf("Deleted bases per 100 aligned bases & %.2f\\%% & %.2f\\%% & %.2f\\%% \\\\\n",
                refSeq.getStatsByType(0).getPercentDeletionErrors(),
                refSeq.getStatsByType(1).getPercentDeletionErrors(),
                refSeq.getStatsByType(2).getPercentDeletionErrors());
        pw.printf("Substitutions per 100 aligned bases & %.2f\\%% & %.2f\\%% & %.2f\\%% \\\\\n",
                refSeq.getStatsByType(0).getPercentSubstitutionErrors(),
                refSeq.getStatsByType(1).getPercentSubstitutionErrors(),
                refSeq.getStatsByType(2).getPercentSubstitutionErrors());
        pw.println("\\end{tabular}");
        pw.println("}");
        pw.println("\\end{table}");     
        
        pw.println("\\begin{figure}[H]");
        pw.println("\\centering");
        includeGraphicsIfExists("\\includegraphics[height=3.5cm]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Template_insertions.pdf", "}");
        includeGraphicsIfExists("\\includegraphics[height=3.5cm]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Complement_insertions.pdf", "}");
        includeGraphicsIfExists("\\includegraphics[height=3.5cm]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_2D_insertions.pdf", "} \\\\");
        includeGraphicsIfExists("\\includegraphics[height=3.5cm]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Template_deletions.pdf", "}");
        includeGraphicsIfExists("\\includegraphics[height=3.5cm]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Complement_deletions.pdf", "}");
        includeGraphicsIfExists("\\includegraphics[height=3.5cm]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_2D_deletions.pdf", "}");
        pw.println("\\end{figure}");
        
        pw.println("\\subsection*{" + id + " read identity}");
        pw.println("\\vspace{-3mm}");
        pw.println("\\begin{figure}[H]");
        pw.println("\\centering");
        includeGraphicsIfExists("\\includegraphics[height=3.5cm]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Template_length_vs_identity_hist.pdf", "}");
        includeGraphicsIfExists("\\includegraphics[height=3.5cm]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Complement_length_vs_identity_hist.pdf", "}");
        includeGraphicsIfExists("\\includegraphics[height=3.5cm]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_2D_length_vs_identity_hist.pdf", "} \\\\");        
        includeGraphicsIfExists("\\includegraphics[height=3.5cm]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Template_length_vs_identity_scatter.pdf", "}");
        includeGraphicsIfExists("\\includegraphics[height=3.5cm]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Complement_length_vs_identity_scatter.pdf", "}");
        includeGraphicsIfExists("\\includegraphics[height=3.5cm]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_2D_length_vs_identity_scatter.pdf", "} \\\\");        
        includeGraphicsIfExists("\\includegraphics[height=3.5cm]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Template_read_fraction_vs_alignment_identity_scatter.pdf", "}");
        includeGraphicsIfExists("\\includegraphics[height=3.5cm]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Complement_read_fraction_vs_alignment_identity_scatter.pdf", "}");
        includeGraphicsIfExists("\\includegraphics[height=3.5cm]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_2D_read_fraction_vs_alignment_identity_scatter.pdf", "}");        
        pw.println("\\end{figure}");
        
        pw.println("\\subsection*{" + id + " coverage}");
        pw.println("\\vspace{-3mm}");
        pw.println("\\begin{figure}[H]");
        pw.println("\\centering");
        includeGraphicsIfExists("\\includegraphics[height=3cm]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Template_coverage.pdf", "}");
        includeGraphicsIfExists("\\includegraphics[height=3cm]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Complement_coverage.pdf", "}");
        includeGraphicsIfExists("\\includegraphics[height=3cm]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_2D_coverage.pdf", "}");
        includeGraphicsIfExists("\\includegraphics[height=3cm]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_gc.pdf", "}");
        pw.println("\\end{figure}");

        pw.println("\\subsection*{" + id + " perfect kmers}");
        pw.println("\\vspace{-3mm}");
        pw.println("\\begin{figure}[H]");
        pw.println("\\centering");
        includeGraphicsIfExists("\\includegraphics[width=.3\\linewidth]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Template_cumulative_perfect_kmers.pdf", "}");
        includeGraphicsIfExists("\\includegraphics[width=.3\\linewidth]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Complement_cumulative_perfect_kmers.pdf", "}");
        includeGraphicsIfExists("\\includegraphics[width=.3\\linewidth]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_2D_cumulative_perfect_kmers.pdf", "}");        
        includeGraphicsIfExists("\\includegraphics[width=.3\\linewidth]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Template_best_perfect_kmers.pdf", "}");
        includeGraphicsIfExists("\\includegraphics[width=.3\\linewidth]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Complement_best_perfect_kmers.pdf", "}");
        includeGraphicsIfExists("\\includegraphics[width=.3\\linewidth]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_2D_best_perfect_kmers.pdf", "}");
        includeGraphicsIfExists("\\includegraphics[width=.3\\linewidth]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Template_longest_perfect_vs_length_scatter.pdf", "}");
        includeGraphicsIfExists("\\includegraphics[width=.3\\linewidth]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_Complement_longest_perfect_vs_length_scatter.pdf", "}");
        includeGraphicsIfExists("\\includegraphics[width=.3\\linewidth]{", options.getGraphsDir() + File.separator + refSeq.getName() + File.separator + refSeq.getName() + "_2D_longest_perfect_vs_length_scatter.pdf", "}");
        pw.println("\\end{figure}");
    }
    
    /**
     * Write Top 10 or Bottom 10 moitf section.
     * @param listType either TYPE_TOP or TYPE_BOTTOM
     * @param k kmer size
     */
    public void writeMotifRange(int listType, int k) {
        ArrayList<Map.Entry<String, Double>>[] insertionMotifs = new ArrayList[3];
        ArrayList<Map.Entry<String, Double>>[] deletionMotifs = new ArrayList[3];
        ArrayList<Map.Entry<String, Double>>[] substitutionMotifs = new ArrayList[3];
        String logoTypeString = new String("Unknown");
        
        for (int type=0; type<3; type++) {
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

        for (int i=0; i<10; i++) {
            if (listType == KmerMotifStatistic.TYPE_TOP) {
                pw.print(i+1);
            } else {
                pw.print("-"+(10-i));
            }
            for (int type=0; type<3; type++) {
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

        pw.println("\\cline{1-10}");
        pw.println("\\rule{0pt}{0.6cm}");
        pw.print(" ");

        for (int type=0; type<3; type++) {
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

        pw.println(" \\\\");
    }
    
    /**
     * Write motif section of report.
     */
    public void writeMotifSection() {
        pw.println("\\clearpage");
        pw.println("\\subsection*{Error motif analysis}");
        
        for (int k=3; k<=5; k++) {             
            pw.println("\\subsection*{"+k+"-mer analysis}");
            pw.println("\\vspace{-3mm}");
            pw.println("\\begin{table}[H]");
            pw.println("{\\footnotesize");
            pw.println("\\fontsize{6pt}{8pt}\\selectfont");
            pw.println("\\tabcolsep=0.15cm");
            pw.println("\\begin{tabular}{|c|c c c|c c c|c c c|c}");
            pw.println("\\cline{1-10}");
            pw.println("& \\multicolumn{3}{c|}{Template} & \\multicolumn{3}{c|}{Complement} & \\multicolumn{3}{c|}{2D} & \\\\");
            pw.println("Rank & Insertion & Deletion & Substitution & Insertion & Deletion & Substitution & Insertion & Deletion & Substitution & \\\\");
            pw.println("\\cline{1-10}");
            writeMotifRange(KmerMotifStatistic.TYPE_TOP, k);
            pw.println("\\cline{1-10}");
            writeMotifRange(KmerMotifStatistic.TYPE_BOTTOM, k);
            pw.println("\\cline{1-10}");
            pw.println("\\end{tabular}");
            pw.println("}");
            pw.println("\\end{table}");  
            pw.println("\\vspace{-9mm}");
            pw.printf("{\\fontsize{8}{8}\\textsf{Kmer space for %d-mers: %d \\hspace{5mm} Random chance for any given %d-mer: %.2f\\%%}}\n", k, (int)Math.pow(4, k), k, 100.0/Math.pow(4, k));
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
        pw.println("\\subsection*{Substitutions}");
        pw.println("\\vspace{-3mm}");

        pw.println("\\begin{table}[H]");
        pw.println("{\\footnotesize");
        pw.println("\\fontsize{8pt}{10pt}\\selectfont");
        pw.println("\\begin{tabular}{|c c|c c c c|c c c c|c c c c|}");
        pw.println("\\hline");
        pw.println(" & & \\multicolumn{4}{c|}{Template substituted \\%} & \\multicolumn{4}{c|}{Complement substituted \\%} & \\multicolumn{4}{c|}{2D substituted \\%} \\\\");
        pw.println(" & & a & c & g & t & a & c & g & t & a & c & g & t \\\\");
        pw.println("\\hline");

        for (int r=0; r<4; r++) {
            if (r == 0) {
                pw.print("\\multirow{4}{*}{\\rotatebox[origin=c]{90}{Reference}} & ");
            } else {
                pw.print(" & ");
            }
            pw.print(intToBase(r));
            for (int type=0; type<3; type++) {
                int subs[][] = overallStats.getStatsByType(type).getSubstitutionErrors();
                double nSubstitutions = (double)overallStats.getStatsByType(type).getNumberOfSubstitutions();
                for (int s=0; s<4; s++) {
                    double pc = (100.0 * (double)subs[r][s]) / nSubstitutions;
                    pw.printf(" & %.2f", pc);
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
        pw.println("\\subsection*{All reference perfect kmers}");
        pw.println("\\vspace{-3mm}");
        includeGraphicsIfExists("\\includegraphics[width=.3\\linewidth]{", options.getGraphsDir() + File.separator + "all_Template_21mers.pdf", "}");
        includeGraphicsIfExists("\\includegraphics[width=.3\\linewidth]{", options.getGraphsDir() + File.separator + "all_Complement_21mers.pdf", "}");
        includeGraphicsIfExists("\\includegraphics[width=.3\\linewidth]{", options.getGraphsDir() + File.separator + "all_2D_21mers.pdf", "}");                
    }
    
    /**
     * Add sections for each reference sequence.
     * @param refs reference sequences
     */
    public void addAllReferenceSections() {
        ArrayList<ReferenceSequence> sortedRefs = references.getSortedReferences();
        for (int i=0; i<sortedRefs.size(); i++) {
            ReferenceSequence rs = sortedRefs.get(i);
            writeReferenceSection(rs);
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

        options.initialiseAlignmentSummaryFile();
        
        for (int type=0; type<3; type++) {            
            writeAlignmentsSection(overallStats.getStatsByType(type));            
            references.writeReferenceStatFiles(type);
            references.writeReferenceSummary(type);
            references.writeTexSummary(type, pw);
        }
        
        addAllReferenceSections();        
        //Set<String> ids = references.getAllIds();
        //for (String id : ids) {
        //    writeReferenceSection(references.getReferenceById(id));
        //}
        
        writeMotifSection();
        writeSubstitutionErrorsSection();        
        writeOverallKmerSection();
        
        writeLaTeXFooter();
        close();
    }
    
    public void makePDF() {
        ProcessLogger pl = new ProcessLogger();
        String command = "pdflatex -interaction=nonstopmode -output-directory " +options.getLatexDir() + " " + options.getLatexDir() + File.separator + options.getSample() + ".tex";
        String logFilename = options.getLogsDir() + File.separator + "pdflatex_output_log.txt";
        System.out.println("pdflatex output " + logFilename);
        pl.runCommand(command, logFilename, false);
    }
}
