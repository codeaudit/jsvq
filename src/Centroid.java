import java.util.Random;

class Centroid {
    short[] data;
    int size, ntrains;

    int MAXTRAINS=100;
    double MINLRATE=1d/MAXTRAINS;

    public Centroid(int size) {
        this.size = size;
        this.ntrains = 1;
        this.data = new short[size];
        Random random = new Random();
        for (int i = 0; i < size; i++) {
            // TESTING WHITE CENTROIDS - DOT MATCH ALL!!
            // data[i] = (short)255;
            data[i] = (short) random.nextInt(255+1);
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
        checkSize(vec);
        return shiftedDotProduct(vec);
        // return simpleDotProduct(vec);
        // return squareError(vec);
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
            // Shift both in range [-0.5,0.5]
            ret += (data[i]-0.5) * (vec[i]-0.5);
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

}
