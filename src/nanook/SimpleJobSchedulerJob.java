package nanook;

import java.io.File;
import java.lang.ProcessBuilder.Redirect;

public class SimpleJobSchedulerJob {
    private String[] commands;
    private Process process;
    private String logFilename;
    private int jobId;
    
    public SimpleJobSchedulerJob(int i, String[] c, String l) {
        jobId = i;
        commands = c;
        logFilename = l;
    }
   
    public void run() {
        try {
            File log = new File(logFilename);            
            ProcessBuilder pb = new ProcessBuilder(commands);
            pb.redirectErrorStream(true);
            pb.redirectOutput(Redirect.appendTo(log));
            process = pb.start();
            //process = Runtime.getRuntime().exec(this.getCommand());
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
    
    public boolean hasFinished() {
        return process.isAlive() ? false:true;
    }
    
    public String getCommand() {
        String command = "";
        
        for (int i=0; i<commands.length; i++) {
            if (i > 0) {
                command = command + " ";
            }
            
            command = command + commands[i];
        }
        return command;
    }
    
    public int getId() {
        return jobId;
    }
    
    public String getLog() {
        return logFilename;
    }
}
