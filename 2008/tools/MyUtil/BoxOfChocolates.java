package MyUtil;

import java.util.Vector;

public class BoxOfChocolates<V> {
    Vector<Pair<Double, V>> chocolates = new Vector();
    double max = 0;

    public BoxOfChocolates() {

    }

    public void add(V v, double weight) {
        chocolates.add(new Pair<Double, V>(weight, v));
        max += weight;
    }
    
    public int size() {
        return chocolates.size();
    }
    
    public boolean remove(V v) {
        for (int i = 0; i < chocolates.size(); i++) {
            Pair<Double, V> pair = chocolates.get(i);
            if (pair.right == v) {
                chocolates.remove(i);
                max -= pair.left;
                return true;
            }
        }
        return false;
    }

    public V grab() {
        double r = U.r.nextDouble() * max;
        for (int i = 0; i < chocolates.size(); i++) {
            Pair<Double, V> pair = chocolates.get(i);
            double weight = pair.left;
            if (r < weight) {
                chocolates.remove(i);
                max -= weight;
                return pair.right;            
            }
            r -= weight;
        }
        return null;
    }
}
