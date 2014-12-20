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
        double[][] images = BMPInterface.readAllBMPInDir("jaffe");

        RollingAverage avg = new RollingAverage(images[0].length);
        int i=0;
        double[] res;

        // first half
        for (; i<images.length/2; i++) {
            avg.add(images[i]);
        }
        res = BMPInterface.rescale(avg.getAvg());
        BMPInterface.writeBMP(res, "half.bmp");
        
        // second half
        for (; i<images.length; i++) {
            avg.add(images[i]);
        }
        res = BMPInterface.rescale(avg.getAvg());
        BMPInterface.writeBMP(res, "full.bmp");

        // reference
        res = BMPInterface.rescale(BMPInterface.average(images));
        BMPInterface.writeBMP(res, "out.bmp");


        System.out.println(BMPInterface.HEIGHT + "x" + 
            BMPInterface.WIDTH + "x" + images.length);
        System.out.println("Done!");
    }
}

