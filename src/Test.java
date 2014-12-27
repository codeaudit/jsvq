public class Test {
    public static void main(String[] args) {

        // directory where the input images are located
        String indir = "jaffe";
        // directory where to place the output (centroids, etc)
        String outdir = "out";
        // number of centroids
        int NCENTR  = 8;
        // size of training set
        int NTRAIN  = -1;          // -1 -> all
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

        // load images
        BMPLoader bmp = new BMPLoader(indir, outdir);
        // short[][] images = bmp.readAll();
        short[][] images = bmp.readAll(NTRAIN);
        System.out.println("Elaborating images: " +
            images.length + "x" + bmp.height + "x" + bmp.width);

        // train
        SVQ svq = new SVQ(NCENTR, images[0].length, COMPMETHOD, SIMILMETHOD);
        for (int i=0; i<NTRAINS; i++) {
            System.out.println("Training "+(i+1));
            svq.train(images, UNTRAIN);
        }
        bmp.saveAll(svq.getData(), "centr");

        short[][] selected = new short[NVALID][];
        short[][] codes    = new short[NVALID][];
        short[][] reconstr = new short[NVALID][];
        short[][] errors   = new short[NVALID][];

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
}
