/*
 * Program: NanoOK
 * Author:  Richard M. Leggett (richard.leggett@earlham.ac.uk)
 * 
 * Copyright 2015-17 Earlham Institute
 */
package nanook;

import java.io.File;
import java.util.ArrayList;

public class SampleChecker {
    private NanoOKOptions options;
    private boolean haveChecked = false;
    private boolean usingBarcodes = false;
    private boolean usingBatchDirs = false;
    private boolean usingPassFailDirs = false;
    
    public SampleChecker(NanoOKOptions o) {
        options = o;
    }
    
    private boolean dirExists(String dir) {
        File d = new File(dir);
        return d.exists();
    }
    
    private void checkForBarcodeAndBatch(String dir) {
        File d = new File(dir);
        File[] listOfFiles = d.listFiles();
        boolean contains = false;
        if (listOfFiles == null) {
            contains = false;
        } else if (listOfFiles.length <= 0) {
            contains = false;
        } else {
            for (File file : listOfFiles) {
                if (file.isDirectory()) {
                    if (file.getName().startsWith("BC") || file.getName().startsWith("barcode")) {
                        usingBarcodes = true;
                        checkForBarcodeAndBatch(file.getPath());
                    } else if (file.getName().startsWith("batch_")) {
                        usingBatchDirs = true;
                        break;
                    }
                }
            }
        }        
    }

    private void showDirectoryType() {
        System.out.println("  Using pass/fail dirs: " + (usingPassFailDirs?"yes":"no"));
        System.out.println("      Using batch dirs: " + (usingBatchDirs?"yes":"no"));
        System.out.println("        Using barcodes: " + (usingBarcodes?"yes":"no"));
        System.out.println("");
    }
    
    public void checkFast5Directory() {
        String passDir = options.getFast5Dir() + File.separator + "pass";
        String failDir = options.getFast5Dir() + File.separator + "fail";
        
        System.out.println("Checking FAST5 directory structure...");
        
        File f = new File(options.getFast5Dir());
        if (!f.exists()) {
            System.out.println("Error: can't find FAST5 directory "+options.getFast5Dir());
            System.exit(1);
        }
        
        if ((options.isProcessingPassReads()) && (dirExists(passDir))) {
            usingPassFailDirs = true;
            checkForBarcodeAndBatch(passDir);
        } else if ((options.isProcessingFailReads()) && (dirExists(failDir))) {
            usingPassFailDirs = true;
            checkForBarcodeAndBatch(failDir);
        } else {
            usingPassFailDirs = false;
            usingBatchDirs = false;
        }
        
        showDirectoryType();
    }
    
    public void checkReadDirectory() {
        boolean gotOne = false;
        ArrayList<String> al = new ArrayList<String>();
        
        System.out.println("Checking FASTA/Q directory structure...");

        File f = new File(options.getReadDir());
        if (!f.exists()) {
            System.out.println("Error: can't find read directory "+options.getReadDir());
            System.exit(1);
        }        
        
        // Check for MinKNOW 1.4.2 and above
        if ((options.isProcessingPassReads()) && (options.isProcessing2DReads())) {
            al.add(new String(options.getReadDir() + File.separator + "2D" + File.separator + "pass"));
        }
        if ((options.isProcessingPassReads()) && (options.isProcessingTemplateReads())) {
            al.add(new String(options.getReadDir() + File.separator + "Template" + File.separator + "pass"));
        }
        if ((options.isProcessingPassReads()) && (options.isProcessingComplementReads())) {
            al.add(new String(options.getReadDir() + File.separator + "Complement" + File.separator + "pass"));
        }
        if ((options.isProcessingFailReads()) && (options.isProcessing2DReads())) {
            al.add(new String(options.getReadDir() + File.separator + "2D" + File.separator + "fail"));
        }       
        if ((options.isProcessingFailReads()) && (options.isProcessingTemplateReads())) {
            al.add(new String(options.getReadDir() + File.separator + "Template" + File.separator + "fail"));
        }        
        if ((options.isProcessingFailReads()) && (options.isProcessingComplementReads())) {
            al.add(new String(options.getReadDir() + File.separator + "Complement" + File.separator + "fail"));
        }        
        for (int i=0; i<al.size(); i++) {
            if (dirExists(al.get(i))) {
                gotOne = true;
                usingPassFailDirs = true; 
                checkForBarcodeAndBatch(al.get(i));
            }
        }
        
        // MinKNOW pre 1.4.2 and after intro of pass/fail dirs
        // Barcode dirs will only be for pass reads
        if (gotOne == false) {
            if ((options.isProcessingPassReads()) && (dirExists(options.getReadDir() + File.separator + "pass"))) {
                usingBatchDirs = false;
                usingPassFailDirs = true;      
                checkForBarcodeAndBatch(options.getReadDir() + File.separator + "pass");
            } else if ((options.isProcessingFailReads()) && (dirExists(options.getReadDir() + File.separator + "fail"))) {
                usingBatchDirs = false;
                usingPassFailDirs = true;            
            }
            // Original - no pass/fail dirs and nop barcodes
            else {
                usingBatchDirs = false;
                usingPassFailDirs = false;
            }
        }

        showDirectoryType();
    }    
    
    public boolean haveChecked() {
        return haveChecked;
    }
    
    public boolean usingBarcodes() {
        return usingBarcodes;
    }
    
    public boolean usingBatchDirs() {
        return usingBatchDirs;
    }
    
    public boolean usingPassFailDirs() {
        return usingPassFailDirs;
    }
}
