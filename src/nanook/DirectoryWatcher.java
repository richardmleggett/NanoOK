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


public class DirectoryWatcher implements FileAlterationListener {
    private WatchService watcher = null;
    private final Map<WatchKey,Path> keys;
    private NanoOKOptions options;
    private ReadAligner aligner;
    private AlignmentFileParser parser;
    private boolean keepWatching = true;
    
    public DirectoryWatcher(NanoOKOptions o, ReadAligner a, AlignmentFileParser p) {    
        options = o;
        aligner = a;
        parser = p;

        keys = new HashMap<WatchKey,Path>();        

            
    }

    public void onStop(FileAlterationObserver observer) {};
    public void onStart(FileAlterationObserver observer) {};
    public void onFileDelete(File file) {};
    public void onFileChange(File file) {};
    public void onDirectoryDelete(File directory) {};
    public void onDirectoryCreate(File directory) {};
    public void onDirectoryChange(File directory) {};
    
    public void onFileCreate(File file) {
        Path child = file.toPath();

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

            options.getThreadExecutor().execute(new WatcherRunnable(options, child.getParent().toString(), child.getFileName().toString(), pf, fastaqDir, alignDir, parser));
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
        checkAndMakeDirectory(options.getReadDir() + File.separator + pf);               
        checkAndMakeDirectory(options.getLogsDir() + File.separator + "blastn_card" + File.separator + pf);               
        checkAndMakeDirectory(options.getLogsDir() + File.separator + "blastn_nt" + File.separator + pf);               
        checkAndMakeDirectory(options.getSampleDirectory() + File.separator + "blastn_card" + File.separator + pf);               
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
        checkAndMakeDirectory(options.getFast5Dir());
        checkAndMakeDirectory(options.getFast5Dir() + File.separator + "pass");
        checkAndMakeDirectory(options.getFast5Dir() + File.separator + "fail");
        checkAndMakeDirectory(options.getLogsDir());               
        checkAndMakeDirectory(options.getReadDir());               
        checkAndMakeDirectory(options.getLogsDir() + File.separator + "blastn_card");     
        checkAndMakeDirectory(options.getLogsDir() + File.separator + "blastn_bacteria");
        checkAndMakeDirectory(options.getLogsDir() + File.separator + "blastn_nt");           
        checkAndMakeDirectory(options.getSampleDirectory() + File.separator + "blastn_card");               
        checkAndMakeDirectory(options.getSampleDirectory() + File.separator + "blastn_bacteria");
        checkAndMakeDirectory(options.getSampleDirectory() + File.separator + "blastn_nt");               
        
        System.out.println("Opening logs");
        options.getWatcherReadLog().open(options.getLogsDir() + File.separator + "watcher_reads", options.clearLogsOnStart());
        options.getWatcherCardFileLog().open(options.getLogsDir() + File.separator + "watcher_CARD_files", options.clearLogsOnStart());
        options.getWatcherCardCommandLog().open(options.getLogsDir() + File.separator + "watcher_CARD_commands", options.clearLogsOnStart());
        options.getWatcherntFileLog().open(options.getLogsDir() + File.separator + "watcher_nt_files", options.clearLogsOnStart());
        options.getWatcherntCommandLog().open(options.getLogsDir() + File.separator + "watcher_nt_commands", options.clearLogsOnStart());        
        
        options.getMergerCardPass().open(options.getSampleDirectory() + File.separator + "blastn_card" + File.separator + "all_pass_blastn_card", options.clearLogsOnStart());
        options.getMergerCardFail().open(options.getSampleDirectory() + File.separator + "blastn_card" + File.separator + "all_fail_blastn_card", options.clearLogsOnStart());
        //options.getMergerntPass().open(options.getSampleDirectory() + File.separator + "blastn_nt" + File.separator + "all_pass_blastn_nt", options.clearLogsOnStart());
        //options.getMergerntFail().open(options.getSampleDirectory() + File.separator + "blastn_nt" + File.separator + "all_fail_blastn_nt", options.clearLogsOnStart());
        
        System.out.println("Watching for new files...");
        try {
            FileAlterationMonitor monitor = new FileAlterationMonitor(500);

            watcher = FileSystems.getDefault().newWatchService();

            if (options.isProcessingPassReads()) {
                String dirName = options.getFast5Dir() + File.separator + "pass";
                Path passDir = Paths.get(dirName);
                
                options.openMergedFile(options.getReadDir() + File.separator + options.getSample()+"_pass_1d", NanoOKOptions.TYPE_TEMPLATE, NanoOKOptions.READTYPE_PASS);
                options.openMergedFile(options.getReadDir() + File.separator + options.getSample()+"_pass_2d", NanoOKOptions.TYPE_2D, NanoOKOptions.READTYPE_PASS);

                FileAlterationObserver observer = new FileAlterationObserver(dirName);
                observer.addListener(this);
                monitor.addObserver(observer);
                
                System.out.println("Watching "+dirName);
                makeDirs("pass");
            }
            
            if (options.isProcessingFailReads()) {                
                String dirName = options.getFast5Dir() + File.separator + "fail";
                Path failDir = Paths.get(dirName);

                options.openMergedFile(options.getReadDir() + File.separator + options.getSample()+"_fail_1d", NanoOKOptions.TYPE_TEMPLATE, NanoOKOptions.READTYPE_FAIL);
                options.openMergedFile(options.getReadDir() + File.separator + options.getSample()+"_fail_2d", NanoOKOptions.TYPE_2D, NanoOKOptions.READTYPE_FAIL);

                FileAlterationObserver observer = new FileAlterationObserver(dirName);
                observer.addListener(this);
                monitor.addObserver(observer);
                
                System.out.println("Watching "+dirName);
                makeDirs("fail");
            }
            
            System.out.println("Waiting...\n");
            monitor.start();
            while (keepWatching) {
                Thread.sleep(1000);
            }
            monitor.stop();
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
        options.getMergerCardPass().close();
        options.getMergerntPass().close();
        options.getMergerCardFail().close();
        options.getMergerntFail().close();
    }
}
