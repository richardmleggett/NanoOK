package nanook;

import java.io.*;
import java.util.*;
import java.io.File;
import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import static java.nio.file.LinkOption.*;
import java.nio.file.attribute.*;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.io.monitor.FileAlterationListener;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;


public class DirectoryWatcherNative implements FileAlterationListener {
    private WatchService watcher = null;
    private final Map<WatchKey,Path> keys;
    private NanoOKOptions options;
    private ReadAligner aligner;
    private AlignmentFileParser parser;
    private boolean keepWatching = true;
    
    public DirectoryWatcherNative(NanoOKOptions o, ReadAligner a, AlignmentFileParser p) {    
        options = o;
        aligner = a;
        parser = p;

        keys = new HashMap<WatchKey,Path>();        
    }

    @SuppressWarnings("unchecked")
    static <T> WatchEvent<T> cast(WatchEvent<?> event) {
        return (WatchEvent<T>)event;
    }    
    
    public void onFileCreate(File file) {
        Path child = file.toPath();
        System.out.println("Created: "+file.getName());

        if (file.getName().toString().equals("stop")) {
            keepWatching = false;
            System.out.println("Stopping...");
        } else if (file.getName().toString().endsWith(".fast5")) {
            // print out event
            System.out.println("Got new file " + file.getName());
            String pf = child.getName(child.getNameCount() - 2).toString();
            String fastaqDir = child.getParent().getParent().getParent().toString() + File.separator + "fasta" + File.separator + pf;
            String alignDir = options.getAlignerDir() + File.separator + pf;
            String logDir = options.getLogsDir() + File.separator + options.getAligner() + File.separator + pf;                

            //executor.execute(new WatcherRunnable(options, child.getParent().toString(), child.getFileName().toString(), fastaqDir, alignDir, parser));
        }
    
    }
    
    /**
     * Process all events for keys queued to the watcher
     */
    void processEvents() {
        System.out.println("Waiting...\n");
        while (keepWatching) {

            // wait for key to be signalled
            WatchKey key;
            try {
                key = watcher.take();
            } catch (InterruptedException x) {
                return;
            }

            Path dir = keys.get(key);
            if (dir == null) {
                System.err.println("WatchKey not recognized!!");
                continue;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind kind = event.kind();

                // Context for directory entry event is the file name of entry
                WatchEvent<Path> ev = cast(event);
                Path name = ev.context();
                Path child = dir.resolve(name);

                System.out.println("File "+child.getFileName().toString());
                if (child.getFileName().toString().equals("stop")) {
                    keepWatching = false;
                    System.out.println("Stopping...");
                } else if (child.getFileName().toString().endsWith(".fast5")) {
                    // print out event
                    System.out.println("Got new file " + child);
                    String pf = child.getName(child.getNameCount() - 2).toString();
                    String fastaqDir = child.getParent().getParent().getParent().toString() + File.separator + "fasta" + File.separator + pf;
                    String alignDir = options.getAlignerDir() + File.separator + pf;
                    String logDir = options.getLogsDir() + File.separator + options.getAligner() + File.separator + pf;                

                    //executor.execute(new WatcherRunnable(options, child.getParent().toString(), child.getFileName().toString(), fastaqDir, alignDir, parser));
                }
            }

            // reset key and remove from set if directory no longer accessible
            boolean valid = key.reset();
            if (!valid) {
                keys.remove(key);

                // all directories are inaccessible
                if (keys.isEmpty()) {
                    break;
                }
            }
        }
    }    

    private void checkAndMakeDirectory(String dir) {
        File f = new File(dir);
        if (f.exists()) {
            if (!f.isDirectory()) {
                System.out.println("Error: " + dir + " is a file, not a directory!");
                System.exit(1);
            }
        } else {
            System.out.println("Making directory " + dir);
            f.mkdir();
        }
    }    
    
