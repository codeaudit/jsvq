// Sparse Vector Quantization
package SVQ;

import java.io.File;
import java.util.Arrays;

public class SVQ {

    Centroid[] centroids;
    int ncentr, imgsize;

    public SVQ(int ncentr, int imgsize, String compMethod, String similMethod) {
        this.ncentr = ncentr;
        this.imgsize = imgsize;
        centroids = new Centroid[ncentr];
        for (int i=0; i<ncentr; i++) {
            centroids[i] = new Centroid(imgsize, compMethod, similMethod);
        }
    }

    public void checkLengths(short[] a, short[] b){
        checkLengths(a, b.length);
    }

    public void checkLengths(short[] a, int bLen){
        if (a.length != bLen) {
            throw new RuntimeException(
                "Vectors lengths don't match.");
        }
    }

    public double[] similarities(short[] vec) {
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

    public short[] code(short[] vec) {
        short[] ret = new short[ncentr];
        Arrays.fill(ret, (short)0);

        double[] mwi = maxWithIndex(similarities(vec));
        // System.out.println("Max: "+mwi[0]+" - idx: "+mwi[1]);
        // here mwi[0] holds the max, if you need it
        ret[(int)mwi[1]] = 1;
        return ret;
    }

    public short[] reconstruct(short[] code) {
        checkLengths(code, ncentr);
        short[] ret = new short[imgsize];
        Arrays.fill(ret, (short)0);

        for (int imgPos=0; imgPos<imgsize; imgPos++) {
            for (int cIdx=0; cIdx<ncentr; cIdx++) {
                ret[imgPos] += code[cIdx]*(centroids[cIdx].getData()[imgPos]);
            }
        }
        return ret; //(new BMPLoader("","")).rescale(ret);
    }

    public short[] reconstructionError(short[] orig, short[] reconstr) {
        checkLengths(orig, reconstr);
        short[] ret = new short[orig.length];
        // simple difference - possibly use squared error instead
        for (int i=0; i<orig.length; i++) {
            ret[i] = (short)Math.abs(orig[i]-reconstr[i]);
        }
        return ret;
    }

    public short totalReconstructionError(short[] orig, short[] reconstr) {
        short[] tmp = reconstructionError(orig, reconstr);
        short ret = 0;
        // just total it - what a godforsaken language Java is...
        for (int i=0; i<tmp.length; i++) {
            ret += tmp[i];
        }
        return ret;
    }

    public void untrainAllBut(int idx, short[] img) {
        for (int i=0; i<ncentr; i++) {
            if (i!=idx) {
                centroids[i].untrain(img);
            }
        }
    }

    private enum TrainOpts { NO, ALL, LEAST }

    // train on single image
    public void train(short[] img, String untrain) {
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

    public void train(short[] img) {
        train(img, "no");
    }

    // train on set of images
    public void train(short[][] imgs, String untrain) {
        for (int i=0; i<imgs.length; i++) {
            train(imgs[i], untrain);
        }
    }

    public void train(short[][] imgs) {
        train(imgs, "no");
    }

    public short[][] getData() {
        short[][] ret = new short[ncentr][];
        for (int i=0; i<ncentr; i++) {
            ret[i] = centroids[i].getData();
        }
        return ret;
    }
}

