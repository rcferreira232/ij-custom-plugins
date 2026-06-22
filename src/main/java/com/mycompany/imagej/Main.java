package com.mycompany.imagej;

import java.io.File;
import java.net.URISyntaxException;
import java.security.CodeSource;

import ij.IJ;
import ij.ImageJ;

public class Main {
    public static void main(String[] args) throws URISyntaxException {
        // set the plugins.dir property to make the plugin appear in the Plugins menu
		// see: https://stackoverflow.com/a/7060464/1207769
        CodeSource codeSource = Main.class.getProtectionDomain().getCodeSource();
        File jarFile = new File(codeSource.getLocation().toURI().getPath());
        String jarDir = jarFile.getParentFile().getPath();
        
		System.setProperty("plugins.dir", jarDir);

        new ImageJ();
        // IJ.run("Clown (14K)");
        IJ.run("Boats");

        // IJ.runPlugIn(Process_Pixels.class.getName(), "");
        // IJ.runPlugIn(Split_Channels.class.getName(), "");
        // IJ.runPlugIn(Merge_Channels.class.getName(), "");
        // IJ.runPlugIn(Peer_To_Peer_Operations.class.getName(), "");
        // IJ.runPlugIn(Convert_Rgb_To_Grey.class.getName(), "");
        IJ.runPlugIn(Histograma_.class.getName(), "");
    }
}
