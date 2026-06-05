package com.mycompany.imagej;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ColorProcessor;
import ij.process.ImageProcessor;

public class Convert_Rgb_To_Grey implements PlugIn {
    
    private static final int METHOD_AVERAGE = 0;
    private static final int METHOD_LUMINOSITY = 1;
    private static final int METHOD_LIGHTNESS = 2;
    
    private int selectedMethod = METHOD_LUMINOSITY;
    private boolean createNewImage = true;
    
    @Override
    public void run(String arg) {
        ImagePlus imagePlus = ij.WindowManager.getCurrentImage();
        
        if (imagePlus == null) {
            IJ.error("Nenhuma imagem aberta!");
            return;
        }
        
        if (imagePlus.getType() != ImagePlus.COLOR_RGB) {
            IJ.error("A imagem deve ser do tipo RGB!");
            return;
        }
        
        if (!showDialog()) {
            return;
        }

        convertToGrayscale(imagePlus);
    }
    
    private boolean showDialog() {
        GenericDialog dialog = new GenericDialog("Converter RGB para Cinza");
        
        dialog.addMessage("Selecione o método de conversão:");
        String[] methods = {
            "Método da Média (Average)",
            "Método da Luminosidade (ITU-R 601-2)",
            "Método da Claridade (Lightness)"
        };
        dialog.addChoice("Método:", methods, methods[selectedMethod]);
        
        dialog.addMessage("");
        dialog.addCheckbox("Criar nova imagem (manter original)", createNewImage);
        
        dialog.showDialog();
        
        if (dialog.wasCanceled()) {
            return false;
        }
        
        String selected = dialog.getNextChoice();
        if (selected.contains("Média")) {
            selectedMethod = METHOD_AVERAGE;
        } else if (selected.contains("Luminosidade")) {
            selectedMethod = METHOD_LUMINOSITY;
        } else {
            selectedMethod = METHOD_LIGHTNESS;
        }
        
        createNewImage = dialog.getNextBoolean();
        
        return true;
    }
    
    private void convertToGrayscale(ImagePlus imagePlus) {
        ImageProcessor processor = imagePlus.getProcessor();
        
        if (!(processor instanceof ColorProcessor)) {
            IJ.error("Processador não é do tipo ColorProcessor!");
            return;
        }

        ImageProcessor grayProcessor = convertToGrayscaleProcessor(processor);
        
        if (createNewImage) {
            ImagePlus grayImage = new ImagePlus(imagePlus.getTitle() + " (Cinza)", grayProcessor);
            grayImage.show();
        } else {
            imagePlus.setProcessor(grayProcessor);
            imagePlus.repaintWindow();
        }
    }
    
    private ImageProcessor convertToGrayscaleProcessor(ImageProcessor sourceProcessor) {
        int width = sourceProcessor.getWidth();
        int height = sourceProcessor.getHeight();

        ImageProcessor targetProcessor = new ij.process.ByteProcessor(width, height);

        int pixelValue[] = {0, 0, 0};
        int newPixelValue[] = {0};

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixelValue = sourceProcessor.getPixel(x, y, pixelValue);
                int r = pixelValue[0];
                int g = pixelValue[1];
                int b = pixelValue[2];
                
                newPixelValue[0] = calculateGrayscale(r, g, b);
                targetProcessor.putPixel(x, y, newPixelValue);
            }
        }

        return targetProcessor;
    }
    
    private int calculateGrayscale(int r, int g, int b) {
        int gray;
        
        switch (selectedMethod) {
            case METHOD_AVERAGE:
                gray = (r + g + b) / 3;
                break;
                
            case METHOD_LUMINOSITY:
                gray = (int) Math.round(0.299 * r + 0.587 * g + 0.114 * b);
                break;
                
            case METHOD_LIGHTNESS:
                int max = Math.max(r, Math.max(g, b));
                int min = Math.min(r, Math.min(g, b));
                gray = (max + min) / 2;
                break;
                
            default:
                gray = (r + g + b) / 3;
        }
        
        return Math.max(0, Math.min(255, gray));
    }
}
