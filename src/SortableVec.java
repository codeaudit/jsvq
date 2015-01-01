package SVQ;
// Just a class to easily sort vectors based on their similarity

// Java Generics gives problems to instantiate generic type arrays
// as in `T[][] ret = new T[list.size()][];`
// Since that's outside the scope of my work, I'll stick to hardcoded `int[]`s.
class SortableVec implements Comparable {
    public int[] vec;
    // I could cache all similarities here
    public double sim;

    public SortableVec(int[] vec, double sim) {
        this.vec = vec;
        this.sim = sim;
    }

    @Override
    public int compareTo(Object o) {
        SortableVec rhs = (SortableVec)o;
        // inverted for descending order
        if (sim < rhs.sim) {
            return 1;
        } else if (sim > rhs.sim) {
            return -1;
        } else {
            return 0;
        }
    }
}

