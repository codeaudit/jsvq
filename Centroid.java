
class Centroid {
    double[] data;
    int size, ntrains;

    public Centroid(int size) {
        this.size = size;
        this.ntrains = 1;
        this.data = new double[size];
        for (int i = 0; i < size; i++) { data[i] = Math.random(); }
    }

    public void checkSize(double[] vec) {
        if (vec.length != size) {
            throw new RuntimeException(
                "Input vector length does not match centroid size.");
        }
    }

    public double lrate() {
        // (linearly decaying) learning rate
        return 1/ntrains;
    }

    public void train(double[] vec) {
        checkSize(vec);
        for (int i=0; i<vec.length; i++) {
            data[i] = data[i]*(1-lrate()) + vec[i]*lrate();
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
        double ret = 0;
        for (int i=0; i<size; i++) {
            ret += data[i] * vec[i];
        }
        return ret;
    }
}
