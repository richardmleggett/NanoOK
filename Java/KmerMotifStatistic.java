package nanotools;

import java.util.*;

public class KmerMotifStatistic {
    public final static int TYPE_TOP = 1;
    public final static int TYPE_BOTTOM = 2;
    private int kSize;
    private Hashtable<String, Integer> motifs = new Hashtable();
    private Hashtable<String, Double> motifsPercent = new Hashtable();
    private int totalCount = 0;
    private int[][] baseCounts;
    
    public KmerMotifStatistic(int s) {
        kSize = s;
        baseCounts = new int[4][kSize];
    }
    
    public void addMotif(String kmer) {
        Integer currentCount = motifs.get(kmer);
        
        if (currentCount == null) {
            currentCount = new Integer(1);
        } else {
            currentCount++;
        }
        
        motifs.put(kmer, currentCount);
        
        totalCount++;
        
        //System.out.println("Adding motif "+kmer+" to size "+kSize);
    }
    
    private void updateBaseCounts(String motif, int count) {
        for (int i=0; i<motif.length(); i++) {
            switch(motif.charAt(i)) {
                case 'A': baseCounts[0][i]+=count; break;
                case 'C': baseCounts[1][i]+=count; break;
                case 'G': baseCounts[2][i]+=count; break;
                case 'T': baseCounts[3][i]+=count; break;
            }
        }
    }
    
    public void calculateMotifs() {
        Set<String> keys = motifs.keySet();
        
        for(String motif : keys) {
            int count = motifs.get(motif);
            double percent = (100.0 * (double)count) / (double)totalCount;
            motifsPercent.put(motif, percent);
            //updateBaseCounts(motif, count);
        }
    }
    
    public void calculateTopBaseCounts() {
        ArrayList<Map.Entry<String, Integer>> list = getSortedMotifCounts();
        for (int i=0; i<10; i++) {
            if (i < list.size()) {
                String motif = list.get(i).getKey();
                updateBaseCounts(motif, list.get(i).getValue());
            }
        }
    }

    public void calculateBottomBaseCounts() {
        ArrayList<Map.Entry<String, Integer>> list = getSortedMotifCounts();
        for (int i=0; i<10; i++) {
            if (i >= 0) {
                String motif = list.get(list.size() - 1 - i).getKey();
                updateBaseCounts(motif, list.get(list.size() - 1 - i).getValue());
            }
        }
    }
    
    public void writeLogoImage(int type, String filename) {
        baseCounts = new int[4][kSize];
        if (type == TYPE_TOP) {
            calculateTopBaseCounts();
        } else if (type == TYPE_BOTTOM) {
            calculateBottomBaseCounts();
        } else {
            System.out.println("Error: wrong type to writeLogoImgae");
            System.exit(1);
        }
        
        SequenceLogo sl = new SequenceLogo(kSize);
        for (int i=0; i<kSize; i++) {           
            sl.addBase(i, baseCounts[0][i], baseCounts[1][i], baseCounts[2][i], baseCounts[3][i]);
        }
        sl.drawImage();
        sl.saveImage(filename);
    }
    
    public ArrayList<Map.Entry<String, Integer>> getSortedMotifCounts() {
        ArrayList<Map.Entry<String, Integer>>list = new ArrayList(motifs.entrySet());
        
        Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
            public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2){
                return o2.getValue().compareTo(o1.getValue());
        }});        

        return list;
    }

    public ArrayList<Map.Entry<String, Double>> getSortedMotifPercentages() {
        if (motifsPercent.size() == 0) {
            calculateMotifs();
        }
                
        ArrayList<Map.Entry<String, Double>>list = new ArrayList(motifsPercent.entrySet());
        
        Collections.sort(list, new Comparator<Map.Entry<String, Double>>() {
            public int compare(Map.Entry<String, Double> o1, Map.Entry<String, Double> o2){
                return o2.getValue().compareTo(o1.getValue());
        }});        

        //for (int i=1; i<=10; i++) {
        //    if (list.size() >= i) {
        //        System.out.println(i + ". " + list.get(i-1).getKey() + "\t" + list.get(i-1).getValue());
        //    }
        //}        
        
        return list;
    }    
    
    public void outputMotifCounts() {
        ArrayList<Map.Entry<String, Integer>>list = getSortedMotifCounts();
        
        for (int i=1; i<=10; i++) {
            if (list.size() >= i) {
                System.out.println(i + ". " + list.get(i-1).getKey() + "\t" + list.get(i-1).getValue());
            }
        }
        
        System.out.println(list);
    }
    
    public int getTotalMotifCount() {
        return totalCount;
    }
}
