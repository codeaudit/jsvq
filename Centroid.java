
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
        return shiftedDotProduct(vec);
        // return simpleDotProduct(vec);
        // return squareError(vec);
    }

    // SIMILARITY MEASURES

    public double simpleDotProduct(double[] vec) {
        // Only measures corresponding lighting
        double ret = 0;
        for (int i=0; i<size; i++) {
            ret += data[i] * vec[i];
        }
        return ret/vec.length;
    }

    public double shiftedDotProduct(double[] vec) {
        // Gives same importance to luminosity and darkness
        double ret = 0;
        for (int i=0; i<size; i++) {
            // Shift both in range [-0.5,0.5]
            ret += (data[i]-0.5) * (vec[i]-0.5);
        }
        return ret/vec.length;
    }

    public double squareError(double[] vec) {
        // Evergreen classic
        double ret = 0;
        for (int i=0; i<size; i++) {
            ret += Math.pow(data[i] - vec[i], 2);
        }
        return ret/vec.length;
    }

}
