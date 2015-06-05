/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package nanook;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author leggettr
 */
public class NanoOKLog {
    PrintWriter pw = null;
    
    public NanoOKLog() {
    }
    
    public void open(String filename) {
        try {
            pw = new PrintWriter(new FileWriter(filename, false));
        } catch (IOException e) {
            System.out.println("NanoOKLog exception");
            e.printStackTrace();
        }        
    }
    
    public void close() {
        if (pw != null) {
            pw.close();
        }
    }

    public void print(String s) {
        if (pw != null) {
            pw.print(s);
        }
    }
        
    public void println(String s) {
        if (pw != null) {
            pw.println(s);
        }
    }
    
    public PrintWriter getPrintWriter() {
        return pw;
    }    
}
