// Sparse Vector Quantization
package SVQ;

import java.io.File;
import java.util.Arrays;

public class SVQ {

    public Centroid[] centroids;
    public int ncentr, imgsize;
    public TrainingSet tset; // if not null, enables autotrain

    private enum TrainOpts { NO, ALL, LEAST }
    TrainOpts untrain;

    public SVQ(int ncentr, int imgsize, String compMethod, String similMethod,
               String untrain, int tsetperflush) {
        this(ncentr, imgsize, compMethod, similMethod, untrain);
        if (tsetperflush>0) {
            tset = new TrainingSet(tsetperflush);
        }
    }

    public SVQ(int ncentr, int imgsize, String compMethod, String similMethod,
               String untrain) {
        this.ncentr = ncentr;
        this.imgsize = imgsize;
        this.untrain = TrainOpts.valueOf(untrain.toUpperCase());
        centroids = new Centroid[ncentr];
        for (int i=0; i<ncentr; i++) {
            centroids[i] = new Centroid(imgsize, compMethod, similMethod);
        }
    }

    // Training set interface: code(vec), autoTrain()

    public void autoTrain() {
        train(tset.flush());
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

    public int maxIdx(double[] vec) {
        int maxidx = 0;
        for (int i=1; i<vec.length; i++) {
            if (vec[i]>vec[maxidx]) {
                maxidx = i;
            }
        }
        return maxidx;
    }

    public int[] minMaxIndices(double[] vec) {
        int[] ret = { 0, 0 };
        for (int i=1; i<vec.length; i++) {
            if (vec[i]<vec[ret[0]]) { ret[0] = i; }
            if (vec[i]>vec[ret[1]]) { ret[1] = i; }
        }
        return ret;
    }

    // I could have easily written a method (ok I admit I did)
    // public int[][] code(String id, int[][] vecs) { return code("0", vecs); }
    // but the current usage does not require it, and it could hide bugs in
    // my migration towards the new id-based lists.
    public int[][] code(String id, int[][] vecs) {
        int[][] ret = new int[vecs.length][];
        for (int i=0; i<vecs.length; i++) {
            ret[i] = code(id, vecs[i]);
        }
        return ret;
    }

    public int[] code(String id, int[] vec) {
        int[] ret = new int[ncentr];
        Arrays.fill(ret, (int)0);

        double[] sims = similarities(vec);
        int idx = maxIdx(sims);

        // if we're using autotrain
        if (tset != null) {
            // try to add the image to the training set
            tset.tryAdd(id, vec,sims[idx]);
            // TODO: I could also cache the similarities
        }

        ret[idx] = 1;
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

    // train on single image
    public void train(int[] img) {
        int[] minmax = minMaxIndices(similarities(img));
        int idxLeastSimilar = minmax[0];
        int idxMostSimilar  = minmax[1];

        switch (untrain) {
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

    // train on set of images
    public void train(int[][] imgs) {
        for (int i=0; i<imgs.length; i++) {
            train(imgs[i]);
        }
    }

    public int[][] getData() {
        int[][] ret = new int[ncentr][];
        for (int i=0; i<ncentr; i++) {
            ret[i] = centroids[i].getData();
        }
        return ret;
    }
}

