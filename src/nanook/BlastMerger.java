/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nanook;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;


public class BlastMerger {
    private transient PrintWriter pw = null;
    private String filename = null;

    public BlastMerger(NanoOKOptions options) {
    }
    
    public synchronized void open(String f, boolean clearLogs) {
        if (clearLogs) {
            filename = f + ".blast.txt";
        } else {
            DateFormat df = new SimpleDateFormat("ddMMyy_HHmmss");
            Date dateobj = new Date();
            filename = f + "_" + df.format(dateobj).toString()+".blast.txt";
        }
        System.out.println("Opening "+filename);
        
        try {
            pw = new PrintWriter(new FileWriter(filename, true));
        } catch (IOException e) {
            System.out.println("NanoOKLog exception");
            e.printStackTrace();
        }        
    }
    
    public synchronized void mergeFile(String fileToMerge) {
        try {
            String line;
            BufferedReader br = new BufferedReader(new FileReader(fileToMerge));
			while ((line = br.readLine()) != null) {
				if (!line.startsWith("#")) {
                    pw.println(line);
                }
			}
            pw.flush();
            br.close();
        } catch (Exception e) {
            System.out.println("BlastMerger exception");
            e.printStackTrace();
        }
    }
    
    public synchronized void close() {
        if (pw != null) {
            pw.close();
        }
    }

    public synchronized void print(String s) {
        if (pw != null) {
            pw.print(s);
            pw.flush();
        }
    }
        
    public synchronized void println(String s) {
        if (pw != null) {
            pw.println(s);
            pw.flush();
        }
    }
    
    public synchronized PrintWriter getPrintWriter() {
        return pw;
    }    
}
