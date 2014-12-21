// Sparse Vector Quantization test file

// Get JAFFE database from http://www.kasrl.org/jaffe_info.html
// Extract pics in folder named "jaffe"
// Convert to bmp with `ls *.tiff | while read f; do convert "$f" "${f%.*}.bmp"; done`

import java.io.File;

public class SVQ {

    Centroid[] centroids;

    public SVQ(int ncentr, int centrSize) {
        centroids = new Centroid[ncentr];
        for (int i=0; i<centroids.length; i++) {
            centroids[i] = new Centroid(centrSize);
        }
    }

    public double[] code(double[] vec) {
        double[] ret = new double[centroids.length];
        for (int i=0; i<centroids.length; i++) {
            ret[i] = centroids[i].similarity(vec);
        }
        return ret;
    }

    // Wrote half asleep, refactor it
    public double[] reconstruct(double[] code) {
        if (code.length != centroids.length) {
            throw new RuntimeException(
                "Code length does not match number of centroids.");
        }
        int imgsize = centroids[0].size;
        double[] ret = new double[imgsize];
        for (int i=0; i<ret.length; i++) { ret[i] = 0d; }
        for (int coord=0; coord<imgsize; coord++) {
            for (int centr=0; centr<centroids.length; centr++) {
                ret[coord] += code[centr]*(centroids[centr].getData()[coord]);
            }
            // ret[coord] /= centroids.length;
        }
        return (new BMPLoader("","")).rescale(ret);
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

    // train on single image
    public void train(double[] img) {
        double[] code = code(img);
        int closest = maxidx(code);
        // train the closest centroid
        centroids[closest].train(img);
    }

    // train on set of images
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
        BMPLoader bmp = new BMPLoader("jaffe", "out");

        // number of centroids
        int NCENTR  = 8;
        // size of training set
        int NTRAIN  = 100; // (bmp.listBMP().length/4)
        // size of validation set
        int NVALID  = 4;

        // load images
        double[][] images = bmp.readAll(NTRAIN);
        System.out.println("Elaborating images: " +
            images.length + "x" + bmp.height + "x" + bmp.width);

        // train
        SVQ svq = new SVQ(NCENTR, images[0].length);
        svq.train(images);
        bmp.saveAll(svq.getData(), "centr");

        // reconstruct
        double[][] reconstructions = new double[NVALID][];
        double[] img, code;
        for (int i=0; i<NVALID; i++) {
            img = images[10*i];
            bmp.save(img, "image_"+i);
            code = svq.code(img);
            reconstructions[i] = svq.reconstruct(code);
        }
        bmp.saveAll(reconstructions, "reconstr");


        System.out.println("\nDone!");
    }
}

