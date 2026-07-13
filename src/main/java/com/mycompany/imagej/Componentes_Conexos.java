package com.mycompany.imagej;

import java.awt.Point;
import java.util.LinkedList;
import java.util.Queue;

import ij.IJ;
import ij.ImagePlus;
import ij.plugin.filter.PlugInFilter;
import ij.process.ImageProcessor;

public class Componentes_Conexos implements PlugInFilter {

    @Override
    public int setup(String arg, ImagePlus imp) {
        return DOES_8G; 
    }

    @Override
    public void run(ImageProcessor ip) {
        int width = ip.getWidth();
        int height = ip.getHeight();
        
        int[][] labels = new int[width][height];
        int label = 1;

        int[] dx = {0, 0, 1, -1};
        int[] dy = {1, -1, 0, 0};

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                
                if (ip.getPixel(x, y) > 0 && labels[x][y] == 0) {
                    Queue<Point> queue = new LinkedList<>();
                    queue.add(new Point(x, y));
                    labels[x][y] = label;

                    while (!queue.isEmpty()) {
                        Point p = queue.poll();

                        for (int i = 0; i < 4; i++) {
                            int nx = p.x + dx[i];
                            int ny = p.y + dy[i];

                            if (nx >= 0 && nx < width && ny >= 0 && ny < height) {
                                if (ip.getPixel(nx, ny) > 0 && labels[nx][ny] == 0) {
                                    labels[nx][ny] = label;
                                    queue.add(new Point(nx, ny));
                                }
                            }
                        }
                    }
                    label++;
                }
            }
        }

        // Fase 2: Pintar os componentes com tons de cinza
        int totalLabels = label - 1;
        if (totalLabels > 0) {
            // Calcula o incremento (step) de cor para garantir tons distintos
            int colorStep = 255 / totalLabels; 

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    if (labels[x][y] > 0) {
                        int grayShade = labels[x][y] * colorStep;
                        ip.putPixel(x, y, grayShade);
                    } else {
                        // Fundo preto
                        ip.putPixel(x, y, 0);
                    }
                }
            }
            IJ.showMessage("Processamento Concluído", "Total de componentes encontrados: " + totalLabels);
        } else {
            IJ.showMessage("Aviso", "Nenhum componente conexo encontrado.");
        }
    }
}