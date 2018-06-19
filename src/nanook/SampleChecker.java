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
    
    private boolean checkIfDirHasSubdirs(String dir) {
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
                    contains = true;
                    break;
                }
            }
        }
        
        return contains;
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
            boolean foundSubDir = false;
            for (File file : listOfFiles) {
                if (file.isDirectory()) {
                    if (file.getName().startsWith("BC") || file.getName().startsWith("barcode")) {
                        usingBarcodes = true;
                        if (usingBatchDirs == false) {
                            if (checkIfDirHasSubdirs(file.getPath())) {
                                usingBatchDirs = true;
                            }
                        }
                        //checkForBarcodeAndBatch(file.getPath());
                    } else if (file.getName().startsWith("batch_")) {
                        usingBatchDirs = true;
                        break;
                    } else {
                        foundSubDir = true;
                        break;
                    }
                }
            }
            
            if ((usingBarcodes == false) && (usingBatchDirs == false) && (foundSubDir == true)) {
                System.out.println("Found subdirectory, assuming batched output");
                usingBatchDirs = true;
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
            checkForBarcodeAndBatch(options.getFast5Dir());
            //File[] listOfFiles = f.listFiles();
            //usingPassFailDirs = false;
            //usingBatchDirs = false;
            //for (File file : listOfFiles) {
            //    if (file.isDirectory()) {
            //        usingBatchDirs = true;
            //        break;                    
            //    }
            //}
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
            al.add(new String(options.getReadDir() + File.separator + "pass" + File.separator + "2D"));
        }
        if ((options.isProcessingPassReads()) && (options.isProcessingTemplateReads())) {
            al.add(new String(options.getReadDir() + File.separator + "pass" + File.separator + "Template"));
        }
        if ((options.isProcessingPassReads()) && (options.isProcessingComplementReads())) {
            al.add(new String(options.getReadDir() + File.separator + "pass" + File.separator + "Complement"));
        }
        if ((options.isProcessingFailReads()) && (options.isProcessing2DReads())) {
            al.add(new String(options.getReadDir() + File.separator + "fail" + File.separator + "2D"));
        }       
        if ((options.isProcessingFailReads()) && (options.isProcessingTemplateReads())) {
            al.add(new String(options.getReadDir() + File.separator + "fail" + File.separator + "Template"));
        }        
        if ((options.isProcessingFailReads()) && (options.isProcessingComplementReads())) {
            al.add(new String(options.getReadDir() + File.separator + "fail" + File.separator + "Complement"));
        }        
        for (int i=0; i<al.size(); i++) {
            if (dirExists(al.get(i))) {
                gotOne = true;
                usingPassFailDirs = true; 
                checkForBarcodeAndBatch(al.get(i));
            }
        }
        
        // Original - no pass/fail dirs, no barcodes, no batch
        // Or Albacore - with separate directories
        if (gotOne == false) {
            System.out.println("Error: FASTA/Q directory structure not understood.");
            System.out.println("This may be because it was created with an earlier version of NanoOK.");
            System.out.println("NanoOK now expects the following structures:");
            System.out.println("    sampledir/fasta/pass/Template/*");
            System.out.println(" or sampledir/fasta/pass/Template/batch_XXX/*");
            System.out.println(" or sampledir/fasta/pass/Template/0/*");
            System.out.println("etc.");
            System.exit(0);
        }

        showDirectoryType();
    }        
    
    public void checkReadDirectorOld() {
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
                gotOne = true;
                usingBatchDirs = false;
                usingPassFailDirs = true;      
                checkForBarcodeAndBatch(options.getReadDir() + File.separator + "pass");
            } else if ((options.isProcessingFailReads()) && (dirExists(options.getReadDir() + File.separator + "fail"))) {
                gotOne = true;
                usingBatchDirs = false;
                usingPassFailDirs = true;            
            }
        }
        
        // Albacore - we end up with sample/fasta/2D/0 etc.
        if (gotOne == false) {
            al.clear();
            if (options.isProcessing2DReads()) {
                al.add(new String(options.getReadDir() + File.separator + "2D"));
            }
            if (options.isProcessingTemplateReads()) {
                al.add(new String(options.getReadDir() + File.separator + "Template"));
            }
            if (options.isProcessingComplementReads()) {
                al.add(new String(options.getReadDir() + File.separator + "Complement"));
            }
            for (int i=0; i<al.size(); i++) {
                if (dirExists(al.get(i))) {
                    gotOne = true;
                    checkForBarcodeAndBatch(al.get(i));
                }
            }
        }
        
        // Original - no pass/fail dirs, no barcodes, no batch
        // Or Albacore - with separate directories
        if (gotOne == false) {
            usingBatchDirs = false;
            usingPassFailDirs = false;
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
