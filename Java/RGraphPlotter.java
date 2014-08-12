package nanotools;

import java.io.*;
import java.util.Set;

public class RGraphPlotter {
    private NanotoolsOptions options;

    public RGraphPlotter(NanotoolsOptions o) {
        options = o;
    }
    
    public void makeSummaryPdf(String summaryPdf) {
        String command;
        String[] cmds = {"/bin/sh",
                         "-c",
                         "cat " + options.getLengthSummaryFilename() + " " + options.getAlignmentSummaryFilename() + " | /Users/leggettr/Documents/Software/text2pdf/text2pdf -s8 -v8 -c100 -t4 > " + summaryPdf
                        };
        System.out.println("Executing "+cmds[2]);

        try {
            Process p = Runtime.getRuntime().exec(cmds);
            p.waitFor();
            BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream())); 
        } catch (Exception e) {
            System.out.println("Exception:");
            e.printStackTrace();
            System.exit(-1);
        }  
    }
    
    public void plot(References references) {
       String s = null;
       String analysisDir = options.getBaseDirectory()+ options.getSeparator() + options.getSample() + options.getSeparator() + "analysis" + options.getSeparator();
       String summaryPdf = analysisDir + options.getSample() + "_summary.pdf";
       String pdfs = summaryPdf + " ";
 
        try {         
            String command="Rscript "+options.getScriptsDir()+options.getSeparator()+"nanotools_plot_lengths.R "+options.getBaseDirectory()+" "+options.getSample();
            System.out.println("Executing "+command);
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();
            
            pdfs += analysisDir + options.getSample()+ "_length_plots.pdf";
            
            Set<String> ids = references.getAllIds();
            for (String id : ids) {
                String name = references.getReferenceById(id).getName();
                command="Rscript "+options.getScriptsDir()+options.getSeparator()+"nanotools_plot_alignments.R "+options.getBaseDirectory()+" "+options.getSample() + " " + name;
                System.out.println("Executing "+command);
                p = Runtime.getRuntime().exec(command);
                p.waitFor();
                pdfs += " " + analysisDir + name + "_alignment_plots.pdf";
            }
            
            makeSummaryPdf(summaryPdf);
                                    
            command="/opt/pdflabs/pdftk/bin/pdftk "+pdfs+" cat output "+ options.getBaseDirectory()+ options.getSeparator() + options.getSample() + options.getSeparator() + options.getSample()+ "_plots.pdf";
            System.out.println("Executing "+command);
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            
            
            //BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getInputStream())); 
            //BufferedReader stdError = new BufferedReader(new InputStreamReader(p.getErrorStream()));
 
            // read the output from the command
            //System.out.println("Here is the standard output of the command:\n");
            //while ((s = stdInput.readLine()) != null) {
            //    System.out.println(s);
            //}
             
            // read any errors from the attempted command
            //System.out.println("Here is the standard error of the command (if any):\n");
            //while ((s = stdError.readLine()) != null) {
            //    System.out.println(s);
            //}
             
            //System.exit(0);
            
        }
        catch (Exception e) {
            System.out.println("Exception:");
            e.printStackTrace();
            System.exit(-1);
        }        
    }
}
