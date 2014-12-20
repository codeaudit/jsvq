// Sparse Vector Quantization test file

// Get JAFFE database from http://www.kasrl.org/jaffe_info.html
// Extract pics in folder named "jaffe"
// Convert to bmp with `for f in $(ls *.tiff); do convert $f $f.bmp; done`

import java.io.File;

public class SVQ {
    public static double dot(double[] a, double[] b) {
        if (a.length != b.length) { throw new RuntimeException("Lengths don't match!");}
        double ret = 0;
        for (int i=0; i<a.length; i++) {
            ret += a[i] * b[i];
        }
        return ret;
    }

    public static void main(String[] args) {
        // get images
        double[][] images = BMPInterface.readAllBMPInDir("jaffe");

        // compute average image
        double[] avg = BMPInterface.rescaledAverage(images);

        // save
        System.out.println(BMPInterface.HEIGHT + "x" + 
            BMPInterface.WIDTH + "x" + images.length);
        BMPInterface.writeBMP(avg, "out.bmp");

        System.out.println("Done!");
    }
}

