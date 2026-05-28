package com.mycompany.imagej;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

/**
* Plugin para transformar uma imagem RGB em três imagens em escala de cinza e apresentar as imagens resultantes na tela do ImageJ.
* O split de canais é uma operação, onde uma imagem colorida é dividida em seus componentes de cor individuais. 
* Uma imagem RGB, ela é composta por três canais: vermelho (Red), verde (Green) e azul (Blue). 
* O plugin "Split Channels" pega uma imagem RGB e cria três novas imagens em escala de cinza, cada uma representando um dos canais de cor.
* O desenvolvimento do plugin, deve evitar uso da bliblioteca do ImageJ diretamente, pois ela abstrai a manipulação de pixels.
* No desenvolvimento do plugin, deve-se manipular os pixels diretamente, utilizando as classes ImageProcessor e ImagePlus do ImageJ para acessar e modificar os dados de pixel.
*/

public class Split_Channels implements PlugInFilter {

    @Override
    public int setup(String arg, ImagePlus imp) {
        if (imp == null) {
            IJ.noImage();
            return DONE;
        }

        return DOES_RGB; 
    }

    @Override
    public void run(ImageProcessor ip) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        
        ImagePlus redImage = IJ.createImage("Canal Vermelho", "8-bit", width, height, 1);
        ImagePlus greenImage = IJ.createImage("Canal Verde", "8-bit", width, height, 1);
        ImagePlus blueImage = IJ.createImage("Canal Azul", "8-bit", width, height, 1);

        ImageProcessor redProcessor = redImage.getProcessor();
        ImageProcessor greenProcessor = greenImage.getProcessor();
        ImageProcessor blueProcessor = blueImage.getProcessor();

        int pixelValue[] = {0, 0, 0};
        int newPixelValue;

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixelValue = ip.getPixel(x, y, pixelValue);
                IJ.log("Pixel: " + pixelValue[0] + " " + pixelValue[1] + " " + pixelValue[2]);

                newPixelValue = pixelValue[0]; // Canal Vermelho
                redProcessor.putPixel(x, y, newPixelValue);
                IJ.log("Canal Vermelho: " + newPixelValue);
                newPixelValue = pixelValue[1]; // Canal Verde
                greenProcessor.putPixel(x, y, newPixelValue);
                IJ.log("Canal Verde: " + newPixelValue);
                newPixelValue = pixelValue[2]; // Canal Azul
                blueProcessor.putPixel(x, y, newPixelValue);
                IJ.log("Canal Azul: " + newPixelValue);
            }
        }

        redImage.show();
        greenImage.show();
        blueImage.show();
    }
}
