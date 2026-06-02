package com.mycompany.imagej;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class Split_Channels implements PlugInFilter {

    @Override
    public int setup(String arg, ImagePlus imp) {
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
                ip.getPixel(x, y, pixelValue);
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
