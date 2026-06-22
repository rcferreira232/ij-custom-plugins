package com.mycompany.imagej;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

public class Sobel_Mediana implements PlugIn {

    private String filtroSelecionado = "Sobel";

    @Override
    public void run(String arg) {

        ImagePlus imp = IJ.getImage();

        if (imp == null) {
            IJ.noImage();
            return;
        }

        if (imp.getType() != ImagePlus.GRAY8) {
            IJ.showMessage("Erro",
                    "A imagem precisa estar em tons de cinza (8 bits).");
            return;
        }

        GenericDialog gd = new GenericDialog("Sobel ou Mediana");

        String[] filtros = {
            "Sobel",
            "Mediana"
        };

        gd.addRadioButtonGroup(
                "Escolha o filtro:",
                filtros,
                2,
                1,
                filtroSelecionado
        );

        gd.showDialog();

        if (gd.wasCanceled())
            return;

        filtroSelecionado = gd.getNextRadioButton();

        ImageProcessor ip = imp.getProcessor().duplicate();

        if (filtroSelecionado.equals("Sobel")) {
            aplicarSobel(ip);
        } else {
            aplicarMediana(ip);
        }
    }

    private void aplicarSobel(ImageProcessor ip) {

        int[][] kernelVertical = {
            {-1, 0, 1},
            {-2, 0, 2},
            {-1, 0, 1}
        };

        int[][] kernelHorizontal = {
            {-1, -2, -1},
            {0, 0, 0},
            {1, 2, 1}
        };

        ImageProcessor gx = ip.duplicate();
        ImageProcessor gy = ip.duplicate();

        aplicarConvolucao(gx, kernelVertical);
        aplicarConvolucao(gy, kernelHorizontal);

        ImageProcessor g = ip.duplicate();
        int largura = ip.getWidth();
        int altura = ip.getHeight();

        for (int y = 0; y < altura; y++) {
            for (int x = 0; x < largura; x++) {
                int sobelX = gx.getPixel(x, y);
                int sobelY = gy.getPixel(x, y);
                int resultado = (int) Math.sqrt(sobelX * sobelX + sobelY * sobelY);

                g.putPixel(x, y, resultado);
            }
        }

        mostrarImagem(gx, "Sobel Vertical");
        mostrarImagem(gy, "Sobel Horizontal");
        mostrarImagem(g, "Sobel Combinado");
    }

    private void aplicarConvolucao(ImageProcessor ip, int[][] kernel) {

        int largura = ip.getWidth();
        int altura = ip.getHeight();

        ImageProcessor copia = ip.duplicate();

        for (int x = 1; x < largura - 1; x++) {
            for (int y = 1; y < altura - 1; y++) {

                int soma = 0;

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {

                        soma += copia.getPixel(x + i, y + j)
                                * kernel[i + 1][j + 1];
                    }
                }

                ip.putPixel(x, y, soma);
            }
        }
    }

    private void aplicarMediana(ImageProcessor ip) {

        int largura = ip.getWidth();
        int altura = ip.getHeight();

        ImageProcessor copia = ip.duplicate();

        for (int y = 1; y < altura - 1; y++) {
            for (int x = 1; x < largura - 1; x++) {

                int[] vizinhanca = new int[9];
                int indice = 0;

                for (int j = -1; j <= 1; j++) {
                    for (int i = -1; i <= 1; i++) {
                        vizinhanca[indice++] =
                                copia.getPixel(x + i, y + j);
                    }
                }

                java.util.Arrays.sort(vizinhanca);

                ip.putPixel(x, y, vizinhanca[4]);
            }
        }

        mostrarImagem(ip, "Filtro Mediana");
    }

    private void mostrarImagem(ImageProcessor ip, String titulo) {
        ImagePlus resultado = new ImagePlus(titulo, ip);
        resultado.show();
    }
}