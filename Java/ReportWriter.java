package nanotools;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ReportWriter {
    private NanoOKOptions options;
    private References references;
    private OverallStats overallStats;
    private PrintWriter pw;
    private String sample;
    
    public ReportWriter(NanoOKOptions o, References r, OverallStats s) {
        options = o;
        references = r;
        overallStats = s;
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
    
    public void beginLengthsSection() {
        pw.println("\\subsection*{Read lengths}");
        pw.println("\\vspace{-3mm}");
        pw.println("\\begin{table}[H]");
        pw.println("{\\footnotesize");
        pw.println("\\fontsize{9pt}{11pt}\\selectfont");
        pw.println("\\begin{tabular}{l c c c c c c c c c}");
        pw.println("{\\bf Type} & {\\bf NumReads} & {\\bf TotalBases} & {\\bf Mean} & {\\bf Longest} & {\\bf Shortest} & {\\bf N50} & {\\bf N50Count} & {\\bf N90} & {\\bf N90Count} \\\\");
    }
    
    public void addReadSet(ReadSetStats r) {
        pw.printf("%s & %d & %d & %.2f & %d & %d & %d & %d & %d & %d \\\\\n", r.getTypeString(), r.getNumReads(), r.getTotalBases(), r.getMeanLength(), r.getLongest(), r.getShortest(), r.getN50(), r.getN50Count(), r.getN90(), r.getN90Count());
    }
    
    public void endLengthsSection() {       
        pw.println("\\end{tabular}");
        pw.println("}");
        pw.println("\\end{table}");
        pw.println("\\vspace{-10mm}");
        pw.println("\\begin{figure}[H]");
        pw.println("\\centering");
        pw.println("\\includegraphics[width=.3\\linewidth]{" + options.getGraphsDir() + options.getSeparator() + "all_Template_lengths.pdf}");
        pw.println("\\includegraphics[width=.3\\linewidth]{" + options.getGraphsDir() + options.getSeparator() + "all_Complement_lengths.pdf}");
        pw.println("\\includegraphics[width=.3\\linewidth]{" + options.getGraphsDir() + options.getSeparator() + "all_2D_lengths.pdf}");
        pw.println("\\end{figure}");
    }
    
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
        pw.printf("Overall (all read) identity & %.2f\\%% & %.2f\\%% & %.2f\\%% \\\\\n",
                refSeq.getStatsByType(0).getReadPercentIdentical(),
                refSeq.getStatsByType(1).getReadPercentIdentical(),
                refSeq.getStatsByType(2).getReadPercentIdentical());                
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
        pw.println("\\includegraphics[height=3.5cm]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName()+ "_Template_insertions.pdf}");
        pw.println("\\includegraphics[height=3.5cm]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_Complement_insertions.pdf}");
        pw.println("\\includegraphics[height=3.5cm]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_2D_insertions.pdf} \\\\");
        pw.println("\\includegraphics[height=3.5cm]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName()+ "_Template_deletions.pdf}");
        pw.println("\\includegraphics[height=3.5cm]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_Complement_deletions.pdf}");
        pw.println("\\includegraphics[height=3.5cm]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_2D_deletions.pdf}");
        pw.println("\\end{figure}");
        
        pw.println("\\subsection*{" + id + " read identity}");
        pw.println("\\vspace{-3mm}");
        pw.println("\\begin{figure}[H]");
        pw.println("\\centering");
        pw.println("\\includegraphics[height=3.5cm]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName()+ "_Template_length_vs_identity_hist.pdf}");
        pw.println("\\includegraphics[height=3.5cm]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_Complement_length_vs_identity_hist.pdf}");
        pw.println("\\includegraphics[height=3.5cm]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_2D_length_vs_identity_hist.pdf} \\\\");        
        pw.println("\\includegraphics[height=3.5cm]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName()+ "_Template_length_vs_identity_scatter.pdf}");
        pw.println("\\includegraphics[height=3.5cm]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_Complement_length_vs_identity_scatter.pdf}");
        pw.println("\\includegraphics[height=3.5cm]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_2D_length_vs_identity_scatter.pdf} \\\\");        
        pw.println("\\includegraphics[height=3.5cm]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName()+ "_Template_read_fraction_vs_alignment_identity_scatter.pdf}");
        pw.println("\\includegraphics[height=3.5cm]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_Complement_read_fraction_vs_alignment_identity_scatter.pdf}");
        pw.println("\\includegraphics[height=3.5cm]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_2D_read_fraction_vs_alignment_identity_scatter.pdf}");        
        pw.println("\\end{figure}");
        
        pw.println("\\subsection*{" + id + " coverage}");
        pw.println("\\vspace{-3mm}");
        pw.println("\\begin{figure}[H]");
        pw.println("\\centering");
        pw.println("\\includegraphics[height=3cm]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName()+ "_Template_coverage.pdf}");
        pw.println("\\includegraphics[height=3cm]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_Complement_coverage.pdf}");
        pw.println("\\includegraphics[height=3cm]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_2D_coverage.pdf}");
        pw.println("\\includegraphics[height=3cm]{" + options.getGraphsDir() + options.getSeparator() + refSeq.getName() + "_gc.pdf}");
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

            overallStats.getStatsByType(type).getMotifStatistics().writeInsertionLogoImage(listType, options.getGraphsDir()+options.getSeparator()+"logo_insertion_"+typeString+"_k"+k+".png", k);
            overallStats.getStatsByType(type).getMotifStatistics().writeDeletionLogoImage(listType, options.getGraphsDir()+options.getSeparator()+"logo_deletion_"+typeString+"_k"+k+".png", k);
            overallStats.getStatsByType(type).getMotifStatistics().writeSubstitutionLogoImage(listType, options.getGraphsDir()+options.getSeparator()+"logo_substitution_"+typeString+"_k"+k+".png", k);
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
                
                if (insertionMotifs[type].size() > insertionPos) {
                    pw.printf(" & %s (%.2f\\%%)", insertionMotifs[type].get(insertionPos).getKey(), insertionMotifs[type].get(insertionPos).getValue());
                } else {
                    pw.print(" &");
                }

                if (deletionMotifs[type].size() > deletionPos) {
                    pw.printf(" & %s (%.2f\\%%)", deletionMotifs[type].get(deletionPos).getKey(), deletionMotifs[type].get(deletionPos).getValue());
                } else {
                    pw.print(" &");
                }

                if (substitutionMotifs[type].size() > substitutionPos) {
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

            pw.print(" & \\includegraphics[height=0.5cm]{" + options.getGraphsDir()+options.getSeparator()+"logo_insertion_"+typeString+"_k"+k + ".png}");
            pw.print(" & \\includegraphics[height=0.5cm]{" + options.getGraphsDir()+options.getSeparator()+"logo_deletion_"+typeString+"_k"+k + ".png}");
            pw.print(" & \\includegraphics[height=0.5cm]{" + options.getGraphsDir()+options.getSeparator()+"logo_substitution_"+typeString+"_k"+k + ".png}");
        }

        pw.println(" \\\\");
    }
    
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
    
    public void addReferencePlots(References refs) {
        List<String> keys = new ArrayList<String>(refs.getAllIds());
        Collections.sort(keys);
        for (String id : keys) {
            writeReferenceSection(refs.getReferenceById(id));
        }
    }
    
    private void writeLaTeXFooter() {
        pw.println("\\end{document}");
    }
    
    public PrintWriter getPrintWriter() {
        return pw;
    }    
    
    public void writeReport() {
        open();
        beginLengthsSection();        
        for (int type = 0; type<3; type++) {            
            addReadSet(overallStats.getStatsByType(type));            
        }
        endLengthsSection();
        
        for (int type=0; type<3; type++) {            
            writeAlignmentsSection(overallStats.getStatsByType(type));            
            references.writeReferenceStatFiles(type);
            references.writeReferenceSummary(type);
            references.writeTexSummary(type, pw);
        }
        
        Set<String> ids = references.getAllIds();
        for (String id : ids) {
            writeReferenceSection(references.getReferenceById(id));
        }
        
        writeMotifSection();
        writeSubstitutionErrorsSection();        
        
        writeLaTeXFooter();
        close();
    }
}
