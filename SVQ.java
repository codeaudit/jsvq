// Sparse Vector Quantization test file

// Get JAFFE database from http://www.kasrl.org/jaffe_info.html
// Extract pics in folder named "jaffe"
// Convert to bmp with `for f in $(ls *.tiff); do convert $f $f.bmp; done`

import java.io.File;

public class SVQ {

    Centroid[] centroids;

    public SVQ(int ncentr, int centrSize) {
        centroids = new Centroid[ncentr];
        for (int i=0; i<centroids.length; i++) {
            centroids[i] = new Centroid(centrSize);
        }
    }

    public double[] similarities(double[] vec) {
        double[] ret = new double[centroids.length];
        for (int i=0; i<centroids.length; i++) {
            ret[i] = centroids[i].similarity(vec);
        }
        return ret;
    }

    public int maxidx(double[] vec) {
        int ret=0;
        double max=vec[ret];
        for (int i=1; i<vec.length; i++) {
            if (vec[i] > max) {
                max = vec[i];
                ret = i;
            }
        }
        return ret;
    }

    public void train(double[] img) {
        double[] code = similarities(img);
        int closest = maxidx(code);
        // train the closest centroid
        centroids[closest].train(img);
    }

    public void train(double[][] imgs) {
        for (int i=0; i<imgs.length; i++) {
            train(imgs[i]);
        }
    }

    public double[][] getData() {
        double[][] ret = new double[centroids.length][];
        for (int i=0; i<centroids.length; i++) {
            ret[i] = centroids[i].getData();
        }
        return ret;
    }

    public static void main(String[] args) {

        int NCENTR = 4;

        // load images
        BMPLoader bmp = new BMPLoader("jaffe", "tmp");
        double[][] images = bmp.readAll();
        System.out.println("Elaborating images: " +
            images.length + "x" + bmp.height + "x" + bmp.width);

        SVQ svq = new SVQ(NCENTR, images[0].length);
        int i=0;
        double[] res;

        // initial
        bmp.saveAll(svq.getData(), "init");

        // first half
        for (; i<images.length/2; i++) {
            svq.train(images[i]);
        }
        bmp.saveAll(svq.getData(), "half");
        
        // second half
        for (; i<images.length; i++) {
            svq.train(images[i]);
        }
        bmp.saveAll(svq.getData(), "full");

        System.out.println("Done!");
    }
}

