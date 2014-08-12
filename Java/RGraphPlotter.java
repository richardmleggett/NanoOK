package nanotools;

import java.io.*;
import java.util.Set;

public class RGraphPlotter {
    private NanotoolsOptions options;

    public RGraphPlotter(NanotoolsOptions o) {
        options = o;
    }
    
    public void plot(References references) {
       String s = null;
 
        try {         
            String command="Rscript "+options.getScriptsDir()+options.getSeparator()+"nanotools_plot_lengths.R "+options.getBaseDirectory()+" "+options.getSample();
            System.out.println("Executing "+command);
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();
            
            Set<String> ids = references.getAllIds();
            for (String id : ids) {
                String name = references.getReferenceById(id).getName();
                command="Rscript "+options.getScriptsDir()+options.getSeparator()+"nanotools_plot_alignments.R "+options.getBaseDirectory()+" "+options.getSample() + " " + name;
                System.out.println("Executing "+command);
                p = Runtime.getRuntime().exec(command);
                p.waitFor();                
            }          
        } catch (Exception e) {
            System.out.println("RGraphPlotter exception:");
            e.printStackTrace();
            System.exit(1);
        }        
    }
}
