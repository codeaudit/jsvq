// SVQ test class

package Test.SVQ;

import BMPLoader.*;
import SVQ.*;

public class Test {
    public static void main(String[] args) {
        // Get JAFFE database from http://www.kasrl.org/jaffe_info.html
        // Extract pics in folder named "jaffe"
        // Convert to bmp with `ls *.tiff | while read f; do convert "$f" "${f%.*}.bmp"; done`

        // directory where the input images are located
        // String indir = System.getProperty("user.home")+"/torcs_imgs/";
        String indir = "torcs";
        // directory where to place the output (centroids, etc)
        String outdir = "out";
        // number of centroids
        int NCENTR  = 8;
        // size of training set
        int NTRAIN  = 100; // -1 -> all
        // size of validation set
        int NVALID  = 4;
        // number of trainings over the training set
        int NTRAINS = 1;
        // whether to activate negative training: { "no", "all", "least" }
        String UNTRAIN = "no";
        // comparison method to use inside centroids: { "dot", "hik" }
        String COMPMETHOD = "dot";
        // similarity method to use inside centroids:
        // { "simpleDotProduct", "shiftedDotProduct", "squareError",
        //   "simpleHistogram", "pyramidMatching", "spacialPyramidMatching" }
        String SIMILMETHOD = "simpleDotProduct";
        // size of autotraining set
        int TRAINSETSIZE = 3; // -1 -> disable
        // save BMP images at end of computation?
        boolean saveimgs = true;

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
        // Single id // TODO maybe add this to SVQ?
        String id = "0";

        // Train - select by CODING them! (autotrain feature)
        for (int i=0; i<NTRAINS; i++) {
            System.out.println("Training "+(i+1));
            for (int j=0; j<images.length; j++ ) {
                // code image - simulate new observation
                svq.code(id, images[j]);
                if (j%10==0) {
                    // change id every 10 images - simulate new individual
                    id = Integer.toString(j);
                }
                if (j%100==0) {
                    // train every 100 images - simulate new generation
                    svq.autoTrain();
                }
            }
            // flush and train on remaining
        }

        int[][] selected = new int[NVALID][];
        int[][] codes    = new int[NVALID][];
        int[][] reconstr = new int[NVALID][];
        int[][] errors   = new int[NVALID][];

        // NVALID "pseudorandom" images, from training range
        for (int i=0; i<NVALID; i++) {
            // select
            selected[i] = images[10*i];
            // compress
            codes[i] = svq.code(id,selected[i]);
            // reconstruct
            reconstr[i] = svq.reconstruct(codes[i]);
            // reconstruction error
            errors[i] = svq.reconstructionError(selected[i], reconstr[i]);
        }
        if (saveimgs) {
            bmp.saveAll(svq.getData(), "centr");
            bmp.saveAll(selected, "image");
            bmp.saveAll(reconstr, "reconstr");
            bmp.saveAll(errors, "x_error");
        }

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


    public static void testTrainingSet() {
        TrainingSet tset = new TrainingSet(1);

        String[] ids = {"ciccio", "pluto", "pippo"};
        int[] data;
        int sim;

        sim = 1;
        data = new int[1];
        data[0] = (int) sim;
        tset.tryAdd(ids[sim], data, sim);
        tset.tryAdd(ids[sim], data, sim);

        sim = 2;
        data = new int[1];
        data[0] = (int) sim;
        tset.tryAdd(ids[sim], data, sim);
        tset.tryAdd(ids[sim], data, sim);

        sim = 0;
        data = new int[1];
        data[0] = (int) sim;
        tset.tryAdd(ids[sim], data, sim);
        tset.tryAdd(ids[sim], data, sim);

        int[][] res = tset.flush();
        for (int[] img : res) {
            for (int i : img) {
                System.out.print(i+" ");
            }
            System.out.println();
        }
    }
}
