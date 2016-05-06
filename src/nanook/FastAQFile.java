/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nanook;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Represent FASTA/FASTQ file
 * @author leggettr
 */
public class FastAQFile {
    private String id;
    private String sequence;
    private String qualities;
    
    /**
     * Constructor
     * @param i id
     * @param s sequence string
     * @param q qualities string
     */
    public FastAQFile(String i, String s, String q) {
        id = i;
        sequence = s;
        qualities = q;
    }
    
    /**
     * Write as FASTQ file
     * @param filename output filename
     */
    public void writeFastq(String filename) {
        PrintWriter pw;
        
        try {
            pw = new PrintWriter(new FileWriter(filename));
            pw.print("@");
            pw.println(id);
            pw.println(sequence);
            pw.println("+");
            pw.println(qualities);
            pw.close();            
        } catch (IOException e) {
            System.out.println("writeFastaFile exception");
            e.printStackTrace();
        }
    }
    
    /**
     * Write as FASTA file
     * 
     * @param filename output filename
     */
    public void writeFasta(String filename, String fast5Path) {
        PrintWriter pw;
        
        try {
            pw = new PrintWriter(new FileWriter(filename));
            pw.print(">");
            pw.print(id);
            if (fast5Path != null) {
                pw.print(" "+fast5Path);
            }
            pw.println("");
            pw.println(sequence);
            pw.close();            
        } catch (IOException e) {
            System.out.println("writeFastaFile exception");
            e.printStackTrace();
        }
    }
}
