// Sparse Vector Quantization test file

// Get JAFFE database from http://www.kasrl.org/jaffe_info.html
// Extract pics in folder named "jaffe"
// Convert to bmp with `ls *.tiff | while read f; do convert "$f" "${f%.*}.bmp"; done`

import java.io.File;
import java.util.Arrays;

public class SVQ {

    Centroid[] centroids;
    int ncentr, imgsize;

    public SVQ(int ncentr, int imgsize) {
        this.ncentr = ncentr;
        this.imgsize = imgsize;
        centroids = new Centroid[ncentr];
        for (int i=0; i<ncentr; i++) {
            centroids[i] = new Centroid(imgsize);
        }
    }

    public void checkLengths(double[] a, double[] b){
        checkLengths(a, b.length);
    }

    public void checkLengths(double[] a, int bLen){
        if (a.length != bLen) {
            throw new RuntimeException(
                "Vectors lengths don't match.");
        }
    }

    public double[] similarities(double[] vec) {
        checkLengths(vec, imgsize);
        double[] ret = new double[ncentr];
        // System.out.print("Simils: ");
        for (int i=0; i<ncentr; i++) {
            ret[i] = centroids[i].similarity(vec);
            // System.out.print(ret[i]+" ");
        }
        // System.out.println();
        return ret;
    }

    public double[] maxWithIndex(double[] vec) {
        double[] ret = { vec[0], 0 };
        for (int i=1; i<vec.length; i++) {
            if (vec[i]>ret[0]) {
                ret[0] = vec[i];
                ret[1] = i;
            }
        }
        return ret;
    }

    public double[] code(double[] vec) {
        double[] ret = new double[ncentr];
        Arrays.fill(ret, 0d);

        double[] mwi = maxWithIndex(similarities(vec));
        // System.out.println("Max: "+mwi[0]+" - idx: "+mwi[1]);
        // here mwi[0] holds the max, if you need it
        ret[(int)mwi[1]] = 1;
        return ret;
    }

    public double[] reconstruct(double[] code) {
        checkLengths(code, ncentr);
        double[] ret = new double[imgsize];
        Arrays.fill(ret, 0d);
        // for (int i=0; i<code.length; i++) {
        //     System.out.print(code[i] + " ");
        // }
        // System.out.println();

        for (int imgPos=0; imgPos<imgsize; imgPos++) {
            for (int cIdx=0; cIdx<ncentr; cIdx++) {
                ret[imgPos] += code[cIdx]*(centroids[cIdx].getData()[imgPos]);
            }
        }
        return ret; //(new BMPLoader("","")).rescale(ret);
    }

    public double[] reconstructionError(double[] orig, double[] reconstr) {
        checkLengths(orig, reconstr);
        double[] ret = new double[orig.length];
        // simple difference - possibly use squared error instead
        for (int i=0; i<orig.length; i++) {
            ret[i] = Math.abs(orig[i]-reconstr[i]);
        }
        return ret;
    }

    public double totalReconstructionError(double[] orig, double[] reconstr) {
        double[] tmp = reconstructionError(orig, reconstr);
        double ret = 0;
        // just total it - what a godforsaken language Java is...
        for (int i=0; i<tmp.length; i++) {
            ret += tmp[i];
        }
        return ret;
    }

    // train on single image
    public void train(double[] img) {
        double[] closestWI = maxWithIndex(similarities(img));
        // here you get the max in closestWI[0] if you need it
        int closestIdx = (int)closestWI[1];
        // train the closest centroid
        centroids[closestIdx].train(img);
    }

    // train on set of images
    public void train(double[][] imgs) {
        for (int i=0; i<imgs.length; i++) {
            train(imgs[i]);
        }
    }

    public double[][] getData() {
        double[][] ret = new double[ncentr][];
        for (int i=0; i<ncentr; i++) {
            ret[i] = centroids[i].getData();
        }
        return ret;
    }

    public static void main(String[] args) {

        // directory where the input images are located
        String indir = "jaffe";
        // directory where to place the output (centroids, etc)
        String outdir = "out";
        // number of centroids
        int NCENTR  = 8;
        // size of training set
        int NTRAIN  = 100; // (bmp.listBMP().length/4)
        // size of validation set
        int NVALID  = 4;

        // load images
        BMPLoader bmp = new BMPLoader(indir, outdir);
        double[][] images = bmp.readAll(NTRAIN);
        System.out.println("Elaborating images: " +
            images.length + "x" + bmp.height + "x" + bmp.width);

        // train
        SVQ svq = new SVQ(NCENTR, images[0].length);
        svq.train(images);
        bmp.saveAll(svq.getData(), "centr");

        double[][] selected = new double[NVALID][];
        double[][] codes    = new double[NVALID][];
        double[][] reconstr = new double[NVALID][];
        double[][] errors   = new double[NVALID][];

        // NVALID "pseudorandom" images, from training range
        for (int i=0; i<NVALID; i++) {
            // select
            selected[i] = images[10*i];
            // compress
            codes[i] = svq.code(selected[i]);
            // reconstruct
            reconstr[i] = svq.reconstruct(codes[i]);
            // reconstruction error
            errors[i] = svq.reconstructionError(selected[i], reconstr[i]);
        }
        bmp.saveAll(selected, "image");
        bmp.saveAll(reconstr, "reconstr");
        bmp.saveAll(errors, "x_error");
        // print codes?

        System.out.println("\nDone!");
    }
}

