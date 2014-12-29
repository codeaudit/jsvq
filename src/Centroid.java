package SVQ;

import java.util.Random;
import java.util.Arrays;

class Centroid {
    int[] data;
    int size, ntrains;

    int INTENSITIES = 256;
    double MINLRATE = 1d/100;
    double UNTRAINRATIO = 1d/100;

    private enum CompareMethod { DOT, HIK }
    CompareMethod compareMethod;
    private enum SimilarityMethod {
        SIMPLEDOTPRODUCT,
        SHIFTEDDOTPRODUCT,
        SQUAREERROR,
        SIMPLEHISTOGRAM,
        PYRAMIDMATCHING,
        SPACIALPYRAMIDMATCHING
    }
    SimilarityMethod similarityMethod;

    public Centroid(int size, String compareMethod, String similarityMethod) {
        this.size = size;
        this.ntrains = 1;
        this.data = new int[size];
        this.compareMethod = CompareMethod.valueOf(compareMethod.toUpperCase());
        this.similarityMethod = SimilarityMethod.valueOf(similarityMethod.toUpperCase());
        randomInitData();
    }

    // TODO: it seems to perform better if the random initialization is drawn
    // from a distribution with same mean and variance as the images
    // TODO: at first SVQ training, first reset all randomly initialized
    // centroids with data from a distribution similar to imgs in first batch
    public void randomInitData() {
        int k = 0; // this allows to bias the random up
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            data[i] = (int) (k + random.nextInt(INTENSITIES-k));
        }
    }

    // Quick error check for vector size consistency
    public void checkSize(int[] vec) {
        if (vec.length != size) {
            throw new RuntimeException(
                "Input vector length does not match centroid size.");
        }
    }

    // Trains the centroid to be more similar to the input vector
    public void train(int[] vec, double[] lrates) {
        checkSize(vec);
        for (int i=0; i<size; i++) {
            data[i] = (int) ((lrates[0]*data[i]) + (lrates[1]*vec[i]));
            // normalize
            if (data[i] < 0) { data[i] = 0; }
            if (data[i] > INTENSITIES-1) { data[i] = (int)(INTENSITIES-1); }
        }
        ntrains++;
    }

    public void train(int[] vec) {
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
    public void untrain(int[] vec) {
        // we want the changes to be minimal
        double[] lrs = lrates(UNTRAINRATIO);
        // adjust data[] learning rate
        lrs[0] = 1-lrs[1];
        // we want the changes to be negative
        lrs[1] = -lrs[1];

        train(vec, lrs);
        ntrains--; // do not count untrains for the centroid
    }

    public int[] getData() {
        return data;
    }

    public double similarity(Centroid c) {
        return similarity(c.getData());
    }

    public double similarity(int[] vec) {
        checkSize(vec);
        double ret = Double.NaN;
        switch (similarityMethod) {
            case SIMPLEDOTPRODUCT:
                ret = simpleDotProduct(vec);
                break;
            case SHIFTEDDOTPRODUCT:
                ret = shiftedDotProduct(vec);
                break;
            case SQUAREERROR:
                ret = squareError(vec);
                break;
            case SIMPLEHISTOGRAM:
                ret = simpleHistogram(vec);
                break;
            case PYRAMIDMATCHING:
                ret = pyramidMatching(vec);
                break;
            case SPACIALPYRAMIDMATCHING:
                ret = spacialPyramidMatching(vec);
                break;
        }
        return ret;
    }

    // SIMILARITY MEASURES

    public double simpleDotProduct(int[] vec) {
        // Only measures corresponding lighting
        double ret = 0;
        for (int i=0; i<size; i++) {
            ret += data[i] * vec[i];
        }
        return ret/vec.length;
    }

    public double shiftedDotProduct(int[] vec) {
        // Gives same importance to luminosity and darkness matches
        double ret = 0;
        for (int i=0; i<size; i++) {
            // Shift both in range [-128,127]
            ret += (data[i]-128) * (vec[i]-128);
        }
        return ret/vec.length;
    }

    public double squareError(int[] vec) {
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

    // TODO: do we need inner() at this point? refactor if not
    // TODO: try comparing with difference and square error
    public int compare(int[] a, int[] b) {
        double ret = Double.NaN; // cosa mi tocca fare per avere un check...
        switch (compareMethod) {
            case DOT:
                ret = dot(a,b);
                break;
            case HIK:
                ret = hik(a,b);
                break;
        }
        if (ret==Double.NaN) {
            throw new RuntimeException("Unrecognized compare method.");
        }
        return (int)ret;
    }

    public int[][] getHists(int[] a, int[] b) {
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

    // Histogram methods

    public int[] getHist(int[] a) {
        int[] ret = new int[INTENSITIES];
        Arrays.fill(ret, 0);
        // count instances of every intensity
        for (int i=0; i<a.length; i++) {
            ret[a[i]]++;
        }
        return ret;
    }

    // TODO: compute (maintain) centroid histogram while training
    public double simpleHistogram(int[] vec) {
        int[][] hists = getHists(data, vec);
        // most work uses histogram intersection kernel rather than dot product
        return compare(hists[0], hists[1]) / (double)INTENSITIES;
    }

    // TODO: compute (maintain) centroid histogram while training
    public double pyramidMatching(int[] vec) {
        int[][] hists = getHists(data, vec);
        double similarity = 0, weight;

        int[][] sums = new int[2][];
        sums[0] = new int[INTENSITIES];
        sums[1] = new int[INTENSITIES];

        // Change nchunks: start by all pixels independently,
        // then sum two, then sum 4...
        for (int nchunks=INTENSITIES; nchunks>=1; nchunks/=2) {
            Arrays.fill(sums[0], (int)0);
            Arrays.fill(sums[1], (int)0);

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
            // Pyramid Match Kernel (Grauman et al.) uses HIK here
            similarity += weight * compare(sums[0],sums[1]);
        }

        return similarity;
    }


    // Spacial histogram methods

    // Returns the histogram for matrix of size linesize,
    // block of size blocksize, row and column in block coordinates
    public int[] getBlockHist(int[] m, int linesize, int blocksize, int row, int col) {

        // Start from top-left corner
        int i = 0;

        // First move down to the block row:
        // - linesize is a complete line
        // - blocksize is the block height
        // - row is the number of block rows
        i += linesize * blocksize * row;

        // Now move to the correct block in the block row
        // - blocksize is the number of columns in a block
        // - col is the number of blocks to skip
        i += blocksize * col;

        // We are now at the top-left corner of the correct block

        // We need a border to know when we finish counting for the current line
        // Place the border at the end of the current line
        int border = i+blocksize;
        // And a variable where to store the block's histogram
        int[] hist = new int[INTENSITIES];
        Arrays.fill(hist, 0);

        // Cycle over the lines of the block
        for (int nline=0; nline<blocksize; nline++) {
            // Now move `i` and build the histogram up to the border
            for (; i<border; i++) {
                hist[m[i]]++;
            }

            // `i` now points at the first cell of the next block/line
            // we need to bring it down one full line, then back a block size
            i += linesize - blocksize;
            // The border instead just needs to go down one full line
            border += linesize;
        }

        // The histogram for the block has been compiled
        return hist;
    }

    // Logarithm base 2 optimized for integers
    // http://stackoverflow.com/questions/3305059/how-do-you-calculate-log-base-2-in-java-for-integers
    public static int log2(int n){
        if(n <= 0) throw new IllegalArgumentException();
        return 31 - Integer.numberOfLeadingZeros(n);
    }

    // Spacial Pyramid Matching algorithm from Lazebnik & al.
    public double spacialPyramidMatching(int [] m) {
        // Hypothesis: m is an array representation of a square matrix
        // Same of course should go for the centroid (they lie in same space)
        int linesize = (int) Math.sqrt(m.length);
        // Hypothesis: the size of m's matrix is a power of 2, with exp <= MAXRES
        int MAXRES = 3;//log2(linesize); // TOO slow!
        // Support vars
        int blocksize, blocksPerLine, nblocks, row, col;
        int[] tmphist;
        int[][] hists = new int[2][], tmphists = new int[2][];
        double similarity = 0, weight;

        // Per each resolution
        for (int res=0; res<=MAXRES; res++) {
            // Compute how many blocks per line(/column)
            blocksPerLine = (int)Math.pow(2,res);
            // Compute the size each block
            blocksize = linesize/blocksPerLine;
            // Compute how many blocks total
            nblocks = (int)Math.pow(blocksPerLine,2);
            // Initialize histogram arrays
            // For easier computation I just append them all together
            hists[0] = new int[nblocks*INTENSITIES];
            hists[1] = new int[nblocks*INTENSITIES];
            int histfill = 0;
            Arrays.fill(hists[0], 0);
            Arrays.fill(hists[1], 0);

            // Cycle for each block
            for (int nblock=0; nblock<nblocks; nblock++) {
                // Calculate row and column
                row = nblock/blocksPerLine;
                col = nblock%blocksPerLine;
                // Calculate histograms
                tmphists[0] = getBlockHist(data, linesize, blocksize, row, col);
                tmphists[1] = getBlockHist(m, linesize, blocksize, row, col);
                // Append them to the hists for this level
                for (int i=0; i<INTENSITIES; i++) {
                    hists[0][histfill] = tmphists[0][i];
                    hists[1][histfill] = tmphists[1][i];
                    histfill++;
                }
            }

            // Calculate weight for this resolution
            weight = ((double)res+1)/MAXRES;

            // Spacial Pyramid Match uses Histogram Intersection Kernel here
            similarity += weight * compare(hists[0], hists[1]);
        }

        // Finally...
        return similarity;
    }
}
