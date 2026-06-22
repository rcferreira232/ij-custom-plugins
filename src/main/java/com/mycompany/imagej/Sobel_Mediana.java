package com.mycompany.imagej;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.plugin.filter.Convolver;
import ij.process.ByteProcessor;
import ij.process.ImageProcessor;

public class Sobel_Mediana implements PlugIn {

    @Override
    public void run(String arg) {

        ImagePlus imp = IJ.getImage();

        if (imp == null) {
            IJ.noImage();
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
                filtros[0]
        );

        gd.showDialog();

        if (gd.wasCanceled())
            return;

        String filtro = gd.getNextRadioButton();

        if (filtro.equals("Mediana")) {
            aplicarMediana(imp);
        } else {
            aplicarSobel(imp);
        }
    }

    private void aplicarMediana(ImagePlus imp) {

        ImageProcessor mediana = imp.getProcessor().duplicate();

        mediana.medianFilter();

        new ImagePlus("Filtro Mediana", mediana).show();
    }

    private void aplicarSobel(ImagePlus imp) {

        ImageProcessor original = imp.getProcessor();

        float[] sobelVertical = {
                -1, 0, 1,
                -2, 0, 2,
                -1, 0, 1
        };

        float[] sobelHorizontal = {
                -1, -2, -1,
                 0,  0,  0,
                 1,  2,  1
        };

        ImageProcessor vertical = original.duplicate();
        ImageProcessor horizontal = original.duplicate();

        Convolver convolver = new Convolver();

        convolver.convolve(vertical, sobelVertical, 3, 3);
        convolver.convolve(horizontal, sobelHorizontal, 3, 3);

        new ImagePlus("Sobel Vertical", vertical).show();
        new ImagePlus("Sobel Horizontal", horizontal).show();

        int largura = original.getWidth();
        int altura = original.getHeight();

        ByteProcessor combinado = new ByteProcessor(largura, altura);

        for (int y = 0; y < altura; y++) {

            for (int x = 0; x < largura; x++) {

                double gx = vertical.getPixelValue(x, y);
                double gy = horizontal.getPixelValue(x, y);

                int valor = (int) Math.sqrt(gx * gx + gy * gy);

                valor = Math.max(0, Math.min(255, valor));

                combinado.putPixel(x, y, valor);
            }
        }

        new ImagePlus("Sobel Combinado", combinado).show();
    }
}