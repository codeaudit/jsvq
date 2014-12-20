// Get JAFFE database from http://www.kasrl.org/jaffe_info.html
// Extract pics in folder named "jaffe"
// package image_test;

import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class svq {

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

    public static void main(String[] args) 
    throws IOException {
        // read image
        BufferedImage img = ImageIO.read(new File("jaffe/KA.AN1.39.tiff.bmp"));
        int height = img.getHeight()/4;
        int width = img.getWidth()/4;

        // make sure it's grayscale ubytes
        int desiredType = BufferedImage.TYPE_BYTE_GRAY;
        if(img.getType()!=desiredType) {
            System.out.println("Converting to grayscale...");
            img = new BufferedImage(width,height,desiredType);
        }

        // get pixels
        byte[] pixels = new byte[width*height];
        WritableRaster ras = img.getRaster();
        int minX = ras.getMinX();
        int minY = ras.getMinY();
        ras.getDataElements(minX, minY,width,height,pixels);

        // manipulation
        double[] tmp = toDouble(pixels);
        for (int i=0; i<tmp.length; i++) {
            if (i%2==0 && (i/width)%2==0) {
                tmp[i] = 0;
            } else {
                tmp[i] = 1;
            }
        }
        printvec(pixels);
        printvec(tmp);
        printvec(toByte(tmp));

        //set pixels
        ras.setDataElements(minX, minY,width,height,toByte(tmp));

        // save result
        System.out.println(height + "x" + width + " - " + pixels.length);
        ImageIO.write(img, "BMP", new File("test.bmp"));

        System.out.println("Done!");
    }
}