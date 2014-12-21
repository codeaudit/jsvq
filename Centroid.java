
class Centroid {
    double[] data;
    int size, ntrains;

    public Centroid(int size) {
        this.size = size;
        this.ntrains = 1;
        this.data = new double[size];
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
        // linearly decaying
        return 1d/ntrains;
    }

    public void train(double[] vec) {
        checkSize(vec);

        System.out.println("\tLR: "+lrate());
        double tot = 0;
        for (int i=0; i<size; i++) {
            tot += data[i];
        }
        System.out.println("\ttot pre:  "+ tot);

        for (int i=0; i<size; i++) {
            data[i] = data[i]*(1-lrate()) + vec[i]*lrate();
        }
        ntrains++;

        tot = 0;
        for (int i=0; i<size; i++) {
            tot += data[i];
        }
        System.out.println("\ttot post: "+ tot);
    }

    public double[] getData() {
        return data;
    }

    public double similarity(Centroid c) {
        return similarity(c.getData());
    }

    public double similarity(double[] vec) {
        checkSize(vec);
        // simple dot product
        double ret = 0;
        for (int i=0; i<size; i++) {
            ret += data[i] * vec[i];
        }
        return ret;
    }
}
