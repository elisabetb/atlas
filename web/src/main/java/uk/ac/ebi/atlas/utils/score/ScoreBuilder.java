package uk.ac.ebi.atlas.utils.score;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;
import java.util.Random;

public class ScoreBuilder {

    private List<List<BitSet>> sets = new ArrayList<List<BitSet>>(50);


    public List<List<BitSet>> getSets() {
        return sets;
    }

    public void initSets(int marksSize, int orgPartsNumber, int geneNumber) {
        for (int i = 0; i < marksSize; i++) {
            List<BitSet> tissueSets = new ArrayList<BitSet>(orgPartsNumber + 1);
            //            BitSet all = new BitSet(geneNumber);
            for (int j = 0; j < orgPartsNumber; j++) {
                BitSet bitSet = initBitSet(geneNumber);
                tissueSets.add(bitSet);
                //                all.and(bitSet);
            }
            //            tissueSets.add(orgPartsNumber, all);
            sets.add(tissueSets);
        }
    }

    private BitSet initBitSet(int size) {
        BitSet set = new BitSet(size);
        for (int i = 0; i < size; i++) {
            Random random = new Random();
            set.set(i, random.nextBoolean());
        }
        return set;
    }

    public List<Integer> counts(List<Integer> parts) {
        for (List<BitSet> set : sets) {
            BitSet result;
            for (BitSet bitSet : set) {
            }
        }
        return null;
    }

    private int countPerMark(List<BitSet> tissueSets, List<Integer> selected) {
        BitSet andSet = new BitSet(50000);
        BitSet notSet = new BitSet(50000);

        //        result.flip(0, 49999);
        //        System.out.println("result.cardinality() Start = " + result.cardinality());
        for (int i = 0; i < tissueSets.size(); i++) {
            BitSet bitSet = tissueSets.get(i);
            if (selected.contains(i)) {
                //add
                andSet.or(bitSet);
            } else {
                notSet.or(bitSet);
            }
        }
        andSet.andNot(notSet);
        return andSet.cardinality();
    }

    public static void main(String[] args) {
        ScoreBuilder builder = new ScoreBuilder();
        long timeStart = System.currentTimeMillis();
        builder.initSets(50, 16, 60000);
        long timeFinished = System.currentTimeMillis();
        System.out.println("init = " + (timeFinished - timeStart));

        timeStart = System.currentTimeMillis();
        ArrayList<Integer> selected = new ArrayList<Integer>();
        selected.add(1);
        selected.add(6);
        selected.add(8);
        for (List<BitSet> tissueSets : builder.sets) {
            int count = builder.countPerMark(tissueSets, selected);
            System.out.println("count = " + count);
        }
        timeFinished = System.currentTimeMillis();
        System.out.println("count time = " + (timeFinished - timeStart));


//        try {
//            FileOutputStream out = new FileOutputStream("bitset.out");
//            ObjectOutputStream oos = new ObjectOutputStream(out);
//            oos.writeObject(builder.getSets());
//            oos.flush();
//        } catch (Exception e) {
//            System.out.println("Problem serializing: " + e);
//        }

    }
}
