/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nanook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * Represent FASTA/FASTQ file
 * @author leggettr
 */
public class MergedFastAQFile {
    private NanoOKOptions options;
    private String mergedFilename;
    private int nSeqs = 0;
    private int seqsPerFile = 500;
    private int fileCounter = 0;
    private ArrayList mergeList = new ArrayList();
    
    public MergedFastAQFile(NanoOKOptions o, String f) {
        mergedFilename = f;
        options = o;
        seqsPerFile = options.getReadsPerBlast();
    }
    
    public synchronized void addFile(String readFilename, String fast5Path) {
        mergeList.add(readFilename);
        nSeqs++;
        if (nSeqs == seqsPerFile) {
            System.out.println("Adding new thread...");
            options.getThreadExecutor().execute(new FastAQBlastMerger(options, mergedFilename, mergeList, fileCounter));
            mergeList = new ArrayList(); 
            fileCounter++;
            nSeqs = 0;
        }
    }
}
