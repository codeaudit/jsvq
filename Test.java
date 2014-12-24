public class Test {
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
