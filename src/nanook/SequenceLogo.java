/*
 * Program: NanoOK
 * Author:  Richard M. Leggett
 * 
 * Copyright 2015 The Genome Analysis Centre (TGAC)
 */

package nanook;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import javax.imageio.ImageIO;

/**
 * Create sequence logo (for error motifs etc.)
 * 
 * @author Richard Leggett
 */
public class SequenceLogo {
    private BufferedImage bImage;
    private int size = 0;
    private double[][] counts;
    private String[] bases = {"A", "C", "G", "T"};
    private Color[] baseColours = {Color.GREEN, Color.BLUE, Color.YELLOW, Color.RED};
    private int imageWidth = 0;
    private int imageHeight = 0;
    private int charWidth = 0;
    private int charHeight = 0;
    
    /**
     * Constructor
     * @param size size (in bases) 
     */
    public SequenceLogo(int s) {
        size = s;
        counts = new double[4][size];
    }

    /**
     * Debugging constructor
     */
    public SequenceLogo() {
        this(6);
        this.addBase(0, 25, 25, 25, 25);
        this.addBase(1, 25, 25, 25, 25);
        this.addBase(2, 50, 0, 0, 50);
        this.addBase(3, 100, 0, 0, 0);
        this.addBase(4, 10, 10, 30, 50);
        this.addBase(5, 33, 33, 0, 34);
    }    

    /**
     * Set relative counts at a given position in the logo.
     * @param position position (0-offset)
     * @param a number of As
     * @param c number of Cs
     * @param g number of Gs
     * @param t number of Ts
     */
    public void addBase(int position, int a, int c, int g, int t) {
        if (position < size) {
            counts[0][position] = (double)a / (double)(a + c + g + t);
            counts[1][position] = (double)c / (double)(a + c + g + t);
            counts[2][position] = (double)g / (double)(a + c + g + t);
            counts[3][position] = (double)t / (double)(a + c + g + t);
        } else {
            System.out.println("Warning: bad index passed to addBase.");
        }
    }
        
    /**
     * Draw the logo image.
     */
    public void drawImage() {
        // Create temporary image to work out sizing
        bImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = bImage.createGraphics();
        AffineTransform stretch;
        Font f = new Font("Arial", Font.BOLD, 40);
        FontMetrics metrics = g.getFontMetrics(f);
        g.setFont(f);
        charWidth = metrics.charWidth('G');        
        charHeight = metrics.charWidth('G');        
        
        // Re-create image at right size
        imageWidth = size * charWidth;
        imageHeight = charHeight*4;
        bImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_RGB);
        g = bImage.createGraphics();
        g.setFont(f);
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, imageWidth, imageHeight);
        //System.out.println("imagesize " + imageWidth + ", " + imageHeight);
        
        for (int i=0; i<size; i++) {
            double drawY = (double)imageHeight;
            for (int j=0; j<4; j++) {
                if (counts[j][i] > 0.0) {
                    double yStretch = counts[j][i] * 4;
                    int drawX = i * charWidth;
                    stretch = AffineTransform.getScaleInstance(1.0, yStretch);
                    g.setTransform(stretch);
                    g.setColor(baseColours[j]);
                    //System.out.println(bases[j] + " at "+drawX+", "+drawY+" with stretch "+yStretch);
                    g.drawString(bases[j], drawX, (int)(drawY / yStretch));
                    drawY -= (yStretch * (double)charHeight);
                }
            }
            //System.out.println("");
        }
        
    }
    
    /**
     * Save the logo as an image.
     * @param filename output filename
     */
    public void saveImage(String filename) {
        try {
            ImageIO.write(bImage, "PNG", new File(filename));
        }
        catch (Exception e)
        {
            System.out.println(e);
        }
    }
}
