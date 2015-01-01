package SVQ;

// Per-individual list of most novel images
// No need for synchronization since it's one per individual

import java.util.Collections;
import java.util.ArrayList;


class MostNovelImagesList {
    int maxsize;
    ArrayList<SortableVec> list;
    // Maximum similarity to enter the test set:
    // we want the _least_ similar vecs to be kept
    double maxsim;

    public MostNovelImagesList(int maxsize) {
        this.maxsize = maxsize;
        this.list    = new ArrayList<SortableVec>(maxsize);
        this.maxsim  = Double.MAX_VALUE;
    }

    public void add(int[] vec, double sim) {
        add(new SortableVec(vec, sim));
    }

    public void add(SortableVec svec) {
        // add to end of currentlist
        list.add(svec);
        Collections.sort(list);
    }

    public void tryAdd(int[] vec, double sim) {
        if (sim<maxsim) {
            add(vec, sim);
            trim();
        }
    }

    public void trim() {
        while (list.size()>maxsize) {
            // remove from front
            list.remove(0);
            // new "first" has highest similarity
            maxsim = list.get(0).sim;
        }
    }

    public int[][] flush() {

        // ArrayList<SortableVec> list

        int[][] ret = new int[list.size()][];
        for (int i=0; i<list.size(); i++) {
            ret[i] = list.get(i).vec;
        }
        list.clear();
        return ret;
    }
}
