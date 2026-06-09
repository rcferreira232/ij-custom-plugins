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
        dialog.addSlider("Contraste (Contrast):", -128, 128, contrast);
        dialog.addSlider("Solarização (Solarization):", 0, 255, solarization);
        dialog.addSlider("Dessaturação (Desaturation):", 0, 1, desaturation, 0.01);
        
        dialog.addDialogListener(this);
        dialog.showDialog();

        if (dialog.wasCanceled()) {
            imagePlus.setProcessor(originalProcessor);
            imagePlus.repaintWindow();
        }
    }
    
    @Override
    public boolean dialogItemChanged(GenericDialog dialog, AWTEvent event) {
        brightness = dialog.getNextNumber();
        contrast = dialog.getNextNumber();
        solarization = dialog.getNextNumber();
        desaturation = dialog.getNextNumber();
        
        brightness = Math.max(-255, Math.min(255, brightness));
        contrast = Math.max(-128, Math.min(128, contrast));
        solarization = Math.max(0, Math.min(255, solarization));
        desaturation = Math.max(0, Math.min(1, desaturation));

        int width = originalProcessor.getWidth();
        int height = originalProcessor.getHeight();

        int pixelValue[] = {0, 0, 0};
        int newPixelValue[] = {0, 0, 0};

        float contrastFactor = calcContrastFactor((int)contrast);

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixelValue = originalProcessor.getPixel(x, y, pixelValue);
                
                int media = (pixelValue[0] + pixelValue[1] + pixelValue[2]) / 3;

                newPixelValue[0] = calcPixel(pixelValue[0], (int)brightness, (int)contrast, desaturation, (int)solarization, media, contrastFactor);
                newPixelValue[1] = calcPixel(pixelValue[1], (int)brightness, (int)contrast, desaturation, (int)solarization, media, contrastFactor);
                newPixelValue[2] = calcPixel(pixelValue[2], (int)brightness, (int)contrast, desaturation, (int)solarization, media, contrastFactor);

                workingProcessor.putPixel(x, y, newPixelValue);
            }
        }
        
        imagePlus.setProcessor(workingProcessor);
        imagePlus.repaintWindow();
        
        return true;
    }

    public int clamp(int value) {
        return Math.max(0, Math.min(255, value));
    }
    
    public int calcPixel(int pixelValue, int brightness, int contrast, Double desaturation, int solarization, int media, float fatorC) {
		
		pixelValue = clamp(calcBrightness(pixelValue, brightness));
		pixelValue = clamp(calcContrast(pixelValue, fatorC));
		pixelValue = clamp(calcDesaturation(pixelValue, desaturation, media));
		pixelValue = clamp(calcSolarization(pixelValue, solarization));
		
		return pixelValue;	
	}
	
	public float calcContrastFactor(int contrast) {
		float fator = (259f*(contrast + 255f))/(255f*(259f-contrast));
		return fator;
	}

	public int calcBrightness(int pixelValue, int brightness) {
		pixelValue = pixelValue + brightness;
		return pixelValue;
	}
	
	public int calcContrast(int pixelValue, float fatorC ) {
		pixelValue = (int)((pixelValue-128) * fatorC + 128);
		return pixelValue;
	}
	
	public int calcDesaturation(int pixelValue, Double desaturation, int media){
		if(desaturation<1) {
			pixelValue = (int)(media + ((pixelValue - media) * desaturation));	
		}
		return pixelValue;
	}
	
	
	public int calcSolarization(int pixelValue, int solarization) {
		if(pixelValue < solarization) {
			pixelValue = 255 - pixelValue;
		}
		return pixelValue;
	}
}
