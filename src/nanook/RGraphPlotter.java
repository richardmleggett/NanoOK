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
    private int nScriptsToRun;
    private int nScriptCounter = 1;
    String logFilename;

    /**
     * Constructor.
     * @param o NanoOKOptions object
     */
    public RGraphPlotter(NanoOKOptions o) {
        options = o;
        logFilename = options.getLogsDir() + File.separator + "R_output_log.txt";
    }
    
    
    public void runScript(String scriptName, String refName) {
        String command = "Rscript " + options.getScriptsDir() + File.separator + scriptName + " " + options.getBaseDirectory() + " " + options.getSample();
        
        if (refName != null) {
            command = command + " " + refName;
        }
        
        System.out.print("\r"+nScriptCounter+"/"+nScriptsToRun);
        
        ProcessLogger pl = new ProcessLogger();
        pl.runCommand(command, logFilename, nScriptCounter == 1 ? false:true);

        nScriptCounter++;
    }
    
    /**
     * Execute plot commands.
     * @param references References object containing all references
     */
    public void plot(References references) {
        String s = null;

        nScriptsToRun = 1 + (references.getNumberOfReferences() * 3);
         
        System.out.println("R log " + logFilename);
        
        runScript("nanook_plot_lengths.R ", null);
       
        Set<String> ids = references.getAllIds();
        for (String id : ids) {
            String name = references.getReferenceById(id).getName();
            runScript("nanook_plot_alignments.R", name);
            runScript("nanook_plot_indels.R", name);
            runScript("nanook_plot_read_identity.R", name);
        }          
        
        System.out.println("");
    }
}
