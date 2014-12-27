import java.util.Random;
import java.util.Arrays;

class Centroid {
    short[] data;
    int size, ntrains;

    int INTENSITIES = 256;
    double MINLRATE = 1d/100;
    double UNTRAINRATIO = 1d/100;

    public Centroid(int size) {
        this.size = size;
        this.ntrains = 1;
        this.data = new short[size];
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            // TESTING WHITE CENTROIDS - DOT MATCH ALL!!
            // data[i] = (short)255;
            data[i] = (short) random.nextInt(INTENSITIES);
        }
    }

    // Quick error check for vector size consistency
    public void checkSize(short[] vec) {
        if (vec.length != size) {
            throw new RuntimeException(
                "Input vector length does not match centroid size.");
        }
    }

    // Trains the centroid to be more similar to the input vector
    public void train(short[] vec, double[] lrates) {
        checkSize(vec);
        for (int i=0; i<size; i++) {
            data[i] = (short) ((lrates[0]*data[i]) + (lrates[1]*vec[i]));
            // normalize
            if (data[i] < 0) { data[i] = 0; }
            if (data[i] > INTENSITIES-1) { data[i] = (short)(INTENSITIES-1); }
        }
        ntrains++;
    }

    public void train(short[] vec) {
        train(vec, lrates());
    }

    // Per-centroid learning rates - [0] for data, [1] for vec
    public double[] lrates(double factor) {
        double[] ret = new double[2];
        // linearly decaying, lower bound
        ret[1] = Math.max(factor/ntrains, factor*MINLRATE);
        ret[0] = 1-ret[1];
        return ret;
    }

    public double[] lrates() {
        return lrates(1d); // default: don't scale
    }

    // Trains the centroid to be a bit less similar to the input vector
    public void untrain(short[] vec) {
        // we want the changes to be minimal
        double[] lrs = lrates(UNTRAINRATIO);
        // adjust data[] learning rate
        lrs[0] = 1-lrs[1];
        // we want the changes to be negative
        lrs[1] = -lrs[1];

        train(vec, lrs);
        ntrains--; // do not count untrains for the centroid
    }

    public short[] getData() {
        return data;
    }

    public double similarity(Centroid c) {
        return similarity(c.getData());
    }

    public double similarity(short[] vec) {
        checkSize(vec);                   // Avg total reconstr errors on 4 imgs
                                          // (/ totals) after training on 100 imgs
        // return simpleDotProduct(vec);  //  12 36 23 41 / 112
            // Using untraining on least:    23 35 18 41 / 117
        // return shiftedDotProduct(vec); // 34 63 32 59 / 188
            // Using untraining on least:    34 63 32 59 / 188
        // return squareError(vec);       // 26 37 34 42 / 139
            // Using untraining on least:    27 47 31 49 / 154
        // return simpleHistogram(vec);   // 29 35 31 31 / 126
            // Using untraining on least:    24 22 33 32 / 111
            // Using hik + untrain least:    25 41 23 31 / 120
        return multiresHistogram(vec); // 29 45 31 54 / 159
            // Using untraining on least:    24 21 16 33 / 94
            // Using hik + untrain least:    23 34 23 31 / 111
    }

    // SIMILARITY MEASURES

    public double simpleDotProduct(short[] vec) {
        // Only measures corresponding lighting
        double ret = 0;
        for (int i=0; i<size; i++) {
            ret += data[i] * vec[i];
        }
        return ret/vec.length;
    }

    public double shiftedDotProduct(short[] vec) {
        // Gives same importance to luminosity and darkness matches
        double ret = 0;
        for (int i=0; i<size; i++) {
            // Shift both in range [-128,127]
            ret += (data[i]-128) * (vec[i]-128);
        }
        return ret/vec.length;
    }

    public double squareError(short[] vec) {
        // Evergreen classic
        double ret = 0;
        for (int i=0; i<size; i++) {
            ret += Math.pow(data[i] - vec[i], 2);
        }
        return ret/vec.length;
    }

    private enum InnerType { PRODUCT, MINIMUM }
    public int inner(int[] a, int[] b, String type) {
        InnerType opt = InnerType.valueOf(type.toUpperCase());
        int ret = 0;
        for (int i=0; i<a.length; i++) {
            switch (opt) {
                case PRODUCT:
                    ret += a[i] * b[i];
                    break;
                case MINIMUM:
                    ret += Math.min(a[i], b[i]);
                    break;
            }
        }
        return ret;
    }

    public int dot(int[] a, int[] b) {
        return inner(a, b, "product");
    }

    // Histogram Intersection Kernel
    public int hik(int[] a, int[] b) {
        return inner(a, b, "minimum");
    }

    public int[][] getHists(short[] a, short[] b) {
        int[][] ret = new int[2][];
        ret[0] = new int[INTENSITIES];
        ret[1] = new int[INTENSITIES];
        Arrays.fill(ret[0], 0);
        Arrays.fill(ret[1], 0);

        // count instances of every intensity
        for (int i=0; i<size; i++) {
            ret[0][a[i]]++;
            ret[1][b[i]]++;
        }
        return ret;
    }

    public int[] getHist(short[] a) {
        int[] ret = new int[INTENSITIES];
        Arrays.fill(ret, 0);
        // count instances of every intensity
        for (int i=0; i<a.length; i++) {
            ret[a[i]]++;
        }
        return ret;
    }

    // TODO: compute (maintain) centroid histogram while training
    public double simpleHistogram(short[] vec) {
        int[][] hists = getHists(data, vec);
        // TODO: try comparing with difference and square error
        // return dot(hists[0],hists[1])/(double)INTENSITIES;

        // most work uses histogram intersection kernel rather than dot product
        return hik(hists[0],hists[1])/(double)INTENSITIES;
    }

    // TODO: compute (maintain) centroid histogram while training
    public double multiresHistogram(short[] vec) {
        int[][] hists = getHists(data, vec);
        double dottotal = 0, weight;

        int[][] sums = new int[2][];
        sums[0] = new int[INTENSITIES];
        sums[1] = new int[INTENSITIES];

        // Change nchunks: start by all pixels independently,
        // then sum two, then sum 4...
        for (int nchunks=INTENSITIES; nchunks>=1; nchunks/=2) {
            Arrays.fill(sums[0], (short)0);
            Arrays.fill(sums[1], (short)0);

            int i=0;
            // Now compute sums per each part in the nchunks
            for (int border=0; border<INTENSITIES; border+=INTENSITIES/nchunks) {
                // walk from where last parted ended until next border
                for (; i<border; i++) {
                    // the position where to write it does not matter, as long
                    // as we don't get out of the array and the position is the
                    // same in both arrays, the dot product will be fine
                    sums[0][border] += hists[0][i];
                    sums[1][border] += hists[1][i];
                }
            }

            // Each nchunks calculation is weighted by its level of detail
            weight = ((double)nchunks/INTENSITIES);
            // then compare them
            // TODO: try comparing with difference and square error
            // TODO: compute avg on moving window instead of blind sums
            // dottotal += weight * dot(sums[0],sums[1]);

            // Pyramid Match Kernel (Grauman et al.)
            dottotal += weight * hik(sums[0],sums[1]);
        }

        return dottotal;
    }


    // Spacial histogram methods

    // Returns the histogram for matrix of size linesize, block of size blocksize, block-row row, block-column col
    public int[] getBlockHist(short[] m, int linesize, int blocksize, int row, int col) {

        // Start from top-right corner
        int i = 0;

        // First move down to the block row:
        // - linesize is a complete line
        // - blocksize is the block height
        // - row is the number of block rows
        i += linesize * blocksize * row;

        // Now move to the right block in the row
        // - blocksize is the number of columns in a block
        // - col is the number of blocks to skip
        i += blocksize * col;

        // We are now at the top-left corner of the right block

        // We need a border to know when we finish counting for the current line
        int border;
        // And a variable where to store the block's histogram
        int[] hist = new int[INTENSITIES];
        Arrays.fill(hist, 0);

        // Cycle over the lines
        for (int nline=0; nline<blocksize; nline++) {
            // Place the border at the end of the current line
            border = i+blocksize;

            // Now move `i` and build the histogram up to the border
            for (; i<border; i++) {
                hist[m[i]]++;
            }

            // `i` now points at the first cell of the next block
            // we need to bring it down one full line, then back a block size
            i += linesize - blocksize;
            // The border instead just needs to go down one full line
            border += linesize;
        }

        // The histogram for the block has been compiled
        return hist;
    }

}
