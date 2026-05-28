package com.mycompany.imagej;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.plugin.PlugIn;
import ij.process.ImageProcessor;

/**
* Plugin para gerar uma imagem RGB através da junção das imagens 8-bits geradas pelos plugins anteriores e que eram representantes dos canais.
* O merge de canais é o processo inverso do split de canais. Ele pega as três imagens em escala de cinza (representando os canais vermelho, verde e azul) e as combina para criar uma única imagem RGB.
* Cada pixel da imagem RGB resultante é formado pela combinação dos valores correspondentes dos pixels nas três imagens de entrada.
* O desenvolvimento do plugin, deve evitar uso da bliblioteca do ImageJ diretamente, pois ela abstrai a manipulação de pixels.
* No desenvolvimento do plugin, deve-se manipular os pixels diretamente, utilizando as classes ImageProcessor e ImagePlus do ImageJ para acessar e modificar os dados de pixel.
*/

public class Merge_Channels implements PlugIn {

    @Override
    public void run(String arg) {
        ImagePlus imgR = WindowManager.getImage("Canal Vermelho");
        ImagePlus imgG = WindowManager.getImage("Canal Verde");
        ImagePlus imgB = WindowManager.getImage("Canal Azul");

        // Valida se as imagens realmente estão abertas e foram encontradas
        if (imgR == null || imgG == null || imgB == null) {
            IJ.error("Merge Channels", "As imagens separadas (Red, Green, Blue) não foram encontradas abertas.");
            return;
        }

        if (imgR.getType() != ImagePlus.GRAY8 || imgG.getType() != ImagePlus.GRAY8 || imgB.getType() != ImagePlus.GRAY8) {
            IJ.error("Merge Channels", "As imagens devem ser do tipo 8-bit (GRAY8).");
        }
        
        int width = imgR.getWidth();
        int height = imgR.getHeight();


        ImageProcessor redProcessor = imgR.getProcessor();
        ImageProcessor greenProcessor = imgG.getProcessor();
        ImageProcessor blueProcessor = imgB.getProcessor();

        ImagePlus mergedImage = IJ.createImage("Merged Image", "RGB", width, height, 1);

        int pixelValue[] = {0};
        int newPixelValue[] = {0, 0, 0};

        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixelValue = redProcessor.getPixel(x, y, pixelValue);
                newPixelValue[0] = pixelValue[0]; // Canal Vermelho

                pixelValue = greenProcessor.getPixel(x, y, pixelValue);
                newPixelValue[1] = pixelValue[0]; // Canal Verde
                pixelValue = blueProcessor.getPixel(x, y, pixelValue);
                newPixelValue[2] = pixelValue[0]; // Canal Azul

                mergedImage.getProcessor().putPixel(x, y, newPixelValue);
            }
        }

        mergedImage.show();
    }
}