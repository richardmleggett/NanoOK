package nanook;

import java.io.File;
import java.util.*;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.concurrent.ThreadPoolExecutor;

public class FileWatcher {
    private NanoOKOptions options;
    private int filesToProcess = 0;
    private int filesProcessed = 0;
    private int lastCompleted = -1;
    private long lastFileTime = System.nanoTime();
    private long secsSinceLast = 0;
    private ArrayList<String> dirsToWatch = new ArrayList();
    private Hashtable<String, Integer> allFiles = new Hashtable();
    private LinkedList<String> pendingFiles = new LinkedList<String>();
    
    public FileWatcher(NanoOKOptions o) {
        options = o;
    }
    
    public FileWatcher(NanoOKOptions o, String d) {
        options = o;
        dirsToWatch.add(d);
    }
    
    public void addWatchDir(String d) {
        options.getLog().println("Added watch dir: "+d);
        dirsToWatch.add(d);
    }
    
    public synchronized void addPendingFile(String s) {
        pendingFiles.add(s);
        filesToProcess++;
    }
    
    public synchronized String getPendingFile() {
        if (pendingFiles.size() > 0) {
            filesProcessed++;
            return pendingFiles.removeFirst();
        } else {
            return null;
        }
    }

    public void writeProgress() {
        long e = 0;
        long s = NanoOKOptions.PROGRESS_WIDTH;
        
        if (filesToProcess > 0) {
            e = NanoOKOptions.PROGRESS_WIDTH * filesProcessed / filesToProcess;
            s = NanoOKOptions.PROGRESS_WIDTH - e;
        }
        
        System.out.print("\rProcessing [");
        for (int i=0; i<e; i++) {
            System.out.print("=");
        }
        for (int i=0; i<s; i++) {
            System.out.print(" ");
        }
        System.out.print("] " + filesProcessed +"/" +  filesToProcess);
        lastCompleted = filesProcessed;
    }    
    
    public void scan() {
        int count = 0;
        for (int i=0; i<dirsToWatch.size(); i++) {
            String dirName = dirsToWatch.get(i);
            File d = new File(dirName);
            File[] listOfFiles = d.listFiles();

            options.getLog().println("Scanning "+dirName);

            if (listOfFiles == null) {
                System.out.println("");
                System.out.println("Directory "+dirName+" doesn't exist");
            } else if (listOfFiles.length <= 0) {
                System.out.println("");
                System.out.println("Directory "+dirName+" empty");
            } else {
                for (File file : listOfFiles) {
                    if (file.isFile()) {
                        if (!file.getName().startsWith(("."))) {
                            if (!allFiles.containsKey(file.getPath())) {
                                count++;
                                options.getLog().println("Got file "+file.getPath());
                                allFiles.put(file.getPath(), 1);
                                this.addPendingFile(file.getPath());
                            }
                        }
                    }
                }            
            }    
        }
        
        options.getLog().println("Found "+count + " new files.");

        if (count == 0) {
            long timeSince = System.nanoTime() - lastFileTime;
            secsSinceLast = timeSince / 1000000000;
            options.getLog().println("Not seen file for " + (secsSinceLast) + "s");
        } else {
            lastFileTime = System.nanoTime();
        }
    }
    
    public long getSecsSinceLastFile() {
        return secsSinceLast;
    }
    
    public int getPendingFiles() {
        return pendingFiles.size();
    }
    
    public boolean timedOut() {
        if (pendingFiles.size() == 0) {        
            if (secsSinceLast >= options.getFileWatcherTimeout()) {
                return true;
            }
        }
        
        return false;               
    }
}
