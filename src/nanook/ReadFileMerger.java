/*
 * Program: NanoOK
 * Author:  Richard M. Leggett
 * 
 * Copyright 2015-2017 The Earlham Institute (formerly The Genome Analysis Centre)
 */

package nanook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class ReadFileMerger {
    private NanoOKOptions options;
    private ArrayList<String>[][] readFiles = new ArrayList[2][3];
    
    public ReadFileMerger(NanoOKOptions o) {    
        options = o;

        for (int pf = 0; pf<2; pf++) {
            for (int type=0; type<3; type++) {
                readFiles[pf][type] = new ArrayList<String>();
            }
        }
    }
    
    public synchronized void addReadFile(String pathname, int type) {
        int pf = NanoOKOptions.READTYPE_PASS;
        
        if (pathname.contains("/fail/")) {
            pf = NanoOKOptions.READTYPE_FAIL;
        }
                
        readFiles[pf-1][type].add(pathname);
    }
    
    public void writeMergedFiles() {
        for (int pf = 0; pf<2; pf++) {
            for (int type=0; type<3; type++) {
                if (readFiles[pf][type].size() > 0) {
                    String outputPathname = options.getReadDir() + File.separator +
                           options.getSample() + "_all_" +
                           NanoOKOptions.getTypeFromInt(type) + "_" +
                           NanoOKOptions.getPassFailFromInt(pf + 1) +
                           (options.getReadFormat() == NanoOKOptions.FASTA ? ".fasta":".fastq");
                    
                    System.out.println("Writing " + outputPathname);
                    
                    try {
                        PrintWriter pw = new PrintWriter(new FileWriter(outputPathname));
                        for (int i=0; i<readFiles[pf][type].size(); i++) {
                            BufferedReader br = new BufferedReader(new FileReader(readFiles[pf][type].get(i)));
                            String line;                
                            while ((line = br.readLine()) != null) {
                                pw.println(line);
                            }
                            br.close();
                        }
                        pw.close();
                    } catch (IOException e) {
                        System.out.println("writeMergedFiles exception");
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
