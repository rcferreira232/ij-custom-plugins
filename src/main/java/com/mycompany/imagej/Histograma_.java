package com.mycompany.imagej;

import java.awt.AWTEvent;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.DialogListener;
import ij.gui.GenericDialog;
import ij.gui.Plot;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;
import ij.process.ImageStatistics;

public class Histograma_ implements PlugIn, DialogListener {
    
    @Override
    public void run(String arg) {
        ImagePlus imagem = WindowManager.getCurrentImage();
        
        if (imagem == null) {
            IJ.noImage();
            return;
        }
        
        if (imagem.getType() == ImagePlus.GRAY8) {
            IG(imagem);    
        } else {
            IJ.error("Imagem não é 8 Bits");
        }
    }
    
    public void IG(ImagePlus imagem) {
        ImagePlus imagemCopia = imagem.duplicate();
        imagemCopia.setTitle(imagem.getShortTitle() + " - Cópia");
        imagemCopia.show();
        
        mostrarHistograma(imagem, "Histograma Original de " + imagem.getShortTitle());

        String[] estrategia = {"Expansão", "Equalização"};
        GenericDialog interfaceGrafica = new GenericDialog("Modificação de Histograma");
        
        interfaceGrafica.addDialogListener(this);
        interfaceGrafica.addRadioButtonGroup("Selecione um método: ", estrategia, 1, 2, estrategia[0]);
        
        interfaceGrafica.showDialog();        
        
        if (interfaceGrafica.wasCanceled()) {
            imagemCopia.changes = false;
            imagemCopia.close();
            IJ.showMessage("PlugIn cancelado!");
        } else if (interfaceGrafica.wasOKed()) {
            if (interfaceGrafica.getNextRadioButton().equals(estrategia[0])) {
                expansao(imagemCopia);
            } else {
                equalizacao(imagemCopia);    
            }
            IJ.showMessage("Plugin encerrado com sucesso!");
        }
    }
    
    @Override
    public boolean dialogItemChanged(GenericDialog interfaceGrafica, AWTEvent e) {
        if (interfaceGrafica.wasCanceled()) return false;
        return true;
    }
    
    public void expansao(ImagePlus imagem) {
        ImageProcessor processador = imagem.getProcessor();
        
        int larguraImagem = imagem.getWidth();
        int alturaImagem = imagem.getHeight();
        int[] vetorTons = new int[256];
        int valorPixel;
        int pixelHigh = 255;
        int pixelLow = 0;

        for (int i = 0; i < 256; i++) {
            vetorTons[i] = 0;
        }
        
        for (int x = 0; x < larguraImagem; x++) {    
            for (int y = 0; y < alturaImagem; y++) {
                valorPixel = processador.getPixel(x, y);
                vetorTons[valorPixel]++;
            }
        }
        
        for (int i = 0; i < 256; i++) {
            if (vetorTons[i] > 0) {
                pixelLow = i;
                break;    
            }
        }
        
        for (int i = 255; i >= 0; i--) {
            if (vetorTons[i] > 0) {
                pixelHigh = i;
                break;
            }
        }
        
        if (pixelHigh == pixelLow) {
            IJ.error("Não é possível expandir o histograma.");
            return;
        }
        
        for (int x = 0; x < larguraImagem; x++) {    
            for (int y = 0; y < alturaImagem; y++) {    
                valorPixel = processador.getPixel(x, y);
                int novoValor = (255 * (valorPixel - pixelLow)) / (pixelHigh - pixelLow);
                processador.putPixel(x, y, novoValor);
            }
        }
        
        imagem.setProcessor(processador);
        imagem.updateAndDraw();
        
        mostrarHistograma(imagem, "Histograma Modificado - Expansão");
    }
    
    public void equalizacao(ImagePlus imagem) {
        ImageProcessor processador = imagem.getProcessor();
        
        int larguraImagem = imagem.getWidth();
        int alturaImagem = imagem.getHeight();
        int[] vetorTons = new int[256];
        int[] vetorF = new int[256];
        int valorPixel;
        double areaImagem = larguraImagem * alturaImagem;
        
        Double[] vetorP = new Double[256];
        Double[] vetorPA = new Double[256];
        
        for (int x = 0; x < larguraImagem; x++) {    
            for (int y = 0; y < alturaImagem; y++) {
                valorPixel = processador.getPixel(x, y);
                vetorTons[valorPixel]++;
            }
        }
        
        for (int i = 0; i < 256; i++) {
            vetorP[i] = (vetorTons[i] * 1.0) / areaImagem;
        }
        
        double acumulado = 0.0;
        for (int x = 0; x < 256; x++) {
            acumulado += vetorP[x];
            vetorPA[x] = acumulado * 255; 
            vetorF[x] = (int) Math.round(vetorPA[x]);
        }
        
        for (int x = 0; x < larguraImagem; x++) {    
            for (int y = 0; y < alturaImagem; y++) {
                valorPixel = processador.getPixel(x, y);
                processador.putPixel(x, y, vetorF[valorPixel]);
            }
        }
        
        imagem.setProcessor(processador);
        imagem.updateAndDraw();
        
        mostrarHistograma(imagem, "Histograma Modificado - Equalização");
    }

    private void mostrarHistograma(ImagePlus imagem, String titulo) {
        ImageStatistics stats = imagem.getStatistics();
        int[] histogram = stats.histogram;
        
        double[] xValues = new double[histogram.length];
        double[] yValues = new double[histogram.length];
        for (int i = 0; i < histogram.length; i++) {
            xValues[i] = i;
            yValues[i] = histogram[i];
        }
        
        Plot plot = new Plot(titulo, "Intensidade", "Frequência");
        plot.add("separate", xValues, yValues);
        plot.show();
    }
}