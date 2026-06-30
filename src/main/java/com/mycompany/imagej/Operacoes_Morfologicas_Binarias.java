package com.mycompany.imagej;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

public class Operacoes_Morfologicas_Binarias implements PlugIn {

    public void run(String arg) {
        ImagePlus imagem = WindowManager.getCurrentImage();
        if (imagem == null) {
            IJ.showMessage("Erro", "Nenhuma imagem aberta.");
            return;
        }

        ImageProcessor processor = imagem.getProcessor();

        if (!isBinaryImage(processor)) {
            IJ.showMessage("Erro", "A imagem não é binária. Certifique-se de que a imagem contém apenas valores 0 e 255.");
            return;
        }

        GenericDialog gd = new GenericDialog("Operações Morfológicas");
        gd.addRadioButtonGroup("Escolha a operação:",
            new String[]{"Borda", "Esqueletização"},
            2, 1, "Borda");
        gd.showDialog();

        if (gd.wasCanceled()) return;

        String operacao = gd.getNextRadioButton();

        switch (operacao) {
            case "Borda":
                ImageProcessor copiaBorda = processor.duplicate();
                // A borda é a imagem original menos a imagem erodida
                processar(copiaBorda, "erosao");
                subtrairImagens(processor, copiaBorda);
                break;
            case "Esqueletização":
                processor = esqueletizar(processor);
                break;
        }

        new ImagePlus("Resultado - " + operacao, processor).show();
    }

    private boolean isBinaryImage(ImageProcessor img) {
        int width = img.getWidth();
        int height = img.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int pixel = img.getPixel(x, y);
                if (!(pixel == 0 || pixel == 255)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void processar(ImageProcessor img, String tipo) {
        int width = img.getWidth();
        int height = img.getHeight();
        
        ImageProcessor resultado = img.createProcessor(width, height);
        ImageProcessor copia = img.duplicate();

        int[][] elementoEstruturante = {
            {0, 1, 0},
            {1, 1, 1},
            {0, 1, 0}
        };

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int valor = aplicarOperacao(copia, x, y, elementoEstruturante, tipo);
                resultado.putPixel(x, y, valor);
            }
        }

        img.setPixels(resultado.getPixels());
    }

    private int aplicarOperacao(ImageProcessor img, int x, int y, int[][] se, String tipo) {
        switch (tipo) {
            case "dilatacao":
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        if (se[dy + 1][dx + 1] == 1 && img.getPixel(x + dx, y + dy) == 255) {
                            return 255;
                        }
                    }
                }
                return 0;

            case "erosao":
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dx = -1; dx <= 1; dx++) {
                        if (se[dy + 1][dx + 1] == 1 && img.getPixel(x + dx, y + dy) == 0) {
                            return 0;
                        }
                    }
                }
                return 255;
        }
        return img.getPixel(x, y);
    }
    
    private ImageProcessor esqueletizar(ImageProcessor imgOriginal) {
        int width = imgOriginal.getWidth();
        int height = imgOriginal.getHeight();
        
        ImageProcessor esqueleto = imgOriginal.createProcessor(width, height); 
        ImageProcessor imagemAtual = imgOriginal.duplicate();

        while (!isImageEmpty(imagemAtual)) {
            // 1. Erodí a imagem atual
            ImageProcessor erodida = imagemAtual.duplicate();
            processar(erodida, "erosao"); // esse proce
            
            ImageProcessor abertura = erodida.duplicate();
            processar(abertura, "dilatacao");
            

            ImageProcessor diferenca = imagemAtual.duplicate();
            subtrairImagens(diferenca, abertura);
            
            unirImagens(esqueleto, diferenca);
            
            imagemAtual = erodida;
        }
        
        return esqueleto;
    }

    private void subtrairImagens(ImageProcessor original, ImageProcessor subtrair) {
        int width = original.getWidth();
        int height = original.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int corOriginal = original.getPixel(x, y);
                int corSubtrair = subtrair.getPixel(x, y);
                original.putPixel(x, y, Math.max(0, corOriginal - corSubtrair));
            }
        }
    }
    
    private void unirImagens(ImageProcessor original, ImageProcessor paraUnir) {
        int width = original.getWidth();
        int height = original.getHeight();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int corOriginal = original.getPixel(x, y);
                int corUnir = paraUnir.getPixel(x, y);
                if (corOriginal == 255 || corUnir == 255) {
                    original.putPixel(x, y, 255);
                }
            }
        }
    }
    
    private boolean isImageEmpty(ImageProcessor img) {
        int width = img.getWidth();
        int height = img.getHeight();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (img.getPixel(x, y) == 255) return false;
            }
        }
        return true;
    }
}