// Automatically maintained training set for SVQ autotraining
package SVQ;


// INTERFACE:
// - `tryAdd(vec)`: Each SVQ coding should call this to candidate the vec.
// - `flush()`: Save chosen vecs, call when an individual has finished.
// - `returnVecsAndReset()`: Retrieve all the vecs and reset the state,
//   call at end of generation.


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
}


// Usage: an individual codes images with SVQ, which holds a unique TrainingSet.
// Each time an image gets coded, it also tries to add it to the TS. Only the
// `maxsize` images with poorest reconstruction (lowest best-similarity) are
// kept in a `current` list. When the individual has finished its run, the
// `current` images are added to the `full` training set. This keeps going for a
// generation, until `full` contains `maxsize*popsize` images. At this point,
// SVQ retrieves the images with `retrieveAndReset()`, and trains on these
// images. The TS is fully reseted and ready for the next generation.
public class TrainingSet {
    // Holds the images currently considered for the test set
        // Higher similarity -> better reconstruction -> lower novelty.
        // List is sorted on sim DESC - `current.get(0).sim == maxSim` holds.
        // Elements are added to tail, then current is re-sorted.
    List<SortableVec> current;
    // Holds the images currently accepted for next training
    List<SortableVec> full;
    // Maximum numbers of images to add to full at next import
    int maxsize;
    // Maximum similarity to enter the test set:
    // we want the _least_ similar vecs to be kept
    double maxSim;

    public TrainingSet(int maxsize) {
        this.maxsize = maxsize;
        reset();
    }

    public void resetCurrent() {
        this.current = new ArrayList<SortableVec>(maxsize);
        this.maxSim = Double.MAX_VALUE;
    }

    public void resetFull(){
        this.full = new ArrayList<SortableVec>();
    }

    public void reset() {
        resetCurrent();
        resetFull();
    }

    public SortableVec makeSV(int[] vec, double sim) {
        return new SortableVec(vec, sim);
    }

    public void add(int[] vec, double sim) {
        add(makeSV(vec,sim));
    }

    public void add(SortableVec svec) {
        // add to end of currentlist
        current.add(svec);
        Collections.sort(current);
    }

    public void trim() {
        while (current.size()>maxsize) {
            // remove from front
            current.remove(0);
            // new "first" has highest similarity
            maxSim = current.get(0).sim;
        }
    }

    public void flush() {
        full.addAll(current);
        resetCurrent();
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

    public int[][] getCurrentVecs() {
        return getVecs(current);
    }

    public int[][] getFullVecs() {
        return getVecs(full);
    }

    public int[][] getVecs(List<SortableVec> lst) {
        int[][] ret = new int[lst.size()][];
        for (int i=0; i<lst.size(); i++) {
            ret[i] = lst.get(i).vec;
        }
        return ret;
    }

    public double[] getCurrentSims() {
        return getSims(current);
    }

    public double[] getFullSims() {
        return getSims(full);
    }

    // mostly for debugging purpose
    public double[] getSims(List<SortableVec> lst) {
        double[] ret = new double[lst.size()];
        for (int i=0; i<lst.size(); i++) {
            ret[i] = lst.get(i).sim;
        }
        return ret;
    }

    public int[][] returnVecsAndReset() {
        int[][] ret = getFullVecs();
        reset();
        return ret;
    }
}
