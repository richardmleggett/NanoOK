package nanook;

import java.io.*;
import java.util.Set;

/**
 * Executes command to plot graphs with R.
 * 
 * @author Richard Leggett
 */
public class RGraphPlotter {
    private NanoOKOptions options;

    /**
     * Constructor.
     * @param o NanoOKOptions object
     */
    public RGraphPlotter(NanoOKOptions o) {
        options = o;
    }
    
    /**
     * Execute plot commands.
     * @param references References object containing all references
     */
    public void plot(References references) {
       String s = null;
 
        try {         
            String command="Rscript "+options.getScriptsDir()+options.getSeparator()+"nanook_plot_lengths.R "+options.getBaseDirectory()+" "+options.getSample();
            System.out.println("Executing "+command);
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();
            
            Set<String> ids = references.getAllIds();
            for (String id : ids) {
                String name = references.getReferenceById(id).getName();
                command="Rscript "+options.getScriptsDir()+options.getSeparator()+"nanook_plot_alignments.R "+options.getBaseDirectory()+" "+options.getSample() + " " + name;
                System.out.println("Executing "+command);
                p = Runtime.getRuntime().exec(command);
                p.waitFor();                
                
                command="Rscript "+options.getScriptsDir()+options.getSeparator()+"nanook_plot_indels.R "+options.getBaseDirectory()+" "+options.getSample() + " " + name;
                System.out.println("Executing "+command);
                p = Runtime.getRuntime().exec(command);
                p.waitFor();

                command="Rscript "+options.getScriptsDir()+options.getSeparator()+"nanook_plot_read_identity.R "+options.getBaseDirectory()+" "+options.getSample() + " " + name;
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
