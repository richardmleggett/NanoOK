/*
 * Program: NanoOK
 * Author:  Richard M. Leggett (richard.leggett@earlham.ac.uk)
 * 
 * Copyright 2015-17 Earlham Institute
 */

package nanook;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Logging
 * 
 * @author Richard Leggett
 */
public class NanoOKLog  implements Serializable {
    private static final long serialVersionUID = NanoOK.SERIAL_VERSION;
    private transient PrintWriter pw = null;
    
    public NanoOKLog() {
    }
        
    public synchronized void open(String filename) {
        try {
            pw = new PrintWriter(new FileWriter(filename, false));
        } catch (IOException e) {
            System.out.println("NanoOKLog exception");
            e.printStackTrace();
        }        
    }
    
    public synchronized void close() {
        if (pw != null) {
            pw.close();
        }
    }
    
    public String getTime() {
        GregorianCalendar timeNow = new GregorianCalendar();
        String s = String.format("%d/%d/%d %02d:%02d:%02d",
                                 timeNow.get(Calendar.DAY_OF_MONTH),
                                 timeNow.get(Calendar.MONTH)+1,
                                 timeNow.get(Calendar.YEAR),
                                 timeNow.get(Calendar.HOUR_OF_DAY),
                                 timeNow.get(Calendar.MINUTE),
                                 timeNow.get(Calendar.SECOND));
        return s;
    }

    public synchronized void writeTimeStamp() {
        if (pw != null) {
        }
    }    
    
    public synchronized void print(String s) {
        if (pw != null) {
            pw.print(getTime() + " " + s);
        }
    }
        
    public synchronized void println(String s) {
        if (pw != null) {
            pw.println(getTime() + " " + s);
        }
    }
    
    public synchronized PrintWriter getPrintWriter() {
        return pw;
    }    
}
