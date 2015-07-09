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
    
    private void readSample(String sample, String name) {
        try {
            FileInputStream fis = new FileInputStream(sample + File.separator + "analysis" + File.separator + "OverallStats.ser");
            ObjectInputStream ois = new ObjectInputStream(fis);
            OverallStats os = (OverallStats)ois.readObject();
            sampleNames.add(name);
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
                        readSample(fields[0], fields[1]);
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
                    
                    filename = options.getComparisonDir() + File.separator + NanoOKOptions.getTypeFromInt(type) + "_map_summary.txt";
                    pw = new PrintWriter(new FileWriter(filename, false));
                    References refs = sampleStats.get(0).getStatsByType(type).getOptions().getReferences();
                    ArrayList<ReferenceSequence> sortedRefs = refs.getSortedReferences();
                    pw.print("Sample");
                    for (int i=0; i<sortedRefs.size(); i++) {
                        ReferenceSequence rs = sortedRefs.get(i);
                        pw.print("\t" + rs.getName());
                    }
                    pw.println("\tUnaligned");
                    for (int i=0; i<sampleStats.size(); i++) {
                        String name = sampleNames.get(i);
                        OverallStats overallStats = sampleStats.get(i);
                        pw.print(name);
                        for (int j=0; j<sortedRefs.size(); j++) {
                            ReferenceSequence rs = overallStats.getStatsByType(type).getOptions().getReferences().getReferenceById(sortedRefs.get(j).getId());
                            double value = 0.0;
                            
                            if (rs.getStatsByType(type).getNumberOfReadsWithAlignments() > 0) {
                                value = 100.0 * (double)rs.getStatsByType(type).getNumberOfReadsWithAlignments() / (double)overallStats.getStatsByType(type).getNumberOfReads();
                            }
                            
                            pw.printf("\t%.4f", value);
                        }
                        
                        double value = 0;                        
                        if (overallStats.getStatsByType(type).getNumberOfReadsWithoutAlignments() > 0) {
                            value = 100.0 * (double)overallStats.getStatsByType(type).getNumberOfReadsWithoutAlignments() / (double)overallStats.getStatsByType(type).getNumberOfReads();
                        }
                        pw.printf("\t%.4f", value);
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
    
    public int getNumberOfSamples() {
        return sampleStats.size();
    }
    
    public OverallStats getSample(int i) {
        return sampleStats.get(i);
    }
    
    public String getSampleName(int i) {
        return sampleNames.get(i);
    }
}
