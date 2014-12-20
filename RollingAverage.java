
// NOTE: this version considers computing window encompassing all samples

class RollingAverage {
	double[] total;
	double min, max;
	int size, nsamples;

	public RollingAverage(int size) {
		this.size = size;
		this.nsamples = 0;
		this.total = new double[size];
		this.min = Double.MAX_VALUE;
		this.max = Double.MIN_VALUE;
		for (int i = 0; i < size; i++) { total[i] = 0d; }
	}

	public void add(double[] vec) {
		if (vec.length != size) { throw new RuntimeException("Lengths don't match!");}
		for (int i=0; i<vec.length; i++) {
			if (vec[i]<min) { min=vec[i]; }
			if (vec[i]>max) { max=vec[i]; }
			total[i] += vec[i];
		}
		nsamples++;
	}

	public double[] getAvg() {
		double[] ret = new double[size];
		for (int i=0; i<size; i++) {
			ret[i] = total[i]/nsamples;
		}
		return ret;
	}
}