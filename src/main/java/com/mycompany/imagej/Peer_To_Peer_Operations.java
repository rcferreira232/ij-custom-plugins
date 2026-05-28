package com.mycompany.imagej;

import java.awt.AWTEvent;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

/**
 * Plugin para alterar brilho, contraste, solarização e dessaturação de uma imagem.
 * 
 * Fornece uma interface gráfica com 4 sliders para ajuste em tempo real:
 * - Brilho (Brightness): -100 a 100
 * - Contraste (Contrast): 0.1 a 3.0
 * - Solarização (Solarization): 0 a 100
 * - Dessaturação (Desaturation): 0 a 100
 */
public class Peer_To_Peer_Operations implements PlugIn, DialogListener {
    
    private ImagePlus imagePlus;
    private ImageProcessor originalProcessor;
    private ImageProcessor workingProcessor;
    
    // Parâmetros dos sliders
    private double brightness = 0;      // -100 a 100
    private double contrast = 1.0;      // 0.1 a 3.0
    private double solarization = 0;    // 0 a 100
    private double desaturation = 0;    // 0 a 100
    
    @Override
    public void run(String arg) {
        imagePlus = ij.WindowManager.getCurrentImage();
        
        if (imagePlus == null) {
            IJ.error("Nenhuma imagem aberta!");
            return;
        }
        
        // Criar cópia da imagem original para poder reverter
        originalProcessor = imagePlus.getProcessor().duplicate();
        workingProcessor = imagePlus.getProcessor().duplicate();
        
        // Mostrar diálogo com sliders
        showDialog();
    }
    
    /**
     * Exibe o diálogo com os 4 sliders
     */
    private void showDialog() {
        GenericDialog dialog = new GenericDialog("Peer-to-Peer Operations");
        
        dialog.addSlider("Brilho (Brightness):", -100, 100, brightness);
        dialog.addSlider("Contraste (Contrast):", 0.1, 3.0, contrast);
        dialog.addSlider("Solarização (Solarization):", 0, 100, solarization);
        dialog.addSlider("Dessaturação (Desaturation):", 0, 100, desaturation);
        
        dialog.addDialogListener(this);
        dialog.showDialog();
    }
    
    /**
     * Chamado automaticamente quando os sliders são movidos (DialogListener)
     */
    @Override
    public boolean dialogItemChanged(GenericDialog dialog, AWTEvent event) {
        // Se o diálogo foi cancelado, restaurar a imagem original
        if (dialog.wasCanceled()) {
            imagePlus.setProcessor(originalProcessor.duplicate());
            imagePlus.repaintWindow();
            return true;
        }
        
        // Obter os valores atuais dos sliders
        brightness = dialog.getNextNumber();
        contrast = dialog.getNextNumber();
        solarization = dialog.getNextNumber();
        desaturation = dialog.getNextNumber();
        
        // Validar valores
        brightness = Math.max(-100, Math.min(100, brightness));
        contrast = Math.max(0.1, Math.min(3.0, contrast));
        solarization = Math.max(0, Math.min(100, solarization));
        desaturation = Math.max(0, Math.min(100, desaturation));
        
        // Criar cópia da imagem original
        workingProcessor = originalProcessor.duplicate();
        
        // Aplicar transformações
        if (brightness != 0) {
            applyBrightness(workingProcessor, brightness);
        }
        
        if (contrast != 1.0) {
            applyContrast(workingProcessor, contrast);
        }
        
        if (solarization > 0) {
            applySolarization(workingProcessor, solarization);
        }
        
        if (desaturation > 0) {
            applyDesaturation(workingProcessor, desaturation);
        }
        
        // Atualizar a imagem com o preview
        imagePlus.setProcessor(workingProcessor);
        imagePlus.repaintWindow();
        
        return true;
    }
    
    /**
     * Aplica ajuste de brilho
     * @param value -100 (mais escuro) a 100 (mais claro)
     */
    private void applyBrightness(ImageProcessor processor, double value) {
        byte[] pixels = (byte[]) processor.getPixels();
        
        for (int i = 0; i < pixels.length; i++) {
            int pixel = pixels[i] & 0xFF;
            pixel += value;
            pixel = Math.max(0, Math.min(255, pixel));
            pixels[i] = (byte) pixel;
        }
    }
    
    /**
     * Aplica ajuste de contraste
     * @param value Multiplicador de contraste (0.1 a 3.0)
     */
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
    
    /**
     * Aplica efeito de solarização
     * Inverte os pixels abaixo de um limiar
     * @param value 0 a 100 (nível de limiar em percentual)
     */
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
    
    /**
     * Aplica dessaturação (conversão para escala de cinza parcial)
     * @param value 0 (sem dessaturação) a 100 (completamente em cinza)
     */
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
