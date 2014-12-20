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
        int imglen = images[0].length;

        // compute average image
        double[] avg = new double[imglen];
        double min, max, val, avgmin=0, avgmax=0;
        for (int pixidx=0; pixidx<imglen; pixidx++) {
            avg[pixidx] = 0;
            min = max = images[0][pixidx];
            for (int imgidx=0; imgidx<images.length; imgidx++) {
                val = images[imgidx][pixidx]; // just a shorthand
                if (val<min) { min = val; }
                if (val>max) { max = val; }
                avg[pixidx] += val;
            }
            avg[pixidx] /= images.length;
            if (pixidx==0){ avgmin = avgmax = images[0][0]; } // for later rescaling
            if (avg[pixidx]<avgmin) { avgmin=avg[pixidx]; }
            if (avg[pixidx]>avgmax) { avgmax=avg[pixidx]; }
        }
        // rescaling
        for (int pixidx=0; pixidx<imglen; pixidx++) {
            avg[pixidx] = ((avg[pixidx] - avgmin) / (avgmax - avgmin));
        }

        // save
        System.out.println(BMPInterface.HEIGHT + "x" + BMPInterface.WIDTH + " - " + avg.length);
        BMPInterface.writeBMP(avg, "out.bmp");

        System.out.println("Done!");
    }
}

