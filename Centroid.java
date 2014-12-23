
class Centroid {
    double[] data;
    int size, ntrains;

    int MAXTRAINS=100;
    double MINLRATE=1d/MAXTRAINS;

    public Centroid(int size) {
        this.size = size;
        this.ntrains = 1;
        this.data = new double[size];
        // TESTING WHITE CENTROIDS - MATCH ALL!!
        // for (int i = 0; i < size; i++) { data[i] = 1d; }
        for (int i = 0; i < size; i++) { data[i] = Math.random(); }
    }

    // Quick error check for vector size consistency
    public void checkSize(double[] vec) {
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

    public void train(double[] vec) {
        checkSize(vec);
        for (int i=0; i<size; i++) {
            data[i] = (1-lrate())*data[i] + lrate()*vec[i];
        }
        ntrains++;
    }

    public double[] getData() {
        return data;
    }

    public double similarity(Centroid c) {
        return similarity(c.getData());
    }

    public double similarity(double[] vec) {
        checkSize(vec);
        return simpleDotProduct(vec);
        // return squareError(vec);
    }

    // SIMILARITY MEASURES

    public double simpleDotProduct(double[] vec) {
        double ret = 0;
        for (int i=0; i<size; i++) {
            ret += data[i] * vec[i];
        }
        return ret/vec.length;
    }

    public double squareError(double[] vec) {
        double ret = 0;
        for (int i=0; i<size; i++) {
            ret += Math.pow(data[i] - vec[i], 2);
        }
        return ret/vec.length;
    }

}
