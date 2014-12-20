// Sparse Vector Quantization test file

// Get JAFFE database from http://www.kasrl.org/jaffe_info.html
// Extract pics in folder named "jaffe"
// Convert to bmp with `for f in $(ls *.tiff); do convert $f $f.bmp; done`
// package image_test;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class SVQ {
    public static int HEIGHT=0;
    public static int WIDTH=0;

    public static void printvec(byte[] vec) {
        for (int i=0; i<vec.length; i++) {
            System.out.print(vec[i] + " ");
        }
        System.out.println();
    }

    public static void printvec(double[] vec) {
        for (int i=0; i<vec.length; i++) {
            System.out.print(vec[i] + " ");
        }
        System.out.println();
    }

    public static double[] toDouble(byte[] vec) {
        double[] ret = new double[vec.length];
        for (int i=0; i<vec.length; i++) {
            ret[i] = (double)(vec[i] & 0xFF) / 255;
        }
        return ret;
    }

    public static byte[] toByte(double[] vec) {
        byte[] ret = new byte[vec.length];
        for (int i=0; i<vec.length; i++) {
            ret[i] = (byte)(vec[i] * 255);
        }
        return ret;
    }

    public static double[] readBMP(String path)
    throws IOException {
        // read image
        BufferedImage img = ImageIO.read(new File(path));
        if (HEIGHT == 0 && WIDTH == 0) { // avoid multiple setting
            HEIGHT = img.getHeight();
            WIDTH  = img.getWidth();
        }
        // make sure it's grayscale (one byte per pixel)
        if(img.getType()!=BufferedImage.TYPE_BYTE_GRAY) {
            System.out.println("Converting to grayscale...");
            img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_BYTE_GRAY);
        }

        // return array of pixels
        byte[] pixels = new byte[WIDTH*HEIGHT];
        img.getRaster().getDataElements(0,0,WIDTH,HEIGHT,pixels);
        return toDouble(pixels);
    }

    public static void writeBMP(double[] pixels, String path)
    throws IOException {
        // make image
        BufferedImage image = new BufferedImage(
            HEIGHT, WIDTH,BufferedImage.TYPE_BYTE_GRAY);

        //set pixels
        image.getRaster().setDataElements(0,0,WIDTH,HEIGHT,toByte(pixels));

        // write file
        ImageIO.write(image, "BMP", new File(path));

    }

    public static void main(String[] args)
    throws IOException {
        // get image
        double[] pixels = readBMP("jaffe/KA.AN1.39.tiff.bmp");

        // edit
        for (int i=0; i<pixels.length; i++) {
            // salt and pepper grid square
            if (i%2==0 && (i/WIDTH)%2==0) {
                pixels[i] = 0;
            } else {
                pixels[i] = 1;
            }
        }

        // save
        System.out.println(HEIGHT + "x" + WIDTH + " - " + pixels.length);
        writeBMP(pixels, "out.bmp");

        System.out.println("Done!");
    }
}