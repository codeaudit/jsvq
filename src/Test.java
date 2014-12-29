// SVQ test class
package Test.SVQ;

// Get JAFFE database from http://www.kasrl.org/jaffe_info.html
// Extract pics in folder named "jaffe"
// Convert to bmp with `ls *.tiff | while read f; do convert "$f" "${f%.*}.bmp"; done`

import BMPLoader.*;
import SVQ.*;

public class Test {
    public static void main(String[] args) {

        // directory where the input images are located
        // String indir = System.getProperty("user.home")+"/torcs_imgs/";
        String indir = "torcs";
        // directory where to place the output (centroids, etc)
        String outdir = "out";
        // number of centroids
        int NCENTR  = 8;
        // size of training set
        int NTRAIN  = -1; // -1 -> all
        // size of validation set
        int NVALID  = 4;
        // number of trainings over the training set
        int NTRAINS = 1;
        // whether to activate negative training: { "no", "all", "least" }
        String UNTRAIN = "no";
        // comparison method to use inside centroids: { "dot", "hik" }
        String COMPMETHOD = "hik";
        // similarity method to use inside centroids:
        // { "simpleDotProduct", "shiftedDotProduct", "squareError",
        //   "simpleHistogram", "pyramidMatching", "spacialPyramidMatching" }
        String SIMILMETHOD = "spacialPyramidMatching";
        // size of autotraining set
        int TRAINSETSIZE = 3; // -1 -> disable

        // load images
        BMPLoader bmp = new BMPLoader(indir, outdir);
        // int[][] images = bmp.readAll();
        int[][] images = bmp.readAll(NTRAIN);
        System.out.println("Elaborating images: " +
            images.length + "x" + bmp.height + "x" + bmp.width);

        // Declare svq
        SVQ svq = new SVQ(NCENTR, images[0].length,
                          COMPMETHOD, SIMILMETHOD, UNTRAIN,
                          TRAINSETSIZE);

        // Train - select by CODING them! (autotrain feature)
        for (int i=0; i<NTRAINS; i++) {
            System.out.println("Training "+(i+1));
            for (int j=0; j<images.length; j++ ) {
                // code image - simulate new observation
                svq.code(images[j]);
                if (j%10==0) {
                    // flush every 10 images - simulate new individual
                    svq.flushTrainingSet();
                }
                if (j%100==0) {
                    // train every 100 images - simulate new generation
                    svq.autoTrain();
                }
            }
            // flush and train on remaining
        }
        bmp.saveAll(svq.getData(), "centr");

        int[][] selected = new int[NVALID][];
        int[][] codes    = new int[NVALID][];
        int[][] reconstr = new int[NVALID][];
        int[][] errors   = new int[NVALID][];

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

        // total errors
        int tot, avg, ttot=0;
        System.out.print("Avg errors: ");
        for (int i=0; i<errors.length; i++) {
            tot=0;
            for (int j=0; j<errors[i].length; j++) {
                tot += errors[i][j];
            }
            avg = tot/errors[i].length;
            ttot += avg;
            System.out.print(avg+" ");
        }
        System.out.print("/ "+ttot);
        System.out.println();

        System.out.println("\nDone!");
    }


// OLD TESTS


    // Test if the sorting is correct - should be DESC
    public static void testTrainingSet() {
        int maxsize = 2;
        TrainingSet ts = new TrainingSet(maxsize);
        double[] sims;
        int[] vals = {0,0,0};
        ts.tryAdd(vals, 2.0);
        ts.tryAdd(vals, 1.0);
        ts.tryAdd(vals, 3.0);

        sims = ts.getCurrentSims();
        System.out.println("Current: ");
        for (int i=0; i<sims.length; i++) {
            System.out.println(sims[i]);
        }

        sims = ts.getFullSims();
        System.out.println("Full size: " + sims.length);

        ts.flushCurrent();

        ts.tryAdd(vals, 4.0);
        ts.tryAdd(vals, 6.0);
        ts.tryAdd(vals, 5.0);

        sims = ts.getCurrentSims();
        System.out.println("Current: ");
        for (int i=0; i<sims.length; i++) {
            System.out.println(sims[i]);
        }

        sims = ts.getFullSims();
        System.out.println("Full: ");
        for (int i=0; i<sims.length; i++) {
            System.out.println(sims[i]);
        }

        ts.flushCurrent();

        sims = ts.getFullSims();
        System.out.println("Full: ");
        for (int i=0; i<sims.length; i++) {
            System.out.println(sims[i]);
        }

        int[][] vecs = ts.returnVecsAndReset();
        System.out.println("Final vecs: ");
        for (int i=0; i<vecs.length; i++) {
            for (int j=0; j<vecs[0].length; j++) {
                System.out.print(vecs[i][j]);
            }
            System.out.println();
        }

        sims = ts.getCurrentSims();
        System.out.println("Current size: " + sims.length);

        sims = ts.getFullSims();
        System.out.println("Full size: " + sims.length);
    }
}
