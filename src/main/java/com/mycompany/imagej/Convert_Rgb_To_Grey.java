package com.mycompany.imagej;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ByteProcessor;
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
        
        // Verificar se é uma imagem RGB
        if (imagePlus.getType() != ImagePlus.COLOR_RGB) {
            IJ.error("A imagem deve ser do tipo RGB!");
            return;
        }
        
        // Mostrar diálogo para seleção de método
        if (!showDialog()) {
            return;
        }
        
        // Aplicar a conversão
        convertToGrayscale(imagePlus);
    }
    
    /**
     * Exibe o diálogo para seleção do método e opções
     */
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
        
        // Obter seleção do método
        String selected = dialog.getNextChoice();
        if (selected.contains("Média")) {
            selectedMethod = METHOD_AVERAGE;
        } else if (selected.contains("Luminosidade")) {
            selectedMethod = METHOD_LUMINOSITY;
        } else {
            selectedMethod = METHOD_LIGHTNESS;
        }
        
        // Obter opção de criar nova imagem
        createNewImage = dialog.getNextBoolean();
        
        return true;
    }
    
    /**
     * Aplica a conversão para escala de cinza
     */
    private void convertToGrayscale(ImagePlus imagePlus) {
        ImageProcessor processor = imagePlus.getProcessor();
        
        if (!(processor instanceof ColorProcessor)) {
            IJ.error("Processador não é do tipo ColorProcessor!");
            return;
        }
        
        ColorProcessor colorProcessor = (ColorProcessor) processor;
        ByteProcessor grayProcessor = convertToGrayscaleProcessor(colorProcessor);
        
        if (createNewImage) {
            // Criar uma nova imagem com a versão em cinza
            ImagePlus grayImage = new ImagePlus(imagePlus.getTitle() + " (Cinza)", grayProcessor);
            grayImage.show();
        } else {
            // Substituir a imagem original
            imagePlus.setProcessor(grayProcessor);
            imagePlus.repaintWindow();
        }
    }
    
    /**
     * Converte um ColorProcessor para ByteProcessor em escala de cinza
     */
    private ByteProcessor convertToGrayscaleProcessor(ColorProcessor colorProcessor) {
        int width = colorProcessor.getWidth();
        int height = colorProcessor.getHeight();
        byte[] grayPixels = new byte[width * height];
        
        // Obter os pixels da imagem original
        int[] rgbPixels = (int[]) colorProcessor.getPixels();
        
        // Converter cada pixel
        for (int i = 0; i < rgbPixels.length; i++) {
            int rgb = rgbPixels[i];
            
            // Extrair componentes RGB
            int r = (rgb >> 16) & 0xFF;
            int g = (rgb >> 8) & 0xFF;
            int b = rgb & 0xFF;
            
            // Calcular valor em cinza baseado no método selecionado
            int gray = calculateGrayscale(r, g, b);
            
            grayPixels[i] = (byte) gray;
        }
        
        return new ByteProcessor(width, height, grayPixels, null);
    }
    
    /**
     * Calcula o valor em cinza para um pixel RGB usando o método selecionado
     */
    private int calculateGrayscale(int r, int g, int b) {
        int gray;
        
        switch (selectedMethod) {
            case METHOD_AVERAGE:
                // Método da Média: (R + G + B) / 3
                gray = (r + g + b) / 3;
                break;
                
            case METHOD_LUMINOSITY:
                // Método da Luminosidade: 0.299*R + 0.587*G + 0.114*B
                gray = (int) Math.round(0.299 * r + 0.587 * g + 0.114 * b);
                break;
                
            case METHOD_LIGHTNESS:
                // Método da Claridade: (max(R,G,B) + min(R,G,B)) / 2
                int max = Math.max(r, Math.max(g, b));
                int min = Math.min(r, Math.min(g, b));
                gray = (max + min) / 2;
                break;
                
            default:
                gray = (r + g + b) / 3;
        }
        
        // Garantir que o valor está no intervalo 0-255
        return Math.max(0, Math.min(255, gray));
    }
}
