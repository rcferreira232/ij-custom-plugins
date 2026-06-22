package com.mycompany.imagej;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

public class Filtros_Lineares implements PlugIn {

    private String filtroSelecionado = "Media";

    @Override
    public void run(String arg) {

        // Obtém a imagem atual
        ImagePlus imp = IJ.getImage();

        if (imp == null) {
            IJ.noImage();
            return;
        }

        // Verifica se a imagem está em tons de cinza
        if (imp.getType() != ImagePlus.GRAY8) {
            IJ.showMessage("Erro", "A imagem precisa estar em tons de cinza (8 bits).");
            return;
        }

        // Cria a janela de seleção
        GenericDialog gd = new GenericDialog("Filtros Lineares");

        String[] filtros = {
                "Passa-Baixa",
                "Passa-Altas",
                "Borda"
        };

        gd.addRadioButtonGroup(
                "Escolha o filtro:",
                filtros,
                3,
                1,
                filtroSelecionado
        );

        gd.showDialog();

        if (gd.wasCanceled())
            return;

        filtroSelecionado = gd.getNextRadioButton();

        // Duplica a imagem original
        ImageProcessor ip = imp.getProcessor();
        ImageProcessor ipCopy = ip.duplicate();

        // Aplica o filtro escolhido
        if (filtroSelecionado.equals("Passa-Baixa")) {
            aplicarPassaBaixaMedia(ipCopy);
            mostrarImagem(ipCopy, "Resultado - Passa-Baixa (Media)");
        } else if (filtroSelecionado.equals("Passa-Altas")) {
            aplicarPassaAltas(ipCopy);
            mostrarImagem(ipCopy, "Resultado - Passa-Altas");

        } else if (filtroSelecionado.equals("Borda")) {
            aplicarBorda(ipCopy);
            mostrarImagem(ipCopy, "Resultado - Borda");
        }
    }

    private void aplicarPassaBaixaMedia(ImageProcessor ip) {

        int[][] kernel = {
                {1, 1, 1},
                {1, 1, 1},
                {1, 1, 1}
        };

        aplicarConvolucao(ip, kernel, 9);
    }

    private void aplicarPassaAltas(ImageProcessor ip) {

        int[][] kernel = {
                {1, -2, 1},
                {-2, 5, -2},
                {1, -2, 1}
        };

        aplicarConvolucao(ip, kernel, 1);
    }

    private void aplicarBorda(ImageProcessor ip) {

        int[][] kernel = {
                {1, 0, -1},
                {1, 0, -1},
                {1, 0, -1} 
        };

        aplicarConvolucao(ip, kernel, 1);
    }

    private void aplicarConvolucao(ImageProcessor ip, int[][] kernel, int divisor) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        ImageProcessor copy = ip.duplicate();

        for (int x = 1; x < width - 1; x++) {
            for (int y = 1; y < height - 1; y++) {

                int soma = 0;

                for (int i = -1; i <= 1; i++) {
                    for (int j = -1; j <= 1; j++) {
                        soma += copy.getPixel(x + i, y + j) * kernel[i + 1][j + 1];
                    }
                }

                ip.putPixel(x, y, soma / divisor);
            }
        }
    }

    private void mostrarImagem(ImageProcessor ip, String titulo) {

        ImagePlus resultado = new ImagePlus(titulo, ip);
        resultado.show();
    }
}