// Sparse Vector Quantization
package SVQ;

import java.io.File;
import java.util.Arrays;

public class SVQ {

    Centroid[] centroids;
    int ncentr, imgsize;
    TrainingSet tset; // if not null, enables autotrain

    public SVQ(int ncentr, int imgsize, String compMethod, String similMethod,
               int tsetsize) {
        this(ncentr, imgsize, compMethod, similMethod);
        if (tsetsize>0) {
            tset = new TrainingSet(tsetsize);
        }
    }

    public SVQ(int ncentr, int imgsize, String compMethod, String similMethod) {
        this.ncentr = ncentr;
        this.imgsize = imgsize;
        centroids = new Centroid[ncentr];
        for (int i=0; i<ncentr; i++) {
            centroids[i] = new Centroid(imgsize, compMethod, similMethod);
        }
    }

    public void checkLengths(int[] a, int[] b){
        checkLengths(a, b.length);
    }

    public void checkLengths(int[] a, int bLen){
        if (a.length != bLen) {
            throw new RuntimeException(
                "Vectors lengths don't match.");
        }
    }

    public double[] similarities(int[] vec) {
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

    public int[] minMaxIndices(double[] vec) {
        int[] ret = { 0, 0 };
        for (int i=1; i<vec.length; i++) {
            if (vec[i]<vec[ret[0]]) { ret[0] = i; }
            if (vec[i]>vec[ret[1]]) { ret[1] = i; }
        }
        return ret;
    }

    public int[] code(int[] vec) {
        int[] ret = new int[ncentr];
        Arrays.fill(ret, (int)0);

        double[] mwi = maxWithIndex(similarities(vec));
        // System.out.println("Max: "+mwi[0]+" - idx: "+mwi[1]);
        // here mwi[0] holds the max, if you need it
        ret[(int)mwi[1]] = 1;
        return ret;
    }

    public int[] reconstruct(int[] code) {
        checkLengths(code, ncentr);
        int[] ret = new int[imgsize];
        Arrays.fill(ret, (int)0);

        for (int imgPos=0; imgPos<imgsize; imgPos++) {
            for (int cIdx=0; cIdx<ncentr; cIdx++) {
                ret[imgPos] += code[cIdx]*(centroids[cIdx].getData()[imgPos]);
            }
        }
        return ret; //(new BMPLoader("","")).rescale(ret);
    }

    public int[] reconstructionError(int[] orig, int[] reconstr) {
        checkLengths(orig, reconstr);
        int[] ret = new int[orig.length];
        // simple difference - possibly use squared error instead
        for (int i=0; i<orig.length; i++) {
            ret[i] = (int)Math.abs(orig[i]-reconstr[i]);
        }
        return ret;
    }

    public int totalReconstructionError(int[] orig, int[] reconstr) {
        int[] tmp = reconstructionError(orig, reconstr);
        int ret = 0;
        // just total it - what a godforsaken language Java is...
        for (int i=0; i<tmp.length; i++) {
            ret += tmp[i];
        }
        return ret;
    }

    public void untrainAllBut(int idx, int[] img) {
        for (int i=0; i<ncentr; i++) {
            if (i!=idx) {
                centroids[i].untrain(img);
            }
        }
    }

    private enum TrainOpts { NO, ALL, LEAST }

    // train on single image
    public void train(int[] img, String untrain) {
        int[] minmax = minMaxIndices(similarities(img));
        int idxLeastSimilar = minmax[0];
        int idxMostSimilar  = minmax[1];

        TrainOpts opt = TrainOpts.valueOf(untrain.toUpperCase());
        switch (opt) {
            case NO:
                centroids[idxMostSimilar].train(img);
                break;
            case ALL:
                untrainAllBut(idxMostSimilar, img);
                centroids[idxMostSimilar].train(img);
                break;
            case LEAST:
                centroids[idxMostSimilar].train(img);
                centroids[idxLeastSimilar].untrain(img);
        }
    }

    public void train(int[] img) {
        train(img, "no");
    }

    // train on set of images
    public void train(int[][] imgs, String untrain) {
        for (int i=0; i<imgs.length; i++) {
            train(imgs[i], untrain);
        }
    }

    public void train(int[][] imgs) {
        train(imgs, "no");
    }

    public int[][] getData() {
        int[][] ret = new int[ncentr][];
        for (int i=0; i<ncentr; i++) {
            ret[i] = centroids[i].getData();
        }
        return ret;
    }
}

