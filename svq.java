// Get JAFFE database from http://www.kasrl.org/jaffe_info.html
// Extract pics in folder named "jaffe"
// package image_test;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class svq {

    public static void main(String[] args) {
        // read image
        BufferedImage input = null;
        try {
            input = ImageIO.read(new File("jaffe/KA.AN1.39.tiff.bmp"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Done!");
    }
}