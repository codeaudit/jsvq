// Automatically maintained training set for SVQ autotraining

package SVQ;

// INTERFACE:
// I maximized simplicity and transparency.
// - `tryAdd(id, vec)`
//     Each SVQ coding should call this to candidate the vec in his list.
// - `flush()`
//     Retrieve the training set so far and reset the state - call at end of gen.

import java.util.HashMap;
import java.util.Arrays;

public class TrainingSet {

    // Max number of images to import from each individual
    int nImgsPerImport;
    // Holds the images currently considered for the test set for each individual
    // Each list is hashed by the individual's ID
        // Higher similarity -> better reconstruction -> lower novelty.
        // List is sorted on sim DESC - `hash.get(0).sim == maxSim` holds.
        // Elements are added to tail, then current is re-sorted.
    HashMap<String, MostNovelImagesList> hash;

    public TrainingSet(int nImgsPerImport) {
        this.nImgsPerImport = nImgsPerImport;
        this.hash = new HashMap<String, MostNovelImagesList>();
    }

    public void tryAdd(String id, int[] vec, double sim) {
        getOrAddList(id).tryAdd(vec, sim);
    }

    public MostNovelImagesList getOrAddList(String id) {
        MostNovelImagesList list = hash.get(id);
        if (list == null) {
            list = new MostNovelImagesList(nImgsPerImport);
            hash.put(id, list);
        }
        return list;
    }

    public int[][] flush() {
        // written low-level to limit image copies
        int[][] ret = new int[hash.size()*nImgsPerImport][];
        int retfill = 0;
        int[][] indlst;
        for (MostNovelImagesList l : hash.values()) {
            indlst = l.flush();
            for (int i=0; i<indlst.length; i++) {
                ret[retfill++] = indlst[i];
            }
        }
        hash.clear();
        return Arrays.copyOf(ret, retfill);
    }
}
