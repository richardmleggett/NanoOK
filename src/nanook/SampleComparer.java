/*
 * Program: NanoOK
 * Author:  Richard M. Leggett
 * 
 * Copyright 2015 The Genome Analysis Centre (TGAC)
 */

package nanook;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 *
 * @author Richard Leggett
 */
public class SampleComparer {
    private NanoOKOptions options;
    private ArrayList<String> sampleNames = new ArrayList();
    private ArrayList<OverallStats> sampleStats = new ArrayList();
    
    public SampleComparer(NanoOKOptions o) {
        options = o;
    }
    
    private void readSample(String sample) {
        try {
            FileInputStream fis = new FileInputStream(sample + File.separator + "analysis" + File.separator + "OverallStats.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            OverallStats os = (OverallStats)ois.readObject();
            sampleNames.add(new File(sample).getName());
            sampleStats.add(os);
            ois.close();
        } catch (Exception e) {
            if (e instanceof InvalidClassException) {
                System.out.println("The saved data is incompatible with this version of NanoOK. You must re-run nanook analyse on all your samples before running compare.");
            } else {
                System.out.println("Exception trying to read object:");
                e.printStackTrace();
            }
            System.exit(1);            
        }
    }
    
    public void loadSamples() {    
        try
        {
            BufferedReader br = new BufferedReader(new FileReader(options.getSampleList()));
            String line;
            
            do {
                line = br.readLine();
                if (line != null) {
                    if (!line.startsWith("SampleDir")) {
                        String[] fields = line.split("\t");
                        readSample(fields[0]);
                    }
                }
            } while (line != null);            
            br.close();
        } catch (Exception e) {
            System.out.println("parseFile Exception:");
            e.printStackTrace();
            System.exit(1);
        }    
    }
    
    public void compareSamples() {
        try {
            for (int type = 0; type<3; type++) {    
                if (options.isProcessingReadType(type)) {
                    String filename = options.getComparisonDir() + File.separator + NanoOKOptions.getTypeFromInt(type) + "_comparison.txt";
                    PrintWriter pw = new PrintWriter(new FileWriter(filename, false));
                    
                    pw.println("Name\tNumReads\tTotalBases\tMeanLen\tLongest\tShortest\tN50\tN50Count\tN90\tN90Count");
                    
                    for (int i=0; i<sampleStats.size(); i++) {
                        String name = sampleNames.get(i);
                        OverallStats overallStats = sampleStats.get(i);
                        ReadSetStats r = overallStats.getStatsByType(type);           

                        pw.printf("%s\t%d\t%d\t%.2f\t%d\t%d\t%d\t%d\t%d\t%d",
                                  name, r.getNumReads(), r.getTotalBases(), r.getMeanLength(), r.getLongest(), r.getShortest(), r.getN50(), r.getN50Count(), r.getN90(), r.getN90Count());
                        pw.println("");
                    }
                    
                    pw.close();                
                }                
            }                    
        } catch (IOException e) {
            System.out.println("AlignmentsTableFile exception");
            e.printStackTrace();
        }
    }
    
    public OverallStats getSample(int i) {
        return sampleStats.get(i);
    }
}
