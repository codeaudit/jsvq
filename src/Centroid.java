import java.util.Random;
import java.util.Arrays;

class Centroid {
    short[] data;
    int size, ntrains;

    int INTENSITIES = 256;
    int MAXTRAINS = 100;
    double MINLRATE = 1d/MAXTRAINS;

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

    // Per-centroid learning rate
    public double lrate() {
        if (ntrains<MAXTRAINS) {
            // linearly decaying
            return 1d/ntrains;
        } else {
            // lower bound
            return MINLRATE;
        }
    }

    public void train(short[] vec) {
        checkSize(vec);
        for (int i=0; i<size; i++) {
            data[i] = (short) ((1-lrate())*data[i] + lrate()*vec[i]);
        }
        ntrains++;
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
        // return simpleDotProduct(vec);  //  0 31 30 39 / 100
        // return shiftedDotProduct(vec); // 27 48 30 50 / 155
        // return squareError(vec);       // 27 34 35 40 / 136
        // return simpleHistogram(vec);   // 27 24 30 43 / 124
        return multiresHistogram(vec);    // 21 47 30 49 / 147
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
        // Gives same importance to luminosity and darkness
        double ret = 0;
        for (int i=0; i<size; i++) {
            // Shift both in range [-0.5,0.5] (or [-128,127] for shorts)
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

    public int dot(int[] a, int[] b) {
        int ret = 0;
        for (int i=0; i<a.length; i++) {
            ret += a[i] * b[i];
        }
        return ret;
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
        return dot(hists[0],hists[1])/(double)INTENSITIES;
    }

    // TODO: compute (maintain) centroid histogram while training
    public double multiresHistogram(short[] vec) {
        int[][] hists = getHists(data, vec);
        double dottotal = 0, weight;

        int[][] tot = new int[2][];
        tot[0] = new int[INTENSITIES];
        tot[1] = new int[INTENSITIES];

        // Change resolution: start by all pixels independently,
        // then sum two, then sum 4...
        for (int resolution=INTENSITIES; resolution>=1; resolution/=2) {
            Arrays.fill(tot[0], (short)0);
            Arrays.fill(tot[1], (short)0);

            int i=0;
            // Now compute sums per each part in the resolution
            for (int border=0; border<INTENSITIES; border+=resolution) {
                // walk from where last parted ended until next border
                for (; i<border; i++) {
                    // the position where to write it does not matter, as long
                    // as we don't get out of the array and the position is the
                    // same in both arrays, the dot product will be fine
                    tot[0][border] += hists[0][i];
                    tot[1][border] += hists[1][i];
                }
            }

            // Each resolution calculation is weighted by its level of detail
            weight = ((double)resolution/INTENSITIES);
            // then compare them
            // TODO: try comparing with difference and square error
            // TODO: compute avg on moving window instead of blind sums
            dottotal += weight * dot(tot[0],tot[1]);
        }

        return dottotal;
    }

}
