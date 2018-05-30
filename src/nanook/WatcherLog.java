/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package nanook;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;


public class WatcherLog {
    private transient PrintWriter pw = null;
    private String filename = null;

    public WatcherLog(NanoOKOptions options) {
    }
    
    public synchronized void open(String f, boolean clearLogs) {
        if (clearLogs) {
            filename = f + ".log";
        } else {
            DateFormat df = new SimpleDateFormat("ddMMyy_HHmmss");
            Date dateobj = new Date();
            filename = f + "_" + df.format(dateobj).toString()+".log";
        }
        
        System.out.println("Opening "+filename);
        
        try {
            pw = new PrintWriter(new FileWriter(filename, true));
        } catch (IOException e) {
            System.out.println("WatcherLog exception");
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
