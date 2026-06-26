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

    // Método principal para processar Dilatação e Erosão
    private void processar(ImageProcessor img, String tipo) {
        int width = img.getWidth();
        int height = img.getHeight();
        
        // Cria uma nova imagem vazia baseada no tipo da imagem original
        ImageProcessor resultado = img.createProcessor(width, height);
        ImageProcessor copia = img.duplicate();

        // Elemento Estruturante: CRUZ 3x3
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
    
    // Algoritmo de Lantuéjoul para esqueletização
    private ImageProcessor esqueletizar(ImageProcessor imgOriginal) {
        int width = imgOriginal.getWidth();
        int height = imgOriginal.getHeight();
        
        // Imagem vazia para acumular o esqueleto
        ImageProcessor esqueleto = imgOriginal.createProcessor(width, height); 
        ImageProcessor imagemAtual = imgOriginal.duplicate();

        while (!isImageEmpty(imagemAtual)) {
            // 1. Erodí a imagem atual
            ImageProcessor erodida = imagemAtual.duplicate();
            processar(erodida, "erosao");
            
            // 2. Abertura da imagem atual (é a dilatação da imagem erodida)
            ImageProcessor abertura = erodida.duplicate();
            processar(abertura, "dilatacao");
            
            // 3. Subtrai a abertura da imagem atual
            ImageProcessor diferenca = imagemAtual.duplicate();
            subtrairImagens(diferenca, abertura);
            
            // 4. Une os resquícios (diferença) ao esqueleto
            unirImagens(esqueleto, diferenca);
            
            // 5. A imagem para o próximo passo do laço passa a ser a imagem erodida
            imagemAtual = erodida;
        }
        
        return esqueleto;
    }

    // Subtração de conjuntos
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
    
    // União de conjuntos (para acumular o esqueleto)
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
    
    // Verifica se a imagem já sumiu por completo
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