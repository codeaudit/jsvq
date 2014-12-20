import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import javax.imageio.ImageIO;

public class BMPInterface {
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

    public static double[] readBMP(String path){
        return readBMP(new File(path));
    }

    public static double[] readBMP(File file){
        // read image
        BufferedImage img;
        try { 
            img = ImageIO.read(file); 
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

    public static void writeBMP(double[] pixels, String path) {
        // make image
        BufferedImage image = new BufferedImage(
            HEIGHT, WIDTH,BufferedImage.TYPE_BYTE_GRAY);

        //set pixels
        image.getRaster().setDataElements(0,0,WIDTH,HEIGHT,toByte(pixels));

        // write file
        try { 
            ImageIO.write(image, "BMP", new File(path));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static File[] listBMPInDir(String path){
        return new File(path).listFiles(new FilenameFilter() {
                @Override
                public boolean accept(File dir, String name) {
                    return name.endsWith(".bmp");
                }
            });
    }

    public static double[][] readAllBMPInDir(String path) {
        File[] files = listBMPInDir(path);
        double[][] ret = new double[files.length][];
        for (int i=0; i<files.length; i++) {
            ret[i] = readBMP(files[i]);
        }
        return ret;
    }
}