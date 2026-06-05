package com.mycompany.imagej;

import java.awt.AWTEvent;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

public class Peer_To_Peer_Operations implements PlugIn, DialogListener {
    
    private ImagePlus imagePlus;
    private ImageProcessor originalProcessor;
    private ImageProcessor workingProcessor;
    
    private double brightness = 0;
    private double contrast = 0;
    private double solarization = 0;
    private double desaturation = 1;
    
    @Override
    public void run(String arg) {
        imagePlus = ij.WindowManager.getCurrentImage();
        
        if (imagePlus == null) {
            IJ.error("Nenhuma imagem aberta!");
            return;
        }

        if (imagePlus.getType() != ImagePlus.COLOR_RGB) {
            IJ.error("A imagem deve ser do tipo RGB.");
            return;
        }
        
        originalProcessor = imagePlus.getProcessor().duplicate();
        workingProcessor = imagePlus.getProcessor().duplicate();
        
        showDialog();
    }
    
    private void showDialog() {
        GenericDialog dialog = new GenericDialog("Peer-to-Peer Operations");
        
        dialog.addSlider("Brilho (Brightness):", -255, 255, brightness);
        dialog.addSlider("Contraste (Contrast):", -255, 255, contrast);
        dialog.addSlider("Solarização (Solarization):", 0, 255, solarization);
        dialog.addSlider("Dessaturação (Desaturation):", 0, 1, desaturation, 0.01);
        
        dialog.addDialogListener(this);
        dialog.showDialog();
    }
    
    @Override
    public boolean dialogItemChanged(GenericDialog dialog, AWTEvent event) {
        if (dialog.wasCanceled()) {
            imagePlus.setProcessor(originalProcessor.duplicate());
            imagePlus.repaintWindow();
            return true;
        }
        
        brightness = dialog.getNextNumber();
        contrast = dialog.getNextNumber();
        solarization = dialog.getNextNumber();
        desaturation = dialog.getNextNumber();
        
        brightness = Math.max(-255, Math.min(255, brightness));
        contrast = Math.max(-255, Math.min(255, contrast));
        solarization = Math.max(0, Math.min(255, solarization));
        desaturation = Math.max(0, Math.min(1, desaturation));
        
        if (brightness != 0) {
            applyBrightness(workingProcessor, brightness);
        }
        
        if (contrast != 0) {
            applyContrast(workingProcessor, contrast);
        }
        
        if (solarization > 0) {
            applySolarization(workingProcessor, solarization);
        }
        
        if (desaturation > 0) {
            applyDesaturation(workingProcessor, desaturation);
        }
        
        imagePlus.setProcessor(workingProcessor);
        imagePlus.repaintWindow();
        
        return true;
    }
    
    private void applyBrightness(ImageProcessor processor, double value) {
        byte[] pixels = (byte[]) processor.getPixels();
        
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i] & 0xFF;
            pixel += value;
            pixel = Math.max(0, Math.min(255, pixel));
            pixels[i] = (byte) pixel;
        }
    }
    
    private void applyContrast(ImageProcessor processor, double value) {
        byte[] pixels = (byte[]) processor.getPixels();
        int midpoint = 128;
        
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i] & 0xFF;
            // Aplicar contraste em torno do ponto médio (128)
            pixel = (int) (midpoint + (pixel - midpoint) * value);
            pixel = Math.max(0, Math.min(255, pixel));
            pixels[i] = (byte) pixel;
        }
    }
    
    private void applySolarization(ImageProcessor processor, double value) {
        byte[] pixels = (byte[]) processor.getPixels();
        int threshold = (int) (255 * (value / 100.0));
        
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i] & 0xFF;
            if (pixel < threshold) {
                pixel = 255 - pixel;
            }
            pixels[i] = (byte) pixel;
        }
    }

    private void applyDesaturation(ImageProcessor processor, double value) {
        // Converter para escala de cinza parcial
        int[] rgbPixels = (int[]) processor.getPixels();
        double factor = value / 100.0;
        
        for (int i = 0; i < rgbPixels.length; i++) {
            int rgb = rgbPixels[i];
            
            // Extrair componentes RGB
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            
            // Calcular valor em escala de cinza
            int gray = (int) (0.299 * r + 0.587 * g + 0.114 * b);
            
            // Interpolar entre cor original e cinza
            int newR = (int) (r * (1 - factor) + gray * factor);
            int newG = (int) (g * (1 - factor) + gray * factor);
            int newB = (int) (b * (1 - factor) + gray * factor);
            
            newR = Math.max(0, Math.min(255, newR));
            newG = Math.max(0, Math.min(255, newG));
            newB = Math.max(0, Math.min(255, newB));
            
            rgbPixels[i] = (0xFF << 24) | (newR << 16) | (newG << 8) | newB;
        }
    }
}
