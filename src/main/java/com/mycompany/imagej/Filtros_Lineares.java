package com.mycompany.imagej;

import ij.IJ;
import ij.ImagePlus;
import ij.gui.GenericDialog;
import ij.plugin.PlugIn;
import ij.plugin.filter.Convolver;
import ij.process.ImageProcessor;

public class Filtros_Lineares implements PlugIn {

    @Override
    public void run(String arg) {

        ImagePlus imp = IJ.getImage();

        if (imp == null) {
            IJ.noImage();
            return;
        }

        GenericDialog gd = new GenericDialog("Filtros Lineares");

        String[] filtros = {
                "Media",
                "Passa-Altas",
                "Borda"
        };

        gd.addRadioButtonGroup(
                "Escolha o filtro:",
                filtros,
                3,
                1,
                filtros[0]
        );

        gd.showDialog();

        if (gd.wasCanceled())
            return;

        String filtro = gd.getNextRadioButton();

        float[] kernel;

        switch (filtro) {

            case "Media":
                kernel = new float[]{
                        1f / 9, 1f / 9, 1f / 9,
                        1f / 9, 1f / 9, 1f / 9,
                        1f / 9, 1f / 9, 1f / 9
                };
                break;

            case "Passa-Altas":
                kernel = new float[]{
                        -1, -1, -1,
                        -1, 8, -1,
                        -1, -1, -1
                };
                break;

            default:
                kernel = new float[]{
                        -1, -1, -1,
                         2,  2,  2,
                        -1, -1, -1
                };
        }

        ImageProcessor resultado = imp.getProcessor().duplicate();

        Convolver convolver = new Convolver();
        convolver.convolve(resultado, kernel, 3, 3);

        new ImagePlus("Resultado - " + filtro, resultado).show();
    }
}