    private void makeDirs(String pf) {
        checkAndMakeDirectory(options.getReadDir());               
        checkAndMakeDirectory(options.getReadDir() + File.separator + pf);               
        checkAndMakeDirectory(options.getLogsDir());               
        checkAndMakeDirectory(options.getLogsDir() + File.separator + "blastn_card");               
        checkAndMakeDirectory(options.getLogsDir() + File.separator + "blastn_nt");               
        checkAndMakeDirectory(options.getLogsDir() + File.separator + "blastn_card" + File.separator + pf);               
        checkAndMakeDirectory(options.getLogsDir() + File.separator + "blastn_nt" + File.separator + pf);               
        checkAndMakeDirectory(options.getSampleDirectory() + File.separator + "blastn_card");               
        checkAndMakeDirectory(options.getSampleDirectory() + File.separator + "blastn_card" + File.separator + pf);               
        checkAndMakeDirectory(options.getSampleDirectory() + File.separator + "blastn_nt");               
        checkAndMakeDirectory(options.getSampleDirectory() + File.separator + "blastn_nt" + File.separator + pf);               
                
        // Make output Template, Complement and 2D directories
        for (int t=0; t<3; t++) {
            if (options.isProcessingReadType(t)) {
                checkAndMakeDirectory(options.getReadDir() + File.separator + pf + File.separator + NanoOKOptions.getTypeFromInt(t));
                checkAndMakeDirectory(options.getSampleDirectory() + File.separator + "blastn_card" + File.separator + pf + File.separator + NanoOKOptions.getTypeFromInt(t));               
                checkAndMakeDirectory(options.getSampleDirectory() + File.separator + "blastn_nt" + File.separator + pf + File.separator + NanoOKOptions.getTypeFromInt(t));               
                checkAndMakeDirectory(options.getLogsDir() + File.separator + "blastn_card" + File.separator + pf + File.separator + NanoOKOptions.getTypeFromInt(t));               
                checkAndMakeDirectory(options.getLogsDir() + File.separator + "blastn_nt" + File.separator + pf + File.separator + NanoOKOptions.getTypeFromInt(t));               
            }
        }
    }
    
    public void watch() {
        System.out.println("Opening logs");
        options.getWatcherReadLog().open(options.getLogsDir() + File.separator + "watcher_reads", options.clearLogsOnStart());
        options.getWatcherCardFileLog().open(options.getLogsDir() + File.separator + "watcher_CARD_files", options.clearLogsOnStart());
        options.getWatcherCardCommandLog().open(options.getLogsDir() + File.separator + "watcher_CARD_commands", options.clearLogsOnStart());
        options.getWatcherntFileLog().open(options.getLogsDir() + File.separator + "watcher_nt_files", options.clearLogsOnStart());
        options.getWatcherntCommandLog().open(options.getLogsDir() + File.separator + "watcher_nt_commands", options.clearLogsOnStart());        
        
        System.out.println("Watching for new files...");
        try {
            watcher = FileSystems.getDefault().newWatchService();

            if (options.isProcessingPassReads()) {
                String dirName = options.getFast5Dir() + File.separator + "pass";
                Path passDir = Paths.get(dirName);

            FileAlterationObserver observer = new FileAlterationObserver(dirName);
            FileAlterationMonitor monitor = new FileAlterationMonitor(500);
            observer.addListener(this);
            monitor.addObserver(observer);
            monitor.start();
                
                System.out.println("Watching "+dirName);
                makeDirs("pass");
                //WatchKey passKey = passDir.register(watcher, ENTRY_CREATE);
                //keys.put(passKey, passDir);
            }
            
            if (options.isProcessingFailReads()) {                
                String dirName = options.getFast5Dir() + File.separator + "fail";
                Path failDir = Paths.get(dirName);
                System.out.println("Watching "+dirName);
                makeDirs("fail");
                //WatchKey failKey = failDir.register(watcher, ENTRY_CREATE);
                //keys.put(failKey, failDir);
            }
            
            this.processEvents();
        } catch (Exception e) {
            System.out.println("ReadExtractor exception:");
            e.printStackTrace();
            System.exit(1);
        }
        
        System.out.println("Closing logs");
        options.getWatcherReadLog().close();
        options.getWatcherCardFileLog().close();
        options.getWatcherCardCommandLog().close();
        options.getWatcherntFileLog().close();
        options.getWatcherntCommandLog().close(); 
    }
    
    public void onStop(FileAlterationObserver observer) {};
    public void onStart(FileAlterationObserver observer) {};
    public void onFileDelete(File file) {};
    public void onFileChange(File file) {};
    public void onDirectoryDelete(File directory) {};
    public void onDirectoryCreate(File directory) {};
    public void onDirectoryChange(File directory) {};
}
