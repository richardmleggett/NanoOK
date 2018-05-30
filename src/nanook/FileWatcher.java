/*
 * Program: NanoOK
 * Author:  Richard M. Leggett (richard.leggett@earlham.ac.uk)
 * 
 * Copyright 2015-17 Earlham Institute
 */

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
    private ArrayList<FileWatcherItem> batchContainersToWatch = new ArrayList();
    private ArrayList<FileWatcherItem> fileDirsToWatch = new ArrayList();
    private Hashtable<String, Integer> batchDirs = new Hashtable();
    private Hashtable<String, Integer> allFiles = new Hashtable();
    private LinkedList<FileWatcherItem> pendingFiles = new LinkedList<FileWatcherItem>();
    
    public FileWatcher(NanoOKOptions o) {
        options = o;
    }
    
    //public FileWatcher(NanoOKOptions o, String d) {
    //    options = o;
    //    fileDirsToWatch.add(new FileWatcherDir(d, pf));
    //}
    
    public void addBatchContainer(String d, int pf) {
        options.getLog().println("Added batch dir: "+d);
        batchContainersToWatch.add(new FileWatcherItem(d, pf));
    }
    
    public void addWatchDir(String d, int pf) {
        options.getLog().println("Added watch dir: "+d);
        fileDirsToWatch.add(new FileWatcherItem(d, pf));
    }
    
    public synchronized void addPendingFile(String s, int pf) {
        pendingFiles.add(new FileWatcherItem(s, pf));
        filesToProcess++;
    }
    
    public synchronized FileWatcherItem getPendingFile() {
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
    
    private void checkForNewBatchDirs() {
        int count = 0;
        for (int i=0; i<batchContainersToWatch.size(); i++) {
            FileWatcherItem dir = batchContainersToWatch.get(i);
            int pf = dir.getPassOrFail();
            String dirName = dir.getPathname();
            File d = new File(dirName);
            File[] listOfFiles = d.listFiles();

            options.getLog().println("Scanning for new batch dirs "+dirName);

            if (listOfFiles == null) {
                options.getLog().println("Directory "+dirName+" doesn't exist");
            } else if (listOfFiles.length <= 0) {
                options.getLog().println("Directory "+dirName+" empty");
            } else {
                for (File file : listOfFiles) {
                    if (file.isDirectory()) {
                        if (!file.getName().startsWith(("."))) {
                            if (!batchDirs.containsKey(file.getPath())) {
                                count++;
                                options.getLog().println("Got batch dir "+file.getPath());
                                batchDirs.put(file.getPath(), 1);
                                fileDirsToWatch.add(new FileWatcherItem(file.getPath(), pf));
                            }
                        }
                    }
                }            
            }    
        }
    }
    
    public void scan() {
        int count = 0;
                
        if (options.usingBatchDirs()) {
            checkForNewBatchDirs();
        }
        
        for (int i=0; i<fileDirsToWatch.size(); i++) {
            FileWatcherItem dir = fileDirsToWatch.get(i);
            String dirName = dir.getPathname();
            File d = new File(dirName);
            File[] listOfFiles = d.listFiles();

            options.getLog().println("Scanning "+dirName);

            if (listOfFiles == null) {
                options.getLog().println("Directory "+dirName+" doesn't exist");
            } else if (listOfFiles.length <= 0) {
                options.getLog().println("Directory "+dirName+" empty");
            } else {
                for (File file : listOfFiles) {
                    if (file.isFile()) {
                        if (!file.getName().startsWith(("."))) {
                            if (!allFiles.containsKey(file.getPath())) {
                                count++;
                                options.getLog().println("Got file "+file.getPath());
                                allFiles.put(file.getPath(), 1);
                                this.addPendingFile(file.getPath(), dir.getPassOrFail());
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
