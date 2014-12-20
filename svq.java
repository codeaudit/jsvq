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

        // to byte array
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            ImageIO.write( input, "bmp", baos );
            baos.flush();
        } catch (IOException e) {

        }
        byte[] bytearray = baos.toByteArray();

        // manipulation
        int height = input.getHeight();
        int width = input.getWidth();
        int pixel;

        System.out.println(height + "x" + width);

        for (int i=5; i<4000; i++) {
            // bytearray[i] = (byte)(bytearray[i]+100%255);
            bytearray[i] = (byte)(255);
        }

        // to BufferedImage
        ByteArrayInputStream bais = new ByteArrayInputStream(bytearray);
        BufferedImage output = null;
        try {
            output = ImageIO.read(bais);
            bais.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // save result
        try {
            ImageIO.write(output, "BMP", new File("test.bmp"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        System.out.println("Done!");
    }
}