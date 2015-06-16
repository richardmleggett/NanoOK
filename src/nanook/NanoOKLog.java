package nanook;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Logging
 * 
 * @author Richard Leggett
 */
public class NanoOKLog {
    PrintWriter pw = null;
    
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

    public synchronized void print(String s) {
        if (pw != null) {
            pw.print(s);
        }
    }
        
    public synchronized void println(String s) {
        if (pw != null) {
            pw.println(s);
        }
    }
    
    public synchronized PrintWriter getPrintWriter() {
        return pw;
    }    
}
