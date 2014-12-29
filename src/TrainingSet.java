// Automatically maintained training set for SVQ autotraining
package SVQ;

import java.util.Collections;
import java.util.List;
import java.util.ArrayList;

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

    // @Override
    // public String toString(){
    //     return this.id;
    // }
}

public class TrainingSet {
    // Holds the images of the test set
        // Higher similarity -> better reconstruction -> lower novelty.
        // List is sorted on sim DESC - `list.get(0).sim == maxSim` holds.
        // Elements are added to tail, then list is re-sorted.
    List<SortableVec> list;
    // Maximum size of test set
    int maxsize;
    // Maximum similarity to enter the test set:
    // we want the _least_ similar vecs to be kept
    double maxSim;

    public TrainingSet(int maxsize) {
        this.maxsize = maxsize;
        reset();
    }

    public void reset() {
        this.list = new ArrayList<SortableVec>(maxsize);
        this.maxSim = Double.MAX_VALUE;
    }


    public void add(int[] vec, double sim) {
        add(makeSV(vec,sim));
    }

    public void add(SortableVec svec) {
        // add to end of list
        list.add(svec);
        Collections.sort(list);
    }

    public void trim() {
        while (list.size()>maxsize) {
            // remove from front
            list.remove(0);
            // new "first" has highest similarity
            maxSim = list.get(0).sim;
        }
    }

    public SortableVec makeSV(int[] vec, double sim) {
        return new SortableVec(vec, sim);
    }

    public void tryAdd(SortableVec svec) {
        if (svec.sim<maxSim) {
            add(svec);
            trim();
        }
    }

    public void tryAdd(int[] vec, double sim) {
        if (sim<maxSim) {
            add(makeSV(vec, sim));
            trim();
        }
    }

    public int[][] getVecs() {
        @SuppressWarnings("unchecked")
        int[][] ret = new int[list.size()][];
        for (int i=0; i<list.size(); i++) {
            ret[i] = list.get(i).vec;
        }
        return ret;
    }

    // mostly for debugging purpose
    public double[] getSims() {
        double[] ret = new double[list.size()];
        for (int i=0; i<list.size(); i++) {
            ret[i] = list.get(i).sim;
        }
        return ret;
    }

    public int[][] returnVecsAndReset() {
        int[][] ret = getVecs();
        reset();
        return ret;
    }

    public static void main(String[] args) {
        // Test if the sorting is correct - should be DESC
        TrainingSet ts = new TrainingSet(3);
        int[] vals = {0,0,0};
        ts.add(vals, 2.0);
        ts.add(vals, 1.0);
        ts.add(vals, 3.0);
        double[] sims = ts.getSims();
        for (int i=0; i<sims.length; i++) {
            System.out.println(sims[i]);
        }
    }
}
